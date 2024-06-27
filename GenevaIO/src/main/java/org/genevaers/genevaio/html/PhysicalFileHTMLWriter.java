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
import org.genevaers.repository.components.PhysicalFile;
import org.genevaers.repository.components.UserExit;
import org.genevaers.repository.components.enums.AccessMethod;

import com.google.common.flogger.FluentLogger;

import j2html.tags.DomContent;
import j2html.tags.specialized.DivTag;
import j2html.tags.specialized.TrTag;

public class PhysicalFileHTMLWriter {
	private static final FluentLogger logger = FluentLogger.forEnclosingClass();

	private static final String filename = "PFs.html";
	private static final String POPUP = "w3-modal-content w3-animate-zoom";
	static String toggleScript = "function toggleDiv(divname) {" + "var ele = document.getElementById(divname);"
			+ "if (ele.style.display == \"none\") {" + "ele.style.display = \"block\";" + "}" + "else {"
			+ "ele.style.display = \"none\";" + "}" + "}";

	public void writeFromVDP(Path cwd) {

		File output = cwd.resolve(filename).toFile();
		try (FileWriter fw = new FileWriter(output);){
			fw.write(html(head(meta().withContent("text/html; charset=UTF-8"),
					link().withRel("stylesheet").withType("text/css")
							.withHref("https://www.w3schools.com/w3css/4/w3.css"),
					link().withRel("stylesheet").withType("text/css")
							.withHref("https://www.w3schools.com/lib/w3-colors-flat.css"),
					link().withRel("stylesheet").withType("text/css").withHref(
							"https://cdnjs.cloudflare.com/ajax/libs/font-awesome/4.7.0/css/font-awesome.min.css"),
					script(join(toggleScript)).withLang("Javascript")), body(bodyContent())).renderFormatted());
		} catch (IOException e) {
			logger.atSevere().log("PhysicalFileHTMLWriter failed %s", e.getMessage());
		}
	}

	private DivTag bodyContent() {
		return div(h2("Physical Files"),
				table(tbody(
						tr(th("ID"), th("Name"), th("File Type"), th("Access Method"), th("Details"), th("Input DD"),
								th("Output DD"), th("Read Exit ID"), th("Read Parm")),
						each(Repository.getPhysicalFiles().getValues(), pf -> getPFRow(pf)))).withClass("w3-table w3-striped w3-border"))
								.withClass("w3-container");
	}

	private TrTag getPFRow(PhysicalFile pf) {
		return tr(
				td(Integer.toString(pf.getComponentId())),
				td(pf.getName()),
				td(pf.getFileType().toString()),
				td(pf.getAccessMethod().toString()),
				getDetails(pf),
				td(pf.getInputDDName()),
				td(pf.getOutputDDName()),
				getExitDetails(pf),
				td(pf.getReadExitIDParm())
				);
	}

	private DomContent getDetails(PhysicalFile pf) {
		if (pf.getAccessMethod() == AccessMethod.DB2SQ || pf.getAccessMethod() == AccessMethod.DB2VSAM) {
			return td(a("Database Info").withHref(getDetailsHref(pf)), getDatabaseInfo(pf));
		} else {
			return td(a("File Info").withHref(getDetailsHref(pf)), getFileInfo(pf));
		}
	}

	private DomContent getExitDetails(PhysicalFile pf) {
		if (pf.getReadExit() != null) {
			UserExit ex = pf.getReadExit();
			return td(
						a(ex.getName()).withHref(getExitHref(ex)),
						getExitInfo(ex)
					);
		}
		else {
			return td("");
		}
	}
	
	private String getExitHref(UserExit ex) {
		return "javascript:toggleDiv(\""+ getExitID(ex) + "\")";
	}

	private String getExitID(UserExit ex) {
		return "EX" + Integer.toString(ex.getComponentId());
	}

	private DomContent getExitInfo(UserExit ex) {
		return
				div(
				div(
					header(
							h2(
							a( i().withClass("fa fa-close")
							).withText(ex.getName() + " Info").withHref(getExitHref(ex))
							.withClass("w3-button")
							)
					).withClass("w3-green"),
					table(
							tbody(
								tr(
											th("ID"),
											th("Name"),
											th("Type"),
											th("Optimized"),
											th("Language"),
											th("Executable")
									  ),
								tr(
											td(Integer.toString(ex.getComponentId())),
											td(ex.getName()),
											td(ex.getExitType().toString()),
											td(ex.isOptimizable() ? "True" : "False"),
											td(ex.getProgramType().toString()),
											td(ex.getExecutable())
								))
							).withClass("w3-table-all w3-small")
					).withClass(POPUP)
			   ).withId(getExitID(ex)).withStyle("display: none").withClass("w3-modal");
	}

