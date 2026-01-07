package org.genevaers.runcontrolanalyser;

import java.io.File;

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


import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Properties;

import org.genevaers.genevaio.fieldnodes.MetadataNode;
import org.genevaers.genevaio.fieldnodes.Records2Dot;
import org.genevaers.genevaio.html.LTRecordsHTMLWriter;
import org.genevaers.genevaio.html.VDPRecordsHTMLWriter;
import org.genevaers.genevaio.ltfile.LTLogger;
import org.genevaers.genevaio.ltfile.LogicTable;
import org.genevaers.genevaio.ltfile.writer.LTCSVWriter;
import org.genevaers.genevaio.report.LogicTableTextWriter;
import org.genevaers.genevaio.report.VDPTextWriter;
import org.genevaers.repository.Repository;
import org.genevaers.runcontrolanalyser.ltcoverage.LTCoverageAnalyser;
import org.genevaers.utilities.GersConfigration;
import org.genevaers.utilities.Status;

import com.google.common.flogger.FluentLogger;


public class AnalyserDriver {
    private static final FluentLogger logger = FluentLogger.forEnclosingClass();

	private static RunControlAnalyser fa = new RunControlAnalyser();
	private static LTCoverageAnalyser ltCoverageAnalyser;
	private static Status status = Status.OK;

	private static String version;

	private static String generation;

	private static int numXLTDiffs;

	private static int numJLTDiffs;

	private static int numVDPDiffs;

	private boolean jlt1Present;
	private boolean jlt2Present;

	public static Status runFromConfig() {
		Path root = Paths.get(GersConfigration.getCurrentWorkingDirectory());
		ltCoverageAnalyser = new LTCoverageAnalyser();
		if (GersConfigration.isCompare()) {
			logger.atInfo().log("We are in compare mode.... best figure out how to do this");
			compareRunControlFiles(root);
		} else {
			if (GersConfigration.isXltReport()) {
				generateXltPrint(root);
			}
			if (GersConfigration.isJltReport()) {
				generateJltPrint(root);
			}
			if (GersConfigration.isVdpReport()) {
				Repository.clearAndInitialise();
				generateVdpPrint(root);
			}
			if (GersConfigration.isRcaReport()) {
				generateRcaPrint(root);
			}
			if (GersConfigration.isAggregate()) {
				ltCoverageAnalyser.aggregateCoverage();;
			}
		}
		setStatus(numVDPDiffs, numXLTDiffs, numJLTDiffs);
		return status;
	}

	private static void setStatus(int numVDPDiffs, int numXLTDiffs, int numJLTDiffs) {
		status =  numVDPDiffs > 0 || numXLTDiffs > 0 || numJLTDiffs > 0 ? Status.DIFF : Status.OK;
	}

