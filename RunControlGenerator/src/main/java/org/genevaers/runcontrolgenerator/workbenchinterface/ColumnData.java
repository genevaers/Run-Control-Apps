package org.genevaers.runcontrolgenerator.workbenchinterface;

/*
 * Copyright Contributors to the GenevaERS Project.
								SPDX-License-Identifier: Apache-2.0 (c) Copyright IBM Corporation
								2008
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


public class ColumnData {
    int viewID;
    int columnNumber;
    int columnId;
    int extractAreaValue;
    int dataTypeValue; // May have to set via ordinal
    boolean signed;
    int length;
    int numDecimalPlaces;
    int dateCodeValue;
    int rounding;
    int alignmentValue;
    String numericMask; // Needed?
    int startPosition;
    int extractAreaPosition;
    int ordinal;
    String name;
    String columnCalculation;

    public int getViewID() {
        return viewID;
    }

    public void setViewID(int viewID) {
        this.viewID = viewID;
    }
    
    public int getColumnNumber() {
        return columnNumber;
    }

    public void setColumnNumber(int columnNumber) {
        this.columnNumber = columnNumber;
    }

    public int getColumnId() {
        return columnId;
    }

    public void setColumnId(int columnId) {
        this.columnId = columnId;
    }

    public int getExtractAreaValue() {
        return extractAreaValue;
    }

    public void setExtractAreaValue(int extractArea) {
        this.extractAreaValue = extractArea;
    }

    public int getDataTypeValue() {
        return dataTypeValue;
    }

    public void setDataTypeValue(int dataType) {
        this.dataTypeValue = dataType;
    }

    public boolean isSigned() {
        return signed;
    }

    public void setSigned(boolean signed) {
        this.signed = signed;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public int getNumDecimalPlaces() {
        return numDecimalPlaces;
    }

    public void setNumDecimalPlaces(int numDecimalPlaces) {
        this.numDecimalPlaces = numDecimalPlaces;
    }

    public int getDateCodeValue() {
        return dateCodeValue;
    }

    public void setDateCodeValue(int dateCode) {
        this.dateCodeValue = dateCode;
    }

    public int getRounding() {
        return rounding;
    }

    public void setRounding(int rounding) {
        this.rounding = rounding;
    }

    public int getAlignment() {
        return alignmentValue;
    }

    public void setAlignment(int alignment) {
        this.alignmentValue = alignment;
    }

    public String getNumericMask() {
        return numericMask;
    }

    public void setNumericMask(String numericMask) {
        this.numericMask = numericMask;
    }

    public int getStartPosition() {
        return startPosition;
    }

    public void setStartPosition(int startPosition) {
        this.startPosition = startPosition;
    }

    public int getOrdinal() {
        return ordinal;
    }

    public void setOrdinal(int ordinal) {
        this.ordinal = ordinal;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getColumnCalculation() {
        return columnCalculation;
    }

    public void setColumnCalculation(String columnCalculation) {
        this.columnCalculation = columnCalculation;
    }

    public int getExtractAreaPosition() {
        return extractAreaPosition;
    }

    public void setExtractAreaPosition(int extractAreaPosition) {
        this.extractAreaPosition = extractAreaPosition;
    }
}
