package org.genevaers.genevaio.yamlreader;

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



public class YAMLLookupPathSourceFieldTransfer {
	private Integer id;
	private Integer environmentId;
	private String sourceFieldType;
	private Integer keySeqNbr;
	private Integer lookupPathStepId;
	private Integer sourceXLRFLDId;
	private Integer sourceXLRFileId;
	private Integer sourceJoinId;
	private String symbolicName;
	private String sourceValue;
    private String dataType;
    private Integer length;
    private Integer decimalPlaces;
    private Boolean signed;
    private Integer scalingFactor;
	private String dateCode;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Integer getEnvironmentId() {
		return environmentId;
	}

	public void setEnvironmentId(Integer environmentId) {
		this.environmentId = environmentId;
	}

	public YAMLLookupPathSourceFieldTransfer() {
	}

	public void setSourceFieldType(String sourceFieldType) {
		this.sourceFieldType = sourceFieldType;
	}

	public String getSourceFieldType() {
		return sourceFieldType;
	}

	public void setKeySeqNbr(Integer keySeqNbr) {
		this.keySeqNbr = keySeqNbr;
	}

	public void setLookupPathStepId(Integer lookupPathStepId) {
		this.lookupPathStepId = lookupPathStepId;
	}

	public Integer getLookupPathStepId() {
		return lookupPathStepId;
	}

	public void setSourceXLRFLDId(Integer sourceXLRFLDId) {
		this.sourceXLRFLDId = sourceXLRFLDId;
	}

	public Integer getSourceXLRFLDId() {
		return sourceXLRFLDId;
	}

	public void setSourceXLRFileId(Integer sourceXLRFileId) {
		this.sourceXLRFileId = sourceXLRFileId;
	}

	public Integer getSourceXLRFileId() {
		return sourceXLRFileId;
	}

	public Integer getKeySeqNbr() {
		return keySeqNbr;
	}

	public Integer getSourceJoinId() {
		return sourceJoinId;
	}

	public void setSourceJoinId(Integer sourceJoinId) {
		this.sourceJoinId = sourceJoinId;
	}

	public String getSymbolicName() {
		return symbolicName;
	}

	public void setSymbolicName(String symbolicName) {
		this.symbolicName = symbolicName;
	}

	public String getSourceValue() {
		return sourceValue;
	}

	public void setSourceValue(String sourceValue) {
		this.sourceValue = sourceValue;
		
	}

	public String getDataType() {
		return dataType;
	}

	public void setDataType(String dataType) {
		this.dataType = dataType;
	}

	public Integer getLength() {
		return length;
	}

	public void setLength(Integer length) {
		this.length = length;
	}

	public Integer getDecimalPlaces() {
		return decimalPlaces;
	}

	public void setDecimalPlaces(Integer decimalPlaces) {
		this.decimalPlaces = decimalPlaces;
	}

	public Boolean getSigned() {
		return signed;
	}

	public void setSigned(Boolean signed) {
		this.signed = signed;
	}

	public Integer getScalingFactor() {
		return scalingFactor;
	}

	public void setScalingFactor(Integer scalingFactor) {
		this.scalingFactor = scalingFactor;
	}

	public void setDateCode(String dateCode) {
		this.dateCode = dateCode;
	}

	public String getDateCode() {
		return dateCode;
	}
}
