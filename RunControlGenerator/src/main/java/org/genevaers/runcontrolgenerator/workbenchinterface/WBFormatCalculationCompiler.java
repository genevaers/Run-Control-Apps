package org.genevaers.runcontrolgenerator.workbenchinterface;

/*
 * Copyright Contributors to the GenevaERS Project. SPDX-License-Identifier: Apache-2.0 (c) Copyright IBM Corporation 2023.
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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.antlr.v4.runtime.tree.ParseTree;
import org.genevaers.compilers.format.FormatCompiler;
import org.genevaers.compilers.format.astnodes.ColumnCalculation;
import org.genevaers.compilers.format.astnodes.FormatASTFactory;
import org.genevaers.compilers.format.astnodes.FormatBaseAST;
import org.genevaers.grammar.FormatCalculationParser.GoalContext;
import org.genevaers.repository.Repository;
import org.genevaers.repository.components.ViewColumn;
import org.genevaers.repository.data.CompilerMessage;

/*
 * Wrap the FormatRecordsBuilder for the workbench
 */
public class WBFormatCalculationCompiler implements SyntaxChecker {

	private ParseErrorListener errorListener;
	private GoalContext tree;
	private Set<Integer> columnRefs;

	private FormatCalculationListener formatListener = new FormatCalculationListener();

	public String generateCalcStack(int viewId, int colNum) {
		String cs;
		ViewColumn vc = Repository.getViews().get(viewId).getColumnNumber(colNum);
		FormatCompiler fc = new FormatCompiler();
		try {
			ColumnCalculation cc = (ColumnCalculation)FormatASTFactory.getNodeOfType(FormatASTFactory.Type.COLCALC);
			cc.addViewColumn(vc);
			FormatBaseAST ast = fc.processLogic(vc.getColumnCalculation(), false);
			cc.addChildIfNotNull(ast);
			if(fc.hasErrors()) {
				cs = "Syntax Errors found";
			} else {
				columnRefs = fc.getColumnRefs();
				cc.emit(true);
				cs = vc.getColumnCalculationStack().toString();
			}
		} catch (IOException e) {
			cs = "Unable to process column calculation";
		}
		return cs;
	}


	@Override
	public ParseTree getParseTree() {
		return tree;
	}

	@Override
	public List<String> getSyntaxErrors() {
		List<String> errs = new ArrayList<String>();
		Iterator<CompilerMessage> ei = Repository.getCompilerErrors().iterator();
		while(ei.hasNext()) {
			CompilerMessage e = ei.next();
			errs.add(e.getDetail());
		}
		return errs;
	}

	@Override
	public int getNumberOfSyntaxWarningss() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean hasSyntaxErrors() {
		return Repository.getCompilerErrors().size() > 0;
	}

    public Set<Integer> getColumnRefs() {
		return columnRefs;
   	}


}
