VDP Record Summary Report
-------------------------------------------------------------------------------

-------------------------------------------------------------------------------
 Component Type              Count
-------------------------------------------------------------------------------
 User Exit Routines          ${userExits?size?c}
 Control Records             ${controlRecords?size?c}
 Physical Files              ${physicalFiles?size?c}
 Logical Files               ${logicalFiles?size?c}
 Logical Records             ${logicalRecords?size?c}
 Lookup Paths                ${lookupPaths?size?c}
 Views                       ${views?size?c}


 Component Type                ID  Name
-------------------------------------------------------------------------------
<#list userExits as ue>
 ${"User Exit Routine"?right_pad(24)} ${ue.componentId?c?left_pad(7)}  ${ue.name}
</#list>

<#list controlRecords as cr>
 ${"Control Record"?right_pad(24)} ${cr.componentId?c?left_pad(7)}  ${cr.name}
</#list>

<#list physicalFiles as pf>
 ${"Physical File"?right_pad(24)} ${pf.componentId?c?left_pad(7)}  ${pf.name}
</#list>

<#list logicalFiles as lf>
 ${"Logical File"?right_pad(24)} ${lf.id?c?left_pad(7)}  ${lf.name}
   PF ID           PF Name
<#list lf.physicalFiles as pf>
   ${pf.componentId?c?right_pad(12)}${pf.name?right_pad(35)}
</#list>

</#list>
<#list logicalRecords as lr>
 Logical Record          ${lr.componentId?c}  ${lr.name}
  ${"Field ID"?right_pad(12)}${"Field Name"?right_pad(35)}${"Data Type"}
<#list lr.fields as field>
  ${field.componentId?c?right_pad(12)}${field.name?right_pad(35)}${field.datatype}
</#list>

</#list>
<#list lookupPaths as lp>
 Lookup Path             ${lp.id?c}  ${lp.name}
   Step   Seq Num    Src LR    Trg LR    Trg File
--------------------------------------------------------------------------------------
<#list lp.steps as step>
   ${step.stepNum?c?right_pad(8)}${step.seqNum?c?right_pad(10)}${step.sourceLR?c?right_pad(10)}${step.targetLR?c?right_pad(10)}${step.targetLF?c?right_pad(12)}
</#list>

</#list>
<#list views as view>
 View                    ${view.id?c}  ${view.name}
   Extract to ${view.outputFileName}
   Src Num        LR        LF
<#list view.sources as vs>
   ${vs.sequenceNumber?c?right_pad(12)}${vs.sourceLRID?c?right_pad(10)}${vs.sourceLFID?c}
</#list>
   Number of columns:   ${view.numberOfColumns?c}
   Extract Length:      ${view.extractLength?c}
   Number of sort keys: ${view.numberOfSortKeys?c}

</#list>
<#if controlRecords?size == 0>
Control Record Report
 *---------------------------------------------------------------------
   <none>

 Total Number of Control Record records: 0

<#else>
Control Record Report
<#list controlRecords as cr>
*---------------------------------------------------------------------
   ID                          ${cr.componentId?c}
   Name                        ${cr.name}
   First Fiscal Month          ${cr.firstFiscalMonth?c}
   Beginning Period            ${cr.beginningPeriod}
   Ending Period               ${cr.endingPeriod}
   Max Extr file Num           0

</#list>
 Total Number of Control Record records: ${controlRecords?size?c}

</#if>
<#if physicalFiles?size == 0>
Physical File Report
 *---------------------------------------------------------------------
   <none>

 Total Number of Physical File records: 0

<#else>
Physical File Report
<#list physicalFiles as pf>
 *---------------------------------------------------------------------
   ID                          ${pf.componentId?c}
   Name                        ${pf.name}
   File Type                   ${pf.fileType}
   Access Method               ${pf.accessMethod}
   Dataset Input Attributes
     Input DD Name             ${pf.inputDDName}
     DSN                       ${pf.dataSetName}
     PF_RD_DISP                
     Record Length             ${pf.minimumLength?c}
     Max Record Length         ${pf.maximumLength?c}
   Dataset Output Attributes
     Output DD Name            ${pf.outputDDName}
     Device Type               
     RECFM                     ${pf.recfm}
     LRECL                     ${pf.lrecl?c}

</#list>
 Total Number of Physical File records: ${physicalFiles?size?c}

</#if>
<#if logicalFiles?size == 0>
Logical File Report
 *---------------------------------------------------------------------
   <none>

 Total Number of Logical File records: 0

<#else>
Logical File Report
<#list logicalFiles as lf>
 *---------------------------------------------------------------------
   ID                          ${lf.id?c}
   Name                        ${lf.name}
   Associated Physical Files
<#list lf.physicalFiles as pf>
     ID                        ${pf.componentId?c}
     Name                      ${pf.name}

</#list>
</#list>
 Total Number of Logical File records: ${logicalFiles?size?c}

</#if>
<#if logicalRecords?size == 0>
Logical Record Report
 *---------------------------------------------------------------------
 
 Total Number of Logical Record records: 0

<#else>
Logical Record Report
<#list logicalRecords as lr>
 *---------------------------------------------------------------------
   ID                          ${lr.componentId?c}
   Name                        ${lr.name}
   Status                      ${lr.status}
   Lookup Exit                 ${lr.lookupExitID?c}
   Lookup Exit Params          ${lr.lookupExitParams}
   LR Fields
