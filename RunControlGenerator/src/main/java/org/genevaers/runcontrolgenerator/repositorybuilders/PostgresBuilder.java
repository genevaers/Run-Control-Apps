package org.genevaers.runcontrolgenerator.repositorybuilders;

/*
 * Copyright Contributors to the GenevaERS Project.
								SPDX-License-Identifier: Apache-2.0 (c) Copyright IBM Corporation
								2008
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


import org.genevaers.genevaio.dbreader.DBReader;
import org.genevaers.genevaio.dbreader.DatabaseConnection.DbType;
import org.genevaers.genevaio.dbreader.DatabaseConnectionParams;
import org.genevaers.runcontrolgenerator.configuration.RunControlConfigration;
import org.genevaers.runcontrolgenerator.utility.Status;

public class PostgresBuilder implements RepositoryBuilder{

    public PostgresBuilder() {
    }

    @Override
    public Status run() {
		DatabaseConnectionParams conParams = new DatabaseConnectionParams();
		conParams.setDatabase(RunControlConfigration.getParm(RunControlConfigration.DB_DATABASE));
		conParams.setDbType(DbType.POSTGRES);
		conParams.setEnvironmentID(RunControlConfigration.getParm(RunControlConfigration.ENVIRONMENT_ID));
		conParams.setPort(RunControlConfigration.getParm(RunControlConfigration.DB_PORT));
		conParams.setServer(RunControlConfigration.getParm(RunControlConfigration.DB_SERVER));
		conParams.setSchema(RunControlConfigration.getParm(RunControlConfigration.DB_SCHEMA));
		conParams.setUsername(System.getenv("PG_USERID"));
		conParams.setPassword(System.getenv("PG_PASSWORD"));
		DBReader dbr = new DBReader();
		dbr.addViewsToRepository(conParams);
		return dbr.hasErrors() ? Status.ERROR : Status.OK;
    }
    
}
