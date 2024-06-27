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

import java.io.EOFException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import com.google.common.flogger.FluentLogger;
import org.genevaers.genevaio.ltfile.LTFileObject;
import org.genevaers.genevaio.vdpfile.VDPFileObject;

public class RecordReaderFactory {
    private static final FluentLogger logger = FluentLogger.forEnclosingClass();

	private RandomAccessFile rFile;
	private int numRecordsRead = 0;
	private boolean EOFreached = false;
	private boolean readable = false;
	private int numRecordsWritten = 0;

	public class FileRecord {
		public short length = 0;
		public ByteBuffer bytes = ByteBuffer.allocate(8 * 1024);
		public String name;

		public int bytesWritten() {
			return bytes.position();
		}
	}

	private FileRecord record = new FileRecord();
	private long filelen;
	private RecordFileWriter rw;
	private boolean writeEBCDIC = false;

	public RecordReaderFactory() {
		//want the spaces buffer to be the correct character
		String os = System.getProperty("os.name");
		logger.atFine().log("Operating System %s", os);
		if(os.startsWith("z")) {
			logger.atFine().log("Using ZosRecordWriter");
			rw = new ZosRecordWriter();
			writeEBCDIC = true;
			RecordFileWriter.setSpacesEBCDIC();
		} else {
			logger.atFine().log("Using BinRecordWriter");
			rw = new BinRecordWriter();
		}
		VDPFileObject.setSpaces(RecordFileWriter.getSpaces());
		LTFileObject.setSpaces(RecordFileWriter.getSpaces());
	}

	public void readRecordsFrom(File file) throws IOException {
		try {
			rFile = new RandomAccessFile(file, "r");
			filelen = rFile.length();
			logger.atFine().log("File length %d", filelen);
		} catch (FileNotFoundException e) {
			logger.atSevere().log("readRecordsFrom %s error \n%s", file.getName(), e.getMessage());
		}
	}

	public void writeRecordsTo(File file) throws IOException {
		rw.writeRecordsTo(file);
	}

	public boolean isFileReadable() {
		return readable;
	}

	public FileRecord readRecord() throws IOException {
		try {
			long offset = rFile.getFilePointer();
			if (offset < filelen) {
				record.length = rFile.readShort();
				if (record.length > 0 && record.bytes.hasArray()) {
					if (record.length < record.bytes.limit()) {
						rFile.readFully(record.bytes.array(), 0, record.length - 2);
						numRecordsRead++;
					} else {
						logger.atFine().log("%d Running past the end of the file - treating as EOF", offset);
						EOFreached = true;
					}
				}
			} else {
				logger.atFine().log("%d Offset past the end of the file - treating as EOF", offset);
				EOFreached = true;
			}
		} catch (EOFException e) {
			EOFreached = true;
		}
		return record;
	}

	public void writeAndClearTheRecord() {
		//The record should now be filled.
		//The first short should be the length of the record
		//Write an ID at the end of each record "JEND"
		rw.write(record.bytes);
		record.bytes.clear();
		numRecordsWritten++;
	}

	public boolean isAtFileEnd() {
		return EOFreached;
	}

	public void close() {
		try {
			if(rFile != null) {
				rFile.close();
			}
			if(rw != null) {
				rw.close();
			}
		} catch (IOException e) {
			logger.atSevere().log("RecordReaderFactory failed to close\n%s", e.getMessage());
		}
	}

	public Object getNumRecordsRead() {
		return numRecordsRead;
	}

	public FileRecord getRecordToFill() {
		return record;
	}

	public byte[] convertOutputIfNeeded(String str){
		byte[] retStr;
		//This flag needed to be determined by detecting 
		//the operating system at startup
		//We can also introduce a factory to select the appropriate
		//record writer to use too.
		if (writeEBCDIC) {
			return asciiToEbcdic(str);
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

	private byte[] asciiToEbcdic(String str) {
      Charset utf8charset = Charset.forName("UTF-8");
      Charset ebccharset = Charset.forName("IBM-1047");
      ByteBuffer inputBuffer = ByteBuffer.wrap(str.getBytes());
      CharBuffer data = utf8charset.decode(inputBuffer);
      return ebccharset.encode(data).array();
	}

	public void setLengthFromPosition() {
		short p = (short)record.bytes.position();
		record.bytes.putShort(0, p);
	}

	public void resetRecordCounters() {
		numRecordsWritten = 0;
		numRecordsRead = 0;
	}

	public int getNumRecordsWritten() {
		return numRecordsWritten;
	}

}
