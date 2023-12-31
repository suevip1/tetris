package com.sumavision.signal.bvc.network.po;

import javax.persistence.Entity;
import javax.persistence.Table;

import com.sumavision.tetris.orm.po.AbstractBasePO;

/**
 * 网络调度输出<br/>
 * <b>作者:</b>wjw<br/>
 * <b>版本：</b>1.0<br/>
 * <b>日期：</b>2020年2月28日 上午10:04:10
 */
@Entity
@Table(name = "TETRIS_SCL_NETWORK_OUTPUT")
public class NetworkOutputPO extends AbstractBasePO{
	
	private static final long serialVersionUID = 1L;
	
	/** 设备bundleId */
	private String bundleId;
	
	/** 设备channelId */
	private String channelId;
	
	/** 设备ip */
	private String bundleIp;
	
	/** 调度网络ip */
	private String netIp;
	
	/** 调度port */
	private Long port;
	
	/** 调度网络id */
	private String netId;
	
	/** 调度会话id */
	private Long sid;

	public String getBundleId() {
		return bundleId;
	}

	public void setBundleId(String bundleId) {
		this.bundleId = bundleId;
	}

	public String getChannelId() {
		return channelId;
	}

	public void setChannelId(String channelId) {
		this.channelId = channelId;
	}

	public String getBundleIp() {
		return bundleIp;
	}

	public void setBundleIp(String bundleIp) {
		this.bundleIp = bundleIp;
	}

	public String getNetIp() {
		return netIp;
	}

	public void setNetIp(String netIp) {
		this.netIp = netIp;
	}

	public Long getPort() {
		return port;
	}

	public void setPort(Long port) {
		this.port = port;
	}

	public String getNetId() {
		return netId;
	}

	public void setNetId(String netId) {
		this.netId = netId;
	}

	public Long getSid() {
		return sid;
	}

	public void setSid(Long sid) {
		this.sid = sid;
	}

}
