package com.sumavision.bvc.command.group.forward;

import java.util.Comparator;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.sumavision.bvc.command.group.basic.CommandGroupPO;
import com.sumavision.bvc.command.group.enumeration.ExecuteStatus;
import com.sumavision.bvc.command.group.enumeration.ForwardBusinessType;
import com.sumavision.bvc.command.group.enumeration.ForwardDstType;
import com.sumavision.bvc.command.group.user.layout.player.CommandGroupUserPlayerPO;
import com.sumavision.bvc.device.group.enumeration.ChannelType;
import com.sumavision.bvc.device.monitor.live.DstDeviceType;
import com.sumavision.bvc.device.monitor.live.LiveType;
import com.sumavision.tetris.orm.po.AbstractBasePO;

/**
 * @ClassName: 转发。同 MonitorLiveDevicePO
 * @author zsy
 * @date 2019年9月23日 上午10:11:22 
 */
@Entity
@Table(name="BVC_COMMAND_GROUP_FORWARD")
public class CommandGroupForwardPO extends AbstractBasePO{
	
	private static final long serialVersionUID = 1L;
	
	/** 转发的业务类型 */
	ForwardBusinessType forwardBusinessType;
	
	/** 执行状态 */
	ExecuteStatus executeStatus;
	
	/** 转发目的类型：角色/用户/设备 */
	private ForwardDstType forwardDstType;
	
	/** 目的为角色时，角色id */
	private Long roleId;
	
	/** 目的，设备组成员id。-1表示不是成员 */
	private Long dstMemberId = -1L;
	
	/** 源，设备组成员id。-1表示不是成员 */
	private Long srcMemberId = -1L;

	/** 关联指挥 */
	CommandGroupPO group;
	
	
	

	/************
	 ***视频源****
	 ************/
	
	/** 当视频源是设备时存设备id */
	private String videoBundleId;
	
	/** 当视频源是设备时存设备名称 */
	private String videoBundleName;
	
	/** 当视频源是设备时存设备类型 */
	private String videoBundleType;
	
	/** 当视频源是设备时存设备接入层id */
	private String videoLayerId;
	
	/** 当视频源是设备时存通道id */
	private String videoChannelId;
	
	/** 当视频源是设备时存通道类型 */
	private String videoBaseType;
	
	/** 当视频源是设备时存通道名称 */
	private String videoChannelName;
	
	/************
	 ***音频源****
	 ************/
	
	/** 当音频源是设备时存设备id */
	private String audioBundleId;
	
	/** 当音频源是设备时存设备名称 */
	private String audioBundleName;
	
	/** 当音频源是设备时存设备类型 */
	private String audioBundleType;
	
	/** 当音频源是设备时存设备接入层 */
	private String audioLayerId;
	
	/** 当音频源是设备时存通道id */
	private String audioChannelId;
	
	/** 当音频源是设备时存通道类型 */
	private String audioBaseType;
	
	/** 当音频源是设备时存通道名称 */
	private String audioChannelName;
	
	/************
	 ***视频目的***
	 ************/
	
	/** 目标设备id */
	private String dstVideoBundleId;
	
	/** 目标设备名称 */
	private String dstVideoBundleName;
	
	/** 目标设备类型 */
	private String dstVideoBundleType;
	
	/** 目标设备接入层id */
	private String dstVideoLayerId;
	
	/** 目标视频通道 */
	private String dstVideoChannelId;
	
	/** 目标视频通道类型 */
	private String dstVideoBaseType;
	
	/** 目标视频通道名称 */
	private String dstVideoChannelName;
	
	/************
	 ***音频目的***
	 ************/
	
	/** 目标音频设备id */
	private String dstAudioBundleId;
	
	/** 目标音频设备名称 */
	private String dstAudioBundleName;
	
	/** 目标音频设备类型 */
	private String dstAudioBundleType;
	
	/** 目标音频设备接入层 */
	private String dstAudioLayerId;
	
	/** 目标音频通道id */
	private String dstAudioChannelId;
	
	/** 目标音频通道类型 */
	private String dstAudioBaseType;
	
	/** 目标音频通道名称 */
	private String dstAudioChannelName;
	
