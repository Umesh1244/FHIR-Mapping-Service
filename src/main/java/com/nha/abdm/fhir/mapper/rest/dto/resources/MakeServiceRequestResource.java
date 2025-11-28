/* (C) 2024 */
package com.nha.abdm.fhir.mapper.rest.dto.resources;

import com.nha.abdm.fhir.mapper.Utils;
import com.nha.abdm.fhir.mapper.rest.common.constants.BundleResourceIdentifier;
import com.nha.abdm.fhir.mapper.rest.common.constants.ResourceProfileIdentifier;
import com.nha.abdm.fhir.mapper.rest.database.h2.services.SnomedService;
import com.nha.abdm.fhir.mapper.rest.database.h2.tables.SnomedDiagnostic;
import com.nha.abdm.fhir.mapper.rest.requests.helpers.ServiceRequestResource;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.hl7.fhir.r4.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MakeServiceRequestResource {

  @Autowired SnomedService snomedService;

  public ServiceRequest getServiceRequest(
      Patient patient,
      List<Practitioner> practitionerList,
      ServiceRequestResource serviceRequestResource,
      String authoredOn)
      throws ParseException {

    HumanName patientName = patient.getNameFirstRep();

    ServiceRequest serviceRequest = new ServiceRequest();
    serviceRequest.setId(UUID.randomUUID().toString());
    serviceRequest.setStatus(ServiceRequest.ServiceRequestStatus.ACTIVE);
    serviceRequest.setIntent(ServiceRequest.ServiceRequestIntent.PROPOSAL);
    serviceRequest.setAuthoredOnElement(Utils.getFormattedDateTime(authoredOn));
    serviceRequest.setMeta(
        new Meta()
            .setLastUpdatedElement(Utils.getCurrentTimeStamp())
            .addProfile(ResourceProfileIdentifier.PROFILE_SERVICE_REQUEST));

    // SNOMED mapping (optional for future coding)
    SnomedDiagnostic snomedDiagnostic =
        snomedService.getSnomedDiagnosticCode(serviceRequestResource.getDetails());

    // TEXT ONLY CODE
    serviceRequest.setCode(
        new CodeableConcept().setText(Utils.clean(serviceRequestResource.getDetails())));

    // SUBJECT
    serviceRequest.setSubject(
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

    if (!performerList.isEmpty()) {
      Practitioner requesterPractitioner = practitionerList.get(0);
      HumanName requesterName = requesterPractitioner.getNameFirstRep();
      serviceRequest.setRequester(
          new Reference()
              .setReference(
                  BundleResourceIdentifier.PRACTITIONER + "/" + requesterPractitioner.getId())
              .setDisplay(Utils.clean(requesterName.getText())));
    }

    serviceRequest.setPerformer(performerList);

    // SPECIMEN
    if (serviceRequestResource.getSpecimen() != null) {
      serviceRequest.addSpecimen(
          new Reference()
              .setDisplay(
                  Utils.clean(
                      snomedService
                          .getSnomedSpecimenCode(serviceRequestResource.getSpecimen())
                          .getDisplay())));
    }

    return serviceRequest;
  }
}
