package org.genevaers.compilers.extract.astnodes;

import org.genevaers.compilers.base.EmittableASTNode;
import org.genevaers.compilers.extract.astnodes.ASTFactory.Type;

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


import org.genevaers.genevaio.ltfactory.LtFactoryHolder;
import org.genevaers.genevaio.ltfile.LTFileObject;
import org.genevaers.genevaio.ltfile.LTRecord;
import org.genevaers.genevaio.ltfile.LogicTableNameF1;
import org.genevaers.genevaio.ltfile.LogicTableNameValue;
import org.genevaers.repository.components.enums.DataType;
import org.genevaers.repository.components.enums.DateCode;

public class RecordCountAST  extends FormattedASTNode implements Assignable, CalculationSource, EmittableASTNode{

    private String accName = "lRecordCount"; //Named like this for historical reasaon - change later
    private short accumulatorNumber = 1;
    private boolean emittable = true;

    public RecordCountAST() {
        type = ASTFactory.Type.RECORD_COUNT;
    }

    @Override
    public void emit() {
        if(emittable) {
            LogicTableNameValue addc = (LogicTableNameValue) LtFactoryHolder.getLtFunctionCodeFactory().getADDC(accName, "1");
            ltEmitter.addToLogicTable((LTRecord)addc);
            addc.setSuffixSeqNbr(accumulatorNumber); //set after writing to table to override column number
        }
    }

    @Override
    public LTFileObject getAssignmentEntry(ColumnAST col, ExtractBaseAST rhs) {
        LogicTableNameF1 rc = (LogicTableNameF1) LtFactoryHolder.getLtFunctionCodeFactory().getDTA(accName, col.getViewColumn());
        ltEmitter.addToLogicTable((LTRecord)rc);
        rc.setSourceSeqNbr(accumulatorNumber); //set after writing to table to override column number
        return null;
    }

    @Override
    public LTFileObject emitSetFunctionCode() {
        return null;
    }

    @Override
    public LTFileObject emitAddFunctionCode() {
        return null;
    }

    @Override
    public LTFileObject emitSubFunctionCode() {
        return null;
    }

    @Override
    public LTFileObject emitMulFunctionCode() {
        return null;
    }

    @Override
    public LTFileObject emitDivFunctionCode() {
        return null;
    }

    public String getAccName() {
        return accName;
    }

    public void setAccName(String accName) {
        this.accName = accName;
    }

    @Override
    public DataType getDataType() {
        return overriddenDataType != DataType.INVALID ? overriddenDataType : DataType.ZONED;
    }

    @Override
    public DateCode getDateCode() {
        return (overriddenDateCode != null) ? overriddenDateCode : DateCode.NONE;
    }

    public void setNotEmittable() {
        this.emittable = false;
    }

    @Override
    public String getMessageName() {
        return "record counter";
    }

    @Override
    public int getAssignableLength() {
        return 10;
    }

    @Override
    public int getMaxNumberOfDigits() {
        return 10;
    }

}
