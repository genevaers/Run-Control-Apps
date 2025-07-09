//${test.name}F JOB (${env["GERS_JOB_ACCT_INFO"]}),'REGRESSION JOB',
//            NOTIFY=&SYSUID.,
//            CLASS=${env["GERS_JOB_CLASS"]},
//            MSGLEVEL=${env["GERS_MSG_LEVEL"]},
//            MSGCLASS=${env["GERS_MSG_CLASS"]},
//            REGION=0M
//*
//JOBLIB   DD DISP=SHR,DSN=${env["OVERRIDE"]}
//         DD DISP=SHR,DSN=${env["PMLOAD"]}
<#include "../common/generation.ftl"/>
<#macro qualifiedTest>
<#-- To avoid the line break at the end I butted up the closing tag -->
${env["GERS_TEST_HLQ"]}.${test.dataSet}</#macro>
<#assign extFiles = test.extractfiles?size>
<#assign fmtFiles = test.formatfiles?size>
<#if fmtFiles gt 0>
//**********************************************************************
//* Number of extract files ${extFiles}
//* Number of format files ${fmtFiles}
//*
//* 
<#if extFiles gt 1>
//******************************************************************
//* For each ExtractFile specified in the test we want a run of MR88
//* The FormatFiles to be generated from each extract are referenced 
//* Via its WORKFILE
//*
</#if>
<#list test.extractfiles as ext>
    <#assign wfNum = ext?counter>
<#if extFiles gt 1>
    <#assign useFormatList = test.getFormatfilesByWorknum(wfNum)>
//******************************************************************
//*
//* MR88 run for extract workfile ${wfNum}
//* 
//* That is Format Views:
    <#list test.getFormatfilesByWorknum(wfNum) as wf>
//*        ${wf.ddname}
    </#list>
<#else>
    <#assign useFormatList = test.formatfiles>
</#if>
//*
//**********************************************************************
//* PSTEP200 - DELETE THE FILE(S) CREATED IN NEXT STEP
//*
//**********************************************************************
//*
//PSTEP200 EXEC PGM=IDCAMS
//*
//SYSPRINT DD SYSOUT=*
//*
//SYSIN    DD *
<#list useFormatList as fmt>
<#if fmt.dsn??>
 DELETE  ${fmt.dsn} PURGE
<#else>
 DELETE  <@qualifiedTest/>.OUTF.MR88.${fmt.ddname} PURGE
 </#if>
 IF LASTCC > 0  THEN        /* IF OPERATION FAILED,     */    -
     SET MAXCC = 0          /* PROCEED AS NORMAL ANYWAY */
</#list>
<#macro qualifiedTest>
<#-- To avoid the line break at the end I butted up the closing tag -->
${env["GERS_TEST_HLQ"]}.${test.dataSet}</#macro>
//*********************************************************************
//* PSTEP205 - SUMMARIZE AND FORMAT VIEWS
//*
//*********************************************************************
//*
//PSTEP205 EXEC PGM=GVBMR88,
// REGION=0M
//*
//*        INPUT GENEVA FILES
//*
//MR88VDP  DD DSN=<@qualifiedTest/>.RCA.VDP,
//            DISP=SHR
//*
//SYSIN    DD DSN=<@qualifiedTest/>.OUTE.MR95.SORT${ext?counter?left_pad(3,"0")},
//            DISP=SHR
//*
//SORTCNTL DD DSN=<@qualifiedTest/>.PARM(REGRF88C),
//            DISP=SHR
//*
//SORTIN   DD DSN=<@qualifiedTest/>.OUTE.MR95.EXTR${ext?counter?left_pad(3,"0")},
//            DISP=SHR
//*
//MR88MSTR DD DUMMY,BLKSIZE=2408
//*
//REFRRTH  DD DSN=<@qualifiedTest/>.OUTR.MR95.RTH,
//            DISP=SHR
//*
//*
<#list test.reffiles as ref>
//REFR${ref?counter?left_pad(3,"0")} DD DSN=<@qualifiedTest/>.OUTR.MR95.F${ref?counter?left_pad(3,"0")}.RED,
//            DISP=SHR
</#list>
//*
//*        OUTPUT GENEVA FILES
//*
//MR88HXM  DD DUMMY,BLKSIZE=27998
//MR88PARM DD DUMMY
//*
//MR88HXD  DD DUMMY,BLKSIZE=2458
//MR88RPT  DD SYSOUT=*
//MR88LOG  DD SYSOUT=*
//SYSOUT   DD SYSOUT=*
//SORTDIAG DD SYSOUT=*
//CEEDUMP  DD SYSOUT=*
//SYSABEND DD SYSOUT=*
//*
//*        OUTPUT VIEW FILES
<#list useFormatList as fmt>
<#if fmt.dsn??>
//${fmt.ddname}  DD DSN=${fmt.dsn},
<#else>
//${fmt.ddname}  DD DSN=<@qualifiedTest/>.OUTF.MR88.${fmt.ddname},
</#if>
//           DISP=(NEW,CATLG,DELETE),
//           UNIT=SYSDA,
//           SPACE=(${fmt.space},(${fmt.primary},${fmt.secondary}),RLSE),
<#if fmt.blksize??>
//           DCB=(DSORG=PS,RECFM=${fmt.recfm},LRECL=${fmt.lrecl},BLKSIZE=${fmt.blksize})
<#else>
//           DCB=(DSORG=PS,RECFM=${fmt.recfm},LRECL=${fmt.lrecl})
</#if>
</#list>
</#list>
//*
//* That's All Folks
<#else>
//***********************************************************
//* No Format Files Defined... this is just a generation dag
</#if>