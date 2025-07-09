package org.genevaers.compilers.extract.astnodes;

import java.util.Iterator;

import org.genevaers.compilers.base.ASTBase;
import org.genevaers.compilers.base.EmittableASTNode;

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
import org.genevaers.repository.components.ViewColumnSource;
import org.genevaers.repository.components.enums.DataType;
import org.genevaers.repository.components.enums.DateCode;
import org.genevaers.repository.components.enums.ExtractArea;

public class CalculationAST  extends FormattedASTNode implements Assignable, CalculationSource, EmittableASTNode{   

    private String accName;
    private int accumulatorNumber;

    public CalculationAST() {
        type = ASTFactory.Type.CALCULATION;
        if(accName == null) {
            generateAccumulatorName();
        }
    }

    @Override
    public void emit() {
        //Accumulators were named in a tedious way.
        //The only requirement is that they be unique within their context.
        //In order for testing to compare the names with the previous CPP version 
        //we will persist with the silly names until we know we are generating things correctly
        //Then we can switch the names to be somthing more simple
        //String accName = LtFactoryHolder.getLtFunctionCodeFactory().generateAccumulatorName(viewSource, vc)
        
        // Need to recurse here without the name if our children are Calculation nodes
        checkAndRecursIfCalcluationChildrenFound();
//        if(accName == null) {
//            generateAccumulatorName();
        LtFactoryHolder.getLtFunctionCodeFactory().setAccumName(accName);
        ltEmitter.addToLogicTable((LTRecord)LtFactoryHolder.getLtFunctionCodeFactory().getDIMN());
//        }
        emitChildNodes();
    }

    private void checkAndRecursIfCalcluationChildrenFound() {
            Iterator<ASTBase> calcIterator = getChildIterator();
            ExtractBaseAST setterChild = (ExtractBaseAST) calcIterator.next().getChildIterator().next();
            ExtractBaseAST opChild = (ExtractBaseAST) calcIterator.next().getChildIterator().next();
            if(setterChild.getType() == ASTFactory.Type.CALCULATION) {
                ((CalculationAST)setterChild).emit();
            }
            if(opChild.getType() == ASTFactory.Type.CALCULATION) {
                ((CalculationAST)opChild).emit();
            }
    }

    @Override
    public LTFileObject getAssignmentEntry(ColumnAST col, ExtractBaseAST rhs) {
        
        if(col.getViewColumn().getExtractArea() == ExtractArea.AREACALC) {
            LogicTableNameF1 cta = (LogicTableNameF1) LtFactoryHolder.getLtFunctionCodeFactory().getCTA(accName, col.getViewColumn());
            cta.getArg().setLogfileId(ltEmitter.getFileId());
            ltEmitter.addToLogicTable((LTRecord)cta);
        } else if(col.getViewColumn().getExtractArea() == ExtractArea.AREADATA) {
            LogicTableNameF1 dta = (LogicTableNameF1) LtFactoryHolder.getLtFunctionCodeFactory().getDTA(accName, col.getViewColumn());
            dta.getArg().setLogfileId(ltEmitter.getFileId());
            ltEmitter.addToLogicTable((LTRecord)dta);
        } else {
            LogicTableNameF1 ska = (LogicTableNameF1) LtFactoryHolder.getLtFunctionCodeFactory().getSKA(accName, col.getViewColumn());
            ska.getArg().setLogfileId(ltEmitter.getFileId());
            ltEmitter.addToLogicTable((LTRecord)ska);
        }
        return null;
    }

    private void generateAccumulatorName() {
        if(accName == null) {
            if(currentViewColumn != null) {
                accName = LtFactoryHolder.getLtFunctionCodeFactory().generateAccumulatorName(currentViewSource, currentViewColumnSource.getColumnNumber());
            } else {
                accName = LtFactoryHolder.getLtFunctionCodeFactory().generateAccumulatorName(currentViewSource, 0);
            }
        }
    }

    @Override
    public LTFileObject emitSetFunctionCode() {
        //Need to make sure all our child nodes have been emitted first
        LTFileObject seta = LtFactoryHolder.getLtFunctionCodeFactory().getSETA(((CalculationAST)parent.getParent()).getAccName(), accName);
        ltEmitter.addToLogicTable((LTRecord)seta);
        return null;
    }

    @Override
    public LTFileObject emitAddFunctionCode() {
        LTFileObject adda = LtFactoryHolder.getLtFunctionCodeFactory().getADDA(((CalculationAST)parent.getParent()).getAccName(), accName);
        ltEmitter.addToLogicTable((LTRecord)adda);
        return null;
    }

    @Override
    public LTFileObject emitSubFunctionCode() {
        LTFileObject suba = LtFactoryHolder.getLtFunctionCodeFactory().getSUBA(((CalculationAST)parent.getParent()).getAccName(), accName);
        ltEmitter.addToLogicTable((LTRecord)suba);
        return null;
    }

    @Override
    public LTFileObject emitMulFunctionCode() {
        LTFileObject mula = LtFactoryHolder.getLtFunctionCodeFactory().getMULA(((CalculationAST)parent.getParent()).getAccName(), accName);
        ltEmitter.addToLogicTable((LTRecord)mula);
        return null;
    }

    @Override
    public LTFileObject emitDivFunctionCode() {
        LTFileObject diva = LtFactoryHolder.getLtFunctionCodeFactory().getDIVA(((CalculationAST)parent.getParent()).getAccName(), accName);
        ltEmitter.addToLogicTable((LTRecord)diva);
        return null;
    }

    public String getAccName() {
        return accName;
    }

    public void setAccName(String accName) {
        this.accName = accName;
    }

    public void setAccumulatorNumber(int accumulaorNumber) {
        this.accumulatorNumber = accumulaorNumber;
    }

    public int getAccumulatorNumber() {
        return accumulatorNumber;
    }

    @Override
    public DataType getDataType() {
        return overriddenDataType != DataType.INVALID ? overriddenDataType : DataType.GENEVANUMBER;
    }

    @Override
    public DateCode getDateCode() {
        return (overriddenDateCode != null) ? overriddenDateCode : DateCode.NONE;
    }

    @Override
    public String getMessageName() {
        return "calculation";
    }

    @Override
    public int getAssignableLength() {
        return 31;
    }

    @Override
    public int getMaxNumberOfDigits() {
        return 0; //An accumulator can always potentially overvlow - need to filter out.
    }

}
