package ComponentGenerator.model.segments.record.fields;

import org.apache.commons.lang3.StringUtils;

import ComponentGenerator.model.NameUtils;

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


public class CookieField extends Field {
    private static final String TYPE = "Cookie";

    @Override
    public String getFieldEntry() {
        String entry = defaultFieldEntryForType("Cookie");
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
        String ccName = NameUtils.getCamelCaseName(name, false);
        String ucName = StringUtils.capitalize(ccName);
        StringBuilder readEntry = new StringBuilder();
        readEntry.append(DBLINDENT + "set" + ucName + "(cookieReader(rec.bytes.getInt(), reader, rec));");
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
        StringBuilder fillEntry = new StringBuilder();
        fillEntry.append(DBLINDENT + "cookieWriter(" + ccName + ", readerWriter, buffer);");
        return fillEntry.toString();
    }

    @Override
    public String getDsectType() {
        return "";
    }

    @Override
    public String getFieldNodeEntry(boolean prefix, boolean arrayValue) {
        String ccName = NameUtils.getCamelCaseName(name, false);
        return  "rn.add(new StringFieldNode(\"value\", "+ ccName + ".getPrintString()), compare);";
    }
}
