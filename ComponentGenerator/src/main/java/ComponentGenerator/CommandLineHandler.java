package ComponentGenerator;

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
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

 /**
  * This is the main class for the ComponentGenerator. Just a wrapper for main, the details of the generation are controlled by the <a href="file:../../../../ComponentGenerator//src/main/resources/modelConfig.yaml">model config</a> file.
  */
  public class CommandLineHandler {

	static ModelGenerator modelGenerateor = new ModelGenerator();
	private static final String MODEL_CONFIGFILE = "../ComponentGenerator/src/main/resources/modelConfig.yaml";

    public static void main(String[] args)  {
		initLogger();
		try {
			modelGenerateor.generateFrom(MODEL_CONFIGFILE);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void initLogger() {
		Logger jdkLogger = Logger.getLogger(CommandLineHandler.class.getName());
    	jdkLogger.setUseParentHandlers(true);
		Logger topLogger = jdkLogger.getParent();
		Handler[] hndlrs = topLogger.getHandlers();
		topLogger.removeHandler(hndlrs[0]);
		topLogger.setLevel(Level.FINE);
		Handler conHdlr = new ConsoleHandler();
		conHdlr.setFormatter(new LogFormatter());
		conHdlr.setLevel(Level.CONFIG);
		topLogger.addHandler(conHdlr);
	}

}
