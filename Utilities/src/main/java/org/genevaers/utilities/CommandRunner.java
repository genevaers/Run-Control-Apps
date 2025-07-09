package org.genevaers.utilities;

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


import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.logging.Logger;

import com.google.common.flogger.FluentLogger;



public class CommandRunner {
    private static final FluentLogger logger = FluentLogger.forEnclosingClass();
	private StringBuilder cmdOutput = new StringBuilder();

	public StringBuilder getCmdOutput() {
		return cmdOutput;
	}

	private class ProcessHandler extends Thread {

		InputStream inpStr;
		
		public ProcessHandler(InputStream inpStr, String strType) {
			this.inpStr = inpStr;
		}

		@Override
		public void run() {
			try (
				InputStreamReader inpStrd = new InputStreamReader(inpStr);
				BufferedReader buffRd = new BufferedReader(inpStrd); ) {
				String line = null;
				while((line = buffRd.readLine()) != null) {
					cmdOutput.append(line);
					cmdOutput.append('\n');
				}
			} catch(Exception e) {
				logger.atSevere().log("Command Runner ProcessHandler error %s", e.getMessage());
			}

		}
	}

	public int run(String cmd, File dir) throws IOException, InterruptedException {

		Process proc = Runtime.getRuntime().exec(cmd, null, dir );

		ProcessHandler inputStream =
			new ProcessHandler(proc.getInputStream(),"INPUT");
		ProcessHandler errorStream =
			new ProcessHandler(proc.getErrorStream(),"ERROR");

		/* start the stream threads */
		inputStream.start();
		errorStream.start();

		return proc.waitFor();
	}

	public void run(Process proc) throws IOException, InterruptedException {

		ProcessHandler inputStream =
			new ProcessHandler(proc.getInputStream(),"INPUT");
		ProcessHandler errorStream =
			new ProcessHandler(proc.getErrorStream(),"ERROR");

		/* start the stream threads */
		inputStream.start();
		errorStream.start();
	}

	public void clear() {
		cmdOutput.setLength(0);
	}

}
