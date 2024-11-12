package org.genevaers.mr91comparisons;

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


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.ConsoleHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.genevaers.testframework.FMLogger;
import org.genevaers.utilities.CommandRunner;

import com.google.common.flogger.FluentLogger;
import com.google.common.flogger.StackSize;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;

// Class is used to generate 
//	The overview.html
//  and optionally
//  the coverage information
//  the com.ibm.safr.VDP.metadata Flow information
//
// This class is to be used only with the new FreeMarker base templates
// And so does not support XSLT
//
public class TestReporter {
	private static final FluentLogger logger = FluentLogger.forEnclosingClass();
	
	/**
	 *
	 */
	private static final String LOCALROOT = "LOCALROOT";
	private static final String COMPARISON_SOURCE = "MR91CMPSRC"; // DB or WBXML
	private static final String COMPARISON_TARGET = "MR91CMPTRG"; // MR91DBXML, MR91WBXMLXML, or gvbrca
	private static final String TEST_MR91REPORT_FTL = "test/mr91report.ftl";
	
	// These should really be done view view source!!!!
	public class VDPResult{
		public Path viewSourcePath;
		public String getResult() {
			//If extract filter passes and all of the columns passed
			String result = "pass";
			return result;
		}
	}

	private static final String TESTOUT = "mr91Outputs";
    private static final String COVERAGE_JSON = "coverage.json";
    private String SAFR_CSS = "../w3.css";
	
    private boolean success = true;
    
	private Path rootPath;
	private MR91CompTestEnvironment environmentVariables;
	Path wbxmlPath;
	private Configuration fmcfg;
	private Path testOutPath;

	private Path reportHTMLFile;
	private Path overallcoverage;

	private Path outPath;
	private boolean covergageEnabled;
	private Configuration cfg;
	
	private TreeMap<String, MR91TestSet> testSetResultsByName = new TreeMap<>();
	
	private boolean allTestsPassed = true;

	private String targetType;

	private String sourceType;

    public static void main(String[] args) {
        try {
            FMLogger.setup();

            TestReporter report = new TestReporter();
            report.generate();
    		if(report.allPassed()) {
    			logger.atInfo().log("All tests were successful");
    			System.exit(0);
    		} else {
    			logger.atSevere().log("Not all tests passed");
    			System.exit(1);
    		}
        } catch (Exception e) {
            logger.atSevere().withStackTrace(StackSize.LARGE).log(e.getMessage());
        }
    }

    public boolean generate() throws Exception {
	    initLogger();

        success = true;
        logger.atInfo().log("Generating Test Result Report");
                
        initialiseFromEnvironment();
        initFreeMarkerConfiguration();
        reportTestResults();
        writeHTMLReport();

        return success;
    }

    public boolean allPassed() { return allTestsPassed; }
    

	private void initLogger() {
		Logger jdkLogger = Logger.getLogger(Tester.class.getName());
    	jdkLogger.setUseParentHandlers(true);
		Logger topLogger = jdkLogger.getParent();
		Handler[] hndlrs = topLogger.getHandlers();
		if(hndlrs.length > 0)
			topLogger.removeHandler(hndlrs[0]);
		topLogger.setLevel(Level.INFO);
		Handler conHdlr = new ConsoleHandler();
		conHdlr.setFormatter(new Formatter() {
			public String format(LogRecord record) {
				return record.getLevel() + ":" + record.getSourceClassName() + " : "
						+ record.getSourceMethodName() + " : " + record.getMessage() + "\n";
			}
		});
		conHdlr.setLevel(Level.INFO);
		topLogger.addHandler(conHdlr);
	}

	private void initialiseFromEnvironment() {
		rootPath = Paths.get(environmentVariables.get(LOCALROOT));
		testOutPath = rootPath.resolve(TESTOUT);
		//wbxmlPath = rootPath.resolve(SFXML);
	}

