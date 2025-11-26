/* (C) 2024 */
package com.nha.abdm.fhir.mapper.rest.dto.compositions;

import com.nha.abdm.fhir.mapper.Utils;
import com.nha.abdm.fhir.mapper.rest.common.constants.BundleCompositionIdentifier;
import com.nha.abdm.fhir.mapper.rest.common.constants.BundleResourceIdentifier;
import com.nha.abdm.fhir.mapper.rest.common.constants.BundleUrlIdentifier;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.hl7.fhir.r4.model.*;
import org.springframework.stereotype.Service;

@Service
public class MakeWellnessComposition {
  public Composition makeWellnessComposition(
      Patient patient,
      String authoredOn,
      Encounter encounter,
      List<Practitioner> practitionerList,
      Organization organization,
      List<Observation> vitalSignsList,
      List<Observation> bodyMeasurementList,
      List<Observation> physicalActivityList,
      List<Observation> generalAssessmentList,
      List<Observation> womanHealthList,
      List<Observation> lifeStyleList,
      List<Observation> otherObservationList,
      List<DocumentReference> documentReferenceList)
      throws ParseException {
    HumanName patientName = patient.getName().get(0);
    HumanName practitionerName = null;
    Composition composition = new Composition();
    composition.setStatus(Composition.CompositionStatus.FINAL);
    composition.setType(new CodeableConcept().setText(BundleCompositionIdentifier.WELLNESS_RECORD));
    composition.setTitle(BundleCompositionIdentifier.WELLNESS_RECORD);
    List<Reference> authorList = new ArrayList<>();
    for (Practitioner practitioner : practitionerList) {
      practitionerName = practitioner.getName().get(0);
      authorList.add(
          new Reference()
              .setReference(BundleResourceIdentifier.PRACTITIONER + "/" + practitioner.getId())
              .setDisplay(practitionerName != null ? practitionerName.getText() : null));
    }
    composition.setEncounter(
        new Reference().setReference(BundleResourceIdentifier.ENCOUNTER + "/" + encounter.getId()));
    composition.setCustodian(
        new Reference()
            .setReference(BundleResourceIdentifier.ORGANISATION + "/" + organization.getId())
            .setDisplay(organization.getName()));
    composition.setAuthor(authorList);
    composition.setSubject(
        new Reference()
            .setReference(BundleResourceIdentifier.PATIENT + "/" + patient.getId())
            .setDisplay(patientName.getText()));
    composition.setDateElement(Utils.getFormattedDateTime(authoredOn));
    List<Composition.SectionComponent> sectionComponentList =
        makeCompositionSection(
            patient,
            encounter,
            practitionerList,
            organization,
            vitalSignsList,
            bodyMeasurementList,
            physicalActivityList,
            generalAssessmentList,
            womanHealthList,
            lifeStyleList,
            otherObservationList,
            documentReferenceList);
    if (Objects.nonNull(sectionComponentList))
      for (Composition.SectionComponent sectionComponent : sectionComponentList)
        composition.addSection(sectionComponent);
    Identifier identifier = new Identifier();
    identifier.setSystem(BundleUrlIdentifier.WRAPPER_URL);
    identifier.setValue(UUID.randomUUID().toString());
    composition.setIdentifier(identifier);
    composition.setId(UUID.randomUUID().toString());
    return composition;
  }

