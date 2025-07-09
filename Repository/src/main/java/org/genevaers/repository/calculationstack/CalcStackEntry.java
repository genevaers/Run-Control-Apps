package org.genevaers.repository.calculationstack;

import java.nio.ByteBuffer;


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


import org.genevaers.repository.calculationstack.CalcStack.CalcStackOpcode;
import org.genevaers.utilities.GersCodePage;

public class CalcStackEntry {

    CalcStackOpcode opCode;
    int offset;

    public CalcStackEntry() {

    }

    //CalcStackEntries are of a limited number of types
    //The all have a length of some sort 
    //  opcode + variable for a string
    //         + 4 when there is an int value like a column number or goto
    //         + 48 for number constants
    // Make derived types to capture these
    // Then as they are created 
    //  set their offset (prev offset + length of entry)
    //          (add a get next offset function?)
    //  their value if known
    public CalcStackEntry(int off) {
        offset = off;
    }

    public void setOpCode(CalcStackOpcode opCode) {
        this.opCode = opCode;
    }

    public CalcStackOpcode getOpCode() {
        return opCode;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public String getValue() {
        return "";
    }

    public int length() {
        return Integer.BYTES;
    }

    public void addTo(ByteBuffer buffer) {
        buffer.putInt(opCode.ordinal());
    }

    protected void addAndConvertStringIfNeeded(ByteBuffer buffer, String str, int len) {
        String os = System.getProperty("os.name");
		if(os.startsWith("z")) {
            //On z/OS these strings will need to be EBCDIC
            buffer.put(GersCodePage.asciiToEbcdic(str), 0, str.length());
            buffer.put(GersCodePage.asciiToEbcdic(CalcStack.spaces), 0, len - str.length());
        } else {
            buffer.put(str.getBytes(), 0, str.length());
            buffer.put(CalcStack.spaces.getBytes(), 0, len - str.length());
        }
    }
}
