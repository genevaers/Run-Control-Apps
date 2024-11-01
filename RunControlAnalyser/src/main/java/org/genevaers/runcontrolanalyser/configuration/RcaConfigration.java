package org.genevaers.runcontrolanalyser.configuration;

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


import org.genevaers.utilities.ConfigEntry;
import org.genevaers.utilities.GersConfigration;

import com.google.common.flogger.FluentLogger;

public class RcaConfigration extends GersConfigration {
    private static final FluentLogger logger = FluentLogger.forEnclosingClass();

    public static final String COMPARE = "COMPARE";
    public static final String COVERAGE = "COVERAGE";
 
    public static final String XLT_REPORT_DDNAME = "XLTRPT";
    public static final String JLT_REPORT_DDNAME = "JLTRPT";
    public static final String VDP_REPORT_DDNAME = "VDPRPT";
    public static final String REPORT_DDNAME = "RCARPT";

    public RcaConfigration() {
        //Map preloaded with expect names and default values
        // parmToValue.put(LOG_LEVEL, new ConfigEntry("STANDARD", false));

        // //Hidden defaults
        // parmToValue.put(PARMFILE, new ConfigEntry(DEFAULT_PARM_FILENAME, true));
        // parmToValue.put(ZOSPARMFILE, new ConfigEntry(DEFAULT_ZOSPARM_FILENAME, true));
        // // parmToValue.put(REPORT_FILE, new ConfigEntry(REPORT_FILE, true));
        // parmToValue.put(LOG_FILE, new ConfigEntry("RCALOG", true));
        // parmToValue.put(LOG_LEVEL, new ConfigEntry("STANDARD", true));
        // parmToValue.put(XLT_DDNAME, new ConfigEntry("XLT", true));
        // parmToValue.put(JLT_DDNAME, new ConfigEntry("JLT", true));
        
        // parmToValue.put(XLT_REPORT, new ConfigEntry("N", false));
        // parmToValue.put(JLT_REPORT, new ConfigEntry("N", false));
        // parmToValue.put(VDP_REPORT, new ConfigEntry("N", false));
        // parmToValue.put(COMPARE, new ConfigEntry("N", false));
        // parmToValue.put(RCA_REPORT, new ConfigEntry("N", false));
        // parmToValue.put(REPORT_FORMAT, new ConfigEntry("TXT", false));
    }

    public static boolean isValid() {
        if(isVdpReport() || isXltReport() || isJltReport()) {
            return true;
        } else {
            return false;
        }
    }

    public static String getLogFileName() {
        return getCWDPrefix() + getParm(LOG_FILE);
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
}
