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


import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import ComponentGenerator.model.segments.record.Record;

public class FreemarkerFieldEntries {

    private List<String> fieldEntries = new ArrayList<>();
    private List<String> gettersAndSetters = new ArrayList<>();
    private List<String> readers = new ArrayList<>();
    private List<String> fieldNodeEntries = new ArrayList<>();
    private List<String> csvEntries = new ArrayList<>();
    private List<String> csvHeaders = new ArrayList<>();
    private List<String> componentEntries = new ArrayList<>();
    private List<String> fillFromComponentEntries = new ArrayList<>();
    private List<String> fillTheWriteBufferEntries = new ArrayList<>();
    private List<String> dsectEntries = new ArrayList<>();
    private List<String> displayNameEntries = new ArrayList<>();

    public List<String> getFieldEntries() {
        return fieldEntries;
    }

    public void setFieldEntries(List<String> fieldEntries) {
        this.fieldEntries = fieldEntries;
    }

    public List<String> getGettersAndSetters() {
        return gettersAndSetters;
    }

    public void setGettersAndSetters(List<String> gettersAndSetters) {
        this.gettersAndSetters = gettersAndSetters;
    }

    public List<String> getReaders() {
        return readers;
    }

    public void setReaders(List<String> readers) {
        this.readers = readers;
    }

    public List<String> getCsvEntries() {
        return csvEntries;
    }

    public void setCsvEntries(List<String> csvEntries) {
        this.csvEntries = csvEntries;
    }

    public List<String> getCsvHeaders() {
        return csvHeaders;
    }

    public void setCsvHeaders(List<String> csvHeaders) {
        this.csvHeaders = csvHeaders;
    }

    public List<String> getComponentEntries() {
        return componentEntries;
    }

    public void setComponentEntries(List<String> componentEntries) {
        this.componentEntries = componentEntries;
    }

    public List<String> getFillFromComponentEntries() {
        return fillFromComponentEntries;
    }

    public void setFillFromComponentEntries(List<String> fillFromComponentEntries) {
        this.fillFromComponentEntries = fillFromComponentEntries;
    }

    public List<String> getFillTheWriteBufferEntries() {
        return fillTheWriteBufferEntries;
    }

    public void setFillTheWriteBufferEntries(List<String> fillTheWriteBufferEntries) {
        this.fillTheWriteBufferEntries = fillTheWriteBufferEntries;
    }

    public List<String> getDsectEntries() {
        return dsectEntries;
    }

    public void setDsectEntries(List<String> dsectEntries) {
        this.dsectEntries = dsectEntries;
    }

    public List<String> getFieldNodeEntries() {
        return fieldNodeEntries;
    }

    public void setFieldNodeEntries(List<String> fieldNodeEntries) {
        this.fieldNodeEntries = fieldNodeEntries;
    }

    public List<String> getDisplayNameEntries() {
        return displayNameEntries;
    }

    public void setDisplayNameEntries(List<String> displayNameEntries) {
        this.displayNameEntries = displayNameEntries;
    }

    public void addEntriesFrom(Record record, boolean arrayValue) {
		addRecordEntries(record, false, arrayValue);
    }

    public void addEntriesFrom(Record prefix, Record record) {
        addComment(Field.INDENT + "//Prefix entries");
		addRecordEntries(prefix, true, false);
        addComment(Field.INDENT + "//Record entries");
		addRecordEntries(record, false, false);
    }

    public void addEntriesFrom(Record prefix, Record arg, Record record) {
        addComment(Field.INDENT + "//Prefix entries");
		addRecordEntries(prefix, true, false);
        addComment(Field.INDENT + "//Field entries");
		addRecordEntries(record, arg);
    }

    /*
     * This is called from the LT Record Generator
     * and expands the arg entries for dsects
     */
    private void addRecordEntries(Record rec, Record arg) {
        for (Field f : rec.getFields()) {
            if (f.getName().contains("arg")) {
                // determine which arg ' ' 1 or 2
                String argNum = ""; 
                if(f.getName().length() > 3) {
                    argNum += f.getName().charAt(3);
                }
                addArgDsectEntries(arg, argNum);
            }
            addFieldEntries(f, false, false);
        }
    }

    private void addComment(String comment) {
        fieldEntries.add(comment);
        gettersAndSetters.add(comment);
        readers.add(comment);
        csvEntries.add(comment);
        csvHeaders.add(comment);
        fillTheWriteBufferEntries.add(comment);
    }

    private void addArgDsectEntries(Record arg, String argNum) {
        dsectEntries.add("*");
        dsectEntries.add("* BEGIN ARG");
        dsectEntries.add("*");
        //need a temp name here based on arg name.
        //cannot change the name in the arg fields list
        for(Field f : arg.getFields()) {
            String useName = f.getName() + argNum;
            addDsectEntryIfNotNull(f, useName);
		}
    }

    private void addRecordEntries(Record rec, boolean prefix, boolean arrayValue) {
        for(Field f : rec.getFields()) {
			addFieldEntries(f, prefix, arrayValue);
		}
    }

    private void addFieldEntries(Field f, boolean prefix, boolean arrayValue) {
        addFieldEntryIfNotNull(f.getFieldEntry());
        addGetAndSetEntryIfNotNull(f.getGetAndSetEntry());
        addReaderEntryIfNotNull(f.getReadEntry());
        addFieldNodes(f.getFieldNodeEntry(prefix, arrayValue));
        addCsvEntryIfNotNull(f.getCsvEntry());
        addCsvHeaderEntryIfNotNull(f.getCsvHeaderEntry());
        addComponentEntryIfNotNull(f.getComponentEntry());
        addFillFromComponentEntryIfNotNull(f.getFillFromComponentEntry());
        addFillWriteBufferEntryIfNotNull(f.getFillTheWriteBufferEntry());
        addDsectEntryIfNotNull(f, "");
        addDisplayNameIfNotNull(f.getDisplayName());
    }

    private void addDisplayNameIfNotNull(String displayName) {
        if (displayName != null) {
            displayNameEntries.add(displayName);
        }
    }

    private void addFieldNodes(String fieldNodeEntry) {
        if(fieldNodeEntry != null)
        	fieldNodeEntries.add(fieldNodeEntry);
    }

    private void addDsectEntryIfNotNull(Field f, String useName) {
        if (f.getDsectType() != null) {
            String name = useName.length() > 0 ? useName : f.getDsectName();
            String entry = StringUtils.rightPad(name, 42) + "DS " + f.getDsectType();
            dsectEntries.add(entry.toUpperCase());
        }
    }

    private void addFieldEntryIfNotNull(String entry) {
        if(entry != null)
        	fieldEntries.add(entry);
    }

    private void addGetAndSetEntryIfNotNull(String entry) {
        if(entry != null)
        	gettersAndSetters.add(entry);
    }
    private void addReaderEntryIfNotNull(String entry) {
        if(entry != null)
        	readers.add(entry);
    }
    private void addCsvEntryIfNotNull(String entry) {
        if(entry != null)
        	csvEntries.add(entry);
    }
    private void addCsvHeaderEntryIfNotNull(String entry) {
        if(entry != null)
        	csvHeaders.add(entry);
    }
    private void addComponentEntryIfNotNull(String entry) {
        if(entry != null)
        	componentEntries.add(entry);
    }
    private void addFillFromComponentEntryIfNotNull(String entry) {
        if(entry != null)
        	fillFromComponentEntries.add(entry);
    }
    private void addFillWriteBufferEntryIfNotNull(String entry) {
        if(entry != null)
        	fillTheWriteBufferEntries.add(entry);
    }

}
