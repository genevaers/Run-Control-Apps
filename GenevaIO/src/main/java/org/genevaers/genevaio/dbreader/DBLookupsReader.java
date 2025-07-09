package org.genevaers.genevaio.dbreader;

import java.sql.Connection;
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
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import org.genevaers.repository.Repository;
import org.genevaers.repository.components.LookupPathKey;
import org.genevaers.repository.components.enums.DataType;
import org.genevaers.repository.components.enums.DateCode;
import org.genevaers.repository.components.enums.JustifyId;

import com.google.common.flogger.FluentLogger;

public class DBLookupsReader extends DBReaderBase{
    private static final FluentLogger logger = FluentLogger.forEnclosingClass();

    private Set<Integer> lookupIds;

    @Override
    public boolean addToRepo(DatabaseConnection dbConnection, DatabaseConnectionParams params) {
        getSourceLookupIds(dbConnection, params);
        if(lookupIds.size() > 0) {
            String query = "SELECT 	distinct "
                + "l.LOOKUPID, "
                + "l.NAME, "
                + "l.VALIDIND as VALID, "
                + "l.SRCLRID as LKUPSRCLR, "
                + "DESTLRLFASSOCID, "
                + "s.STEPSEQNBR, "
                + "s.LOOKUPSTEPID, "
                + "s.SRCLRID as STEPSRCLR, "
                + "s.LRLFASSOCID as STEPLRLF, "
                + "k.KEYSEQNBR, "
                + "k.FLDTYPE, "
                + "K.LRFIELDID, "
                + "k.LRLFASSOCID as KEYLRLF, "
                + "k.VALUEFMTCD, "
                + "k.SIGNED, "
                + "k.VALUELEN, "
                + "k.DECIMALCNT, "
                + "k.FLDCONTENTCD, "
                + "k.ROUNDING, "
                + "k.JUSTIFYCD, "
                + "k.MASK, "
                + "k.SYMBOLICNAME, "
                + "k.VALUE, "
                + "t.LOGRECID, "
                + "t.LOGFILEID "
                + "FROM " + params.getSchema() + ".LOOKUP l "
                + "INNER JOIN " + params.getSchema() + ".LOOKUPSTEP s "
                + "ON(l.LOOKUPID = s.LOOKUPID AND l.environid = s.environid) "
                + "INNER JOIN " + params.getSchema() + ".LOOKUPSRCKEY k "
                + "ON(k.LOOKUPID = s.LOOKUPID AND s.LOOKUPSTEPID = k.LOOKUPSTEPID AND l.environid = k.environid) "
                + "INNER JOIN " + params.getSchema() + ".LRLFASSOC t "
                + "ON(s.LRLFASSOCID = t.LRLFASSOCID AND l.environid = t.environid) "
                + "where l.environid = ? and l.lookupid in(" + getPlaceholders(lookupIds.size()) + ") "
                + "ORDER BY l.LOOKUPID, s.STEPSEQNBR, k.KEYSEQNBR; ";
            executeAndWriteToRepo(dbConnection, query, params, lookupIds);
            return hasErrors;
        } else {
            logger.atInfo().log("No Lookups required");
            return false;
        }
    }

    private void getSourceLookupIds(DatabaseConnection dbConnection, DatabaseConnectionParams params) {
        String query= "SELECT 	DISTINCT LOOKUPID FROM "
            + params.getSchema() + ".VIEWLOGICDEPEND d "
            + "WHERE d.ENVIRONID = ? AND d.VIEWID in(" + getPlaceholders(viewIds.size()) + ") AND LOOKUPID > 0 "
            + "union "
            +"SELECT DISTINCT SORTTITLELOOKUPID as LOOKUPID FROM "
            + params.getSchema() + ".VIEWCOLUMNSOURCE s "
            + "WHERE s.ENVIRONID = ? AND s.VIEWID in(" + getPlaceholders(viewIds.size()) + ") AND SORTTITLELOOKUPID > 0";
        
            lookupIds = new TreeSet<>();
            try(PreparedStatement ps = dbConnection.prepareStatement(query);) {
                int parmNum = 1;
                ps.setInt(parmNum++, params.getEnvironmentIdAsInt());
                Iterator<Integer> ii = viewIds.iterator();
                while(ii.hasNext()) {
                    ps.setInt(parmNum++, ii.next());
                }
                ps.setInt(parmNum++, params.getEnvironmentIdAsInt());
                Iterator<Integer> ii2 = viewIds.iterator();
                while(ii2.hasNext()) {
                    ps.setInt(parmNum++, ii2.next());
                }
                ResultSet rs = dbConnection.getResults(ps);
                while(rs.next()) {
                    lookupIds.add(rs.getInt("LOOKUPID"));
                }
            } catch (SQLException e) {
                logger.atSevere().log("getSourceLookupIds failed\n%s", e.getMessage());
            }
    
    }

