package ComponentGenerator;

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

import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.common.flogger.FluentLogger;

import ComponentGenerator.model.GenevaModel;
import ComponentGenerator.model.segments.ModelSegment;
import ComponentGenerator.model.segments.Segment;
import ComponentGenerator.model.segments.SegmentFactory;
import freemarker.template.Configuration;
import freemarker.template.TemplateExceptionHandler;

public class ModelGenerator {

	private static final FluentLogger logger = FluentLogger.forEnclosingClass();
	private static int numErrors = 0;
	private Configuration cfg;
	private GenevaModel genevaModel;
	private ObjectMapper mapper = new ObjectMapper(new YAMLFactory());

	public void generateFrom(String modelConfig) throws IOException {
		// Iterate through the YAML files in the component directories
		// Or iterate throught the YAML files defined in a config file
		// this will give us more control
		// command line can override the config file
		mapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
		configureFreeMarker();
		generateSegmentsOfModel(modelConfig);
		// writeReadMe();
		logger.atConfig().log("Generation Completed");
	}

	// The model can be used to generate each of the desired outputs once built
	public void  generateSegmentsOfModel(String modelConfig) throws FileNotFoundException, IOException {
		logger.atConfig().log("Build the GenevaERS model");
		logger.atInfo().log("Read model defintion from %s\n", modelConfig);
		readModel(modelConfig);
		generateSegments();
	}


	private void readModel(String modelConfig) throws IOException {
		genevaModel = mapper.readValue(new File(modelConfig), GenevaModel.class);
	}

	private void generateSegments() throws StreamReadException, DatabindException, IOException {
		logger.atConfig().log("Write model segments");
		for(Segment s : genevaModel.getSegments())
		{
		 	logger.atInfo().log("%s from %s", s.getName(), s.getSource());
			ModelSegment modelSegment =  SegmentFactory.getModelSegment(s, mapper);
			modelSegment.writeOutputs(mapper, cfg);
		}
		logger.atConfig().log("--------------------------");
		logger.atInfo().log(" ");
	}



	private void configureFreeMarker() throws IOException {
		cfg = new Configuration(Configuration.VERSION_2_3_31);
		cfg.setDirectoryForTemplateLoading(new File("FreeMarkerTemplates"));
		cfg.setDefaultEncoding("UTF-8");
		cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
	}

	public static int getNumErrors() {
		return numErrors;
	}

}