	/** 做点播设备业务的用户 */
	private Long userId;
	
	/** codec 模板 */
	private Long avTplId;
	
	/** codec模板档位 */
	private Long gearId;
	
	/** 目标设备类型 */
	private DstDeviceType dstDeviceType;
	
	/** 点播设备任务类型 */
	private LiveType type;
	
	/** osd显示 */
	private Long osdId;
	
	/** osd创建用户 */
	private String osdUsername;

	@Enumerated(value = EnumType.STRING)
	@Column(name = "FORWARD_BUSINESS_TYPE")
	public ForwardBusinessType getForwardBusinessType() {
		return forwardBusinessType;
	}

	public void setForwardBusinessType(ForwardBusinessType forwardBusinessType) {
		this.forwardBusinessType = forwardBusinessType;
	}

	@Enumerated(value = EnumType.STRING)
	@Column(name = "EXECUTE_STATUS")
	public ExecuteStatus getExecuteStatus() {
		return executeStatus;
	}

	public void setExecuteStatus(ExecuteStatus executeStatus) {
		this.executeStatus = executeStatus;
	}

	@ManyToOne
	@JoinColumn(name = "GROUP_ID")
	public CommandGroupPO getGroup() {
		return group;
	}

	public void setGroup(CommandGroupPO group) {
		this.group = group;
	}

	@Column(name = "VIDEO_BUNDLE_ID")
	public String getVideoBundleId() {
		return videoBundleId;
	}

	public void setVideoBundleId(String videoBundleId) {
		this.videoBundleId = videoBundleId;
	}

	@Column(name = "VIDEO_BUNDLE_NAME")
	public String getVideoBundleName() {
		return videoBundleName;
	}

	public void setVideoBundleName(String videoBundleName) {
		this.videoBundleName = videoBundleName;
	}

	@Column(name = "VIDEO_BUNDLE_TYPE")
	public String getVideoBundleType() {
		return videoBundleType;
	}

	public void setVideoBundleType(String videoBundleType) {
		this.videoBundleType = videoBundleType;
	}

	@Column(name = "VIDEO_LAYER_ID")
	public String getVideoLayerId() {
		return videoLayerId;
	}

	public void setVideoLayerId(String videoLayerId) {
		this.videoLayerId = videoLayerId;
	}

	@Column(name = "VIDEO_CHANNEL_ID")
	public String getVideoChannelId() {
		return videoChannelId;
	}

	public void setVideoChannelId(String videoChannelId) {
		this.videoChannelId = videoChannelId;
	}

	@Column(name = "VIDEO_BASE_TYPE")
	public String getVideoBaseType() {
		return videoBaseType;
	}

	public void setVideoBaseType(String videoBaseType) {
		this.videoBaseType = videoBaseType;
	}

	@Column(name = "VIDEO_CHANNEL_NAME")
	public String getVideoChannelName() {
		return videoChannelName;
	}

	public void setVideoChannelName(String videoChannelName) {
		this.videoChannelName = videoChannelName;
	}

	@Column(name = "AUDIO_BUNDLE_ID")
	public String getAudioBundleId() {
		return audioBundleId;
	}

	public void setAudioBundleId(String audioBundleId) {
		this.audioBundleId = audioBundleId;
	}

	@Column(name = "AUDIO_BUNDLE_NAME")
	public String getAudioBundleName() {
		return audioBundleName;
	}

	public void setAudioBundleName(String audioBundleName) {
		this.audioBundleName = audioBundleName;
	}

	@Column(name = "AUDIO_BUNDLE_TYPE")
	public String getAudioBundleType() {
		return audioBundleType;
	}

	public void setAudioBundleType(String audioBundleType) {
		this.audioBundleType = audioBundleType;
	}

	@Column(name = "AUDIO_LAYER_ID")
	public String getAudioLayerId() {
		return audioLayerId;
	}

	public void setAudioLayerId(String audioLayerId) {
		this.audioLayerId = audioLayerId;
	}

	@Column(name = "AUDIO_CHANNEL_ID")
	public String getAudioChannelId() {
		return audioChannelId;
	}

	public void setAudioChannelId(String audioChannelId) {
		this.audioChannelId = audioChannelId;
	}

