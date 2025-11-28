/* (C) 2024 */
package com.nha.abdm.fhir.mapper.rest.dto.resources;

import com.nha.abdm.fhir.mapper.Utils;
import com.nha.abdm.fhir.mapper.rest.common.constants.ResourceProfileIdentifier;
import java.text.ParseException;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Meta;
import org.springframework.stereotype.Component;

@Component
public class MakeBundleMetaResource {

  public Meta getMeta() throws ParseException {
    Meta meta = new Meta();
    meta.setVersionId("1");
    meta.setLastUpdatedElement(Utils.getCurrentTimeStamp());
    meta.addProfile(ResourceProfileIdentifier.PROFILE_DOCUMENT_BUNDLE);

    Coding security = new Coding();
    security.setDisplay(Utils.clean("restricted"));

    meta.addSecurity(security);

    return meta;
  }
}
