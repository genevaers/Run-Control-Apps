package org.genevaers.runcontrolgenerator.workbenchinterface;

/*
 * Copyright Contributors to the GenevaERS Project. SPDX-License-Identifier: Apache-2.0 (c) Copyright IBM Corporation 2023
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


import java.util.Set;
import java.util.TreeSet;

import org.genevaers.grammar.FormatFilterBaseListener;
import org.genevaers.grammar.FormatFilterParser;

public class FormatFilterListener extends FormatFilterBaseListener{
	private Set<Integer> columnRefs = new TreeSet<>();

   	@Override 
    public void exitColumnRef(FormatFilterParser.ColumnRefContext ctx) { 
        //get the column number
        String col = ctx.getChild(0).getText();
        String[] bits = col.split("\\.");
        columnRefs.add(Integer.valueOf(bits[1]));
    }

    public Set<Integer> getColumnRefs() {
        return columnRefs;
    }

}
