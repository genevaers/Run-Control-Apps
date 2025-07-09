package ComponentGenerator.model.segments.components;

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


import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.flogger.FluentLogger;

import ComponentGenerator.model.generators.GeneratorBase;
import freemarker.template.Template;

public class ComponentGenerator extends GeneratorBase{

    private static final FluentLogger logger = FluentLogger.forEnclosingClass();
	private ComponentSegment definition;
	public void writeOutputs(ComponentSegment compModel) {
		logger.atConfig().log("Generate Java Component items");
		definition = compModel;
		Iterator<ComponentItem> compi = compModel.getComponents().iterator();
		while(compi.hasNext()) {
			ComponentItem compentry = compi.next();
			logger.atConfig().log("Generate Component items from %s", compentry.getName());
			//compWalker.buildEntryStrings(compentry.getComponent());
			Map<String, Object> nodeMap = new HashMap<>();
			nodeMap.put("component", compentry.getComponent());
            writeJavaObject(compentry.getName(), nodeMap);
            //writeDocumentation(compentry.getName(), nodeMap);
        }
		logger.atConfig().log("-----------------------------");
		logger.atInfo().log(" ");
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

    private void writeJavaObject(String compName, Map<String, Object> nodeMap) {
		Path to = getPathToWriteJavaObjecPath(compName);
		writeModelWithTemplateToPath(nodeMap, "compJavaObject.ftl", to);
    }

    private Path getPathToWriteJavaObjecPath(String compName) {
		String dir = definition.getTargetDirectory();
		String pkg = definition.getPackageName();
		Path dirPath = Paths.get(dir);
		Path trg = dirPath.resolve(pkg.replace('.', '/'));
		trg.resolve(pkg);
		trg.toFile().mkdirs();
		return trg.resolve(compName +".java");
    }

}
