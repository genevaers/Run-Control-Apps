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


import java.sql.ResultSet;
import java.sql.SQLException;
import org.genevaers.repository.Repository;
import org.genevaers.repository.components.LogicalRecord;
import org.genevaers.repository.components.enums.LrStatus;
import org.genevaers.repository.data.CompilerMessage;
import org.genevaers.repository.data.CompilerMessageSource;

public class DBLogicalRecordReader extends DBReaderBase {

    @Override
    public boolean addToRepo(DatabaseConnection dbConnection, DatabaseConnectionParams params) {
        String query = "select "
            + "r.ENVIRONID, "
            + "r.LOGRECID, "
            + "r.NAME, "
            + "r.LRTYPECD, "
            + "r.LRSTATUSCD, "
            + "r.LOOKUPEXITID, "
            + "r.LOOKUPEXITSTARTUP "
            + "from " + params.getSchema() + ".LOGREC r " 
            + "where r.ENVIRONID = ? and r.LOGRECID in(" + getPlaceholders(requiredLRs.size()) + ") "
            + "order by r.LOGRECID; ";
        executeAndWriteToRepo(dbConnection, query, params, requiredLRs);
        return false;
    }

    @Override
    protected void addComponentToRepo(ResultSet rs) throws SQLException {
        LogicalRecord lr = new LogicalRecord();
        lr.setComponentId(rs.getInt("LOGRECID"));
        lr.setName(rs.getString("NAME"));
        lr.setStatus(LrStatus.fromdbcode(rs.getString("LRSTATUSCD")));
        if(lr.getStatus() == LrStatus.INACTIVE) {
            Repository.addErrorMessage(new CompilerMessage(0, CompilerMessageSource.VIEW_PROPS, lr.getComponentId(), 0, 0, "Logical record " + lr.getName() + "[" + lr.getComponentId() + "] is not active"));
        }
        int le = rs.getInt("LOOKUPEXITID");
        lr.setLookupExitID(le);
        if(le > 0) {
            requiredExits.add(le);
        }
        lr.setLookupExitParams(getDefaultedString(rs.getString("LOOKUPEXITSTARTUP"), ""));
        Repository.addLogicalRecord(lr);
    }
    
    public boolean addNamedToRepo(DatabaseConnection dbConnection, DatabaseConnectionParams params, String name) {
        String query = "select "
            + "r.ENVIRONID, "
            + "r.LOGRECID, "
            + "r.NAME, "
            + "r.LRTYPECD, "
            + "r.LRSTATUSCD, "
            + "r.LOOKUPEXITID, "
            + "r.LOOKUPEXITSTARTUP "
            + "from " + params.getSchema() + ".LOGREC r " 
            + "where r.ENVIRONID = ? and UPPER(r.NAME) = ? "
            + "order by r.LOGRECID; ";
        executeAndWriteToRepo(dbConnection, query, params, name);
    
        return false;
    }

    public void addLRToRepo(DatabaseConnection sqlConnection, DatabaseConnectionParams params, int environmentID, int sourceLR) {
                String query = "select "
                + "r.ENVIRONID, "
                + "r.LOGRECID, "
                + "r.NAME, "
                + "r.LRTYPECD, "
                + "r.LRSTATUSCD, "
                + "r.LOOKUPEXITID, "
                + "r.LOOKUPEXITSTARTUP "
                + "from " + params.getSchema() + ".LOGREC r " 
                + "where r.ENVIRONID = ? and r.LOGRECID = ?;";
            executeAndWriteToRepo(sqlConnection, query, params, sourceLR);
    }

}
