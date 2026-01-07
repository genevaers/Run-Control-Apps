<#assign aDateTime = .now>
<#assign aDate = aDateTime?date>
<#assign aTime = aDateTime?iso_utc>
~PERPT
 
GenevaERS - The Single-Pass Optimization Engine
(https://genevaers.org)
Licensed under the Apache License, Version 2.0
Performance Engine for z/OS - Base Product
Release PM ${peversion}
 
Program ID:      GVBPE (${rcaversion})
Program Title:   Performance Engine
Built:           ${buildtimestamp}
 
Executed:        ${aDate?iso_utc} ${aTime?substring(11, 16)}

Report DD Name:  PERPT
Report Title:    Performance Engine Report
 
 
================
Report Sections:
================
 
    Tag    Section name
    -----  ------------------------------------------------------
    ~PARM  Contents of PEPARM file
    ~OPTS  Options in effect
    ~EXEC  Execution summary
 
 
==================================
~PARM - Contents of PEPARM file:
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
 
==========================
~EXEC - Execution summary:
==========================
 

Overall Status: ${status}
See log file for details.
