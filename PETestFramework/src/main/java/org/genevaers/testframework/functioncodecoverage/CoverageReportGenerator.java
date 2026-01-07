package org.genevaers.testframework.functioncodecoverage;

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
import static j2html.TagCreator.a;
import static j2html.TagCreator.body;
import static j2html.TagCreator.div;
import static j2html.TagCreator.each;
import static j2html.TagCreator.h1;
import static j2html.TagCreator.h2;
import static j2html.TagCreator.head;
import static j2html.TagCreator.header;
import static j2html.TagCreator.html;
import static j2html.TagCreator.join;
import static j2html.TagCreator.link;
import static j2html.TagCreator.meta;
import static j2html.TagCreator.script;
import static j2html.TagCreator.table;
import static j2html.TagCreator.tbody;
import static j2html.TagCreator.td;
import static j2html.TagCreator.th;
import static j2html.TagCreator.title;
import static j2html.TagCreator.tr;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.genevaers.testframework.functioncodecoverage.FunctionCodeHit.HITS_STATE;
import org.genevaers.utilities.GersEnvironment;

import j2html.tags.ContainerTag;
import j2html.tags.specialized.ThTag;
import j2html.tags.specialized.TrTag;

public class CoverageReportGenerator {

	private static final String HITS_STYLE = "text-align: right; border-style: solid; border-width: 1px;padding: 3;";

	private static final String TH_STYLE = "w3-green";
	
	private static final String POPUP = "w3-modal-content w3-animate-zoom";

	private static final String TABLE_STYLE = "w3-table w3-bordered w3-small";
	private static final String COVERED = "w3-pale-green";
	private static final String SOME = "w3-pale-yellow";
	private static final String NONE = "w3-pale-red";

	static String toggleScript = "function toggleDiv(divname) {" +
			"var ele = document.getElementById(divname);" +
			"if (ele.style.display == \"none\") {" +
			"ele.style.display = \"block\";" +
			"}" +
			"else {" +
			"ele.style.display = \"none\";" +
			"}" +
			"}";

	private static String SAFR_CSS;
	
	public static void main(String[] args) {
  
		System.out.println("Coverage Report Generator");
		File rootDir = Paths.get(GersEnvironment.get("LOCALROOT")).normalize().toFile();;
		CoverageReportGenerator generator = new CoverageReportGenerator();
 		generator.writeCoverageHTML(rootDir, "LTCov");
	}

	private File accumulationFileName=null;

	private CoverageFile coverageFile;

	public void setCSSDir(String cssDir){
		SAFR_CSS = cssDir;
	}
	
