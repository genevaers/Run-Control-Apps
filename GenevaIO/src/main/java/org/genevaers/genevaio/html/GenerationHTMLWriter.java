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
import static j2html.TagCreator.footer;
import static j2html.TagCreator.h2;
import static j2html.TagCreator.h4;
import static j2html.TagCreator.head;
import static j2html.TagCreator.header;
import static j2html.TagCreator.html;
import static j2html.TagCreator.link;
import static j2html.TagCreator.meta;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;

import org.genevaers.genevaio.vdpfile.VDPGenerationRecord;

import com.google.common.flogger.FluentLogger;

import j2html.tags.specialized.DivTag;

public class GenerationHTMLWriter {
    private static final FluentLogger logger = FluentLogger.forEnclosingClass();
	private static final String filename = "Generation.html";
	VDPGenerationRecord genrec;

	public void writeFromVDP(Path cwd, VDPGenerationRecord gen) {
		genrec = gen;
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
			logger.atSevere().log("GenerationHTMLWriter error\n%s", e.getMessage());
		}
	}

	private DivTag bodyContent() {
		return div(
						div().withClass("w3-card-4"),
						header(
									h2("VDP Build Date : " + genrec.getDate())
								).withClass("w3-container w3-light-green"),
								h4("Environment : " + genrec.getEnvironmentId()).withStyle("margin:0 16px"),
								h4("VDP Version : " + genrec.getVersionInfo()).withStyle("margin:0 16px"),
								h4("Builder : " + genrec.getDescription()).withStyle("margin:0 16px"),
								h4("Number Mode: " + genrec.getMaxDecimalDigits() +":" + genrec.getMaxDecimalPlaces()).withStyle("margin:0 16px"),
								footer("Analyzed @ : " + java.util.Calendar.getInstance().getTime()).withClass("w3-container w3-light-green")			
					).withClass("w3-cols10");
	}

	public String getFileName() {
		return filename;
	}

}
