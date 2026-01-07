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
import java.nio.ByteBuffer;

import com.google.common.flogger.FluentLogger;
import com.google.common.flogger.StackSize;

public class BinRecordWriter extends RecordFileWriter {
	private static final FluentLogger logger = FluentLogger.forEnclosingClass();
	private RandomAccessFile wFile;
	private int bytesWritten;
 
    @Override
	public void writeRecordsTo(File file) throws IOException {
		try {
			wFile = new RandomAccessFile(file, "rw");
			wFile.setLength(0);
			bytesWritten = 0;
		} catch (FileNotFoundException e) {
			logger.atSevere().log("BinRecordWriter %s", e.getMessage());
		}
	}

	@Override
	public void write(ByteBuffer bytes) {
		if(recLen > 0) {
				try {
					wFile.write(bytes.array(), 0, recLen);
					bytesWritten += bytes.position();
				} catch (IOException e){
					logger.atSevere().withCause(e).withStackTrace(StackSize.FULL);
				}
		} else {
			if(bytes.getShort(0) ==  bytes.position()) {
				try {
					wFile.write(bytes.array(), 0, bytes.position());
					bytesWritten += bytes.position();
				} catch (IOException e){
					logger.atSevere().withCause(e).withStackTrace(StackSize.FULL);
				}
			} else {
				logger.atSevere().log("Write length mistmatch record length is %d  buffer position is ", bytes.getShort(0), bytes.position());
			}
		}
    }

	public void writeArray(ByteBuffer bytes) {
		try {
			wFile.write(bytes.array(), 0, bytes.position());
			wFile.write('\n');
			bytesWritten += bytes.position();
		} catch (IOException e){
			logger.atSevere().withCause(e).withStackTrace(StackSize.FULL);
		}
    }

    @Override
	public void close() {
		try {
			wFile.close();
		} catch (IOException e) {
			logger.atSevere().log("BinRecordWriter close error\n", e.getMessage());
		}
	}

	public int getBytesWritten() {
		return bytesWritten;
	}

}
