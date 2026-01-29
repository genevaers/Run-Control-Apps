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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import com.google.common.flogger.FluentLogger;

import org.genevaers.testframework.functioncodecoverage.CoverageReportGenerator;
import org.genevaers.utilities.CommandRunner;
import org.genevaers.testframework.yamlreader.Spec;
import org.genevaers.testframework.yamlreader.YAMLReader;
import org.genevaers.utilities.GersEnvironment;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;

// Class is used to generate 
//	The overview.html
//  and optionally
//  the coverage information
//  the VDP Flow information
//
// This class is to be used only with the new FreeMarker base templates
// And so does not support XSLT
//
public class TestReporter {

	private static final String WRITE_TO = "Write to ";
	private static final String CSS_PATH = "cssPath";
	private static final String W3_CSS = "w3.css";
	private static final String W3_CSSPATH = "../w3.css";
	private static final String COVERAGE_JSON = "coverage.json";
	private static final FluentLogger logger = FluentLogger.forEnclosingClass();
	private Map<String, TestCategory> categories = new HashMap<>();

	private Path overviewHTMLFile;
	private Path overallcoverage;
	private Path rootPath;
	private Path outPath;
	private boolean covergageEnabled;
	private Path specPath;
	private boolean vdpFlowEnabled;
	private Configuration cfg;
	private GersEnvironment fmEnv;

	private boolean allTestsPassed = true;

	public class QandA {
		String question = "";
		String answer = "";

		public String getQuestion() {
			return question;
		}

		public String getAnswer() {
			return answer;
		}
	}

	private List<QandA> qAndAs = new ArrayList<>();
	private YAMLReader yr;

	public static void main(String[] args) {
		try {
			TestReporter overview = new TestReporter();
			overview.generate();
			if (overview.allPassed()) {
				logger.atInfo().log("All tests were successful");
				System.exit(0);
			} else {
				logger.atSevere().log("Not all tests passed");
				System.exit(0);
			}
		} catch (Exception e) {
			logger.atSevere().withCause(e).log("Exception occurred");
		}
	}

	public boolean allPassed() {
		logger.atInfo().log("Reported all tests passed: %s", allTestsPassed);
		return allTestsPassed;
	}

	public boolean generate() throws IOException {
		boolean success = true;
		logger.atInfo().log("Generating Test Result Overview");

		rootPath = Paths.get(GersEnvironment.get("LOCALROOT")).normalize();
		outPath = getOutDir(rootPath).normalize();
		specPath = rootPath.resolve("spec").normalize();
		initFreeMarkerConfiguration();
		processSpecList();
		// Do this before the Overview so we can pick up the correct links
		writeCoverageHTML();
		writeFMOverviewHTML();
		return success;
	}

	private void initFreeMarkerConfiguration() throws IOException {
		cfg = new Configuration(Configuration.VERSION_2_3_30);
		cfg.setDirectoryForTemplateLoading(new File("FreeMarkerTemplates"));
		cfg.setDefaultEncoding("UTF-8");
		cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
	}

	private void writeFMOverviewHTML() {
		try {
			Template template = cfg.getTemplate("test/overview.ftl");
			Map<String, Object> nodeMap = new HashMap<>();
			if(outPath.resolve("aggregateCov.html").toFile().exists()) {
				nodeMap.put("covAvailable", "Yes");
			} else {
				nodeMap.put("covAvailable", "No");
			}
			nodeMap.put("env", fmEnv.getEnvironmentVariables());
			nodeMap.put(CSS_PATH, W3_CSSPATH);
			nodeMap.put("categories", categories.values());
			nodeMap.put("QandA", getQandAs());
			overviewHTMLFile = outPath.resolve("fmoverview.html").normalize();
			logger.atInfo().log(WRITE_TO + overviewHTMLFile.toString());
			generateTemplatedOutput(template, nodeMap, overviewHTMLFile);
			Template textTemplate = cfg.getTemplate("test/overviewText.ftl");
			Path textOverview = outPath.resolve("fmoverview.txt").normalize();
			logger.atInfo().log(WRITE_TO + textOverview.toString());
			generateTemplatedOutput(textTemplate, nodeMap, textOverview);
			Template csvTemplate = cfg.getTemplate("test/overviewCSV.ftl");
			Path csvOverview = outPath.resolve("fmoverview.csv").normalize();
			logger.atInfo().log(WRITE_TO + csvOverview.toString());
			generateTemplatedOutput(csvTemplate, nodeMap, csvOverview);
		} catch (IOException | TemplateException e) {
			logger.atSevere().log("Exception occurred in writing FMOverview html \n%s", e.getMessage());
		}
	}

	private void writeCoverageHTML() {
		
		// Generate the accumulated HTML coverage report
		if (covergageEnabled) {
			CoverageReportGenerator covGenerator = new CoverageReportGenerator();
			covGenerator.setCSSDir(W3_CSSPATH);
			covGenerator.accumulateTo(null);
			covGenerator.writeCoverageHTML(outPath.toFile(), "coverage");
			covGenerator.close();
		}
	}

