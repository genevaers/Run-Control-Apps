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

public class EnumField extends Field {

    private String existingJavaType;
    private String defaultStr;

    public String getExistingJavaType() {
        return existingJavaType;
    }

    public void setExistingJavaType(String existingJavaType) {
        this.existingJavaType = existingJavaType;
    }

    public String getDefault() {
        // TODO Auto-generated method stub
        return super.getDefault();
    }

    public void setDefault(String def) {
        // TODO Auto-generated method stub
        super.setDefault(def);
    }

    @Override
    public String getImportString() {
        return existingJavaType;
    }

    @Override
    public String getFieldEntry() {
        String entry = defaultFieldEntryForType(existingJavaType.replace("org.genevaers.repository.components.enums.", ""));
        if(defaultStr != null && defaultStr.length() > 0) {
            entry += " = " + defaultStr;
        }
        entry += ";";
        return entry;
    }

    @Override
    public String getGetAndSetEntry() {
        return getAndSetEntryForType(existingJavaType.replace("org.genevaers.repository.components.enums.", ""));
    }

    @Override
    public String getReadEntry() {
        return DBLINDENT + "set" + NameUtils.getCamelCaseName(name, true) + "(" + existingJavaType.replaceAll("org.genevaers.repository.components.enums.","") + ".values()[rec.bytes.getInt()]);";
    }

    @Override
    public String getCsvEntry() {
        String ccName = NameUtils.getCamelCaseName(name, false);
        return DBLINDENT + "fw.write((" + ccName + ".value()+\",\"));";
    }

    @Override
    public String getCsvHeaderEntry() {
        return defaultCsvHeaderEntry();
    }

    @Override
    public String getFillTheWriteBufferEntry() {
        return DBLINDENT + "buffer.bytes.putInt(" + NameUtils.getCamelCaseName(name, false) + ".ordinal());";
    }

    @Override
    public String getDsectType() {
        // TODO Auto-generated method stub
        return "FL4";
    }

    @Override
    public String getFieldNodeEntry(boolean prefix, boolean arrayValue) {
        return  DBLINDENT + "rn.add(new StringFieldNode(\"" + NameUtils.getCamelCaseName(name, false) + "\"," + NameUtils.getCamelCaseName(name, false) +".toString()), compare);";  
    }

    @Override
    public String getType() {
        return "enum";
    }
}
