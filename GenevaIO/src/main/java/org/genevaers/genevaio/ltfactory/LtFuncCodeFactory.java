package org.genevaers.genevaio.ltfactory;

import java.text.DateFormat;

/*
 * Copyright Contributors to the GenevaERS Project. SPDX-License-Identifier: Apache-2.0 (c) Copyright IBM Corporation 2008
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


import java.util.HashMap;
import java.util.Map;

import org.genevaers.genevaio.ltfile.ArgHelper;
import org.genevaers.genevaio.ltfile.Cookie;
import org.genevaers.genevaio.ltfile.LTFileObject;
import org.genevaers.genevaio.ltfile.LogicTableArg;
import org.genevaers.genevaio.ltfile.LogicTableCC;
import org.genevaers.genevaio.ltfile.LogicTableF0;
import org.genevaers.genevaio.ltfile.LogicTableF1;
import org.genevaers.genevaio.ltfile.LogicTableF2;
import org.genevaers.genevaio.ltfile.LogicTableName;
import org.genevaers.genevaio.ltfile.LogicTableNameF1;
import org.genevaers.genevaio.ltfile.LogicTableNameF2;
import org.genevaers.genevaio.ltfile.LogicTableNameValue;
import org.genevaers.genevaio.ltfile.LogicTableRE;
import org.genevaers.repository.Repository;
import org.genevaers.repository.components.LRField;
import org.genevaers.repository.components.LookupPathKey;
import org.genevaers.repository.components.ViewColumn;
import org.genevaers.repository.components.ViewSortKey;
import org.genevaers.repository.components.ViewSource;
import org.genevaers.repository.components.enums.DataType;
import org.genevaers.repository.components.enums.DateCode;
import org.genevaers.repository.components.enums.ExtractArea;
import org.genevaers.repository.components.enums.JustifyId;
import org.genevaers.repository.components.enums.LtCompareType;
import org.genevaers.repository.components.enums.LtRecordType;

import com.google.common.flogger.FluentLogger;

public class LtFuncCodeFactory implements LtFunctionCodeFactory{
    private static final FluentLogger logger = FluentLogger.forEnclosingClass();

    private static Map<String, Integer> accumMap = new HashMap<>();

    private String accumName;
    private int logFileId;

    private int accumNumber;
    
    public enum AcummType {
        GENEVA_AC,
        DATE_AC,
        STRING_AC,
        BIN4
    }

    public enum GenerationWarning {
        NONE,
        COLUMN_SHOULD_BE_SIGNED
    } 

    GenerationWarning warning;
    
    //force implementing classes to generate an accumulator name
    public String generateAccumulatorName(ViewSource viewSource, int vc) {
        //Accumulator numbers increment per column
        String accumBase = String.format("g_%d_%d_%d_%d", viewSource.getViewId(), viewSource.getSourceLFID(), viewSource.getSourceLRID(), vc);
        Integer accNum = accumMap.get(accumBase);
        if(accNum == null) {
            accNum = 0;
        } else {
            accNum = accNum + 1;
        }
        accumMap.put(accumBase, accNum);
        accumName = String.format("%s_%d", accumBase, accNum);
        return accumName;
    }

    public void clearAccumulatorMap() {
        accumMap.clear();
    }

    public void setAccumName(String accumName) {
        this.accumName = accumName;
    }

    @Override
    public LTFileObject getADDA(String accum, String rhsAccum) {
        LogicTableNameValue adda = makeNameValueFromAccum(rhsAccum, "ADDA");
        adda.setTableName(accum);
        return adda; 
    }

    @Override
    public LTFileObject getADDC(String accum, String rhsAccum) {
        LogicTableNameValue arithfn = makeNameValueFromAccum(rhsAccum, "ADDC");
        arithfn.setTableName(accum);
        return arithfn;
    }

    @Override
    public LTFileObject getADDE(String accum, LRField f) {
        LogicTableNameF1 adde = makeNameF1FromAccumAndField(accumName, f);
        adde.setFunctionCode("ADDE");
        adde.setCompareType(LtCompareType.EQ);
        return adde;
    }

    @Override
    public LTFileObject getADDL(String accum, LRField f) {
        LogicTableNameF1 addl = makeNameF1FromAccumAndField(accumName, f);
        addl.setFunctionCode("ADDL");
        addl.setCompareType(LtCompareType.EQ);
        return addl;
    }

    @Override
    public LTFileObject getADDP(String accum, LRField f) {
        LogicTableNameF1 adde = makeNameF1FromAccumAndField(accumName, f);
        adde.setFunctionCode("ADDP");
        adde.setCompareType(LtCompareType.EQ);
        return adde;
    }

    @Override
    public LTFileObject getADDX(String accum, ViewColumn vc) {
        LogicTableNameF1 addx = getNameF1FromAccumAndColumn(accumName, vc);
        addx.setFunctionCode("ADDX");
        addx.setAccumulatorName(accumName);
        return addx;
    }

    @Override
    public LTFileObject getCFAA(String accum, String rhsAccum, String op) {
        LogicTableNameValue cfac = makeNameValueFromAccum(accum, "CFAA");
        cfac.setValue(new Cookie(rhsAccum));
        cfac.setTableName(accum);
        cfac.setCompareType(getCompareType(op));
        return cfac; 
    }

    @Override
    public LTFileObject getCFAC(String accum, String rhs, String op) {
        LogicTableNameValue cfac = makeNameValueFromAccum(accum, "CFAC");
        cfac.setValue(new Cookie(rhs));
        cfac.setTableName(accum);
        cfac.setCompareType(getCompareType(op));
        return cfac; 
    }

    @Override
    public LTFileObject getCFAE(String accum, LRField f, String op) {
        LogicTableNameF1 cfae = makeNameF1FromAccumAndField(accum, f);
        cfae.setFunctionCode("CFAE");
        cfae.setCompareType(getCompareType(op));
        return cfae;
    }

    @Override
    public LTFileObject getCFAL(String accum, LRField f, String op) {
        LogicTableNameF1 cfal = makeNameF1FromAccumAndField(accum, f);
        cfal.setFunctionCode("CFAL");
        cfal.setCompareType(getCompareType(op));
        return cfal;
    }

    @Override
    public LTFileObject getCFAP(String accum, LRField f, String op) {
        LogicTableNameF1 cfap = makeNameF1FromAccumAndField(accum, f);
        cfap.setFunctionCode("CFAP");
        cfap.setCompareType(getCompareType(op));
        return cfap;
    }

    @Override
    public LTFileObject getCFAX(String accum, ViewColumn vc, String op) {
        LogicTableNameF1 cfax = getNameF1FromAccumAndColumn(accum, vc);
        cfax.setFunctionCode("CFAX");
        cfax.setCompareType(getCompareType(op));
        return cfax;
    }

    @Override
    public LTFileObject getCFCA(String accum, String val, String op) {
        LogicTableNameValue cfca = makeNameValueFromAccum(accum, "CFCA");
        cfca.setValue(new Cookie(val));
        cfca.setTableName(accum);
        cfca.setCompareType(getCompareType(op));
        return cfca; 
    }

    @Override
    public LTFileObject getCFCC(String c1, String c2, String op) {
        LogicTableCC cfcc = new LogicTableCC();
        cfcc.setRecordType(LtRecordType.CC);
        cfcc.setFunctionCode("CFCC");
        cfcc.setValue1(new Cookie(c1));
        cfcc.setValue2(new Cookie(c2));
        cfcc.setCompareType(getCompareType(op));
        cfcc.setFieldContentCode(DateCode.NONE);
        return cfcc;
    }

    @Override
    public LTFileObject getCFCE(String v, LRField f, String op) {
        return makeF1FromValueAndField(v, f, op, "CFCE");
    }

    @Override
    public LTFileObject getCFCL(String v, LRField f, String op) {
        return makeF1FromValueAndField(v, f, op, "CFCL");
    }

    @Override
    public LTFileObject getCFCP(String v, LRField f, String op) {
        return makeF1FromValueAndField(v, f, op, "CFCP");
    }

    @Override
    public LTFileObject getCFCX(String v, ViewColumn vc, String op) {
        LogicTableF1 cfcx = new LogicTableF1();
        cfcx.setRecordType(LtRecordType.F1);
        cfcx.setArg(getColumnArg(vc));
        cfcx.setFunctionCode("CFCX");
        cfcx.getArg().setValue(new Cookie(v));
        cfcx.setCompareType(getCompareType(op));
        return cfcx;
    }

    @Override
    public LTFileObject getCFEA(String accum, LRField f, String op) {
        LogicTableNameF1 cfea = makeNameF1FromAccumAndField(accum, f);
        cfea.setFunctionCode("CFEA");
        cfea.setCompareType(getCompareType(op));
        return cfea;
    }

    @Override
    public LTFileObject getCFEC(LRField f, String v, String op) {
        return makeF1FromValueAndField(v, f, op, "CFEC");
    }

    @Override
    public LTFileObject getCFEE(LRField f1, LRField f2, String op) {
        return makeF2FromFieldAndField(f1, f2, op, "CFEE");
    }

    @Override
    public LTFileObject getCFEL(LRField f1, LRField f2, String op) {
        return  makeF2FromFieldAndField(f1, f2, op, "CFEL");
    }

    @Override
    public LTFileObject getCFEP(LRField f1, LRField f2, String op) {
        return makeF2FromFieldAndField(f1, f2, op, "CFEP");
    }

    @Override
    public LTFileObject getCFEX(LRField f, ViewColumn c2, String op) {
        LogicTableF2 cfex = makeF2FromFieldAndColumn(f, c2);
        cfex.setFunctionCode("CFEX");
        cfex.setCompareType(getCompareType(op));
        return cfex;
    }

    @Override
    public LTFileObject getCFLA(String accum, LRField f, String op) {
        LogicTableNameF1 cfal = makeNameF1FromAccumAndField(accum, f);
        cfal.setFunctionCode("CFLA");
        cfal.setCompareType(getCompareType(op));
        return cfal;
    }

    @Override
    public LTFileObject getCFLC(LRField f, String v, String op) {
        LogicTableF1 cflc = new LogicTableF1();
        cflc.setRecordType(LtRecordType.F1);
        cflc.setFunctionCode("CFLC");
        LogicTableArg arg = getArgFromField(f);
        arg.setValue(new Cookie(v));
        cflc.setArg(arg);
        cflc.setCompareType(getCompareType(op));
        return cflc;
    }

    @Override
    public LTFileObject getCFLE(LRField f1, LRField f2, String op) {
        return makeF2FromFieldAndField(f1, f2, op, "CFLE");
    }

    @Override
    public LTFileObject getCFLL(LRField f1, LRField f2, String op) {
        return makeF2FromFieldAndField(f1, f2, op, "CFLL");
    }

    @Override
    public LTFileObject getCFLP(LRField f1, LRField f2, String op) {
        return makeF2FromFieldAndField(f1, f2, op, "CFLP");
    }

    @Override
    public LTFileObject getCFLX(LRField f, ViewColumn c2, String op) {
       LogicTableF2 cflx = makeF2FromFieldAndColumn(f, c2);
        cflx.setFunctionCode("CFLX");
        cflx.setCompareType(getCompareType(op));
        return cflx;
    }

    @Override
    public LTFileObject getCFPA(String accum, LRField f, String op) {
        LogicTableNameF1 cfpa = makeNameF1FromAccumAndField(accum, f);
        cfpa.setFunctionCode("CFPA");
        cfpa.setCompareType(getCompareType(op));
        return cfpa;
    }

    @Override
    public LTFileObject getCFPC(LRField f, String v2, String op) {
        LogicTableF1 cfec = new LogicTableF1();
        cfec.setRecordType(LtRecordType.F1);
        cfec.setFunctionCode("CFPC");
        LogicTableArg arg = getArgFromField(f);
        cfec.setArg(arg);
        arg.setValue(new Cookie(v2));
        cfec.setCompareType(getCompareType(op));
        return cfec;
    }

    @Override
    public LTFileObject getCFPE(LRField f1, LRField f2, String op) {
        LogicTableF2 cfpe = makeF2FromFieldAndField(f1, f2, op, "CFPE");
        cfpe.setFunctionCode("CFPE");
        cfpe.setCompareType(getCompareType(op));
        return cfpe;
    }

    @Override
    public LTFileObject getCFPL(LRField f1, LRField f2, String op) {
        return makeF2FromFieldAndField(f1, f2, op, "CFPL");
    }

    @Override
    public LTFileObject getCFPP(LRField f1, LRField f2, String op) {
        return makeF2FromFieldAndField(f1, f2, op, "CFPP");
    }

    @Override
    public LTFileObject getCFPX(LRField f, ViewColumn c2, String op) {
        LogicTableF2 cfpx = makeF2FromFieldAndColumn(f, c2);
        cfpx.setFunctionCode("CFPX");
        cfpx.setCompareType(getCompareType(op));
        return cfpx;
    }

    @Override
    public LTFileObject getCFXA(String accum, ViewColumn vc, String op) {
        LogicTableNameF1 cfxa = getNameF1FromAccumAndColumn(accum, vc);
        cfxa.setFunctionCode("CFXA");
        cfxa.setCompareType(getCompareType(op));
        return cfxa;
    }

    @Override
    public LTFileObject getCFXC(ViewColumn c, String v, String op) {
        LogicTableF1 cfxc = new LogicTableF1();
        cfxc.setRecordType(LtRecordType.F1);
        cfxc.setArg(getColumnArg(c));
        cfxc.setFunctionCode("CFXC");
        cfxc.getArg().setValue(new Cookie(v));
        cfxc.setCompareType(getCompareType(op));
        return cfxc;
    }

    @Override
    public LTFileObject getCFXE(ViewColumn c, LRField f, String op) {
        LogicTableF2 cfxe = makeF2FromColumnAndField(c, f);
        cfxe.setFunctionCode("CFXE");
        cfxe.setCompareType(getCompareType(op));
        return cfxe;
    }

    @Override
    public LTFileObject getCFXL(ViewColumn c, LRField f, String op) {
       LogicTableF2 cfxl = makeF2FromColumnAndField(c, f);
        cfxl.setFunctionCode("CFXL");
        cfxl.setCompareType(getCompareType(op));
        return cfxl;
    }

    @Override
    public LTFileObject getCFXP(ViewColumn c, LRField f, String op) {
        LogicTableF2 cfxp = makeF2FromColumnAndField(c, f);
        cfxp.setFunctionCode("CFXP");
        cfxp.setCompareType(getCompareType(op));
        return cfxp;
    }

    @Override
    public LTFileObject getCFXX(ViewColumn c, ViewColumn c2, String op) {
        LogicTableF2 cfxx = new LogicTableF2();
        cfxx.setArg1(getColumnArg(c));
        cfxx.setArg2(getColumnArg(c2));
        cfxx.setFunctionCode("CFXX");
        cfxx.setRecordType(LtRecordType.F2);
        cfxx.setCompareType(getCompareType(op));
        return cfxx;    
    }

    @Override
    public LTFileObject getCNE(LRField f) {
        LogicTableF1 cne = getF1FromField(f);
        cne.setFunctionCode("CNE");
        cne.setCompareType(LtCompareType.EQ);
        return cne;
    }

    @Override
    public LTFileObject getCNL(LRField f) {
        LogicTableF1 cne = getF1FromField(f);
        cne.setFunctionCode("CNL");
        cne.setCompareType(LtCompareType.EQ);
        return cne;
    }

    @Override
    public LTFileObject getCNP(LRField f) {
        LogicTableF1 cnp = getF1FromField(f);
        cnp.setFunctionCode("CNP");
        cnp.setCompareType(LtCompareType.EQ);
        return cnp;
    }

    @Override
    public LTFileObject getCNX(ViewColumn c) {
        LogicTableF1 cnx = getF1FromFColumn(c);
        cnx.setFunctionCode("CNX");
        cnx.setCompareType(LtCompareType.EQ);
        return cnx;
    }

    @Override
    public LTFileObject getCSE(LRField f) {
        LogicTableF1 cse = getF1FromField(f);
        cse.setFunctionCode("CSE");
        cse.setCompareType(LtCompareType.EQ);
        return cse;
    }

    @Override
    public LTFileObject getCSL(LRField f) {
        LogicTableF1 csl = getF1FromField(f);
        csl.setFunctionCode("CSL");
        csl.setCompareType(LtCompareType.EQ);
        return csl;
    }

    @Override
    public LTFileObject getCSP(LRField f) {
        LogicTableF1 csp = getF1FromField(f);
        csp.setFunctionCode("CSP");
        csp.setCompareType(LtCompareType.EQ);
        return csp;
    }

    @Override
    public LTFileObject getCSX(ViewColumn c) {
        LogicTableF1 csx = getF1FromFColumn(c);
        csx.setFunctionCode("CSX");
        csx.setCompareType(LtCompareType.EQ);
        return csx;
    }

    @Override
    public LTFileObject getCTA(String accum, ViewColumn vc) {
        LogicTableNameF1 dta = getNameF1FromAccumAndColumn(accum, vc);
        dta.setFunctionCode("CTA");
        return dta;
    }

    @Override
    public LTFileObject getCTC(String v, ViewColumn vc) {
        LogicTableF1 ctc = new LogicTableF1();
        ctc.setFunctionCode("CTC");
        LogicTableArg arg = getArgFromValueAndColumn(v, vc);
        arg.setFieldLength((short)12);
        ctc.setRecordType(LtRecordType.F1);
        //TODO this can rise as a common function
        //These may be at adder level
        //Prefix is not a separate object so each record type needs the set
        ctc.setViewId(vc.getViewId());
        ctc.setColumnId(vc.getComponentId());
        ctc.setSuffixSeqNbr((short)vc.getColumnNumber());
        ctc.setArg(arg);
        ctc.setCompareType(LtCompareType.EQ);
        return ctc;
    }

    @Override
    public LTFileObject getCTE(LRField f, ViewColumn v) {
        LogicTableF1 cte = new LogicTableF1();
        cte.setRecordType(LtRecordType.F1);
        LogicTableArg arg = getArgFromField(f);
        arg.setFieldLength(f.getLength());
        cte.setArg(arg);
        cte.setFunctionCode("CTE");
        cte.setColumnId(v.getComponentId());
        cte.setSuffixSeqNbr((short)v.getColumnNumber());
        cte.setCompareType(LtCompareType.EQ);
        return cte;
    }

    @Override
    public LTFileObject getCTL(LRField f, ViewColumn vc) {
        LogicTableF1 ctl = new LogicTableF1();
        ctl.setFunctionCode("CTL");
        ctl.setRecordType(LtRecordType.F1);
        ctl.setCompareType(LtCompareType.EQ);
        return ctl;
    }

    @Override
    public LTFileObject getCTP(LRField f, ViewColumn v) {
        LogicTableF1 ctp = new LogicTableF1();
        ctp.setRecordType(LtRecordType.F1);
        LogicTableArg arg = getArgFromField(f);
        arg.setFieldLength((short)12);
        ctp.setArg(arg);
        ctp.setFunctionCode("CTP");
        ctp.setColumnId(v.getComponentId());
        ctp.setSuffixSeqNbr((short)v.getColumnNumber());
        ctp.setCompareType(LtCompareType.EQ);
        return ctp;
    }

    @Override
    public LTFileObject getCTX(ViewColumn c, ViewColumn v) {
        LogicTableF2 ctx = new LogicTableF2();
        ctx.setArg1(getColumnRefArg(c));
        ctx.setArg2(getColumnArg(v));
        ctx.setFunctionCode("CTX");
        ctx.setRecordType(LtRecordType.F2);
        ctx.setCompareType(LtCompareType.EQ);
        return ctx;    
    }

    @Override
    public LTFileObject getCXE(LRField f) {
        LogicTableF1 cxe = getF1FromField(f);
        cxe.setFunctionCode("CXE");
        cxe.setCompareType(LtCompareType.EQ);
        return cxe;
    }

    @Override
    public LTFileObject getCXL(LRField f) {
        LogicTableF1 cxl = getF1FromField(f);
        cxl.setFunctionCode("CXL");
        cxl.setCompareType(LtCompareType.EQ);
        return cxl;
    }

    @Override
    public LTFileObject getCXP(LRField f) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public LTFileObject getCXX(ViewColumn c) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public LTFileObject getDIM1() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public LTFileObject getDIM2() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public LTFileObject getDIM4() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public LTFileObject getDIM8() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public LTFileObject getDIMD() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public LTFileObject getDIMN() {
        LogicTableName ltn = new LogicTableName();
        ltn.setAccumulatorName(accumName);
        ltn.setRecordType(LtRecordType.NAME);
        ltn.setFunctionCode("DIMN");
        return ltn;
    }

    @Override
    public LTFileObject getDIMS() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public LTFileObject getDIVA(String accum, String rhsAccum) {
        LogicTableNameValue diva = makeNameValueFromAccum(rhsAccum, "DIVA");
        diva.setTableName(accum);
        return diva; 
    }

    @Override
    public LTFileObject getDIVC(String accum, String rhsAccum) {
        LogicTableNameValue arithfn = makeNameValueFromAccum(rhsAccum, "DIVC");
        return arithfn;
    }

    @Override
    public LTFileObject getDIVE(String accum, LRField f) {
        LogicTableNameF1 dive = makeNameF1FromAccumAndField(accumName, f);
        dive.setFunctionCode("DIVE");
        dive.setCompareType(LtCompareType.EQ);
        return dive;
    }

    @Override
    public LTFileObject getDIVL(String accum, LRField f) {
        LogicTableNameF1 divl = makeNameF1FromAccumAndField(accumName, f);
        divl.setFunctionCode("DIVL");
        divl.setCompareType(LtCompareType.EQ);
        return divl;
    }

    @Override
    public LTFileObject getDIVP(String accum, LRField f) {
        LogicTableNameF1 divp = makeNameF1FromAccumAndField(accumName, f);
        divp.setFunctionCode("DIVP");
        divp.setCompareType(LtCompareType.EQ);
        return divp;
    }

    @Override
    public LTFileObject getDIVX(String accum, ViewColumn vc) {
        LogicTableNameF1 divx = getNameF1FromAccumAndColumn(accumName, vc);
        divx.setFunctionCode("DIVX");
        divx.setAccumulatorName(accumName);
        return divx;
    }

    @Override
    public LTFileObject getDTA(String accum, ViewColumn vc) {
        LogicTableNameF1 dta = getNameF1FromAccumAndColumn(accum, vc);
        dta.setFunctionCode("DTA");
        return dta;
    }

    @Override
    public LTFileObject getDTC(String v, ViewColumn vc) {
        LogicTableF1 dtc = new LogicTableF1();
        dtc.setFunctionCode("DTC");
        LogicTableArg arg = getArgFromValueAndColumn(v, vc);
        dtc.setRecordType(LtRecordType.F1);
        dtc.setViewId(vc.getViewId());
        dtc.setColumnId(vc.getComponentId());
        dtc.setSuffixSeqNbr((short)vc.getColumnNumber());
        dtc.setArg(arg);
        dtc.setCompareType(LtCompareType.EQ);
        return dtc;
    }

    @Override
    public LTFileObject getDTE(LRField f, ViewColumn vc) {
        LogicTableF2 dte = makeF2FromFieldAndColumn(f, vc);
        dte.setFunctionCode("DTE");
        return dte;
    }


    @Override
    public LTFileObject getDTL(LRField f, ViewColumn vc) {
        LogicTableF2 dtl = makeF2FromFieldAndColumn(f, vc);
        dtl.setFunctionCode("DTL");
        return dtl;
    }

    @Override
    public LTFileObject getDTP(LRField f, ViewColumn vc) {
        LogicTableF2 dtp = makeF2FromFieldAndColumn(f, vc);
        dtp.setFunctionCode("DTP");
        return dtp;
    }

    @Override
    public LTFileObject getDTX(ViewColumn c, ViewColumn v) {
        LogicTableF2 dtx = getF2ForColumnRef(c, v);
        return dtx;    
    }

    @Override
    public LTFileObject getEN() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public LTFileObject getES() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public LTFileObject getET() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public LTFileObject getFNCC(String accum, LogicTableArg arg1, LogicTableArg arg2) {
        LogicTableNameF2 fncc = new LogicTableNameF2();
        fncc.setRecordType(LtRecordType.NAMEF2);
        fncc.setFunctionCode("FNCC");
        fncc.setCompareType(LtCompareType.DAYSBETWEEN);
        fncc.setArg1(arg1);
        fncc.setArg2(arg2);
        fncc.setAccumulatorName(accum);
        return fncc;
    }

    @Override
    public LTFileObject getFNCE(String accum, LogicTableArg arg1, LogicTableArg arg2) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public LTFileObject getFNCL(String accum, LogicTableArg arg1, LogicTableArg arg2) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public LTFileObject getFNCP(String accum, LogicTableArg arg1, LogicTableArg arg2) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public LTFileObject getFNCX(String accum, LogicTableArg arg1, LogicTableArg arg2) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public LTFileObject getFNEC(String accum, LogicTableArg arg1, LogicTableArg arg2) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public LTFileObject getFNEE(String accum, LogicTableArg arg1, LogicTableArg arg2) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public LTFileObject getFNEL(String accum, LogicTableArg arg1, LogicTableArg arg2) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public LTFileObject getFNEP(String accum, LogicTableArg arg1, LogicTableArg arg2) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public LTFileObject getFNEX(String accum, LogicTableArg arg1, LogicTableArg arg2) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public LTFileObject getFNLC(String accum, LogicTableArg arg1, LogicTableArg arg2) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public LTFileObject getFNLE(String accum, LogicTableArg arg1, LogicTableArg arg2) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public LTFileObject getFNLL(String accum, LogicTableArg arg1, LogicTableArg arg2) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public LTFileObject getFNLP(String accum, LogicTableArg arg1, LogicTableArg arg2) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public LTFileObject getFNLX(String accum, LogicTableArg arg1, LogicTableArg arg2) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public LTFileObject getFNPC(String accum, LogicTableArg arg1, LogicTableArg arg2) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public LTFileObject getFNPE(String accum, LogicTableArg arg1, LogicTableArg arg2) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public LTFileObject getFNPL(String accum, LogicTableArg arg1, LogicTableArg arg2) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public LTFileObject getFNPP(String accum, LogicTableArg arg1, LogicTableArg arg2) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public LTFileObject getFNPX(String accum, LogicTableArg arg1, LogicTableArg arg2) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public LTFileObject getFNXC(String accum, LogicTableArg arg1, LogicTableArg arg2) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public LTFileObject getFNXE(String accum, LogicTableArg arg1, LogicTableArg arg2) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public LTFileObject getFNXL(String accum, LogicTableArg arg1, LogicTableArg arg2) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public LTFileObject getFNXP(String accum, LogicTableArg arg1, LogicTableArg arg2) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public LTFileObject getFNXX(String accum, LogicTableArg arg1, LogicTableArg arg2) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public LTFileObject getFNE(String accum, LRField f) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public LTFileObject getFNL(String accum, LRField f) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public LTFileObject getFNP(String accum, LRField f) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public LTFileObject getGEN() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public LTFileObject getGOTO() {
        LogicTableF0 f0 = new LogicTableF0();
        f0.setRecordType(LtRecordType.F0);
        f0.setFunctionCode("GOTO");
        f0.setGotoRow2(0);
        return f0;
    }

    @Override
    public LTFileObject getHD() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public LTFileObject getJOIN(String val) {
        LogicTableF1 join = new LogicTableF1();
        join.setRecordType(LtRecordType.F1);
        join.setFunctionCode("JOIN");
        join.setFileId(0);
        LogicTableArg arg = new LogicTableArg();
        arg.setValue(new Cookie(val));
        join.setArg(arg);
        join.setCompareType(LtCompareType.EQ);
        return join;
    }

    @Override
    public LTFileObject getKSLK(LRField f) {
        LogicTableF1 kslk = new LogicTableF1();
        kslk.setRecordType(LtRecordType.F1);
        kslk.setFunctionCode("KSLK");

        LogicTableArg arg1 = getArgFromField(f);
        kslk.setArg(arg1);
        kslk.setCompareType(LtCompareType.EQ);

        return kslk;
    }

    @Override
    public LTFileObject getLKC(String v) {
        LogicTableF1 lkc = new LogicTableF1();
        lkc.setRecordType(LtRecordType.F1);
        lkc.setFunctionCode("LKC");
        LogicTableArg arg = new LogicTableArg();
        arg.setFieldContentId(DateCode.NONE);
        lkc.setArg(arg);
        return lkc;
    }

    @Override
    public LTFileObject getLKDC(String val) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public LTFileObject getLKDE(String val) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public LTFileObject getLKDL(String val) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public LTFileObject getLKDX(String val) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public LTFileObject getLKE(LRField f, LookupPathKey key) {
        LogicTableF2 lke = new LogicTableF2();
        lke.setRecordType(LtRecordType.F2);
        lke.setFunctionCode("LKE");

        LogicTableArg arg1 = getArgFromField(f);
        lke.setArg1(arg1);

        LogicTableArg arg2 = new LogicTableArg();
        populateArgFromKeyTarget(arg2, key);
        lke.setArg2(arg2);
        lke.setCompareType(LtCompareType.EQ);
        return lke;
    }

    @Override
    public LTFileObject getLKL(LRField f, LookupPathKey key) {
        LogicTableF2 lke = new LogicTableF2();
        lke.setRecordType(LtRecordType.F2);
        lke.setFunctionCode("LKL");

        LogicTableArg arg1 = getArgFromField(f);
        lke.setArg1(arg1);

        LogicTableArg arg2 = new LogicTableArg();
        populateArgFromKeyTarget(arg2, key);
        lke.setArg2(arg2);
        lke.setCompareType(LtCompareType.EQ);
        return lke;
    }

    @Override
    public LTFileObject getLKLR(String val) {
        LogicTableF1 join = new LogicTableF1();
        join.setRecordType(LtRecordType.F1);
        join.setFunctionCode("LKLR");
        join.setFileId(0);
        LogicTableArg arg = new LogicTableArg();
        arg.setValue(new Cookie(val));
        join.setArg(arg);
        join.setCompareType(LtCompareType.EQ);
        return join;
    }

    @Override
    public LTFileObject getLKS() {
        LogicTableF1 lks = new LogicTableF1();
        lks.setRecordType(LtRecordType.F1);
        lks.setFunctionCode("LKS");
        return lks;
    }

    @Override
    public LTFileObject getLKX(ViewColumn c, LookupPathKey key) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public LTFileObject getLUEX() {
        LogicTableRE luex = new LogicTableRE();
        luex.setRecordType(LtRecordType.RE);
        luex.setFunctionCode("LUEX");
        luex.setFileId(0);
        return luex;
    }

    @Override
    public LTFileObject getLUSM() {
        LogicTableRE lusm = new LogicTableRE();
        lusm.setRecordType(LtRecordType.RE);
        lusm.setFunctionCode("LUSM");
        lusm.setFileId(0);
        return lusm;
    }

    @Override
    public LTFileObject getMULC(String accum, String rhsAccum) {
        LogicTableNameValue arithfn = makeNameValueFromAccum(rhsAccum, "MULC");
        return arithfn;
    }

    @Override
    public LTFileObject getMULE(String accum, LRField f) {
        LogicTableNameF1 mule = makeNameF1FromAccumAndField(accumName, f);
        mule.setFunctionCode("MULE");
        mule.setCompareType(LtCompareType.EQ);
        return mule;
    }

    @Override
    public LTFileObject getMULL(String accum, LRField f) {
        LogicTableNameF1 mull = makeNameF1FromAccumAndField(accumName, f);
        mull.setFunctionCode("MULL");
        mull.setCompareType(LtCompareType.EQ);
        return mull;
    }

    @Override
    public LTFileObject getMULP(String accum, LRField f) {
        LogicTableNameF1 mulp = makeNameF1FromAccumAndField(accumName, f);
        mulp.setFunctionCode("MULP");
        mulp.setCompareType(LtCompareType.EQ);
        return mulp;
    }

    @Override
    public LTFileObject getMULA(String accum, String rhsAccum) {
        LogicTableNameValue mula = makeNameValueFromAccum(rhsAccum, "MULA");
        mula.setTableName(accum);
        return mula; 
    }

    @Override
    public LTFileObject getMULX(String accum, ViewColumn vc) {
        LogicTableNameF1 mulx = getNameF1FromAccumAndColumn(accumName, vc);
        mulx.setFunctionCode("MULX");
        mulx.setAccumulatorName(accumName);
        return mulx;
    }

    @Override
    public LTFileObject getNV() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public LTFileObject getREEX() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public LTFileObject getRENX() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public LTFileObject getRETK() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public LTFileObject getSETA(String accum, String rhsAccum) {
        LogicTableNameValue seta = makeNameValueFromAccum(rhsAccum, "SETA");
        seta.setTableName(accum);
        return seta;
    }

    @Override
    public LTFileObject getSETC(String accum, String rhsAccum) {
        LogicTableNameValue arithfn = makeNameValueFromAccum(rhsAccum, "SETC");
        return arithfn;
    }

    @Override
    public LTFileObject getSETE(String accum, LRField f) {
        LogicTableNameF1 sete = makeNameF1FromAccumAndField(accumName, f);
        sete.setFunctionCode("SETE");
        sete.setAccumulatorName(accumName);
        sete.setCompareType(LtCompareType.EQ);
        return sete;
    }

    @Override
    public LTFileObject getSETL(String accum, LRField f) {
        LogicTableNameF1 setl = makeNameF1FromAccumAndField(accumName, f);
        setl.setFunctionCode("SETL");
        setl.setAccumulatorName(accumName);
        setl.setCompareType(LtCompareType.EQ);
        return setl;
    }

    @Override
    public LTFileObject getSETP(String accum, LRField f) {
        LogicTableNameF1 setp = makeNameF1FromAccumAndField(accumName, f);
        setp.setFunctionCode("SETP");
        setp.setAccumulatorName(accumName);
        setp.setCompareType(LtCompareType.EQ);
        return setp;
    }

    @Override
    public LTFileObject getSETX(String accum, ViewColumn vc) {
        LogicTableNameF1 setx = getNameF1FromAccumAndColumn(accum, vc);
        setx.setFunctionCode("SETX");
        setx.setAccumulatorName(accumName);
        return setx;
    }

    @Override
    public LTFileObject getSFCE(String v, LRField f) {
        LogicTableF1 sfce = new LogicTableF1();
        sfce.setRecordType(LtRecordType.F1);
        sfce.setFunctionCode("SFCE");
        LogicTableArg arg = getArgFromField(f);
        sfce.setArg(arg);
        arg.setValue(new Cookie(v));
        sfce.setCompareType(LtCompareType.CONTAINS);
        return sfce;
    }

    @Override
    public LTFileObject getSFCL(String v, LRField f) {
        LogicTableF1 sfcl = new LogicTableF1();
        sfcl.setRecordType(LtRecordType.F1);
        sfcl.setFunctionCode("SFCL");
        LogicTableArg arg = getArgFromField(f);
        sfcl.setArg(arg);
        arg.setValue(new Cookie(v));
        sfcl.setCompareType(LtCompareType.CONTAINS);
        return sfcl;
    }

    @Override
    public LTFileObject getSFCP(String v, LRField f) {
        LogicTableF1 sfcp = new LogicTableF1();
        sfcp.setRecordType(LtRecordType.F1);
        sfcp.setFunctionCode("SFCP");
        LogicTableArg arg = getArgFromField(f);
        sfcp.setArg(arg);
        arg.setValue(new Cookie(v));
        sfcp.setCompareType(LtCompareType.CONTAINS);
        return sfcp;
    }

    @Override
    public LTFileObject getSFCX(String v, ViewColumn c2) {
        LogicTableF1 sfcx = new LogicTableF1();
        sfcx.setRecordType(LtRecordType.F1);
        sfcx.setArg(getColumnArg(c2));
        sfcx.setFunctionCode("SFCX");
        sfcx.getArg().setValue(new Cookie(v));
        sfcx.setCompareType(LtCompareType.CONTAINS);
        return sfcx;
    }

    @Override
    public LTFileObject getSFEC(LRField f, String v2) {
        LogicTableF1 sfec = new LogicTableF1();
        sfec.setRecordType(LtRecordType.F1);
        sfec.setFunctionCode("SFEC");
        LogicTableArg arg = getArgFromField(f);
        sfec.setArg(arg);
        arg.setValue(new Cookie(v2));
        sfec.setCompareType(LtCompareType.CONTAINS);
        return sfec;
    }

    @Override
    public LTFileObject getSFEE(LRField f1, LRField f2) {
        return makeF2FromFieldAndField(f1, f2, "CONTAINS", "SFEE");
    }

    @Override
    public LTFileObject getSFEL(LRField f, LRField f2) {
        return  makeF2FromFieldAndField(f, f2, "CONTAINS", "SFEL");
    }

    @Override
    public LTFileObject getSFEP(LRField f1, LRField f2) {
        return makeF2FromFieldAndField(f1, f2, "CONTAINS", "SFEP");
    }

    @Override
    public LTFileObject getSFEX(LRField f, ViewColumn c2) {
        LogicTableF2 sfex = makeF2FromFieldAndColumnRef(f, c2);
        sfex.setFunctionCode("SFEX");
        sfex.setCompareType(LtCompareType.CONTAINS);
        return sfex;
    }

    @Override
    public LTFileObject getSFLC(LRField f, String v2) {
        LogicTableF1 sflc = new LogicTableF1();
        sflc.setRecordType(LtRecordType.F1);
        sflc.setFunctionCode("SFLC");
        LogicTableArg arg = getArgFromField(f);
        arg.setValue(new Cookie(v2));
        sflc.setArg(arg);
        sflc.setCompareType(LtCompareType.CONTAINS);
        return sflc;
    }

    @Override
    public LTFileObject getSFLE(LRField f, LRField f2) {
         return makeF2FromFieldAndField(f, f2, "CONTAINS", "SFLE");
    }

    @Override
    public LTFileObject getSFLL(LRField f1, LRField f2) {
        return makeF2FromFieldAndField(f1, f2, "CONTAINS", "SFLL");
    }

    @Override
    public LTFileObject getSFLP(LRField f1, LRField f2) {
        return makeF2FromFieldAndField(f1, f2, "CONTAINS", "SFLP");
    }

    @Override
    public LTFileObject getSFLX(LRField f, ViewColumn c2) {
        LogicTableF2 sflx = makeF2FromFieldAndColumn(f, c2);
        sflx.setFunctionCode("SFLX");
        sflx.setCompareType(LtCompareType.CONTAINS);
        return sflx;
    }

    @Override
    public LTFileObject getSFPC(LRField f, String v2) {
        LogicTableF1 sfpc = new LogicTableF1();
        sfpc.setRecordType(LtRecordType.F1);
        sfpc.setFunctionCode("SFPC");
        LogicTableArg arg = getArgFromField(f);
        sfpc.setArg(arg);
        arg.setValue(new Cookie(v2));
        sfpc.setCompareType(LtCompareType.CONTAINS);
        return sfpc;
    }

    @Override
    public LTFileObject getSFPE(LRField f1, LRField f2) {
        return makeF2FromFieldAndField(f1, f2, "CONTAINS", "SFPE");
    }

    @Override
    public LTFileObject getSFPL(LRField f1, LRField f2) {
        return makeF2FromFieldAndField(f1, f2, "CONTAINS", "SFPL");
    }

    @Override
    public LTFileObject getSFPP(LRField f1, LRField f2) {
        return makeF2FromFieldAndField(f1, f2, "CONTAINS", "SFPP");
    }

    @Override
    public LTFileObject getSFPX(LRField f, ViewColumn c) {
        LogicTableF2 sfpx = makeF2FromFieldAndColumn(f, c);
        sfpx.setFunctionCode("SFPX");
        sfpx.setCompareType(LtCompareType.CONTAINS);
        return sfpx;
    }

    @Override
    public LTFileObject getSFXC(ViewColumn c, String v) {
        LogicTableF1 sfxc = new LogicTableF1();
        sfxc.setRecordType(LtRecordType.F1);
        sfxc.setArg(getColumnArg(c));
        sfxc.setFunctionCode("SFXC");
        sfxc.getArg().setValue(new Cookie(v));
        sfxc.setCompareType(LtCompareType.CONTAINS);
        return sfxc;
    }

    @Override
    public LTFileObject getSFXE(ViewColumn c, LRField f) {
        LogicTableF2 sfxe = makeF2FromColumnAndField(c, f);
        sfxe.setFunctionCode("SFXE");
        sfxe.setCompareType(LtCompareType.CONTAINS);
        return sfxe;
    }

    @Override
    public LTFileObject getSFXL(ViewColumn c, LRField f) {
        LogicTableF2 sfxl = makeF2FromColumnAndField(c, f);
        sfxl.setFunctionCode("SFXL");
        sfxl.setCompareType(LtCompareType.CONTAINS);
        return sfxl;
    }

    @Override
    public LTFileObject getSFXP(ViewColumn c, LRField f) {
        LogicTableF2 sfxp = makeF2FromColumnAndField(c, f);
        sfxp.setFunctionCode("SFXP");
        sfxp.setCompareType(LtCompareType.CONTAINS);
        return sfxp;
    }

    @Override
    public LTFileObject getSFXX(ViewColumn c, ViewColumn c2) {
        LogicTableF2 sfxx = new LogicTableF2();
        sfxx.setArg1(getColumnArg(c));
        sfxx.setArg2(getColumnArg(c2));
        sfxx.setFunctionCode("SFXX");
        sfxx.setRecordType(LtRecordType.F2);
        sfxx.setCompareType(LtCompareType.CONTAINS);
        return sfxx;    
    }

    @Override
    public LTFileObject getSKA(String accum, ViewColumn vc) {
        LogicTableNameF1 ska = getNameF1FromAccumAndColumn(accum, vc);
        ska.setFunctionCode("SKA");
        return ska;
    }

    @Override
    public LTFileObject getSKC(String v, ViewColumn vc, ViewSortKey sk) {
        LogicTableF1 skc = new LogicTableF1();
        skc.setFunctionCode("SKC");
        LogicTableArg arg = getArgFromValueAndSortkKey(v, sk);
        skc.setRecordType(LtRecordType.F1);
        skc.setViewId(vc.getViewId());
        skc.setColumnId(vc.getComponentId());
        skc.setSuffixSeqNbr((short)vc.getColumnNumber());
        skc.setArg(arg);
        skc.setCompareType(LtCompareType.EQ);
        arg.setFieldContentId(DateCode.NONE);
        return skc;
    }

    @Override
    public LTFileObject getSKE(LRField f, ViewColumn vc, ViewSortKey sk) {
        LogicTableF2 ske = makeF2FromFieldAndSortkey(f, vc, sk);
        ske.setFunctionCode("SKE");
        return ske;
    }

    @Override
    public LTFileObject getSKL(LRField f, ViewColumn vc, ViewSortKey sk) {
        LogicTableF2 skl = makeF2FromFieldAndSortkey(f, vc, sk);
        skl.setFunctionCode("SKL");
        return skl;
    }

    @Override
    public LTFileObject getSKP(LRField f, ViewColumn vc, ViewSortKey sk) {
        LogicTableF2 skp = makeF2FromFieldAndColumn(f, vc);
        skp.setFunctionCode("SKP");
        return skp;
    }

    @Override
    public LTFileObject getSKX(ViewColumn c, ViewColumn v, ViewSortKey sk) {
        LogicTableF2 skx = new LogicTableF2();
        skx.setArg1(getColumnArg(c));
        skx.setArg2(getColumnArg(v));
        skx.setFunctionCode("SKX");
        skx.setRecordType(LtRecordType.F2);
        skx.setCompareType(LtCompareType.EQ);
        return skx;    
    }

    @Override
    public LTFileObject getSUBA(String accum, String rhsAccum) {
        LogicTableNameValue suba = makeNameValueFromAccum(rhsAccum, "SUBA");
        suba.setTableName(accum);
        return suba; 
    }

    @Override
    public LTFileObject getSUBC(String accum, String rhsAccum) {
        LogicTableNameValue arithfn = makeNameValueFromAccum(rhsAccum, "SUBC");
        return arithfn;
    }

    @Override
    public LTFileObject getSUBE(String accum, LRField f) {
        LogicTableNameF1 sube = makeNameF1FromAccumAndField(accumName, f);
        sube.setFunctionCode("SUBE");
        sube.setCompareType(LtCompareType.EQ);
        return sube;
    }

    @Override
    public LTFileObject getSUBL(String accum, LRField f) {
        LogicTableNameF1 subl = makeNameF1FromAccumAndField(accumName, f);
        subl.setFunctionCode("SUBL");
        subl.setCompareType(LtCompareType.EQ);
        return subl;
    }

    @Override
    public LTFileObject getSUBP(String accum, LRField f) {
        LogicTableNameF1 subp = makeNameF1FromAccumAndField(accumName, f);
        subp.setFunctionCode("SUBP");
        subp.setCompareType(LtCompareType.EQ);
        return subp;
    }

    @Override
    public LTFileObject getSUBX(String accum, ViewColumn vc) {
        LogicTableNameF1 subx = getNameF1FromAccumAndColumn(accumName, vc);
        subx.setFunctionCode("SUBX");
        subx.setAccumulatorName(accumName);
        return subx;
    }

    @Override
    public LTFileObject getWRDT() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public LTFileObject getWRIN() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public LTFileObject getWRSU() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public LTFileObject getWRTK() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public LTFileObject getWRXT() {
        // TODO Auto-generated method stub
        return null;
    }

    private LogicTableF1 makeF1FromValueAndField(String v, LRField f, String op, String fcName) {
        LogicTableF1 cfcp = new LogicTableF1();
        cfcp.setRecordType(LtRecordType.F1);
        cfcp.setFunctionCode(fcName);
        LogicTableArg arg = getArgFromField(f);
        cfcp.setArg(arg);
        arg.setValue(new Cookie(v));
        cfcp.setCompareType(getCompareType(op));
        return cfcp;
    }

    public void setAccumNumber(int accumNumber) {
        this.accumNumber = accumNumber;
    }

    public void clearGenerationWarnings() {
        warning = GenerationWarning.NONE;
    }

    public GenerationWarning getWarning() {
        return warning;
    }
    
    private LogicTableArg getArgFromField(LRField f) {
        LogicTableArg arg = new LogicTableArg();
        arg.setDecimalCount(f.getNumDecimalPlaces());
        arg.setFieldContentId(f.getDateTimeFormat());
        arg.setFieldFormat(f.getDatatype());
        arg.setLrId(f.getLrID());
        arg.setFieldId(f.getComponentId());
        //TODO the start pos is dependent on extract type
        arg.setStartPosition(f.getStartPosition());
        arg.setFieldLength(f.getLength());
        arg.setJustifyId(f.getJustification() == null ? JustifyId.NONE : f.getJustification());
        arg.setSignedInd(f.isSigned());
        arg.setValue(new Cookie(""));

        arg.setLogfileId(logFileId);
        return arg;
    }

    protected LogicTableArg getColumnArg(ViewColumn vc) {
        LogicTableArg colarg = new LogicTableArg(); //This is the column data
        colarg.setDecimalCount(vc.getDecimalCount());
        colarg.setFieldContentId(vc.getDateCode());
        colarg.setFieldFormat(vc.getDataType());
        colarg.setFieldId(0);
        colarg.setStartPosition(vc.getExtractAreaPosition());
        //TODO the start pos is dependent on extract type
        colarg.setOrdinalPosition(vc.getOrdinalPosition());
        if(vc.getExtractArea() == ExtractArea.AREACALC) {
             colarg.setFieldLength((short)12);
        } else {
            colarg.setFieldLength(vc.getFieldLength());
        }
        colarg.setJustifyId(vc.getJustifyId() == null ? JustifyId.NONE : vc.getJustifyId());
        colarg.setSignedInd(vc.isSigned());
        colarg.setValue(new Cookie(""));
        colarg.setPadding2("");  //This seems a little silly
        return colarg;
    }

    public static void setArgValueFrom(LogicTableArg arg, String valStr) {
        ArgHelper.setArgValueFrom(arg, valStr);
    }

    public void setLogFileId(int logFileId) {
        this.logFileId = logFileId;
    }

    private LogicTableArg getArgFromValueAndColumn(String v, ViewColumn vc) {
        LogicTableArg arg = getColumnArg(vc);;
        arg.setValue(new Cookie(v));
        arg.setLogfileId(logFileId);
        arg.setOrdinalPosition(vc.getOrdinalPosition());
        arg.setJustifyId(vc.getJustifyId() == null ? JustifyId.NONE : vc.getJustifyId());
        return arg;
    }

    private LogicTableArg getArgFromValueAndSortkKey(String v, ViewSortKey sk) {
        LogicTableArg skarg = getArgFromSortkKey(sk);
        skarg.setValue(new Cookie(v));
        return skarg;
    }

    private LogicTableArg getArgFromSortkKey(ViewSortKey sk) {
        LogicTableArg skarg = new LogicTableArg(); //This is the column data
        skarg.setDecimalCount(sk.getSkDecimalCount());
        skarg.setFieldContentId(sk.getSortKeyDateTimeFormat());
        skarg.setFieldFormat(sk.getSortKeyDataType());
        skarg.setFieldId(0);
        skarg.setStartPosition(sk.getSkStartPosition());
        skarg.setOrdinalPosition(sk.getSkOrdinalPosition());
        skarg.setFieldLength(sk.getSkFieldLength());
        skarg.setSignedInd(sk.isSortKeySigned());
        skarg.setPadding2("");  //This seems a little silly
        skarg.setLogfileId(0);
        if(sk.getSortKeyDataType() == DataType.ALPHANUMERIC) {
            skarg.setJustifyId(JustifyId.LEFT);
        } else {
            skarg.setJustifyId(sk.getSkJustifyId() == null ? JustifyId.NONE : sk.getSkJustifyId());
        }
        return skarg;
    }

    private LogicTableF2 makeF2FromFieldAndColumn(LRField f, ViewColumn vc) {
        LogicTableF2 dte = new LogicTableF2();
        // want a function to get a populated arg from the field
        LogicTableArg arg1 = getArgFromField(f);
        dte.setRecordType(LtRecordType.F2);
        //TODO this can rise as a common function
        //These may be at adder level
        dte.setViewId(vc.getViewId());
        dte.setColumnId(vc.getComponentId());
        dte.setSuffixSeqNbr((short)vc.getColumnNumber());

        dte.setArg1(arg1);
        
        dte.setArg2(getColumnArg(vc));
        dte.setCompareType(LtCompareType.EQ);
        return dte;
    }

    private LogicTableF2 makeF2FromFieldAndColumnRef(LRField f, ViewColumn vc) {
        LogicTableF2 dte = new LogicTableF2();
        // want a function to get a populated arg from the field
        LogicTableArg arg1 = getArgFromField(f);
        dte.setRecordType(LtRecordType.F2);
        //TODO this can rise as a common function
        //These may be at adder level
        dte.setViewId(vc.getViewId());
        dte.setColumnId(vc.getComponentId());
        dte.setSuffixSeqNbr((short)vc.getColumnNumber());

        dte.setArg1(arg1);
        
        dte.setArg2(getColumnRefArg(vc));
        dte.setCompareType(LtCompareType.EQ);
        return dte;
    }

    private LogicTableF2 makeF2FromFieldAndSortkey(LRField f, ViewColumn vc, ViewSortKey sk) {
        LogicTableF2 f2 = new LogicTableF2();
        // want a function to get a populated arg from the field
        LogicTableArg arg1 = getArgFromField(f);
        f2.setRecordType(LtRecordType.F2);
        //TODO this can rise as a common function
        //These may be at adder level
        f2.setViewId(vc.getViewId());
        f2.setColumnId(vc.getComponentId());
        f2.setSuffixSeqNbr((short)vc.getColumnNumber());

        f2.setArg1(arg1);
        
        LogicTableArg arg2 = getArgFromSortkKey(sk);
        f2.setArg2(arg2);
        f2.setCompareType(LtCompareType.EQ);
        arg2.setValue(new Cookie(""));

        return f2;
    }

    private LogicTableF2 makeF2FromColumnAndField(ViewColumn vc, LRField f) {
        LogicTableF2 dte = new LogicTableF2();
        // want a function to get a populated arg from the field
        dte.setArg1(getColumnArg(vc));
        LogicTableArg arg2 = getArgFromField(f);
        dte.setRecordType(LtRecordType.F2);
        //TODO this can rise as a common function
        //These may be at adder level
        dte.setViewId(vc.getViewId());
        dte.setColumnId(vc.getComponentId());
        dte.setSuffixSeqNbr((short)vc.getColumnNumber());

        dte.setArg2(arg2);
        
        dte.setCompareType(LtCompareType.EQ);
        return dte;
    }


    private LogicTableNameF1 getNameF1FromAccumAndColumn(String accum, ViewColumn vc) {
        LogicTableNameF1 nf1 = new LogicTableNameF1();
        nf1.setRecordType(LtRecordType.NAMEF1);
        nf1.setViewId(vc.getViewId());
        nf1.setColumnId(vc.getComponentId());
        nf1.setSuffixSeqNbr((short)vc.getColumnNumber());
        nf1.setArg(getColumnArg(vc));
        if(!vc.isSigned()) {
            //This should generate a warning
            logger.atWarning().log("Accumulator assignments should be signed. Changing for %s", vc.getName());
            warning = GenerationWarning.COLUMN_SHOULD_BE_SIGNED;
            nf1.getArg().setSignedInd(true);
        }
        nf1.setAccumulatorName(accum);
        nf1.setCompareType(LtCompareType.EQ);
        return nf1;
    }

    private LogicTableNameF1 makeNameF1FromAccumAndField(String accum, LRField f) {
        LogicTableNameF1 sete = new LogicTableNameF1();
        sete.setRecordType(LtRecordType.NAMEF1);
        sete.setArg(getArgFromField(f));
        sete.setAccumulatorName(accum);
        return sete;
    }

    private LogicTableNameF1 makeF1FromField(String accum, LRField f) {
        LogicTableNameF1 sete = new LogicTableNameF1();
        sete.setRecordType(LtRecordType.NAMEF1);
        sete.setArg(getArgFromField(f));
        sete.setAccumulatorName(accum);
        return sete;
    }

    LtCompareType getCompareType(String op) {
        switch(op) {
            case "<":
            return LtCompareType.LT;
            case "<=":
            return LtCompareType.LE;
            case "=":
            return LtCompareType.EQ;
            case ">=":
            return LtCompareType.GE;
            case ">":
            return LtCompareType.GT;
            case "<>":
            return LtCompareType.NE;
            case "BEGINS_WITH":
            return LtCompareType.BEGINS;
            case "CONTAINS":
            return LtCompareType.CONTAINS;
            case "ENDS_WITH":
            return LtCompareType.ENDS;
            default:
            return LtCompareType.INVALID;
        }
    }

    private LogicTableNameValue makeNameValueFromAccum(String accum, String fc) {
        LogicTableNameValue arithfn = new LogicTableNameValue();
        arithfn.setRecordType(LtRecordType.NAMEVALUE);
        arithfn.setFunctionCode(fc);
        arithfn.setTableName(accumName);
        arithfn.setValue(new Cookie(accum));
        arithfn.setCompareType(LtCompareType.EQ);
        return arithfn;
    }

    protected void populateArgFromKeyTarget(LogicTableArg arg, LookupPathKey key) {
        arg.setDecimalCount(key.getDecimalCount());
        if(key.getDateTimeFormat() == null)
            arg.setFieldContentId(DateCode.NONE);
        else
            arg.setFieldContentId(key.getDateTimeFormat());
        arg.setFieldFormat(key.getDatatype());
        arg.setFieldId(key.getFieldId());
        arg.setLogfileId(key.getTargetlfid());
        arg.setLrId(key.getSourceLrId());
        //TODO the start pos is dependent on extract type
        arg.setStartPosition(key.getStartPosition());
        arg.setFieldLength(key.getFieldLength());
        arg.setJustifyId(key.getJustification() == null ? JustifyId.NONE : key.getJustification());
        arg.setSignedInd(key.isSigned());
        arg.setRounding(key.getRounding());
        arg.setValue(new Cookie(key.getValue()));
        arg.setPadding2("");  //This seems a little silly
    }

    private LogicTableF2 makeF2FromFieldAndField(LRField f1, LRField f2, String op, String fcName) {
         LogicTableF2 ltf2 = new LogicTableF2();
        // want a function to get a populated arg from the field
        LogicTableArg arg1 = getArgFromField(f1);
        ltf2.setRecordType(LtRecordType.F2);
        ltf2.setArg1(arg1);
        
        LogicTableArg arg2 = getArgFromField(f2);
        ltf2.setArg2(arg2);
        ltf2.setFunctionCode(fcName);
        ltf2.setCompareType(getCompareType(op));
        return ltf2;
    }

    private LogicTableF1 getF1FromField(LRField f) {
        LogicTableF1 f1 = new LogicTableF1();
        f1.setRecordType(LtRecordType.F1);
        f1.setArg(getArgFromField(f));
        return f1;
    }

    private LogicTableF1 getF1FromFColumn(ViewColumn vc) {
        LogicTableF1 f1 = new LogicTableF1();
        f1.setRecordType(LtRecordType.F1);
        f1.setArg(getColumnArg(vc));
        return f1;
    }

    private LogicTableF2 getF2ForColumnRef(ViewColumn c, ViewColumn v) {
    //     VDPFormatInfo & fiArg = Arg.GetFormatInfo();
    //     fiArg.SetStartPosition(fiArg.GetStartPosition() + m_offset - 1);
    //     if (m_viewColumn->GetExtractArea() != AttrExtractAreaCT) {
    //         fiArg.SetFieldLength(getLength());
    //     }
    //     else {
    //         fiArg.SetFieldLength(12); //CT columns are 12 bytes
    //     }
            LogicTableF2 dtx = new LogicTableF2();
        dtx.setArg1(getColumnRefArg(c));
        dtx.setArg2(getColumnArg(v));
        dtx.setFunctionCode("DTX");
        dtx.setRecordType(LtRecordType.F2);
        dtx.setCompareType(LtCompareType.EQ);
        return dtx;
    }

    protected LogicTableArg getColumnRefArg(ViewColumn vc) {
        LogicTableArg colarg = new LogicTableArg(); //This is the column data
        colarg.setDecimalCount(vc.getDecimalCount());
        colarg.setFieldContentId(vc.getDateCode());
        colarg.setFieldFormat(vc.getDataType());
        colarg.setFieldId(vc.getColumnNumber());
        colarg.setLogfileId(vc.getExtractArea().ordinal());
        //TODO the start pos is dependent on extract type
        colarg.setStartPosition(vc.getExtractAreaPosition());
        if(vc.getExtractArea().equals(ExtractArea.AREACALC)) {
            colarg.setFieldLength((short)12);
        } else {
            colarg.setFieldLength(vc.getFieldLength());
        }
        colarg.setJustifyId(vc.getJustifyId() == null ? JustifyId.NONE : vc.getJustifyId());
        colarg.setSignedInd(vc.isSigned());
        colarg.setValue(new Cookie(""));
        colarg.setPadding2("");  //This seems a little silly
        return colarg;
    }

}
