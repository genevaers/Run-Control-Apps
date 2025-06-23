package org.genevaers.utilities;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

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


import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;
import java.util.logging.Level;

import com.google.common.flogger.FluentLogger;

public class GersConfigration {
    private static final FluentLogger logger = FluentLogger.forEnclosingClass();

    private static List<String> linesRead = new ArrayList<>();

    //General Parm names
    public static final String RCA_PARM_FILENAME = "RCAPARM";

    public static final String REPORT_FILE = "RCARPT";
    public static final String LOG_FILE = "RCALOG";

    public static final String LOG_LEVEL = "LOG_LEVEL";

    public static final String XLT_DDNAME = "XLTNEW";
    public static final String JLT_DDNAME = "JLTNEW";
    public static final String VDP_DDNAME = "VDPNEW";
    public static final String XLTOLD_DDNAME = "XLTOLD";
    public static final String JLTOLD_DDNAME = "JLTOLD";
    public static final String VDPOLD_DDNAME = "VDPOLD";
    public static final String LTCOV = "LTCOV";
    
    public static final String GENERATE = "GENERATE_RC_FILES";
    public static final String WRITE_VDPXML = "GENERATE_VDPXML";
    public static final String VDPXML_OUT_FILE = "VDPXML";

    //RCG Parms
    public static final String DOT_XLT = "DOT_XLT";
    public static final String DOT_JLT = "DOT_JLT";
    public static final String VIEW_DOTS = "VIEW_DOTS";
    public static final String COLUMN_DOTS = "COLUMN_DOTS";
    private static final String PF_DOTS = "PF_DOTS";

    public static final String DBVIEWS = "DBVIEWS";
    public static final String DBFLDRS = "DBFLDRS";

    public static final String INPUT_TYPE = "INPUT_TYPE";

    public static final String DB_SCHEMA = "DB_SCHEMA";
    public static final String ENVIRONMENT_ID = "ENVIRONMENT_ID";
    public static final String DB_SUBSYSTEM = "DB_SUBSYSTEM";
    public static final String DB_SERVER = "DB_SERVER";
    public static final String DB_PORT = "DB_PORT";
    public static final String DB_DATABASE = "DB_DATABASE";
    
    public static final String RUNVIEWS = "RUNVIEWS";


    public static final String WB_XML_FILES_SOURCE = "WBXMLI";
    public static final String VDP_XML_FILES_SOURCE = "VDPXMLI";

    private static final String DOT_FORMAT = "DOT_FORMAT";

    private static final String NUMBER_MODE = "NUMBER_MODE";

    //Analyser Parms

    public static final String XLT_REPORT = "XLT_REPORT";
    public static final String JLT_REPORT = "JLT_REPORT";
    public static final String VDP_REPORT = "VDP_REPORT";
    public static final String RCA_REPORT = "RCA_REPORT";
    public static final String REPORT_FORMAT = "REPORT_FORMAT";
    
    public static final String RCA_REPORTDIR = "rca/";

    public static final String ZOS = "ZOS";
    public static final String CURRENT_WORKING_DIRECTORY = "CWD";

    public static final String COMPARE = "COMPARE";
    public static final String COVERAGE = "COVERAGE";
    public static final String AGGREGATE = "AGGREGATE";

    public static final String XLT_REPORT_DDNAME = "XLTRPT";
    public static final String JLT_REPORT_DDNAME = "JLTRPT";
    public static final String VDP_REPORT_DDNAME = "VDPRPT";
    public static final String REPORT_DDNAME = "RCARPT";

    public static final String RCA_RUNNAME = "gvbrca";
    public static final String RCA_HTMLREPORTFILENAME = RCA_REPORTDIR + RCA_RUNNAME + ".html";

    private static final String GENEVAERS = ".genevaers";
    
    protected static Map<String, ConfigEntry> parmToValue = new TreeMap<>();

    private static boolean zos;
    private static String zosCodePage;

    private static Path gersHome;

