package org.genevaers.genevaio.dbreader;

import java.sql.Connection;

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
import java.util.Map;

import org.genevaers.genevaio.dataprovider.CompilerDataProvider;
import org.genevaers.repository.Repository;
import org.genevaers.repository.components.LogicalFile;
import org.genevaers.repository.components.LogicalRecord;
import org.genevaers.repository.components.LookupPath;
import org.genevaers.repository.components.PhysicalFile;
import org.genevaers.repository.components.ViewNode;

public class LazyDBReader implements CompilerDataProvider {
 
    private DatabaseConnectionParams params;
    private DatabaseConnection databaseConnection;
    private Connection sqlConnection;
    private int environmentId;
    private int sourceLogicalRecordID;

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

    public void setSQLConnection(Connection c) {
        sqlConnection = c;
    }

    public Connection getSqlConnection() {
        return sqlConnection;
    }

    public void setDatabaseConnection(DatabaseConnection databaseConnection) {
        this.databaseConnection = databaseConnection;
    }

    public DatabaseConnection getDatabaseConnection() {
        return databaseConnection;
    }

    @Override
    public Integer findExitID(String name, boolean procedure) {
        DBExitReader exitReader = new DBExitReader();
        exitReader.addToRepoByName(databaseConnection, params, name, procedure);
        return 0;
    }

    @Override
    public Integer findPFAssocID(String lfName, String pfName) {
        DBLogicalFileReader lfReader = new DBLogicalFileReader();
        lfReader.addToRepoByName(databaseConnection, params, lfName.toUpperCase());
        DBPhysicalFileReader pFileReader = new DBPhysicalFileReader();
        pFileReader.addToRepoByName(databaseConnection, params, pfName.toUpperCase());
        LogicalFile lf = Repository.getLogicalFiles().get(lfName);
        if(lf != null) {
            PhysicalFile pf = Repository.getPhysicalFiles().get(pfName);
            if(pf != null) {
                lf.addPF(pf);
            }
        }
        return lfReader.getLFPFAssocID(databaseConnection, params, lfName, pfName); // To be derived
    }

    @Override
    public Map<String, Integer> getFieldsFromLr(int id) {
        return null;
    }

    @Override
    public Map<String, Integer> getLookupTargetFields(String name) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getLookupTargetFields'");
    }

    @Override
    public void setEnvironmentID(int environmentId) {
        this.environmentId =  environmentId;
    }

    @Override
    public int getEnvironmentID() {
        return environmentId;
    }

    @Override
    public void setLogicalRecordID(int lrid) {
        sourceLogicalRecordID = lrid;
    }

    @Override
    public void loadLR(int environmentID, int sourceLR) {
        DBLogicalRecordReader lrReader = new DBLogicalRecordReader();
        lrReader.addLRToRepo(databaseConnection, params, environmentID, sourceLR);
        DBLRIndexReader lIndexReader = new DBLRIndexReader();
        lIndexReader.addLRToRepo(databaseConnection, params, environmentID, sourceLR);
        DBFieldReader fieldReader = new DBFieldReader();
        fieldReader.addLRToRepo(databaseConnection, params, environmentID, sourceLR);
    }

    public void setParams(DatabaseConnectionParams params) {
        this.params = params;
    }

    @Override
    public LogicalRecord getLogicalRecord(int id) {
        //Uncomment me for more info Repository.dumpLRs();
        LogicalRecord lr = Repository.getLogicalRecords().get(id);
        if(lr == null) {
          loadLR(environmentId, id);
          lr = Repository.getLogicalRecords().get(id);
        } 
        if(lr.getLookupExitID() > 0) {
            DBExitReader exitReader = new DBExitReader();
            exitReader.addToRepoById(databaseConnection, params, lr.getLookupExitID());              
        }
        return lr;
    }

    @Override
    public LookupPath getLookup(String name) {
        LookupPath lk = Repository.getLookups().get(name);
        if(lk == null) {
          loadLookup(environmentId, name);
          lk = Repository.getLookups().get(name);
        } 
        getDependenciesForLookup(lk);
        return lk;
    }

    private void getDependenciesForLookup(LookupPath lk) {
        DBLogicalRecordReader lrReader = new DBLogicalRecordReader();
        lrReader.addToRepo(databaseConnection, params);
        DBFieldReader fr = new DBFieldReader();
        fr.addToRepo(databaseConnection, params);
        DBLRIndexReader lrir = new DBLRIndexReader();
        lrir.addToRepo(databaseConnection, params);
        DBLogicalFileReader lfReader = new DBLogicalFileReader();
        lfReader.addToRepo(databaseConnection, params);
        DBPhysicalFileReader pFileReader = new DBPhysicalFileReader();
        pFileReader.addToRepo(databaseConnection, params);
        DBExitReader er = new DBExitReader();
        er.addToRepo(databaseConnection, params);
    }

    private LogicalFile getLogicalFile(int lfid) {
        LogicalFile lf = Repository.getLogicalFiles().get(lfid);
        if(lf == null) {
            loadLogicalFile(lfid);
            lf = Repository.getLogicalFiles().get(lfid);
        }
        return lf;
    }


    private void loadLogicalFile(int lfid) {
        DBLogicalFileReader lfReader = new DBLogicalFileReader();
        lfReader.addLFtoRepo(databaseConnection, params, environmentId, lfid);
    }

    private void loadLookup(int environmentId, String name) {
        DBLookupsReader lkReader = new DBLookupsReader();
        lkReader.addNamedLookupToRepo(databaseConnection, params, environmentId, name);

    }

    @Override
    public ViewNode getView(int id) {
        ViewNode vw = Repository.getViews().get(id);
        if(vw == null) {
          loadView(environmentId, id);
          vw = Repository.getViews().get(id);
        } 
        return vw;
    }

    private void loadView(int environmentId2, int id) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'loadView'");
    }

    @Override
    public void setEnvironmentName(String name) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'setEnvironmentName'");
    }

    @Override
    public String getEnvironmentName() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getEnvironmentName'");
    }

    
}
