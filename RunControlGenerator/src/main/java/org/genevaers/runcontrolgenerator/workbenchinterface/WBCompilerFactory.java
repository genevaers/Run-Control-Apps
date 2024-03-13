package org.genevaers.runcontrolgenerator.workbenchinterface;

/*
 * Copyright Contributors to the GenevaERS Project. SPDX-License-Identifier: Apache-2.0 (c) Copyright IBM Corporation 2023.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License")
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


public class WBCompilerFactory {
	
	public static SyntaxChecker getProcessorFor(WBCompilerType type) {
		switch(type) {
		case EXTRACT_COLUMN:
			return new WBExtractColumnCompiler();
		case EXTRACT_FILTER:
			return new WBExtractFilterCompiler();
		case EXTRACT_OUTPUT:
			return new WBExtractOutputCompiler();
		case FORMAT_FILTER:
			return new FormatFilterSyntaxChecker();
		case FORMAT_CALCULATION:
			return new FormatCalculationSyntaxChecker();
		default:
			return null;
		}
	}

}
