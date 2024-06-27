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


import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.genevaers.genevaio.ltfile.LTRecord;
import org.genevaers.genevaio.ltfile.LogicTableF1;
import org.genevaers.genevaio.ltfile.LogicTableNameF1;
import org.genevaers.repository.components.enums.DataType;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class LTCoverageEntry1Arg extends LtCoverageEntry{

    private Map<String, Integer> typeHits = new TreeMap<>();

    public void setTypeHits(Map<String, Integer> typeHits) {
        this.typeHits = typeHits;
    }

    public Map<String, Integer> getTypeHits() {
        return typeHits;
    }


    @Override
    public void hit(LTRecord ltr) {
       super.hit(ltr);
       if(ltr instanceof LogicTableF1) {
            DataType dt = ((LogicTableF1)ltr).getArg().getFieldFormat();
            String dts = getTypeName(((LogicTableF1)ltr).getArg().isSignedInd(), dt, ((LogicTableF1)ltr).getArg().getFieldLength());
            hitType(dts);
       } else {
            DataType dt = ((LogicTableNameF1)ltr).getArg().getFieldFormat();
            String dts = getTypeName(((LogicTableNameF1)ltr).getArg().isSignedInd(), dt, ((LogicTableNameF1)ltr).getArg().getFieldLength());
            hitType(dts);
       }
    }


    private void hitType(String dts) {
        //Integers are immutable so we need to mess about
        Integer h = typeHits.get(dts);
        if(h == null) {
            h = Integer.valueOf(1);
        } else {
            h++;
        }
        typeHits.put(dts, h);
    }

    @JsonIgnore
    public Iterator<Entry<String, Integer>> getTypeHitsIterator() {
        return typeHits.entrySet().iterator();
    }

    @Override
    public void addDataFrom(LtCoverageEntry srclte) {
        super.addDataFrom(srclte);
        for( Entry<String, Integer> h : ((LTCoverageEntry1Arg)srclte).getTypeHits().entrySet()) {
            Integer th = typeHits.get(h.getKey());
            if(th != null) {
                th += h.getValue();
                typeHits.put(h.getKey(), th);
            } else {
                typeHits.put(h.getKey(), h.getValue());
            }
        }
    }
    
}
