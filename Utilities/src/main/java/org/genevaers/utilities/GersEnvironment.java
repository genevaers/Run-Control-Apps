package org.genevaers.utilities;

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


import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;

public class GersEnvironment {
	private static final String GERS_ENV_HLQ2 = "GERS_ENV_HLQ";
	private static final String OVERRIDE = "OVERRIDE";
	private static final String GVBLOAD = ".GVBLOAD";

	static Logger logger = Logger.getLogger("org.genevaers.utilities.FMSuiteEnvironment");

	private static Map<String, String> environmentVariables = new HashMap<>();

	private GersEnvironment() {
		initialiseFromTheEnvironment();
	}

	public static void initialiseFromTheEnvironment() {
		getEnvVarOrDefault("OSNAME", System.getProperty("os.name"));

		String locroot = System.getProperty("user.dir");
		locroot = locroot.replaceAll("^[Cc]:", "");
		locroot = locroot.replace("\\", "/");

		getEnvVarOrDefault("LOCALROOT", locroot);
		getEnvVarOrDefault("GERS_TEST_SPEC_LIST", "JBasespeclistV4.yaml");
		getEnvVarOrDefault("RUNTESTS", "N");
		getEnvVarOrDefault("CLEARLOCAL", "N");
		getEnvVarOrDefault("CLEARJUNIT", "Y");
		getEnvVarOrDefault("CLEARREMOTE", "N");
		getEnvVarOrDefault("PUTEVENTREF", "N");

		String dataSet = getEnvVarOrDefault("DATASET_PREFIX", "");
		String buildToTest = getEnvVarOrDefault("BUILD_TO_TEST", "");
		String pmhlq;
		if (buildToTest.isEmpty()) {
			if (dataSet.isEmpty()) {
				pmhlq = getEnvVarOrDefault(GERS_ENV_HLQ2, "");
			} else {
				pmhlq = dataSet;
				environmentVariables.put(GERS_ENV_HLQ2, pmhlq);
			}
		} else {
			pmhlq = buildToTest;
			environmentVariables.put(GERS_ENV_HLQ2, pmhlq);
		}
		if (getEnvVarOrDefault("PMLOAD", "").isEmpty()) {
			environmentVariables.put("PMLOAD", pmhlq + GVBLOAD);
		}
		if (getEnvVarOrDefault(OVERRIDE, "").isEmpty()) {
			environmentVariables.put(OVERRIDE, pmhlq + GVBLOAD);
		} else {
			environmentVariables.put(OVERRIDE, getEnvVarOrDefault(OVERRIDE, "") + GVBLOAD);
		}
		getEnvVarOrDefault("VDPXSD", pmhlq + ".GVBXSD(GVBSVDP)");
		getEnvVarOrDefault("GERS_TEST_HLQ", "");
		getEnvVarOrDefault("GERS_JOB_ACCT_INFO", "ACCT");
		getEnvVarOrDefault("GERS_JOB_CLASS", "A");
		getEnvVarOrDefault("GERS_MSG_LEVEL", "(1,1)");
		getEnvVarOrDefault("GERS_MSG_CLASS", "H");
		getEnvVarOrDefault("GERS_JAVA_HOME", "/Java/J17.0_64");
		getEnvVarOrDefault("GERS_JVM_PROC_LIB", "AJV.V11R0M0.PROCLIB");
		getEnvVarOrDefault("GERS_JZOS_LOAD_LIB", "AJV.V11R0M0.SIEALNKE");
		getEnvVarOrDefault("GERS_DB2_SUBSYSTEM", "DM12");
		getEnvVarOrDefault("GERS_DB2_LOAD_LIB", "DSN.V12R1M0.SDSNLOAD");
		getEnvVarOrDefault("GERS_DB2_EXIT_LIB", "DSN.V12R1M0.SDSNEXIT");
		getEnvVarOrDefault("GERS_DB2_RUN_LIB", "DSN121.RUNLIB.LOAD");
		getEnvVarOrDefault("GERS_DB2_UTILITY", "DSNTIA12");
		getEnvVarOrDefault("GERS_DB2_PLAN_SUFFIX", "");
		getEnvVarOrDefault("GERS_GIT_REPO_DIR", "");
		getEnvVarOrDefault("GERS_RCA_JAR_DIR", "");
		getEnvVarOrDefault("RUNOS", "ZOS");
		getEnvVarOrDefault("TSO_SERVER", "sp13.svl.ibm.com");
		getEnvVarOrDefault("OUTDIR", "out");
		getEnvVarOrDefault("GENERATE_COVERAGE", "N");
		getEnvVarOrDefault("GENERATE_VDPFLOW", "N");
		getEnvVarOrDefault("TSO_USERID", "");
		getEnvVarOrDefault("TSO_PASSWORD", "");
		getEnvVarOrDefault("COVERAGEONLY", "N");
		getEnvVarOrDefault("MESSAGE_COVERAGE", "N");
		getEnvVarOrDefault("REF_TRACE", "N");
		getEnvVarOrDefault("EXTRACT_TRACE", "N");
		getEnvVarOrDefault("VIEW", "0");
		getEnvVarOrDefault("FROMREC", "0");
		getEnvVarOrDefault("THRUREC", "0");
		getEnvVarOrDefault("FROMLTROW", "0");
		getEnvVarOrDefault("THRULTROW", "0");
		getEnvVarOrDefault("FROMCOL", "0");
		getEnvVarOrDefault("THRUCOL", "0");
	}

	private static String getEnvVarOrDefault(String env, String def) {
        String value = System.getenv(env);
		if (value == null) {
			value = def;
		}
		environmentVariables.put(env.toUpperCase(), value);
		return value;
	}

	public static String get(String key) {
		if(environmentVariables.isEmpty()) {
			initialiseFromTheEnvironment();
		}
		return environmentVariables.get(key);
	}

	public void dumpVariables() {
		logger.info("Environment Variables:\n");
		for (Entry<String, String> entry : environmentVariables.entrySet()) {
			String value = entry.getValue();
			if(entry.getKey().equalsIgnoreCase("TSO_PASSWORD")) {
				value = "******";
			}
			String msg = String.format("%s=%s%n", entry.getKey(), value);
			System.out.print(msg);
		}
	}

	public static boolean isWindows() {
		return get("OSNAME").startsWith("Win");
	}
	
	public static Map<String, String> getEnvironmentVariables() {
		return environmentVariables;
	}
}
