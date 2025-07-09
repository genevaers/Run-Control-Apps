package org.genevaers.runcontrolanalyser;

import java.io.IOException;
import org.genevaers.utilities.GenevaLog;
import org.genevaers.utilities.GersConfigration;
import org.genevaers.utilities.ParmReader;
import org.genevaers.utilities.Status;

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


import com.google.common.flogger.FluentLogger;


public class RCAApp {
    private static final FluentLogger logger = FluentLogger.forEnclosingClass();
    private static Status ranOkay;
    private static String cwd = "";

    public static void  main(String[] args) {
		System.out.printf("GenevaERS Run Control Analyser %s\n", AnalyserDriver.readVersion());
		System.out.printf("Java Vendor %s\n", System.getProperty("java.vendor"));
		System.out.printf("Java Version %s\n", System.getProperty("java.version"));
        RCAApp.run();
    } 

    public static void run() {
        GenevaLog.writeHeader("Run Control Analysis");
        ranOkay = Status.OK;
        if (GersConfigration.isRCAConfigValid()) {
            ranOkay = AnalyserDriver.runFromConfig();
        } else {
             logger.atSevere().log("Invalid analysis configuration. No report requested");
        }
        if (ranOkay == Status.OK) {
            logger.atInfo().log("Run control analyser completed");
        } else {
            logger.atSevere().log("Run control analyser failed. See log for details.");
        }
    }

    public static Status ranOkay() {
        return ranOkay;
    }

    public static void setCurrentWorkingDirectory(String dir) {
        cwd = dir;
    }

}
