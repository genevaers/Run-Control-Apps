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


public class InternalField extends Field {

    private String existingJavaType;

    public String getExistingJavaType() {
        return existingJavaType;
    }

    public void setExistingJavaType(String existingJavaType) {
        this.existingJavaType = existingJavaType;
    }

    @Override
    public String getImportString() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getFieldEntry() {
        String entry = defaultFieldEntryForType(existingJavaType.replace("org.genevaers.repository.components.", ""));
        entry += ";";
        return entry;
    }

    @Override
    public String getGetAndSetEntry() {
        return getAndSetEntryForType(existingJavaType.replace("org.genevaers.repository.components.", ""));
    }

    @Override
    public String getReadEntry() {
        String readEntry =  DBLINDENT + name + " = new " + existingJavaType + "();\n";
        readEntry += DBLINDENT + name + ".readRecord(reader, rec);";
        return readEntry;
    }

    @Override
    public String getCsvEntry() {
        return DBLINDENT + name + ".writeCSV(fw);";
    }

    @Override
    public String getCsvHeaderEntry() {
        return DBLINDENT + name + ".writeCSVHeader(fw);";
    }

    @Override
    public String getFillTheWriteBufferEntry() {
        return DBLINDENT + name + ".fillTheWriteBuffer(readerWriter);";
    }

    @Override
    public String getDsectType() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getFieldNodeEntry(boolean prefix, boolean arrayValue) {
        StringBuilder strB = new StringBuilder();
        strB.append(DBLINDENT + "RecordPartNode " + name + "n = new RecordPartNode();\n");
        strB.append(DBLINDENT + name +"n.setName(recordType + \"_\" + rowNbr + \"_" + name + "\");\n");
        strB.append(DBLINDENT + name +"n = (RecordPartNode) rn.add(" + name +"n, compare);\n");
        strB.append(DBLINDENT + name + ".addRecordNodes(" + name + "n, compare);\n");
        return strB.toString();
    }

    @Override
    public String getType() {
        return existingJavaType;
    }
}
