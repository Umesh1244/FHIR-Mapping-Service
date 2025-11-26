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
public class MakeHealthDocumentComposition {

  public Composition makeCompositionResource(
      Patient patient,
      String authoredOn,
      List<Practitioner> practitionerList,
      Organization organization,
      Encounter encounter,
      List<DocumentReference> documentReferenceList)
      throws ParseException {

    Composition composition = new Composition();

    Meta meta = new Meta();
    meta.setVersionId("1");
    meta.setLastUpdatedElement(Utils.getCurrentTimeStamp());
    meta.addProfile(ResourceProfileIdentifier.PROFILE_HEALTH_DOCUMENT_RECORD);
    composition.setMeta(meta);

    composition.setType(new CodeableConcept().setText(BundleCompositionIdentifier.RECORD_ARTIFACT));
    composition.setTitle(BundleCompositionIdentifier.HEALTH_DOCUMENT);

    Composition.SectionComponent section = new Composition.SectionComponent();
    section.setTitle(BundleCompositionIdentifier.RECORD_ARTIFACT);

    if (documentReferenceList != null && !documentReferenceList.isEmpty()) {
      for (DocumentReference doc : documentReferenceList) {
        section.addEntry(
            new Reference()
                .setReference(BundleResourceIdentifier.DOCUMENT_REFERENCE + "/" + doc.getId()));
      }
      composition.addSection(section);
    }

    if (encounter != null) {
      composition.setEncounter(
          new Reference()
              .setReference(BundleResourceIdentifier.ENCOUNTER + "/" + encounter.getId()));
    }

    List<Reference> authorList = new ArrayList<>();
    for (Practitioner practitioner : practitionerList) {
      HumanName pName = practitioner.getName().get(0);
      authorList.add(
          new Reference()
              .setReference(BundleResourceIdentifier.PRACTITIONER + "/" + practitioner.getId())
              .setDisplay(pName.getText()));
    }
    composition.setAuthor(authorList);

    if (organization != null) {
      composition.setCustodian(
          new Reference()
              .setReference(BundleResourceIdentifier.ORGANISATION + "/" + organization.getId())
              .setDisplay(organization.getName()));
    }

    HumanName patientName = patient.getName().get(0);
    composition.setSubject(
        new Reference()
            .setReference(BundleResourceIdentifier.PATIENT + "/" + patient.getId())
            .setDisplay(patientName.getText()));

    composition.setDateElement(Utils.getFormattedDateTime(authoredOn));
    composition.setStatus(Composition.CompositionStatus.FINAL);

    Identifier identifier = new Identifier();
    identifier.setSystem(BundleUrlIdentifier.WRAPPER_URL);
    identifier.setValue(UUID.randomUUID().toString());
    composition.setIdentifier(identifier);

    composition.setId(UUID.randomUUID().toString());

    return composition;
  }
}
