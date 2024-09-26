package org.genevaers.genevaio.report;

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
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;
import org.genevaers.genevaio.fieldnodes.ComparisonState;
import org.genevaers.genevaio.fieldnodes.FieldNodeBase;
import org.genevaers.genevaio.fieldnodes.MetadataNode;
import org.genevaers.genevaio.fieldnodes.NumericFieldNode;
import org.genevaers.genevaio.fieldnodes.StringFieldNode;
import org.genevaers.utilities.GersFile;

import com.google.common.flogger.FluentLogger;
import com.google.common.flogger.StackSize;

public abstract class TextRecordWriter {
	private static final FluentLogger logger = FluentLogger.forEnclosingClass();
	
	protected Map<String, Boolean> ignoreTheseDiffs = new HashMap<>();
	protected int numDiffs;

	public abstract void setIgnores();
	protected abstract String getDiffKey(FieldNodeBase n);
	public abstract void writeDetails( MetadataNode recordsRoot, Writer fw, String generated) throws IOException;

	public void writeFromRecordNodes( MetadataNode recordsRoot, String filename, String generated) {
		logger.atInfo().log("Write report to %s", filename);
		try(Writer fw = new GersFile().getWriter(filename)) {
			writeDetails(recordsRoot, fw, generated);
		} catch (IOException e) {
			logger.atSevere().withCause(e).withStackTrace(StackSize.FULL);
		}
		logger.atInfo().log("Report written for %s", filename);
	}

	protected void writeField(FieldNodeBase f, Writer fw, boolean compareMode) throws IOException {
		switch(f.getFieldNodeType()) {
			case FUNCCODE:
				break;
			case METADATA:
				break;
			case NOCOMPONENT:
				break;
			case NUMBERFIELD:
			fw.write(String.format("%s    %-19s: %s\n", highlightDiff(f, compareMode), f.getName(),((NumericFieldNode) f).getValueString()));
			break;
			case RECORD:
				break;
			case RECORDPART:
				break;
			case ROOT:
				break;
			case STRINGFIELD:
			fw.write(String.format("%s    %-19s: %s\n", highlightDiff(f, compareMode), f.getName(),((StringFieldNode) f).getValue( )));
				break;
			case VIEW:
				break;
			default:
				break;

		}
		if(f.getState() == ComparisonState.DIFF) {
			numDiffs++;
		}
	}

	private String highlightDiff(FieldNodeBase f, boolean compareMode) {
		String retval = "       ";
		switch(f.getState()) {
			case DIFF:
			retval = "***    ";
			break;
			case ORIGINAL:
			if(compareMode) {
				retval = "New    ";
			}
			break;
			case NEW:
			retval = "Old    ";
			break;
			case CHANGED:
				break;
			case IGNORED:
				retval = "Ignored";
				break;
			case RECIGNORE:
				return "RCGOnly";
			case CPPONLY:
				return "CPPOnly";
			case INSTANCE:
				break;
			default:
				break;
		}
		return retval;
	}

	public int getNumDiffs() {
		return numDiffs;
	}

	public boolean diffsFound() {
		return numDiffs > 0;
	}

}
