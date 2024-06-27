package org.genevaers.genevaio.fieldnodes;

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


public class StringFieldNode extends FieldNodeBase {

    private String value;
    private String diffValue;

    public StringFieldNode(String name, String val) {
        setName(name);
        value = val;
        type = FieldNodeBase.FieldNodeType.STRINGFIELD;
        state = ComparisonState.ORIGINAL;
    }

    public String getValue() {
        if(state == ComparisonState.DIFF || state == ComparisonState.IGNORED) {
            return value + " -> " + diffValue;
        }
        return value != null ? value : "";
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getDiffValue() {
        return diffValue;
    }

    public void setDiffValue(String diffValue) {
        this.diffValue = diffValue;
    }

    @Override
    public boolean compareTo(FieldNodeBase rn) {
        boolean result = true;
        if(value.equals(((StringFieldNode)rn).getValue())) {
            state = ComparisonState.INSTANCE;
        } else {
            state = ComparisonState.DIFF;
            diffValue = ((StringFieldNode)rn).getValue();
            result = false;
        }
        return result;
    }
}
