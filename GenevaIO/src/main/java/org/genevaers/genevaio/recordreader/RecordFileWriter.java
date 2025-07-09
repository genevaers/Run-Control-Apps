package org.genevaers.genevaio.recordreader;

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


import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import org.apache.commons.lang3.StringUtils;
import org.genevaers.utilities.GersCodePage;
import org.genevaers.utilities.GersConfigration;
import com.google.common.flogger.FluentLogger;

public abstract class RecordFileWriter {
	private static final FluentLogger logger = FluentLogger.forEnclosingClass();
	private static boolean spacesConverted = false;
	protected static String spaces = StringUtils.repeat(" ", 1536); //!!! Don't change this length  !!!!!

	public abstract void writeRecordsTo(File file) throws IOException;
	public abstract void write(ByteBuffer bytes);
	public abstract void close();
	protected FileRecord record = new FileRecord();
	private int numRecordsWritten;

	public static String getSpaces() {
		return spaces;
	}

	public static void setSpacesEBCDIC() {
		if(spacesConverted == false) {
			spaces = new String(GersCodePage.asciiToEbcdic(spaces));
			spacesConverted = true;
		}
	}

	public void writeAndClearTheRecord() {
		//The record should now be filled.
		//The first short should be the length of the record
		//Write an ID at the end of each record "JEND"
		write(record.bytes);
		record.bytes.clear();
		numRecordsWritten++;
	}

	public void setLengthFromPosition() {
		short p = (short)record.bytes.position();
		record.bytes.putShort(0, p);
	}	
	
	public int getNumRecordsWritten() {
		return numRecordsWritten;
	}

	public byte[] convertOutputIfNeeded(String str){
		byte[] retStr;
		//This flag needed to be determined by detecting 
		//the operating system at startup
		//We can also introduce a factory to select the appropriate
		//record writer to use too.
		if (GersConfigration.isZos()) {
			return GersCodePage.asciiToEbcdic(str);
		} else {
			if(str != null) {
				retStr = str.getBytes();
			} else {
				String mt = "";
				logger.atWarning().log("RecordFileReaderWriter: Converting null string");
				retStr = mt.getBytes();
			}
			return retStr;
		}
	}

	public FileRecord getRecordToFill() {
		return record;
	}
}
