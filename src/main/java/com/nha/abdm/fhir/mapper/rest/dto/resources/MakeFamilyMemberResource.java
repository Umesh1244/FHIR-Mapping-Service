/* (C) 2024 */
package com.nha.abdm.fhir.mapper.rest.dto.resources;

import com.nha.abdm.fhir.mapper.Utils;
import com.nha.abdm.fhir.mapper.rest.common.constants.BundleResourceIdentifier;
import com.nha.abdm.fhir.mapper.rest.common.constants.ResourceProfileIdentifier;
import com.nha.abdm.fhir.mapper.rest.database.h2.services.SnomedService;
import com.nha.abdm.fhir.mapper.rest.requests.helpers.FamilyObservationResource;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.Objects;
import java.util.UUID;
import org.hl7.fhir.r4.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MakeFamilyMemberResource {

  @Autowired SnomedService snomedService;

  public FamilyMemberHistory getFamilyHistory(
      Patient patient, FamilyObservationResource familyObservationResource) throws ParseException {
    HumanName patientName = patient.getName().get(0);
    FamilyMemberHistory familyMemberHistory = new FamilyMemberHistory();
    familyMemberHistory.setId(UUID.randomUUID().toString());
    familyMemberHistory.setStatus(FamilyMemberHistory.FamilyHistoryStatus.COMPLETED);
    familyMemberHistory.setMeta(
        new Meta()
            .setLastUpdatedElement(Utils.getCurrentTimeStamp())
            .addProfile(ResourceProfileIdentifier.PROFILE_FAMILY_MEMBER_HISTORY));
    familyMemberHistory.setPatient(
        new Reference()
            .setReference(BundleResourceIdentifier.PATIENT + "/" + patient.getId())
            .setDisplay(patientName.getText()));

    // FIX 1: Relationship - TEXT ONLY (remove addCoding)
    if (Objects.nonNull(familyObservationResource.getRelationship())) {
      familyMemberHistory.setRelationship(
          new CodeableConcept().setText(familyObservationResource.getRelationship()));
    }

    // Gender - Keep as is (this uses valid FHIR administrative-gender)
    String gender = familyObservationResource.getGender();
    if (Objects.nonNull(gender)) {
      CodeableConcept genderCodeableConcept = new CodeableConcept();
      String genderCode = mapGenderToFhirCode(gender);
      genderCodeableConcept
          .addCoding(
              new Coding()
                  .setSystem("http://hl7.org/fhir/administrative-gender")
                  .setCode(genderCode)
                  .setDisplay(capitalizeFirst(genderCode)))
          .setText(capitalizeFirst(genderCode));

      familyMemberHistory.setSex(genderCodeableConcept);
    }

    // FIX 2: Condition Code - TEXT ONLY (remove addCoding)
    if (Objects.nonNull(familyObservationResource.getObservation())) {
      FamilyMemberHistory.FamilyMemberHistoryConditionComponent conditionComponent =
          new FamilyMemberHistory.FamilyMemberHistoryConditionComponent()
              .setCode(new CodeableConcept().setText(familyObservationResource.getObservation()));

      // Handle contributedToDeath
      Boolean didContributeToDeath = familyObservationResource.getIsDeceased();
      if (didContributeToDeath != null) {
        conditionComponent.setContributedToDeath(didContributeToDeath);
      }

      // Handle age
      Long onsetAge = familyObservationResource.getAge();
      if (onsetAge != null && onsetAge > 0) {
        conditionComponent.setOnset(
            new Age()
                .setValue(new BigDecimal(onsetAge))
                .setUnit("years")
                .setSystem("http://unitsofmeasure.org")
                .setCode("a"));
      }

      familyMemberHistory.addCondition(conditionComponent);
    }

    familyMemberHistory.setDateElement(
        Utils.getFormattedDateTime(familyObservationResource.getDate()));
    return familyMemberHistory;
  }

  private String mapGenderToFhirCode(String gender) {
    if (gender == null) return "unknown";

    switch (gender.toLowerCase().trim()) {
      case "male":
      case "m":
        return "male";
      case "female":
      case "f":
        return "female";
      case "other":
      case "o":
        return "other";
      default:
        return "unknown";
    }
  }

  private String capitalizeFirst(String str) {
    if (str == null || str.isEmpty()) return str;
    return str.substring(0, 1).toUpperCase() + str.substring(1);
  }
}
