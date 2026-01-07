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
import static j2html.TagCreator.head;
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
import java.util.List;

import org.genevaers.testframework.functioncodecoverage.FunctionCodeHit.HITS_STATE;

import j2html.tags.ContainerTag;
import j2html.tags.specialized.TrTag;

public class ReportGenerator {

	private static final String HITS_STYLE = "text-align: right; border-style: solid; border-width: 1px;padding: 3;";

	private static final String TH_STYLE = "border-style: solid; border-width: 1px;padding: 3;";

	private static final String TABLE_STYLE = "border-style: solid; border-width: 1px; border-collapse:collapse;";

	static String toggleScript = "function toggleDiv(divname) {" +
			"var ele = document.getElementById(divname);" +
			"if (ele.style.display == \"none\") {" +
			"ele.style.display = \"block\";" +
			"}" +
			"else {" +
			"ele.style.display = \"none\";" +
			"}" +
			"}";


	public static void main(String[] args) {
		System.out.println("Coverage Report Generator");

		CoverageFile coverageFile = new CoverageFile(new File("LTCOV.json"));
		coverageFile.read();

		FileWriter testHtml;
		try {
			testHtml = new FileWriter("test.html");
			testHtml.write(
					html(
							head(
									meta().withContent("text/html; charset=UTF-8"),
									title("Function Code Coverage"),
									link().withRel("stylesheet").withType("text/css").withHref("../SAFR.css"),
									script(join(toggleScript)).withLang("Javascript")
							),
							body(
									h1("Function Code Coverage"),
									functionCodeTable(coverageFile)
							)).renderFormatted());
			testHtml.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private static ContainerTag functionCodeTable(CoverageFile coverageFile)
	{
		return table(
				tbody(
						tr(
								th("Code").withStyle(TH_STYLE),
								th("Hits").withStyle(TH_STYLE),
								th("Arguments").withStyle(TH_STYLE)),
						        each(coverageFile.getFunctionCodes(), i -> getCodeRow(i)
						)
				)).withStyle(TABLE_STYLE);
	}

	private static ContainerTag getCodeRow(FunctionCodeHit codeHits) {
		return tr(
				td(codeHits.getName()).withStyle(TH_STYLE),
				td(codeHits.getHitsData()).withStyle(HITS_STYLE),
				getArgInfo(codeHits).withStyle(HITS_STYLE)
				)
					.withCondClass(codeHits.getHitsState() == HITS_STATE.NONE, "fail")
					.withCondClass(codeHits.getHitsState() == HITS_STATE.SOME, "some")
					.withCondClass(codeHits.getHitsState() == HITS_STATE.ALL, "pass");
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
		return div(table(
				tbody(
						getMatrixHeader(codeHits).withStyle(TH_STYLE),
						getMatrixRows(codeHits).withStyle(TH_STYLE)
				)).withStyle(TABLE_STYLE)).withId(codeHits.getName()).withStyle("display: none; text-align: right; clear:both border:1px solid black");
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

	private static ContainerTag getColumnHeader(TypeHit h) {
		return th(h.getName()).withClass("info").withStyle(TH_STYLE);
	}

	private static ContainerTag getMatrixRow(List<TypeHit> row) {
		return tr(
				td(row.get(0).getName()).withClass("info").withStyle(TH_STYLE),
				each(row, r -> getTypesRow(r))
			);
	}

	private static ContainerTag getTypesRow(TypeHit r) {
		return td(new Integer(r.getHits()).toString())
				.withStyle(TH_STYLE)
				.withCondClass(r.getHits() == 0, "fail")
				.withCondClass(r.getHits() > 0, "pass");
	}

	private static ContainerTag getTypeHits(FunctionCodeHit codeHits) {
		//want its own table
		return div(table(
				tbody(
						tr(
								th("Type").withClass("info").withStyle(TH_STYLE),
								th("Hits").withClass("info").withStyle(TH_STYLE),
						        each(codeHits.getTypeHits(), i -> getTypeRow(i))
						)
				)).withStyle(TABLE_STYLE)).withId(codeHits.getName()).withStyle("display: none; text-align: right; clear:both border:1px solid black");
	}

	private static String getTypeHref(String name)
	{
		return "javascript:toggleDiv(\"" + name + "\")";
	}

	private static ContainerTag getTypeRow(TypeHit typeHits) {
		return tr(
				td(typeHits.getName()).withClass("info").withStyle(TH_STYLE),
				td(new Integer(typeHits.getHits()).toString()).withStyle(HITS_STYLE)
				.withCondClass(typeHits.getHits() == 0, "fail")
				.withCondClass(typeHits.getHits() > 0, "pass")
				);
	}

}
