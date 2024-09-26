package org.genevaers.genevaio.dbreader;

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


import java.sql.SQLException;
import java.util.List;

import com.google.common.flogger.FluentLogger;

public class DBReader {
    private static final FluentLogger logger = FluentLogger.forEnclosingClass();
 
    protected static List<Integer> viewIds;
    private DatabaseConnection dbConnection;
    private DatabaseConnectionParams params;
    private boolean hasErrors = false;
    private DBViewsReader viewsReader = new DBViewsReader();
    private DBViewColumnsReader viewColumnsReader = new DBViewColumnsReader();
    private DBViewSourceReader viewSourceReader = new DBViewSourceReader();
    private DBViewColumnSourceReader viewColumnSourceReader = new DBViewColumnSourceReader();
    private DBSortKeyReader sortKeyReader = new DBSortKeyReader();
    private DBHeaderFooter headerFooterReader = new DBHeaderFooter();
    private DBControlRecordReader controlRecordReader = new DBControlRecordReader();
    private DBLookupsReader lookupsReader = new DBLookupsReader();
    private DBLogicalRecordReader logicalRecordReader = new DBLogicalRecordReader();
    private DBFieldReader fieldReader = new DBFieldReader();
    private DBLRIndexReader lrIndexReader = new DBLRIndexReader();
    private DBLogicalFileReader logicalFileReader = new DBLogicalFileReader();
    private DBPhysicalFileReader physicalFileReader = new DBPhysicalFileReader();
    private DBExitReader exitReader = new DBExitReader();
    private DBFoldersReader foldersReader = new DBFoldersReader();

    public List<Integer> getViewIds() {
        return viewIds;
    }

    public void addViewsToRepository(DatabaseConnectionParams pms) {
        params = pms;
        try {
            setConnectionType(params);
            dbConnection.connect();
            if(dbConnection.isConnected()) {
                addComponents();
            } 
        } catch (ClassNotFoundException | SQLException e) {
            logger.atSevere().log("DBReader addViewsToRepository failed\n%s", e.getMessage());
            hasErrors = true;
        }
    }

    private void addComponents() {
        hasErrors = foldersReader.addToRepo(dbConnection, params);
        hasErrors |= viewsReader.addToRepo(dbConnection, params);
        if(hasErrors == false) {
            logger.atFine().log("Views read");
            hasErrors |= viewSourceReader.addToRepo(dbConnection, params);
            logger.atFine().log("View sources read");
            hasErrors |= viewColumnsReader.addToRepo(dbConnection, params);
            logger.atFine().log("View columns read");
            hasErrors |= viewColumnSourceReader.addToRepo(dbConnection, params);
            logger.atFine().log("View column sources read");
            hasErrors |= sortKeyReader.addToRepo(dbConnection, params);
            logger.atFine().log("Sortkeys read");
            hasErrors |= headerFooterReader.addToRepo(dbConnection, params);
            logger.atFine().log("Headers and Footers read");
            hasErrors |= controlRecordReader.addToRepo(dbConnection, params);
            logger.atFine().log("Control records read");
            hasErrors |= lookupsReader.addToRepo(dbConnection, params);
            logger.atFine().log("lookups read");
            hasErrors |= logicalRecordReader.addToRepo(dbConnection, params);
            logger.atFine().log("LRs read");
            hasErrors |= fieldReader.addToRepo(dbConnection, params);
            logger.atFine().log("Fields read");
            hasErrors |= lrIndexReader.addToRepo(dbConnection, params);
            logger.atFine().log("Indexes read");
            hasErrors |= logicalFileReader.addToRepo(dbConnection, params);
            logger.atFine().log("LFs read");
            hasErrors |= physicalFileReader.addToRepo(dbConnection, params);
            logger.atFine().log("PFs read");
            hasErrors |= exitReader.addToRepo(dbConnection, params);
            logger.atFine().log("Exits read");
        }
    }

    /** 
     * Funtion to aid the workbench compiler
     * Problem is that the view source may not have been saved
     * Therefore just want the LR?
     * 
     * WB will supply a DataProvider - which is just a wrapper for the database connection
     * 
     */
    // public boolean loadViewSource(int envID, int view, int sourceNumber) {

    // }

    private DatabaseConnection setConnectionType(DatabaseConnectionParams params) throws ClassNotFoundException {
        switch(params.getDbType()) {
            case DB2:
            dbConnection = new DB2Connection(params);
            break;
            case POSTGRES:
            dbConnection = new PostgresConnection(params);
            break;
           default:
                break;
        }
        return null;
    }

    public boolean hasErrors() {
        return hasErrors;
    }

    public void addFromDatabaseConnection(DatabaseConnection dbc, DatabaseConnectionParams pms) {
        params = pms;
        dbConnection = dbc;
        addComponents();
    }


    
}
