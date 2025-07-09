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


public enum CompilerMessageSource {
    
    EXTRACT_FILTER("ExtFilter"), 
    EXTRACT_OUTPUT("ExtrOut"), 
    COLUMN("ColAssign"),
    COLUMN_CALC("ColCalc"), 
    FORMAT_FILTER("FormatFilter"), 
    VIEW_PROPS("View Properties"),
    REPO_BUILDER("Repo Builder");

    private final String value;

    private CompilerMessageSource(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return this.value;
    }

    public String value() {
        return this.value;
    }

}