	public String writeCoverageHTML(File fromDirectory, String jsonName) {
        
        coverageFile = new CoverageFile(new File(fromDirectory, jsonName+".json"));
		if(coverageFile.read()) {
			if(accumulationFileName != null) {
				coverageFile.accumulateTo(accumulationFileName);
			}
			FileWriter testHtml;
			File output = new File(fromDirectory, jsonName+"Cov.html");
			try {
				
				testHtml = new FileWriter(output);
				testHtml.write(
						html(
								head(
										meta().withContent("text/html; charset=UTF-8"),
										title("Function Code Coverage"),
										link().withRel("stylesheet").withType("text/css").withHref(SAFR_CSS),
										script(join(toggleScript)).withLang("Javascript")
								),
								body(
										div(
												div(
														h1("Function Code Coverage").withClass("w3-teal"),
														functionCodeTable(coverageFile)
												).withClass("w3-col l10 m12"),
												div().withClass("w3-col l2 m12")
										).withClass("w3-row")
								)).renderFormatted());
				testHtml.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return output.getName();
		} else {
			return "";
		}
	}

	private static ContainerTag functionCodeTable(CoverageFile coverageFile)
	{
		return table(
				tbody(
						tr(
								th("Code"),
								th("Description"),
								th("Hits"),
								th("Arguments")
						  ).withClass(TH_STYLE),
						  each(coverageFile.getFunctionCodes(), i -> getCodeRow(i))
			    )
			   ).withClass(TABLE_STYLE);
	}

	private static ContainerTag getCodeRow(FunctionCodeHit codeHits) {
		return tr(
				td(codeHits.getName()),
				td(codeHits.getDescription()),
				td(codeHits.getHitsData()),
				getArgInfo(codeHits)
				)
					.withCondClass(codeHits.getHitsState() == HITS_STATE.NONE, NONE)
					.withCondClass(codeHits.getHitsState() == HITS_STATE.SOME, SOME)
					.withCondClass(codeHits.getHitsState() == HITS_STATE.ALL, COVERED);
	}

	private static ContainerTag getArgInfo(FunctionCodeHit codeHits) {
		int args = codeHits.getArgs();
		if (args == 0) {
			return td("");
		}
		if (args == 1) {
			return td(
						a("Show Types").withHref(getTypeHref(codeHits.getName())),
						getTypeHits(codeHits)
					);
		}
		else {
			return td(a("Show Type Matrix").withHref(getTypeHref(codeHits.getName())),
					  getTypesMatrix(codeHits)
					  );
		}
	}

	private static ContainerTag getTypesMatrix(FunctionCodeHit codeHits) {
		return div(
					div(
						header(
								h2(
								a(codeHits.getName() + " Type Hits").withHref(getTypeHref(codeHits.getName()))
								.withClass("w3-button")
								)
						).withClass("w3-green"),
						
						table(
						tbody(
								getMatrixHeader(codeHits),
								getMatrixRows(codeHits)
						)).withClass("w3-table-all w3-small")
					).withClass(POPUP).withStyle("width:1200px")
				).withId(codeHits.getName()).withStyle("display: none").withClass("w3-modal");
	}

	private static TrTag getMatrixRows(FunctionCodeHit codeHits) {
		return tr(
					each(codeHits.getTypeMatrixHits(), i -> getMatrixRow(i))
				);
	}

	private static TrTag getMatrixHeader(FunctionCodeHit codeHits) {
		List<TypeHit> header = codeHits.getTypesMatrixHeader();
		return tr(
					each(header, h -> getColumnHeader(h))
				);
	}

	private static ThTag getColumnHeader(TypeHit h) {
		return th(h.getName()).withClass("info");
	}

	private static ContainerTag getMatrixRow(List<TypeHit> row) {
		return tr(
				td(row.get(0).getName()).withClass("info"),
				each(row, r -> getTypesRow(r))
			);
	}

	private static ContainerTag getTypesRow(TypeHit r) {
		return td(new Integer(r.getHits()).toString())
				.withCondClass(r.getHits() == 0, NONE)
				.withCondClass(r.getHits() > 0, COVERED);
	}

	private static ContainerTag getTypeHits(FunctionCodeHit codeHits) {
		return div(
				div(
					header(
							h2(
							a(codeHits.getName() + " Type Hits").withHref(getTypeHref(codeHits.getName()))
							.withClass("w3-button")
							)
					).withClass("w3-green"),
					
					table(
					tbody(
							th("Type"),
							th("Hits").withClass("info"),
					        each(codeHits.getTypeHits(), i -> getTypeRow(i))
					)).withClass("w3-table-all w3-small")
				).withClass(POPUP)
			).withId(codeHits.getName()).withStyle("display: none").withClass("w3-modal");
	}

	private static String getTypeHref(String name) {
		return "javascript:toggleDiv(\"" + name + "\")";
	}

	private static ContainerTag getTypeRow(TypeHit typeHits) {
		return tr(
				td(typeHits.getName()).withClass("info"),
				td(new Integer(typeHits.getHits()).toString())
				.withCondClass(typeHits.getHits() == 0, NONE)
				.withCondClass(typeHits.getHits() > 0, COVERED)
				);
	}

	public void accumulateTo(Path overallcoverage) {
		if(overallcoverage != null) {
			accumulationFileName = overallcoverage.toFile();
		}
	}

	public void close() {
		coverageFile.close();
	}
}
