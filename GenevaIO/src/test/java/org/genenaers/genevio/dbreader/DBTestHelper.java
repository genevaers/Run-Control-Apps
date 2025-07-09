package org.genenaers.genevio.dbreader;

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


import org.genevaers.genevaio.dbreader.DatabaseConnectionParams;
import org.genevaers.genevaio.dbreader.DatabaseConnection.DbType;

public class DBTestHelper {

    // In order for the database tests to run the relevant database schema needs to have been
    // populated from the WBXMLs preserved here in the test resources

    public static DatabaseConnectionParams getPostgresParams(String folders, String views) {
        DatabaseConnectionParams params = new DatabaseConnectionParams();
        params.setDbType(DbType.POSTGRES);
        params.setDatabase("genevaers");
        params.setEnvironmentID("1");
        params.setPort("5432");
        params.setSchema("gendev");
        params.setServer("localhost");
        params.setUsername(System.getenv("PG_USERID"));
        params.setPassword(System.getenv("PG_PASSWORD"));
        params.setFolderIds(folders);
        params.setViewIds(views);
       return params;
    }

    public static DatabaseConnectionParams getDB2Params(String folders, String views) {
        DatabaseConnectionParams params = new DatabaseConnectionParams();
        params.setDbType(DbType.DB2);
        params.setDatabase("DM12");
        params.setEnvironmentID("308");
        params.setPort("5033");
        params.setSchema("SAFRWBGD");
        params.setServer("sp13.svl.ibm.com");
        params.setUsername(System.getenv("TSO_USERID"));
        //params.setPassword(System.getenv("PGPASSWD"));
        params.setPassword(System.getenv("TSO_PASSWORD"));
        params.setFolderIds(folders);
        params.setViewIds(views);
        return params;
    }
    
}

