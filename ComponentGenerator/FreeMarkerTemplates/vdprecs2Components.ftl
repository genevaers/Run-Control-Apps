

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
   "skyblue"
    <#break>
  <#case "byte">
   "orange"
    <#break>
  <#case "enum">
   "yellow"
    <#break>
  <#case "boolean">
   "beige"
    <#break>
  <#case "String">
   "pink"
    <#break>
  <#default>
    "yellow"
</#switch></#macro>


graph [label="VDP Records to Component\n", labelloc=t, labeljust=center, fontname=Helvetica, fontsize=22];
labeljust=center; ranksep = "3 equally"

<#assign comps = wm.segments["Components"] /> 

<#list comps.components?reverse as k>
    
subgraph cluster_${k.name} { label="${k.name}" node [shape=plaintext]
        
${k.name} [label=<<TABLE BORDER="0" CELLBORDER="1" CELLSPACING="0">
    <TR>
        <TD ALIGN="LEFT" BGCOLOR="white">Name</TD>
        <TD ALIGN="LEFT" BGCOLOR="white">Type</TD>
    </TR>
    <#list k.component.members as mm>
    <TR>
        <TD ALIGN="LEFT" BGCOLOR=<@compcolour mm.type!"missing"/>  PORT="${mm.name}" >${mm.name}</TD>
        <TD ALIGN="LEFT" BGCOLOR=<@compcolour mm.type!"missing"/>  >${mm.type!"missing"}</TD>
    </TR>
    </#list>
    </TABLE>>]
}
</#list> 

<#assign vdprs = wm.segments["VDPRecords"] />
<#list vdprs.records?reverse as r>
<#assign record = r["record"] />
<#if record.componentName != "none" && record.recordName != "VDPPrefix">
subgraph cluster_${record.recordName} { label="${record.recordName} (ID ${record.recordId?c})" node [shape=plaintext]
        
${record.recordName} [label=<<TABLE BORDER="0" CELLBORDER="1" CELLSPACING="0">
    <TR>
        <TD ALIGN="LEFT" BGCOLOR="white">Name</TD>
        <TD ALIGN="LEFT" BGCOLOR="white">Type</TD>
    </TR>
    <#assign prefix = vdprs.records?first />
    <#list prefix.record.fields as ff>
        <TR>
            <TD ALIGN="LEFT" BGCOLOR=<@vdpcolour ff.type/>  >${ff.name}</TD>
            <TD ALIGN="LEFT" BGCOLOR=<@vdpcolour ff.type/>  PORT="${ff.name?replace(".","PP")}" >${ff.type!"missing"}</TD>
        </TR>
    </#list>
    <#list record.fields as ff>
        <#if !ff.name?starts_with("prefix")>
        <TR>
        <#if ff.componentField != "none">
            <TD ALIGN="LEFT" BGCOLOR=<@vdpcolour ff.type!"missing"/>  >${ff.name}</TD>
            <TD ALIGN="LEFT" BGCOLOR=<@vdpcolour ff.type!"missing"/>  PORT="${ff.name?replace(".","PP")}" >${ff.type!"missing"}</TD>
        <#else>
            <TD ALIGN="LEFT" BGCOLOR="lightgrey"  >${ff.name}</TD>
            <TD ALIGN="LEFT" BGCOLOR="lightgrey"  PORT="${ff.name?replace(".","PP")}" >${ff.type!"missing"}</TD>
        </#if>
        </TR>
        </#if>
    </#list>
    </TABLE>>]
}
    <#list record.fields as rr>
        <#if rr.componentField != "none">
            <#if rr.name?starts_with("prefix")>
            ${record.recordName}:${rr.type} -> ${record.componentName}:${rr.componentField} [dir="both"]
            </#if>
            <#if !rr.name?starts_with("prefix")>
            ${record.recordName}:${rr.name} -> ${record.componentName}:${rr.componentField} [dir="both"]
            </#if>
        </#if>
    </#list>
</#if>
</#list>
}
