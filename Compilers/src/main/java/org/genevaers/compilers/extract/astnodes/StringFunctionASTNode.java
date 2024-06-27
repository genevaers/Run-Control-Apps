package org.genevaers.compilers.extract.astnodes;

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


import org.genevaers.repository.components.enums.DataType;
import org.genevaers.repository.components.enums.DateCode;

public class StringFunctionASTNode extends FormattedASTNode {

    private String length;

    public int getLength() {
        return Integer.valueOf(length);
    }

    public void setLength(String length) {
        this.length = length;
    }

    @Override
    public DataType getDataType() {
        return overriddenDataType != DataType.INVALID ? overriddenDataType : DataType.ALPHANUMERIC;
    }

    @Override
    public DateCode getDateCode() {
        return (overriddenDateCode != null) ? overriddenDateCode : DateCode.NONE;
    }

    @Override
    public String getMessageName() {
        return "string function";
    }

    @Override
    public int getMaxNumberOfDigits() {
        return Integer.valueOf(length);
    }

}
