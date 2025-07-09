package org.genevaers.testframework.yamlreader;

/*
 * Copyright Contributors to the GenevaERS Project. SPDX-License-Identifier: Apache-2.0 (c) Copyright IBM Corporation 2008
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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;

public class GersTest {

    private String name;
    private String header;
    private String description;
    private String source;
    private String decimalplaces;
    private String runviews;
    private String rundate;
    private String passviews;
    private PassViews passViewEntries;
    private List<XMLFile> xmlfiles;
    private List<InputFile> eventfiles = new ArrayList<>(); // default to an empty list
    private List<InputFile> reffiles = new ArrayList<>(); // default to an empty list
    private List<OutputFile> extractfiles = new ArrayList<>(); // default to an empty list;
    private List<OutputFile> formatfiles = new ArrayList<>(); // default to an empty list
    private List<OutputFile> errorfiles = new ArrayList<>(); // default to an empty list
    private MergeParm mergeparm;
    private MergeReport mergerpt;
    private String db2bind = "N";
    private String comparephase = "N";
    private String timeout = "60";
    private String exitload;
    private String expandcookies;
    private String fiscaldateoverride;
    private String rtc = "";
    private ExpectedResult expectedresult = new ExpectedResult();
    private Result result = new Result();
    private String mr91only = "N";
    private OutputFile mr91out;
    private Spec spec;
    private Map<Integer, Boolean> viewResults = new TreeMap<>();
    private String runOnly = "N";

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getHeader() {
        return header;
    }

    public void setHeader(String header) {
        this.header = header;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public List<XMLFile> getXmlfiles() {
        return xmlfiles;
    }

    public void setXmlfiles(List<XMLFile> xmlfiles) {
        this.xmlfiles = xmlfiles;
    }

    public List<InputFile> getEventfiles() {
        return eventfiles;
    }

    public void setEventfiles(List<InputFile> eventfiles) {
        this.eventfiles = eventfiles;
    }

    public List<InputFile> getReffiles() {
        return reffiles;
    }

    public void setReffiles(List<InputFile> reffiles) {
        this.reffiles = reffiles;
    }

    public List<OutputFile> getExtractfiles() {
        return extractfiles;
    }

    public void setExtractfiles(List<OutputFile> extractfiles) {
        this.extractfiles = extractfiles;
    }

    public List<OutputFile> getFormatfiles() {
        return formatfiles;
    }

    public List<OutputFile> getFormatfilesByWorknum(int worknum) {
        List<OutputFile> retList = new ArrayList<>();
        for (OutputFile f : formatfiles) {
            if (f.getWorkfile().equals(Integer.toString(worknum))) {
                retList.add(f);
            }
        }
        return retList;
    }

    public void setFormatfiles(List<OutputFile> formatfiles) {
        this.formatfiles = formatfiles;
    }

    public List<OutputFile> getErrorfiles() {
        return errorfiles;
    }

    public void setErrorfiles(List<OutputFile> errorfiles) {
        this.errorfiles = errorfiles;
    }

    public String getRunviews() {
        return runviews;
    }

    public void setRunviews(String runviews) {
        this.runviews = runviews;
    }

    public String getDecimalplaces() {
        return decimalplaces;
    }

    public void setDecimalplaces(String decimalplaces) {
        this.decimalplaces = decimalplaces;
    }

    public MergeParm getMergeparm() {
        return mergeparm;
    }

    public void setMergeparm(MergeParm mergeparm) {
        this.mergeparm = mergeparm;
    }

    public MergeReport getMergerpt() {
        return mergerpt;
    }

    public void setMergerpt(MergeReport mergerpt) {
        this.mergerpt = mergerpt;
    }

    public String getRundate() {
        return rundate;
    }

    public void setRundate(String rundate) {
        this.rundate = rundate;
    }

    public String getDb2bind() {
        return db2bind;
    }

    public void setDb2bind(String db2bind) {
        this.db2bind = db2bind;
    }

    public String getComparephase() {
        return comparephase;
    }

    public void setComparephase(String comparephase) {
        this.comparephase = comparephase;
    }

    public String getTimeout() {
        return timeout;
    }

    public void setTimeout(String timeout) {
        this.timeout = timeout;
    }

    public String getExitload() {
        return exitload;
    }

    public void setExitload(String exitload) {
        this.exitload = exitload;
    }

    public String getExpandcookies() {
        return expandcookies;
    }

    public void setExpandcookies(String expandcookies) {
        this.expandcookies = expandcookies;
    }

    public String getFiscaldateoverride() {
        return fiscaldateoverride;
    }

    public void setFiscaldateoverride(String fiscaldateoverride) {
        this.fiscaldateoverride = fiscaldateoverride;
    }

    public String getRtc() {
        return rtc;
    }

    public void setRtc(String rtc) {
        this.rtc = rtc;
    }

    public ExpectedResult getExpectedresult() {
        return expectedresult;
    }

    public void setExpectedresult(ExpectedResult result) {
        this.expectedresult = result;
    }

    public Result getResult() {
        return result;
    }

    public void setResult(Result result) {
        this.result = result;
    }

    public String getMr91only() {
        return mr91only;
    }

    public void setMr91only(String mr91only) {
        this.mr91only = mr91only;
    }

    public OutputFile getMr91out() {
        return mr91out;
    }

    public void setMr91out(OutputFile mr90out) {
        this.mr91out = mr90out;
    }

    public void setSpecPath(Spec s) {
        spec = s;
    }

    public String getSpecPath() {
        return spec.getPathName();
    }

    public String getFullName() {
        return spec != null ? spec.getPathName() + "/" + name : "";
    }

    public String getDataSet() {
        return spec != null ? spec.getName().replace("/", ".") + "." + name : "";
    }

    public Iterator<OutputFile> getOutputFileIterator() {
        if(errorfiles.size() > 0) {
            return errorfiles.iterator();
        } else if(formatfiles.size() > 0) {
            return formatfiles.iterator();
        } else if(extractfiles.size() > 0) {
            return extractfiles.iterator();
        } else {
            return null;
        }
    }

    public String getOutputFilePrefix() {
        if(errorfiles.size() > 0) {
                return "";
        } else if(formatfiles.size() > 0) {
            return "OUTF.MR88.";
        } else if(extractfiles.size() > 0) {
            return "OUTE.MR95.";
        } else {
            return "";
        }
    }

    public int getNumExpectedJobs() {
        int expectedNumJobs = formatfiles.size() > 0 ? 4 : 3;
		if(db2bind.equals("Y")) {
			expectedNumJobs++;
		}
        return expectedNumJobs;
    }

    public void verifyExpected() {
        if(expectedresult.getMessage().equals(result.getMessage())) {
            if(result.getMessage().equals("pass")){
                result.setMessage(String.format("pass"));
            } else {
                result.setMessage(String.format("pass expected %s", expectedresult.getMessage()));
            }
        } else {
            result.setMessage(String.format("fail expected %s actual %s", expectedresult.getMessage(), result.getMessage()));
        }
    }

    public boolean hasComparePhase() {
        return comparephase.equalsIgnoreCase("Y");
    }
    
    public void setPassviews(String passviews) {
        this.passviews = passviews;
    }

    public String getPassviews() {
        return passviews;
    }

    public void setPassViewEntries(PassViews passViewEntries) {
        this.passViewEntries = passViewEntries;
    }

    public PassViews getPassViewEntries() {
        return passViewEntries;
    }

    public Iterator<String> getPassViewsIterator() {
        return passViewEntries.getViews().values().iterator();
    }

    public Iterator<Entry<Integer, String>> getPassViewsEntriesIterator() {
        return passViewEntries.getViews().entrySet().iterator();
    }

    public String getViewName(int viewNum) {
		return passViewEntries.getViewName(viewNum);
    }

    public Spec getSpec() {
        return spec;
    }

    public void setViewResult(int viewNum, boolean result) {
        viewResults.put(viewNum, result);
    }

    public Iterator<Entry<Integer, Boolean>> getViewResultsIterator() {
        return viewResults.entrySet().iterator();
    }

    public boolean hasResults() {
        return viewResults.size() > 0;
    }

    public OutputFile findOutputFileWithDDname(String name) {
        if(formatfiles.size() > 0) {
            return formatfiles.stream().filter(f -> f.getDdname().equalsIgnoreCase(name)).findAny().orElse(null);
        } else {
            return extractfiles.stream().filter(f -> f.getDdname().equalsIgnoreCase(name)).findAny().orElse(null);
        }
    }

    public String getRunOnly() {
        return runOnly;
    }

    public void setRunOnly(String runOnly) {
        this.runOnly = runOnly;
    }
}
