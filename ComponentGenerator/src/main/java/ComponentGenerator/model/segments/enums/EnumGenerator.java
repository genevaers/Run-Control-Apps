package ComponentGenerator.model.segments.enums;

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


import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.flogger.FluentLogger;

import ComponentGenerator.model.generators.GeneratorBase;
import ComponentGenerator.model.segments.ModelSegment;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

public class EnumGenerator extends GeneratorBase {

    private static final FluentLogger logger = FluentLogger.forEnclosingClass();
	private EnumSegment definition;

    public void writeOutputs(EnumSegment emodel) {
		logger.atConfig().log("Generate Java Enum and Equates items");
		definition = emodel;
		String packName = emodel.getPackageName();
		Iterator<EnumItem> enumfi = emodel.getEnums().iterator();
		while(enumfi.hasNext()) {
			EnumItem enumFileEntry = enumfi.next();
			writeEnumFiles(packName, enumFileEntry);
			writeEquatesFile(definition.getPeTargetDirectory(), enumFileEntry);
        }
		logger.atConfig().log("------------------------------------");
		logger.atInfo().log(" ");
    }

    private void writeEquatesFile(String dir, EnumItem enumFileEntry) {
		MetaDataEnums enumEntires = enumFileEntry.getEnum();
		Map<String, Object> nodeMap = new HashMap<>();
		nodeMap.put("enumEntires", enumEntires);
		writeEquates("GVBEQS", nodeMap);
	}

	private void writeEnumFiles(String packName, EnumItem enumFileEntry) {
		logger.atConfig().log("Generate Enum items from %s", enumFileEntry.getName());
		MetaDataEnums enumEntires = enumFileEntry.getEnum();
		Iterator<GenevaEnum> enumEntryIt = enumEntires.getEnums().iterator();
		while (enumEntryIt.hasNext()) {
			GenevaEnum enumEntry = enumEntryIt.next();
			String enumName = enumEntry.getName();
			Map<String, Object> nodeMap = new HashMap<>();
			nodeMap.put("packageName", packName);
			nodeMap.put("enumName", StringUtils.capitalize(enumName));
			nodeMap.put("enumEntry", enumEntry);
			writeJavaEnum(enumName, nodeMap);
			// writeDocumentation(compentry.get("name").asText(), nodeMap);
		}
	}

	private void writeDocumentation(String cr, Map<String, Object> nodeMap) {
		Template template;
		template = getTemplate("compDocumentation.ftl");
		Path to = Paths.get("docs");
		to = to.resolve("Java" + cr.replace(" ", "") + ".md");
		generateTemplatedOutput(template, nodeMap, to);
	}

    private Path getPathToWriteTo(Map<String, Object> nodeMap) {
		Path trg = Paths.get("docs/ltmac");
		trg.toFile().mkdirs();
		LinkedHashMap<String, JsonNode> ltYaml = (LinkedHashMap<String, JsonNode>)nodeMap.get("ltrecs");
		String curRec = (String) nodeMap.get("currentRecord");
		JsonNode rid = ltYaml.get(curRec).get("recordType");
        //Logic Table records have DSECTs defined by name - need a mapping
		String id = rid.asText();
        if(id.equals("none")) {
            return null;
        } else {
		    return trg.resolve("GVBLT" + id +"A.mac");
        }
    }

    private void writeJavaEnum(String noSpaceName, Map<String, Object> nodeMap) {
		Path to = getPathToWriteJavaEnum(noSpaceName);
		writeModelWithTemplateToPath(nodeMap, "enum.ftl", to);
    }

    private Path getPathToWriteJavaEnum(String recordName) {
		String dir = definition.getTargetDirectory();
		String pkg = definition.getPackageName();
		Path dirPath = Paths.get(dir);
		Path trg = dirPath.resolve(pkg.replace('.', '/'));
		trg.resolve(pkg);
		trg.toFile().mkdirs();
		return trg.resolve(recordName +".java");
    }

    private void writeEquates(String noSpaceName, Map<String, Object> nodeMap) {
		Path to = getPathToWriteEquates(noSpaceName);
		writeModelWithTemplateToPath(nodeMap, "equates.ftl", to);
    }

    private Path getPathToWriteEquates(String eqName) {
		Path trg = Paths.get("docs/mac");
		trg.toFile().mkdirs();
		return trg.resolve(eqName +".mac");
    }

}
