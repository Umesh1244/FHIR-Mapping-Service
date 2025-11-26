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
public class MakeDischargeComposition {

  public Composition makeDischargeCompositionResource(
      Patient patient,
      String authoredOn,
      Encounter encounter,
      List<Practitioner> practitionerList,
      Organization organization,
      List<Condition> chiefComplaintList,
      List<Observation> physicalObservationList,
      List<AllergyIntolerance> allergieList,
      List<MedicationRequest> medicationRequestList,
      List<DiagnosticReport> diagnosticReportList,
      List<Condition> medicalHistoryList,
      List<FamilyMemberHistory> familyMemberHistoryList,
      CarePlan carePlan,
      List<Procedure> procedureList,
      List<DocumentReference> documentReferenceList,
      String docCode, // used only for docRef section
      String docName) // used only for docRef section
      throws ParseException {

    Composition composition = new Composition();
    composition.setId(UUID.randomUUID().toString());
    composition.setStatus(Composition.CompositionStatus.FINAL);

    // --- Composition Type: Discharge Summary (MANDATORY) ---
    composition.setType(
        new CodeableConcept()
            .addCoding(
                new Coding()
                    .setSystem(BundleUrlIdentifier.SNOMED_URL)
                    .setCode(BundleCompositionIdentifier.DISCHARGE_SUMMARY_CODE)
                    .setDisplay(BundleCompositionIdentifier.DISCHARGE_SUMMARY))
            .setText(BundleCompositionIdentifier.DISCHARGE_SUMMARY));

    composition.setTitle(BundleCompositionIdentifier.DISCHARGE_SUMMARY);

    // --- Subject ---
    HumanName pName = patient.getNameFirstRep();
    composition.setSubject(
        new Reference()
            .setReference(BundleResourceIdentifier.PATIENT + "/" + patient.getId())
            .setDisplay(pName != null ? pName.getText() : null));

    // --- Encounter ---
    composition.setEncounter(
        new Reference().setReference(BundleResourceIdentifier.ENCOUNTER + "/" + encounter.getId()));

    // --- Custodian ---
    composition.setCustodian(
        new Reference()
            .setReference(BundleResourceIdentifier.ORGANISATION + "/" + organization.getId())
            .setDisplay(organization.getName()));

    // --- Author List ---
    List<Reference> authorList = new ArrayList<>();
    for (Practitioner practitioner : practitionerList) {
      HumanName prName = practitioner.getNameFirstRep();
      authorList.add(
          new Reference()
              .setReference(BundleResourceIdentifier.PRACTITIONER + "/" + practitioner.getId())
              .setDisplay(prName != null ? prName.getText() : null));
    }
    composition.setAuthor(authorList);

    composition.setDateElement(Utils.getFormattedDateTime(authoredOn));

    // --- Sections ---
    List<Composition.SectionComponent> sections =
        makeCompositionSection(
            chiefComplaintList,
            physicalObservationList,
            allergieList,
            medicationRequestList,
            diagnosticReportList,
            medicalHistoryList,
            familyMemberHistoryList,
            carePlan,
            procedureList,
            documentReferenceList,
            docCode,
            docName);

    sections.forEach(composition::addSection);

    // Identifier
    composition.setIdentifier(
        new Identifier()
            .setSystem(BundleUrlIdentifier.WRAPPER_URL)
            .setValue(UUID.randomUUID().toString()));

    return composition;
  }

