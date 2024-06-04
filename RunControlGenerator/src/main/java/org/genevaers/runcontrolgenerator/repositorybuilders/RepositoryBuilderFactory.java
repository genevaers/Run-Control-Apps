package org.genevaers.runcontrolgenerator.repositorybuilders;

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

import com.google.common.flogger.FluentLogger;

import java.sql.Connection;

import org.genevaers.runcontrolgenerator.configuration.RunControlConfigration;

public class RepositoryBuilderFactory {
	private static final FluentLogger logger = FluentLogger.forEnclosingClass();
	private static Connection databaseConnection;

	public static RepositoryBuilder get() {

		switch (RunControlConfigration.getInputType()) {
			case "WBXML":
				return new WBXMLBuilder();
			case "VDPXML":
				return new VDPXMLBuilder();
			case "DB2":
				return new DB2Builder();
			case "POSTGRES":
				return new PostgresBuilder();
			case "WBCONNECTION":
				return new WBConnectionBuilder(databaseConnection);
			default:
				logger.atSevere().log("Unknown Input Type %s", RunControlConfigration.getInputType());
				return null;
		}
	}

	public static void setDatabaseConnection(Connection dbc) {
		databaseConnection = dbc;
	}

}
