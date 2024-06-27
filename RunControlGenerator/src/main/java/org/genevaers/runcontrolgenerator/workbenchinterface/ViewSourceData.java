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


public class ViewSourceData {
    int id;
    int viewID;
    String extractFilter;
    String outputLogic;
    int sequenceNumber;
    int sourceLrId;
    int sourceLfId;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getViewID() {
        return viewID;
    }

    public void setViewID(int viewID) {
        this.viewID = viewID;
    }

    public String getExtractFilter() {
        return extractFilter;
    }

    public void setExtractFilter(String extractFilter) {
        this.extractFilter = extractFilter;
    }

    public int getSequenceNumber() {
        return sequenceNumber;
    }

    public void setSequenceNumber(int sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }

    public int getSourceLrId() {
        return sourceLrId;
    }

    public void setSourceLrId(int sourceLrId) {
        this.sourceLrId = sourceLrId;
    }

    public int getSourceLfId() {
        return sourceLfId;
    }

    public void setSourceLfId(int sourceLfId) {
        this.sourceLfId = sourceLfId;
    }

    public void setOutputLogic(String outputLogic) {
        this.outputLogic = outputLogic;
    }

    public String getOutputLogic() {
        return outputLogic;
    }
}
