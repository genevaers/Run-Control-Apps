package org.genevaers.runcontrolgenerator.runcontrolwriter;

import java.io.IOException;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

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

import org.genevaers.genevaio.ltfile.LTWriter;
import org.genevaers.genevaio.ltfile.LogicTable;
import org.genevaers.genevaio.vdpfile.VDPExtractRecordFile;
import org.genevaers.genevaio.vdpfile.VDPFileWriter;
import org.genevaers.genevaio.vdpfile.VDPGenerationRecord;
import org.genevaers.genevaio.vdpfile.VDPManagementRecords;
import org.genevaers.genevaio.vdpfile.record.VDPRecord;
import org.genevaers.repository.Repository;
import org.genevaers.utilities.GenevaLog;
import org.genevaers.utilities.GersConfigration;
import org.genevaers.utilities.Status;

import com.google.common.flogger.FluentLogger;

public class RunControlWriter {
    private static final FluentLogger logger = FluentLogger.forEnclosingClass();
    private LogicTable extractLogicTable;
    private LogicTable joinLogicTable;
    private Status status = Status.OK;

    private int numVDPRecords;

	public RunControlWriter() {
	}

	public Status run() {
        GenevaLog.writeHeader("Write the run control files");
		//Emit the XLT from the compiler AST
        LTWriter xltw = new LTWriter();
        LTWriter jltw = new LTWriter();
        try {
            logger.atInfo().log("Write Join Logic Table");
            jltw.write(joinLogicTable, Paths.get(GersConfigration.getJLTFileName()));
            jltw.close();
            logger.atInfo().log("Write Extract Logic Table");
            xltw.write(extractLogicTable, Paths.get(GersConfigration.getXLTFileName()));
            xltw.close();
        } catch (IOException e) {
            logger.atSevere().log("LT write failed %s", e.getMessage());
            status = Status.ERROR;
        }
        VDPFileWriter vdpw = new VDPFileWriter();
        vdpw.open(GersConfigration.getVDPFileName());
        VDPManagementRecords vmrs = makeVDPManagementRecords();
        try {
            vdpw.writeVDPFrom(vmrs);
            vdpw.close();
            numVDPRecords = vdpw.getNumRecordsWritten();
            logger.atInfo().log("VDP Written");
        } catch (Exception e) {
            logger.atSevere().log("VDP write failed %s", e.getMessage());
            status = Status.ERROR;
        }
        return status;
	}

    private VDPManagementRecords makeVDPManagementRecords() {
        VDPManagementRecords vmrs = new VDPManagementRecords();
        vmrs.setViewGeneration(makeGenerationRecord());
        vmrs.setExtractRecordFile(makeExtractRecordFile());
        return vmrs;
    }

    private VDPExtractRecordFile makeExtractRecordFile() {
        VDPExtractRecordFile erf = new VDPExtractRecordFile();
        erf.setRecordType(VDPRecord.VDP_EXTRACT_RECORD_FILE);
        erf.setRecordId(25); 
        erf.setExtractPrefix("EXTR");
        //We need to have accumulated the number of extract files
        //This would be done during compiler generating WR statements
        //For the moment it is 0
        return erf;
    }

    private VDPGenerationRecord makeGenerationRecord() {
        //Need a Generation Record if nothing else
        VDPGenerationRecord gen = new VDPGenerationRecord();
        gen.setRecordType(VDPRecord.VDP_GENERATION);
        gen.setAsciiInd(System.getProperty("os.name").startsWith("z") ? false : true);
        gen.setBigEndianInd(true);
        DateFormat dateFormat1 = new SimpleDateFormat("YYYYMMdd");
        Date dt = Repository.getGenerationTime();
        DateFormat dateFormat = new SimpleDateFormat("YYYY-MM-dd");
        DateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
        gen.setDate(dateFormat.format(dt));
        gen.setRunDate(dateFormat1.format(dt));
        gen.setTime(timeFormat.format(dt));
        gen.setDescription("Java MR91 via " + GersConfigration.getInputType());
        gen.setPadding4("");
        gen.setPadding5("");
        setNumberNode(gen);
        setRecordNumbers(gen);
		gen.setVersionInfo((short)13);
		gen.setLrFieldCount(Repository.getFields().size());
        return gen;
    }


    private void setRecordNumbers(VDPGenerationRecord gen) {
        gen.setInputFileCount(Repository.getNumberOfRequiredPhysicalFiles());
        gen.setPgmFileCount(Repository.getUserExits().size());
        gen.setLrCount(Repository.getLogicalRecords().size());
        gen.setLrFieldCount(Repository.getFields().size());
        gen.setLrIndexFieldCount(Repository.getIndexes().size());
        gen.setJoinStepCount(Repository.getLookups().size());
        gen.setViewCount(Repository.getViews().size());
        //Still need number of records and byte count.
    }

    private void setNumberNode(VDPGenerationRecord gen) {
        if(GersConfigration.isNumberModeStandard()) {
            gen.setMaxDecimalDigits((byte)23);
            gen.setMaxDecimalPlaces((byte)8);
        } else {
            gen.setMaxDecimalDigits((byte)23);
            gen.setMaxDecimalPlaces((byte)3);
        }
    }

    public void setExtractLogicTable(LogicTable lt) {
        extractLogicTable = lt;
    }

    public void setJoinLogicTable(LogicTable lt) {
        joinLogicTable = lt;
    }

    public int getNumVDPRecordsWritten() {
        return numVDPRecords;
    }

    public int getNumXLTRecords() {
        return extractLogicTable.getNumberOfRecords();
    }

    public int getNumJLTRecords() {
        return joinLogicTable.getNumberOfRecords();
    }

}
