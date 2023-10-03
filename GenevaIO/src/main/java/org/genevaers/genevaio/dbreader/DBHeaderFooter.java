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
import org.genevaers.repository.components.ReportFooter;
import org.genevaers.repository.components.ReportHeader;
import org.genevaers.repository.components.ViewNode;
import org.genevaers.repository.components.enums.JustifyId;
import org.genevaers.repository.components.enums.ReportFunction;

public class DBHeaderFooter extends DBReaderBase {

	private ReportHeader rh;
	private ReportFooter rf;
    
    @Override
    public boolean addToRepo(DatabaseConnection dbConnection, DatabaseConnectionParams params) {
        String query = "select * from " + params.getSchema() + ".viewheaderfooter "
        + "where environid = " + params.getEnvironmenID() + " and viewid in(" + params.getViewIds()+ ");";

        executeAndWriteToRepo(dbConnection, query);
        return hasErrors;
    }

    @Override
    protected void addComponentToRepo(ResultSet rs) throws SQLException {
        ViewNode view = Repository.getViews().get(rs.getInt("VIEWID"));
        if(view != null) {
            if(rs.getInt("HEADERFOOTERIND") == 1) {
                rh = new ReportHeader();
                parseHeader(rs);
                view.addReportHeader(rh);
            } else {
                rf = new ReportFooter();
                parseFooter(rs);
                view.addReportFooter(rf);
            }		
        }
    }
    
	private void parseFooter(ResultSet rs) throws SQLException  {
		rf.setComponentId(rs.getInt("HEADERFOOTERID"));
		String field = rs.getString("STDFUNCCD").trim();
		if(field.length() > 0) {
			rf.setFunction(ReportFunction.fromdbcode(field));
		} else {
			rf.setFunction(ReportFunction.INVALID);
		}
		field = rs.getString("JUSTIFYCD").trim();
		if(field.length() > 0) {
			rf.setJustification(JustifyId.fromdbcode(field));
		} else {
			rf.setJustification(JustifyId.NONE);
		}
		rf.setRow(rs.getShort("ROWNUMBER"));
		rf.setColumn(rs.getShort("COLNUMBER"));
		rf.setFooterLength(rs.getShort("LENGTH"));
		rf.setText(rs.getString("ITEMTEXT").trim());
	}

	private void parseHeader(ResultSet rs) throws SQLException  {
		rh.setComponentId(rs.getInt("HEADERFOOTERID"));
		String field = rs.getString("STDFUNCCD").trim();
		if(field.length() > 0) {
			rh.setFunction(ReportFunction.fromdbcode(field));
		} else {
			rh.setFunction(ReportFunction.INVALID);
		}
		field = rs.getString("JUSTIFYCD").trim();
		if(field.length() > 0) {
			rh.setJustification(JustifyId.fromdbcode(field));
		} else {
			rh.setJustification(JustifyId.NONE);
		}
		rh.setRow(rs.getShort("ROWNUMBER"));
		rh.setColumn(rs.getShort("COLNUMBER"));
		rh.setTitleLength(rs.getShort("LENGTH"));
		rh.setText(rs.getString("ITEMTEXT").trim());
	}
}
