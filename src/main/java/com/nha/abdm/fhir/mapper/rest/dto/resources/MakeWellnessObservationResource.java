/* (C) 2024 */
package com.nha.abdm.fhir.mapper.rest.dto.resources;

import com.nha.abdm.fhir.mapper.Utils;
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

    HumanName patientName = patient.getNameFirstRep();
    Observation observation = new Observation();
    observation.setStatus(Observation.ObservationStatus.FINAL);

    // TEXT ONLY - cleaned
    observation.setCode(
        new CodeableConcept().setText(Utils.clean(observationResource.getObservation())));

    // SUBJECT
    observation.setSubject(
        new Reference()
            .setReference(BundleResourceIdentifier.PATIENT + "/" + patient.getId())
            .setDisplay(Utils.clean(patientName.getText())));

    // PERFORMERS
    List<Reference> performerList = new ArrayList<>();
    for (Practitioner practitioner : practitionerList) {
      HumanName practitionerName = practitioner.getNameFirstRep();
      performerList.add(
          new Reference()
              .setReference(BundleResourceIdentifier.PRACTITIONER + "/" + practitioner.getId())
              .setDisplay(Utils.clean(practitionerName.getText())));
    }
    observation.setPerformer(performerList);

    // VALUE - Quantity OR CodeableConcept (text only)
    if (observationResource.getValueQuantity() != null) {
      observation.setValue(
          new Quantity()
              .setValue(observationResource.getValueQuantity().getValue())
              .setUnit(observationResource.getValueQuantity().getUnit()));
    } else if (observationResource.getResult() != null) {
      observation.setValue(
          new CodeableConcept().setText(Utils.clean(observationResource.getResult())));
    }

    observation.setId(UUID.randomUUID().toString());

    return observation;
  }
}
