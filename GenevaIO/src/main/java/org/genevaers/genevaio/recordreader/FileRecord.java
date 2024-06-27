package org.genevaers.genevaio.recordreader;

/*
 * Copyright Contributors to the GenevaERS Project.
								SPDX-License-Identifier: Apache-2.0 (c) Copyright IBM Corporation
								2008
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


import java.nio.ByteBuffer;

import com.google.common.flogger.FluentLogger;

public class FileRecord {
    private static final FluentLogger logger = FluentLogger.forEnclosingClass();
	public short length = 0;
	public ByteBuffer bytes = ByteBuffer.allocate(8 * 1024);
	public String name;

	public int bytesWritten() {
		return bytes.position();
	}

    public void dump() {
		logger.atFine().log("Record length %d", length);
		StringBuilder sb = new StringBuilder();
		for(int i=0; i<length; i++) {
			sb.append(String.format("%02X ", bytes.get(i)));
		}
		sb.append("\n");
		logger.atFine().log(sb.toString());
		bytes.rewind();
    }
}
