package org.genevaers.runcontrolanalyser.ltcoverage;

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


import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.genevaers.genevaio.ltfile.LTRecord;
import org.genevaers.repository.components.enums.DataType;
import org.genevaers.repository.components.enums.LtRecordType;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = As.PROPERTY, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = LtCoverageEntry.class, name = "HD"),
        @JsonSubTypes.Type(value = LtCoverageEntry.class, name = "F0"),
        @JsonSubTypes.Type(value = LTCoverageEntry1Arg.class, name = "F1"),
        @JsonSubTypes.Type(value = LTCoverageEntry2Args.class, name = "F2"),
        @JsonSubTypes.Type(value = LtCoverageEntry.class, name = "RE"),
        @JsonSubTypes.Type(value = LtCoverageEntry.class, name = "WR"),
        @JsonSubTypes.Type(value = LtCoverageEntry.class, name = "CC"),
        @JsonSubTypes.Type(value = LtCoverageEntry.class, name = "NAME"),
        @JsonSubTypes.Type(value = LtCoverageEntry.class, name = "NAMEVALUE"),
        @JsonSubTypes.Type(value = LtCoverageEntry.class, name = "CALC"),
        @JsonSubTypes.Type(value = LTCoverageEntry1Arg.class, name = "NAMEF1"),
        @JsonSubTypes.Type(value = LTCoverageEntry2Args.class, name = "NAMEF2"),
        @JsonSubTypes.Type(value = LtCoverageEntry.class, name = "GENERATION")
})


public class LtCoverageEntry {

    private LtRecordType type;
    private String name;
    private int hits;
    private int expectedItems;
    private String description;   
    private String category;

    public static final int MAXTYPES = 17;


    public LtRecordType getType() {
        return type;
    }

    public void setType(LtRecordType type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getHits() {
        return hits;
    }

    public void setHits(int hits) {
        this.hits = hits;
    }


    public int getExpectedItems() {
        return expectedItems;
    }

    public void setExpectedItems(int expectedItems) {
        this.expectedItems = expectedItems;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getCategory() {
        return category;
    }

    public void hit(LTRecord ltr) {
        hits++;
    }
    
    protected String getTypeName(boolean signed, DataType dt, int len) {
        String dts;
           String sign =  signed ? "S" : "";
           if(dt == DataType.BINARY){
                dts = len < 8 ? sign : "";
                dts += dt.toString(); 
                dts += len;
           } else if(dt == DataType.PACKED || dt == DataType.ZONED ) {
                dts = sign + dt.toString(); 
           } else {
                dts = dt.toString();
           }
        return dts;
    }

    public void addDataFrom(LtCoverageEntry srclte) {
        hits += srclte.getHits();
    }


    // Derive class to hold the argType list
    // of matrix
    // typeHitMap m_typeHitMap;
    // don't really want this for all codes - so create specific classes
    // or new the arrays when needed
    // typeHitMatrixMap m_typeHitMatrixMap;

}
