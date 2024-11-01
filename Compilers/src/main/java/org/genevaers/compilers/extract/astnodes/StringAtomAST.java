package org.genevaers.compilers.extract.astnodes;

import org.genevaers.genevaio.ltfactory.LtFactoryHolder;
import org.genevaers.genevaio.ltfactory.LtFuncCodeFactory;
import org.genevaers.genevaio.ltfile.Cookie;
import org.genevaers.genevaio.ltfile.LTFileObject;
import org.genevaers.genevaio.ltfile.LTRecord;
import org.genevaers.genevaio.ltfile.LogicTableArg;
import org.genevaers.genevaio.ltfile.LogicTableF1;
import org.genevaers.repository.Repository;
import org.genevaers.repository.components.ViewColumn;
import org.genevaers.repository.components.ViewSortKey;
import org.genevaers.repository.components.enums.DataType;
import org.genevaers.repository.components.enums.DateCode;
import org.genevaers.repository.components.enums.ExtractArea;


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


public class StringAtomAST extends FormattedASTNode implements GenevaERSValue, Assignable, Concatable {

    protected String value = "";

    public StringAtomAST() {
        type = ASTFactory.Type.STRINGATOM;
    }

    public void setValue(String value) {
        this.value = value.replace("\"", "");
    }

    public String getValue() {
        return value;
    }

    @Override
    public String getValueString() {
        return value;
    }

    @Override
    public LTFileObject getAssignmentEntry(ColumnAST lhs, ExtractBaseAST rhs) {
        LtFuncCodeFactory fcf = LtFactoryHolder.getLtFunctionCodeFactory();
        LTRecord ltr = null;
        ViewColumn vc = lhs.getViewColumn();
        if(vc.getExtractArea() == ExtractArea.AREACALC) {
            ltr = (LTRecord)fcf.getCTC(value, lhs.getViewColumn());
        } else if(vc.getExtractArea() == ExtractArea.AREADATA) {
            if(lhs.getViewColumn().getFieldLength() > 0) {
                LogicTableF1 dtc = (LogicTableF1) fcf.getDTC(value, lhs.getViewColumn());
                dtc.getArg().setFieldContentId(DateCode.NONE);
                ltr = (LTRecord)dtc;
            }
        } else {
            ViewSortKey sk = Repository.getViews().get(lhs.getViewColumn().getViewId()).getViewSortKeyFromColumnId(lhs.getViewColumn().getComponentId());
            ltr = (LTRecord)fcf.getSKC(value, lhs.getViewColumn(), sk);
        }
        if(ltr != null) {
            ltr.setSourceSeqNbr((short) (ltEmitter.getLogicTable().getNumberOfRecords()));
        }
        return (LTFileObject) ltr;
    }

    @Override
    public DataType getDataType() {
        return overriddenDataType != DataType.INVALID ? overriddenDataType : DataType.ALPHANUMERIC;
    }

    @Override
    public DateCode getDateCode() {
        return (overriddenDateCode != null) ? overriddenDateCode : DateCode.NONE;
    }

    @Override
    public short getConcatinationEntry(ColumnAST col, ExtractBaseAST rhs, short start) {
        LogicTableF1 f1 = (LogicTableF1) getAssignmentEntry(col, rhs);
        LogicTableArg arg = f1.getArg();
        arg.setStartPosition(start);
        arg.setFieldLength((short)arg.getValue().length());
        ltEmitter.addToLogicTable(f1);
        return (short) arg.getValue().length();
    }

    @Override
    public short getLeftEntry(ColumnAST col, ExtractBaseAST rhs, short length) {
        LogicTableF1 f1 = (LogicTableF1) getAssignmentEntry(col, rhs);
        LogicTableArg arg = f1.getArg();
        //This is different in that we need to get the original string value and change it?
        int fieldlen = arg.getValue().length();
        if(length < fieldlen) { 
            String val = ((StringAtomAST)rhs).getValue();
            arg.setValue(new Cookie(val.substring(0, length)));
            ltEmitter.addToLogicTable((LTRecord)f1);
        } else {
            //Error 
        }
        return length;
    }

    @Override
    public short getRightEntry(ColumnAST col, ExtractBaseAST rhs, short length) {
        LogicTableF1 f1 = (LogicTableF1) getAssignmentEntry(col, rhs);
        LogicTableArg arg = f1.getArg();
        //This is different in that we need to get the original string value and change it?
        int fieldlen = arg.getValue().length();
        if(length < fieldlen) { 
            String val = ((StringAtomAST)rhs).getValue();
            arg.setValue(new Cookie(val.substring(fieldlen-length, fieldlen)));
            ltEmitter.addToLogicTable((LTRecord)f1);
        } else {
            //Error 
        }
        return length;
    }

    @Override
    public short getSubstreEntry(ColumnAST col, ExtractBaseAST rhs, short start, short length) {
        LogicTableF1 f1 = (LogicTableF1) getAssignmentEntry(col, rhs);
        LogicTableArg arg = f1.getArg();
        //This is different in that we need to get the original string value and change it?
        //arg.setStartPosition(start);
        int fieldlen = arg.getValue().length();
        if(length < fieldlen) { 
            String val = ((StringAtomAST)rhs).getValue();
            arg.setValue(new Cookie(val.substring(start, start+length)));
            ltEmitter.addToLogicTable((LTRecord)f1);
        } else {
            //Error 
        }
        return length;
    }

    @Override
    public String getMessageName() {
        return "const string '" + value + "'";
    }

    @Override
    public int getAssignableLength() {
        return value.length();
    }

    @Override
    public int getMaxNumberOfDigits() {
        return value.length();
    }

}