  private List<Composition.SectionComponent> makeCompositionSection(
      Patient patient,
      Encounter encounter,
      List<Practitioner> practitionerList,
      Organization organization,
      List<Observation> vitalSignsList,
      List<Observation> bodyMeasurementList,
      List<Observation> physicalActivityList,
      List<Observation> generalAssessmentList,
      List<Observation> womanHealthList,
      List<Observation> lifeStyleList,
      List<Observation> otherObservationList,
      List<DocumentReference> documentReferenceList) {

    List<Composition.SectionComponent> sectionComponentList = new ArrayList<>();

    if (vitalSignsList != null && !vitalSignsList.isEmpty()) {
      Composition.SectionComponent s = new Composition.SectionComponent();
      s.setTitle(BundleCompositionIdentifier.VITAL_SIGNS);
      for (Observation obs : vitalSignsList) {
        s.addEntry(
            new Reference().setReference(BundleResourceIdentifier.VITAL_SIGNS + "/" + obs.getId()));
      }
      sectionComponentList.add(s);
    }

    if (bodyMeasurementList != null && !bodyMeasurementList.isEmpty()) {
      Composition.SectionComponent s = new Composition.SectionComponent();
      s.setTitle(BundleCompositionIdentifier.BODY_MEASUREMENT);
      for (Observation obs : bodyMeasurementList) {
        s.addEntry(
            new Reference()
                .setReference(BundleResourceIdentifier.BODY_MEASUREMENT + "/" + obs.getId()));
      }
      sectionComponentList.add(s);
    }

    if (physicalActivityList != null && !physicalActivityList.isEmpty()) {
      Composition.SectionComponent s = new Composition.SectionComponent();
      s.setTitle(BundleCompositionIdentifier.PHYSICAL_ACTIVITY);
      for (Observation obs : physicalActivityList) {
        s.addEntry(
            new Reference()
                .setReference(BundleResourceIdentifier.PHYSICAL_ACTIVITY + "/" + obs.getId()));
      }
      sectionComponentList.add(s);
    }

    if (generalAssessmentList != null && !generalAssessmentList.isEmpty()) {
      Composition.SectionComponent s = new Composition.SectionComponent();
      s.setTitle(BundleCompositionIdentifier.GENERAL_ASSESSMENT);
      for (Observation obs : generalAssessmentList) {
        s.addEntry(
            new Reference()
                .setReference(BundleResourceIdentifier.GENERAL_ASSESSMENT + "/" + obs.getId()));
      }
      sectionComponentList.add(s);
    }

    if (womanHealthList != null && !womanHealthList.isEmpty()) {
      Composition.SectionComponent s = new Composition.SectionComponent();
      s.setTitle(BundleCompositionIdentifier.WOMEN_HEALTH);
      for (Observation obs : womanHealthList) {
        s.addEntry(
            new Reference()
                .setReference(BundleResourceIdentifier.WOMAN_HEALTH + "/" + obs.getId()));
      }
      sectionComponentList.add(s);
    }

    if (lifeStyleList != null && !lifeStyleList.isEmpty()) {
      Composition.SectionComponent s = new Composition.SectionComponent();
      s.setTitle(BundleCompositionIdentifier.LIFE_STYLE);
      for (Observation obs : lifeStyleList) {
        s.addEntry(
            new Reference().setReference(BundleResourceIdentifier.LIFE_STYLE + "/" + obs.getId()));
      }
      sectionComponentList.add(s);
    }

    if (otherObservationList != null && !otherObservationList.isEmpty()) {
      Composition.SectionComponent s = new Composition.SectionComponent();
      s.setTitle(BundleCompositionIdentifier.OTHER_OBSERVATIONS);
      for (Observation obs : otherObservationList) {
        s.addEntry(
            new Reference()
                .setReference(BundleResourceIdentifier.OTHER_OBSERVATIONS + "/" + obs.getId()));
      }
      sectionComponentList.add(s);
    }

    if (documentReferenceList != null && !documentReferenceList.isEmpty()) {
      Composition.SectionComponent s = new Composition.SectionComponent();
      s.setTitle(BundleCompositionIdentifier.DOCUMENT_REFERENCE);
      for (DocumentReference doc : documentReferenceList) {
        s.addEntry(
            new Reference()
                .setReference(BundleResourceIdentifier.DOCUMENT_REFERENCE + "/" + doc.getId()));
      }
      sectionComponentList.add(s);
    }

    return sectionComponentList;
  }
}
