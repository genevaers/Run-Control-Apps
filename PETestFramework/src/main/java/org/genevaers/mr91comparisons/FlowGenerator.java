package org.genevaers.mr91comparisons;

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
import java.nio.file.Path;

import org.genevaers.utilities.CommandRunner;

public class FlowGenerator {

	private Path rootpath;

	public void setRootPath(Path rootPath) {
		rootpath = rootPath;
	}

	public void generateReport(Path sfoutPath, Path runHere) {
		// Call the FlowAnalyser on both the middle and youngest passes
		CommandRunner cmd = new CommandRunner();
		int rc;
		try {
			rc = cmd.run("FlowAnalyser", runHere.toFile());
			Path flowDot = runHere.resolve("FLOW.dot");
			if (rc == 0) {
				String command = "dot -Gconcentrate=true -Tsvg -O " + flowDot.toFile().getAbsolutePath();
				rc = cmd.run(command, runHere.toFile());
		        cmd.clear();
			} else {
			}
	        cmd.clear();
		} catch (IOException | InterruptedException e) {
			logger.atSevere().log("IO exception on Flow Analyser Report\n%s", e.getMessage());
		}
	}
}
