package org.genevaers.runcontrolgenerator.compilers;

/*
 * Copyright Contributors to the GenevaERS Project.
								SPDX-License-Identifier: Apache-2.0 (c) Copyright IBM Corporation
								2008
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
import java.nio.file.Paths;
import java.util.Iterator;
import org.genevaers.compilers.base.ASTBase;
import org.genevaers.compilers.format.FormatAST2Dot;
import org.genevaers.compilers.format.FormatCompiler;
import org.genevaers.compilers.format.astnodes.ColumnCalculation;
import org.genevaers.compilers.format.astnodes.FormatASTFactory;
import org.genevaers.compilers.format.astnodes.FormatBaseAST;
import org.genevaers.compilers.format.astnodes.FormatFilterAST;
import org.genevaers.compilers.format.astnodes.FormatView;
import org.genevaers.repository.Repository;
import org.genevaers.repository.components.ViewColumn;
import org.genevaers.repository.components.ViewNode;
import org.genevaers.utilities.GersConfigration;

import com.google.common.flogger.FluentLogger;

public class FormatRecordsBuilder {
    private static final FluentLogger logger = FluentLogger.forEnclosingClass();
    
	private static FormatView currentFormatView;
	private static FormatBaseAST formatRoot;

    public static FormatBaseAST run() {
		buildFormatAST();
		emitFormatRecordsIfNeeded();
		return formatRoot;
	}

	private static void buildFormatAST() {
		formatRoot = FormatASTFactory.getNodeOfType(FormatASTFactory.Type.FORMATROOT);
		Iterator<ViewNode> vi = Repository.getViews().getIterator();
		while(vi.hasNext()){
			ViewNode v = vi.next();
			if(v.isFormat()) {
				boolean addedToFormatRequired = true;
				currentFormatView = (FormatView)FormatASTFactory.getNodeOfType(FormatASTFactory.Type.FORMATVIEW);
				currentFormatView.addView(v);
				if(v.getFormatFilterLogic() != null && v.getFormatFilterLogic().length() > 0) {
					formatRoot.addChildIfNotNull(currentFormatView);
					compileFormatFilter(v);
					addedToFormatRequired = false;
				}
				Repository.getFormatViews().add(v, v.getID(), v.getName());
				addColumnCalculations(v, addedToFormatRequired);
			}
		}
		writeFormatAstIfEnabled();
	}

	private static void writeFormatAstIfEnabled() {
		if(GersConfigration.isFormatDotEnabled()) {
        	FormatAST2Dot.write(formatRoot, Paths.get("target/Format.dot"));
		}
	}

	private static void addColumnCalculations(ViewNode v, boolean addedToFormatRequired) {
		Iterator<ViewColumn> vci = v.getColumnIterator();
		while(vci.hasNext()) {
			ViewColumn vc = vci.next();
			if(vc.getColumnCalculation() != null && vc.getColumnCalculation().length() > 0) {
				ColumnCalculation cc = (ColumnCalculation)FormatASTFactory.getNodeOfType(FormatASTFactory.Type.COLCALC);
				cc.addViewColumn(vc);
				currentFormatView.addChildIfNotNull(cc);
				if(addedToFormatRequired) {
					formatRoot.addChildIfNotNull(currentFormatView);
					addedToFormatRequired = false;
				}
				FormatCompiler fc = new FormatCompiler();
				try {
					FormatBaseAST ccast = fc.processLogic(vc.getColumnCalculation(), false);
					if(fc.hasErrors()) {
						
					} else {
						cc.addChildIfNotNull(ccast);
					}
				} catch (IOException e) {
            		logger.atSevere().log("addColumnCalculations error %s", e.getMessage());
				}
			}

		}
	}

	private static void compileFormatFilter(ViewNode view) {
		FormatCompiler ffc = new FormatCompiler();
		try {
			FormatFilterAST ff = (FormatFilterAST) ffc.processLogic(view.getFormatFilterLogic(), true);
			ff.setView(view);
			currentFormatView.addChildIfNotNull(ff);
			if(ffc.hasErrors()) {

			} else {

			}
		} catch (IOException e) {
			logger.atSevere().log("compileFormatFilter error %s", e.getMessage());
		}
	}

   	private static void emitFormatRecordsIfNeeded() {
		//Walk the format root and build the format records
		//where are the calc stacks to be defined?
		//In the view node or the view column node
		if(Repository.newErrorsDetected()) {
			logger.atSevere().log("Number of format logic errors found: %d", Repository.getCompilerErrors().size());
		} else {
			if(formatRoot != null) {
				Iterator<ASTBase> fi = formatRoot.getChildIterator();
				while(fi.hasNext()) {
					FormatBaseAST fn = (FormatBaseAST)fi.next();
					FormatBaseAST.resetOffset()	;
					fn.emit(true);
				}
			}
		}
	}

    public static FormatBaseAST getFFRoot() {
		return formatRoot;
    }

    public static void reset() {
		formatRoot = null;        
    }


}
