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

import com.google.common.flogger.FluentLogger;
import com.google.common.flogger.StackSize;

import org.genevaers.utilities.CommandRunner;

public class WinJavaMR91Runner implements MR91Runner {
	private static final FluentLogger logger = FluentLogger.forEnclosingClass();

	private static final String MR91_EXE = "gvbrca.bat";

    @Override
    public void runFrom(Path runDir) {
		CommandRunner cmd = new CommandRunner();
		int rc;
        try {
            rc = cmd.run(MR91_EXE, runDir.toFile());
            cmd.clear();
            if(rc >= 8) {
                logger.atSevere().log("MR91 returned %d", rc);
            }
        } catch (IOException e) {
            logger.atSevere().withStackTrace(StackSize.LARGE).log("MR91 Run failed %s", e.getMessage());
        } catch (InterruptedException e) {
            logger.atSevere().log("Interrupted %s", e.getMessage());
            Thread.currentThread().interrupt();
        }
    }
    
}
