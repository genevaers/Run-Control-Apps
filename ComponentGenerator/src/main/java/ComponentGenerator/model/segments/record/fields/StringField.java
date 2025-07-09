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

public class StringField extends Field {
    private static final String TYPE = "String";
    private int maxlength;

    public int getMaxlength() {
        return maxlength;
    }

    public void setMaxlength(int maxlength) {
        this.maxlength = maxlength;
    }

    @Override
    public String getFieldEntry() {
        String entry = defaultFieldEntryForType(TYPE);
        if(getComponentField().equals("none")) { 
            entry += " = \"\"";
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
        String uccName = NameUtils.getCamelCaseName(name, true);
        if (padding) {
            readEntry.append(DBLINDENT + "//Padding to ignore\n");
            readEntry.append(DBLINDENT + "rec.bytes.get(reader.getPaddingBuffer(), 0, " + maxlength + ");");
        } else {
            readEntry.append(DBLINDENT + "rec.bytes.get(reader.getCleanStringBuffer(" + maxlength + "), 0, " + maxlength + ");\n");
            readEntry.append(DBLINDENT + "set" + uccName + "(reader.convertStringIfNeeded(reader.getStringBuffer(), " + maxlength + ").trim());");
        }
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
        StringBuilder fillEntry = new StringBuilder();
        String ccName = NameUtils.getCamelCaseName(name, false);
        fillEntry.append(DBLINDENT + "buffer.bytes.put(readerWriter.convertOutputIfNeeded(" + ccName + "));\n");
        if (maxlength > 0) {
            fillEntry.append(DBLINDENT + "buffer.bytes.put(spaces.getBytes(), 0, (" + maxlength + " - " + ccName + ".length()));");
        }
        return fillEntry.toString();
    }

    @Override
    public String getDsectType() {
        return "CL" + maxlength;
    }

    @Override
    public String getFieldNodeEntry(boolean prefix, boolean arrayValue) {
        return defaultStringNodeEntry(prefix, arrayValue);
    }

    @Override
    public String getType() {
        return String.format("%s(%d)", TYPE, maxlength);
    }
}
