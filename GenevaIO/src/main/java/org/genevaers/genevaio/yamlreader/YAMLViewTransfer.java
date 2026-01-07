package org.genevaers.genevaio.yamlreader;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;


public class YAMLViewTransfer  {

	@JsonIgnore
	private int environmentId;
	private boolean activated;
	private boolean updated;


	private int id;
	private String name;
	private String statusCode;
	private String typeCode;
	private Integer workFileNumber;
	private String outputFormatCode;
	private Integer outputLRId;
	private Integer extractFileAssocId;
	private Integer pageSize;
	private Integer lineSize;
	private Boolean zeroSuppressInd;
	private Boolean headerRow;
	private Integer extractMaxRecCount;
	private Boolean extractSummaryIndicator;
	private Integer extractSummaryBuffer;
	private Integer outputMaxRecCount;
	private Integer controlRecId;
	private Integer writeExitId;
	private String writeExitParams;
	private Integer formatExitId;
	private String formatExitParams;
	private String fieldDelimCode;
	private String stringDelimCode;
	
	private String formatFilterlogic;
	private String compilerVersion;

	private String createTime;
	private String createBy;
	private String modifyTime;
	private String modifyBy;

	private List<YAMLViewSourceTransfer> viewSources = new ArrayList<>();
	private List<YAMLViewColumnTransfer> viewColumns = new ArrayList<>();
	private List<ViewSortKeyTransfer> viewSortKeys = new ArrayList<>();
	private List<ViewLogicDependencyTransfer> viewDepenencies = new ArrayList<>();
	
	public YAMLViewTransfer() {
		super();
	}

// 	public YAMLViewTransfer(ViewTransfer viewTransfer) {
// //		this.setActivated(viewTransfer.isActivated());
// //		this.setActivatedBy(viewTransfer.getActivatedBy());
// //		this.setActivatedTime(viewTransfer.getActivatedTime());
// //		this.setComments(viewTransfer.getComments());
// //		this.setCompilerVersion(viewTransfer.getCompilerVersion());
// 		this.setControlRecId(viewTransfer.getControlRecId());
// 		this.setEnvironmentId(viewTransfer.getEnvironmentId());
// 		this.setExtractMaxRecCount(viewTransfer.getExtractMaxRecCount());
// //		this.setExtractSummaryBuffer(viewTransfer.getExtractSummaryBuffer());
// //		this.setFieldDelimCode(viewTransfer.getFieldDelimCode());
// //		this.setHeaderRow(viewTransfer.isHeaderRow());
// //		this.setFormatExitParams(viewTransfer.getFormatExitParams());
// //		this.setFormatExitId(viewTransfer.getFormatExitId());
// //		this.setFormatFilterlogic(viewTransfer.getFormatFilterlogic());
// //		this.setExtractFileAssocId(viewTransfer.getExtractFileAssocId());
// //		this.setOutputFormatCode(viewTransfer.getOutputFormatCode());
// //		this.setWorkFileNumber(viewTransfer.getWorkFileNumber());
// 		this.setId(viewTransfer.getId());
// //		this.setLineSize(viewTransfer.getLineSize());
// 		this.setName(viewTransfer.getName());
// 		this.setOutputFormatCode(viewTransfer.getOutputFormatCode());
// 		this.setOutputMaxRecCount(viewTransfer.getOutputMaxRecCount());
// //		this.setPageSize(viewTransfer.getPageSize());
// 		this.setStatusCode(viewTransfer.getStatusCode());
// //		this.setStringDelimCode(viewTransfer.getStringDelimCode());
// 		this.setTypeCode(viewTransfer.getTypeCode());
// //		this.setWriteExitId(viewTransfer.getWriteExitId());
// //		this.setWriteExitParams(viewTransfer.getWriteExitParams());
// //		this.setZeroSuppressInd(viewTransfer.isSuppressZeroRecords());
// 	}
	
	// public Boolean isExtractSummaryIndicator() {
	// 	return isAggregateBySortKey();
	// }

	// public Boolean isZeroSuppressInd() {
	// 	return isSuppressZeroRecords();
	// }

	public void setEnvironmentId(int environmentId) {
		this.environmentId = environmentId;
	}

	public int getEnvironmentId() {
		return environmentId;
	}

	public void setActivated(boolean activated) {
		this.activated = activated;
	}

	public void setUpdated(boolean updated) {
		this.updated = updated;
	}

	public boolean isActivated() {
		return activated;
	}

	public boolean isUpdated() {
		return updated;
	}

	public List<YAMLViewSourceTransfer> getViewSources() {
		return viewSources;
	}

	public void setViewSources(List<YAMLViewSourceTransfer> viewSources) {
		this.viewSources = viewSources;
	}
	
	public void addViewSource(YAMLViewSourceTransfer vst) {
		viewSources.add(vst);
	}

	public List<YAMLViewColumnTransfer> getViewColumns() {
		return viewColumns;
	}

	public void setViewColumns(List<YAMLViewColumnTransfer> viewColumns) {
		this.viewColumns = viewColumns;
	}
	
	public void addViewColumn(YAMLViewColumnTransfer vct) {
		viewColumns.add(vct);
	}

	public List<ViewSortKeyTransfer> getViewSortKeys() {
		return viewSortKeys;
	}

	public void setViewSortKeys(List<ViewSortKeyTransfer> viewSortKeys) {
		this.viewSortKeys = viewSortKeys;
	}
	
