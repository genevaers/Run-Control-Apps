package ComponentGenerator.model;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;


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

import java.util.Map;
import com.google.common.flogger.FluentLogger;

import ComponentGenerator.model.generators.GeneratorBase;

/**
 * Write the whole model dot files using FreeMarker.
 */
public class GenevaWholeGenerator extends GeneratorBase {
    private static final FluentLogger logger = FluentLogger.forEnclosingClass();
    private GenevaWholeModel wholeModel;

    public void writeOutputs(GenevaWholeModel wm) {
        logger.atConfig().log("Generate Model Diagrams");
        wholeModel = wm;
        Map<String, Object> nodeMap = new HashMap<>();
        nodeMap.put("wm", wholeModel);
        writeRecord2ComponentMapping("VDPRecs2Components", nodeMap);
        writeVDPManagmentRecords("VDPMngrecs", nodeMap);
        writeLogicTableRecordsAndFunctionCodes("LTandFCs", nodeMap);
        writeFunctionCodeDescriptions("FCDescriptions", nodeMap);
        logger.atConfig().log("-----------------------------");
        logger.atInfo().log(" ");
    }

    private void writeFunctionCodeDescriptions(String name, Map<String, Object> nodeMap) {
        Path to = getPathToWriteJavaObjecPath(name);
        writeModelWithTemplateToPath(nodeMap, "ltFcDescriptions.ftl", to);
    }

    private void writeLogicTableRecordsAndFunctionCodes(String name, Map<String, Object> nodeMap) {
        Path to = getPathToWriteJavaObjecPath(name);
        writeModelWithTemplateToPath(nodeMap, "ltAndFcs.ftl", to);
    }

    private void writeVDPManagmentRecords(String name, Map<String, Object> nodeMap) {
        Path to = getPathToWriteJavaObjecPath(name);
        writeModelWithTemplateToPath(nodeMap, "vdpManagementRecords.ftl", to);
    }

    private void writeRecord2ComponentMapping(String name, Map<String, Object> nodeMap) {
        Path to = getPathToWriteJavaObjecPath(name);
        writeModelWithTemplateToPath(nodeMap, "vdprecs2Components.ftl", to);
    }

    private Path getPathToWriteJavaObjecPath(String name) {
        String dir = wholeModel.getTargetDirectory();
        Path trg = Paths.get(dir);
        trg.toFile().mkdirs();
        return trg.resolve(name + ".dot");
    }

}