    @Override
    protected void addComponentToRepo(ResultSet rs) throws SQLException {
        LookupPathKey lpKey = new LookupPathKey();
        lpKey.setComponentId(rs.getInt("LOOKUPID"));
        lpKey.setTargetlfid(rs.getInt("LOGFILEID"));
        requiredLFs.add(rs.getInt("LOGFILEID"));
        lpKey.setStepNumber(rs.getShort("STEPSEQNBR"));
        lpKey.setJoinName(rs.getString("NAME"));
        lpKey.setKeyNumber(rs.getShort("KEYSEQNBR"));
        lpKey.setTargetLrId(rs.getInt("LOGRECID"));
        requiredLRs.add(rs.getInt("LOGRECID"));
        lpKey.setSourceLrId(rs.getInt("STEPSRCLR"));
        requiredLRs.add(rs.getInt("STEPSRCLR"));
        lpKey.setFieldId(rs.getInt("LRFIELDID"));
        lpKey.setDatatype(DataType.fromdbcode(getDefaultedString(rs.getString("VALUEFMTCD"), "NONE")));
        lpKey.setSigned(rs.getInt("SIGNED") == 0 ? false : true);
        lpKey.setStartPosition((short)0);
        lpKey.setFieldLength(rs.getShort("VALUELEN"));
        lpKey.setOrdinalPosition(rs.getShort("KEYSEQNBR"));
        lpKey.setDecimalCount(rs.getShort("DECIMALCNT"));
        lpKey.setRounding(rs.getShort("ROUNDING"));
        lpKey.setDateTimeFormat(DateCode.fromdbcode(getDefaultedString(rs.getString("FLDCONTENTCD"), "NONE")));
        lpKey.setJustification(JustifyId.fromdbcode(getDefaultedString(rs.getString("JUSTIFYCD"), "NONE")));
        lpKey.setMask(getDefaultedString(rs.getString("MASK"), ""));
        String value = rs.getString("VALUE");
        if(value != null && value.length() > 0) {
            lpKey.setValueLength(value.length());
            lpKey.setValue(value);
        } else {
            lpKey.setValueLength(0);
        }
        lpKey.setSymbolicName(getDefaultedString(rs.getString("SYMBOLICNAME"), ""));
        Repository.addLookupPathKey(lpKey);
        Repository.setCurrentLookupPathState(rs.getInt("VALID"));
    }
    
    public boolean addNamedLookupToRepo(DatabaseConnection dbConnection, DatabaseConnectionParams params, int environmentID, String name) {
        if(name.length() > 0) {
            logger.atInfo().log("Getting Lookup %s", name);
            String query = "SELECT 	distinct "
                + "l.LOOKUPID, "
                + "l.NAME, "
                + "l.VALIDIND as VALID, "
                + "l.SRCLRID as LKUPSRCLR, "
                + "DESTLRLFASSOCID, "
                + "s.STEPSEQNBR, "
                + "s.LOOKUPSTEPID, "
                + "s.SRCLRID as STEPSRCLR, "
                + "s.LRLFASSOCID as STEPLRLF, "
                + "k.KEYSEQNBR, "
                + "k.FLDTYPE, "
                + "K.LRFIELDID, "
                + "k.LRLFASSOCID as KEYLRLF, "
                + "k.VALUEFMTCD, "
                + "k.SIGNED, "
                + "k.VALUELEN, "
                + "k.DECIMALCNT, "
                + "k.FLDCONTENTCD, "
                + "k.ROUNDING, "
                + "k.JUSTIFYCD, "
                + "k.MASK, "
                + "k.SYMBOLICNAME, "
                + "k.VALUE, "
                + "t.LOGRECID, "
                + "t.LOGFILEID "
                + "FROM " + params.getSchema() + ".LOOKUP l "
                + "INNER JOIN " + params.getSchema() + ".LOOKUPSTEP s "
                + "ON(l.LOOKUPID = s.LOOKUPID AND l.environid = s.environid) "
                + "INNER JOIN " + params.getSchema() + ".LOOKUPSRCKEY k "
                + "ON(k.LOOKUPID = s.LOOKUPID AND s.LOOKUPSTEPID = k.LOOKUPSTEPID AND l.environid = k.environid) "
                + "INNER JOIN " + params.getSchema() + ".LRLFASSOC t "
                + "ON(s.LRLFASSOCID = t.LRLFASSOCID AND l.environid = t.environid) "
                + "where l.environid = ? and UPPER(l.name) = ? "
                + "ORDER BY l.LOOKUPID, s.STEPSEQNBR, k.KEYSEQNBR; ";
            executeAndWriteToRepo(dbConnection, query, params, name);
            return hasErrors;
        } else {
            logger.atInfo().log("No Lookups required");
            return false;
        }
    }

}
