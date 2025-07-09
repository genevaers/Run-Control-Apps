package org.genevaers.testframework;

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


import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.genevaers.utilities.GenevaLog;

import com.google.common.flogger.FluentLogger;

public class CommandLineHandler {
  private static final FluentLogger logger = FluentLogger.forEnclosingClass();
	public static void main(String[] args) throws  IOException, InterruptedException {
		Options options = buildCommandLineOptions();
		CommandLineParser parser = new DefaultParser();
		try {
			GenevaLog.formatConsoleLogger(CommandLineHandler.class.getName(), Level.FINE);
			CommandLine line = parser.parse( options, args );
			logJavaDetails();
			processTheCommandLine(line);
			//showHelpIfNeeded(options, showHelp);

		}
		catch( ParseException exp ) {
			logger.atSevere().log("Parsing failed.  Reason: %s", exp.getMessage() );
		}
        GenevaLog.closeLogger(CommandLineHandler.class.getName());
	}

	private static void logJavaDetails() {
		logger.atInfo().log("Java Vendor %s", System.getProperty("java.vendor"));
		logger.atInfo().log("Java Version %s", System.getProperty("java.version"));
	}

	// A default command line inteface expect the test specs in the local directory
	// We can also manually drive via a menu
	private static void processTheCommandLine(CommandLine line)  {
    	TestDriver testDriver = new TestDriver();
		if(line.hasOption("menu")) {
			PETestMenu.show(testDriver);
		} else {
			//Call the Runner that reads all of the specs
			//and runs all of the tests
			if(TestDriver.processSpecList()) {
				TestDriver.runAllTests();
				if(line.hasOption("coverage")) {
					TestDriver.generateCoverage();
				}
				TestReporter reporter = new TestReporter();
				try {
					reporter.generate();
					if(reporter.allPassed()) {
						System.exit(0);
					} else {
						System.exit(4);
					}
				} catch (Exception e) {
					logger.atSevere().log("Exception in command line process: \n%s",e.getMessage());
				}
			}
		}
	}

	private static void showHelpIfNeeded(Options options, boolean showHelp) {
		if(showHelp) {
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp( "runcontrolanalyser", options );
		}
	}

	private static Options buildCommandLineOptions() {
		Options options = new Options();

		Option help = new Option( "help", "print this message" );
		Option menu = new Option( "menu", "run-menu",  false, "run from menu");
		Option coverage = new Option( "coverage", "run-coverage",  false, "run function code coverage");

		options.addOption( help );
		options.addOption(menu);
		options.addOption(coverage);
		return options;
	}

	public static String readVersion() {
		String version = "unknown";
		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		Properties properties = new Properties();
		try (InputStream resourceStream = loader.getResourceAsStream("application.properties")) {
			properties.load(resourceStream);
            version = properties.getProperty("build.version") + " (" + properties.getProperty("build.timestamp") + ")";
		} catch (IOException e) {
			logger.atSevere().log("Exception in reading version \n%s",e.getMessage());
		}
		return version;
	}

}
