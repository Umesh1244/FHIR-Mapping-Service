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

    String cleanedEncounterName = Utils.clean(encounterName);
    String cleanedVisitDate = Utils.clean(visitDate);
    String cleanedDefaultClass = Utils.clean(BundleFieldIdentifier.AMBULATORY);

    Encounter encounter = new Encounter();
    encounter.setId(UUID.randomUUID().toString());
    encounter.setStatus(Encounter.EncounterStatus.INPROGRESS);

    encounter.setMeta(
        new Meta()
            .setLastUpdatedElement(Utils.getCurrentTimeStamp())
            .addProfile(ResourceProfileIdentifier.PROFILE_ENCOUNTER));

    Coding encounterClass = new Coding();
    encounterClass.setDisplay(
        (cleanedEncounterName != null && !cleanedEncounterName.isEmpty())
            ? cleanedEncounterName
            : cleanedDefaultClass);
    encounter.setClass_(encounterClass);

    encounter.setSubject(
        new Reference()
            .setReference(BundleResourceIdentifier.PATIENT + "/" + patient.getId())
            .setDisplay(Utils.clean(patient.getNameFirstRep().getText())));

    encounter.setPeriod(new Period().setStartElement(Utils.getFormattedDateTime(cleanedVisitDate)));

    return encounter;
  }
}
