

digraph xml {
rankdir=LR

<#macro vdpcolour type>
<#switch type>
  <#case "int">
    "palegreen"
    <#break>
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
  <#default>
    "pink"
</#switch>
</#macro>

graph [label="VDP Management Records\n", labelloc=t, labeljust=center, fontname=Helvetica, fontsize=22, ordering=out];
labeljust=center; ranksep = "3 equally"

<#assign vdprs = wm.segments["VDPRecords"] />
<#list vdprs.records?reverse as r>
<#assign record = r["record"] />
<#if record.componentName == "none" && record.recordName != "VDPPrefix">
subgraph cluster_${record.recordName} { label="${record.recordName} (ID ${record.recordId?c})" node [shape=plaintext]
        
${record.recordName} [label=<<TABLE BORDER="0" CELLBORDER="1" CELLSPACING="0">
    <TR>
        <TD ALIGN="LEFT" BGCOLOR="white">Name</TD>
        <TD ALIGN="LEFT" BGCOLOR="white">Type</TD>
    </TR>
    <#assign prefix = vdprs.records?first />
    <#list prefix.record.fields as ff>
        <TR>
            <TD ALIGN="LEFT" BGCOLOR=<@vdpcolour ff.type!"missing"/>  >${ff.name}</TD>
            <TD ALIGN="LEFT" BGCOLOR=<@vdpcolour ff.type!"missing"/>  >${ff.type!"missing"}</TD>
        </TR>
    </#list>
    <#list record.fields as ff>
        <#if !ff.name?starts_with("prefix")>
        <TR>
            <TD ALIGN="LEFT" BGCOLOR=<@vdpcolour ff.type!"missing"/>  >${ff.name}</TD>
            <TD ALIGN="LEFT" BGCOLOR=<@vdpcolour ff.type!"missing"/>  >${ff.type!"missing"}</TD>
        </TR>
        </#if>
    </#list>
    </TABLE>>]
}
</#if>
</#list>
}
