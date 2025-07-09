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


import java.nio.file.Path;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;

import org.genevaers.genevaio.ltfile.LTRecord;

public class LTCoverageFile {
    private String ltcoverage;
    private List<Path> sources = new ArrayList<>();
    private String generationDate;

    private Map<String, LtCoverageEntry> functionCodes = new TreeMap<>();

    public List<Path> getSources() {
        return sources;
    }

    public void setSource(List<Path> source) {
        this.sources = source;
    }

    public void addSource(Path s) {
        sources.add(s);
    }

    public String getGenerationDate() {
        if(generationDate == null) {
            DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd hh:mm:ss");
            Date dt = Calendar.getInstance().getTime();
            generationDate = dateFormat.format(dt);
        }
        return generationDate;
    }

    public void setGenerationDate(String generationDate) {
        this.generationDate = generationDate;
    }

    public String getLtcoverage() {
        return ltcoverage == null ? "" : ltcoverage;
    }

    public void setLtcoverage(String ltcoverage) {
        this.ltcoverage = ltcoverage;
    }

    public Map<String, LtCoverageEntry> getFunctionCodes() {
        return functionCodes;
    }

    public void setFunctionCodes(Map<String, LtCoverageEntry> functionCodes) {
        this.functionCodes = functionCodes;
    }

    public void loadFrom(LTCoverageFile ltc) {
        Map<String, LtCoverageEntry> fcs = ltc.getFunctionCodes();
        //Not really an aggregate yet.
        //But want to get the coverage map so we can write it
        for(Entry<String, LtCoverageEntry> fce : fcs.entrySet()) {
            functionCodes.computeIfAbsent(fce.getKey(), e -> fce.getValue());
        }
    }

    public void aggregateFrom(LTCoverageFile ltc) {
        Map<String, LtCoverageEntry> fcs = ltc.getFunctionCodes();
        //Not really an aggregate yet.
        //But want to get the coverage map so we can write it
        for(Entry<String, LtCoverageEntry> fce : fcs.entrySet()) {
            LtCoverageEntry targetlte = functionCodes.get(fce.getKey());
            LtCoverageEntry srclte = fce.getValue();
            if(targetlte != null) {
                targetlte.addDataFrom(srclte);
            } else {
                int bang = 1;
            }
        }
    }

    public void addCoverageEntry(LtCoverageEntry ce) {
        functionCodes.put(ce.getName(), ce);
    }

    public void hit(LTRecord ltr) {
        functionCodes.get(ltr.getFunctionCode()).hit(ltr);
    }
}
