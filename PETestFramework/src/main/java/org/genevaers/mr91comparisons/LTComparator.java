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


import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.genevaers.genevaio.ltfile.LTLogger;
import org.genevaers.genevaio.ltfile.LogicTable;
import org.genevaers.genevaio.ltfile.LTFileReader;
import org.genevaers.utilities.CommandRunner;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

public class LTComparator {
	
	/**
	 *
	 */
	private static final String BYCSCRIPT_TXT = "bycscript.txt";
	private static final String XLT = "XLT";
	private static final String JLT = "JLT";
	private static final String JXSRCRPT = "JavaXLTSRCRPT";
	private static final String JXTRGRPT = "JavaXLTTRGRPT";
	private static final String JJSRCRPT = "JavaJLTSRCRPT";
	private static final String JJTRGRPT = "JavaJLTTRGRPT";
	static transient Logger logger = Logger.getLogger("LTComparitor");
	
	private Path rootpath;
	private Path jltResult; 

	public void setRootPath(Path rootPath) {
		rootpath = rootPath;
	}
	
	public Path getJltResultPath() {
		return jltResult;
	}

	public List<Path> compareXLTs(Configuration cfg, Path testOutPath, Path srcxlt, Path trgxlt ) {
		List<Path> results = null;
		logger.info("Compare XLTs " +  srcxlt.toString() + " and " + trgxlt);
		try {
			//setupAndRunLTPrint(cfg, srcxlt, XLT, SRCRPT);
			runLTPrint(srcxlt, XLT, JXSRCRPT);
			runLTPrint(trgxlt, XLT, JXTRGRPT);
			//setupAndRunLTPrint(cfg, trgxlt, XLT, TRGRPT);
			moveTargetLTReportToSource(srcxlt, trgxlt, JXTRGRPT);
			results = getXLTRPTcomparisonResults(testOutPath, srcxlt, trgxlt, XLT);
		} catch (IOException | InterruptedException e) {
			logger.atSevere().log("IO exception on compare XLT report\n%s", e.getMessage());
		}
		return results;
	}

	public Boolean compareJLTs(Configuration cfg, Path jlt1, Path jlt2) {
		Boolean result = true;
		
		logger.info("Compare JLTs " +  jlt1.toString() + " and " + jlt2);
		try {
			//setupAndRunLTPrint(cfg, jlt1, JLT, SRCRPT);
			runLTPrint(jlt1, JLT, JJSRCRPT);
			runLTPrint(jlt2, JLT, JJTRGRPT);
			//setupAndRunLTPrint(cfg, jlt2, JLT, TRGRPT);
			
			Path src = jlt2.resolve(JJTRGRPT);
			Path trg = jlt1.resolve(JJTRGRPT);
			Files.copy(src, trg, StandardCopyOption.REPLACE_EXISTING);

			CommandRunner cmd = new CommandRunner();
			int rc = cmd.run("fc " + JJSRCRPT + " " + JJTRGRPT, jlt1.toFile());
			if (rc != 0) {
				result = false;
				jltResult = jlt1.resolve("jltdiff.html");
				cmd.run("WinMergeU " + JJSRCRPT + " " + JJTRGRPT + " -minimize -noninteractive -u -or jltdiff.html", jlt1.toFile());
			} else {
				File srcFile = jlt1.resolve(JJSRCRPT).toFile();
				Path res = jlt1.resolve("jltpass.txt");
				srcFile.renameTo(res.toFile());
				jltResult = res;
			}
	        cmd.clear();
		} catch (IOException e) {
			logger.atSevere().log("IO exception on compare JLT report\n%s", e.getMessage());
		}
		catch (InterruptedException e) {
			logger.atSevere().log("Interrupt exception on compare JLT report\n%s", e.getMessage());
		}
		return result;
	}

	private void moveTargetLTReportToSource(Path srcxlt, Path trgxlt, String reportName) throws IOException {
		Path src = trgxlt.resolve(reportName);
		Path trg = srcxlt.resolve(reportName);
		Files.copy(src, trg, StandardCopyOption.REPLACE_EXISTING);
	}

	private List<Path> getXLTRPTcomparisonResults(Path testOutPath, Path srcxlt, Path trgxlt, String ltType) throws IOException, InterruptedException {
		List<Path> results = new ArrayList<>();
		addTheXLTRPTcomparisonsToResults(testOutPath, srcxlt, ltType, results);
        return results;
	}

	private void addTheXLTRPTcomparisonsToResults(Path testOutPath, Path srcxlt, String ltType, List<Path> results) throws IOException, InterruptedException {
		CommandRunner cmd = new CommandRunner();
		int rc = cmd.run("fc " + JXSRCRPT + " " + JXTRGRPT, srcxlt.toFile());
		String yNameDiff = ltType + "diff.html";
		if (rc != 0) {
	        //cmd.clear();
			cmd.run("WinMergeU " + JXSRCRPT + " " + JXTRGRPT + " -minimize -noninteractive -u -or XLTDiff.html", srcxlt.toFile());
			results.add(testOutPath.relativize(srcxlt.resolve(yNameDiff)));
		} else {
			//Just rename the SRC XLT to pass.html
			File src = srcxlt.resolve(JXSRCRPT).toFile();
			Path res = srcxlt.resolve(ltType + "pass.txt");
			src.renameTo(res.toFile());
			results.add(testOutPath.relativize(res));
		}
        cmd.clear();
	}

	private void runLTPrint(Path ltPath, String type, String toName) throws IOException, InterruptedException {
		LTFileReader xltr = new LTFileReader();
		xltr.open(ltPath, type);
		LogicTable lt = xltr.makeLT();
        LTLogger.writeRecordsTo(lt, ltPath.resolve(toName).toString(), "");
	}
}
