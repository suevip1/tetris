package com.sumavision.bvc.meeting.logic.po;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.sumavision.bvc.meeting.logic.po.CommonPO;
import com.sumavision.tetris.orm.po.AbstractBasePO;

@Entity
@Table(name="BVC_LOGIC_COMBINE_AUDIO_DST")
public class CombineAudioDstPO extends AbstractBasePO {
//	private String uuid;
	private String dstUuid;
	private String layerId;
	private String bundleId;
	private String channelId;
	
//	@Column(name="uuid")
//	public String getUuid() {
//		return uuid;
//	}
//
//	public void setUuid(String uuid) {
//		this.uuid = uuid;
//	}

	@Column(name="layerId")
	public String getLayerId() {
		return layerId;
	}

	public void setLayerId(String layerId) {
		this.layerId = layerId;
	}

	@Column(name="bundleId")
	public String getBundleId() {
		return bundleId;
	}

	public void setBundleId(String bundleId) {
		this.bundleId = bundleId;
	}

	@Column(name="channelId")
	public String getChannelId() {
		return channelId;
	}

	public void setChannelId(String channelId) {
		this.channelId = channelId;
	}

	@Column(name="dstUuid")
	public String getDstUuid() {
		return dstUuid;
	}

	public void setDstUuid(String dstUuid) {
		this.dstUuid = dstUuid;
	}
}
