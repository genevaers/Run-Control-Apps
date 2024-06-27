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


import static j2html.TagCreator.body;
import static j2html.TagCreator.div;
import static j2html.TagCreator.each;
import static j2html.TagCreator.h2;
import static j2html.TagCreator.head;
import static j2html.TagCreator.html;
import static j2html.TagCreator.link;
import static j2html.TagCreator.meta;
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
import org.genevaers.repository.components.UserExit;

import com.google.common.flogger.FluentLogger;

import j2html.tags.ContainerTag;
import j2html.tags.specialized.DivTag;
import j2html.tags.specialized.TrTag;

public class ExitHTMLWriter {
    private static final FluentLogger logger = FluentLogger.forEnclosingClass();
	private static final String filename = "Exits.html";

	public void writeFromVDP(Path cwd) {
		File output = cwd.resolve(filename).toFile();
		try (FileWriter fw = new FileWriter(output);){
			fw.write(html(head(meta().withContent("text/html; charset=UTF-8"),
					link().withRel("stylesheet").withType("text/css")
							.withHref("https://www.w3schools.com/w3css/4/w3.css"),
					link().withRel("stylesheet").withType("text/css")
							.withHref("https://www.w3schools.com/lib/w3-colors-flat.css"),
					link().withRel("stylesheet").withType("text/css").withHref(
							"https://cdnjs.cloudflare.com/ajax/libs/font-awesome/4.7.0/css/font-awesome.min.css")),
					body(bodyContent())).renderFormatted());
		} catch (IOException e) {
			logger.atSevere().log("ExitHTMLWriter error\n%s", e.getMessage());
		}
	}

	private ContainerTag<DivTag> bodyContent() {
		return div(h2("User Exit Routines"),
				table(tbody(tr(th("ID"), th("Name"), th("Type"), th("Optimized"), th("Language"), th("Executable")),
						each(Repository.getUserExits().getValues(), ex -> getExitRow(ex)))).withClass("w3-table w3-striped w3-border"))
								.withClass("w3-container");
	}

	private TrTag getExitRow(UserExit ex) {
		return tr(
				td(Integer.valueOf(ex.getComponentId()).toString()),
				td(ex.getName()),
				td(ex.getExitType().toString()),
				td(ex.isOptimizable() ? "True" : "False"),
				td(ex.getExitType().toString()),
				td(ex.getExecutable())
				);
	}

	public String getFileName() {
		return filename;
	}

}
