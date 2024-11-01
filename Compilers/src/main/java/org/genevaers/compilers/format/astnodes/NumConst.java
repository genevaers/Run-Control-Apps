package org.genevaers.compilers.format.astnodes;

import java.math.BigDecimal;

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


import org.genevaers.compilers.format.astnodes.FormatASTFactory.Type;
import org.genevaers.repository.calculationstack.CalcStackEntry;
import org.genevaers.repository.calculationstack.CalcStackShortStringEntry;
import org.genevaers.repository.calculationstack.CalcStack.CalcStackOpcode;

public class NumConst extends FormatBaseAST{

    private String value;

    public NumConst() {
        type = Type.NUMCONST;
    }

    public void setValue(String text) {
        //strip trailing decimal zeros
        BigDecimal strippedVal = new BigDecimal(text).stripTrailingZeros();
        value = strippedVal.toPlainString();
    }

    public String getValue() {
        return negative ? "-" + value : value;
    }

    @Override
    public CalcStackEntry emit(boolean invert) {
        CalcStackShortStringEntry se = (CalcStackShortStringEntry) emitShortStringCodeEntry(CalcStackOpcode.CalcStackPushNumber, null);
        se.setValue(negative ? "-" + value : value);
        return se;
    }
}
