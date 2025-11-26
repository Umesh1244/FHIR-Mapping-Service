/* (C) 2024 */
package com.nha.abdm.fhir.mapper.rest.dto.compositions;

import com.nha.abdm.fhir.mapper.Utils;
import com.nha.abdm.fhir.mapper.rest.common.constants.BundleCompositionIdentifier;
import com.nha.abdm.fhir.mapper.rest.common.constants.BundleResourceIdentifier;
import com.nha.abdm.fhir.mapper.rest.common.constants.BundleUrlIdentifier;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.hl7.fhir.r4.model.*;
import org.springframework.stereotype.Service;

@Service
public class MakeOpComposition {
  public Composition makeOPCompositionResource(
      Patient patient,
      String visitDate,
      Encounter encounter,
      List<Practitioner> practitionerList,
      Organization organization,
      List<Condition> chiefComplaintList,
      List<Observation> physicalObservationList,
      List<AllergyIntolerance> allergieList,
      List<MedicationRequest> medicationList,
      List<Condition> medicalHistoryList,
      List<FamilyMemberHistory> familyMemberHistoryList,
      List<ServiceRequest> investigationAdviceList,
      List<Appointment> followupList,
      List<Procedure> procedureList,
      List<ServiceRequest> referralList,
      List<Observation> otherObservationList,
      List<DocumentReference> documentReferenceList)
      throws ParseException {
    HumanName patientName = patient.getName().get(0);
    HumanName practitionerName = null;
    Composition composition = new Composition();
    CodeableConcept typeCode = new CodeableConcept();
    Coding typeCoding = new Coding();
    typeCoding.setSystem(BundleUrlIdentifier.SNOMED_URL);
    typeCoding.setCode(BundleCompositionIdentifier.CLINICAL_CONSULTATION_REPORT_CODE);
    typeCoding.setDisplay(BundleCompositionIdentifier.CLINICAL_CONSULTATION_REPORT);
    typeCode.addCoding(typeCoding);
    composition.setType(typeCode);
    composition.setTitle(BundleCompositionIdentifier.CLINICAL_CONSULTATION_REPORT);
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
    composition.setDateElement(Utils.getFormattedDateTime(visitDate));
    composition.setStatus(Composition.CompositionStatus.FINAL);
    List<Composition.SectionComponent> sectionComponentList =
        makeCompositionSection(
            patient,
            practitionerList,
            organization,
            chiefComplaintList,
            physicalObservationList,
            allergieList,
            medicationList,
            medicalHistoryList,
            familyMemberHistoryList,
            investigationAdviceList,
            followupList,
            procedureList,
            referralList,
            otherObservationList,
            documentReferenceList);
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
      List<Practitioner> practitionerList,
      Organization organization,
      List<Condition> chiefComplaintList,
      List<Observation> physicalObservationList,
      List<AllergyIntolerance> allergieList,
      List<MedicationRequest> medicationList,
      List<Condition> medicalHistoryList,
      List<FamilyMemberHistory> familyMemberHistoryList,
      List<ServiceRequest> investigationAdviceList,
      List<Appointment> followupList,
      List<Procedure> procedureList,
      List<ServiceRequest> referralList,
      List<Observation> otherObservationList,
      List<DocumentReference> documentReferenceList) {

    List<Composition.SectionComponent> sectionComponentList = new ArrayList<>();

    // Helper to add fallback narrative if no entries exist
    java.util.function.Consumer<Composition.SectionComponent> ensureSectionHasContent =
        (section) -> {
          if (section.getEntry().isEmpty()) {
            Narrative narrative = new Narrative();
            narrative.setStatus(Narrative.NarrativeStatus.GENERATED);
            narrative.setDivAsString("<div>No data available</div>");
            section.setText(narrative);
          }
        };

    // 1 - Chief Complaints
    if (chiefComplaintList != null) {
      Composition.SectionComponent section = new Composition.SectionComponent();
      section.setCode(new CodeableConcept().setText(BundleCompositionIdentifier.CHIEF_COMPLAINTS));
      for (Condition item : chiefComplaintList) {
        section.addEntry(
            new Reference(BundleResourceIdentifier.CHIEF_COMPLAINTS + "/" + item.getId()));
      }
      ensureSectionHasContent.accept(section);
      sectionComponentList.add(section);
    }

    // 2 - Physical Examination
    if (physicalObservationList != null) {
      Composition.SectionComponent section = new Composition.SectionComponent();
      section.setCode(
          new CodeableConcept().setText(BundleCompositionIdentifier.PHYSICAL_EXAMINATION));
      for (Observation item : physicalObservationList) {
        section.addEntry(
            new Reference(BundleResourceIdentifier.PHYSICAL_EXAMINATION + "/" + item.getId()));
      }
      ensureSectionHasContent.accept(section);
      sectionComponentList.add(section);
    }

    // 3 - Allergy
    if (allergieList != null) {
      Composition.SectionComponent section = new Composition.SectionComponent();
      section.setCode(new CodeableConcept().setText(BundleCompositionIdentifier.ALLERGY_RECORD));
      for (AllergyIntolerance item : allergieList) {
        section.addEntry(
            new Reference(BundleResourceIdentifier.ALLERGY_INTOLERANCE + "/" + item.getId()));
      }
      ensureSectionHasContent.accept(section);
      sectionComponentList.add(section);
    }

    // 4 - Medical History
    if (medicalHistoryList != null) {
      Composition.SectionComponent section = new Composition.SectionComponent();
      section.setCode(
          new CodeableConcept().setText(BundleCompositionIdentifier.MEDICAL_HISTORY_SECTION));
      for (Condition item : medicalHistoryList) {
        section.addEntry(
            new Reference(BundleResourceIdentifier.MEDICAL_HISTORY + "/" + item.getId()));
      }
      ensureSectionHasContent.accept(section);
      sectionComponentList.add(section);
    }

    // 5 - Family History
    if (familyMemberHistoryList != null) {
      Composition.SectionComponent section = new Composition.SectionComponent();
      section.setCode(
          new CodeableConcept().setText(BundleCompositionIdentifier.FAMILY_HISTORY_SECTION));
      for (FamilyMemberHistory item : familyMemberHistoryList) {
        section.addEntry(
            new Reference(BundleResourceIdentifier.FAMILY_HISTORY + "/" + item.getId()));
      }
      ensureSectionHasContent.accept(section);
      sectionComponentList.add(section);
    }

    // 6 - Investigation Advice
    if (investigationAdviceList != null) {
      Composition.SectionComponent section = new Composition.SectionComponent();
      section.setCode(new CodeableConcept().setText(BundleCompositionIdentifier.ORDER_DOCUMENT));
      for (ServiceRequest item : investigationAdviceList) {
        section.addEntry(
            new Reference(BundleResourceIdentifier.INVESTIGATION_ADVICE + "/" + item.getId()));
      }
      ensureSectionHasContent.accept(section);
      sectionComponentList.add(section);
    }

    // 7 - Medication Summary
    if (medicationList != null) {
      Composition.SectionComponent section = new Composition.SectionComponent();
      section.setCode(
          new CodeableConcept().setText(BundleCompositionIdentifier.MEDICATION_SUMMARY));
      for (MedicationRequest item : medicationList) {
        section.addEntry(
            new Reference(BundleResourceIdentifier.MEDICATION_REQUEST + "/" + item.getId()));
      }
      ensureSectionHasContent.accept(section);
      sectionComponentList.add(section);
    }

    // 8 - Follow-up
    if (followupList != null) {
      Composition.SectionComponent section = new Composition.SectionComponent();
      section.setCode(new CodeableConcept().setText(BundleCompositionIdentifier.FOLLOW_UP));
      for (Appointment item : followupList) {
        section.addEntry(new Reference(BundleResourceIdentifier.FOLLOW_UP + "/" + item.getId()));
      }
      ensureSectionHasContent.accept(section);
      sectionComponentList.add(section);
    }

    // 9 - Procedure
    if (procedureList != null) {
      Composition.SectionComponent section = new Composition.SectionComponent();
      section.setCode(
          new CodeableConcept().setText(BundleCompositionIdentifier.CLINICAL_PROCEDURE));
      for (Procedure item : procedureList) {
        section.addEntry(new Reference(BundleResourceIdentifier.PROCEDURE + "/" + item.getId()));
      }
      ensureSectionHasContent.accept(section);
      sectionComponentList.add(section);
    }

    // 10 - Referral
    if (referralList != null) {
      Composition.SectionComponent section = new Composition.SectionComponent();
      section.setCode(
          new CodeableConcept().setText(BundleCompositionIdentifier.REFERRAL_TO_SERVICE));
      for (ServiceRequest item : referralList) {
        section.addEntry(new Reference(BundleResourceIdentifier.REFERRAL + "/" + item.getId()));
      }
      ensureSectionHasContent.accept(section);
      sectionComponentList.add(section);
    }

    // 11 - Other Observations
    if (otherObservationList != null) {
      Composition.SectionComponent section = new Composition.SectionComponent();
      section.setCode(new CodeableConcept().setText(BundleCompositionIdentifier.CLINICAL_FINDING));
      for (Observation item : otherObservationList) {
        section.addEntry(
            new Reference(BundleResourceIdentifier.OTHER_OBSERVATIONS + "/" + item.getId()));
      }
      ensureSectionHasContent.accept(section);
      sectionComponentList.add(section);
    }

    // 12 - Document Reference
    if (documentReferenceList != null) {
      Composition.SectionComponent section = new Composition.SectionComponent();
      section.setCode(
          new CodeableConcept().setText(BundleCompositionIdentifier.CLINICAL_CONSULTATION_REPORT));
      for (DocumentReference item : documentReferenceList) {
        section.addEntry(
            new Reference(BundleResourceIdentifier.DOCUMENT_REFERENCE + "/" + item.getId()));
      }
      ensureSectionHasContent.accept(section);
      sectionComponentList.add(section);
    }

    return sectionComponentList;
  }
}
