<#include "/org/alfresco/components/form/controls/common/picker.inc.ftl" />

<#assign controlId = fieldHtmlId + "-cntrl">

<#-- BEGIN Custom code to get the configured templates folder path if any -->
<#if config?? && config.scoped?? && config.scoped["DocumentTemplates"]?? && config.scoped["DocumentTemplates"]["templates-folder-path"]??>
  <#assign templatePath = config.scoped["DocumentTemplates"]["templates-folder-path"].value>
<#else>
  <#assign templatePath = "/app:company_home/app:dictionary/app:node_templates">
</#if>
<#-- END Custom code to get the configured templates folder path if any -->

<#-- BEGIN Snitched from picker.inc.ftl to override startlocation -->

<#macro customRenderPickerJS field picker="picker" cloud=false>
   <#if field.control.params.selectedValueContextProperty??>
      <#if context.properties[field.control.params.selectedValueContextProperty]??>
         <#local renderPickerJSSelectedValue = context.properties[field.control.params.selectedValueContextProperty]>
      <#elseif args[field.control.params.selectedValueContextProperty]??>
         <#local renderPickerJSSelectedValue = args[field.control.params.selectedValueContextProperty]>
      <#elseif context.properties[field.control.params.selectedValueContextProperty]??>
         <#local renderPickerJSSelectedValue = context.properties[field.control.params.selectedValueContextProperty]>
      </#if>
   </#if>

   <#if cloud>
      var ${picker} = new Alfresco.CloudObjectFinder("${controlId}", "${fieldHtmlId}").setOptions(
   <#else>
      var ${picker} = new Alfresco.ObjectFinder("${controlId}", "${fieldHtmlId}").setOptions(
   </#if>
   {
      <#if form.mode == "view" || (field.disabled && !(field.control.params.forceEditable?? && field.control.params.forceEditable == "true"))>disabled: true,</#if>
      field: "${field.name}",
      customFolderStyleConfig: <#if customFolderStyleConfig??>${(customFolderStyleConfig!"")}<#else>null</#if>,
      compactMode: ${compactMode?string},
   <#if field.mandatory??>
      mandatory: ${field.mandatory?string},
   <#elseif field.endpointMandatory??>
      mandatory: ${field.endpointMandatory?string},
   </#if>
   <#if templatePath??>
      startLocation: "${templatePath}",
      <#if form.mode == "edit" && args.itemId??>currentItem: "${args.itemId?js_string}",</#if>
      <#if form.mode == "create" && form.destination?? && form.destination?length &gt; 0>currentItem: "${form.destination?js_string}",</#if>
   </#if>
   <#if field.control.params.startLocationParams??>
      startLocationParams: "${field.control.params.startLocationParams?js_string}",
   </#if>
      currentValue: "${field.value}",
      <#if field.control.params.valueType??>valueType: "${field.control.params.valueType}",</#if>
      <#if renderPickerJSSelectedValue??>selectedValue: "${renderPickerJSSelectedValue}",</#if>
      <#if field.control.params.selectActionLabelId??>selectActionLabelId: "${field.control.params.selectActionLabelId}",</#if>
      selectActionLabel: "${field.control.params.selectActionLabel!msg("button.select")}",
      minSearchTermLength: ${field.control.params.minSearchTermLength!'1'},
      maxSearchResults: ${field.control.params.maxSearchResults!'100'}
   }).setMessages(
      ${messages}
   );
</#macro>

<#-- END Snitched from picker.inc.ftl to override startlocation -->

<script type="text/javascript">//<![CDATA[
(function()
{
<#-- BEGIN Switch default picker for custom one -->
   <@customRenderPickerJS field "picker" />
<#-- END Switch default picker for custom one -->
   picker.setOptions(
   {
   <#if field.control.params.showTargetLink??>
      showLinkToTarget: ${field.control.params.showTargetLink},
      <#if page?? && page.url.templateArgs.site??>
         targetLinkTemplate: "${url.context}/page/site/${page.url.templateArgs.site!""}/document-details?nodeRef={nodeRef}",
      <#else>
         targetLinkTemplate: "${url.context}/page/document-details?nodeRef={nodeRef}",
      </#if>
   </#if>
   <#if field.control.params.allowNavigationToContentChildren??>
      allowNavigationToContentChildren: ${field.control.params.allowNavigationToContentChildren},
   </#if>
      itemType: "${field.endpointType}",
      multipleSelectMode: ${field.endpointMany?string},
      parentNodeRef: "alfresco://company/home",
<#-- BEGIN Switch root node for template path -->
   <#if templatePath??>
      rootNode: "${templatePath}",
   </#if>
<#-- END Switch root node for template path -->
      itemFamily: "node",
      displayMode: "${field.control.params.displayMode!"items"}"
   });
})();
//]]></script>

<div class="form-field">
   <#if form.mode == "view">
      <div id="${controlId}" class="viewmode-field">
         <#if (field.endpointMandatory!false || field.mandatory!false) && field.value == "">
            <span class="incomplete-warning"><img src="${url.context}/res/components/form/images/warning-16.png" title="${msg("form.field.incomplete")}" /><span>
         </#if>
         <span class="viewmode-label">${field.label?html}:</span>
         <span id="${controlId}-currentValueDisplay" class="viewmode-value current-values"></span>
      </div>
   <#else>
      <label for="${controlId}">${field.label?html}:<#if field.endpointMandatory!false || field.mandatory!false><span class="mandatory-indicator">${msg("form.required.fields.marker")}</span></#if></label>
      
      <div id="${controlId}" class="object-finder">
         
         <div id="${controlId}-currentValueDisplay" class="current-values"></div>
         
         <#if field.disabled == false>
            <input type="hidden" id="${fieldHtmlId}" name="-" value="${field.value?html}" />
            <input type="hidden" id="${controlId}-added" name="${field.name}_added" />
            <input type="hidden" id="${controlId}-removed" name="${field.name}_removed" />
            <div id="${controlId}-itemGroupActions" class="show-picker"></div>
         
            <@renderPickerHTML controlId />
         </#if>
      </div>
   </#if>
</div>
