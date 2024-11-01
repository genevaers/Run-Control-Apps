package org.genevaers.compilers.extract.astnodes;

import java.util.Set;

import org.genevaers.genevaio.ltfactory.LtFactoryHolder;
import org.genevaers.genevaio.ltfactory.LtFuncCodeFactory;
import org.genevaers.genevaio.ltfile.Cookie;
import org.genevaers.genevaio.ltfile.LTFileObject;
import org.genevaers.genevaio.ltfile.LTRecord;

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


import org.genevaers.genevaio.ltfile.LogicTableArg;
import org.genevaers.genevaio.ltfile.LogicTableF0;
import org.genevaers.genevaio.ltfile.LogicTableF1;
import org.genevaers.genevaio.ltfile.LogicTableF2;
import org.genevaers.genevaio.ltfile.LogicTableNameF1;
import org.genevaers.repository.RepoHelper;
import org.genevaers.repository.Repository;
import org.genevaers.repository.components.LRField;
import org.genevaers.repository.components.LogicalFile;
import org.genevaers.repository.components.LogicalRecord;
import org.genevaers.repository.components.LookupPath;
import org.genevaers.repository.components.ViewColumn;
import org.genevaers.repository.components.ViewSortKey;
import org.genevaers.repository.components.enums.DataType;
import org.genevaers.repository.components.enums.DateCode;
import org.genevaers.repository.components.enums.ExtractArea;
import org.genevaers.repository.components.enums.JustifyId;
import org.genevaers.repository.jltviews.ExitJoin;
import org.genevaers.repository.jltviews.JLTView;
import org.genevaers.repository.jltviews.JLTViewMap;
import org.genevaers.repository.jltviews.JoinViewsManager;
import org.genevaers.repository.jltviews.ReferenceJoin;

public class LookupFieldRefAST extends LookupPathAST implements Assignable, CalculationSource, Concatable{

    private LRField ref;

    public LookupFieldRefAST() {
        type = ASTFactory.Type.LOOKUPFIELDREF;
    }
 
    public void resolveField(LookupPath lk, String fieldName) {
        //get the targer LR
        //And look for the field in it
        lookup = lk;
        LogicalRecord targLR = lk.getTargetLR();
        LRField fld = targLR.findFromFieldsByName(fieldName);
        if(fld != null) {       
            ref = fld;
            JLTView jv = Repository.getJoinViews().addJLTViewFromLookupField(lookup, fld);
            if(currentViewColumnSource != null) {
                jv.updateLastReason(currentViewColumnSource.getViewId(), currentViewColumnSource.getColumnNumber());
                Repository.getDependencyCache().addNamedLookupField(lookup, fld);
            } else if(currentViewSource != null) {
                jv.updateLastReason(currentViewSource.getViewId(), currentViewSource.getSequenceNumber());
                Repository.getDependencyCache().addNamedLookupField(lookup, fld);
            } else {
                ErrorAST err = (ErrorAST) ASTFactory.getNodeOfType(ASTFactory.Type.ERRORS);
                String eString = String.format("resolve lookup %s Field %s with no source ",lk.getName(), fieldName);
                err.addError(eString);
                addChildIfNotNull(err);                    
            }
        } else {
            ErrorAST err = (ErrorAST) ASTFactory.getNodeOfType(ASTFactory.Type.ERRORS);
            err.addError("Unknown field " + fieldName);
            addChildIfNotNull(err);
        }
    }

    public LRField getRef() {
        return ref;
    }
    
    public void populateArg(LogicTableArg arg1) {
        arg1.setDecimalCount(ref.getNumDecimalPlaces());
        arg1.setFieldContentId(ref.getDateTimeFormat());
        arg1.setFieldFormat(ref.getDatatype());
        arg1.setFieldId(ref.getComponentId());
        //TODO the start pos is dependent on extract type
        arg1.setStartPosition(ref.getStartPosition());
        arg1.setFieldLength(ref.getLength());
        arg1.setJustifyId(ref.getJustification());
        arg1.setSignedInd(ref.isSigned());
        arg1.setValue(new Cookie(0, ""));
        arg1.setPadding2("");  //This seems a little silly
    }

