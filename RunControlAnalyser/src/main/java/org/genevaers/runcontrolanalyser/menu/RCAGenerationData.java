package org.genevaers.runcontrolanalyser.menu;

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

import org.genevaers.runcontrolanalyser.AnalyserDriver;
import org.genevaers.utilities.CommandRunner;

import com.google.common.flogger.FluentLogger;

public class RCAGenerationData {
    private static final FluentLogger logger = FluentLogger.forEnclosingClass();

    private static String rcSet;
    private static String dbServer = "sp13.svl.ibm.com";
    private static String dbDatabase = "DM12";
    private static String dbPort = "5033";
    private static String dbSchema;
    private static String envId;
    private static String viewIds;
    private static String wbxmli = "..";
    private static String localRC = ".";
    private static String hlq;

    private static AnalyserDriver flow;

    public static String getRcSet() {
        return rcSet;
    }

    public static void setRcSet(String rcSet) {
        RCAGenerationData.rcSet = rcSet;
    }

    public static String getDbServer() {
        return dbServer;
    }

    public static void setDbServer(String dbServer) {
        RCAGenerationData.dbServer = dbServer;
    }

    public static String getDbDatabase() {
        return dbDatabase;
    }

    public static void setDbDatabase(String dbDatabase) {
        RCAGenerationData.dbDatabase = dbDatabase;
    }

    public static String getDbPort() {
        return dbPort;
    }

    public static void setDbPort(String dbPort) {
        RCAGenerationData.dbPort = dbPort;
    }

    public static String getDbSchema() {
        return dbSchema;
    }

    public static void setDbSchema(String dbSchema) {
        RCAGenerationData.dbSchema = dbSchema;
    }

    public static String getEnvId() {
        return envId;
    }

    public static void setEnvId(String envId) {
        RCAGenerationData.envId = envId;
    }

    public static String getViewIds() {
        return viewIds;
    }

    public static void setViewIds(String viewIds) {
        RCAGenerationData.viewIds = viewIds;
    }

    public static String getWbxmli() {
        return wbxmli;
    }

    public static void setWbxmli(String wbxmli) {
        RCAGenerationData.wbxmli = wbxmli;
    }

    public static String getHlq() {
        return hlq;
    }

    public static void setHlq(String hlq) {
        RCAGenerationData.hlq = hlq;
    }

    public static String getLocalRC() {
        return localRC;
    }

    public static void setLocalRC(String localRC) {
        RCAGenerationData.localRC = localRC;
    }

    // These functions probably do not belong here
    // They should be static in a RCAMenuExecutor or something
    public static void runRCG(Path rcDir) {
        CommandRunner runner = new CommandRunner();
        try {
            String os = System.getProperty("os.name");
            if(os.startsWith("Windows")) {
                runner.run("gvbrcg.bat", rcDir.toFile());
            } else {
                runner.run("gvbrcg", rcDir.toFile());
            }
        } catch (IOException | InterruptedException e) {
            logger.atSevere().log("runRCG failed\n%s", e.getMessage());
        }
    }

    /*
     * The game is all about setting up to call this function.
     * The function above may be called first to generate
     * a local RC set. Once the appropriate MR91Parms have been configured.
     * Or we may get the RC set by transferring it via FTP.
     * Or we may just be processing/re-processling a local RC set.
     */
    public static void generateFlow() {
        try {
            AnalyserDriver.makeRunControlAnalyserDataStore(null);
            AnalyserDriver.setTargetDirectory(RCAGenerationData.getRcSet());
            AnalyserDriver.generateFlowDataFrom(RCAGenerationData.getRcSet(),
                    true, // default to generate
                    false,
                    "");
        } catch (Exception e) {
            logger.atSevere().log("generateFlow failed\n%s", e.getMessage());
        }
    }

    public static void openDataStore() {
        flow.openDataStore();
    }

    public static void ftpRunControlDatasets(String dbServer2, String hlq, String rc, String user,
            String pwd) throws IOException {
                flow.ftpRunControlDatasets(dbServer2, hlq, rc, user, pwd);
    }

    public static void setFlow(AnalyserDriver f) {
        flow = f;
    }

}
