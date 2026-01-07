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
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import com.google.common.flogger.FluentLogger;

public abstract class IdsReaderBase {
	private static final FluentLogger logger = FluentLogger.forEnclosingClass();

	Set<Integer> ids = new TreeSet<>();
	GersConfigration rcc;
	static List<String> linesRead = new ArrayList<>();

	public enum IDS_RESULT {
		OK, WARN_IGNORED, FAIL
	}

	static IDS_RESULT result = IDS_RESULT.OK;

	public abstract Set<Integer> readIds(String parmName);

	protected void parseLines(BufferedReader parmReader) throws IOException {
		String line = parmReader.readLine();
		while (line != null) {
			// Parse the line to extract the parm name and value
			// set in the config - if expected
			parse(line);
			linesRead.add(line);
			line = parmReader.readLine();
		}
	}

	private void parse(String line) {
		if (line.length() > 0 && line.charAt(0) != '#' && line.charAt(0) != '*') {
			String[] parts = line.trim().split(" ");
			// Anything in part 2 is a comment
			String[] idStrings = parts[0].split(",");
			for (int i = 0; i < idStrings.length; i++) {
				try {
					ids.add(Integer.valueOf(idStrings[i]));
				} catch (NumberFormatException e) {
					logger.atSevere().log("%s cannot be converted to an integer", idStrings[i]);
					result = IDS_RESULT.FAIL;
				}
			}
		}

	}

	public IDS_RESULT getResult() {
		return result;
	}

	public List<String> getLinesRead() {
		return linesRead;
	}

}
