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

    String cleanedPatientName = Utils.clean(patient.getNameFirstRep().getText());
    MedicationRequest medicationRequest = new MedicationRequest();

    medicationRequest.setMeta(
        new Meta()
            .addProfile(ResourceProfileIdentifier.PROFILE_MEDICATION_REQUEST)
            .setLastUpdatedElement(Utils.getCurrentTimeStamp()));

    medicationRequest.setMedication(
        new CodeableConcept().setText(Utils.clean(prescriptionResource.getMedicine())));

    if (prescriptionResource.getDosage() != null) {
      Dosage dosage = new Dosage();
      dosage.setText(Utils.clean(prescriptionResource.getDosage()));

      if (prescriptionResource.getAdditionalInstructions() != null) {
        dosage.addAdditionalInstruction(
            new CodeableConcept()
                .setText(Utils.clean(prescriptionResource.getAdditionalInstructions())));
      }

      if (prescriptionResource.getRoute() != null) {
        dosage.setRoute(
            new CodeableConcept().setText(Utils.clean(prescriptionResource.getRoute())));
      }

      if (prescriptionResource.getMethod() != null) {
        dosage.setMethod(
            new CodeableConcept().setText(Utils.clean(prescriptionResource.getMethod())));
      }

      if (prescriptionResource.getTiming() != null) {
        String[] parts = prescriptionResource.getTiming().split("-");
        if (parts.length == 3) {
          dosage.setTiming(
              new Timing()
                  .setRepeat(
                      new Timing.TimingRepeatComponent()
                          .setFrequency(Integer.parseInt(parts[0]))
                          .setPeriod(Integer.parseInt(parts[1]))
                          .setPeriodUnit(Timing.UnitsOfTime.valueOf(parts[2]))));
        }
      }

      medicationRequest.setDosageInstruction(Collections.singletonList(dosage));
    }

    if (medicationCondition != null) {
      medicationRequest.setReasonCode(
          Collections.singletonList(
              new CodeableConcept().setText(Utils.clean(medicationCondition.getCode().getText()))));
      medicationRequest.setReasonReference(
          Collections.singletonList(
              new Reference()
                  .setReference(
                      BundleResourceIdentifier.CONDITION + "/" + medicationCondition.getId())
                  .setDisplay(Utils.clean(BundleFieldIdentifier.MEDICAL_CONDITION))));
    }

    if (!practitioners.isEmpty()) {
      Practitioner practitioner = practitioners.get(0);
      String cleanedPractitionerName = Utils.clean(practitioner.getNameFirstRep().getText());
      medicationRequest.setRequester(
          new Reference()
              .setReference(BundleResourceIdentifier.PRACTITIONER + "/" + practitioner.getId())
              .setDisplay(cleanedPractitionerName));
    }

    medicationRequest.setSubject(
        new Reference()
            .setReference(BundleResourceIdentifier.PATIENT + "/" + patient.getId())
            .setDisplay(cleanedPatientName));

    if (authoredOn != null) {
      medicationRequest.setAuthoredOnElement(Utils.getFormattedDateTime(authoredOn));
    }

    medicationRequest.setStatus(MedicationRequest.MedicationRequestStatus.COMPLETED);
    medicationRequest.setIntent(MedicationRequest.MedicationRequestIntent.ORDER);
    medicationRequest.setId(UUID.randomUUID().toString());

    return medicationRequest;
  }
}
