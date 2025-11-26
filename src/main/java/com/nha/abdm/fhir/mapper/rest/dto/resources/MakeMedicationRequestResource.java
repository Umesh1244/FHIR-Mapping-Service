/* (C) 2024 */
package com.nha.abdm.fhir.mapper.rest.dto.resources;

import com.nha.abdm.fhir.mapper.Utils;
import com.nha.abdm.fhir.mapper.rest.common.constants.*;
import com.nha.abdm.fhir.mapper.rest.database.h2.services.SnomedService;
import com.nha.abdm.fhir.mapper.rest.requests.helpers.PrescriptionResource;
import java.text.ParseException;
import java.util.*;
import org.hl7.fhir.r4.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MakeMedicationRequestResource {
  @Autowired SnomedService snomedService;

  public MedicationRequest getMedicationResource(
      String authoredOn,
      PrescriptionResource prescriptionResource,
      Condition medicationCondition,
      Organization organization,
      List<Practitioner> practitioners,
      Patient patient)
      throws ParseException {
    HumanName patientName = patient.getName().get(0);
    MedicationRequest medicationRequest = new MedicationRequest();

    // Setting Meta of the Medication Resource
    medicationRequest.setMeta(
        new Meta()
            .addProfile(ResourceProfileIdentifier.PROFILE_MEDICATION_REQUEST)
            .setLastUpdatedElement(Utils.getCurrentTimeStamp()));

    // FIX 1: Setting Medications - TEXT ONLY (remove addCoding)
    medicationRequest.setMedication(
        new CodeableConcept().setText(prescriptionResource.getMedicine()));

    if (prescriptionResource.getDosage() != null) {
      Dosage dosage = new Dosage();
      dosage.setText(prescriptionResource.getDosage());

      // FIX 2: Additional Instructions - TEXT ONLY (remove addCoding)
      if (prescriptionResource.getAdditionalInstructions() != null) {
        dosage.addAdditionalInstruction(
            new CodeableConcept().setText(prescriptionResource.getAdditionalInstructions()));
      }

      // FIX 3: Route - TEXT ONLY (remove addCoding)
      if (prescriptionResource.getRoute() != null) {
        dosage.setRoute(new CodeableConcept().setText(prescriptionResource.getRoute()));
      }

      // FIX 4: Method - TEXT ONLY (remove addCoding)
      if (prescriptionResource.getMethod() != null) {
        dosage.setMethod(new CodeableConcept().setText(prescriptionResource.getMethod()));
      }

      if (prescriptionResource.getTiming() != null) {
        String[] parts = prescriptionResource.getTiming().split("-");
        dosage.setTiming(
            new Timing()
                .setRepeat(
                    new Timing.TimingRepeatComponent()
                        .setFrequency(Integer.parseInt(parts[0]))
                        .setPeriod(Integer.parseInt(parts[1]))
                        .setPeriodUnit(Timing.UnitsOfTime.valueOf(parts[2]))));
      }
      medicationRequest.setDosageInstruction(Collections.singletonList(dosage));
    }

    // FIX 5: Reason Code - TEXT ONLY (remove addCoding)
    if (medicationCondition != null) {
      medicationRequest.setReasonCode(
          Collections.singletonList(
              new CodeableConcept().setText(medicationCondition.getCode().getText())));
      medicationRequest.setReasonReference(
          Collections.singletonList(
              new Reference()
                  .setReference(
                      BundleResourceIdentifier.CONDITION + "/" + medicationCondition.getId())
                  .setDisplay(BundleFieldIdentifier.MEDICAL_CONDITION)));
    }

    if (!practitioners.isEmpty()) {
      Practitioner practitioner = practitioners.get(0);
      HumanName practitionerName = practitioner.getName().get(0);
      medicationRequest.setRequester(
          new Reference()
              .setReference(BundleResourceIdentifier.PRACTITIONER + "/" + practitioner.getId())
              .setDisplay(practitionerName.getText()));
    }

    medicationRequest.setSubject(
        new Reference()
            .setReference(BundleResourceIdentifier.PATIENT + "/" + patient.getId())
            .setDisplay(patientName.getText()));

    if (authoredOn != null)
      medicationRequest.setAuthoredOnElement(Utils.getFormattedDateTime(authoredOn));

    medicationRequest.setStatus(MedicationRequest.MedicationRequestStatus.COMPLETED);
    medicationRequest.setIntent(MedicationRequest.MedicationRequestIntent.ORDER);
    medicationRequest.setId(UUID.randomUUID().toString());

    return medicationRequest;
  }
}
