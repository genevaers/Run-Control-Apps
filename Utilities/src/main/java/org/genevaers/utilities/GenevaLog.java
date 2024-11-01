package org.genevaers.utilities;

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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.ConsoleHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.StreamHandler;

import com.google.common.flogger.FluentLogger;

/**
 * Geneva Logger Helper
 *
 */
public class GenevaLog 
{
    private static final FluentLogger logger = FluentLogger.forEnclosingClass();

	private GenevaLog() {}

    public static void initLogger( String name, String filename, Level minLevel )
    {
        Logger jdkLogger = Logger.getLogger(name);
    	jdkLogger.setUseParentHandlers(true);
		Logger topLogger = jdkLogger.getParent();
		Handler[] hndlrs = topLogger.getHandlers();
		if(hndlrs.length == 1) { //When used by the Workbench it has two handlers leave them be
			topLogger.removeHandler(hndlrs[0]);
		}
		try {
			StreamHandler fh = new GersFile().getFileHandler(filename);
            topLogger.setLevel(minLevel);
			fh.setLevel(minLevel);
			fh.setFormatter(new Formatter() {
				public String format(LogRecord record) {
					return record.getLevel() + " : " + record.getMessage() + "\n";
				}
			});
			topLogger.addHandler(fh);
		} catch (SecurityException | IOException e) {
			logger.atSevere().log("Init logger failed", e.getMessage());
		}  
    }

	public static void initLoggerWithColours(String name, Level lvl) {
		Logger jdkLogger = Logger.getLogger(name);
		jdkLogger.setUseParentHandlers(true);
		Logger topLogger = jdkLogger.getParent();
		Handler[] hndlrs = topLogger.getHandlers();
		if (hndlrs.length > 0) {
			topLogger.removeHandler(hndlrs[0]);
			topLogger.setLevel(Level.FINE);
			Handler conHdlr = new ConsoleHandler();
			conHdlr.setFormatter(new LogFormatter());
			conHdlr.setLevel(Level.CONFIG);
			topLogger.addHandler(conHdlr);
		}
	}

	public static void initSimpleLogger(String name, Level lvl) {
		Logger jdkLogger = Logger.getLogger(name);
    	jdkLogger.setUseParentHandlers(true);
		Logger topLogger = jdkLogger.getParent();
		Handler[] hndlrs = topLogger.getHandlers();
		topLogger.removeHandler(hndlrs[0]);
		topLogger.setLevel(Level.FINE);
		Handler conHdlr = new ConsoleHandler();
		conHdlr.setFormatter(new SimpleFormatter());
		conHdlr.setLevel(Level.CONFIG);
		topLogger.addHandler(conHdlr);
	}


    public static void formatConsoleLogger( String name, Level lvl )
    {
        Logger jdkLogger = Logger.getLogger(name);
    	jdkLogger.setUseParentHandlers(true);
		Logger topLogger = jdkLogger.getParent();
		topLogger.setLevel(lvl);
		Handler[] hndlrs = topLogger.getHandlers();
		if(hndlrs.length > 0) {
			hndlrs[0].setFormatter(new Formatter() {
				public String format(LogRecord record) {
					return record.getLevel() + " : " + record.getMessage() + "\n";
				}
			});
			hndlrs[0].setLevel(lvl);
		}
    }

    public static void closeLogger( String name )
    {
        Logger jdkLogger = Logger.getLogger(name);
    	jdkLogger.setUseParentHandlers(true);
		Logger topLogger = jdkLogger.getParent();
		Handler[] hndlrs = topLogger.getHandlers();
		if(hndlrs.length > 0) {
			hndlrs[hndlrs.length-1].close();
			topLogger.removeHandler(hndlrs[hndlrs.length-1]);
		}
    }

    public static void writeHeader(String hdr) {
		String bar ="---------------------------------------------------------------------------------------";
		bar = bar.substring(0, hdr.length());
		logger.atInfo().log(bar);
		logger.atInfo().log(hdr);
		logger.atInfo().log(bar);
    }

	public static void logNow(String note) {
        DateFormat timeFormat = new SimpleDateFormat("hh:mm:ss.SSS");
        Date dt = Calendar.getInstance().getTime();
		logger.atInfo().log("%s Now %s", note, timeFormat.format(dt));
	}

}
