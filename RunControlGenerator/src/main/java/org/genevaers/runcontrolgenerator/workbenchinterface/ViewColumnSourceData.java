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


public class ViewColumnSourceData {
    int componentId;
    int columnId;
    int columnNumber;
    int viewID;
    String logicText;
    int sequenceNumber;
    int sourceTypeValue;
    int viewSourceId;
    int viewSourceLrId;

    public int getComponentId() {
        return componentId;
    }

    public void setComponentId(int componentId) {
        this.componentId = componentId;
    }

    public int getColumnId() {
        return columnId;
    }

    public void setColumnId(int columnId) {
        this.columnId = columnId;
    }

    public int getColumnNumber() {
        return columnNumber;
    }

    public void setColumnNumber(int columnNumber) {
        this.columnNumber = columnNumber;
    }

    public int getViewID() {
        return viewID;
    }

    public void setViewID(int viewID) {
        this.viewID = viewID;
    }

    public String getLogicText() {
        return logicText;
    }

    public void setLogicText(String logicText) {
        this.logicText = logicText;
    }

    public int getSequenceNumber() {
        return sequenceNumber;
    }

    public void setSequenceNumber(int sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }

    public int getSourceTypeValue() {
        return sourceTypeValue;
    }

    public void setSourceTypeValue(int sourceTypeValue) {
        this.sourceTypeValue = sourceTypeValue;
    }

    public int getViewSourceId() {
        return viewSourceId;
    }

    public void setViewSourceId(int viewSourceId) {
        this.viewSourceId = viewSourceId;
    }

    public int getViewSourceLrId() {
        return viewSourceLrId;
    }

    public void setViewSourceLrId(int viewSourceLrId) {
        this.viewSourceLrId = viewSourceLrId;
    }

}
