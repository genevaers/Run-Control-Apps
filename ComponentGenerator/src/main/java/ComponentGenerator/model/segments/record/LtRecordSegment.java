package ComponentGenerator.model.segments.record;

/*
 * Copyright Contributors to the GenevaERS Project. SPDX-License-Identifier: Apache-2.0 (c) Copyright IBM Corporation 2008
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

import tools.jackson.core.exc.JacksonIOException;
import tools.jackson.core.exc.StreamReadException;
import tools.jackson.databind.DatabindException;
import tools.jackson.databind.ObjectMapper;
import com.google.common.flogger.FluentLogger;

import freemarker.template.Configuration;

public class LtRecordSegment extends RecordSegment {
    private static final FluentLogger logger = FluentLogger.forEnclosingClass();

    @Override
    public void readYaml() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void writeOutputs(ObjectMapper mapper, Configuration cfg) throws StreamReadException, DatabindException, JacksonIOException {
		LTRecordGenerator ltg = new LTRecordGenerator(); //make static functions?
		ltg.setFreeMarkerCfg(cfg);
		//Collect the Records
		//Which means we need to read the yaml files defined in the records list
		//Note the Prefix and Arg are managed in their own record and so we need the list
		//And we generate from the collection not from each record on its own
		for(RecordItem ri : getRecords()) {
			String recFileName = getSourceDirectory() + "/" +ri.getSource();
			Record rec = mapper.readValue(new File(recFileName), Record.class);
			ri.setRecord(rec);
			logger.atInfo().log("Add %s from %s", ri.getName(), recFileName);
		}
		ltg.writeOutputs(this);
    }

}
