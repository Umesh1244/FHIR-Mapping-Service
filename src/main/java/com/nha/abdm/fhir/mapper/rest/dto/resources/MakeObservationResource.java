/* (C) 2024 */
package com.nha.abdm.fhir.mapper.rest.dto.resources;

import com.nha.abdm.fhir.mapper.Utils;
import com.nha.abdm.fhir.mapper.rest.common.constants.BundleResourceIdentifier;
import com.nha.abdm.fhir.mapper.rest.common.constants.ResourceProfileIdentifier;
import com.nha.abdm.fhir.mapper.rest.database.h2.services.SnomedService;
import com.nha.abdm.fhir.mapper.rest.requests.helpers.ObservationResource;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.hl7.fhir.r4.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MakeObservationResource {

  @Autowired SnomedService snomedService;
  private static final Logger log = LoggerFactory.getLogger(MakeObservationResource.class);

  public Observation getObservation(
      Patient patient, List<Practitioner> practitionerList, ObservationResource observationResource)
      throws ParseException {

    String cleanedPatientName = Utils.clean(patient.getNameFirstRep().getText());

    Observation observation = new Observation();
    observation.setStatus(Observation.ObservationStatus.FINAL);

    observation.setCode(
        new CodeableConcept().setText(Utils.clean(observationResource.getObservation())));

    observation.setSubject(
        new Reference()
            .setReference(BundleResourceIdentifier.PATIENT + "/" + patient.getId())
            .setDisplay(cleanedPatientName));

    List<Reference> performerList = new ArrayList<>();
    for (Practitioner practitioner : practitionerList) {
      String cleanedPractitionerName = Utils.clean(practitioner.getNameFirstRep().getText());
      performerList.add(
          new Reference()
              .setReference(BundleResourceIdentifier.PRACTITIONER + "/" + practitioner.getId())
              .setDisplay(cleanedPractitionerName));
    }
    observation.setPerformer(performerList);

    if (Objects.nonNull(observationResource.getValueQuantity())) {
      observation.setValue(
          new Quantity()
              .setValue(observationResource.getValueQuantity().getValue())
              .setUnit(Utils.clean(observationResource.getValueQuantity().getUnit())));
    }

    if (Objects.nonNull(observation.getValueQuantity())
        && observationResource.getResult() != null) {
      observation.setValue(
          new CodeableConcept().setText(Utils.clean(observationResource.getResult())));
    }

    observation.setId(UUID.randomUUID().toString());
    observation.setMeta(
        new Meta()
            .setLastUpdatedElement(Utils.getCurrentTimeStamp())
            .addProfile(ResourceProfileIdentifier.PROFILE_OBSERVATION));

    return observation;
  }
}
