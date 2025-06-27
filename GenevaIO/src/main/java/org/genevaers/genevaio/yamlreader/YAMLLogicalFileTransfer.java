package org.genevaers.genevaio.yamlreader;

import java.util.Date;
import java.util.Map;
import java.util.TreeMap;

public class YAMLLogicalFileTransfer  {
	private Map<Integer, String> pfs = new TreeMap<>();
	
	private int id;
	private String name;
	private Integer environmentId;
	private String comments;
	private Date createTime;
	private String createBy;
	private Date modifyTime;
	private String modifyBy;

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
	public void addPF(Integer id, String name) {
		pfs.put(id, name);
	}
	
	public Map<Integer, String> getPfs() {
		return pfs;
	}
	
	public void setPfs(Map<Integer, String> ps) {
		pfs = ps;
	}
	
	public void removePF(Integer id) {
		pfs.remove(id);
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
}