	private void initFreeMarkerConfiguration() throws IOException {
		cfg = new Configuration(Configuration.VERSION_2_3_30);
		cfg.setDirectoryForTemplateLoading(new File("FreeMarkerTemplates"));
		cfg.setDefaultEncoding("UTF-8");
		cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
	}

	protected void reportTestResults() throws Exception {

		// want to find all of the tests - how do we identify them? Via MR91RPTs?
		// use the Apache beast
        testOutPath = rootPath.resolve(TESTOUT);
		logger.atInfo().log("Processing All XML in: %s", testOutPath);
		WildcardFileFilter fileFilter = new WildcardFileFilter("MR91RPT");
		Collection<File> mr91rptFiles = FileUtils.listFiles(testOutPath.toFile(), fileFilter, TrueFileFilter.TRUE);
		for (File mr91rpt : mr91rptFiles) {
			reportTest(mr91rpt);
		}
		addNotes();
   }

	/**
	 * Take the (WBXML or DB) view(or flow) 
	 * and compare it to the target (MR91XML (or gvbrca) Generated data) <p>
	 * Compare the VDP/XLT/JLT <p>
	 * A pass is generated if all items match.
	*/
	private void reportTest(File mr91rpt) {
		Path absoluteSourcePath = mr91rpt.toPath().getParent();
		// Will be env/views/name/WBXML or DB or MR91DBXML or MR91XMLXML or
		// env/passes/blah
		Path relTestPath = testOutPath.relativize(absoluteSourcePath);
		Iterator<Path> tpi = relTestPath.iterator();

		Path comps = tpi.next();
		Path setName = tpi.next();
		Path viewOrPassPath = tpi.next();
		Path name = tpi.next();
		Path type = tpi.next();

		Path mr91Source = getMR91Source();
		Path mr91Trg = getMR91Target();
		//Only use the sourceType to avoid the generated duplicates
		if (mr91Source != null && mr91Trg != null && sourceType.equals(type.toString())) {

			// Do we know about this environment
			MR91TestSet mr91TestSet = testSetResultsByName.computeIfAbsent(setName.toString(),	k -> new MR91TestSet(this));
			mr91TestSet.name = setName.toString();
			TestResult tr = new TestResult();
			tr.setTestFileName(name.toString());
			tr.setSourcePath(relTestPath);
			tr.setTargetPath(relTestPath.getParent().resolve(targetType));
			Path absoluteTargetPath = absoluteSourcePath.getParent().resolve(targetType);

			mr91TestSet.addViewTestResult(tr);
			String comparison = String.format("Compare %s to %s", sourceType, targetType);
			mr91TestSet.setComparison(comparison);

			// We just have a test set... not passes or runs...
			// Want the XML use in the source - or DBVIEW?
			if (sourceType.equals("CPPMR91")) {
				// XML file in WBXMLI
				WildcardFileFilter fileFilter = new WildcardFileFilter("*.xml");
				Collection<File> xmlFiles = FileUtils.listFiles(mr91rpt.getParentFile(), fileFilter,
						TrueFileFilter.TRUE);
				for (File xml : xmlFiles) {
					Path relXML = testOutPath.relativize(xml.toPath());
					tr.addRelativeXMLInputPath(relXML);
				}
				if(absoluteSourcePath.resolve("VDP").toFile().exists() ) {
					tr.setSourceVDPBuilt();
					mr91TestSet.incrementSourceViewsGenerated();
				}
				//Only compare XLTs if VDP generated
				if(absoluteTargetPath.resolve("VDP").toFile().exists() ) {
					tr.setTargetVDPBuilt();
					mr91TestSet.incrementTargetViewsGenerated();
					logicTablesAlreadyCompared(absoluteSourcePath, mr91TestSet, tr);
					if (tr.xltPaths.size() == 0) {
						logger.atInfo().log("Report test %s", mr91rpt.toPath());
						logger.atInfo().log(comparison);
						compareLogicTables(mr91TestSet, tr);
					} else {
						// work out what the story was
					}
				}
			}
		}
	}