	private String generateSpecHTML(Path specFileDirPath, Spec spec) {
		Path specHTMLPath = specFileDirPath.resolve(spec.getName() + ".html").normalize();
		Path cssPath = specHTMLPath.relativize(outPath).resolve(W3_CSS).normalize();
		if (specHTMLPath.getParent().toFile().exists() == false) {
			// Then jolly well make it exist
			specHTMLPath.getParent().toFile().mkdirs();
		}
		try {
			Template template = cfg.getTemplate("test/spec.ftl");
			Map<String, Object> nodeMap = new HashMap<>();
			nodeMap.put(CSS_PATH, cssPath);
			nodeMap.put("pmspec", spec);
			generateTemplatedOutput(template, nodeMap, specHTMLPath);
		} catch (IOException | TemplateException e) {
			logger.atSevere().log("Exception in generate spec HTML \n%s", e.getMessage());
		}
		Path sphtml = outPath.relativize(specHTMLPath);
		return sphtml.toString();
	}

	protected void processSpecList() {
		Iterator<Spec> si = TestRepository.getSpecIterator();

		while(si.hasNext()) {
			processSpec(si.next());
		}
	}

	private void processSpec(Spec spec) {
		// Just open the file once and get all we need
		// Follow the SAFR way :-)

		TestCategory cat = findOrMakeCategory(spec.getCategory()); // Why do this need the File.... to open it
																	// later?
		// and the name of the spec... that is the beast without the .xml
		// String specName = specFile.substring(0, specFile.length()-4);
		Path specOutputPath = outPath.resolve(spec.getCategory()).resolve(spec.getName()).normalize();

		List<SpecTestResult> results = cat.getSpecTestResults(outPath, specOutputPath, spec);
		// checkForTestCoverageAndVDPFlowResults(results);
		SpecGroup specGroup = cat.addSpecGroups(spec);
		SpecResult specResult = specGroup.addTestResults(spec.getName(), results);
		specResult.setDescription(spec.getDescription());
		String specAbsPath = generateSpecHTML(specOutputPath, spec);
		specResult.setHtmlPath(specAbsPath);
		logger.atInfo().log("Spec " + spec.getName() + " Tests " + results.size() + " Passed " + cat.allPassed()	);
		cat.totalNumTests += results.size();
		logger.atInfo().log("Category " + cat.getName() + "Total Tests: " + cat.totalNumTests + " Passed: " + cat.numPassed);
		allTestsPassed &= cat.allPassed();
		logger.atInfo().log("All tests passed: " + allTestsPassed);

	}

	protected TestCategory findOrMakeCategory(String category) {
		TestCategory cat = null;
		if (categories.containsKey(category)) {
			cat = categories.get(category);
		} else {
			cat = new TestCategory();
			cat.setName(category);
			categories.put(category, cat);
		}
		return cat;
	}

	private Path getOutDir(Path root) {
		Path outDir = Paths.get(root.toString(), "out");
		String outDirName = fmEnv.get("OUTDIR");
		String os = fmEnv.get("RUNOS");
		if (os.equalsIgnoreCase("WINDOWS") && outDirName.length() > 0) { // this will only happen for Windows runs so
																			// default above and reassign here
			outDir = Paths.get(root.toString(), "out");
		}
		return outDir;
	}

	private void generateTemplatedOutput(Template temp, Map<String, Object> templateModel,
			Path target) throws IOException, TemplateException {
		if (target.getParent().toFile().exists() == false) {
			target.getParent().toFile().mkdirs();
		}
		FileWriter cfgWriter = new FileWriter(target.toFile());
		temp.process(templateModel, cfgWriter);
		cfgWriter.close();
	}

	private List<QandA> getQandAs() {
		addQandAs("What is a test category?",
				"A loose collection of similarly purposed tests. Click on a Category to see the tests within. "
						+ "<br>Close the category by clicking on the X name.");
		addQandAs("Can I get more info about a test?",
				"Open a test. Click on the Details link to the right of the test name");
		addQandAs("What do all the pretty colours mean?",
				"Traffic lights for pass results. Green = Pass, Amber = Some passed, Red = Fail, Sand = Unknown");
		addQandAs("How do I create a test?",
				"Black magic");
		addQandAs("As a developer can I create a test for my work?",
				"ABSOLUTLEY! That is what this is for. Not just for regression.");
		addQandAs("I have some ideas about the test framework. How can I pass them on?",
				"Check out Ideas in the side bar. It will take you to RTC");
		return qAndAs;
	}

	private void addQandAs(String q, String a) {
		QandA q1 = new QandA();
		q1.question = q;
		q1.answer = a;
		qAndAs.add(q1);
	}

}
