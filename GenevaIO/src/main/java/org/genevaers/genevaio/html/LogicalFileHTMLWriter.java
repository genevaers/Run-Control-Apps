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
import org.genevaers.repository.components.LogicalFile;
import org.genevaers.repository.components.PhysicalFile;

import com.google.common.flogger.FluentLogger;

import j2html.tags.DomContent;
import j2html.tags.specialized.DivTag;
import j2html.tags.specialized.TrTag;

public class LogicalFileHTMLWriter {
    private static final FluentLogger logger = FluentLogger.forEnclosingClass();
	
	private static final String filename = "LFs.html";
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
			logger.atSevere().log("LogicalFileHTMLWriter error\n%s", e.getMessage());
		}
	}
	
	private DivTag bodyContent() {
		return div(
						h2("Logical Files"),
						table(
								tbody(
										tr(
												th("ID"),
												th("Name"),
												th("Number of PFs"),
												th("PFs")
										  ),
										  each(Repository.getLogicalFiles().getValues(), lf -> getLFRow(lf))
							    )
							   ).withClass("w3-table w3-striped w3-border")
				).withClass("w3-container");
	}
	
	private TrTag getLFRow(LogicalFile lf) {
		return tr(
				td(Integer.valueOf(lf.getID()).toString()),
				td(lf.getName()),
				getPFs(lf),
				getPFList(lf)
				);
	}

	private DomContent getPFList(LogicalFile lf) {
		return td(a("Show List").withHref(getPFsHref(lf)),
				getList(lf));
	}
	
	private DomContent getList(LogicalFile lf) {
		return div(
				div(
					header(
							h2(
							a( i().withClass("fa fa-close")
							).withText(lf.getName() + " Physical Files").withHref(getPFsHref(lf))
							.withClass("w3-button")
							)
					).withClass("w3-green"),
					
					table(
					tbody(
							tr(
									th("ID"),
									th("Name").withClass("info"),
									th("File Type"),
									th("Access Method"),
									th("Input DD"),
									th("Output DD")
							),
							each(lf.getPFs(), pf  -> getPFRow(pf))
					)).withClass("w3-table-all w3-small")
				).withClass(POPUP)
			).withId(getListID(lf)).withStyle("display: none").withClass("w3-modal");
	}

	private DomContent getPFRow(PhysicalFile pf) {
		return tr(
				td(Integer.valueOf(pf.getComponentId()).toString()),
				td(pf.getName()),
				td(pf.getFileType().toString()),
				td(pf.getAccessMethod().toString()),
				td(pf.getInputDDName()),
				td(pf.getOutputDDName())
				);
	}


	private DomContent getPFs(LogicalFile lf) {
		return td(Integer.toString(lf.getNumberOfPFs()));
	}

	private String getPFsHref(LogicalFile lf) {
		return "javascript:toggleDiv(\""+ getListID(lf) + "\")";
	}

	private String getListID(LogicalFile lf) {
		return "LF" + Integer.toString(lf.getID());
	}

	public String getFileName() {
		return filename;
	}

}