	private DomContent getFileInfo(PhysicalFile pf) {
		return div(
				div(
					header(
							h2(
							a( i().withClass("fa fa-close")
							).withText(pf.getName() + " Info").withHref(getDetailsHref(pf))
							.withClass("w3-button")
							)
					).withClass("w3-green"),
					
					table(
					tbody(
							tr(
									th("Attribute"),
									th("Value").withClass("info")
							),
					        getFileInfoAttributes(pf)
					)).withClass("w3-table-all w3-small")
				).withClass(POPUP)
			).withId(getDetailsID(pf)).withStyle("display: none").withClass("w3-modal");
	}

	private String getDetailsID(PhysicalFile pf) {
		return "PF" + Integer.toString(pf.getComponentId());
	}

	private DomContent getFileInfoAttributes(PhysicalFile pf) {
		return join(
					getDDName(pf),
					getDSNName(pf),
					getMinRecordLength(pf),
					getMaxRecordLength(pf),
					getOutputDDName(pf),
					getRECFM(pf),
					getLRECL(pf)
					);
	}

	private TrTag getLRECL(PhysicalFile pf) {
		return tr(
				td("LRECL"),
				td(Integer.toString(pf.getLrecl()))
				);
	}

	private TrTag getRECFM(PhysicalFile pf) {
		return tr(
				td("RECFM"),
				td(pf.getRecfm().toString())
				);
	}

	private TrTag getOutputDDName(PhysicalFile pf) {
		return tr(
				td("Output DD Name"),
				td(pf.getOutputDDName())
				);
	}

	private TrTag getMaxRecordLength(PhysicalFile pf) {
		return tr(
				td("Maximum Record Length"),
				td(Integer.toString(pf.getMaximumLength()))
				);
	}

	private TrTag getMinRecordLength(PhysicalFile pf) {
		return tr(
				td("Minimum Record Length"),
				td(Integer.toString(pf.getMinimumLength()))
				);
	}

	private TrTag getDDName(PhysicalFile pf) {
		return tr(
				td("Input DD Name"),
				td(pf.getInputDDName())
				);
	}
	private TrTag getDSNName(PhysicalFile pf) {
		return tr(
				td("Input DSN Name"),
				td(pf.getDataSetName())
				);
	}

	private DomContent getDatabaseInfo(PhysicalFile pf) {
		return div(
				div(
					header(
							h2(
							a( i().withClass("fa fa-close")
							).withText(pf.getName() + " Info").withHref(getDetailsHref(pf))
							.withClass("w3-button")
							)
					).withClass("w3-green"),
					
					table(
					tbody(
							tr(
									th("Attribute"),
									th("Value").withClass("info")
							),
					        getDatabaseInfoAttributes(pf)
					)).withClass("w3-table-all w3-small")
				).withClass(POPUP)
			).withId(getDetailsID(pf)).withStyle("display: none").withClass("w3-modal");
	}

	private DomContent getDatabaseInfoAttributes(PhysicalFile pf) {
		return join(
				getDB2Subsystem(pf),
				getDDName(pf),
				getSQL(pf),
				getSchema(pf),
				getDBSTable(pf),
				getRowFormat(pf),
				getNullsIndicator(pf)
				);
	}

	private Object getNullsIndicator(PhysicalFile pf) {
		return tr(
				td("Include Nulls"),
				td(pf.isIncludeNulls() ? "True" : " False")
				);
	}

	private Object getRowFormat(PhysicalFile pf) {
		return tr(
				td("Row Format"),
				td(pf.getDatabaseRowFormat().toString())
				);
	}

	private DomContent getDBSTable(PhysicalFile pf) {
		return tr(
				td("Table"),
				td(pf.getDatabaseTable())
				);
	}

	private DomContent getSchema(PhysicalFile pf) {
		return tr(
				td("Schema"),
				td("TBD")
				);
	}

	private DomContent getSQL(PhysicalFile pf) {
		return tr(
				td("SQL"),
				td(pf.getSqlText())
				);
	}

	private DomContent getDB2Subsystem(PhysicalFile pf) {
		return tr(
				td("DB2 Subsystem"),
				td(pf.getDatabaseConnection())
				);
	}

	private String getDetailsHref(PhysicalFile pf) {
		return "javascript:toggleDiv(\""+ getDetailsID(pf) + "\")";
	}	
	
	public String getFileName() {
		return filename;
	}

}
