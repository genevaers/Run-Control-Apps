package org.genevaers.genevaio.html;

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
import static j2html.TagCreator.filter;
import static j2html.TagCreator.h2;
import static j2html.TagCreator.head;
import static j2html.TagCreator.header;
import static j2html.TagCreator.html;
import static j2html.TagCreator.i;
import static j2html.TagCreator.join;
import static j2html.TagCreator.link;
import static j2html.TagCreator.meta;
import static j2html.TagCreator.script;
import static j2html.TagCreator.table;
import static j2html.TagCreator.tbody;
import static j2html.TagCreator.td;
import static j2html.TagCreator.th;
import static j2html.TagCreator.tr;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import org.genevaers.repository.Repository;
import org.genevaers.repository.components.ViewColumn;
import org.genevaers.repository.components.ViewNode;

import com.google.common.flogger.FluentLogger;
import j2html.tags.DomContent;

public class ViewsHTMLWriter {
    private static final FluentLogger logger = FluentLogger.forEnclosingClass();
	private static final String filename = "ViewsTable.html";
	private FileWriter fw;
	private static final String POPUP = "w3-modal-content w3-animate-zoom";
	static String toggleScript = "function toggleDiv(divname) {" +
			"var ele = document.getElementById(divname);" +
			"if (ele.style.display == \"none\") {" +
			"ele.style.display = \"block\";" +
			"}" +
			"else {" +
			"ele.style.display = \"none\";" +
			"}" +
			"}";

	public void writeFromVDP(Path cwd) {

		File output = cwd.resolve(filename).toFile();
		try (FileWriter fw = new FileWriter(output);) {
			fw.write(
					html(
							head(
									meta().withContent("text/html; charset=UTF-8"),
									link().withRel("stylesheet").withType("text/css").withHref("https://www.w3schools.com/w3css/4/w3.css"),
									link().withRel("stylesheet").withType("text/css").withHref("https://www.w3schools.com/lib/w3-colors-flat.css"),
									link().withRel("stylesheet").withType("text/css").withHref("https://cdnjs.cloudflare.com/ajax/libs/font-awesome/4.7.0/css/font-awesome.min.css"),
									script(join(toggleScript)).withLang("Javascript")
							),
							body(
									bodyContent()
							)).renderFormatted());
		} catch (IOException e) {
			logger.atSevere().log("ViewsHTMLWriter writeFromVDP failed, %s", e.getMessage());
		}
	}
	
	private DomContent bodyContent() {
		return div(
						h2("Views"),
						table(
								tbody(
										tr(
												th("ID"),
												th("Name"),
												th("Type"),
												th("Output Format"),
												th("FRA"),
												th("Columns"),
												th("Sources"),
												th("Format Logic"),
												th("Details")
										  ),
										  each(Repository.getViews().getValues(), v  -> getViewRow(v))
							    )
							   ).withClass("w3-table w3-striped w3-border")
				).withClass("w3-container");
	}

	private DomContent getViewRow(ViewNode v) {
		return tr(
				td(Integer.toString(v.getID()).toString()),
				td(v.getName()),
				td(v.getViewDefinition().getViewType().toString()),
				td(v.getViewDefinition().getOutputMedia().toString()),
				td(v.getViewDefinition().isExtractSummarized() ? "True" : "False"),
				td(Integer.toString(v.getNumberOfColumns()).toString()),
				td(Integer.toString(v.getNumberOfViewSources()).toString()),
				getFormatLogic(v),
				td(getDetails(v))
				);
	}

	private DomContent getFormatLogic(ViewNode v) {
		//popup table with logic and links
		if(v.hasFormatFilterLogic()) {
			return td(a("Format Logic").withHref(getFormatHref(v)),getFormatList(v));
		} else {
			return td("none");
		}
	}

	private DomContent getFormatList(ViewNode v) {
		return div(
				div(
					header(
							h2(
							a( i().withClass("fa fa-close")
							).withText(v.getName() + " Format Logic").withHref(getFormatHref(v))
							.withClass("w3-button")
							)
					).withClass("w3-green"),
					
					table(
					tbody(
							tr(
									th("Type"),
									th("Logic Text"),
									th("Link")
							),
							getFormatFilter(v),
							each(filter(v.getColumns(), c -> c.getColumnCalculation() != null), c -> getColumnRow(c))
							//each(lr.getValuesOfFieldsByID(), fld  -> getFieldRow(fld))
					)).withClass("w3-table-all w3-small")
				)
			).withId(getListID(v)).withStyle("display: none");
	}


	private DomContent getColumnRow(ViewColumn c) {
		return tr(td(String.format("Column %03d", c.getColumnNumber())),
			td(c.getColumnCalculation()),
			td(getColumnCalcLink(c)));
	}

	private DomContent getColumnCalcLink(ViewColumn c) {
		return a("Show Details").withHref(String.format("views/VCS%07d_%03d.txt", c.getViewId(), c.getColumnNumber()));
	}

	private DomContent getFormatFilter(ViewNode v) {
		return tr(td("Format Filter"), 
				  td(v.getFormatFilterLogic()!=null ? v.getFormatFilterLogic() : "none"),
				  td(getFormalLogicLink(v)));
	}

	private DomContent getFormalLogicLink(ViewNode v) {
		return a("Show Details").withHref(String.format("views/VFF%07d.txt", v.getID()));
	}

	private String getListID(ViewNode v) {
		return "V" + Integer.toString(v.getID());
	}

	private String getFormatHref(ViewNode v) {
		return "javascript:toggleDiv(\""+ getListID(v) + "\")";
	}

	private String getFormatID(ViewNode v) {
		return "VF" + Integer.toString(v.getID());
	}

	private DomContent getDetails(ViewNode v) {
		return a("Show Details").withHref(getViewHref(v));
	}

	private String getViewHref(ViewNode v) {
		if(v.getName().contains("Ref - phase work file")) {
			return "views/RED"+ v.getID() + ".dot.svg";
		} else {
			return "views/v" + v.getID() + ".dot.svg";
		}
	}

}
