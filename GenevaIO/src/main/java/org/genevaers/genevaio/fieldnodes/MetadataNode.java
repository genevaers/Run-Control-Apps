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


public class MetadataNode extends FieldNodeBase{

    private String source1;
    private String source2;

    public MetadataNode() {
        type = FieldNodeBase.FieldNodeType.METADATA;
        state = ComparisonState.ORIGINAL;
    }

    public void setSource1(String source1) {
        this.source1 = source1;
    }

    public void setSource2(String source2) {
        this.source2 = source2;
    }

    public String getSource1() {
        return source1;
    }

    public String getSource2() {
        return source2;
    }

}
