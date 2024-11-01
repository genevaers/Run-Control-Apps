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


public class RundateAST extends FormattedASTNode implements GenevaERSValue, Assignable{

    private String value = "RUNDAY";

    public RundateAST() {
        type = ASTFactory.Type.RUNDATE;
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

    @Override
    public String getValueString() {
        return value;
    }

    @Override
    public DataType getDataType() {
        //Note the Cookie thing comes into play here
        return overriddenDataType != DataType.INVALID ? overriddenDataType : DataType.ALPHANUMERIC;
    }

    private DateCode rawDateCode() {
        //or is it the 
        switch (value) {
        case "RUNDAY":
            return DateCode.CCYYMMDD;
        case "RUNMONTH":
            return DateCode.CYM;
        case "RUNYEAR":
            return DateCode.CCYY;
        default:
            return DateCode.NONE;
        }
    }

    public int getCookieCode() {
        switch (value) {
        case "RUNDAY":
            return Cookie.LTDateRunDay;
        case "RUNMONTH":
            return Cookie.LTDateRunMonth;
        case "RUNYEAR":
            return Cookie.LTDateRunYear;
        default:
            return Cookie.LTDateRunDay;
        }
    }

    public String getValueBinaryString() {
        int v = 0;
        UnaryInt ui = new UnaryInt();
        ui.setValue("0");
        if(getNumberOfChildren() > 0) {
            ui = (UnaryInt) getChildIterator().next(); //only one child
            v = Integer.parseInt(ui.getValue());
        }
        byte[] bytes = new byte[256];
        int length = Integer.BYTES;
        for (int i = 0; i < length; i++) {
            bytes[length - i - 1] = (byte) (v & 0xFF);
            v >>= 8;
        }
        String val = new String(bytes);
        return val;
    }

/*

uint16_t
RunDateASTNode::getLength() const
{
    switch (getType()) {
    case SAFRLexerTokenTypes::RUNDAY:
        return 8;
    case SAFRLexerTokenTypes::RUNMONTH:
        return 6;
    case SAFRLexerTokenTypes::RUNYEAR:
        return 4;
    }
    return 0;
}
 */

    @Override
    public DateCode getDateCode() {
        //Depends on value
        return (overriddenDateCode != null) ? overriddenDateCode : rawDateCode();
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
    }

    @Override
    public String getMessageName() {
        return value;
    }

    @Override
    public int getAssignableLength() {
       switch (value) {
        case "RUNDAY":
            return 8;
        case "RUNMONTH":
            return 6;
        case "RUNYEAR":
            return 4;
        default:
            return 0;
        }
    }

    @Override
    public int getMaxNumberOfDigits() {
        switch (value) {
            case "RUNDAY":
                return 8;
            case "RUNMONTH":
                return 6;
            case "RUNYEAR":
                return 4;
            default:
                return 0;
        }
    }

}
