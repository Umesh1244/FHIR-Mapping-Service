/* (C) 2024 */
package com.nha.abdm.fhir.mapper.rest.dto.resources;

import com.nha.abdm.fhir.mapper.Utils;
import com.nha.abdm.fhir.mapper.rest.common.constants.BundleResourceIdentifier;
import com.nha.abdm.fhir.mapper.rest.database.h2.services.SnomedService;
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
    carePlan.setIntent(CarePlan.CarePlanIntent.fromCode(Utils.clean(carePlanResource.getIntent())));

    String cleanedDescription = Utils.clean(carePlanResource.getDescription());
    String cleanedType = Utils.clean(carePlanResource.getType());
    String cleanedNotes = Utils.clean(carePlanResource.getNotes());

    if (cleanedDescription != null) {
      carePlan.setDescription(cleanedDescription);
    }

    carePlan.setTitle(cleanedType);

    carePlan.setSubject(
        new Reference()
            .setReference(BundleResourceIdentifier.PATIENT + "/" + patient.getId())
            .setDisplay(
                patient.getName().stream()
                    .map(n -> Utils.clean(n.getText()))
                    .collect(Collectors.joining(" "))));

    CodeableConcept cc = new CodeableConcept();
    cc.setText(cleanedType);
    carePlan.setCategory(Collections.singletonList(cc));

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

    Appointment appointment = null;

    if (carePlanResource.getPeriod() != null && carePlanResource.getPeriod().getFrom() != null) {

      appointment = new Appointment();
      appointment.setId(UUID.randomUUID().toString());
      appointment.setStatus(Appointment.AppointmentStatus.BOOKED);
      appointment.setStartElement(new InstantType(carePlanResource.getPeriod().getFrom()));

      if (carePlanResource.getPeriod().getTo() != null) {
        appointment.setEndElement(new InstantType(carePlanResource.getPeriod().getTo()));
      }

      appointment.setDescription(Utils.clean("Follow-up visit for " + cleanedDescription));

      List<Appointment.AppointmentParticipantComponent> participants = new ArrayList<>();

      participants.add(
          new Appointment.AppointmentParticipantComponent()
              .setActor(
                  new Reference()
                      .setReference("Patient/" + patient.getId())
                      .setDisplay(Utils.clean(patient.getNameFirstRep().getText())))
              .setStatus(Appointment.ParticipationStatus.ACCEPTED));

      if (practitioner != null) {
        participants.add(
            new Appointment.AppointmentParticipantComponent()
                .setActor(
                    new Reference()
                        .setReference("Practitioner/" + practitioner.getId())
                        .setDisplay(Utils.clean(practitioner.getNameFirstRep().getText())))
                .setStatus(Appointment.ParticipationStatus.ACCEPTED));
      }

      appointment.setParticipant(participants);
    }

    List<CarePlan.CarePlanActivityComponent> activities = new ArrayList<>();
    CarePlan.CarePlanActivityComponent activity = new CarePlan.CarePlanActivityComponent();

    if (appointment != null) {
      activity.setOutcomeReference(
          Collections.singletonList(new Reference("urn:uuid:" + appointment.getId())));
    }

    if (cleanedNotes != null) {
      CarePlan.CarePlanActivityDetailComponent detail =
          new CarePlan.CarePlanActivityDetailComponent();
      detail.setDescription(cleanedNotes);

      if (carePlanResource.getPeriod() != null) {
        Period scheduled = new Period();

        if (carePlanResource.getPeriod().getFrom() != null) {
          scheduled.setStartElement(new DateTimeType(carePlanResource.getPeriod().getFrom()));
        }
        if (carePlanResource.getPeriod().getTo() != null) {
          scheduled.setEndElement(new DateTimeType(carePlanResource.getPeriod().getTo()));
        }

        detail.setScheduled(scheduled);
      }

      activity.setDetail(detail);

      Annotation annotation = new Annotation();
      annotation.setText(cleanedNotes);
      carePlan.setNote(Collections.singletonList(annotation));
    }

    activities.add(activity);
    carePlan.setActivity(activities);

    carePlan.setUserData("appointment", appointment);

    return carePlan;
  }
}
