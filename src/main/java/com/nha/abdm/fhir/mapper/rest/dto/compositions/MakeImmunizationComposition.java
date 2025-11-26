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
public class MakeImmunizationComposition {

  public Composition makeCompositionResource(
      Patient patient,
      List<Practitioner> practitionerList,
      Organization organization,
      String authoredOn,
      List<Immunization> immunizationList,
      List<DocumentReference> documentList,
      Encounter encounter)
      throws ParseException {

    Composition composition = new Composition();

    Meta meta = new Meta();
    meta.setVersionId("1");
    meta.setLastUpdatedElement(Utils.getCurrentTimeStamp());
    meta.addProfile(ResourceProfileIdentifier.PROFILE_IMMUNIZATION);
    composition.setMeta(meta);

    composition.setType(
        new CodeableConcept().setText(BundleCompositionIdentifier.IMMUNIZATION_RECORD));
    composition.setTitle(BundleCompositionIdentifier.IMMUNIZATION_RECORD);

    if (organization != null) {
      composition.setCustodian(
          new Reference()
              .setReference(BundleResourceIdentifier.ORGANISATION + "/" + organization.getId()));
    }

    List<Reference> authorList = new ArrayList<>();
    for (Practitioner practitioner : practitionerList) {
      HumanName name = practitioner.getName().get(0);
      authorList.add(
          new Reference()
              .setReference(BundleResourceIdentifier.PRACTITIONER + "/" + practitioner.getId())
              .setDisplay(name.getText()));
    }
    composition.setAuthor(authorList);

    HumanName patientName = patient.getName().get(0);
    composition.setSubject(
        new Reference()
            .setReference(BundleResourceIdentifier.PATIENT + "/" + patient.getId())
            .setDisplay(patientName.getText()));

    composition.setDateElement(Utils.getFormattedDateTime(authoredOn));

    Composition.SectionComponent immunizationSection = new Composition.SectionComponent();
    immunizationSection.setTitle(BundleCompositionIdentifier.IMMUNIZATION_RECORD);

    if (immunizationList != null && !immunizationList.isEmpty()) {
      for (Immunization immunization : immunizationList) {
        immunizationSection.addEntry(
            new Reference()
                .setReference(BundleResourceIdentifier.IMMUNIZATION + "/" + immunization.getId()));
      }
    }

    if (documentList != null && !documentList.isEmpty()) {
      for (DocumentReference doc : documentList) {
        immunizationSection.addEntry(
            new Reference()
                .setReference(BundleResourceIdentifier.DOCUMENT_REFERENCE + "/" + doc.getId()));
      }
    }

    if (!immunizationSection.getEntry().isEmpty()) {
      composition.addSection(immunizationSection);
    }

    composition.setStatus(Composition.CompositionStatus.FINAL);

    Identifier identifier = new Identifier();
    identifier.setSystem(BundleUrlIdentifier.WRAPPER_URL);
    identifier.setValue(UUID.randomUUID().toString());
    composition.setIdentifier(identifier);

    composition.setId(UUID.randomUUID().toString());

    if (encounter != null) {
      composition.setEncounter(
          new Reference()
              .setReference(BundleResourceIdentifier.ENCOUNTER + "/" + encounter.getId()));
    }

    return composition;
  }
}
