/* (C) 2024 */
package com.nha.abdm.fhir.mapper.rest.dto.resources;

import com.nha.abdm.fhir.mapper.Utils;
import com.nha.abdm.fhir.mapper.rest.common.constants.*;
import com.nha.abdm.fhir.mapper.rest.database.h2.services.SnomedService;
import com.nha.abdm.fhir.mapper.rest.database.h2.tables.SnomedConditionProcedure;
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
    HumanName patientName = patient.getName().get(0);
    AllergyIntolerance allergyIntolerance = new AllergyIntolerance();
    allergyIntolerance.setId(UUID.randomUUID().toString());
    allergyIntolerance.setMeta(
        new Meta()
            .setLastUpdatedElement(Utils.getCurrentTimeStamp())
            .addProfile(ResourceProfileIdentifier.PROFILE_ALLERGY_INTOLERANCE));

    Coding coding = new Coding();
    SnomedConditionProcedure snomed = snomedService.getConditionProcedureCode(allergy);
    coding.setSystem(BundleUrlIdentifier.SNOMED_URL);
    coding.setCode(snomed.getCode());
    coding.setDisplay(snomed.getDisplay());
    CodeableConcept code = new CodeableConcept();
    code.addCoding(coding);
    code.setText(snomed.getDisplay());

    allergyIntolerance.setCode(code);
    Coding clinicalStatusCoding = new Coding();
    clinicalStatusCoding.setSystem(ResourceProfileIdentifier.PROFILE_ALLERGY_INTOLERANCE_SYSTEM);
    clinicalStatusCoding.setCode(BundleFieldIdentifier.ACTIVE);
    clinicalStatusCoding.setDisplay(BundleFieldIdentifier.ACTIVE);

    CodeableConcept clinicalStatus = new CodeableConcept();
    clinicalStatus.addCoding(clinicalStatusCoding);
    allergyIntolerance.setClinicalStatus(clinicalStatus);

    Coding verificationStatusCoding =
        new Coding()
            .setSystem("http://terminology.hl7.org/CodeSystem/allergyintolerance-verification")
            .setCode(verificationStatusValue)
            .setDisplay(
                verificationStatusValue.substring(0, 1).toUpperCase()
                    + verificationStatusValue.substring(1)); // e.g., confirmed → Confirmed
    allergyIntolerance.setVerificationStatus(
        new CodeableConcept().addCoding(verificationStatusCoding));

    if (authoredOn != null)
      allergyIntolerance.setRecordedDateElement(Utils.getFormattedDateTime(authoredOn));
    allergyIntolerance.setType(AllergyIntolerance.AllergyIntoleranceType.ALLERGY);
    allergyIntolerance.setPatient(
        new Reference()
            .setReference(BundleResourceIdentifier.PATIENT + "/" + patient.getId())
            .setDisplay(patientName.getText()));
    if (!(practitionerList.isEmpty())) {
      allergyIntolerance.setRecorder(
          new Reference()
              .setReference(
                  BundleResourceIdentifier.PRACTITIONER + "/" + practitionerList.get(0).getId())
              .setDisplay(patientName.getText()));
    }
    return allergyIntolerance;
  }
}