	@Column(name = "AUDIO_BASE_TYPE")
	public String getAudioBaseType() {
		return audioBaseType;
	}

	public void setAudioBaseType(String audioBaseType) {
		this.audioBaseType = audioBaseType;
	}

	@Column(name = "AUDIO_CHANNEL_NAME")
	public String getAudioChannelName() {
		return audioChannelName;
	}

	public void setAudioChannelName(String audioChannelName) {
		this.audioChannelName = audioChannelName;
	}

	@Column(name = "DST_VIDEO_BUNDLE_ID")
	public String getDstVideoBundleId() {
		return dstVideoBundleId;
	}

	public void setDstVideoBundleId(String dstVideoBundleId) {
		this.dstVideoBundleId = dstVideoBundleId;
	}

	@Column(name = "DST_VIDEO_BUNDLE_NAME")
	public String getDstVideoBundleName() {
		return dstVideoBundleName;
	}

	public void setDstVideoBundleName(String dstVideoBundleName) {
		this.dstVideoBundleName = dstVideoBundleName;
	}

	@Column(name = "DST_VIDEO_BUNDLE_TYPE")
	public String getDstVideoBundleType() {
		return dstVideoBundleType;
	}

	public void setDstVideoBundleType(String dstVideoBundleType) {
		this.dstVideoBundleType = dstVideoBundleType;
	}

	@Column(name = "DST_VIDEO_LAYER_ID")
	public String getDstVideoLayerId() {
		return dstVideoLayerId;
	}

	public void setDstVideoLayerId(String dstVideoLayerId) {
		this.dstVideoLayerId = dstVideoLayerId;
	}

	@Column(name = "DST_VIDEO_CHANNEL_ID")
	public String getDstVideoChannelId() {
		return dstVideoChannelId;
	}

	public void setDstVideoChannelId(String dstVideoChannelId) {
		this.dstVideoChannelId = dstVideoChannelId;
	}

	@Column(name = "DST_VIDEO_BASE_TYPE")
	public String getDstVideoBaseType() {
		return dstVideoBaseType;
	}

	public void setDstVideoBaseType(String dstVideoBaseType) {
		this.dstVideoBaseType = dstVideoBaseType;
	}

	@Column(name = "DST_VIDEO_CHANNEL_NAME")
	public String getDstVideoChannelName() {
		return dstVideoChannelName;
	}

	public void setDstVideoChannelName(String dstVideoChannelName) {
		this.dstVideoChannelName = dstVideoChannelName;
	}

	@Column(name = "DST_AUDIO_BUNDLE_ID")
	public String getDstAudioBundleId() {
		return dstAudioBundleId;
	}

	public void setDstAudioBundleId(String dstAudioBundleId) {
		this.dstAudioBundleId = dstAudioBundleId;
	}

	@Column(name = "DST_AUDIO_BUNDLE_NAME")
	public String getDstAudioBundleName() {
		return dstAudioBundleName;
	}

	public void setDstAudioBundleName(String dstAudioBundleName) {
		this.dstAudioBundleName = dstAudioBundleName;
	}

	@Column(name = "DST_AUDIO_BUNDLE_TYPE")
	public String getDstAudioBundleType() {
		return dstAudioBundleType;
	}

	public void setDstAudioBundleType(String dstAudioBundleType) {
		this.dstAudioBundleType = dstAudioBundleType;
	}

	@Column(name = "DST_AUDIO_LAYER_ID")
	public String getDstAudioLayerId() {
		return dstAudioLayerId;
	}

	public void setDstAudioLayerId(String dstAudioLayerId) {
		this.dstAudioLayerId = dstAudioLayerId;
	}

	@Column(name = "DST_AUDIO_CHANNEL_ID")
	public String getDstAudioChannelId() {
		return dstAudioChannelId;
	}

	public void setDstAudioChannelId(String dstAudioChannelId) {
		this.dstAudioChannelId = dstAudioChannelId;
	}

	@Column(name = "DST_AUDIO_BASE_TYPE")
	public String getDstAudioBaseType() {
		return dstAudioBaseType;
	}

	public void setDstAudioBaseType(String dstAudioBaseType) {
		this.dstAudioBaseType = dstAudioBaseType;
	}

