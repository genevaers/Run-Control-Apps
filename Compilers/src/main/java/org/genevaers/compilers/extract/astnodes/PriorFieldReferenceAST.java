package org.genevaers.compilers.extract.astnodes;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.genevaers.genevaio.ltfactory.LtFactoryHolder;
import org.genevaers.genevaio.ltfactory.LtFuncCodeFactory;
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
import org.genevaers.genevaio.ltfile.LogicTableF1;
import org.genevaers.genevaio.ltfile.LogicTableF2;
import org.genevaers.genevaio.ltfile.LogicTableNameF1;
import org.genevaers.repository.components.LRField;
import org.genevaers.repository.components.ViewColumn;
import org.genevaers.repository.components.enums.DateCode;
import org.genevaers.repository.components.enums.ExtractArea;
import org.genevaers.repository.components.enums.DataType;

// The FormattedASTNode could/should be an inteface it implements
// Ditto the TypedASTNode
public class PriorFieldReferenceAST extends FieldReferenceAST{

    public PriorFieldReferenceAST() {
        type = ASTFactory.Type.PRIORLRFIELD;
    }

    public String getName() {
        if(ref != null) {
            return ref.getName();
        } else {
            return name;
        }
    }


    @Override
    public LTFileObject getAssignmentEntry(ColumnAST lhs, ExtractBaseAST rhs) {
        ViewColumn vc = ((ColumnAST)lhs).getViewColumn();
        LTFileObject ltEntry = null;
        LRField field = ((FieldReferenceAST)rhs).getRef();
        if(field != null) {
            ltEntry =((ColumnAST)lhs).getPriorFieldLtEntry(field);
            LogicTableArg arg;
            if(vc.getExtractArea() == ExtractArea.AREACALC) {
                arg = ((LogicTableF1)ltEntry).getArg();
            } else {
                arg = ((LogicTableF2)ltEntry).getArg1();
                LogicTableArg arg2 = ((LogicTableF2)ltEntry).getArg2();
                flipDataTypeIfFieldAlphanumeric(arg, arg2);
            }
            arg.setFieldFormat(getDataType());
            arg.setLogfileId(getLtEmitter().getFileId());        
            arg.setFieldContentId(getDateCode());
        }
        return ltEntry;
    }

    @Override
    public LTFileObject emitSetFunctionCode() {
        LtFuncCodeFactory fcf = LtFactoryHolder.getLtFunctionCodeFactory();
        LogicTableNameF1 setp = (LogicTableNameF1) fcf.getSETP("", ref);
        setp.getArg().setLogfileId(ltEmitter.getFileId());
        ltEmitter.addToLogicTable((LTRecord)setp);
        return setp;
    }

    @Override
    public LTFileObject emitAddFunctionCode() {
        LtFuncCodeFactory fcf = LtFactoryHolder.getLtFunctionCodeFactory();
        LogicTableNameF1 addp = (LogicTableNameF1) fcf.getADDP("", ref);
        addp.getArg().setLogfileId(ltEmitter.getFileId());
        ltEmitter.addToLogicTable((LTRecord)addp);
        return null;
    }

    @Override
    public LTFileObject emitSubFunctionCode() {
        LtFuncCodeFactory fcf = LtFactoryHolder.getLtFunctionCodeFactory();
        LogicTableNameF1 subp = (LogicTableNameF1) fcf.getSUBP("", ref);
        subp.getArg().setLogfileId(ltEmitter.getFileId());
        ltEmitter.addToLogicTable((LTRecord)subp);
        return null;
    }

    @Override
    public LTFileObject emitMulFunctionCode() {
        LtFuncCodeFactory fcf = LtFactoryHolder.getLtFunctionCodeFactory();
        LogicTableNameF1 mulp = (LogicTableNameF1) fcf.getMULP("", ref);
        mulp.getArg().setLogfileId(ltEmitter.getFileId());
        ltEmitter.addToLogicTable((LTRecord)mulp);
        return null;
    }

    @Override
    public LTFileObject emitDivFunctionCode() {
        LtFuncCodeFactory fcf = LtFactoryHolder.getLtFunctionCodeFactory();
        LogicTableNameF1 divp = (LogicTableNameF1) fcf.getDIVP("", ref);
        divp.getArg().setLogfileId(ltEmitter.getFileId());
        ltEmitter.addToLogicTable((LTRecord)divp);
        return null;
    }

    @Override
    public DataType getDataType() {
        // Casting may set the format to be something we are not
        if(ref != null) {
            return overriddenDataType != DataType.INVALID ? overriddenDataType : ref.getDatatype();
        } else {
            return overriddenDataType;
        }
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
    public String getMessageName() {
        return "PRIOR({" + ref.getName() + "})";
    }

}
