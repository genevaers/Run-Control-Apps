package org.genevaers.repository.data;

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


import org.genevaers.repository.components.enums.DateCode;

public class NormalisedDate {

    final static int DATE_LENGTH = 8;
    private static int DatePieces [] [] =
{  // CC  YY  MM  DD  HH  NN  SS  TT 
    { -1, -1, -1, -1, -1, -1, -1, -1, 0 },    // 0 AttrContentNone
    { -1,  0,  2,  4, -1, -1, -1, -1, 6 },    // 1 AttrContentYYMMDD
    { -1,  0,  3,  6, -1, -1, -1, -1, 8 },    // 2 AttrContentYY_MM_DD
    {  0,  2,  4,  6, -1, -1, -1, -1, 8 },    // 3 AttrContentCCYYMMDD
    {  0,  2,  5,  8, -1, -1, -1, -1, 10 },   // 4 AttrContentCCYY_MM_DD
    { -1,  4,  2,  0, -1, -1, -1, -1, 6 },    // 5 AttrContentDDMMYY
    { -1,  6,  3,  0, -1, -1, -1, -1, 8 },    // 6 AttrContentDD_MM_YY
    {  4,  6,  2,  0, -1, -1, -1, -1, 8 },    // 7 AttrContentDDMMCCYY
    {  6,  8,  3,  0, -1, -1, -1, -1, 10 },   // 8 AttrContentDD_MM_CCYY
    { -1,  0, -1, -1, -1, -1, -1, -1, 5 },    // 9 AttrContentYYDDD
    { -1,  0, -1, -1, -1, -1, -1, -1, 6 },    // 10 AttrContentYY_DDD
    {  0,  2, -1, -1, -1, -1, -1, -1, 7 },    // 11 AttrContentCCYYDDD
    {  0,  2, -1, -1, -1, -1, -1, -1, 8 },    // 12 AttrContentCCYY_DDD
    { -1, -1,  0,  2, -1, -1, -1, -1, 4 },    // 13 AttrContentMMDD
    { -1, -1,  0,  3, -1, -1, -1, -1, 5 },    // 14 AttrContentMM_DD
    { -1, -1,  3,  0, -1, -1, -1, -1, 5 },    // 15 AttrContentDD_MM
    { -1, -1,  0, -1, -1, -1, -1, -1, 2 },    // 16 AttrContentMM
    { -1, -1, -1,  0, -1, -1, -1, -1, 2 },    // 17 AttrContentDD
    { -1, -1, -1, -1, -1, -1, -1, -1, 8 },    // 18 AttrContentDDDDDDDD
    { -1, -1, -1, -1,  0,  2,  4,  6, 8 },    // 19 AttrContentHHMMSSTT
    { -1, -1, -1, -1,  0,  3,  6,  9, 11 },   // 20 AttrContentHH_MM_SS_TT
    { -1, -1, -1, -1,  0,  2,  4, -1, 6 },    // 21 AttrContentHHMMSS
    { -1, -1, -1, -1,  0,  3,  6, -1, 8 },    // 22 AttrContentHH_MM_SS
    { -1, -1, -1, -1,  0,  2, -1, -1, 4 },    // 23 AttrContentHHMM
    { -1, -1, -1, -1,  0,  3, -1, -1, 5 },    // 24 AttrContentHH_MM
    {  0,  2,  4,  6,  8, 10, 12, -1, 14 },   // 25 AttrContentCCYYMMDDHHNNSS
    { -1, -1, -1, -1, -1, -1, -1, -1, 0 },    // 26 AttrContentSSSSSSSS
    { -1, -1, -1, -1, -1, -1, -1, -1, 0 },    // 27 AttrContentLowerCase
    { -1, -1, -1, -1, -1, -1, -1, -1, 0 },    // 28 AttrContentUpperCase
    { -1, -1, -1, -1, -1, -1, -1, -1, 0 },    // 29 AttrContentDBCS
    {  0,  2,  4, -1, -1, -1, -1, -1, 6 },    // 30 AttrContentCCYYMM
    {  0,  2, -1, -1, -1, -1, -1, -1, 4 },    // 31 AttrContentCCYY
    { -1,  0, -1, -1, -1, -1, -1, -1, 2 },    // 32 AttrContentYY
    {  7,  9, -1,  4, 12, 15, 18, 21, 26 },   // 33 AttrContentSybaseDate
    {  0,  2,  5,  8, 11, 14, 17, 20, 23 },   // 34 AttrContentPosixDate
    { -1,  4,  0,  2, -1, -1, -1, -1, 6 },    // 35 AttrContentMMDDYY
    {  4,  6,  0,  2, -1, -1, -1, -1, 8 },    // 36 AttrContentMMDDCCYY
    {  6,  8,  0,  3, -1, -1, -1, -1, 10 },   // 37 AttrContentMM_DD_CCYY
    {  0,  2, -1, -1, -1, -1, -1, -1, 8 },    // 38 AttrContentCCYY_DDD2
    {  0,  2,  5, -1, -1, -1, -1, -1, 7 },    // 39 AttrContentCCYY_MM2
    {  0,  2,  5,  8, -1, -1, -1, -1, 10 },   // 40 AttrContentCCYY_MM_DD2
    {  0,  2,  5, -1, -1, -1, -1, -1, 7 },    // 41 AttrContentCCYY_MM
    {  0,  2,  5,  8, 11, 14, 17, 20, 23 },   // 42 AttrContentPosixDate2
    { -1, -1,  3,  0, -1, -1, -1, -1, 5 },    // 43 AttrContentDD_MM2
    {  6,  8,  3,  0, -1, -1, -1, -1, 10 },   // 44 AttrContentDD_MM_CCYY2
    { -1,  6,  3,  0, -1, -1, -1, -1, 8 },    // 45 AttrContentDD_MM_YY2
    { -1, -1,  0,  3, -1, -1, -1, -1, 5 },    // 46 AttrContentMM_DD2
    {  6,  8,  0,  3, -1, -1, -1, -1, 10 },   // 47 AttrContentMM_DD_CCYY2
    { -1,  0, -1, -1, -1, -1, -1, -1, 6 },    // 48 AttrContentYY_DDD2
    { -1,  0,  3,  6, -1, -1, -1, -1, 8 },    // 49 AttrContentYY_MM_DD2
    {  7,  9, -1,  4, 12, 15, 18, 21, 26 },   // 50 AttrContentMONTH_DD_CCYY
    {  7,  9,  3,  0, -1, -1, -1, -1, 11 },   // 51 AttrContentDD_MONTH_CCYY
    {  7,  9, -1,  0, -1, -1, -1, -1, 11 },   // 52 AttrContentDD_MON_CCYY
    { -1, -1,  0, -1, -1, -1, -1, -1, 11 },   // 53 AttrContentMONTH_CCYY
    {  0,  2,  4,  6,  8, 10, 12, 14, 16 },   // 54 AttrContentCCYYMMDDHHNNSSTT
    {  0,  2,  4,  6,  8, 10, 12, 14, 20 },   // 55 AttrContentCCYYMMDDHHMMSSTTTTTT 20-digit date
    {  0,  2,  5,  8, 11, 14, 17, 20, 27 }    // 56 AttrContentDB2Date
};

//Uses DatePieces array in C++
// maps the different offsets of the final date from the incoming string
// could do something similar(same) mapped from datecode.
// 
    
