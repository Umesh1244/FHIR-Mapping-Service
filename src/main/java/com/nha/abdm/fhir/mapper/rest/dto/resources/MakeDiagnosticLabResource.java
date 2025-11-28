/* (C) 2024 */
package com.nha.abdm.fhir.mapper.rest.dto.resources;

import com.nha.abdm.fhir.mapper.Utils;
import com.nha.abdm.fhir.mapper.rest.common.constants.ResourceProfileIdentifier;
import com.nha.abdm.fhir.mapper.rest.database.h2.services.SnomedService;
import com.nha.abdm.fhir.mapper.rest.requests.helpers.DiagnosticResource;
import java.text.ParseException;
import java.util.List;
import java.util.UUID;
import org.hl7.fhir.r4.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MakeDiagnosticLabResource {

  @Autowired SnomedService snomedService;

  public DiagnosticReport getDiagnosticReport(
      Patient patient,
      List<Practitioner> practitionerList,
      List<Observation> observationList,
      Encounter encounter,
      DiagnosticResource diagnosticResource)
      throws ParseException {

    String serviceName = Utils.clean(diagnosticResource.getServiceName());
    String serviceCategory = Utils.clean(diagnosticResource.getServiceCategory());
    String conclusion = Utils.clean(diagnosticResource.getConclusion());

    DiagnosticReport diagnosticReport = new DiagnosticReport();
    diagnosticReport.setId(UUID.randomUUID().toString());
    diagnosticReport.setMeta(
        new Meta()
            .setLastUpdatedElement(Utils.getCurrentTimeStamp())
            .addProfile(ResourceProfileIdentifier.PROFILE_DIAGNOSTIC_REPORT_LAB));

    diagnosticReport.setStatus(DiagnosticReport.DiagnosticReportStatus.FINAL);
    diagnosticReport.setCode(new CodeableConcept().setText(serviceName));

    diagnosticReport.setSubject(
        new Reference()
            .setReference("Patient/" + patient.getId())
            .setDisplay(Utils.clean(patient.getNameFirstRep().getText())));

    if (encounter != null) {
      diagnosticReport.setEncounter(new Reference().setReference("Encounter/" + encounter.getId()));
    }

    for (Practitioner practitioner : practitionerList) {
      String ref = "Practitioner/" + practitioner.getId();
      diagnosticReport.addPerformer(new Reference(ref));
      diagnosticReport.addResultsInterpreter(new Reference(ref));
    }

    diagnosticReport.addCategory(new CodeableConcept().setText(serviceCategory));

    for (Observation obs : observationList) {
      diagnosticReport.addResult(new Reference("Observation/" + obs.getId()));
    }

    if (encounter != null && encounter.getPeriod() != null) {
      diagnosticReport.setIssued(encounter.getPeriod().getStart());
    }

    diagnosticReport.setConclusion(conclusion);
    diagnosticReport.addConclusionCode(new CodeableConcept().setText(conclusion));

    if (diagnosticResource.getPresentedForm() != null) {
      Attachment attachment = new Attachment();
      attachment.setContentType(
          Utils.clean(diagnosticResource.getPresentedForm().getContentType()));
      attachment.setData(diagnosticResource.getPresentedForm().getData());
      diagnosticReport.addPresentedForm(attachment);
    }

    return diagnosticReport;
  }
}
