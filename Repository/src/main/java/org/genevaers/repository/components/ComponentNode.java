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


 /**
  * An abstract base class for all components.
  *
  * <p>A component has a flag to indicate whether it is required in the final VDP file.</p>
  * <p>The required flag is only used in the management of Physical Files. See Single Pass Optimisation.</p>
  * <p> The class also has a static maxColumnID. Which probably shouldn't be here</p>
  */
public abstract class ComponentNode { 
	protected static int maxColumnID; //share betweem all views
    private boolean required = true;

    public static void setMaxColumnID(int maxColumnID) {
        ComponentNode.maxColumnID = maxColumnID;
    }

    public static int getMaxColumnID() {
        return ComponentNode.maxColumnID;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }
}
