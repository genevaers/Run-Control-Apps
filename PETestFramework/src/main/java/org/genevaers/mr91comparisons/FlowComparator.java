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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import org.genevaers.utilities.CommandRunner;

public class FlowComparator {

	private Path rootpath;

	public void setRootPath(Path rootPath) {
		rootpath = rootPath;
	}

	public void compareTextReports(Path sfoutPath, Path youngestPath, Path middlePath) {
		// Call the FlowAnalyser on both the middle and youngest passes
		CommandRunner cmd = new CommandRunner();
		Path src = rootpath.resolve("bycAllscript.txt");
		Path trg = youngestPath.resolve("bycAllscript.txt");
		
		FlowGenerator flowGen = new FlowGenerator();
		flowGen.generateReport(sfoutPath, youngestPath);
		flowGen.generateReport(sfoutPath, middlePath);
		Path fsrc = middlePath.resolve("FLOWLOG");
		Path ftrg = youngestPath.resolve("FLOWLOGM");
		
		String fullcommand = "bc2 /silent @bycAllscript.txt " +  "FLOWLOG" + " " + "FLOWLOGM" + " " + "flowdiff.html";
		try {
			Files.copy(src, trg, StandardCopyOption.REPLACE_EXISTING);
			Files.copy(fsrc, ftrg, StandardCopyOption.REPLACE_EXISTING);
//			int rc = cmd.run(qcommand, xlt1.toFile());
//			if (rc != 0) {
//		        cmd.clear();
			    cmd.run(fullcommand, youngestPath.toFile());
		        cmd.clear();
		} catch (IOException e) {
			logger.atSevere().log("IO exception on compare text report\n%s", e.getMessage());
		} catch (InterruptedException e) {
			logger.atSevere().log("Interrupt exception on compare text report\n%s", e.getMessage());
		}
		
	}

}
