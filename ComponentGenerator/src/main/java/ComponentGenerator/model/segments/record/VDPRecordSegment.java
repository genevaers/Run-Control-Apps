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
import java.nio.file.Path;
import java.nio.file.Paths;

import tools.jackson.core.exc.JacksonIOException;
import tools.jackson.core.exc.StreamReadException;
import tools.jackson.databind.DatabindException;
import tools.jackson.databind.ObjectMapper;
import com.google.common.flogger.FluentLogger;

import freemarker.template.Configuration;


public class VDPRecordSegment extends RecordSegment {
    private static final FluentLogger logger = FluentLogger.forEnclosingClass();

    @Override
    public void writeOutputs(ObjectMapper mapper, Configuration cfg) throws StreamReadException, DatabindException, JacksonIOException {
		VDPRecordGenerator vrg = new VDPRecordGenerator(); //make static functions?
		vrg.setFreeMarkerCfg(cfg);
		//Collect the Records
		//Which means we need to read the yaml files defined in the records list
		//Note the Prefix is managed as its own record and so we need the list
		//And we generate from the collection not from each record on its own
		for(RecordItem ri : getRecords()) {
			String recFileName = getSourceDirectory() + "/" +ri.getSource();
			Record rec = mapper.readValue(new File(recFileName), Record.class);
			ri.setRecord(rec);
			logger.atInfo().log("Add %s from %s", ri.getName(), recFileName);
		}
		vrg.writeOutputs(this);
    }

    @Override
    public void readYaml() {
		// logger.atConfig().log("Read %s definition from %s", getName(), s.getSource());
		// if(s.getType().equals("record")) {
		// 	LtRecordSegment rs = (LtRecordSegment)s.getModel();
		// 	for(RecordItem ri : rs.getRecords()) {
		// 		String recFileName = s.getModel().getSourceDirectory() + "/" +ri.getSource();
		// 		Record rec = mapper.readValue(new File(recFileName), Record.class);
		// 		ri.setRecord(rec);
		// 		logger.atInfo().log("Add %s from %s", ri.getName(), recFileName);
		// 	}
    }

}
