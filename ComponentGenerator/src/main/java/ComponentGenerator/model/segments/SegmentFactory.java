package ComponentGenerator.model.segments;

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

import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.flogger.FluentLogger;

import ComponentGenerator.model.segments.components.ComponentSegment;
import ComponentGenerator.model.segments.enums.EnumSegment;
import ComponentGenerator.model.segments.functioncodes.FunctionCodeSegment;
import ComponentGenerator.model.segments.record.LtRecordSegment;
import ComponentGenerator.model.segments.record.VDPRecordSegment;

/**
 * Factory to read the ModelSegment of the supplied type
 */
public class SegmentFactory {
	private static final FluentLogger logger = FluentLogger.forEnclosingClass();
    
	public static ModelSegment getModelSegment(Segment s,  ObjectMapper mapper)
			throws IOException, StreamReadException, DatabindException {
		ModelSegment segModel = null;
		if(s.getType().equalsIgnoreCase("record")) {
            if(s.getName().equalsIgnoreCase("VDPRecords")) {
			    segModel = mapper.readValue(new File(s.getSource()), VDPRecordSegment.class);
            } else if (s.getName().equalsIgnoreCase("LTRecords")) {
			    segModel = mapper.readValue(new File(s.getSource()), LtRecordSegment.class);
            }
		} else if(s.getType().equalsIgnoreCase("component")){
			segModel = mapper.readValue(new File(s.getSource()), ComponentSegment.class);
		} else if(s.getType().equalsIgnoreCase("enum")) {
			segModel = mapper.readValue(new File(s.getSource()), EnumSegment.class);
		} else if(s.getType().equalsIgnoreCase("functionCodes")) {
			segModel = mapper.readValue(new File(s.getSource()), FunctionCodeSegment.class);
		} else {
			logger.atSevere().log("Unknown segment type %s", s.getType());
		}
		return segModel;
	}
}
