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



public class ViewColumnData {

	private Integer viewId;
	private Integer columnNo;
	private Integer startPosition;
	private String dataAlignmentCode;
	private Boolean visible;
	private Integer spacesBeforeColumn;
	private String sortkeyFooterLabel;

	private Integer ordinalPosition;
	private String subtotalTypeCode;
	private String extractAreaCode;
	private Integer extractAreaPosition;
	private String formatColumnLogic;
 	private String dataType;
	private Integer length;
	private Integer decimalPlaces;
	private Boolean signed;
	private Integer scalingFactor;
	private String dateTimeFormat;
	private String headerAlignment;
	private String columnHeading1;
	private String columnHeading2;
	private String columnHeading3;
	private String numericMask;
	private String sortKeyLabel;
	private String defaultValue;
	private String subtotalLabel;
   
    public ViewColumnData() {
    	
    }
    

	public Integer getViewId() {
		return viewId;
	}

	public void setViewId(Integer viewId) {
		this.viewId = viewId;
	}

	public Integer getColumnNo() {
		return columnNo;
	}

	public void setColumnNo(Integer columnNo) {
		this.columnNo = columnNo;
	}

	public Integer getStartPosition() {
		return startPosition;
	}

	public void setStartPosition(Integer startPosition) {
		this.startPosition = startPosition;
	}

	public String getDataAlignmentCode() {
		return dataAlignmentCode;
	}

	public void setDataAlignmentCode(String dataAlignmentCode) {
		this.dataAlignmentCode = dataAlignmentCode;
	}

	public Boolean getVisible() {
		return visible;
	}

	public void setVisible(Boolean visible) {
		this.visible = visible;
	}

	public Integer getSpacesBeforeColumn() {
		return spacesBeforeColumn;
	}

	public void setSpacesBeforeColumn(Integer spacesBeforeColumn) {
		this.spacesBeforeColumn = spacesBeforeColumn;
	}

	public String getSortkeyFooterLabel() {
		return sortkeyFooterLabel;
	}

	public void setSortkeyFooterLabel(String sortkeyFooterLabel) {
		this.sortkeyFooterLabel = sortkeyFooterLabel;
	}

	public Integer getOrdinalPosition() {
		return ordinalPosition;
	}

	public void setOrdinalPosition(Integer ordinalPosition) {
		this.ordinalPosition = ordinalPosition;
	}

	public String getSubtotalTypeCode() {
		return subtotalTypeCode;
	}

	public void setSubtotalTypeCode(String subtotalTypeCode) {
		this.subtotalTypeCode = subtotalTypeCode;
	}

	public String getExtractAreaCode() {
		return extractAreaCode;
	}

	public void setExtractAreaCode(String extractAreaCode) {
		this.extractAreaCode = extractAreaCode;
	}

	public Integer getExtractAreaPosition() {
		return extractAreaPosition;
	}

	public void setExtractAreaPosition(Integer extractAreaPosition) {
		this.extractAreaPosition = extractAreaPosition;
	}

	public String getFormatColumnLogic() {
		return formatColumnLogic;
	}

	public void setFormatColumnLogic(String formatColumnLogic) {
		this.formatColumnLogic = formatColumnLogic;
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
