/* (C) 2024 */
package com.nha.abdm.fhir.mapper.rest.dto.resources;

import com.nha.abdm.fhir.mapper.Utils;
import com.nha.abdm.fhir.mapper.rest.common.constants.*;
import com.nha.abdm.fhir.mapper.rest.database.h2.services.SnomedService;
import java.text.ParseException;
import java.util.List;
import java.util.UUID;
import org.hl7.fhir.r4.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MakeAllergyToleranceResource {
  @Autowired SnomedService snomedService;

  public AllergyIntolerance getAllergy(
      Patient patient,
      List<Practitioner> practitionerList,
      String allergy,
      String authoredOn,
      String verificationStatusValue)
      throws ParseException {

    String cleanedAllergy = Utils.clean(allergy);
    String cleanedVerification = Utils.clean(verificationStatusValue);

    HumanName patientName = patient.getName().get(0);

    AllergyIntolerance allergyIntolerance = new AllergyIntolerance();
    allergyIntolerance.setId(UUID.randomUUID().toString());
    allergyIntolerance.setMeta(
        new Meta()
            .setLastUpdatedElement(Utils.getCurrentTimeStamp())
            .addProfile(ResourceProfileIdentifier.PROFILE_ALLERGY_INTOLERANCE));

    CodeableConcept code = new CodeableConcept();
    code.setText(cleanedAllergy);
    allergyIntolerance.setCode(code);

    Coding clinicalStatusCoding = new Coding();
    clinicalStatusCoding.setSystem(
        "http://terminology.hl7.org/CodeSystem/allergyintolerance-clinical");
    clinicalStatusCoding.setCode("active");
    clinicalStatusCoding.setDisplay("Active");

    CodeableConcept clinicalStatus = new CodeableConcept();
    clinicalStatus.addCoding(clinicalStatusCoding);
    allergyIntolerance.setClinicalStatus(clinicalStatus);

    Coding verificationStatusCoding = new Coding();
    verificationStatusCoding.setSystem(
        "http://terminology.hl7.org/CodeSystem/allergyintolerance-verification");

    String verificationCode = mapVerificationStatus(cleanedVerification);
    verificationStatusCoding.setCode(verificationCode);
    verificationStatusCoding.setDisplay(capitalizeFirstLetter(verificationCode));

    allergyIntolerance.setVerificationStatus(
        new CodeableConcept().addCoding(verificationStatusCoding));

    if (authoredOn != null)
      allergyIntolerance.setRecordedDateElement(Utils.getFormattedDateTime(authoredOn));

    allergyIntolerance.setType(AllergyIntolerance.AllergyIntoleranceType.ALLERGY);
    allergyIntolerance.setPatient(
        new Reference()
            .setReference(BundleResourceIdentifier.PATIENT + "/" + patient.getId())
            .setDisplay(Utils.clean(patientName.getText())));

    if (!practitionerList.isEmpty()) {
      Practitioner practitioner = practitionerList.get(0);
      allergyIntolerance.setRecorder(
          new Reference()
              .setReference(BundleResourceIdentifier.PRACTITIONER + "/" + practitioner.getId())
              .setDisplay(Utils.clean(practitioner.getName().get(0).getText())));
    }

    return allergyIntolerance;
  }

  private String mapVerificationStatus(String status) {
    if (status == null) {
      return "unconfirmed";
    }

    String lowerStatus = status.toLowerCase().trim();

    switch (lowerStatus) {
      case "confirmed":
      case "active":
        return "confirmed";
      case "refuted":
      case "denied":
      case "false":
        return "refuted";
      case "entered-in-error":
      case "error":
        return "entered-in-error";
      case "unconfirmed":
      case "unverified":
      case "provisional":
      default:
        return "unconfirmed";
    }
  }

  private String capitalizeFirstLetter(String str) {
    if (str == null || str.length() == 0) {
      return str;
    }
    return str.substring(0, 1).toUpperCase() + str.substring(1);
  }
}
