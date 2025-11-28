/* (C) 2024 */
package com.nha.abdm.fhir.mapper.rest.dto.resources;

import com.nha.abdm.fhir.mapper.Utils;
import com.nha.abdm.fhir.mapper.rest.common.constants.BundleResourceIdentifier;
import com.nha.abdm.fhir.mapper.rest.common.constants.ResourceProfileIdentifier;
import com.nha.abdm.fhir.mapper.rest.requests.helpers.ImmunizationResource;
import java.text.ParseException;
import java.util.*;
import org.hl7.fhir.r4.model.*;
import org.springframework.stereotype.Component;

@Component
public class MakeImmunizationResource {

  public Immunization getImmunization(
      Patient patient,
      java.util.List<Practitioner> practitionerList,
      Organization manufacturer,
      ImmunizationResource immunizationResource)
      throws ParseException {

    Immunization immunization = new Immunization();
    immunization.setId(UUID.randomUUID().toString());

    // Set profile
    immunization.setMeta(
        new Meta()
            .setLastUpdatedElement(Utils.getCurrentTimeStamp())
            .addProfile(ResourceProfileIdentifier.PROFILE_IMMUNIZATION));

    immunization.setStatus(Immunization.ImmunizationStatus.COMPLETED);

    // REQUIRED: Set patient reference
    if (Objects.nonNull(patient) && Objects.nonNull(patient.getId())) {
      immunization.setPatient(
          new Reference().setReference(BundleResourceIdentifier.PATIENT + "/" + patient.getId()));
    }

    // REQUIRED: Set occurrence date/time from the 'date' field
    if (Objects.nonNull(immunizationResource.getDate())) {
      try {
        immunization.setOccurrence(new DateTimeType(immunizationResource.getDate()));
      } catch (Exception e) {
        // If date parsing fails, use current timestamp
        immunization.setOccurrence(Utils.getCurrentTimeStamp());
      }
    } else {
      immunization.setOccurrence(Utils.getCurrentTimeStamp());
    }

    // REQUIRED: Set vaccine code from vaccineName
    if (Objects.nonNull(immunizationResource.getVaccineName())
        && !immunizationResource.getVaccineName().isEmpty()) {
      CodeableConcept vaccineCode = new CodeableConcept();
      vaccineCode.setText(immunizationResource.getVaccineName());
      immunization.setVaccineCode(vaccineCode);
    }

    // Optional: Set lot number if provided
    if (Objects.nonNull(immunizationResource.getLotNumber())
        && !immunizationResource.getLotNumber().isEmpty()) {
      immunization.setLotNumber(immunizationResource.getLotNumber());
    }

    // Optional: Set performer if practitioners are provided
    if (Objects.nonNull(practitionerList) && !practitionerList.isEmpty()) {
      for (Practitioner practitioner : practitionerList) {
        if (Objects.nonNull(practitioner.getId())) {
          immunization.addPerformer(
              new Immunization.ImmunizationPerformerComponent()
                  .setActor(
                      new Reference()
                          .setReference(
                              BundleResourceIdentifier.PRACTITIONER + "/" + practitioner.getId())));
        }
      }
    }

    // REMOVED: manufacturer field - not allowed in profile 6.5.0
    // REMOVED: protocolApplied field - not allowed in profile 6.5.0
    // Note: If you need to track manufacturer and dose number, consider adding them
    // as extensions or in a different resource

    return immunization;
  }
}
