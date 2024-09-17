package org.genevaers.rcapps;

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

import org.genevaers.runcontrolanalyser.RCAApp;
import org.genevaers.runcontrolgenerator.RCGApp;
import org.genevaers.runcontrolgenerator.configuration.RunControlConfigration;



public class Runner {
    private static final FluentLogger logger = FluentLogger.forEnclosingClass();
	private static Status status = Status.ERROR;

    public static void main(String[] args) {
		System.out.printf("GenevaERS RunControls version %s\n", Runner.getVersion());
		System.out.printf("Java Vendor %s\n", System.getProperty("java.vendor"));
		System.out.printf("Java Version %s\n", System.getProperty("java.version"));
        Runner.choose();
        exitWithRC();
    } 
     
    private static void choose() {
        new RunControlConfigration();
        ParmReader pr = new ParmReader();
        if(pr.generatorParmExists()) {
            System.out.printf("Running Run Control Generator\n");
            RCGApp.run("", "", RunControlConfigration.LOG_FILE, "", "", "");
            status = RCGApp.getResult();
        } else {
            System.out.printf("Unable to find generator parm file\n");
        }
        if(pr.analyserParmExists()) {
            RCAApp.run();
            status = RCAApp.ranOkay();
        } else {
            System.out.printf("Unable to find analyser arm file\n");
        }
        String res = status == Status.OK ? "OK" : "with issues";
        System.out.printf("GenevaERS RunControls completed %s\n", res);
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
            logger.atSevere().log("IO exception on Test Runner get version\n%s", e.getMessage());
		}
		return ver;
	}

}
