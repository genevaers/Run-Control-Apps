package org.genevaers.compilers.extract.emitters.rules;

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


import org.genevaers.compilers.extract.astnodes.ExtractBaseAST;
import org.genevaers.repository.components.enums.DateCode;

public abstract class Rule {

    public enum RuleResult {
        RULE_PASSED,
        RULE_WARNING,
        RULE_ERROR
    }

    protected static final int MAX_ZONED_LENGTH = 31;

    protected static final int LTContentMaskCC = 0x00000001;
    protected static final int LTContentMaskYY = 0x00000002;
    protected static final int LTContentMaskMM = 0x00000004;
    protected static final int LTContentMaskDD = 0x00000008;
    protected static final int LTContentMaskHH = 0x00000010;
    protected static final int LTContentMaskNN = 0x00000020;
    protected static final int LTContentMaskSS = 0x00000040;
    protected static final int LTContentMaskTT = 0x00000080;
    protected static final int LTContentInvalidShift = 16;

    protected static final int DateMasks [] =
    {
    // AttrContentNone
    LTContentMaskCC | LTContentMaskYY | LTContentMaskMM | LTContentMaskDD |
    LTContentMaskHH | LTContentMaskNN | LTContentMaskSS | LTContentMaskTT,
    // AttrContentYYMMDD
    LTContentMaskYY | LTContentMaskMM | LTContentMaskDD,
    // AttrContentYY_MM_DD
    LTContentMaskYY | LTContentMaskMM | LTContentMaskDD,
    // AttrContentCCYYMMDD
    LTContentMaskCC | LTContentMaskYY | LTContentMaskMM | LTContentMaskDD,
    // AttrContentCCYY_MM_DD
    LTContentMaskCC | LTContentMaskYY | LTContentMaskMM | LTContentMaskDD,
    // AttrContentDDMMYY
    LTContentMaskDD | LTContentMaskMM | LTContentMaskYY,
    // AttrContentDD_MM_YY
    LTContentMaskDD | LTContentMaskMM | LTContentMaskYY,
    // AttrContentDDMMCCYY
    LTContentMaskDD | LTContentMaskMM | LTContentMaskCC | LTContentMaskYY,
    // AttrContentDD_MM_CCYY
    LTContentMaskDD | LTContentMaskMM | LTContentMaskCC | LTContentMaskYY,
    // AttrContentYYDDD
    LTContentMaskDD | LTContentMaskMM | LTContentMaskYY,
    // AttrContentYY_DDD
    LTContentMaskDD | LTContentMaskMM | LTContentMaskYY,
    // AttrContentCCYYDDD
    LTContentMaskCC | LTContentMaskYY | LTContentMaskMM | LTContentMaskDD,
    // AttrContentCCYY_DDD
    LTContentMaskCC | LTContentMaskYY | LTContentMaskMM | LTContentMaskDD,
    // AttrContentMMDD
    LTContentMaskMM | LTContentMaskDD,
    // AttrContentMM_DD
    LTContentMaskMM | LTContentMaskDD,
    // AttrContentDD_MM
    LTContentMaskDD | LTContentMaskMM,
    // AttrContentMM
    LTContentMaskMM,
    // AttrContentDD
    LTContentMaskDD,
    // AttrContentDDDDDDDD
    6<<LTContentInvalidShift,
    // AttrContentHHMMSSTT
    LTContentMaskHH | LTContentMaskNN | LTContentMaskSS | LTContentMaskTT,
    // AttrContentHH_MM_SS_TT
    LTContentMaskHH | LTContentMaskNN | LTContentMaskSS | LTContentMaskTT,
    // AttrContentHHMMSS
    LTContentMaskHH | LTContentMaskNN | LTContentMaskSS,
    // AttrContentHH_MM_SS
    LTContentMaskHH | LTContentMaskNN | LTContentMaskSS,
    // AttrContentHHMM
    LTContentMaskHH | LTContentMaskNN,
    // AttrContentHH_MM
    LTContentMaskHH | LTContentMaskNN,
    // AttrContentCCYYMMDDHHNNSS
    LTContentMaskCC | LTContentMaskYY | LTContentMaskMM | LTContentMaskDD |
    LTContentMaskHH | LTContentMaskNN | LTContentMaskSS,
    // AttrContentSSSSSSSS
    7<<LTContentInvalidShift,
    // AttrContentLowerCase
    8<<LTContentInvalidShift,
    // AttrContentUpperCase
    9<<LTContentInvalidShift,
    // AttrContentDBCS
    10<<LTContentInvalidShift,
    // AttrContentCCYYMM
    LTContentMaskCC | LTContentMaskYY | LTContentMaskMM,
    // AttrContentCCYY
    LTContentMaskCC | LTContentMaskYY,
    // AttrContentYY
    LTContentMaskYY,
    // AttrContentSybaseDate
    LTContentMaskCC | LTContentMaskYY | LTContentMaskMM | LTContentMaskDD |
    LTContentMaskHH | LTContentMaskNN | LTContentMaskSS | LTContentMaskTT,
    // AttrContentPosixDate
    LTContentMaskCC | LTContentMaskYY | LTContentMaskMM | LTContentMaskDD |
    LTContentMaskHH | LTContentMaskNN | LTContentMaskSS | LTContentMaskTT,
    // AttrContentMMDDYY
    LTContentMaskMM | LTContentMaskDD | LTContentMaskYY,
    // AttrContentMMDDCCYY
    LTContentMaskMM | LTContentMaskDD | LTContentMaskCC | LTContentMaskYY,
    // AttrContentMM_DD_CCYY
    LTContentMaskMM | LTContentMaskDD | LTContentMaskCC | LTContentMaskYY,
    // AttrContentCCYY_DDD2
    LTContentMaskCC | LTContentMaskYY | LTContentMaskMM | LTContentMaskDD,
    // AttrContentCCYY_MM2
    LTContentMaskCC | LTContentMaskYY | LTContentMaskMM,
    // AttrContentCCYY_MM_DD2
    LTContentMaskCC | LTContentMaskYY | LTContentMaskMM | LTContentMaskDD,
    // AttrContentCCYY_MM
    LTContentMaskCC | LTContentMaskYY | LTContentMaskMM,
    // AttrContentPosixDate2
    LTContentMaskCC | LTContentMaskYY | LTContentMaskMM | LTContentMaskDD |
    LTContentMaskHH | LTContentMaskNN | LTContentMaskSS | LTContentMaskTT,
    // AttrContentDD_MM2
    LTContentMaskMM | LTContentMaskDD,
    // AttrContentDD_MM_CCYY2
    LTContentMaskCC | LTContentMaskYY | LTContentMaskMM | LTContentMaskDD,
    // AttrContentDD_MM_YY2
    LTContentMaskYY | LTContentMaskMM | LTContentMaskDD,
    // AttrContentMM_DD2
    LTContentMaskMM | LTContentMaskDD,
    // AttrContentMM_DD_CCYY2
    LTContentMaskCC | LTContentMaskYY | LTContentMaskMM | LTContentMaskDD,
    // AttrContentYY_DDD2
    LTContentMaskYY | LTContentMaskMM | LTContentMaskDD,
    // AttrContentYY_MM_DD2
    LTContentMaskYY | LTContentMaskMM | LTContentMaskDD,
    // AttrContentMONTH_DD_CCYY
    LTContentMaskMM | LTContentMaskDD | LTContentMaskCC | LTContentMaskYY,
    // AttrContentDD_MONTH_CCYY
    LTContentMaskDD | LTContentMaskMM | LTContentMaskCC | LTContentMaskYY,
    // AttrContentDD_MMM_CCYY
    LTContentMaskDD | LTContentMaskMM | LTContentMaskCC | LTContentMaskYY,
    // AttrContentMONTH_CCYY
    LTContentMaskCC | LTContentMaskYY | LTContentMaskMM,
    // AttrContentCCYYMMDDHHNNSSTT
    LTContentMaskCC | LTContentMaskYY | LTContentMaskMM | LTContentMaskDD |
    LTContentMaskHH | LTContentMaskNN | LTContentMaskSS | LTContentMaskTT,
    // AttrContentCCYYMMDDHHNNSSTT CYMDT
    LTContentMaskCC | LTContentMaskYY | LTContentMaskMM | LTContentMaskDD |
    LTContentMaskHH | LTContentMaskNN | LTContentMaskSS | LTContentMaskTT
    };



    String name;

    //Some rules will require only a single argument, so just null op2?
    public abstract RuleResult apply(ExtractBaseAST op1, ExtractBaseAST op2);

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean canAssignDates(DateCode target, DateCode source) {
        int bitwiseDifferences = DateMasks[target.ordinal()] ^ DateMasks[source.ordinal()];
        int someMissing = DateMasks[target.ordinal()] & bitwiseDifferences; // != 0 target bits missing
        return (someMissing == 0);
    }

    public boolean canCompareDates(DateCode d1, DateCode d2) {
        int d1DateFields = DateMasks[d1.ordinal()];
        int d2DateFields = DateMasks[d2.ordinal()];
        int bitwiseDifferences = d1DateFields ^ d2DateFields;
        //There should not be any differences
        return (bitwiseDifferences == 0);
    }

}
