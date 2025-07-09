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


import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.google.common.flogger.FluentLogger;

import ComponentGenerator.model.generators.GeneratorBase;
import ComponentGenerator.model.segments.record.fields.FreemarkerFieldEntries;
import freemarker.template.Template;
import freemarker.template.TemplateException;

public class LTRecordGenerator extends GeneratorBase{

    private static final FluentLogger logger = FluentLogger.forEnclosingClass();
	private LtRecordSegment model;
	private Record prefix;
	private LtRecordSegment definition;

    public void writeOutputs(LtRecordSegment ltModel) {
		model = ltModel;

		logger.atConfig().log("------------------");
		logger.atInfo().log(" ");
		definition = ltModel;
		logger.atConfig().log("Generate Logic Table items");
		//first is the prefix - second the arg
		Iterator<RecordItem> ri = ltModel.getRecords().iterator();
		Record prefix = ri.next().getRecord();
		RecordItem argItem = ri.next();
		Record argRec = argItem.getRecord();
		while(ri.hasNext()) {
			RecordItem ltrentry = ri.next();
			Map<String, Object> nodeMap = populateNodeMap(prefix, argRec, ltrentry);
            writeJavaObject(ltrentry, nodeMap);
            writeRecordToDSECT(ltrentry, nodeMap);
        }
		Map<String, Object> nodeMap = populateNodeMap(prefix, argRec, argItem);
		writeJavaObject(argItem, nodeMap);
		logger.atConfig().log("--------------------------");
		logger.atInfo().log(" ");
    }

	private Map<String, Object> populateNodeMap(Record prefix, Record arg, RecordItem ltrentry) {
		logger.atConfig().log("Generate LT items from %s", ltrentry.getName());
		Map<String, Object> nodeMap = new HashMap<>();
		nodeMap.put("entries", prepocessTemplateEntries(prefix, arg, ltrentry.getRecord()));
		nodeMap.put("record", ltrentry.getRecord());
		nodeMap.put("imports", ltrentry.getRecord().getImports());
//		nodeMap.put("dsectEntry", new DSectEntryMethod());
		return nodeMap;
	}

	private FreemarkerFieldEntries prepocessTemplateEntries(Record prefix, Record arg, Record record) {
		// build lists of the strings to be written
		FreemarkerFieldEntries freeMarkerEntries = new FreemarkerFieldEntries(); 
		if(record.getRecordName().equals("LogicTableArg")) {
			freeMarkerEntries.addEntriesFrom(arg, false);
		} else {
			freeMarkerEntries.addEntriesFrom(prefix, arg, record);
		}
		return freeMarkerEntries;
	}
	
    private void writeRecordToDSECT(RecordItem ltrentry, Map<String, Object> nodeMap) {
        Path to = getPathToWriteTo(ltrentry);
        if (to != null) {
            writeModelWithTemplateToPath(nodeMap, "ltDsect.ftl", to);
            //writeLTRecordDocumentation(ltrentry, nodeMap);
        } else {
            logger.atInfo().log("DSECT generation ignoring %s", ltrentry);
        }
    }

    private void writeLTRecordDocumentation(String cr, Map<String, Object> nodeMap) {
		Template template;
		template = getTemplate("ltDocumentation.ftl");
		Path to = Paths.get("docs");
		to = to.resolve(cr.replace(" ", "") + ".md");
		generateTemplatedOutput(template, nodeMap, to);
    }

    private Path getPathToWriteTo(RecordItem ltrentry) {
		Path trg = Paths.get("docs/ltmac");
		trg.toFile().mkdirs();
        //Logic Table records have DSECTs defined by name - need a mapping
		String id = ltrentry.getRecord().getRecordType();
        if(id.equals("none")) {
            return null;
        } else {
		    return trg.resolve("GVBLT" + id +"A.mac");
        }
    }

    private void writeJavaObject(RecordItem ltrentry, Map<String, Object> nodeMap) {
		Path to = getPathToWriteJavaObjecPath(ltrentry.getName());
		writeModelWithTemplateToPath(nodeMap, "ltJavaObject.ftl", to);
    }

    private Path getPathToWriteJavaObjecPath(String recordName) {
		String dir = definition.getTargetDirectory();
		String pkg = definition.getPackageName();
		Path dirPath = Paths.get(dir);
		Path trg = dirPath.resolve(pkg.replace('.', '/'));
		trg.resolve(pkg);
		trg.toFile().mkdirs();
		return trg.resolve(recordName +".java");
    }


}
