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
import static j2html.TagCreator.h3;
import static j2html.TagCreator.h4;
import static j2html.TagCreator.head;
import static j2html.TagCreator.html;
import static j2html.TagCreator.iframe;
import static j2html.TagCreator.join;
import static j2html.TagCreator.link;
import static j2html.TagCreator.meta;
import static j2html.TagCreator.script;
import static j2html.TagCreator.span;
import static j2html.TagCreator.title;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;

import org.genevaers.genevaio.vdpfile.VDPManagementRecords;
import org.genevaers.repository.Repository;

import com.google.common.flogger.FluentLogger;

import j2html.tags.ContainerTag;
import j2html.tags.DomContent;
import j2html.tags.specialized.ATag;
import j2html.tags.specialized.DivTag;
import j2html.tags.specialized.H3Tag;
import j2html.tags.specialized.H4Tag;
import j2html.tags.specialized.SpanTag;

public class RCAFrameworkHTMLWriter {
    private static final FluentLogger logger = FluentLogger.forEnclosingClass();

	private static final String MENU_BADGE = "w3-badge w3-right";
	private static final String MENU_CLASS = "w3-bar-item w3-button w3-hover-blue w3-flat-silver";
	private static final String PAGE_IFRAME = "page_iframe";
	private static final String TEXT_CSS = "text/css";
	private static final String STYLESHEET = "stylesheet";
	private Path filename;
	private FileWriter fw;
	private GenerationHTMLWriter gw = new GenerationHTMLWriter();
	private ControlRecordHTMLWriter crw = new ControlRecordHTMLWriter();
	private ExitHTMLWriter erw = new ExitHTMLWriter();
	private PhysicalFileHTMLWriter pfw = new PhysicalFileHTMLWriter();
	private LogicalFileHTMLWriter lfw = new LogicalFileHTMLWriter();
	private LogicalRecordHTMLWriter lrw = new LogicalRecordHTMLWriter();
	private LookupHTMLWriter lkupw = new LookupHTMLWriter();
	private ViewsHTMLWriter vw = new ViewsHTMLWriter();
	private VDPManagementRecords viewManagementRecords;

	String toggleScript = "function toggleDiv(divname) {" +
			"var ele = document.getElementById(divname);" +
			"if (ele.style.display == \"none\") {" +
			"ele.style.display = \"block\";" +
			"}" +
			"else {" +
			"ele.style.display = \"none\";" +
			"}" +
			"}";
	private Path cwd;
	private String reportFormat;
	private String xltReportName;
	private String jltReportName;
	private String vdpReportName;
	

	public void setFileName(String fileName) {
		filename = cwd.resolve(fileName);
	}

	public void write(VDPManagementRecords vmrs) {

		viewManagementRecords = vmrs;
		writeFamework();
		writeComponentFiles();
	}

	private void writeComponentFiles() {
		gw.writeFromVDP(cwd, viewManagementRecords.getViewGeneration());
		crw.writeFromVDP(cwd);
		erw.writeFromVDP(cwd);
		pfw.writeFromVDP(cwd);
		lfw.writeFromVDP(cwd);
		lrw.writeFromVDP(cwd);
		lkupw.writeFromVDP(cwd);
		vw.writeFromVDP(cwd);
	}

	private void writeFamework() {
		File output = filename.toFile();
		try (FileWriter fw = new FileWriter(output);) {
			fw.write(
					html(
							head(
									meta().withContent("text/html; charset=UTF-8"),
									title("Run Control Analysis"),
									link().withRel(STYLESHEET).withType(TEXT_CSS).withHref("https://www.w3schools.com/w3css/4/w3.css"),
									link().withRel(STYLESHEET).withType(TEXT_CSS).withHref("https://www.w3schools.com/lib/w3-colors-flat.css"),
									link().withRel(STYLESHEET).withType(TEXT_CSS).withHref("https://cdnjs.cloudflare.com/ajax/libs/font-awesome/4.7.0/css/font-awesome.min.css"),
									script(join(toggleScript)).withLang("Javascript")
							),
							body(
									bodyContent()
							)).renderFormatted());
		} catch (IOException e) {
			logger.atSevere().log("RCAFrameworkHTMLWriter writeFamework failed %e", e.getMessage());
		}
	}

	private DivTag bodyContent() {
		return div( sidebar(), pageContent());
	}

	private DomContent pageContent() {
		return div(
						div(
								iframe().withSrc(gw.getFileName()).attr("height", "100%").attr("width", "99%").attr("name", PAGE_IFRAME)
							).withStyle("width:100% ; height:100% ; overflow:hidden;")
					).withStyle("margin-left:25% ");
	}

	private DivTag sidebar() {
		return div( 
						div(
								h3("Run Control Analysis"),
								generationEntry(),
								componentsSection(),
								views(),
								lookupPaths(),
								logicalRecords(),
								logicalFiles(),
								physicalFiles(),
								userExits(),
								controlRecords(),
								flowBreaker(),
								flowSection(),
								joinsSection(),
								reportsBreaker(),
								vdpReport(),
								xltreport(),
								jltreport(),
								grcgreport(),
								functionCodeCoverage()
						).withClass("w3-container w3-dark-grey")
					).withClass("w3-sidebar w3-bar-block w3-light-grey").withStyle("width:25%; height:100%");
	}


