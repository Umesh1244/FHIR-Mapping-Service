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

    String cleanedDetails = Utils.clean(conditionDetails);
    String cleanedRecorded = Utils.clean(recordedDate);

    Condition condition = new Condition();
    condition.setId(UUID.randomUUID().toString());

    condition.setCode(new CodeableConcept().setText(cleanedDetails));

    condition.setMeta(
        new Meta()
            .setLastUpdatedElement(Utils.getCurrentTimeStamp())
            .addProfile(ResourceProfileIdentifier.PROFILE_CONDITION));

    condition.setSubject(
        new Reference()
            .setReference(BundleResourceIdentifier.PATIENT + "/" + patient.getId())
            .setDisplay(Utils.clean(patient.getName().get(0).getText())));

    if (cleanedRecorded != null) {
      condition.setRecordedDateElement(Utils.getFormattedDateTime(cleanedRecorded));
    }

    if (dateRange != null) {
      String cleanedFrom = Utils.clean(dateRange.getFrom());
      String cleanedTo = Utils.clean(dateRange.getTo());

      condition.setOnset(
          new Period()
              .setStartElement(Utils.getFormattedDateTime(cleanedFrom))
              .setEndElement(Utils.getFormattedDateTime(cleanedTo)));
    }

    return condition;
  }
}