  private List<Composition.SectionComponent> makeCompositionSection(
      List<Condition> chiefComplaintList,
      List<Observation> physicalObservationList,
      List<AllergyIntolerance> allergieList,
      List<MedicationRequest> medicationRequestList,
      List<DiagnosticReport> diagnosticReportList,
      List<Condition> medicalHistoryList,
      List<FamilyMemberHistory> familyMemberHistoryList,
      CarePlan carePlan,
      List<Procedure> procedureList,
      List<DocumentReference> documentReferenceList,
      String docCode,
      String docName) {

    List<Composition.SectionComponent> list = new ArrayList<>();

    // --- Helper method ---
    java.util.function.Consumer<Composition.SectionComponent> addIfValid =
        sec -> {
          if (sec.getEntry() != null && !sec.getEntry().isEmpty()) list.add(sec);
        };

    //  Chief Complaints
    if (chiefComplaintList != null && !chiefComplaintList.isEmpty()) {
      Composition.SectionComponent sec = new Composition.SectionComponent();
      sec.setCode(new CodeableConcept().setText("ChiefComplaints"));
      chiefComplaintList.forEach(c -> sec.addEntry(new Reference("ChiefComplaints/" + c.getId())));
      addIfValid.accept(sec);
    }

    //  Physical Exam
    if (physicalObservationList != null && !physicalObservationList.isEmpty()) {
      Composition.SectionComponent sec = new Composition.SectionComponent();
      sec.setCode(new CodeableConcept().setText("PhysicalExamination"));
      physicalObservationList.forEach(
          o -> sec.addEntry(new Reference("PhysicalExamination/" + o.getId())));
      addIfValid.accept(sec);
    }

    //  Allergy
    if (allergieList != null && !allergieList.isEmpty()) {
      Composition.SectionComponent sec = new Composition.SectionComponent();
      sec.setCode(new CodeableConcept().setText("AllergyRecord"));
      allergieList.forEach(a -> sec.addEntry(new Reference("AllergyIntolerance/" + a.getId())));
      addIfValid.accept(sec);
    }

    //  Past Medical History
    if (medicalHistoryList != null && !medicalHistoryList.isEmpty()) {
      Composition.SectionComponent sec = new Composition.SectionComponent();
      sec.setCode(new CodeableConcept().setText("PastMedicalHistory"));
      medicalHistoryList.forEach(m -> sec.addEntry(new Reference("MedicalHistory/" + m.getId())));
      addIfValid.accept(sec);
    }

    //  Family History
    if (familyMemberHistoryList != null && !familyMemberHistoryList.isEmpty()) {
      Composition.SectionComponent sec = new Composition.SectionComponent();
      sec.setCode(new CodeableConcept().setText("FamilyHistory"));
      familyMemberHistoryList.forEach(
          f -> sec.addEntry(new Reference("FamilyHistory/" + f.getId())));
      addIfValid.accept(sec);
    }

    //  Care Plan
    if (carePlan != null) {
      Composition.SectionComponent sec = new Composition.SectionComponent();
      sec.setCode(new CodeableConcept().setText("CarePlan"));
      sec.addEntry(new Reference("CarePlan/" + carePlan.getId()));
      addIfValid.accept(sec);
    }

    //  Medication Summary
    if (medicationRequestList != null && !medicationRequestList.isEmpty()) {
      Composition.SectionComponent sec = new Composition.SectionComponent();
      sec.setCode(new CodeableConcept().setText("MedicationSummary"));
      medicationRequestList.forEach(
          m -> sec.addEntry(new Reference("MedicationRequest/" + m.getId())));
      addIfValid.accept(sec);
    }

    //  Diagnostic Report
    if (diagnosticReportList != null && !diagnosticReportList.isEmpty()) {
      Composition.SectionComponent sec = new Composition.SectionComponent();
      sec.setCode(new CodeableConcept().setText("DiagnosticStudiesReport"));
      diagnosticReportList.forEach(
          r -> sec.addEntry(new Reference("DiagnosticReport/" + r.getId())));
      addIfValid.accept(sec);
    }

    //  Procedure
    if (procedureList != null && !procedureList.isEmpty()) {
      Composition.SectionComponent sec = new Composition.SectionComponent();
      sec.setCode(new CodeableConcept().setText("ClinicalProcedure"));
      procedureList.forEach(p -> sec.addEntry(new Reference("Procedure/" + p.getId())));
      addIfValid.accept(sec);
    }

    //  DocumentReference
    if (documentReferenceList != null && !documentReferenceList.isEmpty()) {
      Composition.SectionComponent sec = new Composition.SectionComponent();
      sec.setCode(new CodeableConcept().setText("DocumentReference"));
      documentReferenceList.forEach(
          d -> sec.addEntry(new Reference("DocumentReference/" + d.getId())));
      addIfValid.accept(sec);
    }

    return list;
  }
}