	private static void compareRunControlFiles(Path root) {
		Path vdp1 = root.resolve(GersConfigration.VDP_DDNAME);
		Path vdp2 = root.resolve(GersConfigration.VDPOLD_DDNAME);
		Path xlt1 = root.resolve(GersConfigration.XLT_DDNAME);
		Path xlt2 = root.resolve(GersConfigration.XLTOLD_DDNAME);
		try {
			if (GersConfigration.isVdpReport()) {
				generateVDPDiffReport(root, vdp1, vdp2);
			}
			if (GersConfigration.isXltReport()) {
				generateXLTDiffReport(root, xlt1, xlt2);
			}
			if (GersConfigration.isJltReport()) {
				generateJLTDiffReport(root, xlt1, xlt2);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static boolean generateRcaPrint(Path root) {
		boolean ranOkay = true;
		try {
            setTargetDirectory(root, "rca");
            generateFlowDataFrom(GersConfigration.getCurrentWorkingDirectory(), 
             ""
            );
        } catch (Exception e) {
			ranOkay = false;
            logger.atSevere().log("Problem running the analyser. %s", e.getMessage());
        }
		return ranOkay;
	}

	public static void generateFlowDataFrom(String baseFileName, String joinsFilter) throws Exception {
		fa.generateFlowDataFrom(baseFileName, joinsFilter);
	}

	public static void setTargetDirectory(Path root, String trg) {
		Path trgPath = root.resolve(trg);
		if(trgPath.toFile().exists() == false) {
			trgPath.toFile().mkdirs();
		}
		fa.setTargetDirectory(trgPath);
	}

	public static void generateXltPrint(Path root) {
		logger.atInfo().log("Generate %s", GersConfigration.XLT_REPORT_DDNAME);
		LogicTable xlt = writeLtReport(root, GersConfigration.XLT_DDNAME, GersConfigration.getXLTReportName());
		collectCoverageDataFrom(root, xlt);
	}

	private static LogicTable writeLtReport(Path root, String ddname, String ltReportDdname) {
		LogicTable lt=null;
		switch (GersConfigration.getReportFormat()) {
			case "TEXT":
			case "TXT":
				lt = fa.readLT(root, null, false, ddname);
				LTLogger.writeRecordsTo(lt, ltReportDdname, generation);
				break;
			case "CSV":
				lt = fa.readLT(root, null, false, ddname);
				LTCSVWriter.write(lt, ltReportDdname);
				break;
			case "HTML":
				MetadataNode recordsRoot = new MetadataNode();
				recordsRoot.setName("Root");
				recordsRoot.setSource1(root.toString());
				lt = fa.readLT(root, recordsRoot, false, ddname);
				LTRecordsHTMLWriter ltrw = new LTRecordsHTMLWriter();
				ltrw.setIgnores();
				ltrw.writeFromRecordNodes(recordsRoot, ltReportDdname);
				break;
			default:
				logger.atSevere().log("Invalid or no LT report format specified.");
				break;
		}
		return lt;
	}

	private static void collectCoverageDataFrom(Path xltp, LogicTable xlt) {
		if(GersConfigration.isCoverage()) {
			ltCoverageAnalyser.addDataFrom(xltp, xlt);
			ltCoverageAnalyser.writeResults(xltp);
		}
	}

	public static void generateJltPrint(Path root) {
		logger.atInfo().log("Generate %s", GersConfigration.JLT_REPORT_DDNAME);
		Path jltp = root.resolve(GersConfigration.JLT_DDNAME);
		if(GersConfigration.isZos() || jltp.toFile().exists()) {
			writeLtReport(root, GersConfigration.JLT_DDNAME, GersConfigration.getJLTReportName());
		}
    }

	public static void generateVdpPrint(Path root) {
		logger.atInfo().log("Generate %s", GersConfigration.getVDPReportName());
		MetadataNode recordsRoot = new MetadataNode();
		recordsRoot.setName("Root");
		Path vdpp = root.resolve(GersConfigration.VDP_DDNAME);
		fa.readVDP(vdpp, GersConfigration.VDP_DDNAME, recordsRoot, false);
		writeVDPReport(recordsRoot, GersConfigration.getVDPReportName());
		//collectCoverageDataFrom(xltp, xlt);
	}

	private static void writeVDPReport(MetadataNode recordsRoot, String vdpReportDdname) {
		switch (GersConfigration.getReportFormat()) {
			case "TEXT":
			case "TXT":
				VDPTextWriter vdptw = new VDPTextWriter();
				vdptw.writeFromRecordNodes(recordsRoot, vdpReportDdname, generation);
				break;
			case "CSV":
				break;
			case "HTML":
				VDPRecordsHTMLWriter vdprw = new VDPRecordsHTMLWriter();
				vdprw.setIgnores();
				vdprw.writeFromRecordNodes(recordsRoot, GersConfigration.getVDPReportName());					
				break;
		
			default:
				break;
		}
	}

	public void writeCoverageResults(Path root) {
		ltCoverageAnalyser.setName(root.getFileName());
		ltCoverageAnalyser.writeResults(root.resolve("rca"));
	}

	public void aggregateLtCoverage() {
		ltCoverageAnalyser.aggregateCoverage();
	}

	private static void generateJLTDiffReport(Path root, Path rc1, Path rc2) {
		MetadataNode recordsRoot = new MetadataNode();
		recordsRoot.setName("Compare");
		recordsRoot.setSource1(GersConfigration.JLT_DDNAME.toString());
		recordsRoot.setSource2(GersConfigration.JLTOLD_DDNAME.toString());
		fa.readLT(root, recordsRoot, false, GersConfigration.JLT_DDNAME);
		logger.atInfo().log("JLT Tree built from %s", rc1.toString());
		//Records2Dot.write(recordsRoot, root.resolve("JLT1records.gv"));
		fa.readLT(root, recordsRoot, true, GersConfigration.JLTOLD_DDNAME);
		logger.atInfo().log("JLT Tree added to from %s", rc2.toString());
		//Records2Dot.write(recordsRoot, root.resolve("JLTrecords.gv"));
		switch (GersConfigration.getReportFormat()) {
			case "TXT":
			case "TEXT":
				LogicTableTextWriter lttw = new LogicTableTextWriter();
				lttw.writeFromRecordNodes(recordsRoot, GersConfigration.getJLTReportName(), generation);
				numJLTDiffs = lttw.getNumDiffs();
				break;
			case "HTML":
				LTRecordsHTMLWriter ltrw = new LTRecordsHTMLWriter();
				ltrw.setIgnores();
				ltrw.writeFromRecordNodes(recordsRoot, GersConfigration.getJLTReportName());
				break;
			default:
				logger.atSevere().log("Invalid or no JLT report format specified.");
				break;
		}
	}

	private static void generateXLTDiffReport(Path root, Path rc1, Path rc2) {
		MetadataNode recordsRoot = new MetadataNode();
		recordsRoot.setName("Compare");
		recordsRoot.setSource1(GersConfigration.XLT_DDNAME.toString());
		recordsRoot.setSource2(GersConfigration.XLTOLD_DDNAME.toString());
		fa.readLT(root, recordsRoot, false, GersConfigration.XLT_DDNAME);
		logger.atInfo().log("XLT Tree built from %s", rc1.toString());
		Records2Dot.write(recordsRoot, root.resolve("xlt1records.gv"));
		fa.readLT(root, recordsRoot, true, GersConfigration.XLTOLD_DDNAME);
		logger.atInfo().log("XLT Tree added to from %s", rc2.toString());
		// Records2Dot.write(recordsRoot, root.resolve("xltrecords.gv"));
		switch (GersConfigration.getReportFormat()) {
			case "TXT":
			case "TEXT":
				LogicTableTextWriter lttw = new LogicTableTextWriter();
				lttw.writeFromRecordNodes(recordsRoot, GersConfigration.getXLTReportName(), generation);
				numXLTDiffs = lttw.getNumDiffs();
				break;
			case "HTML":
				LTRecordsHTMLWriter ltrw = new LTRecordsHTMLWriter();
				ltrw.setIgnores();
				ltrw.writeFromRecordNodes(recordsRoot, GersConfigration.getXLTReportName());
				break;
		}
	}

	private static void generateVDPDiffReport(Path root, Path rc1, Path rc2) throws Exception {
		MetadataNode recordsRoot = new MetadataNode();
		recordsRoot.setName("Compare");
		recordsRoot.setSource1(GersConfigration.VDP_DDNAME.toString());
		recordsRoot.setSource2(GersConfigration.VDPOLD_DDNAME.toString());
		Path vdp1p = root.resolve(GersConfigration.VDP_DDNAME);
		fa.readVDP(vdp1p, GersConfigration.VDP_DDNAME, recordsRoot, false);
		logger.atInfo().log("VDP Tree built from %s", rc1.toString());
		VDPRecordsHTMLWriter vdprw = new VDPRecordsHTMLWriter();
		vdprw.setIgnores();
		//vdprw.writeFromRecordNodes(recordsRoot, "VDP1.html");
		//Records2Dot.write(recordsRoot, root.resolve("records1.gv"));
		Path vdp2p = root.resolve(GersConfigration.VDPOLD_DDNAME);
		fa.readVDP(vdp2p, GersConfigration.VDPOLD_DDNAME, recordsRoot, true);
		logger.atInfo().log("VDP Tree added to from %s", rc2.toString());
		//Records2Dot.write(recordsRoot, root.resolve("records.gv"));
		switch(GersConfigration.getReportFormat()) {
			case "TXT":
			case "TEXT":
			VDPTextWriter vdptw = new VDPTextWriter();
			vdptw.writeFromRecordNodes(recordsRoot, GersConfigration.getVDPReportName(), generation);
			numVDPDiffs = vdptw.getNumDiffs();
			break;
			case "HTML":
			vdprw.writeFromRecordNodes(recordsRoot, GersConfigration.getVDPReportName());
			break;
		}		
		logger.atInfo().log("VDP Diff Completed");
	}

	private boolean runControlFilesPresent(Path root) {
		boolean allPresent = false;
		Path rc1 = root.resolve("RC1");
		Path rc2 = root.resolve("RC2");
		if(rcFilesPresent(rc1) && rcFilesPresent(rc2)) {
			logger.atInfo().log("Run control files found");
			jlt1Present = checkJLTPresent(rc1, "RC1");
			jlt2Present = checkJLTPresent(rc1, "RC2");
			allPresent = true;
		} else {
			logger.atSevere().log("Not all run control files are present.\nNeed subdirectories RC1 and RC2 to have VDP, XLT amd JLT files.");
		}
		return allPresent;
	}

	private boolean checkJLTPresent(Path rc, String name) {
		boolean present = false;
		Path jltPath = rc.resolve("JLT");
		if(jltPath.toFile().exists()) {
			logger.atInfo().log("JLT present %s", name);
			present = true;
		} else {
			logger.atInfo().log("No JLT for %s", name);
		};
		return present;
	}

	private boolean rcFilesPresent(Path rc) {
		Path vdpPath = rc.resolve(GersConfigration.VDP_DDNAME);
		Path xltPath = rc.resolve(GersConfigration.XLT_DDNAME);
		return vdpPath.toFile().exists() &&	xltPath.toFile().exists();
	}

	public static String readVersion() {
		version = "unknown";
		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		Properties properties = new Properties();
		try (InputStream resourceStream = loader.getResourceAsStream("application.properties")) {
			properties.load(resourceStream);
            version = properties.getProperty("build.version") + " (" + properties.getProperty("build.timestamp") + ")";
		} catch (IOException e) {
			logger.atSevere().log("Cannot read application.properties\n,%s", e.getMessage());
		}
    	String formattedDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
		generation = String.format("Generated %s version %s",formattedDate, version);
		return generation;
	}

	public static int getNumVDPDiffs() {
		return numVDPDiffs;
	}

	public static int getNumXLTDiffs() {
		return numXLTDiffs;
	}

	public static int getNumJLTDiffs() {
		return numJLTDiffs;
	}
}
