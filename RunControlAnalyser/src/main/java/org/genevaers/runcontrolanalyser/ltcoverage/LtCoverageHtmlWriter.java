package org.genevaers.runcontrolanalyser.ltcoverage;

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

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.flogger.FluentLogger;

import j2html.tags.specialized.DivTag;
import j2html.tags.specialized.TableTag;
import j2html.tags.specialized.TdTag;
import j2html.tags.specialized.ThTag;
import j2html.tags.specialized.TrTag;

public class LtCoverageHtmlWriter {
    private static final FluentLogger logger = FluentLogger.forEnclosingClass();

	public enum HITS_STATE {
		NONE, SOME, ALL
	}

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

	private static List<String> typeNames = new ArrayList<>();

	public static void init() {
		typeNames.add("ALPHANUMERIC");
		typeNames.add("BCD");
		typeNames.add("BINARY1");
		typeNames.add("BINARY2");
		typeNames.add("BINARY4");
		typeNames.add("BINARY8");
		typeNames.add("BSORT");
		typeNames.add("EDITED");
		typeNames.add("MASKED");
		typeNames.add("PACKED");
		typeNames.add("PSORT");
		typeNames.add("SBINARY1");
		typeNames.add("SBINARY2");
		typeNames.add("SBINARY4");
		typeNames.add("SPACKED");
		typeNames.add("SZONED");
		typeNames.add("ZONED");
	}
	
	public void setCSSDir(String cssDir){
		SAFR_CSS = cssDir;
	}

	public static void writeTo(Path output, LTCoverageFile ltcov) {
		FileWriter testHtml;
		try {

			testHtml = new FileWriter(output.toFile());
			testHtml.write(
					html(
							head(
									meta().withContent("text/html; charset=UTF-8"),
									title(ltcov.getLtcoverage() + "Function Code Coverage"),
									link().withRel("stylesheet").withType("text/css").withHref("https://www.w3schools.com/w3css/4/w3.css"),
									link().withRel("stylesheet").withType("text/css").withHref("https://www.w3schools.com/lib/w3-colors-flat.css"),
									link().withRel("stylesheet").withType("text/css").withHref("https://cdnjs.cloudflare.com/ajax/libs/font-awesome/4.7.0/css/font-awesome.min.css"),
									script(join(toggleScript)).withLang("Javascript")),
							body(
									div(
											div(
													h1(ltcov.getLtcoverage() + " Function Code Coverage").withClass("w3-teal"),
													getSources(ltcov.getSources(), output),
													h2("Data Generated " + ltcov.getGenerationDate()).withClass("w3-green"),
													functionCodeTable(ltcov.getFunctionCodes()).withClass("w3-col l11 m12"),
											div().withClass("w3-col l1 m12")).withClass("w3-row")))
					).renderFormatted());
			testHtml.close();
		} catch (IOException e) {
            logger.atSevere().log("write coverage failed\n%s", e.getMessage());
		}
	}

	private static DivTag getSources(List<Path> list, Path output) {
		if(list.size() > 0) {
			return div(
					div(
						header(
								h2(
								a("Sources (click to toggle)").withHref(getTypeHref("Sources"))
								.withClass("w3-button")
								)
						).withClass("w3-green"),
						div(
								table(
								tbody(
										tr(th("Source")),
										each(list, e -> getSourceRow(e, output)
								)).withClass("w3-table-all w3-small")
							)
						).withId("Sources").withStyle("display: none"))
			);
		} {
			return div();
		}
	}


	private static TrTag getSourceRow(Path e, Path output) {
		String rp = output.getParent().relativize(e).toString();
		return tr(
				td(
					a(rp.toString()).withHref(rp.toString().replace("yaml", "html"))
				));
	}

	private static TableTag functionCodeTable(Map<String, LtCoverageEntry> coverageMap)
	{
		return table(
				tbody(
						tr(
								th("Code"),
								th("Description"),
								th("Hits"),
								th("Arguments")
						  ).withClass(TH_STYLE),
						  each(coverageMap.entrySet(), e -> getCodeRow(e))
			    )
			   ).withClass(TABLE_STYLE);
	}

	private static TrTag getCodeRow(Entry<String, LtCoverageEntry> codeHits) {
		return tr(
				td(codeHits.getKey()),
				td(codeHits.getValue().getDescription()),
				td(Integer.toString(codeHits.getValue().getHits())),
				getArgInfo(codeHits.getValue())
				)
					.withCondClass(getHitsState(codeHits.getValue()) == HITS_STATE.NONE, NONE)
					.withCondClass(getHitsState(codeHits.getValue()) == HITS_STATE.SOME, SOME)
					.withCondClass(getHitsState(codeHits.getValue()) == HITS_STATE.ALL, COVERED);
	}

