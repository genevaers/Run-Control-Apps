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


public class LRFieldData {
    int id;
    int dataTypeValue;
    int dateCodeValue;
    int length;
    int lrId;
    String name;
    int numDecimals;
    int rounding;
    boolean signed;
    int position;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getDataTypeValue() {
        return dataTypeValue;
    }

    public void setDataTypeValue(int dataTypeValue) {
        this.dataTypeValue = dataTypeValue;
    }

    public int getDateCodeValue() {
        return dateCodeValue;
    }

    public void setDateCodeValue(int dateCodeValue) {
        this.dateCodeValue = dateCodeValue;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public int getLrId() {
        return lrId;
    }

    public void setLrId(int lrId) {
        this.lrId = lrId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getNumDecimals() {
        return numDecimals;
    }

    public void setNumDecimals(int numDecimals) {
        this.numDecimals = numDecimals;
    }

    public int getRounding() {
        return rounding;
    }

    public void setRounding(int rounding) {
        this.rounding = rounding;
    }

    public boolean isSigned() {
        return signed;
    }

    public void setSigned(boolean signed) {
        this.signed = signed;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

}
