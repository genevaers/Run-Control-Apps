package org.genevaers.genevaio.dbreader;

import java.sql.PreparedStatement;

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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections4.SetUtils;
import org.apache.commons.collections4.SetUtils.SetView;
import org.genevaers.repository.Repository;
import org.genevaers.repository.components.ViewDefinition;
import org.genevaers.repository.components.enums.OutputMedia;
import org.genevaers.repository.components.enums.ViewStatus;
import org.genevaers.repository.components.enums.ViewType;
import org.genevaers.utilities.GersConfigration;
import org.genevaers.utilities.IdsReader;

import com.google.common.flogger.FluentLogger;

public class DBViewsReader extends DBReaderBase {
    private static final FluentLogger logger = FluentLogger.forEnclosingClass();

    private static List<String> linesRead = new ArrayList<>();

    String queryBase = "SELECT "
    + "v.ENVIRONID, "
    + "VIEWID, "
    + "NAME, "
    + "EFFDATE, "
    + "VIEWSTATUSCD, "
    + "VIEWTYPECD, "
    + "EXTRACTFILEPARTNBR, "
    + "OUTPUTMEDIACD, "
    + "OUTPUTLRID, "
    + "PAGESIZE, "
    + "LINESIZE, "
    + "ZEROSUPPRESSIND, "
    + "EXTRACTMAXRECCNT, "
    + "EXTRACTSUMMARYIND, "
    + "EXTRACTSUMMARYBUF, "
    + "OUTPUTMAXRECCNT, "
    + "CONTROLRECID, "
    + "WRITEEXITID, "
    + "WRITEEXITSTARTUP, "
    + "FORMATEXITID, "
    + "FORMATEXITSTARTUP, "
    + "FILEFLDDELIMCD, "
    + "FILESTRDELIMCD, "
    + "DELIMHEADERROWIND, "
    + "v.LFPFASSOCID, "
    + "a.PHYFILEID, "
    + "FORMATFILTLOGIC, "
    + "v.CREATEDUSERID, "
    + "v.LASTMODUSERID ";


    @Override
    public boolean addToRepo(DatabaseConnection dbConnection, DatabaseConnectionParams params) {
        decideOnViewIds();
         if(viewIds.size() > 0) {
            logViewIds();
            verifyViewsExist(dbConnection, params); //Do we need to verify - should find out anyway?
            if(hasErrors == false) {
                addViewsToRepo(dbConnection, params);
            }
        } else {
            logger.atSevere().log("No views defined");
            hasErrors = true;
        }
        return hasErrors;
    }



    private void decideOnViewIds() {
        if(Repository.getRunviews().size() > 0) {
            logger.atInfo().log("Overriding viewids with Runviews");
            viewIds = Repository.getRunviews();
        } else {
            viewIds.addAll(IdsReader.getIdsFrom(GersConfigration.DBVIEWS));
            linesRead.addAll(IdsReader.getLinesRead());    
        }
    }



    private void logViewIds() {
        logger.atInfo().log("Views used to build the RC Files");
        Iterator<Integer> vi = viewIds.iterator();
        while (vi.hasNext()) {
            logger.atInfo().log("view %d ", vi.next());
        }
    }



    private void verifyViewsExist(DatabaseConnection dbConnection, DatabaseConnectionParams params) {
        Set<Integer> viewsFound = new HashSet<>();
        String viewsQuery = "select VIEWID from " + params.getSchema() + ".view where ENVIRONID = ?"
                + " and VIEWID IN(" + getPlaceholders(viewIds.size()) + ") and VIEWSTATUSCD='ACTVE'";
        try (PreparedStatement ps = dbConnection.prepareStatement(viewsQuery);) {
            int parmNum = 1;
            ps.setInt(parmNum++, params.getEnvironmentIdAsInt());
            Iterator<Integer> vi = viewIds.iterator();
            while (vi.hasNext()) {
                ps.setInt(parmNum++, vi.next());
            }
            ResultSet rs = dbConnection.getResults(ps);
            while (rs.next()) {
                viewsFound.add(rs.getInt("VIEWID"));
            }
            checkAllFound(viewsFound);
        } catch (SQLException e) {
            logger.atSevere().log("verifyViewsExist failed %s", e.getMessage());
            hasErrors = true;
        }
    }

