package org.genevaers.visualisation;

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


import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

import com.google.common.flogger.FluentLogger;

import org.genevaers.utilities.CommandRunner;

public class GraphVizRunner {
    private static final FluentLogger logger = FluentLogger.forEnclosingClass();

	public void processDot(File d) {
		CommandRunner cmd = new CommandRunner();
		//Need the absoloutePath
		//String command = "dot -Gconcentrate=true -Tsvg -O " + d.getAbsolutePath();
		String command = "dot -Tsvg -O " + d.getAbsolutePath();

        try {
			int rc = cmd.run(command, d.getParentFile());
			if(rc != 0) {
				logger.atWarning().log("Running %s returned %s", command, cmd.getCmdOutput());
			}
	        cmd.clear();
		} catch (IOException | InterruptedException e) {
            logger.atSevere().log("processDot failed\n%s", e.getMessage());
		}
	}
}
