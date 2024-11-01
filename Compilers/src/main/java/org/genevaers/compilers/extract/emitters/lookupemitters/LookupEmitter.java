package org.genevaers.compilers.extract.emitters.lookupemitters;

import java.util.ArrayList;

/*
 * Copyright Contributors to the GenevaERS Project. SPDX-License-Identifier: Apache-2.0 (c) Copyright IBM Corporation 2008.
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
import java.util.List;

import org.genevaers.compilers.extract.astnodes.ASTFactory.Type;
import org.apache.commons.lang3.StringUtils;
import org.genevaers.compilers.extract.astnodes.ExtractBaseAST;
import org.genevaers.compilers.extract.astnodes.LookupPathAST;
import org.genevaers.compilers.extract.astnodes.SortTitleAST;
import org.genevaers.compilers.extract.emitters.CodeEmitter;
import org.genevaers.compilers.extract.emitters.helpers.EmitterArgHelper;
import org.genevaers.genevaio.ltfactory.LtFactoryHolder;
import org.genevaers.genevaio.ltfactory.LtFuncCodeFactory;
import org.genevaers.genevaio.ltfile.Cookie;
import org.genevaers.genevaio.ltfile.LogicTableArg;
import org.genevaers.genevaio.ltfile.LogicTableF1;
import org.genevaers.genevaio.ltfile.LogicTableF2;
import org.genevaers.genevaio.ltfile.LogicTableRE;
import org.genevaers.repository.RepoHelper;
import org.genevaers.repository.Repository;
import org.genevaers.repository.components.LRField;
import org.genevaers.repository.components.LookupPath;
import org.genevaers.repository.components.LookupPathKey;
import org.genevaers.repository.components.LookupPathStep;
import org.genevaers.repository.components.enums.DataType;
import org.genevaers.repository.components.enums.DateCode;
import org.genevaers.repository.components.enums.JustifyId;
import org.genevaers.repository.jltviews.JLTView;

import com.google.common.flogger.FluentLogger;

public class LookupEmitter extends CodeEmitter {
    private static final FluentLogger logger = FluentLogger.forEnclosingClass();
    boolean optimizable;

    private LookupPathAST parentAST;
    private LogicTableF1 firstLookupRecord;
    private LogicTableRE lusm;
    private LogicTableRE luex;
    private LogicTableF1 kslk;
    private LKCEmitter lkce;
    private LKSEmitter lkse;
    private LKFieldEmitter lkfe;
    List<LogicTableRE> lusms;
    private int sortTitleFieldId;

    public LogicTableF1 emitJoin(LookupPathAST lookupAST, boolean skt) {
        // We need to determine JOIN or LKLR
        // Then itereate through the steps to emit the key codes
        // and finally the lookup function
        // and don't forget the default for not found
        lkce = new LKCEmitter();
        lkse = new LKSEmitter();
        lkfe = new LKFieldEmitter();
        parentAST = lookupAST;
        lusms = new ArrayList<>();

        // C++ The emitter base has a handle on the source LR and LF
        // We need them for the Lookup definitions.

        // We also have to take into account the optimizable flag
 
        // Iterate the steps of the lookup
        // First step mean JOIN or LKLR
        LogicTableF1 retEntry = null;
        LookupPath lookup = lookupAST.getLookup();
        optimizable = isLookupOptimizable(lookup);
        Iterator<LookupPathStep> si = lookup.getStepIterator();
        //Uncomment me for some help lookup.dumpDetails("emitJoin");
        while (si.hasNext()) {
            LookupPathStep step = si.next();
            if (step.getStepNum() == 1 && optimizable && parentAST.getType() != Type.SORTTITLE) {
                retEntry = addJOIN(lookup, skt, lookupAST.getNewJoinId());
                retEntry.setSuffixSeqNbr(ExtractBaseAST.getCurrentColumnNumber());
                firstLookupRecord = retEntry;
                ExtractBaseAST.getLtEmitter().addToLogicTable(firstLookupRecord);
            } else {
                // An LKLR does not have gotos really
                retEntry = addLKLR(lookup, step, skt, lookupAST.getNewJoinId());
                if (step.getStepNum() == 1) {
                    firstLookupRecord = retEntry;
                }
            }
            Iterator<LookupPathKey> ki = step.getKeyIterator();
            while(ki.hasNext()) {
                emitKey(lookupAST, lookup, ki);
            }
            emitEffectiveDateForStepIfNeeded(lookupAST, step);
            if(parentAST.getType() == Type.SORTTITLE) {
                kslk = ((SortTitleAST)parentAST).emitKSLK();
                kslk.getArg().setLogfileId(lookup.getTargetLFID());
                kslk.setFileId(lookup.getTargetLFID());
                ExtractBaseAST.getLtEmitter().addToLogicTable(kslk);
            } else {
                if(step.hasExit()) {
                    lusm = emitLUEX(step, lookup);               
                } else {
                    lusm = emitLUSM(step, lookup);               
                }
            }
        }
        int truePos = ExtractBaseAST.getLtEmitter().getNumberOfRecords();
        //Naughty hard coding... should be done after assigment or whatever
        int falsePos = truePos +2;
        if(firstLookupRecord != null) {
            firstLookupRecord.setGotoRow1(truePos);
            firstLookupRecord.setGotoRow2(falsePos);
        }
        if(lusm != null) {
            //The default is that the LUSM gotos are the same as the JOIN
            //But for the intermediate steps this is not the case
            lusm.setGotoRow1(truePos);
            lusm.setGotoRow2(falsePos);
            for (LogicTableRE intenalLUSM : lusms) {
                intenalLUSM.setGotoRow2(falsePos);
            }
        }
        return retEntry;
    }

    private void emitEffectiveDateForStepIfNeeded(LookupPathAST lookupAST, LookupPathStep step) {
        if(RepoHelper.isLrEffectiveDated(step.getTargetLR())) {
            lookupAST.emitEffectiveDate();
        }
    }

    private LogicTableF1 emitKSLK() {
        LtFuncCodeFactory ltFact = LtFactoryHolder.getLtFunctionCodeFactory();
        LogicTableF1 kslk = (LogicTableF1) ltFact.getKSLK(Repository.getFields().get(sortTitleFieldId));
        return kslk;
    }

    public void setSortTitleFieldId(int sortTitleFieldId) {
        this.sortTitleFieldId = sortTitleFieldId;
    }


    private LogicTableRE emitLUEX(LookupPathStep step, LookupPath lookup) {
        LtFuncCodeFactory ltFact = LtFactoryHolder.getLtFunctionCodeFactory();
        luex = (LogicTableRE) ltFact.getLUEX();
        luex.setReadExitId(step.getReadExitId());
        ExtractBaseAST.getLtEmitter().addToLogicTable(luex);
        if(step.getStepNum() < lookup.getNumberOfSteps()) {
            int truePos = ExtractBaseAST.getLtEmitter().getNumberOfRecords();
            luex.setGotoRow1(truePos);
            //the false pos can only be worked out at the end.
            //keep a list of LUSMs
            lusms.add(luex);
            luex.setGotoRow2(parentAST.getGoto2());
        }
        return luex;
    }

    private void emitKey(LookupPathAST lookupAST, LookupPath lookup,
            Iterator<LookupPathKey> ki) {
        LookupPathKey key = ki.next();
        //C++ uses LPSourceKeyAST Nodes 
        //Where are they added to the 
        // There is a resolveSourceKeys function on the LPEmitter that builds the steps and underlying sourcekeys
        // Do we need that it is already in the repo like that?
        // Or at least sort of ... the key tyoe is not stored directly
        //So that is done at Parse time....

        //call path is the g calling generateASTFieldRefNode
        // resolveFieldDef -> m_lpEmitter.resolveSourceKeys

        // Look like there could be many copies of the lookup tree. But I don't imagine there is?

        // Do we need to do this?
        //
        // Thought we could build the XLT function code sequence and keep it around... then we can 
        // simply recall it later if and when needed? Build once... use many
        // Or not if we can add the optimisation to not repeat the function codes...?
        // We need to accumulate date for the JLT anyway...

        // Where will we keep this collection of lookup information?
        // The repository would seem to be the natural place.
        if(key.getFieldId() != 0) {
            //To get the correct lf we need to find the step that has the key field's LR as its target
            //Then that's steps target lf id what we want
            //But the field arg are those of the generated/mapped field 
            int lfid;
            lkfe.setLogicalFileID(ExtractBaseAST.getCurrentViewSource().getSourceLFID());
            LogicTableF2 lk = lkfe.emit(key, lookup);
            ExtractBaseAST.getLtEmitter().addToLogicTable(lk);
        } else if(key.getSymbolicName().length() > 0) {
            LogicTableF1 lks = lkse.emit(key);
            String val = lookupAST.getSymbolValue(key.getSymbolicName());
            if(val != null) {
                EmitterArgHelper.setArgValueFrom(lks.getArg(), val);
            } else {
                //Error if symbolic name not found at all...
                //  How to track... tick off the ones found
                //  then at end show the unused?
            }
            ExtractBaseAST.getLtEmitter().addToLogicTable(lks);
        } else {
            //must be a constant
            LogicTableF1 lkc = lkce.emit(key);
            ExtractBaseAST.getLtEmitter().addToLogicTable(lkc);
        }
        addKey(key);
    }

    private LogicTableRE emitLUSM(LookupPathStep step, LookupPath lookup) {
        LtFuncCodeFactory ltFact = LtFactoryHolder.getLtFunctionCodeFactory();
        lusm = (LogicTableRE) ltFact.getLUSM();

        ExtractBaseAST.getLtEmitter().addToLogicTable(lusm);
        if(step.getStepNum() < lookup.getNumberOfSteps()) {
            int truePos = ExtractBaseAST.getLtEmitter().getNumberOfRecords();
            lusm.setGotoRow1(truePos);
            //the false pos can only be worked out at the end.
            //keep a list of LUSMs
            lusms.add(lusm);
            lusm.setGotoRow2(parentAST.getGoto2());
        }
        return lusm;
    }

    private void addKey(LookupPathKey key) {
        //For a symbol we need to know if the value was supplied
        //ditto for effective date
        //That we should know at parse time...
        //Which is where and when we should build the AST nodes to be emitted.


        //C++ 
        //extractParser.g:lookupRef      : m:META_REF {#lookupRef = generateASTLookupRefNode(#m);}
        // makes a LP REF node and calls resolveLookup on it
        //  pulls apart the text to get the lookupField Ref, the effective date and the symbol list
        //      resolveLookupRef
        //          checks to see lookup exists
        //          preps the lpemitter with final step and joinstep vector
        //          adds the join field ref to track the lookups being used
        //      resolveDate
        //          format of the date is verified first
        //          makes an AST Node ... Typed
        //          add the node to the AST tree
        //          add the eff date to the lpEmitter of the LPRefASTNode
        //      resolveSymbolList
        //          makes a symbolListRef...
        //          resolveSymbolList called on it
        //             take each entry and resolves it
        //                checks to see if it is valid
        //                   error if not
        //                adds value to a local map keyed by sym name
        //                adds AST string node to the tree
        //          adds the symbol list to the tree
        //          sets the symbol list AST to the lpEmitter
        //      resolveSourceKeys
        //          


        //What type of key field is this
    }

    private LogicTableF1  addLKLR(LookupPath lookup, LookupPathStep step, boolean skt, int newJoinId) {
        LtFuncCodeFactory ltFact = LtFactoryHolder.getLtFunctionCodeFactory();
        //we need to know if this is an skt case or not
        String idStr = Integer.toString(newJoinId);
        JLTView jv = Repository.getJoinViews().getJLTViewFromLookup(lookup, skt);
        LogicTableF1 lklr = (LogicTableF1) ltFact.getLKLR(idStr);
        LogicTableArg arg = lklr.getArg();
        arg.setFieldId(lookup.getTargetLRIndexID());
        arg.setLogfileId(step.getTargetLF());
        arg.setLrId(step.getTargetLR());
        lklr.setArg(arg);
        arg.setFieldContentId(DateCode.NONE);
        arg.setFieldFormat(DataType.INVALID);
        arg.setJustifyId(JustifyId.NONE);
        arg.setOrdinalPosition((short)step.getStepNum());
        arg.setValue(new Cookie(idStr));
        lklr.setColumnId(lookup.getID());

        ExtractBaseAST.getLtEmitter().addToLogicTable(lklr);
        return lklr;
    }

    private LogicTableF1 addJOIN(LookupPath lookup, boolean skt, int newJoinId) {
        LtFuncCodeFactory ltFact = LtFactoryHolder.getLtFunctionCodeFactory();
        //TODO pr.set column ID is set as a place holder to old Join ID
        //remember JoinIDs are all juggled BEFORE the extract is emitted
        String idStr = Integer.toString(newJoinId);
        JLTView jv = Repository.getJoinViews().getJLTViewFromLookup(lookup, skt);
        LogicTableF1 join = (LogicTableF1) ltFact.getJOIN(idStr);

        LogicTableArg arg = join.getArg();
        //CPP version writes the LF and LR from the target of the first step
        //Which leads to comparison errors in the LT Print
        //for multistep lookups
        arg.setFieldId(lookup.getTargetLRIndexID());
        arg.setLogfileId(lookup.getStep(1).getTargetLF());
        arg.setLrId(lookup.getStep(1).getTargetLR());
        join.setArg(arg);
        arg.setFieldContentId(DateCode.NONE);
        arg.setFieldFormat(DataType.INVALID);
        arg.setJustifyId(JustifyId.NONE);
        arg.setValue(new Cookie(idStr));

        //TODO There is still magic to do with the gotos
        join.setGotoRow1(parentAST.getGoto1());
        join.setGotoRow2(parentAST.getGoto2());
        join.setColumnId(lookup.getID());
        return join;
    }

    private boolean isLookupOptimizable(LookupPath lookup) {
        if(lookup.getReadExitId() > 0) {
            return Repository.getUserExits().get(lookup.getReadExitId()).isOptimizable();
        } else {
            return true;
        }
    }

    protected void populateArgFromField(LogicTableArg arg, LRField fld) {
        arg.setDecimalCount(fld.getNumDecimalPlaces());
        arg.setFieldContentId(fld.getDateTimeFormat());
        arg.setFieldFormat(fld.getDatatype());
        arg.setFieldId(fld.getComponentId());
        //TODO the start pos is dependent on extract type
        arg.setStartPosition(fld.getStartPosition());
        arg.setFieldLength(fld.getLength());
        arg.setJustifyId(fld.getJustification());
        arg.setValue(new Cookie("0"));
        arg.setPadding2("");  //This seems a little silly
    }

    protected void populateArgFromKeyTarget(LogicTableArg arg, LookupPathKey key) {
        arg.setDecimalCount(key.getDecimalCount());
        if(key.getDateTimeFormat() == null)
            arg.setFieldContentId(DateCode.NONE);
        else
            arg.setFieldContentId(key.getDateTimeFormat());
        arg.setFieldFormat(key.getDatatype());
        arg.setFieldId(key.getComponentId());
        //TODO the start pos is dependent on extract type
        arg.setStartPosition(key.getStartPosition());
        arg.setFieldLength(key.getFieldLength());
        arg.setJustifyId(key.getJustification());
        arg.setSignedInd(key.isSigned());
        //This stuff should be manage at compile time?
        //The warning generation and rewrite
        if(key.getValue().length() > 0) {
            if(key.getDatatype() == DataType.ALPHANUMERIC) {
                arg.setValue(new Cookie(key.getValue()));
            } else {
                if(StringUtils.isNumeric(key.getValue())) {
                    arg.setValue(new Cookie(key.getValue()));
                } else {
                    arg.setValue(new Cookie("0"));
                }
            }
        } else {
            Cookie c;
            if(key.getDatatype() == DataType.ALPHANUMERIC) {
                c = new Cookie(StringUtils.repeat(" ", key.getFieldLength()));
            } else {
                c = new Cookie("0");
            }
            arg.setValue(c);
        }
        arg.setPadding2("");  //This seems a little silly
    }

    public void resolveGotos(Integer joinT, Integer joinF) {
        if(joinT != null) {
            if(firstLookupRecord != null) {
                firstLookupRecord.setGotoRow1(joinT);
            }
            lusm.setGotoRow1(joinT);
        }
        if(joinF != null) {
            setFalseGotos(joinF);
        }
    }

    public void setFalseGotos(Integer pos) {
        int falsePos;
        if(pos != null) {
            falsePos = pos;
        } else {
            falsePos = ExtractBaseAST.getLtEmitter().getLogicTable().getLastEntry().getRowNbr();
        }
        if(firstLookupRecord != null) {
            firstLookupRecord.setGotoRow2(falsePos);
        }
        if(lusm != null) {
            //The default is that the LUSM gotos are the same as the JOIN
            //But for the intermediate steps this is not the case
            lusm.setGotoRow2(falsePos);
            for (LogicTableRE intenalLUSM : lusms) {
                intenalLUSM.setGotoRow2(falsePos);
            }
        }
    }

}
