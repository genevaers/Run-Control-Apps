package org.genevaers.repository.data;

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


public class InputReport {
    private String ddName;
    private String memberName;
    private String title;
    private String generationID;
    private int    recordCount;
    private String toBeProcessed;

    public void setDdName(String ddName) {
        this.ddName = ddName;
    }

    public String getDdName() {
        return ddName;
    }

    public void setGenerationID(String generationID) {
        this.generationID = generationID;
    }

    public String getGenerationID() {
        return generationID;
    }

    public void setMemberName(String memberName) {
        this.memberName = memberName;
    }

    public String getMemberName() {
        return memberName;
    }

    public void setRecordCount(int recordCount) {
        this.recordCount = recordCount;
    }

    public int getRecordCount() {
        return recordCount;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public void setToBeProcessed(String toBeProcessed) {
        this.toBeProcessed = toBeProcessed;
    }

    public String getToBeProcessed() {
        return toBeProcessed;
    }

}
