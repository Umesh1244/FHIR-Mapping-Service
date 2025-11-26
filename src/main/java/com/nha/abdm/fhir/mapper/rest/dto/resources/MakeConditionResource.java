/* (C) 2025 */
package com.nha.abdm.fhir.mapper.rest.dto.resources;

import com.nha.abdm.fhir.mapper.Utils;
import com.nha.abdm.fhir.mapper.rest.common.constants.BundleResourceIdentifier;
import com.nha.abdm.fhir.mapper.rest.common.constants.ResourceProfileIdentifier;
import com.nha.abdm.fhir.mapper.rest.common.helpers.DateRange;
import java.text.ParseException;
import java.util.UUID;
import org.hl7.fhir.r4.model.*;
import org.springframework.stereotype.Component;

@Component
public class MakeConditionResource {

  public Condition getCondition(
      String conditionDetails, Patient patient, String recordedDate, DateRange dateRange)
      throws ParseException {

    HumanName patientName = patient.getName().get(0);

    Condition condition = new Condition();
    condition.setId(UUID.randomUUID().toString());

    // --------------------------------------------
    // TEXT-ONLY (NO SNOMED, NO CODING)
    // --------------------------------------------
    condition.setCode(new CodeableConcept().setText(conditionDetails));

    // Meta
    condition.setMeta(
        new Meta()
            .setLastUpdatedElement(Utils.getCurrentTimeStamp())
            .addProfile(ResourceProfileIdentifier.PROFILE_CONDITION));

    // Subject Reference
    condition.setSubject(
        new Reference()
            .setReference(BundleResourceIdentifier.PATIENT + "/" + patient.getId())
            .setDisplay(patientName.getText()));

    // Recorded Date
    if (recordedDate != null) {
      condition.setRecordedDateElement(Utils.getFormattedDateTime(recordedDate));
    }

    // Onset (Period)
    if (dateRange != null) {
      condition.setOnset(
          new Period()
              .setStartElement(Utils.getFormattedDateTime(dateRange.getFrom()))
              .setEndElement(Utils.getFormattedDateTime(dateRange.getTo())));
    }

    return condition;
  }
}
