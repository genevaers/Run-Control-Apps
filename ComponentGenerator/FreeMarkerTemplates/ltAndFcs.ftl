

digraph xml {
rankdir=LR

<#macro vdpcolour type>
<#switch type>
  <#case "int">
    "palegreen"<#break>
  <#case "short">
   "skyblue"<#break>
  <#case "byte">
   "orange"<#break>
  <#case "Cookie">
   "orange"<#break>
  <#case "enum">
   "yellow"<#break>
  <#case "boolean">
   "beige"<#break>
  <#default>"pink"</#switch></#macro>

<#macro compcolour type>
<#switch type>
  <#case "int">
    "palegreen"<#break>
  <#case "short">
   "skyblue"<#break>
  <#case "byte">
   "orange"<#break>
  <#case "enum">
   "yellow"<#break>
  <#case "boolean">
   "beige"<#break>
  <#case "String">
   "pink"<#break>
  <#default>
    "yellow"</#switch></#macro>


graph [label="Logic Table Records and Function Codes\n", labelloc=t, labeljust=center, fontname=Helvetica, fontsize=22];
labeljust=center; ranksep = "3 equally"

<#assign fcs = wm.segments["Function Codes"] /> 

<#--<#list fcs.codes[0].fcdefs.codes?sort_by("ltRecordType") as fc>
 ${fc.ltRecordType} ${fc.functionCode}
</#list>

<#list fcs.codes[0].fcdefs.codes?sort_by("ltRecordType")?filter(l -> l.ltRecordType == "F1") as x>
${x.ltRecordType} ${x.functionCode}
</#list> -->

<#assign lts = wm.segments["LTRecords"] /> 

<#list lts.records?reverse as ltr>
<#if ltr.name != "LTPrefix" && ltr.name != "LogicTableArg">

<#assign prefix = lts.records?first />
<#assign arg = lts.records[1] />


subgraph cluster_${ltr.name} { label="${ltr.name}" node [shape=plaintext]

       
${ltr.name} [label=<<TABLE BORDER="0" CELLBORDER="1" CELLSPACING="0">
    <TR>
        <TD ALIGN="LEFT" BGCOLOR="white">Name</TD>
        <TD ALIGN="LEFT" BGCOLOR="white">Type</TD>
    </TR>
    <#list prefix.record.fields as ff>
        <TR>
            <TD ALIGN="LEFT" BGCOLOR=<@vdpcolour ff.type/>  >${ff.name}</TD>
            <TD ALIGN="LEFT" BGCOLOR=<@vdpcolour ff.type/> PORT="${ff.name}" >${ff.type!"missing"}</TD>
        </TR>
    </#list>
    <#list ltr.record.fields as mm>
        <#if mm.name?starts_with("arg")>
        <#list arg.record.fields as ff>
            <#if ff.name != "value_area">
            <TR>
                <TD ALIGN="LEFT" BGCOLOR=<@vdpcolour ff.type!"notype"/>  >${ff.name}</TD>
                <TD ALIGN="LEFT" BGCOLOR=<@vdpcolour ff.type!"notype"/>  >${ff.type!"missing"}</TD>
            </TR>
            </#if>
        </#list>
        </#if> 
        <#if !mm.name?starts_with("prefix") && !mm.name?starts_with("arg")>
        <TR>
            <TD ALIGN="LEFT" BGCOLOR=<@compcolour mm.type!"missing"/>  >${mm.name}</TD>
            <TD ALIGN="LEFT" BGCOLOR=<@compcolour mm.type!"missing"/> >${mm.type!"missing"}</TD>
        </TR>
        </#if>
    </#list>
    </TABLE>>]
}

subgraph cluster_${ltr.name}_FC { label="${ltr.name} Function Codes" node [shape=plaintext]
       
${ltr.name}_FC [label=<<TABLE BORDER="0" CELLBORDER="1" CELLSPACING="0">
    <TR>
        <TD ALIGN="LEFT" BGCOLOR="white" PORT="FCS">Function Code</TD>
        <TD ALIGN="LEFT" BGCOLOR="white">Category</TD>
        <TD ALIGN="LEFT" BGCOLOR="white">Description</TD>
    </TR>
    <#list fcs.codes[0].fcdefs.codes?sort_by("ltRecordType")?filter(l -> l.ltRecordType == ltr.name?remove_beginning("LogicTable")?upper_case) as x>
        <TR>
            <TD ALIGN="LEFT" BGCOLOR="beige">${x.functionCode}</TD>
            <TD ALIGN="LEFT" BGCOLOR="beige">${x.category}</TD>
            <TD ALIGN="LEFT" BGCOLOR="beige">${x.description}</TD>
        </TR>
    </#list>
    </TABLE>>]
}

${ltr.name}:function_code -> ${ltr.name}_FC:FCS

</#if>
</#list> 


}  
