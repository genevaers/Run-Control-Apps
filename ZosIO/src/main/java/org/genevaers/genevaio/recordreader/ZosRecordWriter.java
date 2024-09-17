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

import com.google.common.flogger.FluentLogger;

public class ZosRecordWriter extends RecordFileWriter {
	private static final FluentLogger logger = FluentLogger.forEnclosingClass();
    com.ibm.jzos.RecordWriter writer = null;
	protected static String spaces = StringUtils.repeat("@", 1536); //!!! Don't change this length  !!!!!

    @Override
    public void writeRecordsTo(File file) throws IOException {
        try {
            if (inCommandLineMode()) {
                String dsn = "//'GEBT.ETEST.NEW." + file.toString() + "'";
                System.out.println("Open DSN = " + dsn);
                writer = com.ibm.jzos.RecordWriter.newWriter(dsn, com.ibm.jzos.ZFileConstants.FLAG_DISP_OLD);

            } else {
                String ddname = file.toString();
                System.out.println("Open DD Name = " + ddname);
                writer = com.ibm.jzos.RecordWriter.newWriterForDD(ddname);
            }
        } catch (com.ibm.jzos.ZFileException e) {
            logger.atSevere().log("jzos file exception in open\n%s", e.getMessage());
        }
    }

    private boolean inCommandLineMode() {
        return false;
    }

    @Override
    public void write(ByteBuffer bytes) {
        try {
            // int len = 27;
            // if(bytes.position() < 27) {
            //     len = bytes.position();
            // }
            // for(int i = 0; i<len; i++) {
            //     System.out.print(String.format("%02X ", bytes.get(i)));
            // }
            // System.out.println();
            if(bytes.position() > 4) {
                writer.write(bytes.array(), 4, bytes.position() - 4);
            }
        } catch (com.ibm.jzos.ZFileException e) {
            logger.atSevere().log("jzos file exception in write to file\n%s", e.getMessage());
            close();
		}
    }

    @Override
    public void close() {
        try {
            System.out.println("Close dataset");
            writer.close();
        } catch (com.ibm.jzos.ZFileException e) {
            logger.atSevere().log("jzos file exception in file close\n%s", e.getMessage());
		}
    }

}
