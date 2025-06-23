package org.genevaers.genevaio.yamlreader;

import com.fasterxml.jackson.annotation.JsonIgnore;

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


public class LRFieldTransfer {

	private Integer id;
	private String name;
	private Integer lrId;
	private String dbmsColName;
	private boolean redefinesInd;
	private Integer redefine;
	private Integer fixedStartPos;
	private Integer ordinalPos;
	private Integer ordinalOffset;
	private boolean effStartDate;
	private boolean effEndDate;
	private Integer pkeySeqNo;
	private Integer environmentId;
	private String dataType;
	private Integer length;
	private Integer decimalPlaces;
	private Boolean signed;
	private Integer scalingFactor;
	private String dateTimeFormat;
	private String headerAlignment;
	@JsonIgnore
	private String columnHeading1;
	@JsonIgnore
	private String columnHeading2;
	@JsonIgnore
	private String columnHeading3;
	private String numericMask;
	private String sortKeyLabel;
	private String defaultValue;
	private String subtotalLabel;


	
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the lrId
	 */
	public Integer getLrId() {
		return lrId;
	}

	/**
	 * @param lrId
	 *            the lrId to set
	 */
	public void setLrId(Integer lrId) {
		this.lrId = lrId;
	}

	/**
	 * @return the dbmsColName
	 */
	public String getDbmsColName() {
		return dbmsColName;
	}

	/**
	 * @param dbmsColName
	 *            the dbmsColName to set
	 */
	public void setDbmsColName(String dbmsColName) {
		this.dbmsColName = dbmsColName;
	}

    public Integer getRedefine() {
        return redefine;
    }

    public void setRedefine(Integer redefine) {
        this.redefine = redefine;
    }
    
	/**
	 * @return the fixedStartPos
	 */
	public Integer getFixedStartPos() {
		return fixedStartPos;
	}

	/**
	 * @param fixedStartPos
	 *            the fixedStartPos to set
	 */
	public void setFixedStartPos(Integer fixedStartPos) {
		this.fixedStartPos = fixedStartPos;
	}

	/**
	 * @return the ordinalPos
	 */
	public Integer getOrdinalPos() {
		return ordinalPos;
	}

	/**
	 * @param ordinalPos
	 *            the ordinalPos to set
	 */
	public void setOrdinalPos(Integer ordinalPos) {
		this.ordinalPos = ordinalPos;
	}

	/**
	 * @return the ordinalOffset
	 */
	public Integer getOrdinalOffset() {
		return ordinalOffset;
	}

	/**
	 * @param ordinalOffset
	 *            the ordinalOffset to set
	 */
	public void setOrdinalOffset(Integer ordinalOffset) {
		this.ordinalOffset = ordinalOffset;
	}

	/**
	 * @param effStartDate
	 *            the effStartDate to set
	 */
	public void setEffStartDate(boolean effStartDate) {
		this.effStartDate = effStartDate;
	}

	/**
	 * @return the effStartDate
	 */
	public boolean isEffStartDate() {
		return effStartDate;
	}

	/**
	 * @param effEndDate
	 *            the effEndDate to set
	 */
	public void setEffEndDate(boolean effEndDate) {
		this.effEndDate = effEndDate;
	}

	/**
	 * @return the effEndDate
	 */
	public boolean isEffEndDate() {
		return effEndDate;
	}

	/**
	 * @param pkeySeqNo
	 *            the pkeySeqNo to set
	 */
	public void setPkeySeqNo(Integer pkeySeqNo) {
		this.pkeySeqNo = pkeySeqNo;
	}

	/**
	 * @return the pkeySeqNo
	 */
	public Integer getPkeySeqNo() {
		return pkeySeqNo;
	}

    public boolean isRedefinesInd() {
        return redefinesInd;
    }

    public void setRedefinesInd(boolean redefinesInd) {
        this.redefinesInd = redefinesInd;
    }

	public Integer getEnvironmentId() {
		return environmentId;
	}

	public void setEnvironmentId(Integer environmentId) {
		this.environmentId = environmentId;
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

	public String getDateTimeFormat() {
		return dateTimeFormat;
	}

	public void setDateTimeFormat(String dateTimeFormat) {
		this.dateTimeFormat = dateTimeFormat;
	}

	public String getHeaderAlignment() {
		return headerAlignment;
	}

	public void setHeaderAlignment(String headerAlignment) {
		this.headerAlignment = headerAlignment;
	}

	public String getColumnHeading1() {
		return columnHeading1;
	}

	public void setColumnHeading1(String columnHeading1) {
		this.columnHeading1 = columnHeading1;
	}

	public String getColumnHeading2() {
		return columnHeading2;
	}

	public void setColumnHeading2(String columnHeading2) {
		this.columnHeading2 = columnHeading2;
	}

	public String getColumnHeading3() {
		return columnHeading3;
	}

	public void setColumnHeading3(String columnHeading3) {
		this.columnHeading3 = columnHeading3;
	}

	public String getNumericMask() {
		return numericMask;
	}

	public void setNumericMask(String numericMask) {
		this.numericMask = numericMask;
	}

	public String getSortKeyLabel() {
		return sortKeyLabel;
	}

	public void setSortKeyLabel(String sortKeyLabel) {
		this.sortKeyLabel = sortKeyLabel;
	}

	public String getDefaultValue() {
		return defaultValue;
	}

	public void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
	}

	public String getSubtotalLabel() {
		return subtotalLabel;
	}

	public void setSubtotalLabel(String subtotalLabel) {
		this.subtotalLabel = subtotalLabel;
	}

	
}
