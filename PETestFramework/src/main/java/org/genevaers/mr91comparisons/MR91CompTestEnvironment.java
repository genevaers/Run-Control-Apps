package org.genevaers.mr91comparisons;

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


import java.util.HashMap;
import java.util.Map;

// From a command line generate the params.xsl file

public class MR91CompTestEnvironment {

	private static Map<String, String> environmentVariables = new HashMap<String, String>();
	
	private  MR91CompTestEnvironment() {
	}

	public static void initialiseFromTheEnvironment() {
		String locroot = System.getProperty("user.dir");
    	locroot = locroot.replaceAll("^[Cc]:", "");
    	locroot = locroot.replace("\\", "/");

		getEnvVarOrDefault("LOCALROOT", locroot); 			
		getEnvVarOrDefault("GERS_TEST_SPEC_LIST", "fmspeclist.xml"); 			
		getEnvVarOrDefault("RUNTESTS", "N"); 			
		getEnvVarOrDefault("CLEARLOCAL", "N"); 			
		getEnvVarOrDefault("CLEARJUNIT", "Y"); 			
		getEnvVarOrDefault("CLEARREMOTE", "N"); 			
		getEnvVarOrDefault("PUTEVENTREF", "N"); 			
		//Build Forge has the performance engine HLQ in DATASET_PREFIX
		//Don't like this name but get it from there if it exists
		String dataSet = getEnvVarOrDefault("DATASET_PREFIX", ""); 
		String buildToTest = getEnvVarOrDefault("BUILD_TO_TEST", ""); 
		String pmhlq;
		if(buildToTest.isEmpty()) {
			if(dataSet.isEmpty()) {
				pmhlq = getEnvVarOrDefault("GERS_ENV_HLQ", "");		
			} else {
				pmhlq = dataSet;
				environmentVariables.put("GERS_ENV_HLQ", pmhlq);
			}
		} else {
			pmhlq = buildToTest;
			environmentVariables.put("GERS_ENV_HLQ", pmhlq);			
		}
		if(getEnvVarOrDefault("PMLOAD", "").isEmpty()) {
			environmentVariables.put("PMLOAD", pmhlq + ".GVBLOAD");
		}
		if(getEnvVarOrDefault("OVERRIDE", "").isEmpty()) {
			environmentVariables.put("OVERRIDE", pmhlq + ".GVBLOAD");
		}
		getEnvVarOrDefault("VDPXSD", pmhlq + ".GVBXSD(GVBSVDP)");
		getEnvVarOrDefault("GERS_TEST_HLQ", ""); 
		getEnvVarOrDefault(" GERS_DB2_SUBSYSTEM", "DM11");
		getEnvVarOrDefault("GERS_DB2_LOAD_LIB", "DSN.V11R1M0.SDSNLOAD"); 
		getEnvVarOrDefault("GERS_DB2_EXIT_LIB", "DSN.V11R1M0.SDSNEXIT");
		getEnvVarOrDefault("GERS_DB2_RUN_LIB", "DSN111.RUNLIB.LOAD");
		getEnvVarOrDefault("  GERS_DB2_UTILITY", "DSNTIA11");
		getEnvVarOrDefault("RUNOS", "ZOS"); 
		getEnvVarOrDefault("TSO_SERVER", "sp13.svl.ibm.com");
		getEnvVarOrDefault("OUTDIR", "out"); 
		getEnvVarOrDefault("GENERATE_COVERAGE", "N");
		getEnvVarOrDefault("GENERATE_VDPFLOW", "N");
		getEnvVarOrDefault("TSO_USERID", "");
		getEnvVarOrDefault("TSO_PASSWORD", "");
		getEnvVarOrDefault("COVERAGEONLY", "N");
		getEnvVarOrDefault("MESSAGE_COVERAGE", "N");
		getEnvVarOrDefault("COMPARE_MR91", "N");
		getEnvVarOrDefault("MR91TESTTYPE", "SINGLERUN");
		getEnvVarOrDefault("MR91ENV", "308");
		getEnvVarOrDefault("MR91SCHEMA", "SAFRWBGD");
		getEnvVarOrDefault("MR91CMPSRC", "CPPMR91");
		getEnvVarOrDefault("MR91CMPTRG", "gvbrca");
		getEnvVarOrDefault("EXTRACT_TRACE", "N");
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
		return environmentVariables.get(key);
	}
	
	public void dumpVariables() {
		for(String key : environmentVariables.keySet()) {
			String value = environmentVariables.get(key);
			if(key.equalsIgnoreCase("TSO_PASSWORD")) {
				value = "******";
			}
			System.out.format("%s=%s%n", key, value);
		}
	}
	
	public static Map<String, String> getEnvironmentVariables() {
		return environmentVariables;
	}

    public static void set(String member, String value) {
		environmentVariables.put(member.toUpperCase(), value);
    }
}
