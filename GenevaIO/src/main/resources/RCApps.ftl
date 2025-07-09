<#assign aDateTime = .now>
<#assign aDate = aDateTime?date>
<#assign aTime = aDateTime?iso_utc>
~RCARPT
 
GenevaERS - The Single-Pass Optimization Engine
(https://genevaers.org)
Licensed under the Apache License, Version 2.0
Performance Engine for z/OS - Base Product
Release PM ${peversion}
 
Program ID:      GVBRCA (${rcaversion})
Program Title:   Run-Control App
Built:           ${buildtimestamp}
 
Executed:        ${aDate?iso_utc} ${aTime?substring(11, 16)}

Report DD Name:  RCARPT
Report Title:    GVBRCA Control Report
 
 
================
Report Sections:
================
 
    Tag    Section name
    -----  ------------------------------------------------------
    ~PARM  Contents of RCAPARM file
    ~OPTS  Options in effect
<#if generate> 
    ~DFOL  Contents of DBFLDRS file
    ~DVWS  Contents of DBVIEWS file
    ~RUNV  Contents of RUNVIEWS file
    ~VIEW  Views selected
</#if>
    ~IFIL  Input files
    ~OFIL  Output files
<#if generate> 
    ~REFW  Reference Work Files
    ~WRNS  SAFR compiler warnings
    ~ERRS  SAFR compiler errors
</#if>
    ~EXEC  Execution summary
 
 
==================================
~PARM - Contents of RCAPARM file:
==================================

<#list parmsRead as parm>
${parm}
</#list>
 
 
==========================
~OPTS - Options in effect:
==========================
 
<#list optsInEffect as opt>
${opt}
</#list>
 
<#if generate> 

==================================
~DFOL - Contents of DBFLDRS file:
==================================
 
<#list dbfolders as opt>
${opt}
</#list>
 
==================================
~DVWS - Contents of DBVIEWS file:
==================================
 
<#list dbviews as opt>
${opt}
</#list>
 
==================================
~RUNV - Contents of RUNVIEWS file:
==================================
 
<#list runviews as opt>
${opt}
</#list>
 
=======================
~VIEW - Views selected:
=======================
 
                                                           Output              >Output         ERA  ERA Buf Size  FRA
View ID  View Name                                         Phase               >Format         On   (in Records)  On
=======  ------------------------------------------------  ---------           >-------------  ---  ------------  ---
<#if views?? > 
<#list views as v>
${v.IDStr}  ${v.name?right_pad(48)}  ${v.phase?right_pad(9)}           >${v.outputFormat?right_pad(13)}  ${v.ERAon?right_pad(3)}  ${v.ERAsize?left_pad(12)}  ${v.FRAon}
</#list>
<#else>
<none>
</#if>
 
 
====================
~IFIL - Input files:
====================
 
DD Name   Member    Create Date/Time   
========  --------  -------------------
<#list inputReports as ir>
${ir.ddName?right_pad(8)}  ${ir.memberName}  ${ir.generationID!0} 
</#list>
 
 
=====================
~OFIL - Output files:
=====================
 
DD Name   Member    Record Count
========  ========  ------------
VDP                 ${vdpRecordsWritten!0?left_pad(12)}
JLT                 ${jltRecordsWritten!0?left_pad(12)}
XLT                 ${xltRecordsWritten!0?left_pad(12)}
 
 
===================================
~REFW - Reference-phase Work Files:
===================================
<#if refviews??> 
 
Ref Work  Runtime                                            Ref       Ref     >Ref                                               Ref      Ref      Key  St  En 
DD Name   View ID  View Name                                 DD Name   PF ID   >PF Name                                           LR ID    LF ID    Len  Dt  Dt
========  -------  ----------------------------------------  --------  ------- >------------------------------------------------  -------  -------  ---  --  --
<#list refviews as r>
${r.workDDName}   <#if r.viewID gt 0>${r.viewID?c?left_pad(7)}<#else>       </#if>  ${r.viewName?right_pad(40)}  ${r.refDDName?right_pad(8)}  ${r.refPFID?c?left_pad(7)} >${r.refPFName?right_pad(48)}  ${r.refLRID?c?left_pad(7)}  ${r.refLFID?c?left_pad(7)}  ${r.keylen?c?left_pad(3)}  ${r.effStart}  ${r.effEnd}
</#list>
<#--<#if reh??>
${reh.outputFile.outputDDName}   ${reh.ID?c}  ${reh.name?right_pad(40)}
</#if>
<#if rth??>
${rth.outputFile.outputDDName}   ${rth.ID?c}  ${rth.name?right_pad(40)}
</#if><#-->
<#else>
<none>
</#if>
 
===============================
~WRNS - Warnings:
===============================
View    Loc            SrcLR   SrcLF Col Message
------- ------------ ------- ------- --- -------------------------------------------------------------------------------
<#if warnings?size == 0> 
<none>
<#else>
<#list warnings as warn>
${warn.viewid?c?left_pad(7)} ${warn.source?right_pad(12)} ${warn.srcLR?c?left_pad(7)} ${warn.srcLF?c?left_pad(7)} ${warn.columnNumber?left_pad(3)} ${warn.detail}
</#list>
</#if> 
 
=============================
~ERRS - Errors:
=============================
View    Loc            SrcLR   SrcLF Col Message
------- ------------ ------- ------- --- -------------------------------------------------------------------------------
<#if compErrs?size == 0> 
<none>
<#else>
<#list compErrs as err>
${err.viewid?c?left_pad(7)} ${err.source?right_pad(12)} ${err.srcLR?c?left_pad(7)} ${err.srcLF?c?left_pad(7)} ${err.columnNumber?left_pad(3)} ${err.detail}
</#list>
</#if>

</#if>
 
 
==========================
~EXEC - Execution summary:
==========================
 
<#if generate> 
Run Control Generation
----------------------

Number of warnings:              ${warnings?size!0?c?left_pad(11)}
Number of errors:                ${compErrs?size!0?c?left_pad(11)}
Number of reference-phase views: ${numrefviews!0?c?left_pad(11)}
Number of extract-phase views:   ${numextviews!0?c?left_pad(11)}
 
<#if compErrs?size == 0> 
Compilation completed successfully
<#else>
There were compilation errors. No Run Control Files written.
</#if>
</#if>

<#if analyse> 
Run Control Analysis
--------------------
<#if compare> 
Comparison Mode

VDP difference count = ${numVDPDiffs}
XLT difference count = ${numXLTDiffs}
JLT difference count = ${numJLTDiffs}

</#if>
<#if vdpreport>
VDP Report written
</#if>
<#if xltreport>
XLT Report written
</#if>
<#if jltreport>
JLT Report written
</#if>
</#if>


Overall Status: ${status}
See log file for details.
