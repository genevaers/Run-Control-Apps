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
import org.genevaers.utilities.GersConfigration;
import org.genevaers.utilities.Status;

public class DB2Builder implements RepositoryBuilder{

    public DB2Builder() {
    }

    @Override
    public Status run() {
		Status retval = Status.OK;
		DatabaseConnectionParams conParams = new DatabaseConnectionParams();
		conParams.setDatabase(GersConfigration.getParm(GersConfigration.DB_DATABASE));
		conParams.setDbType(DbType.DB2);
		conParams.setEnvironmentID(GersConfigration.getParm(GersConfigration.ENVIRONMENT_ID));
		conParams.setPort(GersConfigration.getParm(GersConfigration.DB_PORT));
		conParams.setServer(GersConfigration.getParm(GersConfigration.DB_SERVER));
		conParams.setSchema(GersConfigration.getParm(GersConfigration.DB_SCHEMA));
		conParams.setUsername(System.getenv("TSO_USERID"));
		conParams.setPassword(System.getenv("TSO_PASSWORD"));
		DBReader dbr = new DBReader();
		dbr.addViewsToRepository(conParams);
		return dbr.hasErrors() ? Status.ERROR : Status.OK;
    }
    
}
