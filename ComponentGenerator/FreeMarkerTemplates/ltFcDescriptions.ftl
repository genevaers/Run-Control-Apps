

digraph xml {
rankdir=LR

<#macro categorycolour type>
<#switch type>
  <#case "Arithmetic">
    "palegreen"<#break>
  <#case "Comparison">
   "paleturquoise"<#break>
  <#case "Assignment">
   "pink"<#break>
  <#case "Cookie">
   "orange"<#break>
  <#case "Function">
   "yellow"<#break>
  <#case "Lookup">
   "orange"<#break>
  <#case "Sort">
   "peachpuff"<#break>
  <#case "Write">
   "yellowgreen"<#break>
  <#default>"beige"</#switch></#macro>

<#assign fcTypeArguments = { 
  "F1": "F1 Description", 
  "NV": "New View Description" }>

<#macro argDescription fc>
<#if fc.ltDescription?? >
${fc.ltDescription!"FC defined yet"}
<#else>
${fcTypeArguments[fc.ltRecordType]!"Not defined yet"}
</#if>
</#macro>


graph [label="Logic Table Function Codes\n", labelloc=t, labeljust=center, fontname=Helvetica, fontsize=22];
labeljust=center; ranksep = "3 equally"

<#assign fcs = wm.segments["Function Codes"] /> 

subgraph cluster_FC { label="Function Codes" node [shape=plaintext]

FCS [label=<<TABLE BORDER="0" CELLBORDER="1" CELLSPACING="0">
    <TR>
        <TD ALIGN="LEFT" BGCOLOR="white" PORT="FCS">Function Code</TD>
        <TD ALIGN="LEFT" BGCOLOR="white">Category</TD>
        <TD ALIGN="LEFT" BGCOLOR="white">Description</TD>
        <TD ALIGN="LEFT" BGCOLOR="white">Record Type</TD>
        <TD ALIGN="LEFT" BGCOLOR="white">Arguments</TD>
    </TR>

<#list fcs.codes[0].fcdefs.codes as fc>
        <TR>
            <TD ALIGN="LEFT" BGCOLOR=<@categorycolour fc.category/>>${fc.functionCode}</TD>
            <TD ALIGN="LEFT" BGCOLOR=<@categorycolour fc.category/>>${fc.category}</TD>
            <TD ALIGN="LEFT" BGCOLOR=<@categorycolour fc.category/>>${fc.description}</TD>
            <TD ALIGN="LEFT" BGCOLOR=<@categorycolour fc.category/>>${fc.ltRecordType}</TD>
            <TD ALIGN="LEFT" BGCOLOR=<@categorycolour fc.category/>><@argDescription fc/></TD>
        </TR>
</#list>
    </TABLE>>]
}


}  
