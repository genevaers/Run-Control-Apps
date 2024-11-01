package org.genevaers.runcontrolgenerator;

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


import java.io.FileWriter;
import java.util.Calendar;
import java.util.List;

import org.genevaers.compilers.base.ASTBase;
import org.genevaers.genevaio.ltfile.LTLogger;
import org.genevaers.genevaio.ltfile.LogicTable;
import org.genevaers.genevaio.report.ReportWriter;
import org.genevaers.repository.Repository;
import org.genevaers.repository.data.CompilerMessage;
import org.genevaers.runcontrolgenerator.compilers.ExtractPhaseCompiler;
import org.genevaers.runcontrolgenerator.compilers.FormatRecordsBuilder;
import org.genevaers.runcontrolgenerator.repositorybuilders.RepositoryBuilder;
import org.genevaers.runcontrolgenerator.repositorybuilders.RepositoryBuilderFactory;
import org.genevaers.runcontrolgenerator.runcontrolwriter.RunControlWriter;
import org.genevaers.runcontrolgenerator.singlepassoptimiser.LogicGroup;
import org.genevaers.runcontrolgenerator.singlepassoptimiser.SinglePassOptimiser;
import org.genevaers.utilities.GenevaLog;
import org.genevaers.utilities.GersConfigration;
import org.genevaers.utilities.IdsReader;
import org.genevaers.utilities.Status;

import com.google.common.flogger.FluentLogger;

public class RunControlGenerator {
    private static final FluentLogger logger = FluentLogger.forEnclosingClass();

	private Status status = Status.OK;

	private List<LogicGroup> logicGroups;

	private ASTBase xltRoot;

	private LogicTable extractLogicTable;
	private LogicTable joinLogicTable;

	public Status runFromConfig() {
		GenevaLog.writeHeader("Run Control Generator");
        Repository.setRunviews(IdsReader.getIdsFrom(GersConfigration.RUNVIEWS));
		if(buildComponentRepositoryFromSelectedInput() != Status.ERROR) {
			logger.atInfo().log("Repository populated");
			Repository.fixupMaxHeaderLines();
			Repository.fixupPFDDNames();
			Repository.allLFsNotRequired();
			Repository.setGenerationTime(Calendar.getInstance().getTime());
			singlePassOptimise();
			runCompilers();
			writeRunControlFiles();
		} else {
			Repository.addErrorMessage(new CompilerMessage(0, null, 0, 0, 0, "Failed to build the component repository"));
			logger.atSevere().log("Failed to build the component repository. No run control files will be written");
		}
		ReportWriter.setRCGStatus(status);
		return status;
	}

	private void writeRunControlFiles() {
		if(status != Status.ERROR) {
			RunControlWriter rcw = new RunControlWriter();
			logger.atFine().log("Join Logic Table");
			logger.atFine().log(LTLogger.logRecords(joinLogicTable));
			logger.atFine().log("Extract Logic Table");
			logger.atFine().log(LTLogger.logRecords(extractLogicTable));
			rcw.setExtractLogicTable(extractLogicTable);
			rcw.setJoinLogicTable(joinLogicTable);
			status = rcw.run();
			ReportWriter.setNumJLTRecordsWritten(joinLogicTable.getNumberOfRecords());
			ReportWriter.setNumXLTRecordsWritten(extractLogicTable.getNumberOfRecords());
			ReportWriter.setNumVDPRecordsWritten(rcw.getNumVDPRecordsWritten());
		} else {
			logger.atSevere().log("There were errors. No run control files will be written");
		}
	}

	/**
	 * Now that the single pass optimisation has been completed
	 * We need to form the tree of information to be enitted
	 * The top level of which is derived from the logic groups.
	 * The lower level as a result of compiling the logic text in the 
	 * view sources and view column sources.
	 * 
	 * We also need to compile the format phase filter and column calculations.
	 */
	private void runCompilers() {
		GenevaLog.logNow("runCompilers");
		if(status != Status.ERROR) {
			status = ExtractPhaseCompiler.run(logicGroups);
			extractLogicTable = ExtractPhaseCompiler.getExtractLogicTable();
			joinLogicTable = ExtractPhaseCompiler.getJoinLogicTable();
			FormatRecordsBuilder.run();
		} else {
			logger.atSevere().log("There were SPO errors. No compilation performed.");
		}
	}

	private void singlePassOptimise() {
		if(status != Status.ERROR) {
			SinglePassOptimiser spo = new SinglePassOptimiser();
			status = spo.run();
			logicGroups = spo.getLogicGroups();
			dumpLogicGroups();
		}
	}

	private void dumpLogicGroups() {
		StringBuilder sb = new StringBuilder();
		logicGroups.stream().forEach(lg -> lg.logData());
	}

	private Status buildComponentRepositoryFromSelectedInput() {
		RepositoryBuilder rb = RepositoryBuilderFactory.get();
		status = rb.run();
		return status;
	}

}
