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


import org.genevaers.genevaio.wbxml.WBXMLSaxIterator;
import org.genevaers.repository.data.InputReport;
import org.genevaers.utilities.GersConfigration;
import org.genevaers.utilities.Status;

import com.google.common.flogger.FluentLogger;
import com.google.common.flogger.StackSize;

public class WBXMLBuilder extends XMLBuilder{
	private static final FluentLogger logger = FluentLogger.forEnclosingClass();

    public WBXMLBuilder() {
    }

    @Override
    protected Status buildFromXML(InputReport ir) {
        Status retval;
		logger.atInfo().log("Read WBXML");
		WBXMLSaxIterator wbReader = new WBXMLSaxIterator();
		try {
			wbReader.setInputReader(inputReader);
			wbReader.addToRepsitory();
			ir.setGenerationID(wbReader.getGenerationID());
			retval = Status.OK;
		} catch (Exception e) {
			logger.atSevere().withStackTrace(StackSize.FULL).log("Repo build failed " + e.getMessage());
			retval = Status.ERROR;
		}
		return retval;
	}

    @Override
    protected String getXMLDirectory() {
        return GersConfigration.getWBXMLDirectory();
    }


    
}