	private DomContent grcgreport() {
		return a().withText("RCARPT")
				.withClass(MENU_CLASS)
				.withHref("../RCARPT")
				.withTarget(PAGE_IFRAME);
	}

	private DomContent reportsBreaker() {
		return h3("Reports").withClass("w3-dark-grey");
	}

	private DomContent flowBreaker() {
		return h3("Flow Diagrams").withClass("w3-dark-grey");
	}

	private DomContent functionCodeCoverage() {
		return a().withText("Function Code Coverage")
				.withClass(MENU_CLASS)
				.withHref("ltcov.html")
				.withTarget(PAGE_IFRAME);
	}

	private DomContent vdpReport() {
		return a().withText("VDP Report (" + reportFormat + ")")
				.withClass(MENU_CLASS)
				.withHref(vdpReportName)
				.withTarget(PAGE_IFRAME);
	}
	private DomContent jltreport() {
		return a().withText("JLT Report (" + reportFormat + ")")
				.withClass(MENU_CLASS)
				.withHref(jltReportName)
				.withTarget(PAGE_IFRAME);
	}

	private DomContent xltreport() {
		return a().withText("XLT Report (" + reportFormat + ")")
				.withClass(MENU_CLASS)
				.withHref(xltReportName)
				.withTarget(PAGE_IFRAME);
	}

	private DomContent joinsSection() {
		return a().withText("Joins")
				.withClass(MENU_CLASS)
				.withHref("joins.dot.svg")
				.withTarget(PAGE_IFRAME);
	}

	private DomContent flowSection() {
		//return h3("Flow").withClass("w3-pink");
		return a().withText("Views")
				.withClass(MENU_CLASS)
				.withHref("flow.dot.svg")
				.withTarget(PAGE_IFRAME);
	}

	private DomContent controlRecords() {
		return a(controlRecordsSpan()).withText("Control Records")
				.withClass(MENU_CLASS)
				.withHref(crw.getFileName())
				.withTarget(PAGE_IFRAME);
	}

	private DomContent controlRecordsSpan() {
		return span(Integer.toString(Repository.getControlRecords().size())).withClass(MENU_BADGE);
	}

	private DomContent userExits() {
		return a(exitsSpan()).withText("User Exit Routines")
				.withClass(MENU_CLASS)
				.withHref(erw.getFileName())
				.withTarget(PAGE_IFRAME);
	}

	private DomContent exitsSpan() {
		return span(Integer.toString(Repository.getUserExits().size())).withClass(MENU_BADGE);
	}

	private DomContent logicalFiles() {
		return a(logicalFilesSpan()).withText("Logical Files")
				.withClass(MENU_CLASS)
				.withHref(lfw.getFileName())
				.withTarget(PAGE_IFRAME);
	}
	
	private ContainerTag<SpanTag> logicalFilesSpan() {
		return span(Integer.toString(Repository.getLogicalFiles().size())).withClass(MENU_BADGE);
	}

	private DomContent physicalFiles() {
		return a(physicalFilesSpan()).withText("Physical Files")
				.withClass(MENU_CLASS)
				.withHref(pfw.getFileName())
				.withTarget(PAGE_IFRAME);
	}
	
	private ContainerTag<SpanTag> physicalFilesSpan() {
		return span(Integer.toString(Repository.getPhysicalFiles().size())).withClass(MENU_BADGE);
	}

	private DomContent logicalRecords() {
		return a(lrsSpan()).withText("Logical Records")
				.withClass(MENU_CLASS)
				.withHref(lrw.getFileName())
				.withTarget(PAGE_IFRAME);
	}

	private ContainerTag<SpanTag> lrsSpan() {
		return span(Integer.toString(Repository.getLogicalRecords().size())).withClass(MENU_BADGE);
	}

	private H3Tag componentsSection() {
		return h3("Components").withClass("w3-dark-grey");
	}

	private ATag lookupPaths() {
		return a(lookupsSpan()).withText("Lookup Paths")
				.withClass(MENU_CLASS)
				.withHref("LookupsTable.html")
				.withTarget(PAGE_IFRAME)				;
	}

	private SpanTag lookupsSpan() {
		return span(Integer.toString(Repository.getLookups().size())).withClass(MENU_BADGE);
	}

	private ContainerTag<SpanTag> viewsSpan() {
		return span(Integer.toString(Repository.getViews().size())).withClass(MENU_BADGE);	
	}

	private ATag views() {
		return a(viewsSpan()).withText("Views")
				.withClass(MENU_CLASS)
				.withHref("ViewsTable.html")
				.withTarget(PAGE_IFRAME)				;
	}

	private H4Tag generationEntry() {
		return h4(
					a("Generation Details")
					.withClass("w3-bar-item w3-button w3-flat-silver w3-hover-light-green")
					.withHref(gw.getFileName())
					.withTarget(PAGE_IFRAME)
				);
	}

	public void close() throws IOException {
		fw.close();
	}

	public void setCurrentWorkingDirectory(Path c) {
		cwd=c;
	}

	public void setReportType(String reportFormat) {
		this.reportFormat = reportFormat;
	}

	public void setXLTReportName(String xltReportName) {
		this.xltReportName = xltReportName;
	}

    public void setJLTReportName(String jltReportName) {
		this.jltReportName = jltReportName;
    }

    public void setVDPReportName(String vdpReportName) {
		this.vdpReportName = vdpReportName;
    }

}
