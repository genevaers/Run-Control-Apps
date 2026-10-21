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
import java.util.Iterator;

import org.genevaers.repository.Repository;
import org.genevaers.repository.components.LRField;
import org.genevaers.repository.components.LRIndex;
import org.genevaers.repository.components.LogicalRecord;
import org.genevaers.repository.components.UserExit;

import com.google.common.flogger.FluentLogger;

import j2html.tags.ContainerTag;
import j2html.tags.DomContent;
import j2html.tags.specialized.DivTag;

public class LogicalRecordHTMLWriter {
	private static final FluentLogger logger = FluentLogger.forEnclosingClass();

	private static final String filename = "LRs.html";
	private FileWriter fw;
	private static final String POPUP = "w3-modal-content w3-animate-zoom";
	static String toggleScript = "function toggleDiv(divname) {" + "var ele = document.getElementById(divname);"
			+ "if (ele.style.display == \"none\") {" + "ele.style.display = \"block\";" + "}" + "else {"
			+ "ele.style.display = \"none\";" + "}" + "}";

	public void writeFromVDP(Path cwd) {

		File output = cwd.resolve(filename).toFile();
		try {

			fw = new FileWriter(output);
			fw.write(html(head(meta().withContent("text/html; charset=UTF-8"),
					link().withRel("stylesheet").withType("text/css")
							.withHref("https://www.w3schools.com/w3css/4/w3.css"),
					link().withRel("stylesheet").withType("text/css")
							.withHref("https://www.w3schools.com/lib/w3-colors-flat.css"),
					link().withRel("stylesheet").withType("text/css").withHref(
							"https://cdnjs.cloudflare.com/ajax/libs/font-awesome/4.7.0/css/font-awesome.min.css"),
					script(join(toggleScript)).withLang("Javascript")), body(bodyContent())).renderFormatted());
			fw.close();
		} catch (IOException e) {
			logger.atSevere().log("LogicalRecordHTMLWriter writeFromVDP failes %s\n", e.getMessage());
		}
	}

	private ContainerTag<DivTag> bodyContent() {
		return div(h2("Logical Records"),
				table(tbody(
						tr(th("ID"), th("Name"), th("Number of Fields"), th("Fields"), th("Record Length"),
								th("Key Length"), th("Lookup Exit"), th("Param(s)")),
						each(Repository.getLogicalRecords().getValues(), lr -> getLRRow(lr)))).withClass("w3-table w3-striped w3-border"))
								.withClass("w3-container");
	}

	private ContainerTag getLRRow(LogicalRecord lr) {
		return tr(td(Integer.valueOf(lr.getComponentId()).toString()), td(lr.getName()), getFieldCount(lr),
				getFieldList(lr), getRecordLength(lr), getKeyLength(lr), getLookupExit(lr), getLookupParam(lr));
	}

	private DomContent getLookupParam(LogicalRecord lr) {
		return td(lr.getLookupExitParams());
	}

	// TODO this exit stuff is common - remove the repeated code
	private DomContent getLookupExit(LogicalRecord lr) {
		if (lr.getLookupExit() != null) {
			UserExit ex = lr.getLookupExit();
			return td(a(ex.getName()).withHref(getExitHref(ex)), getExitInfo(ex));
		} else {
			return td("");
		}
	}

	private String getExitHref(UserExit ex) {
		return "javascript:toggleDiv(\"" + getExitID(ex) + "\")";
	}

	private DomContent getExitInfo(UserExit ex) {
		return div(div(
				header(h2(a(i().withClass("fa fa-close")).withText(ex.getName() + " Info").withHref(getExitHref(ex))
						.withClass("w3-button"))).withClass("w3-green"),
				table(tbody(tr(th("ID"), th("Name"), th("Type"), th("Optimized"), th("Language"), th("Executable")),
						tr(td(Integer.valueOf(ex.getComponentId()).toString()), td(ex.getName()),
								td(ex.getExitType().toString()), td(ex.isOptimizable() ? "True" : "False"),
								td(ex.getProgramType().toString()), td(ex.getExecutable()))))
										.withClass("w3-table-all w3-small")).withClass(POPUP)).withId(getExitID(ex))
												.withStyle("display: none").withClass("w3-modal");
	}

	private String getExitID(UserExit ex) {
		return "EX" + Integer.toString(ex.getComponentId());
	}

	private DomContent getKeyLength(LogicalRecord lr) {
		Iterator<LRIndex> keyIt = lr.getIteratorForIndexBySeq();
		int len = 0;
		while(keyIt.hasNext()) {
			LRIndex k = keyIt.next();
			LRField f = lr.findFromFieldsByID(k.getFieldID());
			if (f == null) {
				logger.atWarning().log("LogicalRecord %d missing field %d referenced by index; skipping length", lr.getComponentId(), k.getFieldID());
				continue;
			}
			len += f.getLength();
		}
		return td(Integer.toString(len));
	}

	private DomContent getRecordLength(LogicalRecord lr) {
		int maxEndPos = lr.getValuesOfFieldsByID()
				.stream()
				.mapToInt(f -> f.getStartPosition() + f.getLength())
				.max()
				.orElse(1);
		return td(Integer.toString(maxEndPos - 1));
	}

	private DomContent getFieldList(LogicalRecord lr) {
		return td(a("Show Fields").withHref(getFieldsHref(lr)),
				getList(lr));
	}
	
	private DomContent getList(LogicalRecord lr) {
		return div(
				div(
					header(
							h2(
							a( i().withClass("fa fa-close")
							).withText(lr.getName() + " Fields").withHref(getFieldsHref(lr))
							.withClass("w3-button")
							)
					).withClass("w3-green"),
					
					table(
					tbody(
							tr(
									th("ID"),
									th("Name").withClass("info"),
									th("Data Type"),
									th("Position"),
									th("Length"),
									th("Date"),
									th("Signed"),
									th("Decimals"),
									th("Scaling"),
									th("Align"),
									th("Effective Date")
							),
							each(lr.getValuesOfFieldsByID(), fld  -> getFieldRow(fld))
					)).withClass("w3-table-all w3-small")
				)
			).withId(getListID(lr)).withStyle("display: none");
	}

	private DomContent getFieldRow(LRField fld) {
		return tr(
				td(Integer.toString(fld.getComponentId())),
				td(fld.getName()),
				td(fld.getDatatype().toString()),
				td(Integer.toString(fld.getStartPosition())),
				td(Integer.toString(fld.getLength())),
				td(fld.getDateTimeFormat().toString()),
				td(fld.isSigned() ? "True" : "False"),
				td(Integer.toString(fld.getNumDecimalPlaces())),
				td(Integer.toString(fld.getRounding())),
				td(fld.getJustification().toString()),
				getPrimaryKey(fld)
				);
	}


	private DomContent getPrimaryKey(LRField fld) {
		//TODO got to work this one out too
		// if(fld.isIndex()) {
		// 	return td(fld.getPrimaryKey());		
		// } else {
		 	return td("TODO");
		// }
	}

	private DomContent getFieldCount(LogicalRecord lr) {
		return td(Integer.toString(lr.getValuesOfFieldsByID().size()));
	}

	private String getFieldsHref(LogicalRecord lr) {
		return "javascript:toggleDiv(\""+ getListID(lr) + "\")";
	}

	private String getListID(LogicalRecord lr) {
		return "LR" + Integer.toString(lr.getComponentId());
	}

	public String getFileName() {
		return filename;
	}

}
