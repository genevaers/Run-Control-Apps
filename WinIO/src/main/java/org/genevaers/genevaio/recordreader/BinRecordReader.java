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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import com.google.common.flogger.FluentLogger;

public class BinRecordReader extends RecordFileReader {
	private static final FluentLogger logger = FluentLogger.forEnclosingClass();
	private RandomAccessFile rFile;
	private int bytesRead;
	private long filelen;
	private FileRecord record = new FileRecord();
 
    @Override
	public void close() {
		try {
			rFile.close();
		} catch (IOException e) {
			logger.atSevere().log("BinRecordReader close error%s", e.getMessage());
		}
	}

	public int getBytesRead() {
		return bytesRead;
	}

	@Override
	public void readRecordsFrom(File file) throws IOException {
		try {
			rFile = new RandomAccessFile(file, "r");
			filelen = rFile.length();
			logger.atFine().log("File length %d", filelen);
		} catch (FileNotFoundException e) {
			logger.atSevere().log("BinRecordReader file not found %s", e.getMessage());
		}
	}

	@Override
	public FileRecord readRecord() {
		try {
			long offset = rFile.getFilePointer();
			if (offset < filelen) {
				if(recLen > 0) {
					record.length = (short) recLen;
				} else {
					record.length = rFile.readShort();
				}
				if (record.length > 0 && record.bytes.hasArray()) {
					if (record.length < record.bytes.limit()) {
						if(recLen > 0) {
							rFile.readFully(record.bytes.array(), 0, recLen);
						} else {
							rFile.readFully(record.bytes.array(), 0, record.length - 2);
						}
						bytesRead++;
					} else {
						logger.atFine().log("%d Running past the end of the file - treating as EOF", offset);
						EOFreached = true;
					}
				}
			} else {
				logger.atFine().log("%d Offset past the end of the file - treating as EOF", offset);
				EOFreached = true;
			}
		} catch (IOException e) {
			EOFreached = true;
		}
		return record;
	}

}
