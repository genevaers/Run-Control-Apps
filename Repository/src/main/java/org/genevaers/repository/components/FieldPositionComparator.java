package org.genevaers.repository.components;

/*
 * Copyright Contributors to the GenevaERS Project. SPDX-License-Identifier: Apache-2.0 (c) Copyright IBM Corporation 2008.
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


import java.util.Comparator;
/**
 * Comparator used in Lookup generation.
 */
public class FieldPositionComparator implements Comparator<LRField>{

    /**
     * Field sort by position and length
     * Those that start at the same position will have longer first
     */
    @Override
    public int compare(LRField f1, LRField f2) {
        if (f1.getStartPosition() > f2.getStartPosition()) {
            return 1;
        } else if (f1.getStartPosition() < f2.getStartPosition()) {
            return -1;
        } else {
            if(f1.getLength() < f2.getLength()) {
                return 1;
            } else if(f1.getLength() > f2.getLength()) {
                return -1;
            } else {
                return 0;
            }
        }
    }
    
}
