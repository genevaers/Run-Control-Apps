package com.ibm.safr.test.pm.messageCoverage;

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


import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.List;

import org.junit.Test;
import com.google.common.flogger.FluentLogger;

public class TestMessageCoverage {
	private static final FluentLogger logger = FluentLogger.forEnclosingClass();

	@Test
	public void testBaseFiles() {
		try {
			MessageCoverage coverage = new MessageCoverage();
			assertTrue(coverage.isInitialised());
			assertTrue(coverage.numberOfMR91BaseFiles() > 0);

			coverage.extractLogFiles("MR91LOG");
			assertTrue(coverage.numberOfLogs() > 0);
			
			coverage.getCoverage();

//			assertTrue(coverage.numberOfErrors() > 0);
//			assertTrue(coverage.numberOfWarnings() > 0);
//			assertTrue(coverage.numberOfInfos() > 0);
			
			List<String> rep1lines = coverage.report();
			for(String repLine : rep1lines) {
				System.out.println(repLine);
			}
			coverage.resetValues();

			assertTrue(coverage.numberOfErrors() == 0);
			assertTrue(coverage.numberOfWarnings() == 0);
			assertTrue(coverage.numberOfInfos() == 0);

		} catch (IOException e1) {
            logger.atSevere().log("IO exception in Test Message generation\n%s", e.getMessage());
		}
	}

	@Test
	public void testOutputLogs() {
		try {
			MessageCoverage coverage = new MessageCoverage();
			assertTrue(coverage.isInitialised());

			assertTrue(coverage.getOutputLogs("out/MR91") > 0);
			coverage.extractLogFiles("MR91LOG");
			assertTrue(coverage.numberOfLogs() > 0);

			coverage.getCoverage();

			List<String> rep2lines = coverage.report();
			//assertEquals(lines.size(), errorsCovered.numErrorEntries()+errorsCovered.unkownHits()+1);

			for(String repLine : rep2lines) {
				System.out.println(repLine);
			}
		} catch (IOException e1) {
            logger.atSevere().log("IO exception in Test Message write\n%s", e.getMessage());
		}
	}

}
