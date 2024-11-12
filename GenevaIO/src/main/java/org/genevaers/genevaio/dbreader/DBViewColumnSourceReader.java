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
import org.genevaers.repository.components.ViewColumnSource;
import org.genevaers.repository.components.ViewNode;
import org.genevaers.repository.components.enums.ColumnSourceType;

public class DBViewColumnSourceReader extends DBReaderBase{

    @Override
    public boolean addToRepo(DatabaseConnection dbConnection, DatabaseConnectionParams params) {
        String query = "select "
            + "VIEWCOLUMNSOURCEID, "
            + "VIEWCOLUMNID, "
            + "VIEWSOURCEID, "
            + "VIEWID, "
            + "SOURCETYPEID, "
            + "CONSTVAL, "
            + "LOOKUPID, "
            + "LRFIELDID, "
            + "SORTTITLELOOKUPID, "
            + "SORTTITLELRFIELDID, "
            + "EXTRACTCALCLOGIC "
            + "from " + params.getSchema() + ".viewcolumnsource "
            + "where environid = ? and viewid in(" + getPlaceholders(viewIds.size()) + ") ";

        executeAndWriteToRepo(dbConnection, query, params, viewIds);
        return hasErrors;
    }

    @Override
    protected void addComponentToRepo(ResultSet rs) throws SQLException {
        ViewNode view = Repository.getViews().get(rs.getInt("VIEWID"));
        if(view != null) {
            ViewColumnSource vcs = new ViewColumnSource();
            vcs.setComponentId(rs.getInt("VIEWCOLUMNSOURCEID"));
            vcs.setColumnID(rs.getInt("VIEWCOLUMNID"));
            vcs.setViewSourceId(rs.getInt("VIEWSOURCEID"));
            vcs.setViewId(rs.getInt("VIEWID"));
            vcs.setSourceType(ColumnSourceType.values()[rs.getInt("SOURCETYPEID")]);
            vcs.setSrcValue(rs.getString("CONSTVAL"));
            vcs.setValueLength(rs.getString("CONSTVAL").length());
            vcs.setSrcJoinId(rs.getInt("LOOKUPID"));
            vcs.setViewSrcLrFieldId(rs.getInt("LRFIELDID"));
            //We now need to support sort key titles
            //Need to update the generation to include these here
            vcs.setLogicText(rs.getString("EXTRACTCALCLOGIC"));
            vcs.setSortTitleFieldId(rs.getInt("SORTTITLELRFIELDID"));
            vcs.setSortTitleLookupId(rs.getInt("SORTTITLELOOKUPID"));
            view.addViewColumnSource(vcs);
        } else {
           //Can there ever be an else here... ?
           hasErrors = true;            
        }
    }
    
}
