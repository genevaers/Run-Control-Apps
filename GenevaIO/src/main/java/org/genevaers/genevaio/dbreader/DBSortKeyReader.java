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
import org.genevaers.repository.components.ViewNode;
import org.genevaers.repository.components.ViewSortKey;
import org.genevaers.repository.components.enums.DataType;
import org.genevaers.repository.components.enums.DateCode;
import org.genevaers.repository.components.enums.JustifyId;
import org.genevaers.repository.components.enums.PerformBreakLogic;
import org.genevaers.repository.components.enums.SortBreakFooterOption;
import org.genevaers.repository.components.enums.SortBreakHeaderOption;
import org.genevaers.repository.components.enums.SortKeyDispOpt;
import org.genevaers.repository.components.enums.SortOrder;

public class DBSortKeyReader extends DBReaderBase {

   	private void setDefault(ViewSortKey vsk) {
		vsk.setDescDateCode(DateCode.NONE);
		vsk.setDescDataType(DataType.ALPHANUMERIC);
		vsk.setDescJustifyId(JustifyId.LEFT);
		vsk.setLabel("");
		vsk.setSkJustifyId(JustifyId.LEFT);
		vsk.setSktDateCode(DateCode.NONE);
		vsk.setSktDataType(DataType.ALPHANUMERIC);
		vsk.setSktJustifyId(JustifyId.LEFT);
		vsk.setSortKeyDateTimeFormat(DateCode.NONE);
		vsk.setSortDisplay(SortKeyDispOpt.CATEGORIZE);
	}

    @Override
    public boolean addToRepo(DatabaseConnection dbConnection, DatabaseConnectionParams params) {
        String query = "select * from " + params.getSchema() + ".viewsortkey "
        + "where environid = ? and viewid in(" + getPlaceholders(viewIds.size())+ ");";
        executeAndWriteToRepo(dbConnection, query, params, viewIds);
        return hasErrors;
    }

    @Override
    protected void addComponentToRepo(ResultSet rs) throws SQLException {
        ViewNode view = Repository.getViews().get(rs.getInt("VIEWID"));
        if(view != null) {
            ViewSortKey vsk = new ViewSortKey();
            setDefault(vsk);
            vsk.setComponentId(rs.getInt("VIEWCOLUMNID"));
            vsk.setColumnId(rs.getInt("VIEWCOLUMNID"));
            vsk.setViewSortKeyId(rs.getInt("VIEWSORTKEYID"));
            vsk.setSequenceNumber(rs.getShort("KEYSEQNBR"));
            vsk.setSortorder(SortOrder.fromdbcode(rs.getString("SORTSEQCD")));
			int sbi = Integer.parseInt(rs.getString("SORTBRKIND"));
            int footerMask = 0x0001;
            int re = sbi & footerMask;
            if (re > 0) {
                vsk.setSortBreakFooterOption(SortBreakFooterOption.PRINT);
            } else {
                vsk.setSortBreakFooterOption(SortBreakFooterOption.NOPRINT);
            }
            int breakMask = 0x0002;
            re = sbi & breakMask;
            if (re > 0) {
                vsk.setPerformBreakLogic(PerformBreakLogic.NOBREAK);
            } else {
                vsk.setPerformBreakLogic(PerformBreakLogic.BREAK);
            }

            //vsk.setSortBreakFooterOption(sortBreakFooterOption);
            //There is a sort display option here that is not available 
            //Set via the SORTKEYDISPLAYCD
            // This is the sort break header option
            // 0 = Print header line on Same Page     [value 1]
            // 1 = Print header line on New Page      [value 2]
            // 2 = Don't display a header line at all [value 3]
            //   = Print title line 1                 [value 4]
            //   = Print title line 2                 [value 5]
            //   = Print title line 3                 [value 6]
            int sBreakIndicator = rs.getInt("PAGEBRKIND");
            SortBreakHeaderOption tHeaderDisp = SortBreakHeaderOption.SAMEPAGE;
            if (sBreakIndicator == 1)
                tHeaderDisp = SortBreakHeaderOption.NEWPAGE;
            else if (sBreakIndicator == 2)
                tHeaderDisp = SortBreakHeaderOption.NONE;
            vsk.setSortBreakHeaderOption(tHeaderDisp);
            //vsk.setDisplaySubtotalCount(subtotalCountInd);
            vsk.setLabel(getDefaultedString(rs.getString("SORTKEYLABEL"), ""));
			vsk.setSortDisplay(SortKeyDispOpt.fromdbcode(getDefaultedString(rs.getString("SORTKEYDISPLAYCD"), "CAT")));
            vsk.setRtdLrFieldId(rs.getInt("SORTTITLELRFIELDID"));
            //vsk.setRtdJoinId(rtdJoinId);
            vsk.setSortKeyDataType(DataType.fromdbcode(rs.getString("SKFLDFMTCD")));
            vsk.setSortKeySigned(rs.getInt("SKSIGNED") == 0 ? false : true);
            vsk.setSkStartPosition(rs.getShort("SKSTARTPOS"));
            vsk.setSkFieldLength(rs.getShort("SKFLDLEN"));
            //vsk.setSkOrdinalPosition(skOrdinalPosition);
            vsk.setSkDecimalCount(rs.getShort("SKDECIMALCNT"));
            //vsk.setSkRounding(skRounding);
            vsk.setSortKeyDateTimeFormat(DateCode.fromdbcode(getDefaultedString(rs.getString("SKFLDCONTENTCD"), "NONE")));
            //vsk.setSkJustifyId(skJustifyId);
            // vsk.setSkOrdinalOffset(skOrdinalOffset);
            // vsk.setSktDataType(sktDataType);
            // vsk.setSktSigned(sktSignedInd);
            // vsk.setSktStartPosition(sktStartPosition);
            //vsk.setSktFieldLength(sktFieldLength);
            // vsk.setSktOrdinalPosition(sktOrdinalPosition);
            // vsk.setSktDecimalCount(sktDecimalCount);
            // vsk.setSktRounding(sktRounding);
            // vsk.setSktDateCode(sktDateCode);
            // vsk.setSktJustifyId(sktJustifyId);
            // vsk.setSktOrdinalOffset(sktOrdinalOffset);
            // vsk.setDescDataType(descDataType);
            // vsk.setDescSigned(descSignedInd);
            // vsk.setDescStartPosition(descStartPosition);
            // vsk.setDescFieldLength(descFieldLength);
            // vsk.setDescOrdinalPosition(descOrdinalPosition);
            // vsk.setDescDecimalCount(descDecimalCount);
            // vsk.setDescRounding(descRounding);
            // vsk.setDescDateCode(descDateCode);
            // vsk.setDescJustifyId(descJustifyId);
            // vsk.setDescOrdinalOffset(descOrdinalOffset);
            // vsk.setPerformBreakLogic(performBreakLogic);
            view.addViewSortKey(vsk);
            } else {
           //Can there ever be an else here... ?
           hasErrors = true;            
        }
    }
    
}