<#list lr.fields as field>
     ID                        ${field.componentId?c}
     Name                      ${field.name}
     Data Type                 ${field.datatype}
     Fixed Position            ${field.startPosition?c}
     Length                    ${field.length?c}
     Decimal Places            ${field.numDecimalPlaces?c}
     Primary Key Sequence #    
     Effective Date            
     Ordinal Position          ${field.ordinalPosition?c}
     Ordinal Offset            ${field.ordinalOffset?c}
     Scaling                   ${field.rounding?c}
     Date/Time Format          ${field.dateTimeFormat}
     Align Heading             ${field.justification}
     Numeric Mask              ${field.mask}
     DBMS ColName              ${field.dbColName}

</#list>
</#list>
 Total Number of Logical Record records: ${logicalRecords?size?c}

</#if>
Lookup Path Report
<#list lookupPaths as lp>
 *---------------------------------------------------------------------
   ID                          ${lp.id?c}
   Name                        ${lp.name}
   Lookup Steps                ${lp.numberOfSteps?c}
<#list lp.steps as step>
   Step Number ${step.stepNum?c}
     Source Logical Record     ${step.sourceLRName}
     Target Logical Record     ${step.targetLRName}
     Target Logical File       ${step.targetLFName}
     Source Field Properties   
<#list step.keys as key>
       Source Field Seq Num ${key.keyNumber?c}
<#if key.fieldId gt 0>
         Source Type           LR Field
         LR Field              ${key.fieldId?c}
         LR                    ${step.sourceLRName}
<#elseif key.symbolicName?has_content>
         Source Type           Symbol
         Symbol                ${key.symbolicName}
         Default Symbol        ${key.value}
<#else>
         Source Type           Constant
         Constant              ${key.value}
</#if>
         Data Attributes       
           Data Type           ${key.datatype}
           Length              ${key.length?c}
           Scaling Factor      ${key.rounding?c}
           Date/Time Format    ${key.dateTimeFormat}
           Decimal Places      ${key.decimalCount?c}
           Signed              <#if key.signed>Y<#else>N</#if>
</#list>
</#list>

</#list>
 Total Number of Lookup Path records: ${lookupPaths?size?c}

View Properties Report 
<#list views as view>
*---------------------------------------------------------------------
   View ID                     ${view.id?c}
   View Name                   ${view.name}
   Status                      ${view.status}
   View Phase                  ${view.viewPhase}
   Output Format               TO DO
   View Aggregation Level      ${view.aggregationLevel}
   Lines Per Page              ${view.linesPerPage?c}
   Report Width                ${view.reportWidth?c}
   View folder ID              TO DO
   Control Record              ${view.controlRecordName}
   Extract Phase               
     Output Logical File       ${view.outputLogicalFile}
     Output Physical File      ${view.outputPhysicalFile}
     User Exit Name            ${view.userExitName}
     User Exit Parameters      ${view.userExitParams}
     Record Aggregation
     Buffer Size               
     Output Limit
  View Sources
<#list view.sources as vs>
     View Source ${vs.sequenceNumber?c}
       Source ID               ${vs.sourceID}
       Logical Record          ${vs.sourceLRName}
       Logical File            ${vs.sourceLFName}
       Record Filter           ***see View Logic Report***
</#list>
   Column Properties
<#if view.columns??>
<#list view.columns as column>
     Column Data
       Column ID               ${column.columnId?c}
       Name                    ${column.name}
       Ordinal Position        ${column.ordinalPosition?c}
       Extract Area            ${column.extractArea}
       Column Output Properties
         Heading 1             ${column.heading1}
         Heading 2             ${column.heading2}
         Heading 3             ${column.heading3}
         Start Position        ${column.startPosition?c}
         Data Type             ${column.dataType}
         Date/Time Format      ${column.dateTimeFormat}
         Length                ${column.length?c}
         Data Alignment        ${column.dataAlignment}
         Visible Flag          ${column.visibleFlag?c}
         Spaces before column  ${column.spacesBeforeColumn?c}
         Header Alignment      ${column.headerAlignment}
         Decimal Places        ${column.decimalPlaces?c}
         Scaling Factor        ${column.scalingFactor?c}
         Signed Flag           ${column.signedFlag?c}
         Numeric Mask          ${column.numericMask}
         Format Phase Calc     ${column.formatPhaseCalc}
<#if column.columnSources??>
<#list column.columnSources as source>
       Column Source Properties ${source.sourceNumber?c}
         ID                    ${source.id?c}
         Column Source Type    ${source.sourceType}
<#if source.sourceValue?has_content>
         Column Source Value   ${source.sourceValue}
</#if>
<#if source.sourceFieldId??>
         Column Source Field   ${source.sourceFieldName!""} ${source.sourceFieldId?c}
</#if>
<#if source.lookupPath??>
         Column Lookup Path    ${source.lookupPath!""}
</#if>
<#if source.lookupLR??>
         Column Lookup LR      ${source.lookupLR!""}
</#if>
<#if source.lookupField??>
         Column Lookup Field   ${source.lookupField!""}
</#if>
</#list>
</#if>
</#list>
</#if>              

</#list>
<#if compareMode>
Comparison Summary
==================
Source1: ${source1}
Source2: ${source2}

</#if>