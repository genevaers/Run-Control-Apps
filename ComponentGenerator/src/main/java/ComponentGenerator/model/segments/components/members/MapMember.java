package ComponentGenerator.model.segments.components.members;

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

public class MapMember extends Member {

    private String key;
    private String keyType;
    private boolean sorted = false;
    private String values;
    private String valuesType;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getKeyType() {
        return keyType;
    }

    public void setKeyType(String keyType) {
        this.keyType = keyType;
    }

    public boolean isSorted() {
        return sorted;
    }

    public void setSorted(boolean sorted) {
        this.sorted = sorted;
    }

    public String getValues() {
        return values;
    }

    public void setValues(String values) {
        this.values = values;
    }

    public String getValuesType() {
        return valuesType;
    }

    public void setValuesType(String valuesType) {
        this.valuesType = valuesType;
    }

    @Override
    public String getFieldEntry() {
        if(sorted)
            if(keyType.equals("String")) {
                return "    private TreeMap<" + keyType + ", " + valuesType + "> " + name + " = new java.util.TreeMap<>(String.CASE_INSENSITIVE_ORDER);";
            } else {
                return "    private TreeMap<" + keyType + ", " + valuesType + "> " + name + " = new java.util.TreeMap<>();";
            }
        else
            return "    private HashMap<" + keyType + ", " + valuesType + "> " + name + " = new java.util.HashMap<>();";
    }

    @Override
    public String getGetAndSetEntry() {
        StringBuilder gasEntry = new StringBuilder();
        String ucName = StringUtils.capitalize(name);
        gasEntry.append("    public " + valuesType + " findFrom" + ucName + "(" + keyType + " key) {\n");
        gasEntry.append("        return " + name + ".get(key);\n    }\n\n");
    
        gasEntry.append("    public void addTo" + ucName + "(" + valuesType + " value) {\n");
        gasEntry.append("        " + name + ".put(value.get" + StringUtils.capitalize(key) + "(), value);\n    }\n\n");
    
        gasEntry.append("    public Iterator<" + valuesType + "> getIteratorFor" + ucName + "() {\n");
        gasEntry.append("        return " + name + ".values().iterator();\n    }\n\n");
    
        gasEntry.append("    public Collection<" + valuesType + "> getValuesOf" + ucName + "() {\n");
        gasEntry.append("        return " + name + ".values();\n    }\n");
        return gasEntry.toString();
    }

    @Override
    public String getImportString() {
        String imports;
        if(sorted)
            imports = "import java.util.TreeMap;";
        else
            imports = "import java.util.HashMap;";
        imports += "\nimport java.util.Collection;\nimport java.util.Iterator;";
        return imports;
    }

    @Override
    public String getType() {
        return "Map of " + valuesType;
    }
}
