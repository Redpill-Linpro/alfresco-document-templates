package org.redpill_linpro.alfresco.repo.form;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.forms.Form;
import org.alfresco.repo.forms.FormData;
import org.alfresco.repo.forms.processor.FilterRegistry;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.apache.commons.io.FilenameUtils;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.redpill_linpro.alfresco.repo.model.DocumentTemplatesModel;


public class DocumentTemplatesContentFilterTest {
  
  private DocumentTemplatesContentFilter documentTemplatesContentFilter;
  private Mockery m;
  private NodeService nodeService;
  private FileFolderService fileFolderService;
  private FilterRegistry filterRegistry;
  
  private ContentReader contentReader;
  private ContentWriter contentWriter;
  private final NodeRef nodeRef1 = new NodeRef("workspace", "SpacesStore", "1");
  private final NodeRef nodeRef2 = new NodeRef("workspace", "SpacesStore", "2");
  private final String node1name = "node1name.doc";
  private final String node2name = "node2name.txt";
  @Before
  public void setUp() throws Exception {
    m = new Mockery();
    nodeService = m.mock(NodeService.class);
    fileFolderService = m.mock(FileFolderService.class);
    filterRegistry = new FilterRegistry();
    
    documentTemplatesContentFilter = new DocumentTemplatesContentFilter();
    documentTemplatesContentFilter.setNodeService(nodeService);
    documentTemplatesContentFilter.setFileFolderService(fileFolderService);
    documentTemplatesContentFilter.setFilterRegistry(filterRegistry);
    documentTemplatesContentFilter.afterPropertiesSet();
    
    contentReader = m.mock(ContentReader.class);
    contentWriter = m.mock(ContentWriter.class);
  }

  @After
  public void tearDown() throws Exception {
    documentTemplatesContentFilter = null; //Clear the instance
  }

  @Test
  public void testAfterPersist() {
    
    m.checking(new Expectations() {
      {
        oneOf(nodeService).exists(nodeRef2);
        will(returnValue(true));
        
        oneOf(fileFolderService).getReader(nodeRef2);
        will(returnValue(contentReader));
        
        oneOf(fileFolderService).getWriter(nodeRef1);
        will(returnValue(contentWriter));

        oneOf(contentReader).getMimetype();
        will(returnValue(MimetypeMap.MIMETYPE_VISIO));
        oneOf(contentWriter).setMimetype(MimetypeMap.MIMETYPE_VISIO);
        
        oneOf(contentWriter).putContent(contentReader);
        
        oneOf(nodeService).removeAssociation(nodeRef1, nodeRef2, DocumentTemplatesModel.ASSOC_TEMPLATE);
        
        oneOf(nodeService).hasAspect(nodeRef1, DocumentTemplatesModel.ASPECT_TEMPLATE);
        will(returnValue(true));
        
        oneOf(nodeService).removeAspect(nodeRef1, DocumentTemplatesModel.ASPECT_TEMPLATE);
      }
    });
    
    documentTemplatesContentFilter.afterPersist(null, null, null);
    
    Object item = null;
    FormData data = new FormData();
    NodeRef persistedObject = null;
    documentTemplatesContentFilter.afterPersist(item, data, persistedObject);
    persistedObject = nodeRef1;
    documentTemplatesContentFilter.afterPersist(item, data, persistedObject);
    data.addFieldData(DocumentTemplatesContentFilter.ADDED_ASSOC_TEMPLATE_FIELD, "", true);
    documentTemplatesContentFilter.afterPersist(item, data, persistedObject);
    
    data.addFieldData(DocumentTemplatesContentFilter.ADDED_ASSOC_TEMPLATE_FIELD, "notANodeRef", true);
    documentTemplatesContentFilter.afterPersist(item, data, persistedObject);
    
    data.addFieldData(DocumentTemplatesContentFilter.ADDED_ASSOC_TEMPLATE_FIELD, nodeRef2.toString(), true);
    documentTemplatesContentFilter.afterPersist(item, data, persistedObject);
  }
  
  @Test
  public void testBeforePersist() {
    
    m.checking(new Expectations() {
      {
        oneOf(nodeService).exists(nodeRef2);
        will(returnValue(true));
        
        oneOf(nodeService).getProperty(nodeRef2, ContentModel.PROP_NAME);
        will(returnValue(node2name));
      }
    });
    
    documentTemplatesContentFilter.beforePersist(null, null);
    
    Object item = null;
    FormData data = new FormData();
    documentTemplatesContentFilter.beforePersist(item, data);
    data.addFieldData(DocumentTemplatesContentFilter.PROP_NAME_FIELD, "", true);
    documentTemplatesContentFilter.beforePersist(item, data);
    data.addFieldData(DocumentTemplatesContentFilter.PROP_NAME_FIELD, node1name, true);
    documentTemplatesContentFilter.beforePersist(item, data);
    
    data.addFieldData(DocumentTemplatesContentFilter.ADDED_ASSOC_TEMPLATE_FIELD, "notANodeRef", true);
    documentTemplatesContentFilter.beforePersist(item, data);
    
    data.addFieldData(DocumentTemplatesContentFilter.ADDED_ASSOC_TEMPLATE_FIELD, nodeRef2.toString(), true);
    documentTemplatesContentFilter.beforePersist(item, data);
    
    String newName = (String) data.getFieldData(DocumentTemplatesContentFilter.PROP_NAME_FIELD).getValue();
    String expectedName = node1name +"."+ FilenameUtils.getExtension(node2name);
    assertEquals("Name should match ", expectedName, newName);
  }

}
