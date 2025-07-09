package ComponentGenerator.model.segments.record.fields;

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


import ComponentGenerator.model.NameUtils;

public class ShortField extends Field {

    private static final String TYPE = "short";

    @Override
    public String getImportString() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getFieldEntry() {
        String entry = defaultFieldEntryForType(TYPE);
        if(getDefault() != null) {
            entry += " = " + getDefault();
        } else if(getComponentField().equals("none")) { 
            entry += " = 0";
        }
        entry += ";";
        return entry;
    }

    @Override
    public String getGetAndSetEntry() {
        return getAndSetEntryForType(TYPE);
    }

    @Override
    public String getReadEntry() {
        if (NameUtils.getCamelCaseName(name, false).equals("recLen")) {
            return DBLINDENT + "setRecLen(rec.length);";
        } else {
            return DBLINDENT + "set" + NameUtils.getCamelCaseName(name, true) + "(rec.bytes.getShort());";
        }
    }

    @Override
    public String getCsvEntry() {
        return defaultCsvEntry();
    }

    @Override
    public String getCsvHeaderEntry() {
        return defaultCsvHeaderEntry();
    }

    @Override
    public String getFillTheWriteBufferEntry() {
        return DBLINDENT + "buffer.bytes.putShort(" + NameUtils.getCamelCaseName(name, false) + ");";
    }

    @Override
    public String getDsectType() {
        return "HL2";
    }

    @Override
    public String getFieldNodeEntry(boolean prefix, boolean arrayValue) {
        return defaultNumericNodeEntry(prefix, arrayValue);
    }  

    @Override
    public String getType() {
        return TYPE;
    }

}
