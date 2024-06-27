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
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.genevaers.genevaio.ltfile.LTRecord;
import org.genevaers.genevaio.ltfile.LogicTableArg;
import org.genevaers.genevaio.ltfile.LogicTableF2;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class LTCoverageEntry2Args extends LtCoverageEntry{

    private Map<String, Map<String, Integer>> typeHitsMatrix = new TreeMap<>();

    @Override
    public void hit(LTRecord ltr) {
        super.hit(ltr);
        hit(((LogicTableF2)ltr).getArg1(), ((LogicTableF2)ltr).getArg2());;

    }

    private void hit(LogicTableArg arg1, LogicTableArg arg2) {
        String dts1 = getTypeName(arg1.isSignedInd(), arg1.getFieldFormat(), arg1.getFieldLength());
        String dts2 = getTypeName(arg2.isSignedInd(), arg2.getFieldFormat(), arg2.getFieldLength());
        Map<String, Integer> thr = typeHitsMatrix.computeIfAbsent(dts1, d -> makeTypeHitsRow(dts1));
        hitRow(dts2, thr);
    }

    private void hitRow(String dts2, Map<String, Integer> thr) {
        Integer h = thr.get(dts2);
        if(h == null) {
            h = Integer.valueOf(1);
        } else {
            h++;
        }
        thr.put(dts2, h);
    }

    private Map<String, Integer> makeTypeHitsRow(String dts1) {
        return new TreeMap<>();
    }

    public Map<String, Map<String, Integer>> getTypeHitsMatrix() {
        return typeHitsMatrix;
    }

    public void setTypeHitsMatrix(Map<String, Map<String, Integer>> typeHitsMatrix) {
        this.typeHitsMatrix = typeHitsMatrix;
    }

    @JsonIgnore
    public Map<String, Integer> getRowForType(String t) {
        return typeHitsMatrix.get(t);
    }

    @JsonIgnore
    public Iterator<Entry<String, Map<String, Integer>>> getTypeMatrixIterator() {
        return typeHitsMatrix.entrySet().iterator();
    }
    
    @Override
    public void addDataFrom(LtCoverageEntry srclte) {
        super.addDataFrom(srclte);
        for( Entry<String, Map<String, Integer>> srcTypeHitsRow : ((LTCoverageEntry2Args)srclte).getTypeHitsMatrix().entrySet()) {
            Map<String, Integer> thr = typeHitsMatrix.get(srcTypeHitsRow.getKey());
            Map<String, Integer> srcths = srcTypeHitsRow.getValue();
            if(thr != null) {
                addHits(srcths, thr);
            } else {
                typeHitsMatrix.put(srcTypeHitsRow.getKey(), srcths);
            }
        }
    }

    private void addHits(Map<String, Integer> srcths, Map<String, Integer> thr) {
        for(Entry<String, Integer> row : srcths.entrySet()) {
            Integer h = thr.get(row.getKey());
            if(h == null) {
                h = row.getValue();
            } else {
                h += row.getValue();
            }
            thr.put(row.getKey(), h);    
        }
    }

}
