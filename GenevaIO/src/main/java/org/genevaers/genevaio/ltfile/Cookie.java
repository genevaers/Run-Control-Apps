package org.genevaers.genevaio.ltfile;

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


import java.nio.ByteBuffer;

import org.genevaers.repository.components.enums.DateCode;

public class Cookie {
    public static final int LTDateRunDay                = 0xffffffff;
    public static final int LTDateRunMonth              = 0xfffffffe;
    public static final int LTDateRunYear               = 0xfffffffd;
    public static final int LTDateFirstOfQuarter        = 0xfffffffc;
    public static final int LTDateLastOfQuarter         = 0xfffffffb;
    public static final int LTDateFirstOfQ1             = 0xfffffffa;
    public static final int LTDateFirstOfQ2             = 0xfffffff9;
    public static final int LTDateFirstOfQ3             = 0xfffffff8;
    public static final int LTDateFirstOfQ4             = 0xfffffff7;
    public static final int LTDateLastOfQ1              = 0xfffffff6;
    public static final int LTDateLastOfQ2              = 0xfffffff5;
    public static final int LTDateLastOfQ3              = 0xfffffff4;
    public static final int LTDateLastOfQ4              = 0xfffffff3;
    public static final int LTDateRunPeriod             = 0xfffffff2;
    public static final int LTDateTimeStamp             = 0xfffffff1;
    public static final int LTDateFiscalDay             = 0xfffffff0;
    public static final int LTDateFiscalMonth           = 0xffffffef;
    public static final int LTDateFiscalYear            = 0xffffffee;
    public static final int LTDateFiscalPeriod          = 0xffffffed;
    public static final int LTDateFiscalFirstOfQuarter  = 0xffffffec;
    public static final int LTDateFiscalLastOfQuarter   = 0xffffffeb;
    public static final int LTFillField                 = 0xfff0fff0;

    private int length;
    private String vaString;

    public Cookie(String value) {
        vaString = value;
        length = value.length();
    }

    public Cookie(int len, String value) {
        length = len;
        vaString = value;
    }

    public int length() {
        return length;
    }

    public byte[] getBytes() {
        int v = Integer.parseInt(vaString);
        byte[] bytes = new byte[256];
        int intLen = Integer.BYTES;
        for (int i = 0; i < intLen; i++) {
            bytes[intLen - i - 1] = (byte) (v & 0xFF);
            v >>= 8;
        }
        return bytes;
    }

    public String getString() {
        return vaString;
    }

    public String getPrintString() {
        switch (length) {
            case LTDateRunDay:
                return "RUNDAY(" + vaString + ")";
            case LTDateFiscalDay:
                return "FISCALDAY(" + vaString + ")";
            case LTDateRunMonth:
            return "RUNMONTH(" + vaString + ")";
            case LTDateFiscalMonth:
            return "FISCALMONTH(" + vaString + ")";
            case LTDateRunYear:
            return "RUNYEAR(" + vaString + ")";
            case LTDateFiscalYear:
            return "FISCALYEAR(" + vaString + ")";
            default:
                return vaString;
        }
    }

    public void setValueLength(int rawDateValue) {
        length = rawDateValue;
    }

    public void setIntegerData(String value) {
        vaString = value;
    }

    public DateCode getDateCode() {
        switch (length) {
        case LTDateRunDay:
        case LTDateFiscalDay:
            return DateCode.CCYYMMDD;
        case LTDateRunMonth:
        case LTDateFiscalMonth:
            return DateCode.CYM;
        case LTDateRunYear:
        case LTDateFiscalYear:
            return DateCode.CCYY;
        default:
            return DateCode.NONE;
        }
    }

}
