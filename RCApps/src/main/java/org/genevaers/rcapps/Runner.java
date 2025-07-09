package org.genevaers.rcapps;

import org.genevaers.utilities.GenevaLog;
import org.genevaers.utilities.GersConfigration;
import org.genevaers.utilities.ParmReader;
import org.genevaers.utilities.Status;

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
import com.google.common.flogger.FluentLogger;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.genevaers.runcontrolanalyser.AnalyserDriver;
import org.genevaers.runcontrolanalyser.RCAApp;
import org.genevaers.runcontrolgenerator.RCGApp;

public class Runner {
    private static final FluentLogger logger = FluentLogger.forEnclosingClass();
	private static Status status = Status.OK;
    private static StringBuilder header = new StringBuilder();

    public static void main(String[] args) {
		header.append(String.format("GenevaERS RunControls version %s\n", Runner.getVersion()));
		header.append(String.format("Java Vendor %s\n", System.getProperty("java.vendor")));
		header.append(String.format("Java Version %s\n", System.getProperty("java.version")));
        GersConfigration.initialise();
        if(GersConfigration.isZos()) {
            header.append(String.format("Code Page %s\n", GersConfigration.getZosCodePage()));
        }
        Runner.run();
        exitWithRC();
    } 

    public static void runFrom(String path) {
        GersConfigration.initialise();
        GersConfigration.setCurrentWorkingDirectory(path);
        run();
    }
     
    private static void run() {
        ParmReader pr = new ParmReader();
        if(pr.RCAParmExists()) {
            header.append("Reading Run Control Parm\n");
            pr.populateConfigFrom(GersConfigration.getParmFileName());
            GenevaLog.initLogger(Runner.class.getName(), GersConfigration.getLogFileName(), GersConfigration.getLogLevel());
            GersConfigration.setLinesRead(pr.getLinesRead());
            header.append(GersConfigration.getLinesReadString());
            logger.atInfo().log(header.toString());
            if(GersConfigration.generatorRunRequested()) {
                RCGApp.run();
                status = RCGApp.getResult();
            }
            if(status != Status.ERROR && GersConfigration.analyserRunRequested())  {
                RCAApp.run();
                status = status == Status.OK ? RCAApp.ranOkay() : status; 
                ReportWriter.setDiffs(AnalyserDriver.getNumVDPDiffs(), AnalyserDriver.getNumXLTDiffs(), AnalyserDriver.getNumJLTDiffs());   
            }
        } else {
            System.out.printf("Unable to find generator parm file\n");
        }
        ReportWriter.write(status);
        String res = status == Status.OK ? "OK" : "with issues";
        logger.atInfo().log("GenevaERS RunControls completed %s\n", res);
        GenevaLog.closeLogger(Runner.class.getName());
    }

    private static void exitWithRC() {
        switch (status) {
            case ERROR:
                System.exit(8);
                break;
            case WARNING:
                System.exit(4);
                break;
            case DIFF:
                System.exit(1);
                break;
            default:
                System.exit(0);
                break;
        }
    }

	public static String getVersion() {
		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		Properties properties = new Properties();
		String ver = "";
		try (InputStream resourceStream = loader.getResourceAsStream("application.properties")) {
			properties.load(resourceStream);
			ver = properties.getProperty("build.version");
		} catch (IOException e) {
			logger.atSevere().log("Exception in get version \n%s", e.getMessage());
		}
		return ver;
	}

}
