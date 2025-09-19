/* (C) 2024 */
package com.nha.abdm.fhir.mapper.rest.dto.resources;

import com.nha.abdm.fhir.mapper.rest.common.constants.BundleResourceIdentifier;
import com.nha.abdm.fhir.mapper.rest.common.constants.BundleUrlIdentifier;
import com.nha.abdm.fhir.mapper.rest.database.h2.services.SnomedService;
import com.nha.abdm.fhir.mapper.rest.database.h2.tables.SnomedEncounter;
import com.nha.abdm.fhir.mapper.rest.requests.helpers.CarePlanResource;
import java.text.ParseException;
import java.util.*;
import java.util.stream.Collectors;
import org.hl7.fhir.r4.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MakeCarePlanResource {

  @Autowired SnomedService snomedService;

  public CarePlan getCarePlan(
      CarePlanResource carePlanResource, Patient patient, Practitioner practitioner)
      throws ParseException {
    CarePlan carePlan = new CarePlan();
    carePlan.setId(UUID.randomUUID().toString());
    carePlan.setStatus(CarePlan.CarePlanStatus.ACTIVE);
    carePlan.setIntent(CarePlan.CarePlanIntent.fromCode(carePlanResource.getIntent()));

    if (carePlanResource.getDescription() != null) {
      carePlan.setDescription(carePlanResource.getDescription());
    }
    carePlan.setTitle(carePlanResource.getType());
    carePlan.setSubject(
        new Reference()
            .setReference(BundleResourceIdentifier.PATIENT + "/" + patient.getId())
            .setDisplay(
                patient.getName().stream()
                    .map(HumanName::getText)
                    .collect(Collectors.joining(" "))));

    CodeableConcept codeableConcept = new CodeableConcept();
    Coding carePlanCoding = new Coding();
    SnomedEncounter snomed = snomedService.getSnomedEncounterCode(carePlanResource.getType());
    carePlanCoding.setDisplay(snomed.getDisplay());
    carePlanCoding.setSystem(BundleUrlIdentifier.SNOMED_URL);
    carePlanCoding.setCode(snomed.getCode());
    codeableConcept.addCoding(carePlanCoding);
    carePlan.setCategory(Collections.singletonList(codeableConcept));

    if (carePlanResource.getPeriod() != null) {
      Period period = new Period();

      if (carePlanResource.getPeriod().getFrom() != null) {
        period.setStartElement(new DateTimeType(carePlanResource.getPeriod().getFrom()));
      }
      if (carePlanResource.getPeriod().getTo() != null) {
        period.setEndElement(new DateTimeType(carePlanResource.getPeriod().getTo()));
      }

      carePlan.setPeriod(period);
    }

    // Create appointment inline if period is available
    Appointment appointment = null;
    if (carePlanResource.getPeriod() != null && carePlanResource.getPeriod().getFrom() != null) {

      appointment = new Appointment();
      appointment.setId(UUID.randomUUID().toString());
      appointment.setStatus(Appointment.AppointmentStatus.BOOKED);

      if (carePlanResource.getPeriod().getFrom() != null) {
        appointment.setStartElement(new InstantType(carePlanResource.getPeriod().getFrom()));
      }
      if (carePlanResource.getPeriod().getTo() != null) {
        appointment.setEndElement(new InstantType(carePlanResource.getPeriod().getTo()));
      }
      appointment.setDescription("Follow-up visit for " + carePlanResource.getDescription());
      //      appointment.setCreated(new Date());

      // Add participants
      List<Appointment.AppointmentParticipantComponent> participants = new ArrayList<>();

      // Patient participant
      Appointment.AppointmentParticipantComponent patientParticipant =
          new Appointment.AppointmentParticipantComponent();
      patientParticipant.setActor(
          new Reference()
              .setReference(BundleResourceIdentifier.PATIENT + "/" + patient.getId())
              .setDisplay(
                  patient.getName().stream()
                      .map(HumanName::getText)
                      .collect(Collectors.joining(" "))));
      patientParticipant.setStatus(Appointment.ParticipationStatus.ACCEPTED);
      participants.add(patientParticipant);

      // Practitioner participant
      if (practitioner != null) {
        Appointment.AppointmentParticipantComponent practitionerParticipant =
            new Appointment.AppointmentParticipantComponent();
        practitionerParticipant.setActor(
            new Reference()
                .setReference(BundleResourceIdentifier.PRACTITIONER + "/" + practitioner.getId())
                .setDisplay(
                    practitioner.getName().stream()
                        .map(HumanName::getText)
                        .collect(Collectors.joining(" "))));
        practitionerParticipant.setStatus(Appointment.ParticipationStatus.ACCEPTED);
        participants.add(practitionerParticipant);
      }

      appointment.setParticipant(participants);
    }

    // Modified activity creation
    List<CarePlan.CarePlanActivityComponent> activities = new ArrayList<>();
    CarePlan.CarePlanActivityComponent activity = new CarePlan.CarePlanActivityComponent();

    // Add appointment reference if appointment was created
    if (appointment != null) {
      Reference appointmentReference = new Reference();
      appointmentReference.setReference("urn:uuid:" + appointment.getId());
      activity.setOutcomeReference(Collections.singletonList(appointmentReference));
    }

    if (carePlanResource.getNotes() != null) {
      // Detail
      CarePlan.CarePlanActivityDetailComponent activityDetailComponent =
          new CarePlan.CarePlanActivityDetailComponent();
      activityDetailComponent.setDescription(carePlanResource.getNotes());

      // Set scheduled period if available
      if (carePlanResource.getPeriod() != null) {
        Period scheduledPeriod = new Period();
        if (carePlanResource.getPeriod().getFrom() != null) {
          scheduledPeriod.setStartElement(new DateTimeType(carePlanResource.getPeriod().getFrom()));
        }
        if (carePlanResource.getPeriod().getTo() != null) {
          scheduledPeriod.setEndElement(new DateTimeType(carePlanResource.getPeriod().getTo()));
        }
        activityDetailComponent.setScheduled(scheduledPeriod);
      }

      activity.setDetail(activityDetailComponent);

      Annotation annotation = new Annotation();
      annotation.setText(carePlanResource.getNotes());
      carePlan.setNote(Collections.singletonList(annotation));
    }

    activities.add(activity);
    carePlan.setActivity(activities);

    // Store the appointment reference for bundle creation
    // You can add this as a field in your class or return it separately
    carePlan.setUserData("appointment", appointment);

    return carePlan;
  }
}
