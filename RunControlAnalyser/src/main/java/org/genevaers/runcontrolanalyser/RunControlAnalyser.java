package org.genevaers.runcontrolanalyser;
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
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;

import com.google.common.flogger.FluentLogger;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FalseFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.genevaers.genevaio.dots.LookupGenerationDotWriter;
import org.genevaers.genevaio.fieldnodes.MetadataNode;
import org.genevaers.genevaio.html.RCAFrameworkHTMLWriter;
import org.genevaers.genevaio.ltfile.LogicTable;
import org.genevaers.genevaio.ltfile.LTFileReader;
import org.genevaers.genevaio.vdpfile.VDPFileReader;
import org.genevaers.genevaio.vdpfile.VDPManagementRecords;
import org.genevaers.repository.Repository;
import org.genevaers.runcontrolanalyser.configuration.RcaConfigration;
import org.genevaers.utilities.GersConfigration;
import org.genevaers.utilities.GersFile;
import org.genevaers.visualisation.GraphVizRunner;

public class RunControlAnalyser {
    private static final FluentLogger logger = FluentLogger.forEnclosingClass();

	private String htmlFileName;
	private LogicTable xlt;
	private LogicTable jlt;
	private Path trg;
	private VDPManagementRecords vmrs;
	private FlowGenerator flowGen;

	private Path viewsPath;

	public RunControlAnalyser() {
		trg = Paths.get("RunControls");
	}

	public void readVDP(Path vdpPath, String ddName, MetadataNode recordsRoot, boolean compare) {
		logger.atInfo().log("Read %s, from %s", ddName, vdpPath.toString());
		if(new GersFile().exists(vdpPath.toString())) {
			VDPFileReader vdpr = new VDPFileReader();
			vdpr.setRecordsRoot(recordsRoot);
			vdpr.setCompare(compare);
			vdpr.open(vdpPath, ddName);
			vdpr.addToRepsitory();
			vmrs = vdpr.getViewManagementRecords();
			try {
				logger.atInfo().log("Close %s", ddName);
				vdpr.close();
			} catch (IOException e) {
				logger.atSevere().log("VDP Close error %e",e.getMessage());
			}
		} else {
			logger.atSevere().log("VDP %s not found", ddName);
		}
	}

	public LogicTable readLT(Path ltPath, MetadataNode recordsRoot, boolean compare, String ddname) {
		logger.atInfo().log("Read LT %s", ltPath);
		LTFileReader ltr = new LTFileReader();
		ltr.setCompare(compare);
		ltr.setRecordsRoot(recordsRoot);
		ltr.open(ltPath, ddname);
		return ltr.makeLT();
	}

	public void writeHTML(String joinsFilter) throws IOException {
		/*
		 * We want to generate the Dot nodes for
		 * 	lookup diagrams
		 *  View diagrams
		 *  Flow diagrams
		 *  Then the html Writer can do its thing and link to the products of what we generate
		 *  It does not generate them itself
		 *  Single responsibility and all that
		 */
		
		writeViewDiagrams(xlt, joinsFilter);
		//This must follow the above which builds the flow.dot file
		writeFlowAndLookupGenerationDiagrams(jlt);
		convertDotsToSVGs(viewsPath);
		convertDotsToSVGs(trg);

		RCAFrameworkHTMLWriter htmlWriter= new RCAFrameworkHTMLWriter();
		htmlWriter.setCurrentWorkingDirectory(trg);
		htmlWriter.setFileName(htmlFileName);
		htmlWriter.setReportType(RcaConfigration.getReportFormat());
		htmlWriter.setXLTReportName(RcaConfigration.getRelativeXLTReport());
		htmlWriter.setJLTReportName(RcaConfigration.getRelativeJLTReport());
		htmlWriter.setVDPReportName(RcaConfigration.getRelativeVDPReport());
		htmlWriter.write(vmrs);
	}

	private void writeViewDiagrams(LogicTable xlt, String joinsFilter) throws IOException {
		flowGen = new FlowGenerator(vmrs, xlt);
		flowGen.setCurrentWorkingDirectory(trg);
		flowGen.setDetailJoins(joinsFilter);
		flowGen.writeDotFilesFromLT("flow.dot");
		viewsPath = trg.resolve("views");
		viewsPath.toFile().mkdirs();
	}

	private void writeFlowAndLookupGenerationDiagrams(LogicTable jlt) throws IOException {
		if(Repository.getLookups().size() > 0 && jlt != null) {
			LookupGenerationDotWriter lgw = new LookupGenerationDotWriter(jlt);
			lgw.writeREDGenerations(viewsPath);
			flowGen.setLogicTable(jlt);
			flowGen.clearView2fileMap();
			flowGen.writeDotFilesFromLT("joins.dot");
		}
	}
	
	private void convertDotsToSVGs(Path vdpPath) {
		//Iterate through all of the dot files and convert to svg
		//logger.info("Processing All dot files in: " + vdpPath.toString());
		// want to find all of the xml files under the spec
		// use the Apache beast
		WildcardFileFilter fileFilter = new WildcardFileFilter("*.dot");
		Collection<File> dotFiles = FileUtils.listFiles(vdpPath.toFile(), fileFilter, FalseFileFilter.FALSE);
		
		for(File d : dotFiles) {
	        //Need to find an iterate for these ... but
	        GraphVizRunner gvr = new GraphVizRunner();
	        gvr.processDot(d);
		}
	}

	public void setTargetDirectory(Path trgPath) {
		trg = trgPath;
	}

	public void generateFlowDataFrom(String baseFileName, String joinsFilter)
			throws Exception {
		File baseDir = new File(baseFileName);
		Path rcPath = Paths.get(baseFileName);
		htmlFileName = "gvbrca.html";
		logger.atInfo().log("Write to %s", htmlFileName);

		readVDP(rcPath.resolve(GersConfigration.VDP_DDNAME), GersConfigration.VDP_DDNAME, null, false);
		xlt = readLT(rcPath, null, false, GersConfigration.XLT_DDNAME);
		jlt = readLT(rcPath, null, false, GersConfigration.JLT_DDNAME);
		writeHTML(joinsFilter);
	}
	
}