	private void writeHTMLReport() {
		try {
			Template template = cfg.getTemplate(TEST_MR91REPORT_FTL);
			Map<String, Object> nodeMap = new HashMap<>();
			nodeMap.put("env", environmentVariables.getEnvironmentVariables());
			nodeMap.put("cssPath", "../w3.css");
			nodeMap.put("testSetResults", testSetResultsByName.values());
	        reportHTMLFile = testOutPath.resolve("report.html");
	        logger.atInfo().log("Write to " + reportHTMLFile.toString());
			generateTemplatedOutput(template, nodeMap, reportHTMLFile);
		} catch (IOException | TemplateException e) {
            logger.atSevere().log("IO exception on HTML test set results\n%s", e.getMessage());
		}
	}
	

	public void runOverviewHTML() throws IOException, InterruptedException {
		CommandRunner cmdRunner = new CommandRunner();
		cmdRunner.run("cmd /C " + reportHTMLFile.toString(), reportHTMLFile.getParent().toFile());
	}


	private void addNote(String envName, String testName, String note)
	{
		if(testSetResultsByName.get(envName) != null) {
			MR91TestSet mr91te = testSetResultsByName.get(envName);
			TestResult tr = mr91te.viewsByName.get(testName);
			if(tr != null) {
				tr.note = note;
			}
		}
	}

	private void forcePass(String envName, String testName, String note)
	{
		testSetResultsByName.get(envName).viewsByName.get(testName).note = note;
		//How do we force a pass?
		//Each individual XLT has its own path. So need to id which one(s) and the summary
		//testEnvironmentResults.get(envName).testViewsResults.get(testName).
		//Or just have a single override flag...
		testSetResultsByName.get(envName).viewsByName.get(testName).overridePass = true;
	}


	private Path getMR91Target() {
		if(MR91CompTestEnvironment.get(COMPARISON_TARGET) == null) {
			logger.atSevere().log("No comparison target in environment variable MR91CMPTRG");
			return null;
		} else {
			targetType = MR91CompTestEnvironment.get(COMPARISON_TARGET);
			return testOutPath.resolve(targetType);
		}
	}

	private Path getMR91Source() {
		if(MR91CompTestEnvironment.get(COMPARISON_SOURCE) == null) {
			logger.atSevere().log("No comparison source in environment variable MR91CMPSRC");
			return null;
		} else {
			
			sourceType = MR91CompTestEnvironment.get(COMPARISON_SOURCE);
			return testOutPath.resolve(sourceType);
		}
	}

	private void compareFlowInfo(MR91TestSet testEnv, TestResult tr) {
		FlowComparator flowCompare = new FlowComparator();
		flowCompare.setRootPath(rootPath);
//		flowCompare.compareTextReports(testOutPath, testOutPath.resolve(tr.testPath), testOutPath.resolve(tr.targettestPath));
	}

	private void logicTablesAlreadyCompared(Path mr91Ref, MR91TestSet testEnv, TestResult tr) {
		//Grab the html files
		//Should be at least one *diff.html or *pass.html
		WildcardFileFilter fileFilter = new WildcardFileFilter("*.html");
		Collection<File> filterFiles = FileUtils.listFiles(mr91Ref.toFile(), fileFilter, TrueFileFilter.TRUE);	
		for(File f : filterFiles) {
			if(f.getName().contains("jlt")) {
				if(f.getName().contains("jltna")) {
					logger.atInfo().log("JLT N/A");			
					tr.jltPath = testOutPath.resolve(tr.getSourcePath()).resolve("jltna.html");
				} else {
					testEnv.numJLTs++;
					if(f.getName().contains("pass")) {
						tr.jltmatch = true;
						testEnv.jltMatches++;
					}
				}
			} else {
				if(f.getName().contains("pass")) {
					tr.xltPaths.add(testOutPath.relativize(f.toPath()));
				}
			}
		}
	}

