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
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import com.google.common.flogger.FluentLogger;

import java.sql.ResultSet;

public class DB2Connection extends DatabaseConnection{
    private static final FluentLogger logger = FluentLogger.forEnclosingClass();
    
    private DatabaseConnectionParams params;
    private Connection con;

    DB2Connection(DatabaseConnectionParams params) throws ClassNotFoundException {
        this.params = params;
		loadDriver();
    }

    @Override
    public void connect() throws SQLException {
        String url = "jdbc:db2://" + params.getServer()+ ":"
        + params.getPort() + "/"
        + params.getDatabase();

        con = DriverManager.getConnection(url, params.getUsername(), params.getPassword());
   }

//    public void connectUserOnly() throws SQLException {

//         Properties properties = new Properties(); 
//         properties.put("user", params.getUsername()); 
//         properties.put("passowrd", params.getPassword()); 
//         properties.put("port", params.getPort()); 
//         properties.put("securityMechanism", new String("" + com.ibm.db2.jcc.DB2BaseDataSource.USER_ONLY_SECURITY + "")); 
//         String url = "jdbc:db2://" + params.getServer()+ ":"
//         + params.getPort() + "/"
//         + params.getDatabase();
//         con = DriverManager.getConnection(url, properties); 
//     }

    @Override   
    public boolean isConnected() {
        boolean retval = false;
        try {
            retval =  con.isValid(10);
        } catch (SQLException e) {
            logger.atSevere().log("DB2Connection failed %s", e.getMessage() );
        }
        return retval;
    }

    void loadDriver() throws ClassNotFoundException{
		// Load the DB2 JDBC Type 4 Driver with DriverManager
		Class.forName("com.ibm.db2.jcc.DB2Driver");
    }

    //@Override
    public List<Integer> getExistingFolderIds(String folderIds) throws SQLException {
        String folderQuery;
        folderQuery = "select VIEWFOLDERID from ";
        folderQuery += params.getSchema();
        folderQuery += ".viewfolder v where ENVIRONID = ";
        folderQuery += params.getEnvironmentID();
        folderQuery += " and VIEWFOLDERID IN(";
        folderQuery += params.getFolderIds();
        folderQuery += ")";
        PreparedStatement ps = con.prepareStatement(folderQuery);

        ResultSet rs = ps.executeQuery();
        List<Integer> fldrs = new ArrayList<>();
        while(rs.next()) {
            fldrs.add(rs.getInt("VIEWFOLDERID"));
        }
        return fldrs;
    }

    //@Override
    public List<Integer> getViewIdsFromFolderIds(String folderIds) {
        List<Integer> views = new ArrayList<>();
        String viewsQuery = "select viewid  from " + params.getSchema() + ".vfgetPlaceholdersvassoc vf "
        + "where vf.environid = ? and vf.viewfolderid in(?);";
        try(PreparedStatement ps = con.prepareStatement(viewsQuery);) {
            ResultSet rs = ps.executeQuery();
            while(rs.next()) {
                views.add(rs.getInt("viewid"));
            }
        } catch (SQLException e) {
            logger.atSevere().log("getViewIdsFromFolderIds error %s", e.getMessage());
        }
        return views;
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