    public static String get(String in, DateCode dateCode) {
        char normDate[] = {'0','0','0','1','0','1','0','1','0','0','0','0','0','0','0','0'};
        if (in.length() < dataCvtGetDateLength(dateCode))
        {
            return "0000000000000000";
        }

        // Start with the default and overlay the new date/time pieces over it

        //use StringBuilder instead
        for (int i = 0; i < DATE_LENGTH; i++)
        {
            int offset = DatePieces [dateCode.ordinal()] [i];
            if (offset != -1)
            {   
                int ndx = 2*i;
                normDate[ndx] = in.charAt(offset);
                normDate[ndx+1] = in.charAt(offset+1);
            }
        }   // End of for i loop

//
// else if (tAttrContent == AttrContentDDDDDDDD)
// {
// char szBuf [9];
// char * pszBuf = szBuf;
// for (int i = 0; i < 8 && (* pszSource); i++)
// * pszBuf++ = * pszSource++;
// * pszBuf = '\0';
// long lYear, lMonth, lDay;
// long lJulian = atol (szBuf);
// GregorianDate (lJulian, lYear, lMonth, lDay);
// // The sprintf will write a NULL at the end of the date string
// // so we'll save the character in that position and put it back later
// char chSaveTensHour = pszDest [HH_OFFSET];
// sprintf (pszDest, "%04d%02d%02d", lYear, lMonth, lDay);
// pszDest [HH_OFFSET] = chSaveTensHour;
// }
        return new String(normDate);
    }

    private static int dataCvtGetDateLength (DateCode dc)
    {
        return DatePieces [dc.ordinal()] [DATE_LENGTH];
    }

}
