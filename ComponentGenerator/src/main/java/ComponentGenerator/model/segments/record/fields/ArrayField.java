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

public class ArrayField extends Field {

    private String arraySize;
    private String arrayType;

    public String getArraySize() {
        return arraySize;
    }

    public void setArraySize(String arraySize) {
        this.arraySize = arraySize;
    }

    public String getArrayType() {
        return arrayType;
    }

    public void setArrayType(String arrayType) {
        this.arrayType = arrayType;
    }

    @Override
    public String getImportString() {
        String imps = "java.util.ArrayList;\nimport java.util.List";
        return imps;
    }

    @Override
    public String getFieldEntry() {
        return INDENT + "private List<" + StringUtils.capitalize(arrayType) + ">" + NameUtils.getCamelCaseName(name, false) + "  = new ArrayList<>();";
    }

    @Override
    public String getGetAndSetEntry() {
        StringBuilder gasEntry = new StringBuilder();
        String ccName =NameUtils.getCamelCaseName(name, false);
        String ucName = StringUtils.capitalize(ccName);
        String retType = "List<" + StringUtils.capitalize(arrayType) + ">";
        gasEntry.append(DBLINDENT + "public " + retType + " get" + ucName + "() {\n");
        gasEntry.append(DBLINDENT + "    return " + ccName + ";\n    }\n\n");
        gasEntry.append(DBLINDENT + "public void set" + ucName + "(" + retType + " " + ccName + ") {\n");
        gasEntry.append(DBLINDENT + "    this." + ccName + " = " + ccName + ";\n    }\n");
        return gasEntry.toString();
    }

    @Override
    public String getReadEntry() {
        StringBuilder readEntry = new StringBuilder();
        String ccName = NameUtils.getCamelCaseName(name, false);
        readEntry.append(DBLINDENT + "for (int i = 0; i<" + arraySize + "; i += 1) {\n");
        if (arrayType.startsWith("VDP")) {
            readEntry.append(DBLINDENT + "    //Read the " + arrayType + "\n");
            readEntry.append(DBLINDENT + "    " + arrayType + " entry = new " + arrayType + "();\n");
            readEntry.append(DBLINDENT + "    entry.readRecord(reader, rec);\n");
            readEntry.append(DBLINDENT + "    " + ccName+".add(entry);\n");
        } else {
            if (arrayType.equalsIgnoreCase("short")) {
                readEntry.append(DBLINDENT + "    " + ccName + ".add(rec.bytes.getShort());\n");
            }
            else if (arrayType.equalsIgnoreCase("integer")) {
                readEntry.append(DBLINDENT + "    " + ccName + ".add(rec.bytes.getInt());\n");
            }
            else if (arrayType.equalsIgnoreCase("byte")) {
                readEntry.append(DBLINDENT + "    " + ccName + ".add(rec.bytes.get());\n");
            }
        }
        readEntry.append(DBLINDENT + "}");
        return readEntry.toString();
    }

    @Override
    public String getCsvEntry() {
        StringBuilder csvEntry = new StringBuilder();
        String ccName = NameUtils.getCamelCaseName(name, false);
        csvEntry.append(DBLINDENT + "for (int i = 0; i<" + arraySize + "; i += 1) {\n");
        if (arrayType.startsWith("VDP")) {
            csvEntry.append(DBLINDENT + "    " + ccName + ".get(i).writeCSV(fw);\n");
        } else {
            csvEntry.append(DBLINDENT + "    fw.write(" + ccName + ".get(i)+\",\");\n");
        }
        csvEntry.append(DBLINDENT + "}\n");
        return csvEntry.toString();
    }

    @Override
    public String getCsvHeaderEntry() {
        return defaultCsvEntry();
    }

    @Override
    public String getFillTheWriteBufferEntry() {
        StringBuilder fillEntry = new StringBuilder();
        String ccName = NameUtils.getCamelCaseName(name, false);
        fillEntry.append(DBLINDENT + "for (int i = 0; i<" + arraySize + "; i += 1) {\n");
        if (arrayType.startsWith("VDP")) {
            fillEntry.append(DBLINDENT + "    " + ccName + ".get(i).fillTheWriteBuffer(readerWriter);\n");
        } else {
            if (arrayType.equals("short")) {
                fillEntry.append(DBLINDENT + "   buffer.bytes.putShort(" + ccName + ".get(i));\n");
            } else if (arrayType.equals("integer")) {
                fillEntry.append(DBLINDENT + "    buffer.bytes.putInt(" + ccName + ".get(i));\n");
            } else if (arrayType.equals("byte")) {
                fillEntry.append(DBLINDENT + "    buffer.bytes.put(" + ccName + ".get(i));\n");
            }
        }
        fillEntry.append(DBLINDENT + "}\n");
        return fillEntry.toString();
    }

    @Override
    public String getDsectType() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getFieldNodeEntry(boolean prefix, boolean arrayValue) {
        StringBuilder fieldNodeEntry = new StringBuilder();
        String ccName = NameUtils.getCamelCaseName(name, false);
        if(name.equals("stack")) {
            generateStackDump(ccName, fieldNodeEntry);
        } else {
            fieldNodeEntry.append(DBLINDENT + "StringBuilder " + ccName+"Str = new StringBuilder();\n");
            fieldNodeEntry.append(DBLINDENT + "for (int i = 0; i<" + arraySize + "; i += 1) {\n");
            if (arrayType.startsWith("VDP")) {
                fieldNodeEntry.append(DBLINDENT + "    " + ccName + ".get(i).addRecordNodes(rn, i, compare);\n");
            } else {
                fieldNodeEntry.append(DBLINDENT + "    " + ccName+"Str" + ".append(" + ccName + ".get(i)+\",\");\n");
            }
            fieldNodeEntry.append(DBLINDENT + "}\n");
            fieldNodeEntry.append(DBLINDENT + "rn.add(new StringFieldNode(\"" + NameUtils.getCamelCaseName(name, false) + "\"," + ccName+"Str.toString()), compare);");  
        }
        return fieldNodeEntry.toString();
    }

    private void generateStackDump(String ccName, StringBuilder fieldNodeEntry) {
        fieldNodeEntry.append(DBLINDENT + "StringBuilder " + ccName+"Str = new StringBuilder();\n");
           fieldNodeEntry.append(DBLINDENT + "ByteBuffer newBuffer = ByteBuffer.allocate(stackLength);\n" + //
        "        for(int i = 0 ; i < stack.size() ; i++) {\r\n" + //
        "            newBuffer.put(stack.get(i));\r\n" + //
        "        }\r\n" + //
        "        CalcStack cs = new CalcStack(newBuffer, columnId, columnId);\r\n" + //
        "        cs.buildEntriesArrayFromTheBuffer();\r\n" + //
        "");
        fieldNodeEntry.append(DBLINDENT + "rn.add(new StringFieldNode(\"" + NameUtils.getCamelCaseName(ccName, false) + "\"," + "cs.toString()), compare);");  
    }

}
