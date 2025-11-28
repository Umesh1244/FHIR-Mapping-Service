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
public class MakePrescriptionComposition {
  public Composition makeCompositionResource(
      Patient patient,
      List<Practitioner> practitionerList,
      Organization organization,
      String authoredOn,
      Encounter encounter,
      List<MedicationRequest> medicationRequestList,
      List<Binary> documentList)
      throws ParseException {

    Composition composition = new Composition();

    Meta meta = new Meta();
    meta.setVersionId("1");
    meta.setLastUpdatedElement(Utils.getCurrentTimeStamp());
    meta.addProfile(ResourceProfileIdentifier.PROFILE_PRESCRIPTION_RECORD);
    composition.setMeta(meta);

    // FIX: Add both coding and text for Composition type
    CodeableConcept typeCode = new CodeableConcept();
    typeCode.addCoding(
        new Coding()
            .setSystem(BundleUrlIdentifier.SNOMED_URL)
            .setCode("440545006")
            .setDisplay("Prescription record"));
    typeCode.setText(BundleCompositionIdentifier.PRESCRIPTION);
    composition.setType(typeCode);
    composition.setTitle(BundleCompositionIdentifier.PRESCRIPTION);

    if (organization != null) {
      composition.setCustodian(
          new Reference()
              .setReference(BundleResourceIdentifier.ORGANISATION + "/" + organization.getId()));
    }

    if (encounter != null) {
      composition.setEncounter(
          new Reference()
              .setReference(BundleResourceIdentifier.ENCOUNTER + "/" + encounter.getId()));
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

    Composition.SectionComponent medicationSection = new Composition.SectionComponent();
    medicationSection.setTitle(BundleResourceIdentifier.MEDICATIONS);

    // FIX 1: Add MedicationRequest entries with setType()
    if (medicationRequestList != null && !medicationRequestList.isEmpty()) {
      for (MedicationRequest mr : medicationRequestList) {
        medicationSection.addEntry(
            new Reference()
                .setReference(BundleResourceIdentifier.MEDICATION_REQUEST + "/" + mr.getId())
                .setType("MedicationRequest"));
      }
    }

    // FIX 2: Add Binary entries with setType()
    if (documentList != null && !documentList.isEmpty()) {
      for (Binary binary : documentList) {
        medicationSection.addEntry(
            new Reference()
                .setReference(BundleResourceIdentifier.BINARY + "/" + binary.getId())
                .setType("Binary"));
      }
    }

    if (!medicationSection.getEntry().isEmpty()) {
      composition.addSection(medicationSection);
    }

    composition.setStatus(Composition.CompositionStatus.FINAL);

    Identifier identifier = new Identifier();
    identifier.setSystem(BundleUrlIdentifier.WRAPPER_URL);
    identifier.setValue(UUID.randomUUID().toString());
    composition.setIdentifier(identifier);

    composition.setId(UUID.randomUUID().toString());

    return composition;
  }
}
