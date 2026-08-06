package ComponentGenerator.model.segments.functioncodes;

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


public class FunctionCodeDefinition {

    String function_code;
    String ltRecordType;
    String category;
    String description;
    String ltDescription;

    public String getFunctionCode() {
        return function_code;
    }

    public void setFunctionCode(String function_code) {
        this.function_code = function_code;
    }

    public String getLtRecordType() {
        return ltRecordType;
    }

    public void setLtRecordType(String ltRecordType) {
        this.ltRecordType = ltRecordType;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLtDescription() {
        return ltDescription;
    }

    public void setLtDescription(String ltDescription) {
        this.ltDescription = ltDescription;
    }

}
