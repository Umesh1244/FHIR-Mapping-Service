/* (C) 2025 */
package com.nha.abdm.fhir.mapper.rest.dto.resources;

import com.nha.abdm.fhir.mapper.Utils;
import com.nha.abdm.fhir.mapper.rest.common.constants.BundleResourceIdentifier;
import com.nha.abdm.fhir.mapper.rest.common.constants.BundleUrlIdentifier;
import com.nha.abdm.fhir.mapper.rest.common.constants.ResourceProfileIdentifier;
import com.nha.abdm.fhir.mapper.rest.common.helpers.DocumentResource;
import java.text.ParseException;
import java.util.UUID;
import org.hl7.fhir.r4.model.*;
import org.springframework.stereotype.Component;

@Component
public class MakeDocumentResource {

  public DocumentReference getDocument(
      Patient patient,
      Organization organization,
      DocumentResource documentResource,
      String docCode,
      String docName)
      throws ParseException {

    String cleanedType = Utils.clean(documentResource.getType());
    String cleanedContentType = Utils.clean(documentResource.getContentType());
    String cleanedDocName = Utils.clean(docName);

    Identifier identifier = new Identifier();
    identifier.setSystem(BundleUrlIdentifier.FACILITY_URL);

    if (organization != null && organization.getId() != null) {
      identifier.setValue(Utils.clean(organization.getId()));
    } else {
      identifier.setValue(UUID.randomUUID().toString());
    }

    identifier.setType(new CodeableConcept().setText(cleanedType));

    Attachment attachment = new Attachment();
    attachment.setContentType(cleanedContentType);
    attachment.setData(documentResource.getData());
    attachment.setTitle(cleanedType);
    attachment.setCreationElement(new DateTimeType(Utils.getCurrentTimeStamp().getValueAsString()));

    DocumentReference.DocumentReferenceContentComponent content =
        new DocumentReference.DocumentReferenceContentComponent().setAttachment(attachment);

    DocumentReference documentReference = new DocumentReference();
    documentReference.setId(UUID.randomUUID().toString());
    documentReference.setMeta(
        new Meta()
            .setLastUpdatedElement(Utils.getCurrentTimeStamp())
            .addProfile(ResourceProfileIdentifier.PROFILE_DOCUMENT_REFERENCE));

    documentReference.addIdentifier(identifier);
    documentReference.addContent(content);
    documentReference.setStatus(Enumerations.DocumentReferenceStatus.CURRENT);
    documentReference.setDocStatus(DocumentReference.ReferredDocumentStatus.FINAL);

    documentReference.setSubject(
        new Reference()
            .setReference(BundleResourceIdentifier.PATIENT + "/" + patient.getId())
            .setDisplay(Utils.clean(patient.getNameFirstRep().getText())));

    documentReference.setType(new CodeableConcept().setText(cleanedDocName));

    return documentReference;
  }
}
