/* (C) 2024 */
package com.nha.abdm.fhir.mapper.rest.dto.compositions;

import com.nha.abdm.fhir.mapper.Utils;
import com.nha.abdm.fhir.mapper.rest.common.constants.BundleCompositionIdentifier;
import com.nha.abdm.fhir.mapper.rest.common.constants.BundleResourceIdentifier;
import com.nha.abdm.fhir.mapper.rest.common.constants.BundleUrlIdentifier;
import com.nha.abdm.fhir.mapper.rest.common.constants.ResourceProfileIdentifier;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.hl7.fhir.r4.model.*;
import org.springframework.stereotype.Service;

@Service
public class MakeDiagnosticComposition {

  public Composition makeCompositionResource(
      Patient patient,
      String authoredOn,
      List<Practitioner> practitionerList,
      Organization organization,
      Encounter encounter,
      List<DiagnosticReport> diagnosticReportList,
      List<DocumentReference> documentReferenceList)
      throws ParseException {

    Composition composition = new Composition();

    // META
    Meta meta = new Meta();
    meta.setVersionId("1");
    meta.setLastUpdatedElement(Utils.getCurrentTimeStamp());
    meta.addProfile(ResourceProfileIdentifier.PROFILE_DIAGNOSTIC_REPORT);
    composition.setMeta(meta);

    // TYPE — TEXT ONLY (NO CODING)
    composition.setType(
        new CodeableConcept().setText(BundleCompositionIdentifier.DIAGNOSTIC_STUDIES_REPORT));
    composition.setTitle(BundleCompositionIdentifier.DIAGNOSTIC_STUDIES_REPORT);

    // SECTION — ADDED ONLY IF ENTRIES EXIST
    Composition.SectionComponent section = new Composition.SectionComponent();
    section.setTitle(BundleCompositionIdentifier.DIAGNOSTIC_STUDIES_REPORT);

    boolean hasEntries = false;

    if (diagnosticReportList != null) {
      for (DiagnosticReport diagnosticReport : diagnosticReportList) {
        section.addEntry(
            new Reference()
                .setReference(
                    BundleResourceIdentifier.DIAGNOSTIC_REPORT + "/" + diagnosticReport.getId()));
        hasEntries = true;
      }
    }

    if (documentReferenceList != null) {
      for (DocumentReference documentReference : documentReferenceList) {
        section.addEntry(
            new Reference()
                .setReference(
                    BundleResourceIdentifier.DOCUMENT_REFERENCE + "/" + documentReference.getId()));
        hasEntries = true;
      }
    }

    if (hasEntries) {
      composition.addSection(section);
    }

    // AUTHORS
    List<Reference> authorList = new ArrayList<>();
    for (Practitioner practitioner : practitionerList) {
      HumanName name = practitioner.getName().get(0);
      authorList.add(
          new Reference()
              .setReference(BundleResourceIdentifier.PRACTITIONER + "/" + practitioner.getId())
              .setDisplay(name.getText()));
    }
    composition.setAuthor(authorList);

    // CUSTODIAN
    if (organization != null) {
      composition.setCustodian(
          new Reference()
              .setReference(BundleResourceIdentifier.ORGANISATION + "/" + organization.getId())
              .setDisplay(organization.getName()));
    }

    // ENCOUNTER
    if (encounter != null) {
      composition.setEncounter(
          new Reference()
              .setReference(BundleResourceIdentifier.ENCOUNTER + "/" + encounter.getId()));
    }

    // SUBJECT (PATIENT)
    HumanName patientName = patient.getName().get(0);
    composition.setSubject(
        new Reference()
            .setReference(BundleResourceIdentifier.PATIENT + "/" + patient.getId())
            .setDisplay(patientName.getText()));

    // DATE
    composition.setDateElement(Utils.getFormattedDateTime(authoredOn));

    // STATUS
    composition.setStatus(Composition.CompositionStatus.FINAL);

    // IDENTIFIER
    Identifier identifier = new Identifier();
    identifier.setSystem(BundleUrlIdentifier.WRAPPER_URL);
    identifier.setValue(UUID.randomUUID().toString());
    composition.setIdentifier(identifier);

    composition.setId(UUID.randomUUID().toString());

    return composition;
  }
}
