package org.genevaers.engine;

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

public class Engine {
    private static final FluentLogger logger = FluentLogger.forEnclosingClass();
	private static Status status = Status.OK;
    private static StringBuilder header = new StringBuilder();

    public static void main(String[] args) {
		header.append(String.format("GenevaERS Perfomance Engine version %s\n", Engine.getVersion()));
		header.append(String.format("Java Vendor %s\n", System.getProperty("java.vendor")));
		header.append(String.format("Java Version %s\n", System.getProperty("java.version")));
        GersConfigration.initialise();
        if(GersConfigration.isZos()) {
            header.append(String.format("Code Page %s\n", GersConfigration.getZosCodePage()));
        }
        Engine.run();
        exitWithRC();
    } 

    public static void runFrom(String path) {
        GersConfigration.initialise();
        GersConfigration.setCurrentWorkingDirectory(path);
        run();
    }
     
    private static void run() {
        ParmReader pr = new ParmReader();
        if(pr.EngineParmExists()) {
            header.append("Reading Engine Parm\n");
            pr.populateConfigFrom(GersConfigration.getEngineParmFileName());
            GenevaLog.initLogger(Engine.class.getName(), GersConfigration.getEngineLogFileName(), GersConfigration.getLogLevel());
            GersConfigration.setLinesRead(pr.getLinesRead());
            header.append(GersConfigration.getLinesReadString());
            logger.atInfo().log(header.toString());
            PERun perun = new PERun();
            perun.execute();
        } else {
            System.out.printf("Unable to find engine parm file\n");
        }
        ReportWriter.write(status);
        String res = status == Status.OK ? "OK" : "with issues";
        logger.atInfo().log("GenevaERS Engine completed %s\n", res);
        GenevaLog.closeLogger(Engine.class.getName());
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
			e.printStackTrace();
		}
		return ver;
	}

}
