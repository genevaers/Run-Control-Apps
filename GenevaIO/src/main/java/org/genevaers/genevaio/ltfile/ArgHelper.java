package org.genevaers.genevaio.ltfile;

import java.lang.reflect.Array;

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


import org.apache.commons.lang3.StringUtils;
import org.genevaers.repository.components.LookupPathKey;
import org.genevaers.repository.components.enums.DataType;
import org.genevaers.repository.components.enums.DateCode;
import org.genevaers.repository.components.enums.JustifyId;


public class ArgHelper {
    
    private ArgHelper() { }

    public static void setArgValueFrom(LogicTableArg arg, String valStr) {
        arg.setValue(new Cookie(valStr));
    }

    public static void setValBuffer(LogicTableArg arg, byte[] in) {
        arg.setValue(new Cookie(new String(in)));
    }

    public static LogicTableArg makeDefaultArg() {
        LogicTableArg arg = new LogicTableArg();
        arg.setDecimalCount((short)0);
        arg.setFieldContentId(DateCode.NONE);
        arg.setFieldFormat(DataType.INVALID);
        arg.setLrId(0);
        arg.setFieldId(0);
        arg.setStartPosition((short)0);
        arg.setFieldLength((short)0);
        arg.setJustifyId(JustifyId.NONE);
        arg.setSignedInd(false);
        arg.setValue(new Cookie("0"));
        arg.setPadding2("");
        return arg;
    }

    public static void populateArgFromKeyTarget(LogicTableArg arg, LookupPathKey key) {
        arg.setDecimalCount(key.getDecimalCount());
        if(key.getDateTimeFormat() == null)
            arg.setFieldContentId(DateCode.NONE);
        else
            arg.setFieldContentId(key.getDateTimeFormat());
        arg.setFieldFormat(key.getDatatype());
        arg.setFieldId(key.getFieldId());
        arg.setLogfileId(key.getTargetlfid());
        arg.setLrId(key.getSourceLrId());
        arg.setSignedInd(key.isSigned());
        arg.setStartPosition(key.getStartPosition());
        arg.setFieldLength(key.getFieldLength());
        arg.setJustifyId(key.getJustification());
        //Should check if the key value is compatible with the field data type
        //if all spaces correct to a 0 for numeric and generate a warning
        if(key.getValue().startsWith(" ") && key.getDatatype() != DataType.ALPHANUMERIC) {
            arg.setValue(new Cookie("0"));
        } else {
            //Strip leading 0s for numeric data types
            //This should be a more general function
            String kv = key.getValue();
            if(key.getDatatype() != DataType.ALPHANUMERIC && kv.startsWith("0")) {
                String stripped = StringUtils.stripStart(key.getValue(), "0");
                if(stripped.length() == 0) {
                    arg.setValue(new Cookie("0"));
                } else {
                    arg.setValue(new Cookie(stripped));
                }
            } else {
                arg.setValue(new Cookie(key.getValue()));
            }
        }
        arg.setPadding2("");  //This seems a little silly
    }

}
