package org.genevaers.compilers.extract.astnodes;

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


import java.util.Iterator;

import org.genevaers.compilers.base.ASTBase;
import org.genevaers.compilers.base.EmittableASTNode;
import org.genevaers.compilers.extract.astnodes.ASTFactory.Type;
import org.genevaers.compilers.extract.emitters.helpers.EmitterArgHelper;
import org.genevaers.compilers.extract.emitters.lookupemitters.LookupEmitter;
import org.genevaers.genevaio.ltfactory.LtFactoryHolder;
import org.genevaers.genevaio.ltfile.Cookie;
import org.genevaers.genevaio.ltfile.LogicTableArg;
import org.genevaers.genevaio.ltfile.LogicTableF1;
import org.genevaers.genevaio.ltfile.LogicTableF2;
import org.genevaers.repository.Repository;
import org.genevaers.repository.components.LookupPath;
import org.genevaers.repository.components.enums.LtCompareType;
import org.genevaers.repository.components.enums.DateCode;
import org.genevaers.repository.components.enums.JustifyId;
import org.genevaers.repository.components.enums.DataType;
import org.genevaers.repository.components.enums.LtRecordType;
import org.genevaers.repository.jltviews.JLTView;
import org.genevaers.repository.jltviews.UniqueKeyData;
import org.genevaers.repository.jltviews.UniqueKeys;

public class LookupPathAST extends FormattedASTNode implements EmittableASTNode{

    protected LookupPath lookup;
    protected LookupEmitter lkEmitter = new LookupEmitter();
    protected Integer goto1 = Integer.valueOf(0);
    protected Integer goto2 = Integer.valueOf(0);
    protected int newJoinId;

    protected SymbolList symbols;
    protected EffDateValue effDateValue;
    private String uniqueKey;

    @Override
    public void emit() {
        goto1 = ExtractBaseAST.getLtEmitter().getNumberOfRecords();
    }
    

    public LookupPath getLookup() {
        return lookup;
    }

    public Integer getGoto1() {
        return goto1;
    }

    public Integer getGoto2() {
        return goto2;
    }

    @Override
    public void resolveGotos(Integer compT, Integer compF, Integer joinT, Integer joinF) {
        lkEmitter.resolveGotos(joinT, joinF);
    }

    public String getSymbolValue(String symbolicName) {
        String val = null;
        if(symbols != null) {
            val = symbols.getValueFor(symbolicName);
        }
        return val;
    }

    public void setSymbols(SymbolList symbols) {
        this.symbols = symbols;
    }

    public SymbolList getSymbols() {
        return symbols;
    }

    public void setEffDateValue(EffDateValue effDateValue) {
        this.effDateValue = effDateValue;
    }


    public void emitEffectiveDate() {
        boolean defaultRequired = true;
        if(effDateValue != null) {
            defaultRequired = getEffDateValueIfSet();
        }
        if(defaultRequired) {
            emitLKDC(ASTFactory.getNodeOfType(ASTFactory.Type.RUNDATE));
        }
    }


    private boolean getEffDateValueIfSet() {
        boolean defaultRequired = true;
        Iterator<ASTBase> ci = effDateValue.getChildIterator();
        while (ci.hasNext()) {
            ExtractBaseAST c = (ExtractBaseAST)ci.next();
            if(c.getType() == ASTFactory.Type.LRFIELD) {
                emitLKDE((FieldReferenceAST) c);
                defaultRequired = false;
            } else {
                emitLKDC(c);
                defaultRequired = false;
            }
        }
        return  defaultRequired;
    }


    private void emitLKDE(FieldReferenceAST f) {
        //Need to change the generation of this... needs the fields and key part
        //LtFactoryHolder.getLtFunctionCodeFactory().getLKDE(null);
        LogicTableF2 lkde = new LogicTableF2();
        lkde.setRecordType(LtRecordType.F2);
        lkde.setFunctionCode("LKDE");

        LogicTableArg arg1 = new LogicTableArg();
        arg1.setStartPosition((short)f.getRef().getStartPosition());
        arg1.setFieldContentId(f.getDateCode());
        arg1.setFieldLength(f.getRef().getLength());
        arg1.setFieldFormat(f.getDataType());
        arg1.setJustifyId(JustifyId.NONE);
        arg1.setLrId(f.getRef().getLrID());
        arg1.setLogfileId(ltEmitter.getFileId());
        arg1.setFieldId(f.getRef().getComponentId());
        arg1.setValue(new Cookie(""));
        lkde.setArg1(arg1);

        LogicTableArg arg2 = new LogicTableArg();
        arg2.setStartPosition((short)1);
        arg2.setFieldContentId(DateCode.CCYYMMDD);
        arg2.setFieldLength((short)4);
        arg2.setFieldFormat(DataType.BINARY);
        arg2.setJustifyId(JustifyId.NONE);
        arg2.setValue(new Cookie(""));
        lkde.setArg2(arg2);
        lkde.setCompareType(LtCompareType.EQ);
        ExtractBaseAST.getLtEmitter().addToLogicTable(lkde);
}


    private void emitLKDC(ExtractBaseAST val) {

        LogicTableF1 lkd = new LogicTableF1();
        lkd.setRecordType(LtRecordType.F1);
        lkd.setFunctionCode("LKDC");

        LogicTableArg arg = new LogicTableArg();
        arg.setStartPosition((short)1);
        arg.setFieldContentId(DateCode.CCYYMMDD);
        arg.setFieldLength((short)4);
        arg.setFieldFormat(DataType.BINARY);
        arg.setJustifyId(JustifyId.NONE);
        EmitterArgHelper.setConstArgVal(val, arg);
        arg.setLogfileId(ExtractBaseAST.getLtEmitter().getFileId());
        lkd.setArg(arg);
        lkd.setCompareType(LtCompareType.EQ);
        ExtractBaseAST.getLtEmitter().addToLogicTable(lkd);
    }

    public LookupEmitter getLkEmitter() {
        return lkEmitter;
    }

    public void resolveLookup(LookupPath lk) {
        JLTView jv = Repository.getJoinViews().addJLTView(lookup);
        if(currentViewColumnSource != null) {
            jv.updateLastReason(currentViewColumnSource.getViewId(), currentViewColumnSource.getColumnNumber());
        }
    }


    @Override
    public DataType getDataType() {
        // TODO Auto-generated method stub
        return null;
    }


    @Override
    public DateCode getDateCode() {
        // TODO Auto-generated method stub
        return null;
    }


    /* 
     * Lookups are considered unique based on the combination of
     * Effective date values and symbols
     * So at MR95 runtime they each have their own lookup buffer
     * Therefore we renumber the lookups based on the uniquness.
     */
    public void makeUnique() {
        uniqueKey = lookup.getID() + "_";
        if(lookup.isOptimizable()) {
            uniqueKey += effDateValue != null ? effDateValue.getUniqueKey() : "";
            uniqueKey += symbols != null ? symbols.getUniqueKey() : "";
        }
        UniqueKeyData uk = UniqueKeys.getOrMakeUniuUniqueKeyData(uniqueKey, lookup.getID());
        newJoinId = uk.getNewJoinId();
    }

    public int getNewJoinId() {
        return newJoinId;
    }

    public String getUniqueKey() {
        return uniqueKey;
    }


    @Override
    public String getMessageName() {
        return lookup.getName();
    }


    @Override
    public int getMaxNumberOfDigits() {
        return 0;
    }
}
