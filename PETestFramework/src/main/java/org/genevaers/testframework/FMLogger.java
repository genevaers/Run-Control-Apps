package org.genevaers.testframework;

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
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class FMLogger {
	static private FileHandler fileTxt;
	static private SimpleFormatter formatterTxt;
	static private ConsoleHandler conHandler;

	public FMLogger() {
		
	}
	
	static public void setup() {
		try {
			// Create Logger
			Logger logger = Logger.getLogger("");
//			logger.setLevel(Level.INFO);
			logger.setLevel(Level.FINE);
			Handler[] handlers = logger.getHandlers();
			if( handlers.length == 0) { 
				fileTxt = new FileHandler("Logging.txt");
	
				// Create txt Formatter
				formatterTxt = new SimpleFormatter();
				fileTxt.setFormatter(formatterTxt);
				logger.addHandler(fileTxt);
				
				conHandler = new ConsoleHandler();
				conHandler.setLevel(Level.WARNING);
				logger.addHandler(conHandler);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
} 
