package com.suma.venus.resource.pojo;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

import com.suma.venus.resource.pojo.BundlePO.SOURCE_TYPE;
import com.suma.venus.resource.pojo.BundlePO.SYNC_STATUS;

//服务节点PO
@Entity
public class SerNodePO extends CommonPO<SerNodePO>{
	
	private String nodeUuid;
	
	private String nodeName;
	
	//上级服务节点ID
	private String nodeFather;
	
	//关联服务节点 ID
	private String nodeRelations;
	
	//厂商信息
	private String nodeFactInfo;
	
	/** 节点路由信息 */
	private String nodeRouter;
	
	/** 订阅节点 */
	private String nodePublisher;
	
	private SYNC_STATUS syncStatus = SYNC_STATUS.ASYNC;
	
	private SOURCE_TYPE sourceType = SOURCE_TYPE.SYSTEM;
	
	public SerNodePO() {
		super();
	}
	
	public SerNodePO(String nodeUuid, String nodeName, String nodeFather, String nodeRelations, 
			String nodeFactInfo) {
		super();
		this.nodeUuid = nodeUuid;
		this.nodeName = nodeName;
		this.nodeFather = nodeFather;
		this.nodeRelations = nodeRelations;
		this.nodeFactInfo = nodeFactInfo;
	}

	@Column(unique=true)
	public String getNodeUuid() {
		return nodeUuid;
	}

	public void setNodeUuid(String nodeUuid) {
		this.nodeUuid = nodeUuid;
	}

	@Column
	public String getNodeName() {
		return nodeName;
	}

	public void setNodeName(String nodeName) {
		this.nodeName = nodeName;
	}

	@Column
	public String getNodeFather() {
		return nodeFather;
	}

	public void setNodeFather(String nodeFather) {
		this.nodeFather = nodeFather;
	}

	@Column
	public String getNodeRelations() {
		return nodeRelations;
	}

	public void setNodeRelations(String nodeRelations) {
		this.nodeRelations = nodeRelations;
	}

	@Column
	public String getNodeFactInfo() {
		return nodeFactInfo;
	}

	public void setNodeFactInfo(String nodeFactInfo) {
		this.nodeFactInfo = nodeFactInfo;
	}
	
	@Column(name="sync_status")
	@Enumerated(EnumType.STRING)
	public SYNC_STATUS getSyncStatus() {
		return syncStatus;
	}

	public void setSyncStatus(SYNC_STATUS syncStatus) {
		this.syncStatus = syncStatus;
	}
	
	@Column(name="source_type")
	@Enumerated(EnumType.STRING)
	public SOURCE_TYPE getSourceType() {
		return sourceType;
	}

	public void setSourceType(SOURCE_TYPE sourceType) {
		this.sourceType = sourceType;
	}

	@Column(name="node_router", columnDefinition = "longtext")
	public String getNodeRouter() {
		return nodeRouter;
	}

	public void setNodeRouter(String nodeRouter) {
		this.nodeRouter = nodeRouter;
	}
	
	@Column
	public String getNodePublisher() {
		return nodePublisher;
	}

	public void setNodePublisher(String nodePublisher) {
		this.nodePublisher = nodePublisher;
	}
}
