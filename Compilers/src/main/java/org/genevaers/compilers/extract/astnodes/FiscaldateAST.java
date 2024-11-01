package org.genevaers.compilers.extract.astnodes;

import org.genevaers.genevaio.ltfactory.LtFactoryHolder;
import org.genevaers.genevaio.ltfactory.LtFuncCodeFactory;
import org.genevaers.genevaio.ltfile.Cookie;
import org.genevaers.genevaio.ltfile.LTFileObject;
import org.genevaers.genevaio.ltfile.LTRecord;
import org.genevaers.genevaio.ltfile.LogicTableArg;
import org.genevaers.genevaio.ltfile.LogicTableF1;
import org.genevaers.repository.Repository;
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


public class FiscaldateAST extends FormattedASTNode implements GenevaERSValue, Assignable{

    private String value;

    public FiscaldateAST() {
        type = ASTFactory.Type.FISCALDATE;
    }

    public void setValue(String value) {
        this.value = value.replace("\"", "");
    }

    public String getValue() {
        UnaryInt ui = new UnaryInt();
        ui.setValue("0");
        if(getNumberOfChildren() > 0) {
            ui = (UnaryInt) getChildIterator().next(); //only one child
        }
        return ui.getValue();
    }

    public int getCookieCode() {
        switch (value) {
        case "FISCALDAY":
            return Cookie.LTDateFiscalDay;
        case "FISCALMONTH":
            return Cookie.LTDateFiscalMonth;
        case "FISCALYEAR":
            return Cookie.LTDateFiscalYear;
        default:
            return Cookie.LTDateFiscalDay;
        }
    }

    private DateCode rawDateCode() {
        //or is it the 
        switch (value) {
        case "FISCALDAY":
            return DateCode.CCYYMMDD;
        case "FISCALMONTH":
            return DateCode.CYM;
        case "FISCALYEAR":
            return DateCode.CCYY;
        default:
            return DateCode.NONE;
        }
    }

    @Override
    public String getValueString() {
        return value;
    }

    @Override
    public DataType getDataType() {
        return overriddenDataType != DataType.INVALID ? overriddenDataType : DataType.ALPHANUMERIC;
    }

    @Override
    public DateCode getDateCode() {
        return (overriddenDateCode != null) ? overriddenDateCode : rawDateCode();
    }

    @Override
    public String getMessageName() {
        return "fiscal date";
    }

    @Override
    public int getAssignableLength() {
        return getMaxNumberOfDigits();
    }

    @Override
    public LTFileObject getAssignmentEntry(ColumnAST col, ExtractBaseAST rhs) {
        LtFuncCodeFactory fcf = LtFactoryHolder.getLtFunctionCodeFactory();
        LTRecord fc;
        if(currentViewColumn.getExtractArea() == ExtractArea.AREACALC) {
            fc = (LTRecord) fcf.getCTC(String.valueOf(value), currentViewColumn);
        } else if(currentViewColumn.getExtractArea() == ExtractArea.AREADATA) {
            fc = (LTRecord) fcf.getDTC(String.valueOf(value), currentViewColumn);
        } else {
            ViewSortKey sk = Repository.getViews().get(currentViewColumn.getViewId()).getViewSortKeyFromColumnId(currentViewColumn.getComponentId());
             fc = (LTRecord) fcf.getSKC(String.valueOf(value), currentViewColumn, sk);
        }
        expandArgCookieValue((LogicTableF1)fc);
        fc.setSourceSeqNbr((short) (ltEmitter.getLogicTable().getNumberOfRecords() + 1));
        ltEmitter.addToLogicTable((LTRecord)fc);
        return null;
    }

     private void expandArgCookieValue(LogicTableF1 f) {
        LogicTableArg arg = f.getArg();
        arg.setValue(new Cookie(getCookieCode(), getValue()));
        arg.setFieldContentId(rawDateCode());
    }

    @Override
    public int getMaxNumberOfDigits() {
        switch (value) {
            case "FISCALDAY":
                return 8;
            case "FISCALMONTH":
                return 6;
            case "FISCALYEAR":
                return 6;
            default:
                return 0;
        }
    }

}