	@Column(name = "DST_AUDIO_CHANNEL_NAME")
	public String getDstAudioChannelName() {
		return dstAudioChannelName;
	}

	public void setDstAudioChannelName(String dstAudioChannelName) {
		this.dstAudioChannelName = dstAudioChannelName;
	}

	@Column(name = "USER_ID")
	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	@Column(name = "AVTPL_ID")
	public Long getAvTplId() {
		return avTplId;
	}

	public void setAvTplId(Long avTplId) {
		this.avTplId = avTplId;
	}

	@Column(name = "GEAR_ID")
	public Long getGearId() {
		return gearId;
	}

	public void setGearId(Long gearId) {
		this.gearId = gearId;
	}

	@Enumerated(value = EnumType.STRING)
	@Column(name = "DST_DEVICE_TYPE")
	public DstDeviceType getDstDeviceType() {
		return dstDeviceType;
	}

	public void setDstDeviceType(DstDeviceType dstDeviceType) {
		this.dstDeviceType = dstDeviceType;
	}

	@Enumerated(value = EnumType.STRING)
	@Column(name = "TYPE")
	public LiveType getType() {
		return type;
	}

	public void setType(LiveType type) {
		this.type = type;
	}

	@Column(name = "OSD_ID")
	public Long getOsdId() {
		return osdId;
	}

	public void setOsdId(Long osdId) {
		this.osdId = osdId;
	}

	@Column(name = "OSD_USERNAME")
	public String getOsdUsername() {
		return osdUsername;
	}

	public void setOsdUsername(String osdUsername) {
		this.osdUsername = osdUsername;
	}

	@Enumerated(value = EnumType.STRING)
	@Column(name = "FORWARD_DST_TYPE")
	public ForwardDstType getForwardDstType() {
		return forwardDstType;
	}

	public void setForwardDstType(ForwardDstType forwardDstType) {
		this.forwardDstType = forwardDstType;
	}

	@Column(name = "ROLE_ID")
	public Long getRoleId() {
		return roleId;
	}

	public void setRoleId(Long roleId) {
		this.roleId = roleId;
	}

	@Column(name = "DST_MEMBER_ID")
	public Long getDstMemberId() {
		return dstMemberId;
	}

	public void setDstMemberId(Long dstMemberId) {
		this.dstMemberId = dstMemberId;
	}

	@Column(name = "SRC_MEMBER_ID")
	public Long getSrcMemberId() {
		return srcMemberId;
	}

	public void setSrcMemberId(Long srcMemberId) {
		this.srcMemberId = srcMemberId;
	}
	
	public CommandGroupForwardPO() {}
	
