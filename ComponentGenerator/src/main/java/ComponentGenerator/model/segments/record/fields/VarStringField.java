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


import org.apache.commons.lang3.StringUtils;

import ComponentGenerator.model.NameUtils;

/*
 * Can this be just the same as a StringField?
 */
public class VarStringField extends Field {

    private static final String TYPE = "String";
    private String sizeFrom;

    public String getSizeFrom() {
        return sizeFrom;
    }

    public void setSizeFrom(String sizeFrom) {
        this.sizeFrom = sizeFrom;
    }

    @Override
    public String getFieldEntry() {
        String entry = defaultFieldEntryForType(TYPE);
        if(getComponentField().equals("none")) { 
            entry += " = \"\";";
        }
        entry += ";";
        return entry;
    }

    @Override
    public String getGetAndSetEntry() {
        return getAndSetEntryForType(TYPE);
    }

    @Override
    public String getImportString() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getReadEntry() {
        StringBuilder readEntry = new StringBuilder();
        String ccName = NameUtils.getCamelCaseName(name, false);
        String ucName = StringUtils.capitalize(ccName);
        readEntry.append(DBLINDENT + "rec.bytes.get(reader.getCleanStringBuffer("+ sizeFrom + "), 0, " + sizeFrom + ");\n");
        readEntry.append(DBLINDENT + "set" + ucName + "(reader.convertStringIfNeeded(reader.getStringBuffer(), " + sizeFrom + ").trim());");
        return readEntry.toString();
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
        String ccName = NameUtils.getCamelCaseName(name, false);
        return DBLINDENT + "buffer.bytes.put(readerWriter.convertOutputIfNeeded(" + ccName + "));\n";
    }

    @Override
    public String getDsectType() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getFieldNodeEntry(boolean prefix, boolean arrayValue) {
        return defaultStringNodeEntry(prefix, arrayValue);    
    }

    @Override
    public String getType() {
        return "varstring";
    }
}
