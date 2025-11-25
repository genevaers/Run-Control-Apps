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

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;

import ComponentGenerator.model.NameUtils;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = As.PROPERTY, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = IntegerField.class, name = "integer"),
        @JsonSubTypes.Type(value = ShortField.class, name = "short"),
        @JsonSubTypes.Type(value = StringField.class, name = "string"),
        @JsonSubTypes.Type(value = CookieField.class, name = "cookie"),
        @JsonSubTypes.Type(value = VarStringField.class, name = "varstring"),
        @JsonSubTypes.Type(value = EnumField.class, name = "enum"),
        @JsonSubTypes.Type(value = BooleanField.class, name = "boolean"),
        @JsonSubTypes.Type(value = InternalField.class, name = "internal"),
        @JsonSubTypes.Type(value = ByteField.class, name = "byte"),
        @JsonSubTypes.Type(value = ArrayField.class, name = "array"),
        @JsonSubTypes.Type(value = MappingOnlyField.class, name = "mappingOnly"),
        @JsonSubTypes.Type(value = DsectOperandField.class, name = "dsect_operand")
})

public abstract class Field implements FieldGenerator {
    protected String name;
    private String type;
    private String description;
    protected boolean padding = false;
    private String dsectName;
    protected String componentField;
    private String def;
    private String displayName;

    public String getDisplayName() {
        return displayName != null ? displayName : componentField != null 
        ? StringUtils.capitalize(componentField) : NameUtils.getCamelCaseName(name, true);
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public static final String INDENT = "    ";
    public static final String DBLINDENT = "        ";

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isPadding() {
        return padding;
    }

    public void setPadding(boolean padding) {
        this.padding = padding;
    }

    public String getDsectName() {
        return dsectName != null ? dsectName : name;
    }

    public void setDsectName(String dsectName) {
        this.dsectName = dsectName;
    }

    public String getComponentField() {
        return componentField != null ? componentField : "none";
    }

    public String getRawComponentField() {
        return componentField;
    }

    public void setComponentField(String componentField) {
        this.componentField = componentField;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDefault() {
        return def;
    }

    public void setDefault(String def) {
        this.def = def;
    }

    @Override
    public String getImportString() {
        return null;
    }

    protected String defaultFieldEntryForType(String t) {
        return "    private " + t + " " + NameUtils.getCamelCaseName(name, false);
    }

    @Override
    public String getFieldEntry() {
        return null;
    }

    protected String getAndSetEntryForType(String type) {
        StringBuilder gasEntry = new StringBuilder();
        String ccName = NameUtils.getCamelCaseName(name, false);
        String ucName = StringUtils.capitalize(ccName);
        gasEntry.append(INDENT +"public " + type + " get" + ucName + "() {\n");
        gasEntry.append(DBLINDENT + "return " + ccName + ";\n");
        gasEntry.append(INDENT + "}\n\n");
        gasEntry.append(INDENT +"public void set" + ucName + "(" + type + " " + ccName + ") {\n");
        gasEntry.append("        this." + ccName + " = " + ccName + ";\n");
        gasEntry.append(INDENT + "}\n");
        return gasEntry.toString();
    }


    protected String defaultCsvEntry() {
        String  csvEntry;
        String ccName = NameUtils.getCamelCaseName(name, false);
        csvEntry = DBLINDENT + "fw.write(" + ccName + "+\",\");";
        return csvEntry;
    }

    public String defaultReadEntry() {
        String readEntry = null;;
        String ccName = NameUtils.getCamelCaseName(name, false);
        if (ccName.equals("recLen")) {
            readEntry = DBLINDENT + "setRecLen(rec.length);";
        }
        return readEntry;
    }


    public String defaultCsvHeaderEntry() {
        String csvHeader;
        String ccName = NameUtils.getCamelCaseName(name, false);
        csvHeader = DBLINDENT + "fw.write(\"" + ccName + "\"+\",\");";
        return csvHeader;
    }

    public String defaultNumericNodeEntry(boolean prefix, boolean arrayValue) {
        if (getRawComponentField() != null && getRawComponentField().equals("none") && prefix==false) {
            return  DBLINDENT + "rn.add(new NoComponentNode(\"" + getDisplayName() +"\"), compare);";  
        } else {
            if(arrayValue) {
                return  DBLINDENT + "rn.add(new NumericFieldNode(\"" + getDisplayName() + "\" + n," + NameUtils.getCamelCaseName(name, false) +"), compare);";  
            } else {
                return  DBLINDENT + "rn.add(new NumericFieldNode(\"" + getDisplayName() + "\"," + NameUtils.getCamelCaseName(name, false) +"), compare);";  
            }
        }
    }

    public String defaultStringNodeEntry(boolean prefix, boolean arrayValue) {
        if (getRawComponentField() != null && getRawComponentField().equals("none") && prefix==false) {
            return  DBLINDENT + "rn.add(new NoComponentNode(\"" + getDisplayName() +"\"), compare);";  
        } else {
            if(arrayValue) {
                return  DBLINDENT + "rn.add(new StringFieldNode(\"" + getDisplayName() + "\" + n," + NameUtils.getCamelCaseName(name, false) +"), compare);";  
            } else {
               return  DBLINDENT + "rn.add(new StringFieldNode(\"" + getDisplayName() + "\"," + NameUtils.getCamelCaseName(name, false) +"), compare);";                  
            }
        }
    }

    @Override
    public String getComponentEntry() {
        String populateEntry= null;
        if (!getComponentField().equals("none")) {
            String ccName = NameUtils.getCamelCaseName(name, false);
            String noPrefixName = ccName.replace("prefix.", "");
            populateEntry = DBLINDENT + "component.set" + StringUtils.capitalize(componentField) +"(" + noPrefixName + ");";
        }
        return populateEntry;
    }

    @Override
    public String getFillFromComponentEntry() {
        String entry = null;
        if (!getComponentField().equals("none") || getComponentField() == null) {
            String noPrefixName = name.replace("prefix.", "");
            String uccName = NameUtils.getCamelCaseName(noPrefixName, true);
            entry = DBLINDENT + "set" + uccName + "(component.get" + StringUtils.capitalize(componentField) + "());";
        }
        return entry;
    }

    public String getFillTheWriteBufferEntry() {
        return "";
    }

    public String getDsectType() {
        String dsectType = "";
        return dsectType;
    }
}

