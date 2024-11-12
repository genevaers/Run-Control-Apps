package org.genevaers.utilities;

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


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.logging.StreamHandler;

import com.google.common.flogger.FluentLogger;
import com.ibm.jzos.ZFile;
import com.ibm.jzos.ZFileException;

public class ZosGersFile extends GersFile{
	private static final FluentLogger logger = FluentLogger.forEnclosingClass();

    public Writer getWriter(String name) throws IOException {
        ZFile dd = new ZFile("//DD:" + name, "w");
		return new OutputStreamWriter(dd.getOutputStream(), "IBM-1047");
    }

    public Reader getReader(String name) {
		String ddname = "//DD:" + name;
		logger.atInfo().log("Read %s", ddname);
		try {
			return new BufferedReader(new InputStreamReader(new ZFile(ddname, "r").getInputStream()));
		} catch (ZFileException e) {
			logger.atSevere().log("Zos GersFile getReader failed %s", e.getMessage());
		}
		return null;
    }

    public boolean exists(String name) {
		boolean retval = false;
		String dd = "//DD:" + name;
		try {
			retval = ZFile.exists(dd);
		} catch (ZFileException e) {
			logger.atSevere().log("Zfile %s", e.getMessage());
		} 
		return retval;
    }

    public StreamHandler getFileHandler(String name) throws SecurityException, IOException {
	    ZFileHandler zfh = new ZFileHandler();
		zfh.setFile(name);
		return zfh;
    }
	

}
