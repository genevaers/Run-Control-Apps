package org.genevaers.compilers.extract.astnodes;

import org.genevaers.genevaio.ltfactory.LtFactoryHolder;
import org.genevaers.genevaio.ltfactory.LtFuncCodeFactory;
import org.genevaers.genevaio.ltfile.LTFileObject;
import org.genevaers.genevaio.ltfile.LTRecord;
import org.genevaers.genevaio.ltfile.LogicTableArg;
import org.genevaers.genevaio.ltfile.LogicTableF2;
import org.genevaers.genevaio.ltfile.LogicTableNameF1;
import org.genevaers.repository.RepoHelper;
import org.genevaers.repository.Repository;
import org.genevaers.repository.components.ViewColumn;
import org.genevaers.repository.components.ViewSortKey;
import org.genevaers.repository.components.enums.DataType;
import org.genevaers.repository.components.enums.DateCode;
import org.genevaers.repository.components.enums.ExtractArea;
import org.genevaers.repository.components.enums.JustifyId;

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

public class ColumnRefAST extends FormattedASTNode implements CalculationSource, Assignable, Concatable {

    private ViewColumn vc;

    public ColumnRefAST() {
        type = ASTFactory.Type.COLUMNREF;
    }

     @Override
    public LTFileObject emitSetFunctionCode() {
        LtFuncCodeFactory fcf = LtFactoryHolder.getLtFunctionCodeFactory();
        LogicTableNameF1 setx = (LogicTableNameF1) fcf.getSETX("", vc);
        setx.getArg().setFieldId(vc.getColumnNumber());
        setx.getArg().setLogfileId(vc.getExtractArea().ordinal());
        ltEmitter.addToLogicTable((LTRecord)setx);
        return null;
    }

    @Override
    public LTFileObject emitAddFunctionCode() {
        LtFuncCodeFactory fcf = LtFactoryHolder.getLtFunctionCodeFactory();
        LogicTableNameF1 addx = (LogicTableNameF1) fcf.getADDX("", vc);
        addx.getArg().setFieldId(vc.getColumnNumber());
        addx.getArg().setLogfileId(vc.getExtractArea().ordinal());
        ltEmitter.addToLogicTable((LTRecord)addx);
        return null;
    }

    @Override
    public LTFileObject emitSubFunctionCode() {
        LtFuncCodeFactory fcf = LtFactoryHolder.getLtFunctionCodeFactory();
        LogicTableNameF1 subx = (LogicTableNameF1) fcf.getSUBX("", vc);
        subx.getArg().setFieldId(vc.getColumnNumber());
        subx.getArg().setLogfileId(vc.getExtractArea().ordinal());
        ltEmitter.addToLogicTable((LTRecord)subx);
        return null;
    }

    @Override
    public LTFileObject emitMulFunctionCode() {
        LtFuncCodeFactory fcf = LtFactoryHolder.getLtFunctionCodeFactory();
        LogicTableNameF1 mulx = (LogicTableNameF1) fcf.getMULX("", vc);
        mulx.getArg().setFieldId(vc.getColumnNumber());
        mulx.getArg().setLogfileId(vc.getExtractArea().ordinal());
        ltEmitter.addToLogicTable((LTRecord)mulx);
        return null;
    }

    @Override
    public LTFileObject emitDivFunctionCode() {
        LtFuncCodeFactory fcf = LtFactoryHolder.getLtFunctionCodeFactory();
        LogicTableNameF1 divx = (LogicTableNameF1) fcf.getDIVX("", vc);
        divx.getArg().setFieldId(vc.getColumnNumber());
        divx.getArg().setLogfileId(vc.getExtractArea().ordinal());
        ltEmitter.addToLogicTable((LTRecord)divx);
        return null;
    }