    @Override
    public LTFileObject getAssignmentEntry(ColumnAST lhs, ExtractBaseAST rhs) {
        LtFuncCodeFactory fcf = LtFactoryHolder.getLtFunctionCodeFactory();
        //An issue here is that the original ref field has been mapped to a generated field
        //That is within the RED LR
        //We need to use that for arg1
        //How do we get it?
        //ask the repo to find it
        //And this is then seriously messed up since there is a mixture
        //of old and new used in the dtl function code.
        //The arg1 LF LR and field ID refer to the orignal
        //but the start position etc refer to the RED field
        LRField redField = Repository.getREDfieldFrom(lookup, ref);
        if(currentViewColumn.getExtractArea() == ExtractArea.AREADATA) {
            LogicTableF2 dtl = (LogicTableF2) fcf.getDTL(redField, currentViewColumn);
            LogicTableArg arg1 = dtl.getArg1();
            arg1.setLogfileId(lookup.getTargetLFID());
            arg1.setLrId(lookup.getTargetLRID());
            arg1.setFieldId(ref.getComponentId());
            arg1.setFieldFormat(getDataType());
            arg1.setFieldContentId(getDateCode());
            LogicTableArg arg2 = dtl.getArg2();
            flipDataTypeIfFieldAlphanumeric(arg1, arg2);
            arg2.setFieldContentId(lhs.getDateCode());
            ltEmitter.addToLogicTable((LTRecord)dtl);
        }else if(currentViewColumn.getExtractArea() == ExtractArea.AREACALC) {
            LogicTableF1 ctl = (LogicTableF1) fcf.getCTL(redField, currentViewColumn);
            ctl.setColumnId(currentViewColumn.getComponentId());
            LogicTableArg arg1 = new LogicTableArg();
            ctl.setArg(arg1);
            arg1.setLogfileId(lookup.getTargetLFID());
            arg1.setLrId(lookup.getTargetLRID());
            arg1.setFieldId(ref.getComponentId());
            arg1.setFieldFormat(getDataType());
            arg1.setFieldContentId(getDateCode());
            arg1.setFieldFormat(redField.getDatatype());
            arg1.setStartPosition(redField.getStartPosition());
            arg1.setFieldLength(redField.getLength());
            arg1.setSignedInd(redField.isSigned());
            arg1.setDecimalCount(redField.getNumDecimalPlaces());
            arg1.setOrdinalPosition(currentViewColumn.getOrdinalOffset());
            arg1.setJustifyId(JustifyId.RIGHT);
            arg1.setValue(new Cookie(""));
            ltEmitter.addToLogicTable((LTRecord)ctl);
        } else {
            ViewSortKey sk = Repository.getViews().get(currentViewColumn.getViewId()).getViewSortKeyFromColumnId(currentViewColumn.getComponentId());
            LogicTableF2 skl = (LogicTableF2) fcf.getSKL(redField, currentViewColumn, sk);
            skl.getArg1().setLogfileId(lookup.getTargetLFID());
            LogicTableArg arg1 = skl.getArg1();
            arg1.setLogfileId(lookup.getTargetLFID());
            arg1.setLrId(lookup.getTargetLRID());
            arg1.setFieldId(ref.getComponentId());
            LogicTableArg skarg2 = skl.getArg2();
            flipDataTypeIfFieldAlphanumeric(arg1, skarg2);
            stripDatesIfSame(skl);
            ltEmitter.addToLogicTable((LTRecord)skl);
        }
        return null;
    }

    @Override
    public LTFileObject emitSetFunctionCode() {
        LtFuncCodeFactory fcf = LtFactoryHolder.getLtFunctionCodeFactory();
        //The ref field should now be replaced by the gen lr field.
        //How do we get to it?
        LogicTableNameF1 setl = (LogicTableNameF1) fcf.getSETL("", ref);
        LogicTableArg arg = setl.getArg();
        argFixup(arg);
        ltEmitter.addToLogicTable((LTRecord)setl);
        return null;
    }

    private void argFixup(LogicTableArg arg) {
        LRField redField = null;
        //Look in the ref joins 
        if(Repository.getJoinViews().getReferenceJLTViews() != null) {
            ReferenceJoin refJoin = Repository.getJoinViews().getReferenceJLTViews().getJLTView(ref.getLrID(), false);
            if(refJoin != null) {
                redField = refJoin.getRedLR().findFromFieldsByName(ref.getName());
            }
        }
        if(redField == null) {
                JLTViewMap<ExitJoin> exitJoins = Repository.getJoinViews().getExitJLTViews();
                ExitJoin exitj = exitJoins.getJLTView(ref.getLrID(), false);
                redField = exitj.getRefFields().stream().filter(ele -> ele.getName().equals(ref.getName()))
                .findFirst()
                .orElse(null);
        }
        JoinViewsManager jvm = Repository.getJoinViews();
        arg.setStartPosition(redField.getStartPosition());
        arg.setLogfileId(lookup.getTargetLFID());
        arg.setOrdinalPosition(ref.getOrdinalPosition());
    }

    @Override
    public LTFileObject emitAddFunctionCode() {
        LtFuncCodeFactory fcf = LtFactoryHolder.getLtFunctionCodeFactory();
        LogicTableNameF1 addl = (LogicTableNameF1) fcf.getADDL("", ref);
        argFixup(addl.getArg());
        ltEmitter.addToLogicTable((LTRecord)addl);
        return null;
    }