    private void checkAllFound(Set<Integer> views) {
        SetView<Integer> diff = SetUtils.difference(viewIds, views);
        if(diff.size() > 0) {
            hasErrors = true;
            logMissingViews(diff);
        } else {
            logger.atFine().log("All views found");
            hasErrors = false;
        }
    }


    private void logMissingViews(SetView<Integer> diff) {
        logger.atSevere().log("Not all views found");
        Iterator<Integer> di = diff.iterator();
        while (di.hasNext()) {
            logger.atSevere().log("view %d missing or not active", di.next());
        }
    }



    private void addViewsToRepo(DatabaseConnection dbConnection, DatabaseConnectionParams params) {

        String viewsQuery = queryBase + " from " + params.getSchema() + ".View v " 
        + "LEFT JOIN " + params.getSchema() + ".LFPFASSOC a ON(v.LFPFASSOCID = a.LFPFASSOCID and v.environid = a.environid) "
        + "where v.environid = ? and viewid IN(" + getPlaceholders(viewIds.size()) + ");";

        executeAndWriteToRepo(dbConnection, viewsQuery, params, viewIds);
    }

    @Override
    protected void addComponentToRepo(ResultSet rs) throws SQLException {
        //Have to be able to generate this bit...
        ViewDefinition vd = new ViewDefinition();
        vd.setComponentId(rs.getInt("VIEWID"));
        vd.setName(rs.getString("NAME"));
        vd.setStatus(ViewStatus.fromdbcode(rs.getString("VIEWSTATUSCD")));
        // + "OUTPUTLRID, "
        vd.setOutputPageSizeMax((short)rs.getInt("PAGESIZE"));
        vd.setOutputLineSizeMax((short)rs.getInt("LINESIZE"));
        // + "FILEFLDDELIMCD, "
        // + "FILESTRDELIMCD, "
        // + "DELIMHEADERROWIND, "
        // + "FORMATFILTLOGIC, "
        vd.setViewType(ViewType.fromdbcode(rs.getString("VIEWTYPECD")));
        vd.setOutputMedia(OutputMedia.fromdbcode(rs.getString("OUTPUTMEDIACD")));
        vd.setExtractFileNumber((short)rs.getInt("EXTRACTFILEPARTNBR"));
        vd.setMaxExtractSummaryRecords(rs.getInt("EXTRACTSUMMARYBUF"));
        vd.setOutputLrId(rs.getInt("OUTPUTLRID"));
        vd.setDefaultOutputFileId(rs.getInt("PHYFILEID"));
        // this one can be dropped vd.setOutputDestinationId(outputDestinationId);
        // not sure whatvd.setDetailed(outputDetailInd);
        vd.setZeroValueRecordSuppression(rs.getInt("ZEROSUPPRESSIND") == 0 ? false : true);
        vd.setGenerateDelimitedHeader(rs.getInt("DELIMHEADERROWIND") == 0 ? false : true);
        vd.setExtractMaxRecCount(rs.getInt("EXTRACTMAXRECCNT"));
        vd.setOutputMaxRecCount(rs.getInt("OUTPUTMAXRECCNT"));
        //vd.setProcessAsofDate(processAsofDate);
        //vd.setLookupAsofDate(lookupAsofDate);
        //vd.setFillErrorValue(fillErrorValue);
        //vd.setFillTruncationValue(fillTruncationValue);
        vd.setExtractSummarized(rs.getInt("EXTRACTSUMMARYIND") == 0 ? false : true);
        vd.setWriteExitId(rs.getInt("WRITEEXITID"));
        vd.setWriteExitParams(getDefaultedString(rs.getString("WRITEEXITSTARTUP"), ""));
        vd.setFormatExitId(rs.getInt("FORMATEXITID"));
        vd.setFormatExitParams(getDefaultedString(rs.getString("FORMATEXITSTARTUP"), ""));
        vd.setControlRecordId(rs.getInt("CONTROLRECID"));

        //These probably should not be here
        vd.setOwnerUser(rs.getString("CREATEDUSERID"));
        vd.setOwnerUser(rs.getString("LASTMODUSERID"));

        currentViewNode = Repository.getViewNodeMakeIfDoesNotExist(vd);
        currentViewNode.setFormatFilterLogic(rs.getString("FORMATFILTLOGIC"));
    }

    public boolean hasErrors() {
        return hasErrors;
    }

    public static  List<String> getLinesRead() {
        if(linesRead.isEmpty()) {
            linesRead.add("<none>");
        }
        return linesRead;
    }
}
