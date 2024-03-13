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


import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Stream;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.genevaers.genevaio.dataprovider.CompilerDataProvider;
import org.genevaers.grammar.GenevaERSLexer;
import org.genevaers.grammar.GenevaERSParser;
import org.genevaers.grammar.GenevaERSParser.GoalContext;
import org.genevaers.repository.Repository;
import org.genevaers.repository.components.LRField;
import org.genevaers.repository.components.LogicalRecord;
import org.genevaers.repository.components.enums.ViewType;


public class WorkbenchCompiler implements SyntaxChecker, DependencyAnalyser {

	protected WBCompilerType type;
	private GoalContext tree;
	private ExtractDependencyAnalyser dependencyAnalyser = new ExtractDependencyAnalyser();
	private ParseErrorListener errorListener;
	private CompilerDataProvider dataProvider;

	public WorkbenchCompiler() {
	}

    public String greetings() {
        return "Greetings from the Run Control Generator.\nWatch this space.";
    }
    

    public void setViewDetails(int environmentId, int viewNum, ViewType type) {
        
    }

    public void  setColumnInfo(ColumnInfo ci) {
        
    }

	public void syntaxCheckLogic(String logicText) throws IOException {
        InputStream is = new ByteArrayInputStream(logicText.getBytes());
        GenevaERSLexer lexer = new GenevaERSLexer(CharStreams.fromStream(is));
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        GenevaERSParser parser = new GenevaERSParser(tokens);
        parser.removeErrorListeners(); // remove ConsoleErrorListener
        errorListener = new ParseErrorListener();
        parser.addErrorListener(errorListener); // add ours
        tree = parser.goal(); // parse
	}

	@Override
	public ParseTree getParseTree() {
		return tree;
	}

	public void setDataProvider(CompilerDataProvider dataFromHere) {
		dataProvider = dataFromHere;
		dependencyAnalyser.setDataProvider(dataFromHere);
	}

	@Override
	public void generateDependencies() {
        ParseTreeWalker walker = new ParseTreeWalker(); // create standard walker
		dependencyAnalyser.setSycadaType(type);
        walker.walk(dependencyAnalyser, tree); // initiate walk of tree with listener		
	}

	@Override
	public boolean hasSyntaxErrors() {
		return errorListener.getErrors().size() > 0;
	}

	@Override
	public List<String> getSyntaxErrors() {
		return errorListener.getErrors();
	}

	@Override
	public int getNumberOfSyntaxWarningss() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean hasDataErrors() {
		return dependencyAnalyser.hasErrors();
	}

	@Override
	public List<String> getDataErrors() {
		return dependencyAnalyser.getErrors();
	}
	
	@Override
	public Stream<Integer> getFieldIDs() {
		return dependencyAnalyser.getFieldIDs();
	}

	public Stream<LookupRef> getLookupsStream() {
		return dependencyAnalyser.getLookupsStream();
	}

	public Set<Integer> getExitIDs() {
		return dependencyAnalyser.getExitIDs();
	}

	public Stream<Integer> getLFPFAssocIDs() {
		return dependencyAnalyser.getLFPFAssocIDs();
	}

	public void clearErrors() {
		dependencyAnalyser.getErrors().clear();
	}

	public void clearDependencies() {
		dependencyAnalyser.clear();
	}

	public String getDependenciesAsString() {
		return dependencyAnalyser.getDependenciesAsString();
	}


	@Override
	public Map<Integer, List<Integer>> getLookupIDs() {
		throw new UnsupportedOperationException("Unimplemented method 'getLookupIDs'");
	}

	@Override
	public void getSourceLr(int lrid) {
		//Add the LR to the repo
		//Don't want to know about WB transfer objects here?

		//dataProvider.g
		dependencyAnalyser.preloadCacheFromLR(lrid);
	}

	public static String getVersion() {
		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		Properties properties = new Properties();
		String ver = "";
		try (InputStream resourceStream = loader.getResourceAsStream("sycadas.properties")) {
			properties.load(resourceStream);
			ver = properties.getProperty("library.name") + ": " + properties.getProperty("build.version");
		} catch (IOException e) {
			e.printStackTrace();
		}
		return ver;
	}

	public void addLR(LogicalRecord lr) {
		Repository.addLogicalRecord(lr);
	}

	public void addField(LRField f) {
		Repository.addLRField(f);
	}
}
