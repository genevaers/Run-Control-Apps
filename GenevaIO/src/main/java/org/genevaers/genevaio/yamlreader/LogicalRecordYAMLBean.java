package org.genevaers.genevaio.yamlreader;

import java.util.ArrayList;

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


import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class LogicalRecordYAMLBean {

	private Integer id;
	private String name;
	private String status;
	private Integer totLen;
    private Integer keyLen;
	private String type;
	private String comments;
	private Date createTime;
	private String createBy;
	private Date modifyTime;
	private String modifyBy;
	private Integer environmentId;
	private Boolean activated;
	private Boolean updated;
	private String lrTypeCode;
	private String lrStatusCode;
	private Integer lookupExitId;
	private String lookupExitParams;
	private Map<Integer, String> lfs = new TreeMap<>();
	private Map<Integer, LRFieldTransfer> fields = new TreeMap<>();
	private List<LRIndexFieldTransfer> indexFields = new ArrayList<>();
	private LRIndexTransfer index;
	/**
	 *Parameterized constructor to initialize values
	 */
	public LogicalRecordYAMLBean(Integer environmentId, Integer id,
			String name, String status, Integer totLen, Integer keyLen, String type, Date createTime, String createBy,
			Date modifyTime, String modifyBy, Date activatedTime, String activatedBy) {

		this.status = status;
		this.totLen = totLen;
		this.keyLen = keyLen;
        this.type = type;
	}

	public LogicalRecordYAMLBean() {
		//TODO Auto-generated constructor stub
	}

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

	public Integer getKeyLen() {
        return keyLen;
    }

    public Integer getTotLen() {
        return totLen;
    }

    public String getType() {
        return type;
    }

    public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public void setTotLen(Integer totLen) {
		this.totLen = totLen;
	}

	public void setKeyLen(Integer keyLen) {
		this.keyLen = keyLen;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getComments() {
		return comments;
	}

	public void setComments(String comments) {
		this.comments = comments;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public String getCreateBy() {
		return createBy;
	}

	public void setCreateBy(String createBy) {
		this.createBy = createBy;
	}

	public Date getModifyTime() {
		return modifyTime;
	}

	public void setModifyTime(Date modifyTime) {
		this.modifyTime = modifyTime;
	}

	public String getModifyBy() {
		return modifyBy;
	}

	public void setModifyBy(String modifyBy) {
		this.modifyBy = modifyBy;
	}

	public Integer getEnvironmentId() {
		return environmentId;
	}

	public void setEnvironmentId(Integer environmentId) {
		this.environmentId = environmentId;
	}

	public Boolean getActivated() {
		return activated;
	}

	public void setActivated(Boolean activated) {
		this.activated = activated;
	}

	public Boolean getUpdated() {
		return updated;
	}

	public void setUpdated(Boolean updated) {
		this.updated = updated;
	}

	public String getLrTypeCode() {
		return lrTypeCode;
	}

	public void setLrTypeCode(String lrTypeCode) {
		this.lrTypeCode = lrTypeCode;
	}

	public String getLrStatusCode() {
		return lrStatusCode;
	}

	public void setLrStatusCode(String lrStatusCode) {
		this.lrStatusCode = lrStatusCode;
	}

	public Integer getLookupExitId() {
		return lookupExitId;
	}

	public void setLookupExitId(Integer lookupExitId) {
		this.lookupExitId = lookupExitId;
	}

	public String getLookupExitParams() {
		return lookupExitParams;
	}

	public void setLookupExitParams(String lookupExitParams) {
		this.lookupExitParams = lookupExitParams;
	}

	public void setLfs(Map<Integer, String> lfs) {
		this.lfs = lfs;
	}

	public Map<Integer, String> getLfs() {
		return lfs;
	}

	public Map<Integer, LRFieldTransfer> getFields() {
		return fields;
	}

	public void setFields(Map<Integer, LRFieldTransfer> fields) {
		this.fields = fields;
	}

	public LRIndexTransfer getIndex() {
		return index;
	}

	public void setIndex(LRIndexTransfer index) {
		this.index = index;
	}

	public void setIndexes(List<LRIndexFieldTransfer> ndxs) {
		this.indexFields = ndxs;
	}
	
	public void clearIndexes() {
		indexFields.clear();
	}
	
}