    public static void initialise() {
        clear();
		String os = System.getProperty("os.name");
		logger.atFine().log("Operating System %s", os);
		zos = os.startsWith("z");
        zosCodePage = new GersCodePage().getCodePage();
 
        parmToValue.put(GENERATE, new ConfigEntry("N", false));

        //Hidden defaults
        parmToValue.put(REPORT_FILE, new ConfigEntry(REPORT_FILE, true));
        parmToValue.put(LOG_FILE, new ConfigEntry(LOG_FILE, true));
        parmToValue.put(WB_XML_FILES_SOURCE, new ConfigEntry(WB_XML_FILES_SOURCE, true));

        parmToValue.put(CURRENT_WORKING_DIRECTORY, new ConfigEntry("", true));
        parmToValue.put(DOT_XLT, new ConfigEntry("N", true));
        parmToValue.put(DOT_JLT, new ConfigEntry("N", true));
        parmToValue.put(VIEW_DOTS, new ConfigEntry("", true));
        parmToValue.put(COLUMN_DOTS, new ConfigEntry("", true));
        parmToValue.put(PF_DOTS, new ConfigEntry("N", true));
        parmToValue.put(DOT_FORMAT, new ConfigEntry("N", true));

        parmToValue.put(GENERATE, new ConfigEntry("Y", false));
        parmToValue.put(INPUT_TYPE, new ConfigEntry("", false));

        parmToValue.put(XLT_DDNAME, new ConfigEntry("XLTNEW", true));
        parmToValue.put(JLT_DDNAME, new ConfigEntry("JLTNEW", true));
        parmToValue.put(VDP_DDNAME, new ConfigEntry("VDPNEW", true));

        parmToValue.put(LOG_LEVEL, new ConfigEntry("STANDARD", false));

        parmToValue.put(NUMBER_MODE, new ConfigEntry("STANDARD", false )); //Could be LARGE

        parmToValue.put(XLT_REPORT, new ConfigEntry("N", false));
        parmToValue.put(JLT_REPORT, new ConfigEntry("N", false));
        parmToValue.put(VDP_REPORT, new ConfigEntry("N", false));
        parmToValue.put(WRITE_VDPXML, new ConfigEntry("N", false));
        parmToValue.put(COMPARE, new ConfigEntry("N", false));
        parmToValue.put(RCA_REPORT, new ConfigEntry("N", true));
        parmToValue.put(REPORT_FORMAT, new ConfigEntry("TXT", false));
        parmToValue.put(COVERAGE, new ConfigEntry("N", false));
        parmToValue.put(AGGREGATE, new ConfigEntry("N", false));

        parmToValue.put(DB_SCHEMA, new ConfigEntry("", false));
        parmToValue.put(ENVIRONMENT_ID, new ConfigEntry("", false));
        parmToValue.put(DB_SERVER, new ConfigEntry("", false));
        parmToValue.put(DB_PORT, new ConfigEntry("", false));
        parmToValue.put(DB_DATABASE, new ConfigEntry("", false));
    }

    public static void addParmValue(String name, String value) {
        ConfigEntry pv = parmToValue.get(name.toUpperCase());
        if(pv != null) {
            pv.setValue(value);
        } else {
            logger.atWarning().log("Ignoring unexpected parameter %s=%s", name, value);
        }
    }

    public static String getValue(String parm) {
        ConfigEntry pv = parmToValue.get(parm);
        return pv != null ? pv.getValue() : "";
    }

    public static boolean isParmExpected(String name) {
        return parmToValue.get(name.toUpperCase()) != null;
    }

	public static Level getLogLevel() {
		if(parmToValue.get(LOG_LEVEL).getValue().equalsIgnoreCase("STANDARD")){
            return Level.INFO;
        } else {
            return Level.FINE;
        }
	}

    public static String getParm(String parm) {
        ConfigEntry cfe = parmToValue.get(parm);
        if(cfe != null) {
            return cfe.getValue();
        } else {
            return "";
        }
    }

    public static void setLinesRead(List<String> lr) {
        linesRead = lr;
    }

    public static List<String> getLinesRead() {
        return linesRead;
    }

    public static String getLinesReadString() {
        StringBuilder read = new StringBuilder();
        for (String string : linesRead) {
            read.append(string+"\n");
        }
        return read.toString();
    }

    public static List<String> getOptionsInEffect() {
        List<String> optsInEfect = new ArrayList<>();
        for(Entry<String, ConfigEntry> parm : parmToValue.entrySet()) {
            if(!parm.getValue().isHidden()) {
                optsInEfect.add(String.format("%-33s = %s", parm.getKey(), parm.getValue().getValue()));
            }
        };
        return optsInEfect;
    }

