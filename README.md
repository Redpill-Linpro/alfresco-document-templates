# Alfresco Document Templates

With this addon enabled the user can when creating content choose from a set of predefined templates residing in either the data-dictionary/node-templates folder or in a special template-library site.

To bootstrap the template-library site the property `template-library.site.disabled` in alfresco-global.properties needs to be set to false.



## Enable the Document templates module in Alfresco Share
To enable the possibility to pick a template for alfresco datatypes this module needs to be enabled in share. Navigate to `http://[your-host]:[port]/page/modules/deploy` and enable the "Redpill Linpro Default Document Templates" module.

## Activate templates for certain content types
To activate a document template for a content type (for example cm:content) edit your share configuration files. See example in `alfresco-document-templates-extension.xml` file.
```
<extension>
  <modules>

    <module>
      <id>Redpill Linpro Default Document Templates</id>
      <auto-deploy>false</auto-deploy>
      <version>${project.version}</version>

      <configurations>
        <config evaluator="string-compare" condition="DocumentLibrary">
          <create-content>
            <content id="content" label="create-content.document" type="pagelink" index="6" icon="text">
              <param name="page">create-content?destination={nodeRef}&amp;itemId=cm:content</param>
            </content>
          </create-content>
        </config>

        <config evaluator="model-type" condition="cm:content">
          <forms>
            <!-- Default Create Content form -->
            <form>
              <field-visibility>
                <show id="cm:name" />
                <show id="cm:title" force="true" />
                <show id="cm:description" force="true" />
                <show id="rplpdt:template" force="true" />
              </field-visibility>
              <appearance>
                <field id="cm:name">
                  <control>
                    <control-param name="maxLength">255</control-param>
                  </control>
                </field>
                <field id="cm:title">
                  <control template="/org/alfresco/components/form/controls/textfield.ftl" />
                </field>
                <!-- This field is used to control document templates for this type. A temporary aspect attached to the node when created and cleared by a formfilter afterwards -->
                <field id="rplpdt:template" mandatory="true">
                  <control template="/org/redpill_linpro/alfresco/share/components/form/controls/documenttemplate.ftl" />
                </field>
              </appearance>
            </form>
          </forms>
        </config>
      </configurations>
    </module>

  </modules>
</extension>
```

To configure which folder to pick templates from (default is Data Dictionary/Node Templates). Edit your share-config-custom.xml file for your installation and add the following config section:

```
<alfresco-config>
...
  <config evaluator="string-compare" condition="DocumentTemplates" replace="true">
    <!-- Defaults to the Node templates directory in the data dictionary -->
    <templates-folder-path>/app:company_home/app:dictionary/app:node_templates</templates-folder-path>
    <!-- If the template-library site patch is activated the templates are choosen like below. -->
    <templates-folder-path>/app:company_home/st:sites/cm:template-library/cm:documentLibrary/cm:document-templates</templates-folder-path>
  </config>
...
</alfresco-config>
```