	private void compareLogicTables(MR91TestSet testEnv, TestResult tr) {
		//Don't repeat... delete old results to force run
		LTComparator ltComparitor = new LTComparator();
		ltComparitor.setRootPath(rootPath);
		Path srcPath = testOutPath.resolve(tr.getSourcePath());
		Path trgPath = testOutPath.resolve(tr.getTargetPath());
		tr.xltPaths = ltComparitor.compareXLTs(cfg, testOutPath, srcPath, trgPath);
		compareJLTsIfThere(testEnv, tr, ltComparitor, srcPath, trgPath);
	}

	private void compareJLTsIfThere(MR91TestSet testEnv, TestResult tr, LTComparator ltComparitor, Path srcPath, Path trgPath) {
		if(srcPath.resolve("jltna.html").toFile().exists()) {
			logger.atInfo().log("JLT Already N/A");			
			tr.jltPath = srcPath.resolve("jltna.html");
		}
		else {
			testExistsAndCompareJLTs(testEnv, tr, ltComparitor, srcPath, trgPath);
		}
	}

	private void testExistsAndCompareJLTs(MR91TestSet testEnv, TestResult tr, LTComparator ltComparitor, Path srcPath, Path trgPath) {
		if(srcPath.resolve("JLT").toFile().exists()) {
			testEnv.numJLTs ++;
			logger.atInfo().log("JLT Processing");			
			tr.jltmatch = ltComparitor.compareJLTs(cfg, srcPath, trgPath);
			if(tr.jltmatch) {
				testEnv.jltMatches++;
			}
			tr.jltPath = ltComparitor.getJltResultPath();
		} else {
			markNoJLTAvailable(tr, srcPath);
		}
	}

	private void markNoJLTAvailable(TestResult tr, Path srcPath) {
		logger.atInfo().log("No JLT");			
		FileWriter dummyWriter;
		try {
			tr.jltPath = srcPath.resolve("jltna.html");
			dummyWriter = new FileWriter(tr.jltPath.toFile());
			dummyWriter.close();
		} catch (IOException e) {
            logger.atSevere().log("IO exception on no JLT Log file\n%s", e.getMessage());
		}
	}

	private void generateTemplatedOutput(Template temp, 
			Map<String, Object> templateModel, 
			Path target) throws IOException, TemplateException {
		FileWriter cfgWriter = new FileWriter(target.toFile());
		temp.process(templateModel, cfgWriter);
		cfgWriter.close();
	}
	
	// Externalise this
    private void addNotes() {
    	try {
			FileReader notes = new FileReader("NotesAndOverrides.txt");
	        BufferedReader reader = new BufferedReader(notes);
	        int i = 1;
	        String line;
	        while ((line = reader.readLine()) != null) {
	        	if(line.length() > 0 ) {
		        	String[] parts = line.split(",");
		        	if(parts.length > 3) {
		        		if(parts[3].trim().equalsIgnoreCase("Pass")) {
		        			forcePass(parts[0].trim(), parts[1].trim(), parts[2].trim());
		        		} else {
			        		forceJLTPass(parts[0].trim(), parts[1].trim(), parts[2].trim());
		        		}
		        	   	if(parts.length == 5) {
			        		forceJLTPass(parts[0].trim(), parts[1].trim(), parts[2].trim());
		        	   	}
		        	} else {
		        		addNote(parts[0].trim(), parts[1].trim(), parts[2].trim());
		        	}
	        	}
	        }
			reader.close();
		} catch (IOException e) {
            logger.atSevere().log("IO exception on MR91 Test HTML file close\n%s", e.getMessage());
		}
	}

	private void forceJLTPass(String envName, String testName, String note) {
		testSetResultsByName.get(envName).viewsByName.get(testName).note = note;
		testSetResultsByName.get(envName).viewsByName.get(testName).overrideJLTPass = true;
		testSetResultsByName.get(envName).jltMatches++;
    }


}