    public static boolean isZos() {
        return zos;
    }

    public static String getZosCodePage() {
        return zosCodePage;
    }

    public static void setCurrentWorkingDirectory(String cwd) {
        if(cwd != null) {
            parmToValue.put(CURRENT_WORKING_DIRECTORY, new ConfigEntry(cwd, true));
        } else {
            parmToValue.put(CURRENT_WORKING_DIRECTORY, new ConfigEntry(".", true));
        }
    }

    public static String getCurrentWorkingDirectory() {
        ConfigEntry cwdentry = parmToValue.get(CURRENT_WORKING_DIRECTORY);
        return cwdentry != null ? cwdentry.getValue() : "";
    }

    protected static String getCWDPrefix() {
		return getCurrentWorkingDirectory().length() > 0 ? getCurrentWorkingDirectory() + "/" : "";
    }

    public static void clear() {
        parmToValue.clear();
    }

    public static boolean analyserRunRequested() {
        boolean rcaRequested = false;
        rcaRequested |= isVdpReport();
        rcaRequested |= isXltReport();
        rcaRequested |= isJltReport();
        rcaRequested |= isRcaReport();
        rcaRequested |= isAggregate();
        return rcaRequested;
    }

    public static boolean generatorRunRequested() {
        return parmToValue.get(GENERATE).getValue().equalsIgnoreCase("Y");
    }

    public static boolean isXltReport() {
        return parmToValue.get(XLT_REPORT).getValue().equalsIgnoreCase("Y");
    }

    public static boolean isJltReport() {
        return parmToValue.get(JLT_REPORT).getValue().equalsIgnoreCase("Y");
    }

    public static boolean isVdpReport() {
        return parmToValue.get(VDP_REPORT).getValue().equalsIgnoreCase("Y");
    }

    public static boolean isRcaReport() {
        return parmToValue.get(RCA_REPORT).getValue().equalsIgnoreCase("Y");
    }

    public static boolean isCoverage() {
        return parmToValue.get(COVERAGE).getValue().equalsIgnoreCase("Y");
    }

    public static boolean isAggregate() {
        return parmToValue.get(AGGREGATE).getValue().equalsIgnoreCase("Y");
    }

    public static boolean isNumberModeStandard() {
        return parmToValue.get(NUMBER_MODE).getValue().equalsIgnoreCase("STANDARD");      
    }

    public static void set(String parm, String val) {
        ConfigEntry pv = parmToValue.get(parm);
        pv.setValue(val);
    }

   public static boolean isXltDotEnabled() {
        if( parmToValue.get(DOT_XLT) != null) {
            return parmToValue.get(DOT_XLT).getValue().equalsIgnoreCase("Y");
        } else {
            return false;
        }
    }

    public static boolean isJltDotEnabled() {
        if( parmToValue.get(DOT_JLT) != null) {
            return parmToValue.get(DOT_JLT).getValue().equalsIgnoreCase("Y");
        } else {
            return false;
        }
    }

    public static String getViewDots() {
        if( parmToValue.get(VIEW_DOTS) != null) {
            return parmToValue.get(VIEW_DOTS).getValue();
        } else {
            return "";
        }
    }

    public static String getColumnDots() {
        if( parmToValue.get(COLUMN_DOTS) != null) {
            return parmToValue.get(COLUMN_DOTS).getValue();
        } else {
            return "";
        }
    }

    public static void setDotFilter(String views, String cols, String pfs) {
        parmToValue.put(DOT_XLT, new ConfigEntry("Y", true));
        parmToValue.put(VIEW_DOTS, new ConfigEntry(views, true));
        parmToValue.put(COLUMN_DOTS, new ConfigEntry(cols, true));
        parmToValue.put(PF_DOTS, new ConfigEntry(pfs, true));
    }

    public static void setJltDotFilter(String views, String cols, String pfs) {
        parmToValue.put(DOT_JLT, new ConfigEntry("Y", true));
        parmToValue.put(VIEW_DOTS, new ConfigEntry(views, true));
        parmToValue.put(COLUMN_DOTS, new ConfigEntry(cols, true));
        parmToValue.put(PF_DOTS, new ConfigEntry(pfs, true));
    }

    public static Boolean isPFDotEnabled() {
        return parmToValue.get(PF_DOTS).getValue().equalsIgnoreCase("Y");
    }

