package org.genevaers.genevaio.report;



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
import java.io.Writer;
import java.security.PublicKey;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.genevaers.genevaio.dbreader.DBFoldersReader;
import org.genevaers.genevaio.dbreader.DBViewsReader;
import org.genevaers.repository.Repository;
import org.genevaers.utilities.GersConfigration;
import org.genevaers.utilities.GersEnvironment;
import org.genevaers.utilities.GersFile;
import org.genevaers.utilities.Status;

import com.google.common.flogger.FluentLogger;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;

public class ReportWriter {

	private static final FluentLogger logger = FluentLogger.forEnclosingClass();

    private final static String REPORT_TEMPLATE = "RCApps.ftl";

    private static final String LOCALROOT = "LOCALROOT";
	private static  Configuration cfg;

    private static int jltRecordsWritten;
    private static int xltRecordsWritten;
    private static int vdpRecordsWritten;

    private static Status rcgStatus = Status.ERROR;

    private static String peVersion;

    private static String rcaVersion;

    private static String buildTimestamp;

    private static Object numVDPDiffs;

    private static Object numXLTDiffs;

    private static Object numJLTDiffs;

	public static void write(Status status){
		GersEnvironment.initialiseFromTheEnvironment();
		configureFreeMarker();
        Template template;
        try {
            readProperties();
            ClassLoader loader = Thread.currentThread().getContextClassLoader();
            Properties properties = new Properties();
            InputStream resourceStream = loader.getResourceAsStream("application.properties");
			properties.load(resourceStream);
            template = cfg.getTemplate(REPORT_TEMPLATE);
            Map<String, Object> nodeMap = new HashMap<>();
            nodeMap.put("env", "stuff");
            nodeMap.put("generate", GersConfigration.generatorRunRequested());
            nodeMap.put("analyse", GersConfigration.analyserRunRequested());
            nodeMap.put("compare", GersConfigration.isCompare());
            nodeMap.put("numVDPDiffs", numVDPDiffs);
            nodeMap.put("numXLTDiffs", numXLTDiffs);
            nodeMap.put("numJLTDiffs", numJLTDiffs);
            nodeMap.put("parmsRead", GersConfigration.getLinesRead());
            nodeMap.put("optsInEffect", GersConfigration.getOptionsInEffect());
            nodeMap.put("dbfolders", DBFoldersReader.getLinesRead());
            nodeMap.put("dbviews", DBViewsReader.getLinesRead());
            nodeMap.put("runviews", Repository.getRunviews());
            nodeMap.put("inputReports", Repository.getInputReports());
            nodeMap.put("compErrs", Repository.getCompilerErrors());
            nodeMap.put("warnings", Repository.getWarnings());
            nodeMap.put("rcaversion", rcaVersion);
            nodeMap.put("peversion", peVersion);
            nodeMap.put("buildtimestamp", buildTimestamp);
            nodeMap.put("status", status.toString());
            nodeMap.put("vdpreport", GersConfigration.isVdpReport());
            nodeMap.put("xltreport", GersConfigration.isXltReport());
            nodeMap.put("jltreport", GersConfigration.isJltReport());
            if(Repository.getCompilerErrors().isEmpty()) {
                nodeMap.put("vdpRecordsWritten", String.format("%,d", vdpRecordsWritten));
                nodeMap.put("xltRecordsWritten", String.format("%,d", xltRecordsWritten));
                nodeMap.put("jltRecordsWritten", String.format("%,d", jltRecordsWritten));
                nodeMap.put("views", Repository.getViews().getValues());
                nodeMap.put("refviews", Repository.getJoinViews().getRefReportEntries());
                nodeMap.put("reh", Repository.getViews().get(Repository.getJoinViews().getREHViewNumber()));
                nodeMap.put("rth", Repository.getViews().get(Repository.getJoinViews().getRTHViewNumber()));
                nodeMap.put("numextviews", Repository.getNumberOfExtractViews());
                nodeMap.put("numrefviews", Repository.getNumberOfReferenceViews());
            }
            logger.atInfo().log(GersConfigration.getReportFileName());
            generateTemplatedOutput(template, nodeMap, GersConfigration.getReportFileName());
        } catch (IOException e) {
            logger.atSevere().log("Report Writer error %s",e.getMessage());
        }
		logger.atConfig().log("Report Generated");
	}


	private static  void generateTemplatedOutput(Template template, Map<String, Object> nodeMap, String reportFileName) {
        try(Writer fw = new GersFile().getWriter(reportFileName)) {
	    	template.process(nodeMap, fw);
		} catch (IOException | TemplateException e) {
			logger.atSevere().log("Template generation failed %e", e.getMessage());;
		}
    }


    private  static void configureFreeMarker() {
		cfg = new Configuration(Configuration.VERSION_2_3_31);
        cfg.setClassForTemplateLoading(ReportWriter.class, "/");
        cfg.setDefaultEncoding("UTF-8");
        cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
	}


    public static void setNumJLTRecordsWritten(int numberOfRecords) {
        jltRecordsWritten = numberOfRecords;
    }


    public static void setNumXLTRecordsWritten(int numberOfRecords) {
        xltRecordsWritten = numberOfRecords;
    }


    public static void setNumVDPRecordsWritten(int numberOfRecords) {
        vdpRecordsWritten = numberOfRecords;
    }

	public static String readProperties() {
		String version = "unknown";
		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		Properties properties = new Properties();
		try (InputStream resourceStream = loader.getResourceAsStream("application.properties")) {
			properties.load(resourceStream);
            rcaVersion = "" + properties.getProperty("build.version");
            peVersion = "" + properties.getProperty("pe.version");
            buildTimestamp = "" +  properties.getProperty("buildTimestamp");
		} catch (IOException e) {
            logger.atSevere().log("Cannot readVersion %s", e.getMessage());
		}
		return version;
	}

    public static void setRCGStatus(Status s) {
        rcgStatus = s;
    }

    public static void setDiffs(int vdp, int xlt, int jlt) {
        numVDPDiffs = vdp;
        numXLTDiffs = xlt;
        numJLTDiffs = jlt;
    }
}
