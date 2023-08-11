package org.redpill_linpro.alfresco.repo.form;

import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.forms.Form;
import org.alfresco.repo.forms.FormData;
import org.alfresco.repo.forms.FormData.FieldData;
import org.alfresco.repo.forms.processor.AbstractFilter;
import org.alfresco.repo.forms.processor.node.FormFieldConstants;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.redpill_linpro.alfresco.repo.model.DocumentTemplatesModel;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

/**
 * This class will handle content creation based on templates using Alfresco Share Forms
 *
 * @author mars
 *
 */
public class DocumentTemplatesContentFilter extends AbstractFilter<Object, NodeRef> implements InitializingBean {
  private static final Logger LOG = LoggerFactory.getLogger(DocumentTemplatesContentFilter.class);

  private NodeService nodeService;
  private FileFolderService fileFolderService;

  final static String ASSOC_TEMPLATE_FIELD = FormFieldConstants.ASSOC_DATA_PREFIX + DocumentTemplatesModel.SHORT_PREFIX + FormFieldConstants.DATA_KEY_SEPARATOR
      + DocumentTemplatesModel.ASSOC_TEMPLATE.getLocalName();
  final static String ADDED_ASSOC_TEMPLATE_FIELD = ASSOC_TEMPLATE_FIELD + FormFieldConstants.ASSOC_DATA_ADDED_SUFFIX;
  final static String PROP_NAME_FIELD = FormFieldConstants.PROP_DATA_PREFIX + NamespaceService.CONTENT_MODEL_PREFIX + FormFieldConstants.DATA_KEY_SEPARATOR + ContentModel.PROP_NAME.getLocalName();

  @Override
  public void afterGenerate(Object item, List<String> fields, List<String> forcedFields, Form form, Map<String, Object> context) {
    // Do nothing
  }

  @Override
  public void afterPersist(Object item, FormData data, NodeRef persistedObject) {
    if (data != null && persistedObject != null) {
      FieldData fieldData = data.getFieldData(ADDED_ASSOC_TEMPLATE_FIELD);
      if (fieldData != null && NodeRef.isNodeRef((String) fieldData.getValue())) {
        NodeRef templateNode = new NodeRef((String) fieldData.getValue());
        if (LOG.isDebugEnabled())
          LOG.debug("Updating node " + persistedObject.toString() + " based on template " + templateNode);
        if (nodeService.exists(templateNode)) {
          ContentReader reader = fileFolderService.getReader(templateNode);
          ContentWriter writer = fileFolderService.getWriter(persistedObject);
          writer.setMimetype(reader.getMimetype());
          writer.putContent(reader);
        } else {
          LOG.warn("The template " + templateNode + " could not be found.");
        }
        nodeService.removeAssociation(persistedObject, templateNode, DocumentTemplatesModel.ASSOC_TEMPLATE);
        if (nodeService.hasAspect(persistedObject, DocumentTemplatesModel.ASPECT_TEMPLATE)) {
          nodeService.removeAspect(persistedObject, DocumentTemplatesModel.ASPECT_TEMPLATE);
        }
      } else {
        if (LOG.isDebugEnabled())
          LOG.debug("(afterPersist) No nodeRef found in ADDED_ASSOC_TEMPLATE_FIELD: " + fieldData);
      }
    } else {
      if (LOG.isDebugEnabled())
        LOG.debug("One or more values were null - Data:" + data + " persistedObject:" + persistedObject);
    }
  }

  @Override
  public void beforeGenerate(Object item, List<String> fields, List<String> forcedFields, Form form, Map<String, Object> context) {
    // Do nothing
  }

  @Override
  public void beforePersist(Object item, FormData data) {
    if (data != null) {
      FieldData fieldData = data.getFieldData(PROP_NAME_FIELD);
      if (fieldData != null && !((String) fieldData.getValue()).isEmpty()) {
        String name = (String) fieldData.getValue();
        fieldData = data.getFieldData(ADDED_ASSOC_TEMPLATE_FIELD);
        if (fieldData != null && NodeRef.isNodeRef((String) fieldData.getValue())) {
          NodeRef templateNode = new NodeRef((String) fieldData.getValue());

          if (nodeService.exists(templateNode)) {
            String templateName = (String) nodeService.getProperty(templateNode, ContentModel.PROP_NAME);
            String templateExtension = FilenameUtils.getExtension(templateName);
            String thisExtension = FilenameUtils.getExtension(name);
            if (!templateExtension.equalsIgnoreCase(thisExtension)) {
              if (LOG.isDebugEnabled())
                LOG.debug("Detected that file extension of new document does not match the one from the template. Changing the file extension to " + templateExtension);
              name = name + "." + templateExtension;
              data.addFieldData(PROP_NAME_FIELD, name, true);
            }

          } else {
            LOG.warn("The template " + templateNode + " could not be found.");
          }
        } else {
          if (LOG.isDebugEnabled())
            LOG.debug("(beforePersist) No nodeRef found in ADDED_ASSOC_TEMPLATE_FIELD: " + fieldData);
        }
      }
      else {
        LOG.debug("Name field was null or empty");
      }
    } else {
      LOG.debug("Data was null");
    }
  }

  public void setNodeService(NodeService nodeService) {
    this.nodeService = nodeService;
  }

  public void setFileFolderService(FileFolderService fileFolderService) {
    this.fileFolderService = fileFolderService;
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    Assert.notNull(nodeService, "NodeService is null");
    Assert.notNull(fileFolderService, "FileFolderService is null");
    Assert.notNull(filterRegistry, "FilterRegistry is null");
  }
}