    @Override
    public LTFileObject emitSubFunctionCode() {
        LtFuncCodeFactory fcf = LtFactoryHolder.getLtFunctionCodeFactory();
        LogicTableNameF1 subl = (LogicTableNameF1) fcf.getSUBL("", ref);
        argFixup(subl.getArg());
        ltEmitter.addToLogicTable((LTRecord)subl);
        return null;
    }

    @Override
    public LTFileObject emitMulFunctionCode() {
        LtFuncCodeFactory fcf = LtFactoryHolder.getLtFunctionCodeFactory();
        LogicTableNameF1 mull = (LogicTableNameF1) fcf.getMULL("", ref);
        argFixup(mull.getArg());
        ltEmitter.addToLogicTable((LTRecord)mull);
        return null;
    }

    @Override
    public LTFileObject emitDivFunctionCode() {
        LtFuncCodeFactory fcf = LtFactoryHolder.getLtFunctionCodeFactory();
        LogicTableNameF1 divl = (LogicTableNameF1) fcf.getDIVL("", ref);
        argFixup(divl.getArg());
        ltEmitter.addToLogicTable((LTRecord)divl);
        return null;
    }

    public void emitLookupDefault() {
        LtFuncCodeFactory fcf = LtFactoryHolder.getLtFunctionCodeFactory();
        // Emit goto followed by correct DTC
        LTRecord lkEntry = ltEmitter.getLogicTable().getLastEntry();
        LogicTableF0 ltgoto = (LogicTableF0) fcf.getGOTO();
        ltEmitter.addToLogicTable((LTRecord) ltgoto);

        // we could let the fcf auto correc the DT type base on the column?
        
        switch (currentViewColumn.getExtractArea()) {
            case AREADATA:
                LogicTableF1 dtc = null;
                if(lkEntry.getFunctionCode().equals("DTL")) {
                    DataType dtlDataType = ((LogicTableF2)lkEntry).getArg2().getFieldFormat();
                    if (dtlDataType == DataType.ALPHANUMERIC) {
                        dtc = (LogicTableF1)fcf.getDTC(" ", currentViewColumn);
                    } else {
                        dtc = (LogicTableF1)fcf.getDTC("0", currentViewColumn);
                    }
                    dtc.getArg().setFieldFormat(dtlDataType);
                    if(currentViewColumn.getDateCode() == ref.getDateTimeFormat()) {
                        dtc.getArg().setFieldContentId(DateCode.NONE);
                    } 
                } else if(lkEntry.getFunctionCode().equals("DTA")) {
                    dtc = (LogicTableF1)fcf.getDTC("0", currentViewColumn);
                    dtc.getArg().setSignedInd(true); //Force signed
                }
                ltEmitter.addToLogicTable((LTRecord)dtc );
                break;

            case AREACALC:
                if (currentViewColumn.getDataType() == DataType.ALPHANUMERIC) {
                    ltEmitter.addToLogicTable((LTRecord) fcf.getCTC(" ", currentViewColumn));
                } else {
                    ltEmitter.addToLogicTable((LTRecord) fcf.getCTC("0", currentViewColumn));
                }
                break;
            case INVALID:
                break;
            case SORTKEY:
                ViewSortKey sk = Repository.getViews().get(currentViewColumn.getViewId()).getViewSortKeyFromColumnId(currentViewColumn.getComponentId());
                DataType dtlDataType = ((LogicTableF2)lkEntry).getArg2().getFieldFormat();
                LogicTableF1 skc;
                if (dtlDataType == DataType.ALPHANUMERIC) {
                    skc = (LogicTableF1) fcf.getSKC(" ", currentViewColumn, sk);
                } else {
                    skc = (LogicTableF1) fcf.getSKC("0", currentViewColumn, sk);
                }
                skc.getArg().setFieldFormat(((LogicTableF2)lkEntry).getArg2().getFieldFormat());
                ltEmitter.addToLogicTable((LTRecord) skc);
                break;
            case SORTKEYTITLE:
                break;
            default:
                break;
        }
        ltgoto.setGotoRow1(ltEmitter.getNumberOfRecords());
        //Can now set the lkEmitter gotos
        lkEmitter.setFalseGotos(null);
    }

    @Override
    public DataType getDataType() {
        // Casting may set the format to be something we are not
        return overriddenDataType != DataType.INVALID ? overriddenDataType : ref.getDatatype();
   }

    @Override
    public DateCode getDateCode() {
        return (overriddenDateCode != null) ? overriddenDateCode : ref.getDateTimeFormat();
    }

