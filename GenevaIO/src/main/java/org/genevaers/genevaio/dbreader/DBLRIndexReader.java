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

import java.sql.ResultSet;
import java.sql.SQLException;

import org.genevaers.repository.Repository;
import org.genevaers.repository.components.LRIndex;
import org.genevaers.repository.components.LogicalRecord;

public class DBLRIndexReader extends DBReaderBase {

    private int lastlrid;
    private boolean effectiveDatesMade = false;
    private LRIndex efsi;
    private LRIndex efei;

    @Override
    public boolean addToRepo(DatabaseConnection dbConnection, DatabaseConnectionParams params) {
        String query = "select distinct "
                + "i.lrindexid, "
                + "i.logrecid, "
                + "f.lrindexfldid, "
                + "f.fldseqnbr, "
                + "f.lrfieldid, "
                + "effdatestartfldid, "
                + "effdateendfldid "
                + "from " + params.getSchema() + ".lrindex i "
                + "inner join  " + params.getSchema() + ".lrindexfld f "
                + "on(f.environid = i.environid and f.lrindexid = i.lrindexid) "
                + "where i.ENVIRONID = ? and i.logrecid in(" + getPlaceholders(requiredLRs.size()) + ") "
                + "order by logrecid;";
        executeAndWriteToRepo(dbConnection, query, params, requiredLRs);
        return hasErrors;
    }

    @Override
    protected void addComponentToRepo(ResultSet rs) throws SQLException {
        LRIndex lri = new LRIndex();
        int id = rs.getInt("LRINDEXID");
        lri.setComponentId(id);
        int rslrid = rs.getInt("LOGRECID");
        if(lastlrid != rslrid) {
            lastlrid = rslrid;
            effectiveDatesMade = false;
        }
        if (effectiveDatesMade == false) {
            changingLRCheckAndAddEffectiveDateIndexes(rs);
        }

        lri.setLrId(rs.getInt("LOGRECID"));
        lri.setKeyNumber(rs.getShort("FLDSEQNBR"));
        lri.setEffectiveDateStart(false);
        lri.setEffectiveDateEnd(false);
        lri.setName("Primary");
        lri.setFieldID(rs.getInt("LRFIELDID"));
        Repository.addLRIndex(lri);
    }

    private void changingLRCheckAndAddEffectiveDateIndexes(ResultSet rs) throws SQLException {
        int id = rs.getInt("LRINDEXID");
        int lrid = rs.getInt("LOGRECID");
        LogicalRecord lr = Repository.getLogicalRecords().get(lrid);
        Integer efstart = rs.getInt("effdatestartfldid");
        if (efstart > 0) {
            efsi = new LRIndex();
            efsi.setComponentId(id);
            efsi.setLrId(rs.getInt("LOGRECID"));
            efsi.setKeyNumber((short) (lr.getValuesOfIndexBySeq().size() + 1));
            efsi.setEffectiveDateEnd(false);
            efsi.setFieldID(rs.getInt("EFFDATESTARTFLDID"));
            efsi.setEffectiveDateStart(true);
            efsi.setName("Starting Effective Date");
            effectiveDatesMade = true;
            effdateStarts.put(id, efsi);
        }
        if (rs.getInt("EFFDATEENDFLDID") > 0) {
            efei = new LRIndex();
            efei.setComponentId(id);
            efei.setLrId(rs.getInt("LOGRECID"));
            efei.setKeyNumber((short) (lr.getValuesOfIndexBySeq().size() + 1));
            efei.setEffectiveDateStart(false);
            efei.setFieldID(rs.getInt("EFFDATEENDFLDID"));
            efei.setEffectiveDateEnd(true);
            efei.setName("End Effective Date");
            effdateEnds.put(id, efei);
            effectiveDatesMade = true;
        }
    }

    public void addLRToRepo(DatabaseConnection dbConnection, DatabaseConnectionParams params, int environmentID,
            int sourceLR) {
        String query = "select distinct "
                + "i.lrindexid, "
                + "i.logrecid, "
                + "f.lrindexfldid, "
                + "f.fldseqnbr, "
                + "f.lrfieldid, "
                + "effdatestartfldid, "
                + "effdateendfldid "
                + "from " + params.getSchema() + ".lrindex i "
                + "inner join  " + params.getSchema() + ".lrindexfld f "
                + "on(f.environid = i.environid and f.lrindexid = i.lrindexid) "
                + "where i.ENVIRONID = ? and i.logrecid = ?; ";
        executeAndWriteToRepo(dbConnection, query, params, sourceLR);
    }

}
