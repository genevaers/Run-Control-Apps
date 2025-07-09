package org.genevaers.runcontrolgenerator.repositorybuilders;

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


import java.io.BufferedInputStream;
import java.io.FileNotFoundException;
import java.io.Reader;
import java.util.Collection;

import org.genevaers.repository.Repository;
import org.genevaers.repository.data.InputReport;
import org.genevaers.utilities.GersConfigration;
import org.genevaers.utilities.GersFile;
import org.genevaers.utilities.GersFilesUtils;
import org.genevaers.utilities.Status;

import com.google.common.flogger.FluentLogger;
import com.google.common.flogger.StackSize;

public abstract class XMLBuilder implements RepositoryBuilder{
	private static final FluentLogger logger = FluentLogger.forEnclosingClass();
	protected BufferedInputStream inputBuffer;
	protected Reader inputReader;
	private Status retval = Status.OK;


    public XMLBuilder() {
    }

    @Override
    public Status run() {
		readXMLs();
        return retval;
    }
	protected abstract Status buildFromXML(InputReport ir);
	protected abstract String getXMLDirectory();

	protected void readXMLs() {
		String activeDir = GersConfigration.getActiveXMLDirectory();
		logger.atInfo().log("Reading XML from %s", activeDir);
		Collection<GersFile> gfs = new GersFilesUtils().getGersFiles(activeDir);
		if(gfs != null && gfs.size() > 0) {
			logger.atInfo().log("found %d files in %s", gfs.size(), activeDir);
			for (GersFile gf : gfs) {
				logger.atFine().log("Read XML file from %s", gf.getName());
				try {
					this.inputReader = gf.getReader(gf.getName());
					InputReport ir = new InputReport();
					ir.setDdName(getXMLDirectory());
					ir.setMemberName(gf.getName());
					retval = buildFromXML(ir);
					Repository.addInputReport(ir);
				} catch (FileNotFoundException e) {
					logger.atSevere().withStackTrace(StackSize.FULL).log("Repo build failed " + e.getMessage());
					retval = Status.ERROR;
				}
			}
		} else {
			logger.atSevere().withStackTrace(StackSize.FULL).log("Repo build failed: no XML files found");
			retval = Status.ERROR;
		}
	}

}
