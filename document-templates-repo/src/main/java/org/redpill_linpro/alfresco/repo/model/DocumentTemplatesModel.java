package org.redpill_linpro.alfresco.repo.model;

import org.alfresco.service.namespace.QName;

public interface DocumentTemplatesModel {
  public static final String URI = "http://www.redpill-linpro.org/alfresco/model/documentTemplates/1.0";
  public static final String SHORT_PREFIX = "rplpdt";

  public static final QName ASPECT_TEMPLATE = QName.createQName(URI, "templatedAspect");
  public static final QName ASSOC_TEMPLATE = QName.createQName(URI, "template");
}
