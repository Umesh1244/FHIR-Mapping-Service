/* (C) 2024 */
package com.nha.abdm.fhir.mapper.rest.dto.resources;

import com.nha.abdm.fhir.mapper.rest.common.constants.BundleResourceIdentifier;
import com.nha.abdm.fhir.mapper.rest.database.h2.services.SnomedService;
import com.nha.abdm.fhir.mapper.rest.requests.helpers.WellnessObservationResource;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.hl7.fhir.r4.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MakeWellnessObservationResource {
  @Autowired SnomedService snomedService;

  public Observation getObservation(
      Patient patient,
      List<Practitioner> practitionerList,
      WellnessObservationResource observationResource,
      String type) {
    HumanName patientName = patient.getName().get(0);
    Observation observation = new Observation();
    observation.setStatus(Observation.ObservationStatus.FINAL);

    // FIX: TEXT ONLY - No SNOMED coding to avoid validation errors
    CodeableConcept typeCode = new CodeableConcept();
    typeCode.setText(observationResource.getObservation());
    observation.setCode(typeCode);

    observation.setSubject(
        new Reference()
            .setReference(BundleResourceIdentifier.PATIENT + "/" + patient.getId())
            .setDisplay(patientName.getText()));

    List<Reference> performerList = new ArrayList<>();
    HumanName practitionerName = null;
    for (Practitioner practitioner : practitionerList) {
      practitionerName = practitioner.getName().get(0);
      performerList.add(
          new Reference()
              .setReference(BundleResourceIdentifier.PRACTITIONER + "/" + practitioner.getId())
              .setDisplay(practitionerName.getText()));
    }
    observation.setPerformer(performerList);

    if (observationResource.getValueQuantity() != null) {
      observation.setValue(
          new Quantity()
              .setValue(observationResource.getValueQuantity().getValue())
              .setUnit(observationResource.getValueQuantity().getUnit()));
    }

    if (observationResource.getResult() != null) {
      observation.setValue(new CodeableConcept().setText(observationResource.getResult()));
      observation.setValue(new StringType(observationResource.getResult()));
    }

    observation.setId(UUID.randomUUID().toString());
    return observation;
  }
}
