package org.genevaers.compilers.extract.astnodes;

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


import org.genevaers.repository.components.enums.DateCode;
import org.genevaers.repository.components.enums.ExtractArea;

import java.math.BigDecimal;

import org.genevaers.genevaio.ltfactory.LtFactoryHolder;
import org.genevaers.genevaio.ltfactory.LtFuncCodeFactory;
import org.genevaers.genevaio.ltfile.LTFileObject;
import org.genevaers.genevaio.ltfile.LTRecord;
import org.genevaers.genevaio.ltfile.LogicTableArg;
import org.genevaers.genevaio.ltfile.LogicTableF1;
import org.genevaers.repository.Repository;
import org.genevaers.repository.components.ViewSortKey;
import org.genevaers.repository.components.enums.DataType;

public class NumAtomAST extends FormattedASTNode  implements GenevaERSValue, Assignable, CalculationSource{

    private float value;
    private String numStr;

    public NumAtomAST() {
        type = ASTFactory.Type.NUMATOM;
    }

    public void setValue(String numString) {
        BigDecimal strippedVal = new BigDecimal(numString).stripTrailingZeros();
        value = strippedVal.floatValue();
        numStr = strippedVal.toPlainString();
    }

    private String stripLeadingingZeros(String numString) {
        boolean notDone = true;
        int ndx = 0;
        int end = numString.length() - 1;
        int dpndx = numString.indexOf('.');
        if(dpndx > 0) {
            end = dpndx - 1;
        }
        while (notDone && ndx != end ) {
            if(numString.charAt(ndx) != '0') {
                notDone = false; 
            } else {
                ndx++;
            }
        }
        return numString.substring(ndx);
    }

    private String stripTrailingZeros(String numString) {
        int dpndx = numString.indexOf('.');
        if(dpndx > 0) {
            boolean allZeros = true;
            for(int i=dpndx+1; i<numString.length(); i++) {
                if(numString.charAt(i) != '0') {
                    allZeros = false;
                }
            }
            if(allZeros) {
                return numString.substring(0,dpndx);
            }
        }
        return numString;
    }

    public int getValue() {
        return Math.round(value);
    }

    @Override
    public String getValueString() {
        return numStr;
    }

    @Override
    public void setNegative() {
        numStr = "-" + numStr;
    }

    @Override
    public LTFileObject getAssignmentEntry(ColumnAST col, ExtractBaseAST rhs) {
        LtFuncCodeFactory fcf = LtFactoryHolder.getLtFunctionCodeFactory();
        if(col.getViewColumn().getExtractArea() == ExtractArea.AREACALC) {
            ltEmitter.addToLogicTable((LTRecord)fcf.getCTC(numStr, col.getViewColumn()));
        } else if(col.getViewColumn().getExtractArea() == ExtractArea.AREADATA) {
            ltEmitter.addToLogicTable((LTRecord)fcf.getDTC(numStr, col.getViewColumn()));
        } else {
            ViewSortKey sk = Repository.getViews().get(col.getViewColumn().getViewId()).getViewSortKeyFromColumnId(col.getViewColumn().getComponentId());
            LTRecord ltr = (LTRecord)fcf.getSKC(numStr, col.getViewColumn(), sk);
            ltEmitter.addToLogicTable(ltr);
        }
        return null;
    }

    @Override
    public LTFileObject emitSetFunctionCode() {
        LtFuncCodeFactory fcf = LtFactoryHolder.getLtFunctionCodeFactory();
        ltEmitter.addToLogicTable((LTRecord)fcf.getSETC(((CalculationAST)parent.getParent()).getAccName(), numStr));
        return null;
    }

    @Override
    public LTFileObject emitAddFunctionCode() {
        LtFuncCodeFactory fcf = LtFactoryHolder.getLtFunctionCodeFactory();
        ltEmitter.addToLogicTable((LTRecord)fcf.getADDC(((CalculationAST)parent.getParent()).getAccName(), numStr));
        return null;
    }

    @Override
    public LTFileObject emitSubFunctionCode() {
        LtFuncCodeFactory fcf = LtFactoryHolder.getLtFunctionCodeFactory();
        ltEmitter.addToLogicTable((LTRecord)fcf.getSUBC(((CalculationAST)parent.getParent()).getAccName(), numStr));
        return null;
    }

    @Override
    public LTFileObject emitMulFunctionCode() {
        LtFuncCodeFactory fcf = LtFactoryHolder.getLtFunctionCodeFactory();
        ltEmitter.addToLogicTable((LTRecord)fcf.getMULC(((CalculationAST)parent.getParent()).getAccName(), numStr));
        return null;
    }

    @Override
    public LTFileObject emitDivFunctionCode() {
        LtFuncCodeFactory fcf = LtFactoryHolder.getLtFunctionCodeFactory();
        ltEmitter.addToLogicTable((LTRecord)fcf.getDIVC(((CalculationAST)parent.getParent()).getAccName(), numStr));
        return null;
    }

    @Override
    public DataType getDataType() {
        return overriddenDataType != DataType.INVALID ? overriddenDataType : DataType.ZONED;
    }

    @Override
    public DateCode getDateCode() {
        return (overriddenDateCode != null) ? overriddenDateCode : DateCode.NONE;
    }

    @Override
    public String getMessageName() {
        return "numeric constant '" + numStr +"'";
    }

    @Override
    public int getAssignableLength() {
        return numStr.length();
    }

    @Override
    public int getMaxNumberOfDigits() {
        if(numStr.contains("."))
            return numStr.length() - 1;
        else 
            return numStr.length();
    }

}
