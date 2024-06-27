package org.genevaers.genevaio.dbreader;

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


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/*
 * A DatabaseConnection implementation that uses the exising 
 * workbench database connection
 */
public class WBConnection extends DatabaseConnection{

    private Connection sqlConnection;

    public WBConnection() {
    }

    @Override
    public void connect() throws SQLException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'connect'");
    }

    @Override
    public boolean isConnected() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'isConnected'");
    }

    //@Override
    public List<Integer> getExistingFolderIds(String folderIds) throws SQLException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getExistingFolderIds'");
    }

    //@Override
    public List<Integer> getViewIdsFromFolderIds(String folderIds) throws SQLException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getViewIdsFromFolderIds'");
    }

    @Override
    public ResultSet getResults(PreparedStatement ps) throws SQLException {
        return ps.executeQuery();
   }

    public void setSQLConnection(Connection c) {
        sqlConnection = c;
    }

    @Override
    public Connection getConnection() {
        return sqlConnection;
    }

    @Override
    public void closeStatement(PreparedStatement ps) throws SQLException {
        ps.close();
    }

    @Override
    public PreparedStatement prepareStatement(String query) throws SQLException {
            return sqlConnection.prepareStatement(query);
    }
    
}