	public CommandGroupForwardPO(
			ForwardBusinessType forwardBusinessType,
			ExecuteStatus executeStatus,
			ForwardDstType forwardDstType,
			Long dstMemberId,
			Long srcMemberId,
			String videoBundleId,
			String videoBundleName,
			String videoBundleType,
			String videoLayerId,
			String videoChannelId,
			String videoBaseType,
			String audioBundleId,
			String audioBundleName,
			String audioBundleType,
			String audioLayerId,
			String audioChannelId,
			String audioBaseType,
			String dstVideoBundleId,
			String dstVideoBundleName,
			String dstVideoBundleType,
			String dstVideoLayerId,
			String dstVideoChannelId,
			String dstVideoBaseType,
			String dstAudioBundleId,
			String dstAudioBundleName,
			String dstAudioBundleType,
			String dstAudioLayerId,
			String dstAudioChannelId,
			String dstAudioBaseType,
			Long userId,
			Long avTplId,
			Long gearId,
			DstDeviceType dstDeviceType,
			LiveType type,
			Long osdId,
			String osdUsername) throws Exception{
		
		this.forwardBusinessType = forwardBusinessType;
		this.executeStatus = executeStatus;
		this.forwardDstType = forwardDstType;
		this.dstMemberId = dstMemberId;
		this.srcMemberId = srcMemberId;
		
		this.setUpdateTime(new Date());
		this.videoBundleId = videoBundleId;
		this.videoBundleName = videoBundleName;
		this.videoBundleType = videoBundleType;
		this.videoLayerId = videoLayerId;
		this.videoChannelId = videoChannelId;
		this.videoBaseType = videoBaseType;
		this.videoChannelName = ChannelType.transChannelName(videoChannelId);
		
		if(audioBundleId != null){
			this.audioBundleId = audioBundleId;
			this.audioBundleName = audioBundleName;
			this.audioBundleType = audioBundleType;
			this.audioLayerId = audioLayerId;
			this.audioChannelId = audioChannelId;
			this.audioBaseType = audioBaseType;
			this.audioChannelName = ChannelType.transChannelName(audioChannelId);
		}
		
		this.dstVideoBundleId = dstVideoBundleId;
		this.dstVideoBundleName = dstVideoBundleName;
		this.dstVideoBundleType = dstVideoBundleType;
		this.dstVideoLayerId = dstVideoLayerId;
		this.dstVideoChannelId = dstVideoChannelId;
		this.dstVideoBaseType = dstVideoBaseType;
		this.dstVideoChannelName = ChannelType.transChannelName(dstVideoChannelId);
		
		if(dstAudioBundleId != null){
			this.dstAudioBundleId = dstAudioBundleId;
			this.dstAudioBundleName = dstAudioBundleName;
			this.dstAudioBundleType = dstAudioBundleType;
			this.dstAudioLayerId = dstAudioLayerId;
			this.dstAudioChannelId = dstAudioChannelId;
			this.dstAudioBaseType = dstAudioBaseType;
			this.dstAudioChannelName = ChannelType.transChannelName(dstAudioChannelId);
		}
		
		this.userId = userId;
		this.avTplId = avTplId;
		this.gearId = gearId;
		this.dstDeviceType = dstDeviceType;
		this.type = type;
		this.osdId = osdId;
		this.osdUsername = osdUsername;
	}
	
	public CommandGroupForwardPO setDstPlayer(CommandGroupUserPlayerPO player) throws Exception{
		
		this.setUpdateTime(new Date());
		
		this.dstVideoBundleId = player.getBundleId();
		this.dstVideoBundleName = player.getBundleName();
		this.dstVideoBundleType = player.getBundleType();
		this.dstVideoLayerId = player.getLayerId();
		this.dstVideoChannelId = player.getVideoChannelId();
		this.dstVideoBaseType = "VenusVideoOut";
		this.dstVideoChannelName = ChannelType.transChannelName(player.getVideoChannelId());
		
		this.dstAudioBundleId = player.getBundleId();
		this.dstAudioBundleName = player.getBundleName();
		this.dstAudioBundleType = player.getBundleType();
		this.dstAudioLayerId = player.getLayerId();
		this.dstAudioChannelId = player.getAudioChannelId();
		this.dstAudioBaseType = "VenusAudioOut";
		this.dstAudioChannelName = ChannelType.transChannelName(player.getAudioChannelId());
		
		return this;
	}
	
	public CommandGroupForwardPO clearDst(){
		
		this.setUpdateTime(new Date());
		
		this.dstVideoBundleId = null;
		this.dstVideoBundleName = null;
		this.dstVideoBundleType = null;
		this.dstVideoLayerId = null;
		this.dstVideoChannelId = null;
		this.dstVideoBaseType = null;
		this.dstVideoChannelName = null;
		
		this.dstAudioBundleId = null;
		this.dstAudioBundleName = null;
		this.dstAudioBundleType = null;
		this.dstAudioLayerId = null;
		this.dstAudioChannelId = null;
		this.dstAudioBaseType = null;
		this.dstAudioChannelName = null;
		
		return this;
	}
	
	/**
	 * 按id排序，从小到大<br/>
	 * <b>作者:</b>zsy<br/>
	 * <b>版本：</b>1.0<br/>
	 * <b>日期：</b>2020年4月10日 下午6:47:33
	 */
	public static final class ForwardComparatorFromId implements Comparator<CommandGroupForwardPO>{
		@Override
		public int compare(CommandGroupForwardPO o1, CommandGroupForwardPO o2) {
			
			long id1 = o1.getId();
			long id2 = o2.getId();
			
			if(id1 > id2){
				return 1;
			}
			if(id1 == id2){
				return 0;
			}
			return -1;
		}
	}
	
}
