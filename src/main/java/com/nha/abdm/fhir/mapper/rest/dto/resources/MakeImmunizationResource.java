/* (C) 2024 */
package com.nha.abdm.fhir.mapper.rest.dto.resources;

import com.nha.abdm.fhir.mapper.Utils;
import com.nha.abdm.fhir.mapper.rest.common.constants.BundleResourceIdentifier;
import com.nha.abdm.fhir.mapper.rest.common.constants.ResourceProfileIdentifier;
import com.nha.abdm.fhir.mapper.rest.requests.helpers.ImmunizationResource;
import java.text.ParseException;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.hl7.fhir.r4.model.*;
import org.springframework.stereotype.Component;

@Component
public class MakeImmunizationResource {

  public Immunization getImmunization(
      Patient patient,
      List<Practitioner> practitionerList,
      Organization organization,
      ImmunizationResource immunizationResource)
      throws ParseException {

    Immunization immunization = new Immunization();
    immunization.setId(UUID.randomUUID().toString());

    Meta meta = new Meta();
    meta.setVersionId("1");
    meta.setLastUpdatedElement(Utils.getCurrentTimeStamp());
    meta.addProfile(ResourceProfileIdentifier.PROFILE_IMMUNIZATION);
    immunization.setMeta(meta);

    immunization.setStatus(Immunization.ImmunizationStatus.COMPLETED);

    immunization.setPatient(
        new Reference().setReference(BundleResourceIdentifier.PATIENT + "/" + patient.getId()));

    if (immunizationResource.getDate() != null) {
      immunization.setOccurrence(Utils.getFormattedDateTime(immunizationResource.getDate()));
    }

    if (immunizationResource.getVaccineName() != null) {
      immunization.addExtension(
          new Extension()
              .setValue(new StringType(immunizationResource.getVaccineName()))
              .setUrl(ResourceProfileIdentifier.PROFILE_VACCINE_BRAND_NAME));

      immunization.setVaccineCode(
          new CodeableConcept().setText(immunizationResource.getVaccineName()));
    }

    immunization.setPrimarySource(true);

    if (immunizationResource.getManufacturer() != null) {
      immunization.setManufacturer(
          new Reference()
              .setReference(BundleResourceIdentifier.MANUFACTURER + "/" + organization.getId())
              .setDisplay(organization.getName()));
    }

    if (immunizationResource.getLotNumber() != null) {
      immunization.setLotNumber(immunizationResource.getLotNumber());
    }

    if (Objects.nonNull(immunizationResource.getDoseNumber())) {
      immunization.setDoseQuantity(new Quantity().setValue(immunizationResource.getDoseNumber()));

      immunization.setProtocolApplied(
          Collections.singletonList(
              new Immunization.ImmunizationProtocolAppliedComponent()
                  .setDoseNumber(new PositiveIntType(immunizationResource.getDoseNumber()))));
    }

    for (Practitioner practitioner : practitionerList) {
      immunization.addPerformer(
          new Immunization.ImmunizationPerformerComponent()
              .setActor(
                  new Reference()
                      .setReference(
                          BundleResourceIdentifier.PRACTITIONER + "/" + practitioner.getId())));
    }

    return immunization;
  }
}
