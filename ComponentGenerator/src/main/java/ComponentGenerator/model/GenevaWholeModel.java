package ComponentGenerator.model;



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


import java.util.Map;
import java.util.TreeMap;

import ComponentGenerator.model.segments.ModelSegment;

/**
 * POJO for the Whole Geneva model. Used in the Jackson processing.
 */
public class GenevaWholeModel {
    private String description;
    private Map<String, ModelSegment> segments = new TreeMap<>();

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ModelSegment getSegment(String name) {
        return segments.get(name);
    }

    public Map<String, ModelSegment> getSegments() {
        return segments;
    }

    public void addSegment(String name, ModelSegment modelSegment) {
        segments.put(name, modelSegment);
    }

    public String getTargetDirectory() {
        return "target";
    }

}