    private void flipDataTypeIfFieldAlphanumeric(LogicTableArg arg, LogicTableArg arg2) {
        if(arg2.getFieldFormat() != DataType.ALPHANUMERIC && arg.getFieldFormat() == DataType.ALPHANUMERIC) {
            arg.setFieldFormat(DataType.ZONED);
        }
        if(arg2.getFieldFormat() == DataType.ALPHANUMERIC && arg.getFieldFormat() != DataType.ALPHANUMERIC) {
            arg2.setFieldFormat(DataType.ZONED);
        }
    }

    @Override
    public short getConcatinationEntry(ColumnAST col, ExtractBaseAST rhs, short start) {
        getAssignmentEntry(col, rhs);
        //now we need to fixup the col start and length
        //for both the assigned entry and the default
        //The last ltemitter entry will be the DTC
        //Last - 2 will be the DTL
        int numRecords = ltEmitter.getNumberOfRecords();
        LogicTableF1 dtc = (LogicTableF1) ltEmitter.getLogicTable().getFromPosition(numRecords -1);
        LogicTableF2 dtl = (LogicTableF2) ltEmitter.getLogicTable().getFromPosition(numRecords -3);
        fixupDtl(dtl, start);
        return fixupDtc(dtc, start, dtl.getArg1().getFieldLength());
    }

    private short fixupDtc(LogicTableF1 dtc, short start, short len) {
        LogicTableArg arg = dtc.getArg();
        arg.setStartPosition(start);
        arg.setFieldLength(len);
        return (short) len;
    }

    private short fixupDtl(LogicTableF2 dtl, short start) {
        LogicTableArg arg1 = ((LogicTableF2)dtl).getArg1();
        LogicTableArg arg2 = ((LogicTableF2)dtl).getArg2();
        arg2.setStartPosition(start);
        arg2.setFieldLength(arg1.getFieldLength());
        return arg1.getFieldLength();
    }

    @Override
    public short getLeftEntry(ColumnAST col, ExtractBaseAST rhs, short length) {
        getAssignmentEntry(col, rhs);
        int numRecords = ltEmitter.getNumberOfRecords();
        LogicTableF2 dtl = (LogicTableF2) ltEmitter.getLogicTable().getFromPosition(numRecords -1);
        LogicTableArg arg1 = ((LogicTableF2)dtl).getArg1();
        short fieldlen = arg1.getFieldLength();
        if(length < fieldlen) { 
            arg1.setFieldLength(length);
        } else {
            //Error 
        }
        return length;
    }

    @Override
    public short getRightEntry(ColumnAST col, ExtractBaseAST rhs, short length) {
        getAssignmentEntry(col, rhs);
        int numRecords = ltEmitter.getNumberOfRecords();
        LogicTableF2 dtl = (LogicTableF2) ltEmitter.getLogicTable().getFromPosition(numRecords -1);
        LogicTableArg arg1 = ((LogicTableF2)dtl).getArg1();
        short fieldlen = arg1.getFieldLength();
        if(length < fieldlen) { 
            arg1.setStartPosition((short)(arg1.getStartPosition() + fieldlen - length));
            arg1.setFieldLength(length);
        } else {
            //Error 
        }
        return length;
    }

    @Override
    public short getSubstreEntry(ColumnAST col, ExtractBaseAST rhs, short start, short length) {
        getAssignmentEntry(col, rhs);
        int numRecords = ltEmitter.getNumberOfRecords();
        LogicTableF2 dtl = (LogicTableF2) ltEmitter.getLogicTable().getFromPosition(numRecords -1);
        LogicTableArg arg1 = ((LogicTableF2)dtl).getArg1();
        short fieldlen = arg1.getFieldLength();
        if(length < fieldlen) { 
            arg1.setStartPosition((short)(arg1.getStartPosition() + start));
            arg1.setFieldLength(length);
        } else {
            //Error 
        }
        return length;
    }

    public void fixupGotos() {
        //
        goto2 = ltEmitter.getNumberOfRecords();
    }

    @Override
    public int getAssignableLength() {
        return ref.getLength();
    }

    @Override
    public String getMessageName() {
        return String.format("{%s.%s}",lookup.getName(),ref.getName());
    }

    @Override
    public int getMaxNumberOfDigits() {
        return RepoHelper.getMaxNumberOfDigitsForType(getDataType(), ref.getLength());
    }

    private void stripDatesIfSame(LogicTableF2 lte) {
        if(lte.getArg1().getFieldFormat() == lte.getArg2().getFieldFormat() && lte.getArg1().getFieldContentId() == lte.getArg2().getFieldContentId()) {
           lte.getArg1().setFieldContentId(DateCode.NONE);
           lte.getArg2().setFieldContentId(DateCode.NONE);
        }
    }
}
