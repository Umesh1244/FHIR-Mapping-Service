/* (C) 2025 */
package com.nha.abdm.fhir.mapper.rest.dto.resources;

import com.nha.abdm.fhir.mapper.Utils;
import com.nha.abdm.fhir.mapper.rest.common.constants.BundleFieldIdentifier;
import com.nha.abdm.fhir.mapper.rest.common.constants.BundleResourceIdentifier;
import com.nha.abdm.fhir.mapper.rest.common.constants.ResourceProfileIdentifier;
import java.text.ParseException;
import java.util.UUID;
import org.hl7.fhir.r4.model.*;
import org.springframework.stereotype.Component;

@Component
public class MakeEncounterResource {

  public Encounter getEncounter(Patient patient, String encounterName, String visitDate)
      throws ParseException {

    HumanName patientName = patient.getName().get(0);

    Encounter encounter = new Encounter();
    encounter.setId(UUID.randomUUID().toString());
    encounter.setStatus(Encounter.EncounterStatus.INPROGRESS);

    // META
    encounter.setMeta(
        new Meta()
            .setLastUpdatedElement(Utils.getCurrentTimeStamp())
            .addProfile(ResourceProfileIdentifier.PROFILE_ENCOUNTER));

    // ------------------------------------------------------
    // TEXT-ONLY CLASS (NO SYSTEM, NO CODE)
    // ------------------------------------------------------
    Coding encounterClass = new Coding();
    encounterClass.setDisplay(
        (encounterName != null && !encounterName.isEmpty())
            ? encounterName
            : BundleFieldIdentifier.AMBULATORY);
    encounter.setClass_(encounterClass);

    // SUBJECT
    encounter.setSubject(
        new Reference()
            .setReference(BundleResourceIdentifier.PATIENT + "/" + patient.getId())
            .setDisplay(patientName.getText()));

    // PERIOD
    encounter.setPeriod(new Period().setStartElement(Utils.getFormattedDateTime(visitDate)));

    return encounter;
  }
}
