package org.genevaers.genevaio.yamlreader;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class YAMLLookupTransfer {
	
	private List<YAMLLookupPathStepTransfer> steps = new ArrayList<>();
	private int id;
	private String name;
	private int targetLR;
	private int targetLF;
	private String comments;
	private Date createTime;
	private String createBy;
	private Date modifyTime;
	private String modifyBy;
	private Integer environmentId;
	private Boolean activated;
	private Boolean updated;
	private Boolean validInd;
	private int sourceLRId;

	public YAMLLookupTransfer() {
		
	}
	
	public void setId(int id) {
		this.id = id;
	}

	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void addStep(YAMLLookupPathStepTransfer s) {
		steps.add(s);
	}
	
	public List<YAMLLookupPathStepTransfer> getSteps() {
		return steps;
	}
	
	public void setSteps(List<YAMLLookupPathStepTransfer> steps) {
		this.steps = steps;
	}
	
	public void clearSteps() {
		steps.clear();
	}
	
	public int getTargetLF() {
		return targetLF;
	}
	
	public int getTargetLR() {
		return targetLR;
	}
	
	public void setTargetLF(int targetLF) {
		this.targetLF = targetLF;
	}
	
	public void setTargetLR(int targetLR) {
		this.targetLR = targetLR;
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

	public void setEnvironmentId(Integer environmentId) {
		this.environmentId = environmentId;
	}

	public Integer getEnvironmentId() {
		return environmentId;
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

	public Boolean getValidInd() {
		return validInd;
	}

	public void setValidInd(Boolean validInd) {
		this.validInd = validInd;
	}

	public void setSourceLRId(int sourceLRId) {
		this.sourceLRId = sourceLRId;
	}

	public int getSourceLRId() {
		return sourceLRId;
	}
}