    @Override
    public LTFileObject getAssignmentEntry(ColumnAST lhs, ExtractBaseAST rhs) {
        LtFuncCodeFactory fcf = LtFactoryHolder.getLtFunctionCodeFactory();
        if(currentViewColumn.getExtractArea() == ExtractArea.AREACALC) {
            ltEmitter.addToLogicTable((LTRecord)fcf.getCTX(vc, currentViewColumn));
        } else if(currentViewColumn.getExtractArea() == ExtractArea.AREADATA) {
            LogicTableF2 dtx = (LogicTableF2) fcf.getDTX(vc, currentViewColumn);
            ltEmitter.addToLogicTable((LTRecord)dtx);
            ColumnRefAST cr = (ColumnRefAST) rhs;
            LogicTableArg arg1 = ((LogicTableF2)dtx).getArg1();
            if(cr.getViewColumn().getExtractArea() == ExtractArea.AREACALC) {
                short sp = (short) ((vc.getExtractAreaPosition() == 1 ? 0 :  vc.getExtractAreaPosition()) + 2);
                arg1.setStartPosition(sp);
                arg1.setFieldFormat(DataType.PACKED);
                arg1.setDecimalCount((short)8); //Realy depends on number fromat
                arg1.setJustifyId(JustifyId.LEFT);
            }
        } else {
            ViewSortKey sk = Repository.getViews().get(currentViewColumn.getViewId()).getViewSortKeyFromColumnId(currentViewColumn.getComponentId());
            ltEmitter.addToLogicTable((LTRecord)fcf.getSKX(vc, currentViewColumn, sk));
        }
        return null;
    }

    public void setViewColumn(ViewColumn c) {
        vc = c;
    }

    public ViewColumn getViewColumn() {
        return vc;
    }

    @Override
    public DataType getDataType() {
        return vc.getDataType();
    }

    @Override
    public DateCode getDateCode() {
        return vc.getDateCode();
    }

    @Override
    public short getConcatinationEntry(ColumnAST col, ExtractBaseAST rhs, short start) {
        getAssignmentEntry(col, rhs);
        int numRecords = ltEmitter.getNumberOfRecords();
        LogicTableF2 dtx = (LogicTableF2) ltEmitter.getLogicTable().getFromPosition(numRecords -1);
        LogicTableArg arg1 = ((LogicTableF2)dtx).getArg1();
        LogicTableArg arg2 = ((LogicTableF2)dtx).getArg2();
        arg2.setStartPosition(start);
        arg2.setFieldLength(((ColumnRefAST)rhs).getViewColumn().getFieldLength());
        
        return ((ColumnRefAST)rhs).getViewColumn().getFieldLength();
    }

    @Override
    public short getLeftEntry(ColumnAST col, ExtractBaseAST rhs, short length) {
        getAssignmentEntry(col, rhs);
        int numRecords = ltEmitter.getNumberOfRecords();
        LogicTableF2 dtx = (LogicTableF2) ltEmitter.getLogicTable().getFromPosition(numRecords -1);
        LogicTableArg arg1 = ((LogicTableF2)dtx).getArg1();
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
        LogicTableF2 dtx = (LogicTableF2) ltEmitter.getLogicTable().getFromPosition(numRecords -1);
        LogicTableArg arg1 = ((LogicTableF2)dtx).getArg1();
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
        LogicTableF2 dtx = (LogicTableF2) ltEmitter.getLogicTable().getFromPosition(numRecords -1);
        LogicTableArg arg1 = ((LogicTableF2)dtx).getArg1();
        short fieldlen = arg1.getFieldLength();
        if(length < fieldlen) { 
            arg1.setStartPosition((short)(arg1.getStartPosition() + start));
            arg1.setFieldLength(length);
        } else {
            //Error 
        }
        return length;
    }

    @Override
    public String getMessageName() {
        return "COL." + vc.getColumnNumber();
    }

    @Override
    public int getAssignableLength() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getFormattedLength'");
    }

    @Override
    public int getMaxNumberOfDigits() {
         return RepoHelper.getMaxNumberOfDigitsForType(vc.getDataType(), vc.getFieldLength());
    }

}