    public static boolean isFormatDotEnabled() {
        return parmToValue.get(DOT_FORMAT).getValue().equalsIgnoreCase("Y");
    }

    public static boolean isWriteVDPXMLEnabled() {
        return parmToValue.get(WRITE_VDPXML).getValue().equalsIgnoreCase("Y");
    }

    public static String getWBXMLDirectory() {
        return getCWDPrefix() + WB_XML_FILES_SOURCE;
    }

    public static String getVDPXMLDirectory() {
        return getCWDPrefix() + VDP_XML_FILES_SOURCE;
    }

    public static String getActiveXMLDirectory() {
        switch(parmToValue.get(INPUT_TYPE).getValue()) {
            case "WBXML":
            return getWBXMLDirectory();
            case "VDPXML":
            return getVDPXMLDirectory();
            default:
            return "";
        }
    }

	public static String getParmFileName() {
        return getCWDPrefix() + RCA_PARM_FILENAME;
	}

	public static String getLogFileName() {
        return getCWDPrefix() + parmToValue.get(LOG_FILE).getValue();
	}

	public static String getReportFileName() {
        return getCWDPrefix() + parmToValue.get(REPORT_FILE).getValue();
	}

	public static String getVDPFileName() {
        return getCWDPrefix() + VDP_DDNAME;
	}

	public static String getXLTFileName() {
        return getCWDPrefix() + XLT_DDNAME;
	}

	public static String getJLTFileName() {
        return getCWDPrefix() + JLT_DDNAME;
	}

    public static boolean isCompare() {
        return parmToValue.get(COMPARE).getValue().equalsIgnoreCase("Y");
    }

    public static String getReportFormat() {
        return parmToValue.get(REPORT_FORMAT).getValue();
    }
    
    public static String getXLTReportName() {
        return isZos() ? XLT_REPORT_DDNAME : getCWDPrefix() + XLT_REPORT_DDNAME + "." +  parmToValue.get(REPORT_FORMAT).getValue().toLowerCase();
    }
    
    public static String getJLTReportName() {
        return isZos() ? JLT_REPORT_DDNAME : getCWDPrefix() + JLT_REPORT_DDNAME + "." + parmToValue.get(REPORT_FORMAT).getValue().toLowerCase();
    }
    
    public static String getVDPReportName() {
        return isZos() ? VDP_REPORT_DDNAME : getCWDPrefix() + VDP_REPORT_DDNAME + "." +  parmToValue.get(REPORT_FORMAT).getValue().toLowerCase();
    }
    
    public static String getRelativeXLTReport() {
        return isZos() ? XLT_REPORT_DDNAME : "../" + XLT_REPORT_DDNAME + "." +  parmToValue.get(REPORT_FORMAT).getValue().toLowerCase();
    }
    
    public static String getRelativeJLTReport() {
        return isZos() ? JLT_REPORT_DDNAME : "../" + JLT_REPORT_DDNAME + "." + parmToValue.get(REPORT_FORMAT).getValue().toLowerCase();
    }
    
    public static String getRelativeVDPReport() {
        return isZos() ? VDP_REPORT_DDNAME : "../" + VDP_REPORT_DDNAME + "." +  parmToValue.get(REPORT_FORMAT).getValue().toLowerCase();
    }

    public static boolean isRCGValid() {
        boolean valid = true;
        switch(parmToValue.get(INPUT_TYPE).getValue()) {
            case "WBXML":
            break;
            case "VDPXML":
            break;
            case "DB2":
            break;
            case "POSTGRES":
            break;
            default:
                valid = false;

        }
        return valid;
    }

    public static String getInputType() {
        return parmToValue.get(INPUT_TYPE).getValue();
    }

    public static boolean isRCAConfigValid() {
        if(isVdpReport() || isXltReport() || isJltReport() || isAggregate()) {
            return true;
        } else {
            return false;
        }
    }
	public static Path getGersHome() {
        if (gersHome == null) {
    		String home = System.getProperty("user.home");
	    	Path homep = Paths.get(home);
		    gersHome = homep.resolve(GENEVAERS);
        }
		return gersHome;
	}
	
	private void makeGenevaERSDirectory() {
		String home = System.getProperty("user.home");
		Path homep = Paths.get(home);
		gersHome = homep.resolve(GENEVAERS);
		gersHome.toFile().mkdirs();
	}

}