	public void addViewSortKey(ViewSortKeyTransfer vskt) {
		viewSortKeys.add(vskt);
	}

	public void setViewDepenencies(List<ViewLogicDependencyTransfer> viewDepenencies) {
		this.viewDepenencies = viewDepenencies;
	}
	
	public List<ViewLogicDependencyTransfer> getViewDepenencies() {
		return viewDepenencies;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getStatusCode() {
		return statusCode;
	}

	public void setStatusCode(String statusCode) {
		this.statusCode = statusCode;
	}

	public String getTypeCode() {
		return typeCode;
	}

	public void setTypeCode(String typeCode) {
		this.typeCode = typeCode;
	}

	public Integer getWorkFileNumber() {
		return workFileNumber;
	}

	public void setWorkFileNumber(Integer workFileNumber) {
		this.workFileNumber = workFileNumber;
	}

	public String getOutputFormatCode() {
		return outputFormatCode;
	}

	public void setOutputFormatCode(String outputFormatCode) {
		this.outputFormatCode = outputFormatCode;
	}

	public Integer getOutputLRId() {
		return outputLRId;
	}

	public void setOutputLRId(Integer outputLRId) {
		this.outputLRId = outputLRId;
	}

	public Integer getExtractFileAssocId() {
		return extractFileAssocId;
	}

	public void setExtractFileAssocId(Integer extractFileAssocId) {
		this.extractFileAssocId = extractFileAssocId;
	}

	public Integer getPageSize() {
		return pageSize;
	}

	public void setPageSize(Integer pageSize) {
		this.pageSize = pageSize;
	}

	public Integer getLineSize() {
		return lineSize;
	}

	public void setLineSize(Integer lineSize) {
		this.lineSize = lineSize;
	}

	public Boolean getZeroSuppressInd() {
		return zeroSuppressInd;
	}

	public void setZeroSuppressInd(Boolean zeroSuppressInd) {
		this.zeroSuppressInd = zeroSuppressInd;
	}

	public Boolean getHeaderRow() {
		return headerRow;
	}

	public void setHeaderRow(Boolean headerRow) {
		this.headerRow = headerRow;
	}

	public Integer getExtractMaxRecCount() {
		return extractMaxRecCount;
	}

	public void setExtractMaxRecCount(Integer extractMaxRecCount) {
		this.extractMaxRecCount = extractMaxRecCount;
	}

	public Boolean getExtractSummaryIndicator() {
		return extractSummaryIndicator;
	}

	public void setExtractSummaryIndicator(Boolean extractSummaryIndicator) {
		this.extractSummaryIndicator = extractSummaryIndicator;
	}

	public Integer getExtractSummaryBuffer() {
		return extractSummaryBuffer;
	}

	public void setExtractSummaryBuffer(Integer extractSummaryBuffer) {
		this.extractSummaryBuffer = extractSummaryBuffer;
	}

	public Integer getOutputMaxRecCount() {
		return outputMaxRecCount;
	}

	public void setOutputMaxRecCount(Integer outputMaxRecCount) {
		this.outputMaxRecCount = outputMaxRecCount;
	}

	public Integer getControlRecId() {
		return controlRecId;
	}

	public void setControlRecId(Integer controlRecId) {
		this.controlRecId = controlRecId;
	}

	public Integer getWriteExitId() {
		return writeExitId;
	}

	public void setWriteExitId(Integer writeExitId) {
		this.writeExitId = writeExitId;
	}

	public String getWriteExitParams() {
		return writeExitParams;
	}

	public void setWriteExitParams(String writeExitParams) {
		this.writeExitParams = writeExitParams;
	}

	public Integer getFormatExitId() {
		return formatExitId;
	}

	public void setFormatExitId(Integer formatExitId) {
		this.formatExitId = formatExitId;
	}

	public String getFormatExitParams() {
		return formatExitParams;
	}

	public void setFormatExitParams(String formatExitParams) {
		this.formatExitParams = formatExitParams;
	}

	public String getFieldDelimCode() {
		return fieldDelimCode;
	}

	public void setFieldDelimCode(String fieldDelimCode) {
		this.fieldDelimCode = fieldDelimCode;
	}

	public String getStringDelimCode() {
		return stringDelimCode;
	}

	public void setStringDelimCode(String stringDelimCode) {
		this.stringDelimCode = stringDelimCode;
	}

	public String getFormatFilterlogic() {
		return formatFilterlogic;
	}

	public void setFormatFilterlogic(String formatFilterlogic) {
		this.formatFilterlogic = formatFilterlogic;
	}

	public String getCompilerVersion() {
		return compilerVersion;
	}

	public void setCompilerVersion(String compilerVersion) {
		this.compilerVersion = compilerVersion;
	}

	public String getCreateTime() {
		return createTime;
	}

	public void setCreateTime(String createTime) {
		this.createTime = createTime;
	}

	public String getCreateBy() {
		return createBy;
	}

	public void setCreateBy(String createBy) {
		this.createBy = createBy;
	}

	public String getModifyTime() {
		return modifyTime;
	}

	public void setModifyTime(String modifyTime) {
		this.modifyTime = modifyTime;
	}

	public String getModifyBy() {
		return modifyBy;
	}

	public void setModifyBy(String modifyBy) {
		this.modifyBy = modifyBy;
	}

	
}
