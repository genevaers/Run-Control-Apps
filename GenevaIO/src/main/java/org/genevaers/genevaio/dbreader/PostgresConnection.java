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


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.postgresql.ds.PGSimpleDataSource;
import com.google.common.flogger.FluentLogger;

public class PostgresConnection extends DatabaseConnection {
    private static final FluentLogger logger = FluentLogger.forEnclosingClass();

    private DatabaseConnectionParams params;
    private Connection con;

    public PostgresConnection(DatabaseConnectionParams params) {
        this.params = params;
    }

    @Override
    public void connect() throws SQLException {
        PGSimpleDataSource ds = new PGSimpleDataSource();
        ds.setServerName("localhost");
        ds.setDatabaseName(params.getDatabase());
        ds.setUser(params.getUsername());
        ds.setPassword(params.getPassword());
        ds.setCurrentSchema(params.getSchema());
        logger.atInfo().log("Built the Postgres Datasource");
        con  = ds.getConnection();
        logger.atInfo().log("Connected");
    }
    
    @Override
    public boolean isConnected() {
        boolean retval = false;
        try {
            retval =  con.isValid(10);
        } catch (SQLException e) {
            logger.atSevere().log("isConnected failed\n%s", e.getMessage());
        }
        return retval;
    }

    //@Override
    public List<Integer> getExistingFolderIds(String folderIds) {
        return null;
    }

    //@Override
    public List<Integer> getViewIdsFromFolderIds(String folderIds) {
        return null;
    }

    @Override
    public Connection getConnection() {
        return con;
    }

    @Override
    public PreparedStatement prepareStatement(String query) throws SQLException {
        return con.prepareStatement(query);
    }

    @Override
    public ResultSet getResults(PreparedStatement ps) throws SQLException {
        return ps.executeQuery();
    }

    @Override
    public void closeStatement(PreparedStatement ps) throws SQLException {
        ps.close();
    }

}
