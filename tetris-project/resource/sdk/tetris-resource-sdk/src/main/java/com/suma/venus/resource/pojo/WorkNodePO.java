package com.suma.venus.resource.pojo;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

import com.suma.venus.resource.pojo.BundlePO.ONLINE_STATUS;

/**
 * 接入层节点pojo
 *
 * @author ylt 2017年8月16日
 */
@Entity
public class WorkNodePO extends CommonPO<WorkNodePO> {
	
	private String name;

	/**
	 * 节点IP
	 */
	private String ip;
	
	//访问端口
	private Integer port;
	
	/**
	 * 工作节点类型
	 */
	private NodeType type;
	
	/**
	 * 标识符，加唯一索引
	 */
	private String nodeUid;
	
	/**
	 * 工作节点在线状态
	 */
	private ONLINE_STATUS onlineStatus = ONLINE_STATUS.OFFLINE;
	
	/**
	 * 节点访问URL(可空)
	 */
	private String url;
	
	/** 网管url */
	private String netUrl;
	
	/** 监控url */
	private String monitorUrl;
	
	/**最后一次心跳时间*/
	private String lastHeartBeatTime;
	
	/**录制资源下载端口**/
	private Integer downloadPort;
	
	public enum NodeType {
//		ACCESS_JV210("JV210接入"),
//		ACCESS_CDN("CDN接入"),
//		ACCESS_IPC("IPC接入"),
		ACCESS_JV230("JV230接入"),
		ACCESS_TVOS("机顶盒接入"),
		ACCESS_MIXER("混合器接入"),
		ACCESS_MOBILE("手机终端接入"),
		ACCESS_JV210("音视频转发服务设备"),
		ACCESS_CDN("录像存储服务单元A型"),
		ACCESS_VOD("录像存储服务单元B型"),
		ACCESS_IPC("监控资源汇接网关"),
		ACCESS_STREAMMEDIA("流媒体管理服务设备"),
		ACCESS_NETWORK("联网服务设备"),
		ACCESS_DISPLAYCTRL("显控汇接网关"),
		ACCESS_S100("流转发器接入"),
		ACCESS_VODPROXY("点播代理服务设备");
		
        private String name;
        
        private NodeType(String name){
            this.name = name;
        }
        
        public String getName(){
            return this.name;
        }
        
        public static NodeType fromString(String s) throws Exception{
            if("ACCESS_JV210".equals(s)){
                return ACCESS_JV210;
            }else if("ACCESS_JV230".equals(s)){
                return ACCESS_JV230;
            }else if("ACCESS_TVOS".equals(s)){
                return ACCESS_TVOS;
            }else if("ACCESS_MIXER".equals(s)){
                return ACCESS_MIXER;
            }else if("ACCESS_CDN".equals(s)){
                return ACCESS_CDN;
            }else if("ACCESS_IPC".equals(s)){
                return ACCESS_IPC;
            }else if("ACCESS_MOBILE".equals(s)){
            	return ACCESS_MOBILE;
            }else if("ACCESS_VOD".equals(s)){
            	return ACCESS_VOD;
            }else if("ACCESS_STREAMMEDIA".equals(s)){
            	return ACCESS_STREAMMEDIA;
            }else if("ACCESS_NETWORK".equals(s)){
            	return ACCESS_NETWORK;
            }else if("ACCESS_DISPLAYCTL".equals(s)){
            	return ACCESS_DISPLAYCTRL;
            }else if("ACCESS_S100".equals(s)){
            	return ACCESS_S100;
            }else if("ACCESS_VODPROXY".equals(s)){
            	return ACCESS_VODPROXY;
            }else {
                throw new Exception("错误的类型："+s);
            }
        }
    }
	
	@Column
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Column
	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	@Column
	@Enumerated(EnumType.STRING)
	public NodeType getType() {
		return type;
	}
	
    public void setType(NodeType type) {
		this.type = type;
	}
    
    @Column
    @Enumerated(EnumType.STRING)
    public ONLINE_STATUS getOnlineStatus() {
        return onlineStatus;
    }

    public void setOnlineStatus(ONLINE_STATUS onlineStatus) {
        this.onlineStatus = onlineStatus;
    }

    @Column
    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
    
    @Column(name="node_uid",unique=true)
	public String getNodeUid() {
		return nodeUid;
	}

	public void setNodeUid(String nodeUid) {
		this.nodeUid = nodeUid;
	}

	@Column
	public String getLastHeartBeatTime() {
		return lastHeartBeatTime;
	}

	public void setLastHeartBeatTime(String lastHeartBeatTime) {
		this.lastHeartBeatTime = lastHeartBeatTime;
	}

	@Column
	public Integer getPort() {
		return port;
	}

	public void setPort(Integer port) {
		this.port = port;
	}

	@Column
	public Integer getDownloadPort() {
		return downloadPort;
	}

	public void setDownloadPort(Integer downloadPort) {
		this.downloadPort = downloadPort;
	}

	@Column
	public String getNetUrl() {
		return netUrl;
	}

	public void setNetUrl(String netUrl) {
		this.netUrl = netUrl;
	}

	@Column
	public String getMonitorUrl() {
		return monitorUrl;
	}

	public void setMonitorUrl(String monitorUrl) {
		this.monitorUrl = monitorUrl;
	}
}
