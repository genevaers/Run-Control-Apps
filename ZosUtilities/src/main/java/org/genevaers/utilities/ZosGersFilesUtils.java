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


import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import com.google.common.flogger.FluentLogger;
import com.ibm.jzos.PdsDirectory;
import com.ibm.jzos.PdsDirectory.MemberInfo;
import com.ibm.jzos.ZFile;
import com.ibm.jzos.ZFileConstants;
import com.ibm.jzos.ZFileException;

public class ZosGersFilesUtils extends GersFilesUtils{
	private static final FluentLogger logger = FluentLogger.forEnclosingClass();

	Collection<GersFile> gersFiles = new ArrayList<>();

	public Collection<GersFile> getGersFiles(String dir) {
		try {
			String ddname = "//DD:" + dir;
			ZFile dd = new ZFile(ddname, "r");
			// Problem here is that this will be a PDS and we need to iterate its memebers
			int type = dd.getDsorg();
			switch (type) {
				case ZFileConstants.DSORG_PDSE: 
					logger.atInfo().log("found PDS E");
					//Drop through
				case ZFileConstants.DSORG_PDS_DIR: {
					logger.atInfo().log("found PDS");
					String pdsName = dd.getActualFilename();
					logger.atInfo().log("Actual filename " + pdsName);
					try {
						PdsDirectory pds = new PdsDirectory(ddname);
						Iterator pdsi = pds.iterator();
						while (pdsi.hasNext()) {
							MemberInfo mem = (MemberInfo) pdsi.next();
							String mname = mem.getName();
							String buildName = ddname + "(" + mname + ")";
							logger.atInfo().log("Build Repo from " + buildName);
							GersFile gf = new GersFile();
							gf.setName(buildName.substring(5)); //strip the //DD:
							gersFiles.add(gf);
						}
					} catch (IOException e) {
						logger.atSevere().log("new PDSDirectory failed %s",e);
					}
				}
					break;
				case ZFileConstants.DSORG_PDS_MEM:
				case ZFileConstants.DSORG_PS:
				default:
					logger.atSevere().log("Unhandled DSORG " + type);
			}
			dd.close();
		} catch (ZFileException e) {
			logger.atSevere().log("readFromDataSet error %s", e.getMessage());
		}
		return gersFiles;
	}

}
