package org.genevaers.testframework;

/*
 * Copyright Contributors to the GenevaERS Project. SPDX-License-Identifier: Apache-2.0 (c) Copyright IBM Corporation 2008.
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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.logging.Logger;

import org.genevaers.testframework.yamlreader.GersTest;
import org.genevaers.testframework.yamlreader.OutputFile;
import org.genevaers.testframework.yamlreader.Spec;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class TestCategory {
	private static final String RUN_CLASS_BASE = "org.genevaers.test";
	public String name = "";
	public Integer numPassed = 0;
	public Integer numUnknown = 0;
	public Integer totalNumTests = 0;
	public List<SpecGroup> specGroups = new ArrayList<SpecGroup>();
	static Logger logger = Logger.getLogger("org.genevaers.testframework.TestCategory");

	public String getName() {
		return name;
	}

	public Integer getNumPassed() {
		return numPassed;
	}

	public Integer getNumUnknown() {
		return numUnknown;
	}

	public Integer getTotalNumTests() {
		return totalNumTests;
	}

	public boolean allPassed() {
		if (totalNumTests == null || numPassed == null) {
			return false;
		}
		return totalNumTests.equals(numPassed);
	}

	public List<SpecGroup> getSpecGroups() {
		return specGroups;
	}

	public void addSpecGroup(SpecGroup group) {
		specGroups.add(group);
	}

	public void setName(String category) {
		name = category;
	}

	// A Spec group is keyed on its list of header names
	public SpecGroup addSpecGroups(Spec spec) {

		// form list of headers
		List<String> headers = new ArrayList<>();
		headers.add("Spec"); // for first column of the table
		for (GersTest test : spec.getTests()) {
			headers.add(test.getHeader());
		}

		// look for existing group
		SpecGroup mGroup = null;
		for (SpecGroup group : getSpecGroups()) {
			// compare headers
			int i = 0;
			boolean headerEq = true;
			if (headers.size() != group.headers.size()) {
				continue;
			}
			for (String header : headers) {
				String gHeader = group.headers.get(i);
				if (header.compareToIgnoreCase(gHeader) != 0) {
					headerEq = false;
					break;
				}
				i++;
			}
			if (headerEq) {
				mGroup = group;
				break;
			}
		}

		if (mGroup == null) {
			// form new group
			mGroup = new SpecGroup();
			mGroup.headers = headers;
			addSpecGroup(mGroup);
		}
		return mGroup;
	}

	private String getTitle(Document doc) {
		String title = "";
		NodeList titleNodes = doc.getElementsByTagName("Title");
		if (titleNodes.getLength() > 0) {
			Node topName = titleNodes.item(0); // Start at the first Name
			return topName.getNodeValue().trim();
		}
		return title;
	}

	protected List<SpecTestResult> getSpecTestResults(Path specOutputPath, Path specFileDirPath, Spec spec) {
		List<SpecTestResult> testResults = new ArrayList<>();
		for (GersTest test : spec.getTests()) {
			if(test.getPassviews() != null) {
				expandResults(testResults, test);
			} else {
				addToTestResults(specOutputPath, specFileDirPath, spec, testResults, test);
			}
		}
		return testResults;
	}

	private void addToTestResults(Path specOutputPath, Path specFileDirPath, Spec spec, List<SpecTestResult> testResults,
			GersTest test) {
		String WBXMLFile = test.getXmlfiles().get(0).getName();

		SpecTestResult stres = getTestResult(specOutputPath, specFileDirPath, spec.getName(), test.getName(),
				WBXMLFile);
		if (stres.passed()) {
			numPassed++;
		}
		if (stres.isUnknown()) {
			numUnknown++;
		}
		stres.setDescription(test.getDescription());
		stres.setRTC(test.getRtc());
		testResults.add(stres);
	}

	private void expandResults(List<SpecTestResult> testResults, GersTest test) {
		if(test.hasResults()) {
			addExpandedResults(testResults, test);
		} else {
			addUnknownResults(testResults, test);
		}
	}

	private void addUnknownResults(List<SpecTestResult> testResults, GersTest test) {
		Iterator<Entry<Integer, String>> pvi = test.getPassViewsEntriesIterator();
		while(pvi.hasNext()) {
			SpecTestResult testResult = new SpecTestResult();
			Entry<Integer, String> pv = pvi.next();
			testResult.name = pv.getValue() + "[" + pv.getKey() + "]";	
			testResult.result = "unknown";
			OutputFile ff = test.findOutputFileWithDDname(String.format("F%07d", pv.getKey()));
			if(ff != null) {
				testResult.setDescription(test.getDescription() + " - Workfile " + ff.getWorkfile());
				numUnknown++;
			} else {
				testResult.setDescription(test.getDescription() + " not used");
			}
			testResult.setRTC(test.getRtc());
			testResults.add(testResult);
		}
	}

	private void addExpandedResults(List<SpecTestResult> testResults, GersTest test) {
		Iterator<Entry<Integer, Boolean>> vri = test.getViewResultsIterator();
		while(vri.hasNext()) {
			Entry<Integer, Boolean> vr = vri.next();
			SpecTestResult testResult = new SpecTestResult();
			testResult.name = test.getViewName(vr.getKey()) + "[" + vr.getKey() + "]";	
			if(vr.getValue()) {
				testResult.result = "pass";
				numPassed++;
			} else {
				testResult.result = "fail";
			}
			OutputFile ff = test.findOutputFileWithDDname(String.format("F%07d", vr.getKey()));
			if(ff != null) {
				testResult.setDescription(test.getDescription() + " - Workfile " + ff.getWorkfile());
			} else {
				testResult.setDescription(test.getDescription() + " not used");
			}
			testResult.setRTC(test.getRtc());
			testResults.add(testResult);
		}
	}

	private SpecTestResult getTestResult(Path specOutputPath, Path specFileDirPath, String specName, String testName,
			String wbXMLFile) {
		SpecTestResult testResult = new SpecTestResult();
		testResult.name = testName;
		testResult.wbxmlFileName = wbXMLFile;
		Path resPath = specFileDirPath.resolve(testName);
		testResult.viewhtmlPath = specOutputPath.relativize(resPath.resolve("view.html")).toString();
		
		if (resPath.resolve("pass.html").toFile().exists()) {
			testResult.result = "pass";
			testResult.absPath = specOutputPath.relativize(resPath.resolve("pass.html")).toString();
		} else if (resPath.resolve("fail.html").toFile().exists()) {
			testResult.result = "fail";
			testResult.absPath = specOutputPath.relativize(resPath.resolve("fail.html")).toString();
		} else if (resPath.resolve("nobase.html").toFile().exists()) {
			testResult.result = "nobase";
			testResult.absPath = specOutputPath.relativize(resPath.resolve("nobase.html")).toString();
		} else if (resPath.resolve("unexpectedPass.html").toFile().exists()) {
			testResult.result = "unexpectedPass";
			testResult.absPath = specOutputPath.relativize(resPath.resolve("unexpectedPass.html")).toString();
		} else if (resPath.resolve("jesFail.html").toFile().exists()) {
			testResult.result = "jesFail";
			testResult.absPath = specOutputPath.relativize(resPath.resolve("jesFail.html")).toString();
		} else {
			testResult.result = "unknown";
		}
		// Needs the name / -> . and the base class name . "test"testName
		String runClassName = specName + "#test" + testName;
		testResult.setClassRunName(runClassName);
		return testResult;
	}

}
