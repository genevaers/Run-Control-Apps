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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.ConsoleHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import javax.swing.Box;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;

import com.google.common.flogger.FluentLogger;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;


/**
 * 
 * @author IanCunningham
 * 
 * Simply pick up input WB XML files
 *  each file constitutes a test - make them as we see fit
 *  
 * Then run it through the new MR91 
 * 	- generate all the outputs we can to see what is happening
 *  - generate outputs from the middle child version
 *  - want to compare... not sure we can automatically
 *  
 * Save the results so they can be picked up by the reporter
 */

public class Tester {

	/**
	 *
	 */
	private static final String MR91RUN_CFG = "MR91RUN.cfg";

	private static final FluentLogger logger = FluentLogger.forEnclosingClass();

	private static final String MR91PARM = "MR91PARM";
	private static final String MR91PARM_CFG = "MR91PARM.cfg";
	private static final String XMLFILES = "*.xml";
	private static final String MR91RUN = MR91RUN_CFG;
	private static final String MR91CONFIG_TEMPLATE = "cfg/TesterMR91CFG.ftl";
	private static final String MR91RUN_TEMPLATE = "cfg/TesterMR91RUN.ftl";
	private static final String RESULTS_PATH = "mr91Outputs";
	private static final String LOCALROOT = "LOCALROOT";
	private static final String INPUTS = "mr91Inputs";

	Tester() {
	}

    
	private MR91CompTestEnvironment testEnv;
	private Configuration fmcfg;
	private Path resultsPath;

	private Path inputsPath;

	private String userid;

	private String pd;

	private Path rootPath;

	private String source;

	//private List<String> xmlfiles = new ArrayList<>();

	private Path testResultsPath;

	private MR91Runner runner;

	private String mr91Type;

	
	public boolean processMR91Inputs() {
	    initLogger();
		try {
			MR91CompTestEnvironment.initialiseFromTheEnvironment();
			initFreeMarkerConfiguration();
			initialisePaths();
			readAndProcessTheInputs();
			} catch (IOException e) {
				logger.atSevere().log("IO exception on MR91 inputs\n%s", e.getMessage());
		}
		return true;
    }

	private void readAndProcessTheInputs() {
		WildcardFileFilter mr91Filter = new WildcardFileFilter(XMLFILES);
		Collection<File> xmlFiles = FileUtils.listFiles(inputsPath.toFile(), mr91Filter, TrueFileFilter.TRUE);
		logger.atInfo().log("Processing XML Files: " + inputsPath.toString());
		processXMLs(xmlFiles);
	}

	private void initialisePaths() {
		rootPath = Paths.get(MR91CompTestEnvironment.get(LOCALROOT));
		inputsPath = rootPath.resolve(INPUTS);
		resultsPath = rootPath.resolve(RESULTS_PATH);
	}

	private void processXMLs(Collection<File> xmlFiles){
		for (File xmlFile : xmlFiles) {
			Path mr91input = xmlFile.toPath().getParent();
			if(outputDoesNotExistFor(mr91input)) {
				setupAndRunMR91(mr91input);
			}
		}
	}

	private void setupAndRunMR91(Path mr91input) {
		//Get the XML files in this directory
		WildcardFileFilter mr91Filter = new WildcardFileFilter(XMLFILES);
		Collection<File> xmlFiles = FileUtils.listFiles(mr91input.toFile(), mr91Filter, TrueFileFilter.TRUE);
		try {
			run(xmlFiles);
		} catch (Exception e) {
			logger.atSevere().log("IO exception MR91 XML file reads\n%s", e.getMessage());
		}
	}

	private void run(Collection<File> xmlFiles) throws Exception {
		runner = MR91RunnerFactory.getRunner("Windows");
		Path cppMr91OutPath = testResultsPath.resolve("CPPMR91");
		xmlrun(cppMr91OutPath, xmlFiles);
		Path gvbrcaOutPath = testResultsPath.resolve("gvbrca");
		runner = MR91RunnerFactory.getRunner("Java");
		xmlrun(gvbrcaOutPath, xmlFiles);
	}

	private boolean outputDoesNotExistFor(Path src) {
		generateTestResultsPath(src);
		//return !testResultsPath.toFile().exists();
		return true;
	}

	private void generateTestResultsPath(Path src) {
		Path relRoot = inputsPath.relativize(src);
		testResultsPath = resultsPath.resolve(relRoot);
	}

	private void initFreeMarkerConfiguration() throws IOException {
		fmcfg = new Configuration(Configuration.VERSION_2_3_30);
		fmcfg.setDirectoryForTemplateLoading(new File("FreeMarkerTemplates"));
		fmcfg.setDefaultEncoding("UTF-8");
		fmcfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
	}

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

 	protected void xmlrun(Path mr91outPath, Collection<File> xmlFiles) throws Exception {
		logger.atInfo().log("Run Test: " + mr91outPath);
		setupWBXMLRun(mr91outPath, xmlFiles);
		runTest(mr91outPath);
	}

	private void runTest(Path mr91outPath) {
		runner.runFrom(mr91outPath);
	}

	private void setupWBXMLRun(Path mr91OutPath, Collection<File> xmlFiles) throws IOException, TemplateException {
		
		mr91OutPath.toFile().mkdirs();
		
    	Map<String, Object> templateModel = addTestDataToNodeModel(mr91OutPath.toString(), "WBXML");
    	Template template = fmcfg.getTemplate(MR91CONFIG_TEMPLATE);
    	Path mr91ConfigPath = mr91OutPath.resolve(MR91PARM_CFG);
		generateTestTemplatedOutput(template, templateModel, mr91ConfigPath);

    	Template vdpnltemplate = fmcfg.getTemplate("cfg/sfvdpnl.ftl");
    	Path vdpnlConfigPath = mr91OutPath.resolve("VDPNPARM");
		generateTestTemplatedOutput(vdpnltemplate, templateModel, vdpnlConfigPath);
		
		//Need to loop here on xml files
    	Path wbxmliPath = mr91OutPath.resolve("WBXMLI");
    	wbxmliPath.toFile().mkdirs();
		Iterator<File> xi = xmlFiles.iterator();
		while(xi.hasNext()) {
			File x = xi.next();
			//Path src = x);
        	Path trg = wbxmliPath.resolve(x.getName());
			Files.copy(x.toPath(), trg, StandardCopyOption.REPLACE_EXISTING);
		}
	}

	private void generateTestTemplatedOutput(Template temp, 
			Map<String, Object> templateModel, 
			Path target) throws IOException, TemplateException {
		FileWriter cfgWriter = new FileWriter(target.toFile());
		temp.process(templateModel, cfgWriter);
		cfgWriter.close();
	}
	
	private Map<String, Object> addTestDataToNodeModel(String testName, String source) {
		Map<String, Object> nodeMap = new HashMap<>();
		nodeMap.put("test", testName);
		nodeMap.put("source", source);
		return nodeMap;
	}
}
