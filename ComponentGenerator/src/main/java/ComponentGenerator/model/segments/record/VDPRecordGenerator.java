package ComponentGenerator.model.segments.record;

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

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.google.common.flogger.FluentLogger;

import ComponentGenerator.DSectEntryMethod;
import ComponentGenerator.model.generators.GeneratorBase;
import ComponentGenerator.model.segments.ModelSegment;
import ComponentGenerator.model.segments.record.fields.FreemarkerFieldEntries;
import freemarker.ext.beans.BeansWrapperBuilder;
import freemarker.template.Configuration;
import freemarker.template.Template;


public class VDPRecordGenerator extends GeneratorBase {

	private static final FluentLogger logger = FluentLogger.forEnclosingClass();
	private Record prefix;
	private VDPRecordSegment model;

	private void generateRecordOutputs(RecordItem recordItem) {
		String recName = recordItem.getName();
		String source = recordItem.getSource();
		logger.atConfig().log("Generate VDP %s items from %s", recName, source);
		writeRecordOutputs(recordItem);
	}

	private void logVDPTargets(ModelSegment vdpModel) {
		logger.atConfig().log("Generate VDP items");
		logger.atConfig().log("Write Java to %s", vdpModel.getTargetDirectory());
		logger.atConfig().log("Write DSECTS to %s", vdpModel.getPeTargetDirectory());
	}

	private void writeRecordOutputs(RecordItem recordItem) {
		//Preprocess template strings
		Map<String, Object> nodeMap = new HashMap<>();
		nodeMap.put("entries", prepocessTemplateEntries(recordItem.getRecord()));
		nodeMap.put("record", recordItem.getRecord());
		nodeMap.put("imports", recordItem.getRecord().getImports());
		nodeMap.put("dsectEntry", new DSectEntryMethod());
		nodeMap.put("statics", new BeansWrapperBuilder(Configuration.VERSION_2_3_31).build().getStaticModels());
		writeJavaObject(recordItem, nodeMap);
		//writeRecordToDSECT(recordItem, nodeMap);
	}

	private FreemarkerFieldEntries prepocessTemplateEntries(Record record) {
		// build lists of the strings to be written
		FreemarkerFieldEntries freeMarkerEntries = new FreemarkerFieldEntries(); 
		if(record.getRecordId() == 0) {
			freeMarkerEntries.addEntriesFrom(record, true);
		} else {
			freeMarkerEntries.addEntriesFrom(prefix, record);
		}
		return freeMarkerEntries;
	}


	public void writeJavaObject(RecordItem recordItem, Map<String, Object> nodeMap) {
		Path to = getPathToWriteJavaObjecPath(recordItem);
		writeModelWithTemplateToPath(nodeMap, "vdpJavaObject.ftl", to);
	}

	public void writeRecordToDSECT(RecordItem recordItem, Map<String, Object> nodeMap) {
		//None parts have ID > 0
		int recID = recordItem.getRecord().getRecordId();
		if(recID > 0) {
			Path to = getPathToWriteTo(recID);
			writeModelWithTemplateToPath(nodeMap, "vdpDsect.ftl", to);
			//writeVdpRecordDocumentation(recordItem, nodeMap);
		} else {
			logger.atInfo().log("DSECT generation ignoring %s", recordItem);
		}
	}

	private void writeVdpRecordDocumentation(RecordItem recordItem, Map<String, Object> nodeMap) {
		Template template;
		try {
			template = getFreeMarkerCfg().getTemplate("vdpDocumentation.ftl");
			Path to = Paths.get("docs");
			to = to.resolve(recordItem.getName().replace(" ", "") + ".md");
			generateTemplatedOutput(template, nodeMap, to);
		} catch (IOException e) {
			logger.atSevere().log("File errors "+ e);
		}
	}

	private Path getPathToWriteJavaObjecPath(RecordItem recordItem) {
		String dir = model.getTargetDirectory();
		String pkg = model.getPackageName();
		Path dirPath = Paths.get(dir);
		Path trg = dirPath.resolve(pkg.replace('.', '/'));
		trg.resolve(pkg);
		trg.toFile().mkdirs();
		return trg.resolve(recordItem.getRecord().getRecordName() +".java");
	}

	private Path getPathToWriteTo(int recordID) {
		Path trg = Paths.get("docs/mac");
		trg.toFile().mkdirs();
		String num = String.format("%04d", recordID);
		return trg.resolve("GVB" + num +"A.mac");
	}

	public void writeOutputs(VDPRecordSegment vdpModel) {
		model = vdpModel;
		logVDPTargets(vdpModel);
		Iterator<RecordItem> ri = vdpModel.getRecords().iterator();
		prefix = ri.next().getRecord();
		while(ri.hasNext()) {
			generateRecordOutputs(ri.next());
		}
		logger.atConfig().log("------------------");
		logger.atInfo().log(" ");
	}

}