	private static TdTag getArgInfo(LtCoverageEntry codeHits) {
		//Use expected items as an indicator of matrix
		//Single arg will have expected max @12
		int exp = codeHits.getExpectedItems();
		if (exp == 0) {
			return td("");
		}
		if (exp <= LtCoverageEntry.MAXTYPES) {
			return td(
						a("Show Types").withHref(getTypeHref(codeHits.getName())),
						getTypeHits((LTCoverageEntry1Arg)codeHits)
					);
		}
		else {
			return td(
						a("Show Type Matrix").withHref(getTypeHref(codeHits.getName())),
					  	getTypesMatrix((LTCoverageEntry2Args)codeHits)
					);
		}
	}

	private static DivTag getTypesMatrix(LTCoverageEntry2Args codeHits) {
		return 	div(
					div(
						header(
								h2(
										a(codeHits.getName() + " Type Hits").withHref(getTypeHref(codeHits.getName()))
												.withClass("w3-button")
								)
						).withClass("w3-green"),

						table(
								tbody(
										getMatrixHeader(),
										each(typeNames, t -> getMatrixRows(t, codeHits))
								).withClass("w3-table-all w3-small")
						)
					).withClass(POPUP).withStyle("width:1500px")
				).withId(codeHits.getName()).withStyle("display: none").withClass("w3-modal");
	}

	private static TrTag getMatrixRows(String rowType, LTCoverageEntry2Args codeHits) {
		return 	tr(
			td(rowType),
			each(typeNames, t -> getMatrixRow(t, codeHits.getRowForType(rowType))));
	}


	private static TdTag getMatrixRow(String ntryType, Map<String, Integer> row) {
		return td(
					getTypeEntryFromRow(ntryType, row)
				);
	}


	private static String getTypeEntryFromRow(String rowType, Map<String, Integer> row) {
		String num = "";
		if(row != null) {
			Integer hits = row.get(rowType);
			if(hits != null)
				num =  hits.toString();
		}
		return num;
	}

	private static TrTag getMatrixHeader() {
		return tr(
					getColumnHeader("Type"),
					each(typeNames, h -> getColumnHeader(h))
				);
	}

	private static ThTag getColumnHeader(String h) {
		return th(h).withClass("info");
	}

	private static DivTag getTypeHits(LTCoverageEntry1Arg codeHits) {
		return div(
					div(
						header(
								h2(
										a(codeHits.getName() + " Type Hits").withHref(getTypeHref(codeHits.getName()))
												.withClass("w3-button")))
								.withClass("w3-green"),

						table(
								tbody(
										th("Type"),
										th("Hits").withClass("info"),
										each(codeHits.getTypeHits().entrySet(), i -> getTypeRow(i)))
										.withClass("w3-table-all w3-small")
						)
					).withClass(POPUP).withStyle("width:300px")
				).withId(codeHits.getName()).withStyle("display: none").withClass("w3-modal");
	}

	private static String getTypeHref(String name) {
		return "javascript:toggleDiv(\"" + name + "\")";
	}

	private static TrTag getTypeRow(Entry<String, Integer> typeHits) {
		return tr(
				td(typeHits.getKey()).withClass("info"),
				td(Integer.toString(typeHits.getValue()))
				.withCondClass(typeHits.getValue() == 0, NONE)
				.withCondClass(typeHits.getValue() > 0, COVERED)
				);
	}

	public static HITS_STATE getHitsState(LtCoverageEntry codeHits) {
		// if args is 1 the check all relevant types have passed
		// Can we do this... will typehits have been set
		if(codeHits.getExpectedItems() == 0){
			return codeHits.getHits() > 0 ? HITS_STATE.ALL : HITS_STATE.NONE; 
		} else {
			if (codeHits.getHits() == 0)
			{
				return HITS_STATE.NONE;
			}
			else {
				return getItemsHits(codeHits) >= codeHits.getExpectedItems() ? HITS_STATE.ALL : HITS_STATE.SOME;
			}
		}
	}

	private static int getItemsHits(LtCoverageEntry codeHits) {
		int typeHits = 0;
		if(codeHits.getExpectedItems() <= LtCoverageEntry.MAXTYPES) {
			LTCoverageEntry1Arg f1 = (LTCoverageEntry1Arg)codeHits;
			Iterator<Entry<String, Integer>> thi = f1.getTypeHitsIterator();
			if(thi != null) {
				while(thi.hasNext()) {
					thi.next();
					typeHits++;
				}
			}
		}
		return typeHits;
	}


}
