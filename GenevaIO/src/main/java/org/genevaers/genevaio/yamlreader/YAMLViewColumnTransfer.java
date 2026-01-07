package org.genevaers.genevaio.yamlreader;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;


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



public class YAMLViewColumnTransfer {

	private List<ViewColumnSourceTransfer> columnSources;
	private ViewColumnData column;
   
    public YAMLViewColumnTransfer() {
    	
    }
    
	public List<ViewColumnSourceTransfer> getColumnSources() {
		return columnSources;
	}

	public void setColumnSources(List<ViewColumnSourceTransfer> columnSources) {
		this.columnSources = columnSources;
	}
	
	public void addColumnSource(ViewColumnSourceTransfer vcs) {
		columnSources.add(vcs);
	}

	public void setColumn(ViewColumnData column) {
		this.column = column;
	}

	public ViewColumnData getColumn() {
		return column;
	}
	
}
