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


public class ReferenceReportEntry {
    private String workDDName;
    private int viewID;
    private String viewName;
    private String refDDName;
    private int refPFID;
    private String refPFName;
    private int refLRID;
    private Integer refLFID;
    private short keylen;
    private String effStart;
    private String effEnd;

    public String getWorkDDName() {
        return workDDName;
    }

    public void setWorkDDName(String workDDName) {
        this.workDDName = workDDName;
    }

    public int getViewID() {
        return viewID;
    }

    public void setViewID(int v) {
        this.viewID = v;
    }

    public String getViewName() {
        return viewName;
    }

    public void setViewName(String viewName) {
        this.viewName = viewName;
    }

    public String getRefDDName() {
        return refDDName;
    }

    public void setRefDDName(String refDDName) {
        this.refDDName = refDDName;
    }

    public int getRefPFID() {
        return refPFID;
    }

    public void setRefPFID(int pf) {
        this.refPFID = pf;
    }

    public String getRefPFName() {
        return refPFName;
    }

    public void setRefPFName(String refPFName) {
        this.refPFName = refPFName;
    }

    public int getRefLRID() {
        return refLRID;
    }

    public void setRefLRID(int id) {
        this.refLRID = id;
    }

    public Integer getRefLFID() {
        return refLFID;
    }

    public void setRefLFID(Integer lfid) {
        this.refLFID = lfid;
    }

    public short getKeylen() {
        return keylen;
    }

    public void setKeylen(short s) {
        this.keylen = s;
    }

    public String getEffStart() {
        return effStart;
    }

    public void setEffStart(String effStart) {
        this.effStart = effStart;
    }

    public String getEffEnd() {
        return effEnd;
    }

    public void setEffEnd(String effEnd) {
        this.effEnd = effEnd;
    }
}
