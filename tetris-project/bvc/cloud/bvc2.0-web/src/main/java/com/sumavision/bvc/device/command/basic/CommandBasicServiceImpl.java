package com.sumavision.bvc.device.command.basic;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.suma.venus.resource.base.bo.UserBO;
import com.suma.venus.resource.dao.FolderUserMapDAO;
import com.suma.venus.resource.pojo.BundlePO;
import com.suma.venus.resource.pojo.FolderPO;
import com.suma.venus.resource.pojo.FolderUserMap;
import com.suma.venus.resource.service.ResourceRemoteService;
import com.suma.venus.resource.service.ResourceService;
import com.sumavision.bvc.command.group.basic.CommandGroupAvtplGearsPO;
import com.sumavision.bvc.command.group.basic.CommandGroupAvtplPO;
import com.sumavision.bvc.command.group.basic.CommandGroupMemberPO;
import com.sumavision.bvc.command.group.basic.CommandGroupPO;
import com.sumavision.bvc.command.group.dao.CommandGroupDAO;
import com.sumavision.bvc.command.group.dao.CommandGroupMemberDAO;
import com.sumavision.bvc.command.group.dao.CommandGroupUserPlayerDAO;
import com.sumavision.bvc.command.group.enumeration.EditStatus;
import com.sumavision.bvc.command.group.enumeration.ExecuteStatus;
import com.sumavision.bvc.command.group.enumeration.ForwardBusinessType;
import com.sumavision.bvc.command.group.enumeration.ForwardDemandBusinessType;
import com.sumavision.bvc.command.group.enumeration.ForwardDemandStatus;
import com.sumavision.bvc.command.group.enumeration.ForwardDstType;
import com.sumavision.bvc.command.group.enumeration.GroupSpeakType;
import com.sumavision.bvc.command.group.enumeration.GroupStatus;
import com.sumavision.bvc.command.group.enumeration.GroupType;
import com.sumavision.bvc.command.group.enumeration.MediaType;
import com.sumavision.bvc.command.group.enumeration.MemberStatus;
import com.sumavision.bvc.command.group.enumeration.OriginType;
import com.sumavision.bvc.command.group.forward.CommandGroupForwardDemandPO;
import com.sumavision.bvc.command.group.forward.CommandGroupForwardPO;
import com.sumavision.bvc.command.group.user.layout.player.CommandGroupUserPlayerPO;
import com.sumavision.bvc.command.group.user.layout.player.PlayerBusinessType;
import com.sumavision.bvc.control.device.command.group.vo.BusinessPlayerVO;
import com.sumavision.bvc.device.command.bo.MessageSendCacheBO;
import com.sumavision.bvc.device.command.cascade.util.CommandCascadeUtil;
import com.sumavision.bvc.device.command.cast.CommandCastServiceImpl;
import com.sumavision.bvc.device.command.common.CommandCommonServiceImpl;
import com.sumavision.bvc.device.command.common.CommandCommonUtil;
import com.sumavision.bvc.device.command.exception.CommandGroupNameAlreadyExistedException;
import com.sumavision.bvc.device.command.exception.HasNotUsefulPlayerException;
import com.sumavision.bvc.device.command.exception.UserHasNoAvailableEncoderException;
import com.sumavision.bvc.device.command.exception.UserHasNoFolderException;
import com.sumavision.bvc.device.command.meeting.CommandMeetingSpeakServiceImpl;
import com.sumavision.bvc.device.command.record.CommandRecordServiceImpl;
import com.sumavision.bvc.device.command.time.CommandFightTimeServiceImpl;
import com.sumavision.bvc.device.command.vod.CommandVodService;
import com.sumavision.bvc.device.group.bo.CodecParamBO;
import com.sumavision.bvc.device.group.bo.ConnectBO;
import com.sumavision.bvc.device.group.bo.ConnectBundleBO;
import com.sumavision.bvc.device.group.bo.DisconnectBundleBO;
import com.sumavision.bvc.device.group.bo.ForwardDelBO;
import com.sumavision.bvc.device.group.bo.ForwardSetBO;
import com.sumavision.bvc.device.group.bo.LogicBO;
import com.sumavision.bvc.device.group.bo.PassByBO;
import com.sumavision.bvc.device.group.bo.XtBusinessPassByContentBO;
import com.sumavision.bvc.device.group.enumeration.ChannelType;
import com.sumavision.bvc.device.group.service.test.ExecuteBusinessProxy;
import com.sumavision.bvc.device.group.service.util.CommonQueryUtil;
import com.sumavision.bvc.device.group.service.util.QueryUtil;
import com.sumavision.bvc.device.monitor.live.DstDeviceType;
import com.sumavision.bvc.feign.ResourceServiceClient;
import com.sumavision.bvc.log.OperationLogService;
import com.sumavision.bvc.meeting.logic.ExecuteBusinessReturnBO;
import com.sumavision.bvc.resource.dao.ResourceBundleDAO;
import com.sumavision.bvc.resource.dao.ResourceChannelDAO;
import com.sumavision.bvc.resource.dto.ChannelSchemeDTO;
import com.sumavision.bvc.system.po.AvtplGearsPO;
import com.sumavision.bvc.system.po.AvtplPO;
import com.sumavision.tetris.auth.token.TerminalType;
import com.sumavision.tetris.bvc.cascade.CommandCascadeService;
import com.sumavision.tetris.bvc.cascade.ConferenceCascadeService;
import com.sumavision.tetris.bvc.cascade.bo.GroupBO;
import com.sumavision.tetris.bvc.cascade.bo.MinfoBO;
import com.sumavision.tetris.commons.exception.BaseException;
import com.sumavision.tetris.commons.exception.code.StatusCode;
import com.sumavision.tetris.commons.util.date.DateUtil;
import com.sumavision.tetris.commons.util.wrapper.ArrayListWrapper;
import com.sumavision.tetris.commons.util.wrapper.HashMapWrapper;
import com.sumavision.tetris.commons.util.wrapper.HashSetWrapper;
import com.sumavision.tetris.commons.util.wrapper.StringBufferWrapper;
import com.sumavision.tetris.user.UserQuery;
import com.sumavision.tetris.user.UserVO;
import com.sumavision.tetris.websocket.message.WebsocketMessageService;
import com.sumavision.tetris.websocket.message.WebsocketMessageType;
import com.sumavision.tetris.websocket.message.WebsocketMessageVO;
import lombok.extern.slf4j.Slf4j;

/**
 * 
* @ClassName: CommandBasicServiceImpl 
* @Description: 普通会议业务
* @author zsy
* @date 2019年10月24日 上午10:56:48 
*
 */
@Slf4j
@Transactional(rollbackFor = Exception.class)
@Service
public class CommandBasicServiceImpl {
	
	/** 是否强制接听进入指挥和会议，false需要点击同意 */
	private static boolean autoEnter = true;
	
	@Autowired
	private CommandGroupDAO commandGroupDao;
	
	@Autowired
	private CommandGroupUserPlayerDAO commandGroupUserPlayerDao;
	
	@Autowired
	private CommandGroupMemberDAO commandGroupMemberDao;
	
	@Autowired
	private ResourceBundleDAO resourceBundleDao;
	
	@Autowired
	private ResourceChannelDAO resourceChannelDao;
	
	@Autowired
	private FolderUserMapDAO folderUserMapDao;
	
	@Autowired
	private ResourceService resourceService;
	
	@Autowired
	private CommandCommonServiceImpl commandCommonServiceImpl;
	
	@Autowired
	private CommandCastServiceImpl commandCastServiceImpl;
	
	@Autowired
	private CommandRecordServiceImpl commandRecordServiceImpl;
	
	@Autowired
	private CommandFightTimeServiceImpl commandFightTimeServiceImpl;
	
	@Autowired
	private CommandMeetingSpeakServiceImpl commandMeetingSpeakServiceImpl;
	
	@Autowired
	private CommandVodService commandVodService;
	
	@Autowired
	private ResourceServiceClient resourceServiceClient;
	
	@Autowired
	private ResourceRemoteService resourceRemoteService;

	@Autowired
	private WebsocketMessageService websocketMessageService;
	
	@Autowired
	private QueryUtil queryUtil;
	
	@Autowired
	private CommandCommonUtil commandCommonUtil;
	
	@Autowired
	private CommonQueryUtil commonQueryUtil;
	
	@Autowired
	private ExecuteBusinessProxy executeBusiness;	
	
	@Autowired
	private OperationLogService operationLogService;
	
	@Autowired
	private UserQuery userQuery;
	
	@Autowired
	private CommandCascadeUtil commandCascadeUtil;
	
	@Autowired
	private CommandCascadeService commandCascadeService;
	
	@Autowired
	private ConferenceCascadeService conferenceCascadeService;
		
	public CommandGroupPO save(
			Long creatorUserId,
			Long chairmanUserId,
			String creatorUsername,
			String name,
			String subject,
			GroupType type,
			OriginType originType,
			List<Long> userIdList
			) throws Exception{
		
		return save(
				creatorUserId,
				chairmanUserId,
				creatorUsername,
				name,
				subject,
				type,
				originType,
				userIdList,
				null);
	}
	
	/**
	 * 
	 * 新建一个会议<br/>
	 * <p>详细描述</p>
	 * <b>作者:</b>zsy<br/>
	 * <b>版本：</b>1.0<br/>
	 * <b>日期：</b>2019年10月24日 上午11:01:25
	 * @param creatorUserId
	 * @param chairmanUserId
	 * @param creatorUsername
	 * @param name 名称
	 * @param type BASIC或SECRET
	 * @param userIdList 成员的userId列表
	 * @param uuid 级联时使用，非级联为null
	 * @return
	 * @throws Exception
	 */
	public CommandGroupPO save(
			Long creatorUserId,
			Long chairmanUserId,
			String creatorUsername,
			String name,
			String subject,
			GroupType type,
			OriginType originType,
			List<Long> userIdList,
			String uuid
			) throws Exception{
		
		CommandGroupPO group1 = commandGroupDao.findByUuid(uuid);
		if(group1 != null){
			log.warn("级联建会，uuid已经存在：" + uuid);
			return group1;
		}
		
		UserBO creatorUserBo = resourceService.queryUserById(creatorUserId, TerminalType.QT_ZK);
		if(creatorUserBo == null){
			throw new BaseException(StatusCode.FORBIDDEN, "当前用户已失效，请重新登录");
		}
		if(creatorUserBo.getFolderUuid() == null){
			throw new UserHasNoFolderException(creatorUserBo.getName());
		}
		
		//确保成员中有创建者
		if(!userIdList.contains(creatorUserId)){
			userIdList.add(creatorUserId);
		}		
		
		//本系统创建，则鉴权，则校验主席有编码器
		if(!OriginType.OUTER.equals(originType)){
			
			//鉴权，区分指挥与会议
//			if(type.equals(GroupType.BASIC)){
//				commandCommonServiceImpl.authorizeUsers(userIdList, chairmanUserId, BUSINESS_OPR_TYPE.ZK);
//			}else if(type.equals(GroupType.MEETING)){
//				commandCommonServiceImpl.authorizeUsers(userIdList, chairmanUserId, BUSINESS_OPR_TYPE.HY);
//			}
			
			String creatorEncoderId = commonQueryUtil.queryExternalOrLocalEncoderIdFromUserBO(creatorUserBo);
			List<BundlePO> creatorBundleEntities = resourceBundleDao.findByBundleIds(new ArrayListWrapper<String>().add(creatorEncoderId).getList());
			if(creatorBundleEntities.size() == 0){
				throw new UserHasNoAvailableEncoderException(creatorUserBo.getName());
			}
		}
		
		//会议组重名校验
		if(!OriginType.OUTER.equals(originType)){
			List<CommandGroupPO> existGroups = commandGroupDao.findByName(name);
			if(existGroups!=null && existGroups.size()>0){
				List<CommandGroupPO> likeGroups = commandGroupDao.findByNameLike(name + "-%");
				String recommendedName0 = name + "-";
				String recommendedName = null;
				boolean ok = false;
				for(int i=2; ; i++){
					ok = true;
					recommendedName = recommendedName0 + i;
					for(CommandGroupPO likeGroup : likeGroups){
						if(recommendedName.equals(likeGroup.getName())){
							ok = false;
							break;
						}
					}
					if(ok){
						break;
					}
				}
				throw new CommandGroupNameAlreadyExistedException(name, recommendedName);
			}
		}
		
		CommandGroupPO group = new CommandGroupPO();
		if(uuid != null){
			group.setUuid(uuid);
		}
		group.setName(name);
		group.setSubject(subject);
		group.setType(type);
		group.setOriginType(originType);
		group.setEditStatus(EditStatus.NORMAL);
		
		group.setUserId(creatorUserId);
		group.setUserName(creatorUsername);
		group.setCreatetime(new Date());
		
//		group.setRecord(false);
		group.setStatus(GroupStatus.STOP);
		
		//参数模板
		Map<String, Object> result = commandCommonServiceImpl.queryDefaultAvCodec();
		AvtplPO sys_avtpl = (AvtplPO)result.get("avtpl");
		Set<AvtplGearsPO> sys_gears = sys_avtpl.getGears();
		CommandGroupAvtplPO g_avtpl = new CommandGroupAvtplPO().set(sys_avtpl);
		g_avtpl.setGears(new HashSet<CommandGroupAvtplGearsPO>());
		CommandGroupAvtplGearsPO currentGear = null;
		for(AvtplGearsPO sys_gear:sys_gears){
			CommandGroupAvtplGearsPO g_gear = new CommandGroupAvtplGearsPO().set(sys_gear);
			g_avtpl.getGears().add(g_gear);
			g_gear.setAvtpl(g_avtpl);
			currentGear = g_gear;
			break;
		}
		group.setAvtpl(g_avtpl);
		g_avtpl.setGroup(group);
		group.setCurrentGearLevel(currentGear.getLevel());
		
		//保存以获得id
		commandGroupDao.save(group);
		
		//TODO:关联主席、会议员角色
		
		//【注：解码器（播放器）在开启会议时选择，如果选择不到，则该成员的转发状态为 ExecuteStatus.NO_AVAILABLE_PLAYER】
		//用户管理层的批量接口，根据userIds查询List<UserBO>，由于缺少folderId，所以额外查询queryAllFolders，给UserBO中的folderId赋值
		String userIdListStr = StringUtils.join(userIdList.toArray(), ",");
		List<UserBO> commandUserBos = resourceService.queryUserListByIds(userIdListStr, TerminalType.QT_ZK);
		if(commandUserBos == null) commandUserBos = new ArrayList<UserBO>();
		List<FolderPO> allFolders = resourceService.queryAllFolders();
		
		List<FolderUserMap> folderUserMaps = folderUserMapDao.findByUserIdIn(userIdList);
		String localLayerId = null;
		
		//从List<UserBO>取出bundleId列表，注意判空；给UserBO中的folderId赋值
		List<String> bundleIds = new ArrayList<String>();
		for(UserBO user : commandUserBos){
			String encoderId = commonQueryUtil.queryExternalOrLocalEncoderIdFromUserBO(user);
			if(encoderId != null){
				bundleIds.add(encoderId);
			}
			for(FolderPO folder : allFolders){
				if(folder.getUuid().equals(user.getFolderUuid())){
					user.setFolderId(folder.getId());
					break;
				}
			}
		}
		
		//从bundleId列表查询所有的bundlePO
		List<BundlePO> srcBundleEntities = resourceBundleDao.findByBundleIds(bundleIds);
		if(srcBundleEntities == null) srcBundleEntities = new ArrayList<BundlePO>();
		
		//从bundleId列表查询所有的视频编码1通道
		List<ChannelSchemeDTO> videoEncode1Channels = resourceChannelDao.findByBundleIdsAndChannelId(bundleIds, ChannelType.VIDEOENCODE1.getChannelId());
		if(videoEncode1Channels == null) videoEncode1Channels = new ArrayList<ChannelSchemeDTO>();
		//通过视频编码通道来校验编码器是否可用
		if(videoEncode1Channels.size() < commandUserBos.size()){
			for(UserBO user : commandUserBos){
				
				//外部系统用户则跳过
				if(queryUtil.isLdapUser(user, folderUserMaps)){
					continue;
				}
				
				boolean hasChannel = false;
				String encoderId = commonQueryUtil.queryExternalOrLocalEncoderIdFromUserBO(user);
				for(ChannelSchemeDTO channel : videoEncode1Channels){
					if(channel.getBundleId().equals(encoderId)){
						hasChannel = true;
						break;
					}
				}
				if(!hasChannel){
					throw new UserHasNoAvailableEncoderException(user.getName());
				}
			}
		}
		
		//从bundleId列表查询所有的音频编码1通道
		List<ChannelSchemeDTO> audioEncode1Channels = resourceChannelDao.findByBundleIdsAndChannelId(bundleIds, ChannelType.AUDIOENCODE1.getChannelId());
		if(audioEncode1Channels == null) audioEncode1Channels = new ArrayList<ChannelSchemeDTO>();
		
		List<CommandGroupMemberPO> members = new ArrayList<CommandGroupMemberPO>();
		List<CommandGroupForwardPO> forwards = new ArrayList<CommandGroupForwardPO>();
		CommandGroupMemberPO chairmanMember = null;
//		BasicRolePO chairmanRole = basicRoleDao.findByName("主席");
//		BasicRolePO memberRole = basicRoleDao.findByName("会议员");
		for(UserBO user : commandUserBos){
			//注意判断主席
			CommandGroupMemberPO memberPO = new CommandGroupMemberPO();
			if(chairmanUserId.equals(user.getId())){
//				chairmanMember = memberPO;//这里不好使
				memberPO.setAdministrator(true);
				//关联主席角色
//				memberPO.setRoleId(chairmanRole.getId());
//				memberPO.setRoleName(chairmanRole.getName());
			}else{
				//关联会议员角色
//				memberPO.setRoleId(memberRole.getId());
//				memberPO.setRoleName(memberRole.getName());
				
			}
			
			memberPO.setUserId(user.getId());
			memberPO.setUserName(user.getName());
			memberPO.setUserNum(user.getUserNo());
			memberPO.setGroup(group);
			if(user.getFolderId() == null){
				throw new BaseException(StatusCode.FORBIDDEN, memberPO.getUserName() + " 没有组织机构！");
			}
			memberPO.setFolderId(user.getFolderId());
			members.add(memberPO);
			
			//ldap用户，生成一套参数id
			if(queryUtil.isLdapUser(user, folderUserMaps)){
				if(localLayerId == null){
					localLayerId = resourceRemoteService.queryLocalLayerId();
				}
				memberPO.setOriginType(OriginType.OUTER);
				memberPO.setSrcBundleId(memberPO.getUserNum() + "user" + UUID.randomUUID().toString().replace("-", ""));
				memberPO.setSrcLayerId(localLayerId);
				memberPO.setSrcVideoChannelId(ChannelType.VIDEOENCODE1.getChannelId());
				memberPO.setSrcAudioChannelId(ChannelType.AUDIOENCODE1.getChannelId());
				continue;
			}
			
//			//遍历bundle
			String encoderId = commonQueryUtil.queryExternalOrLocalEncoderIdFromUserBO(user);
			for(BundlePO bundle : srcBundleEntities){
				if(bundle.getBundleId().equals(encoderId)){
					memberPO.setSrcBundleId(bundle.getBundleId());
					memberPO.setSrcBundleName(bundle.getBundleName());
					memberPO.setSrcBundleType(bundle.getDeviceModel());
					memberPO.setSrcVenusBundleType(bundle.getBundleType());
					memberPO.setSrcLayerId(bundle.getAccessNodeUid());
					break;
				}
			}
			
			//遍历视频通道
			for(ChannelSchemeDTO videoChannel : videoEncode1Channels){
				if(videoChannel.getBundleId().equals(encoderId)){
					memberPO.setSrcVideoChannelId(videoChannel.getChannelId());
					break;
				}
			}
			
			//遍历音频通道
			for(ChannelSchemeDTO audioChannel : audioEncode1Channels){
				if(audioChannel.getBundleId().equals(encoderId)){
					memberPO.setSrcAudioChannelId(audioChannel.getChannelId());
					break;
				}
			}
		}
		
		//保存以获得member的id
		group.setMembers(members);
		commandGroupDao.save(group);
		
		//获得主席
		for(CommandGroupMemberPO member : members){
			if(member.isAdministrator()){
				chairmanMember = member;
			}
		}
		
		for(CommandGroupMemberPO member : members){
			if(member.isAdministrator()){
				//建立所有成员到主席的转发（在下边）
				
			}else{
				//建立主席到成员的转发
				CommandGroupForwardPO c2m_forward = new CommandGroupForwardPO(
						ForwardBusinessType.BASIC_COMMAND,
						ExecuteStatus.UNDONE,
						ForwardDstType.USER,
						member.getId(),
						chairmanMember.getId(),
						chairmanMember.getSrcBundleId(),
						chairmanMember.getSrcBundleName(),
						chairmanMember.getSrcVenusBundleType(),
						chairmanMember.getSrcLayerId(),
						chairmanMember.getSrcVideoChannelId(),
						"VenusVideoIn",//videoBaseType,
						chairmanMember.getSrcBundleId(),
						chairmanMember.getSrcBundleName(),
						chairmanMember.getSrcBundleType(),
						chairmanMember.getSrcLayerId(),
						chairmanMember.getSrcAudioChannelId(),
						"VenusAudioIn",//String audioBaseType,
						null,//member.getDstBundleId(),
						null,//member.getDstBundleName(),
						null,//member.getDstBundleType(),
						null,//member.getDstLayerId(),
						null,//member.getDstVideoChannelId(),
						"VenusVideoOut",//String dstVideoBaseType,
						null,//member.getDstAudioChannelId(),
						null,//member.getDstBundleName(),
						null,//member.getDstBundleType(),
						null,//member.getDstLayerId(),
						null,//member.getDstAudioChannelId(),
						"VenusAudioOut",//String dstAudioBaseType,
						creatorUserId,
						g_avtpl.getId(),//Long avTplId,
						currentGear.getId(),//Long gearId,
						DstDeviceType.WEBSITE_PLAYER,
						null,//LiveType type,
						null,//Long osdId,
						null//String osdUsername);
						);
				c2m_forward.setGroup(group);
				forwards.add(c2m_forward);
				
				//建立成员到主席的转发
				CommandGroupForwardPO m2c_forward = new CommandGroupForwardPO(
						ForwardBusinessType.BASIC_COMMAND,
						ExecuteStatus.UNDONE,
						ForwardDstType.USER,
						chairmanMember.getId(),
						member.getId(),
						member.getSrcBundleId(),
						member.getSrcBundleName(),
						member.getSrcVenusBundleType(),
						member.getSrcLayerId(),
						member.getSrcVideoChannelId(),
						"VenusVideoIn",//videoBaseType,
						member.getSrcBundleId(),
						member.getSrcBundleName(),
						member.getSrcBundleType(),
						member.getSrcLayerId(),
						member.getSrcAudioChannelId(),
						"VenusAudioIn",//String audioBaseType,
						null,//chairmanMember.getDstBundleId(),
						null,//chairmanMember.getDstBundleName(),
						null,//chairmanMember.getDstBundleType(),
						null,//chairmanMember.getDstLayerId(),
						null,//chairmanMember.getDstVideoChannelId(),
						null,//String dstVideoBaseType,
						null,//chairmanMember.getDstAudioChannelId(),
						null,//chairmanMember.getDstBundleName(),
						null,//chairmanMember.getDstBundleType(),
						null,//chairmanMember.getDstLayerId(),
						null,//chairmanMember.getDstAudioChannelId(),
						null,//String dstAudioBaseType,
						creatorUserId,
						g_avtpl.getId(),//Long avTplId,
						currentGear.getId(),//Long gearId,
						DstDeviceType.WEBSITE_PLAYER,
						null,//LiveType type,
						null,//Long osdId,
						null//String osdUsername);
						);
				m2c_forward.setGroup(group);
				forwards.add(m2c_forward);
				
			}
		}
		
		group.setForwards(forwards);
		
		commandGroupDao.save(group);
		
		if(!OriginType.OUTER.equals(originType)){
			if(GroupType.BASIC.equals(type)){
				GroupBO groupBO = commandCascadeUtil.createCommand(group);
				commandCascadeService.create(groupBO);
			}else if(GroupType.MEETING.equals(type)){
				GroupBO groupBO = commandCascadeUtil.createMeeting(group);
				conferenceCascadeService.create(groupBO);
			}
		}
		
		log.info(name + " 创建完成");
		operationLogService.send(creatorUserBo.getName(), "新建指挥", creatorUserBo.getName() + "新建指挥groupId:" + group.getId());
		return group;
	}	
	
	/**
	 * 刷新一个会议信息<br/>
	 * <p>刷新内容：用户换编码器，编码器换接入层，重建参数模板</p>
	 * <b>作者:</b>zsy<br/>
	 * <b>版本：</b>1.0<br/>
	 * <b>日期：</b>2020年4月13日 下午6:50:51
	 * @param group
	 * @return
	 * @throws Exception
	 */
	private CommandGroupPO refresh(CommandGroupPO group) throws Exception{
		
		Long creatorUserId = group.getUserId();
		UserBO creatorUserBo = resourceService.queryUserById(group.getUserId(), TerminalType.QT_ZK);
		
		//清除需要重新生成的数据
		List<CommandGroupForwardPO> forwards = group.getForwards();		
		forwards.removeAll(forwards);
		
		//参数模板
		Map<String, Object> result = commandCommonServiceImpl.queryDefaultAvCodec();
		AvtplPO sys_avtpl = (AvtplPO)result.get("avtpl");
		Set<AvtplGearsPO> sys_gears = sys_avtpl.getGears();
//		g_avtpl.setGears(new HashSet<CommandGroupAvtplGearsPO>());
		CommandGroupAvtplPO g_avtpl = group.getAvtpl().set(sys_avtpl);//new CommandGroupAvtplPO().set(sys_avtpl);
		g_avtpl.getGears().removeAll(g_avtpl.getGears());
		CommandGroupAvtplGearsPO currentGear = null;
		for(AvtplGearsPO sys_gear:sys_gears){
			CommandGroupAvtplGearsPO g_gear = new CommandGroupAvtplGearsPO().set(sys_gear);
			g_avtpl.getGears().add(g_gear);
			g_gear.setAvtpl(g_avtpl);
			currentGear = g_gear;
			break;
		}
		group.setAvtpl(g_avtpl);
		g_avtpl.setGroup(group);
		group.setCurrentGearLevel(currentGear.getLevel());
		group.setSpeakType(GroupSpeakType.CHAIRMAN);
		
		//保存以获得id
//		commandGroupDao.save(group);
		
		List<Long> userIdList = new ArrayList<Long>();
		List<CommandGroupMemberPO> members = group.getMembers();
		CommandGroupMemberPO chairmanMember = commandCommonUtil.queryChairmanMember(members);
		for(CommandGroupMemberPO member : members){
			userIdList.add(member.getUserId());
		}
		String userIdListStr = StringUtils.join(userIdList.toArray(), ",");
		List<UserBO> commandUserBos = resourceService.queryUserListByIds(userIdListStr, TerminalType.QT_ZK);
		if(commandUserBos == null) commandUserBos = new ArrayList<UserBO>();
		List<FolderPO> allFolders = resourceService.queryAllFolders();
		
		List<FolderUserMap> folderUserMaps = folderUserMapDao.findByUserIdIn(userIdList);
		
		if(creatorUserBo.getFolderUuid() == null){
			throw new UserHasNoFolderException(creatorUserBo.getName());
		}
		//从List<UserBO>取出bundleId列表，注意判空；给UserBO中的folderId赋值
		List<String> bundleIds = new ArrayList<String>();
		for(UserBO user : commandUserBos){
			String bundleId = commonQueryUtil.queryExternalOrLocalEncoderIdFromUserBO(user);
			if(bundleId != null){
				bundleIds.add(bundleId);
			}
			for(FolderPO folder : allFolders){
				if(folder.getUuid().equals(user.getFolderUuid())){
					user.setFolderId(folder.getId());
					break;
				}
			}
		}
		
		//从bundleId列表查询所有的bundlePO
		List<BundlePO> srcBundleEntities = resourceBundleDao.findByBundleIds(bundleIds);
		if(srcBundleEntities == null) srcBundleEntities = new ArrayList<BundlePO>();
		
		//从bundleId列表查询所有的视频编码1通道
		List<ChannelSchemeDTO> videoEncode1Channels = resourceChannelDao.findByBundleIdsAndChannelId(bundleIds, ChannelType.VIDEOENCODE1.getChannelId());
		if(videoEncode1Channels == null) videoEncode1Channels = new ArrayList<ChannelSchemeDTO>();
		//通过视频编码通道来校验编码器是否可用
		if(videoEncode1Channels.size() < commandUserBos.size()){
			for(UserBO user : commandUserBos){
				
				//外部系统用户则跳过
				if(queryUtil.isLdapUser(user, folderUserMaps)){
					continue;
				}
				
				boolean hasChannel = false;
				for(ChannelSchemeDTO channel : videoEncode1Channels){
					String encoderId = commonQueryUtil.queryExternalOrLocalEncoderIdFromUserBO(user);
					if(encoderId!=null && channel.getBundleId().equals(encoderId)){
						hasChannel = true;
						break;
					}
				}
				if(!hasChannel){
					throw new UserHasNoAvailableEncoderException(user.getName());
				}
			}
		}
		
		//从bundleId列表查询所有的音频编码1通道
		List<ChannelSchemeDTO> audioEncode1Channels = resourceChannelDao.findByBundleIdsAndChannelId(bundleIds, ChannelType.AUDIOENCODE1.getChannelId());
		if(audioEncode1Channels == null) audioEncode1Channels = new ArrayList<ChannelSchemeDTO>();
					
		for(CommandGroupMemberPO member : members){
			UserBO user = queryUtil.queryUserById(commandUserBos, member.getUserId());
			if(user == null){
				continue;//后续考虑删除成员
			}
			
			//重设folderId
			if(user.getFolderId() == null){
				throw new BaseException(StatusCode.FORBIDDEN, member.getUserName() + " 没有组织机构！");
			}
			member.setFolderId(user.getFolderId());
			
			//跳过ldap用户
			if(queryUtil.isLdapUser(user, folderUserMaps)){
//				member.setOriginType(OriginType.OUTER);
				continue;
			}
			
			String encoderId = commonQueryUtil.queryExternalOrLocalEncoderIdFromUserBO(user);
			BundlePO bundle = queryUtil.queryBundlePOByBundleId(srcBundleEntities, encoderId);
			//校验编码器是否存在
			if(bundle == null){
				throw new BaseException(StatusCode.FORBIDDEN, member.getUserName() + " 的编码器异常！");
			}
			//校验编码器是否有layerId
			if(bundle.getAccessNodeUid() == null || "".equals(bundle.getAccessNodeUid())){
				throw new BaseException(StatusCode.FORBIDDEN, member.getUserName() + " 的编码器未上线！");
			}
			if(member.getSrcBundleId().equals(encoderId)){
				if(!bundle.getAccessNodeUid().equals(member.getSrcLayerId())){
					//bundle不变，layer变了
					member.setSrcLayerId(bundle.getAccessNodeUid());
				}
			}else{
				//bundle变了
				member.setSrcBundleId(bundle.getBundleId());
				member.setSrcBundleName(bundle.getBundleName());
				member.setSrcBundleType(bundle.getDeviceModel());
				member.setSrcVenusBundleType(bundle.getBundleType());
				member.setSrcLayerId(bundle.getAccessNodeUid());
				
				//遍历视频通道
				for(ChannelSchemeDTO videoChannel : videoEncode1Channels){
					if(videoChannel.getBundleId().equals(encoderId)){
						member.setSrcVideoChannelId(videoChannel.getChannelId());
						break;
					}
				}
				
				//遍历音频通道
				for(ChannelSchemeDTO audioChannel : audioEncode1Channels){
					if(audioChannel.getBundleId().equals(encoderId)){
						member.setSrcAudioChannelId(audioChannel.getChannelId());
						break;
					}
				}
			}			
		}
		
		for(CommandGroupMemberPO member : members){
			if(member.isAdministrator()){
				//建立所有成员到主席的转发（在下边）				
			}else{
				//建立主席到成员的转发
				CommandGroupForwardPO c2m_forward = new CommandGroupForwardPO(
						ForwardBusinessType.BASIC_COMMAND,
						ExecuteStatus.UNDONE,
						ForwardDstType.USER,
						member.getId(),
						chairmanMember.getId(),
						chairmanMember.getSrcBundleId(),
						chairmanMember.getSrcBundleName(),
						chairmanMember.getSrcVenusBundleType(),
						chairmanMember.getSrcLayerId(),
						chairmanMember.getSrcVideoChannelId(),
						"VenusVideoIn",//videoBaseType,
						chairmanMember.getSrcBundleId(),
						chairmanMember.getSrcBundleName(),
						chairmanMember.getSrcBundleType(),
						chairmanMember.getSrcLayerId(),
						chairmanMember.getSrcAudioChannelId(),
						"VenusAudioIn",//String audioBaseType,
						null,//member.getDstBundleId(),
						null,//member.getDstBundleName(),
						null,//member.getDstBundleType(),
						null,//member.getDstLayerId(),
						null,//member.getDstVideoChannelId(),
						"VenusVideoOut",//String dstVideoBaseType,
						null,//member.getDstAudioChannelId(),
						null,//member.getDstBundleName(),
						null,//member.getDstBundleType(),
						null,//member.getDstLayerId(),
						null,//member.getDstAudioChannelId(),
						"VenusAudioOut",//String dstAudioBaseType,
						creatorUserId,
						g_avtpl.getId(),//Long avTplId,
						currentGear.getId(),//Long gearId,
						DstDeviceType.WEBSITE_PLAYER,
						null,//LiveType type,
						null,//Long osdId,
						null//String osdUsername);
						);
				c2m_forward.setGroup(group);
				forwards.add(c2m_forward);
				
				//建立成员到主席的转发
				CommandGroupForwardPO m2c_forward = new CommandGroupForwardPO(
						ForwardBusinessType.BASIC_COMMAND,
						ExecuteStatus.UNDONE,
						ForwardDstType.USER,
						chairmanMember.getId(),
						member.getId(),
						member.getSrcBundleId(),
						member.getSrcBundleName(),
						member.getSrcVenusBundleType(),
						member.getSrcLayerId(),
						member.getSrcVideoChannelId(),
						"VenusVideoIn",//videoBaseType,
						member.getSrcBundleId(),
						member.getSrcBundleName(),
						member.getSrcBundleType(),
						member.getSrcLayerId(),
						member.getSrcAudioChannelId(),
						"VenusAudioIn",//String audioBaseType,
						null,//chairmanMember.getDstBundleId(),
						null,//chairmanMember.getDstBundleName(),
						null,//chairmanMember.getDstBundleType(),
						null,//chairmanMember.getDstLayerId(),
						null,//chairmanMember.getDstVideoChannelId(),
						null,//String dstVideoBaseType,
						null,//chairmanMember.getDstAudioChannelId(),
						null,//chairmanMember.getDstBundleName(),
						null,//chairmanMember.getDstBundleType(),
						null,//chairmanMember.getDstLayerId(),
						null,//chairmanMember.getDstAudioChannelId(),
						null,//String dstAudioBaseType,
						creatorUserId,
						g_avtpl.getId(),//Long avTplId,
						currentGear.getId(),//Long gearId,
						DstDeviceType.WEBSITE_PLAYER,
						null,//LiveType type,
						null,//Long osdId,
						null//String osdUsername);
						);
				m2c_forward.setGroup(group);
				forwards.add(m2c_forward);
				
			}
		}
		
		group.setForwards(forwards);		
		commandGroupDao.save(group);
		
		log.info(group.getName() + " 刷新完成");		
		return group;
	}

	/**
	 * 修改会议名称<br/>
	 * <p>详细描述</p>
	 * <b>作者:</b>zsy<br/>
	 * <b>版本：</b>1.0<br/>
	 * <b>日期：</b>2020年2月16日 上午9:35:29
	 * @param userId
	 * @param groupId
	 * @param name
	 * @throws Exception
	 */
	public void modifyName(Long userId, Long groupId, String name) throws Exception{
		UserVO user = userQuery.current();		
		if(name==null || name.equals("")){
			throw new BaseException(StatusCode.FORBIDDEN, "请输入名称");
		}
		
		List<CommandGroupPO> existGroups = commandGroupDao.findByName(name);
		if(existGroups!=null && existGroups.size()>0){
			throw new CommandGroupNameAlreadyExistedException(name);
		}
		
		CommandGroupPO group = commandGroupDao.findOne(groupId);
		if(!GroupStatus.STOP.equals(group.getStatus())){
			throw new BaseException(StatusCode.FORBIDDEN, group.getName() + " 已经开始，请停止后再删除。id: " + group.getId());
		}
		if(!group.getUserId().equals(userId)){
			throw new BaseException(StatusCode.FORBIDDEN, "只有主席才能修改");
		}
		group.setName(name);
		commandGroupDao.save(group);
		
		//级联 groupUpdate
		if(!OriginType.OUTER.equals(group.getOriginType())){
			GroupType groupType = group.getType();
			if(GroupStatus.STOP.equals(group.getStatus())){
				if(GroupType.BASIC.equals(groupType)){
					GroupBO groupBO = commandCascadeUtil.updateCommand(group);
					commandCascadeService.update(groupBO);						
				}else if(GroupType.MEETING.equals(groupType)){
					GroupBO groupBO = commandCascadeUtil.updateMeeting(group);
					conferenceCascadeService.update(groupBO);			
				}
			}
		}
		operationLogService.send(user.getNickname(), "修改指挥名称", user.getNickname() + "修改指挥名称" + group.getId());
	}
	
	/**
	 * 
	 * 删除指挥<br/>
	 * <p>非创建者不能删，已经开始的指挥不能删</p>
	 * <b>作者:</b>zsy<br/>
	 * <b>版本：</b>1.0<br/>
	 * <b>日期：</b>2019年10月24日 下午1:26:09
	 * @param userId 操作人
	 * @param groupIds
	 * @throws Exception
	 */
	public void remove(Long userId, List<Long> groupIds) throws Exception{
		UserVO user = userQuery.current();
		groupIds.remove(null);
		List<CommandGroupPO> groups = commandGroupDao.findAll(groupIds);
		StringBuffer dis = new StringBuffer();
		
		//校验
		for(CommandGroupPO group : groups){
			if(!userId.equals(group.getUserId()) && !group.getType().equals(GroupType.SECRET)){
				if(!OriginType.OUTER.equals(group.getOriginType())){
					throw new BaseException(StatusCode.FORBIDDEN, "只有创建者能删除 " + group.getName());
				}
			}
			if(!GroupStatus.STOP.equals(group.getStatus())){
				throw new BaseException(StatusCode.FORBIDDEN, group.getName() + " 已经开始，请停止后再删除。id: " + group.getId());
			}
			dis.append(group.getName() + "，");
			
			GroupType type = group.getType();
			if(!OriginType.OUTER.equals(group.getOriginType())){
				if(GroupType.BASIC.equals(type)){
					GroupBO groupBO = commandCascadeUtil.deleteCommand(group);
					commandCascadeService.delete(groupBO);
				}else if(GroupType.MEETING.equals(type)){
					GroupBO groupBO = commandCascadeUtil.deleteMeeting(group);
					conferenceCascadeService.delete(groupBO);
				}
			}
		}
		
		commandGroupDao.deleteByIdIn(groupIds);
		
		log.info(dis.toString() + "被删除");
		operationLogService.send(user.getNickname(), "删除指挥", user.getNickname() + "删除指挥groupIds:" + groupIds.toString());
	}
	
	/**
	 * 
	 * 开启会议<br/>
	 * <p>详细描述</p>
	 * <b>作者:</b>zsy<br/>
	 * <b>版本：</b>1.0<br/>
	 * <b>日期：</b>2019年10月24日 上午11:03:19
	 * @param groupId
	 * @param locationIndex 指定播放器序号，序号从0起始；-1为自动选择。仅在专向会议中可以指定序号；普通会议中必须为-1。
	 * @return result 主席的播放器信息
	 * @throws Exception
	 */	
	public Object start(Long groupId, int locationIndex) throws Exception{
		return start(groupId, locationIndex, true, null, null, null);
	}
	
	/**
	 * 开启指挥/会议<br/>
	 * <p>详细描述</p>
	 * <b>作者:</b>zsy<br/>
	 * <b>版本：</b>1.0<br/>
	 * <b>日期：</b>2020年4月25日 上午10:03:50
	 * @param groupId
	 * @param locationIndex 指定播放器序号，序号从0起始；-1为自动选择。仅在专向会议中可以指定序号；普通会议中必须为-1。
	 * @param refresh 是否刷新，默认true
	 * @param enterUserIds 默认null。指定进入会议的userId列表，注意应该包含主席
	 * @param startTime 默认null。指定会议开启时间（通常在全量信息同步时）
	 * @param groupStatus 默认null。指定会议当前状态，取值为START/PAUSE，其它不支持（通常在全量信息同步时）
	 * @return
	 * @throws Exception
	 */
	public Object start(Long groupId, int locationIndex, boolean refresh, List<Long> enterUserIds, Date startTime, GroupStatus groupStatus) throws Exception{
		
		UserVO user = userQuery.current();
		JSONObject result = new JSONObject();
		JSONArray chairSplits = new JSONArray();
		
		if(groupId == null || "".equals(groupId)){
			log.warn("开始会议，会议id有误");
			return result;
		}
		
		synchronized (new StringBuffer().append("command-group-").append(groupId).toString().intern()) {
					
		CommandGroupPO group = commandGroupDao.findOne(groupId);
		GroupType groupType = group.getType();
		List<Long> userIdList = new ArrayList<Long>();
		List<CommandGroupMemberPO> members = group.getMembers();
		for(CommandGroupMemberPO member : members){
			userIdList.add(member.getUserId());
		}
		
		//本系统创建的，则鉴权，区分指挥与会议
//		if(!OriginType.OUTER.equals(group.getOriginType())){
//			if(groupType.equals(GroupType.BASIC)){
//				commandCommonServiceImpl.authorizeUsers(userIdList, group.getUserId(), BUSINESS_OPR_TYPE.ZK);
//			}else if(groupType.equals(GroupType.MEETING)){
//				commandCommonServiceImpl.authorizeUsers(userIdList, group.getUserId(), BUSINESS_OPR_TYPE.HY);
//			}
//		}
		
		//普通指挥、会议，刷新会议数据
		if(!groupType.equals(GroupType.SECRET) && refresh){
			refresh(group);
		}
		
		//后需考虑支持重复开始
		if(!group.getStatus().equals(GroupStatus.STOP)){
//			throw new BaseException(StatusCode.FORBIDDEN, group.getName() + " 已开始，请不要重复开始，id: " + group.getId());
			result.put("splits", chairSplits);
			return result;
		}
		String commandString = commandCommonUtil.generateCommandString(groupType);
		if(GroupStatus.PAUSE.equals(groupStatus)){
			group.setStatus(groupStatus);
		}else{
			group.setStatus(GroupStatus.START);
		}
		if(startTime == null) startTime = new Date();
		group.setStartTime(startTime);		
		
		//处理主席
		CommandGroupMemberPO chairman = commandCommonUtil.queryChairmanMember(members);
		chairman.setMemberStatus(MemberStatus.CONNECT);
		
		//处理其它成员		
		for(CommandGroupMemberPO member : members){
			
			if(member.isAdministrator()){
				continue;
			}
			
			member.setMemberStatus(MemberStatus.CONNECTING);
			
			JSONObject message = new JSONObject();
			message.put("fromUserId", chairman.getUserId());
			message.put("fromUserName", chairman.getUserName());
			message.put("businessId", group.getId().toString());
			if(group.getType().equals(GroupType.BASIC) || group.getType().equals(GroupType.MEETING)){
				if(!autoEnter){
					String businessType = null;
					if(GroupType.MEETING.equals(group.getType())){
						businessType = "meetingStart";//自动接听使用meetingStartNow
					}else{
						businessType = "commandStart";//自动接听使用commandStartNow
					}
					message.put("businessType", businessType);
					message.put("businessInfo", "接受到 " + group.getName() + " 邀请，主席：" + chairman.getUserName() + "，是否进入？");
					
					//发送消息
					WebsocketMessageVO ws = websocketMessageService.send(member.getUserId(), message.toJSONString(), WebsocketMessageType.COMMAND, chairman.getUserId(), chairman.getUserName());
					member.setMessageId(ws.getId());						
				}
			}else if(group.getType().equals(GroupType.SECRET)){
				message.put("businessType", "secretStart");
				message.put("businessInfo", chairman.getUserName() + " 邀请你专向" + commandString);
				
				//发送消息
				WebsocketMessageVO ws = websocketMessageService.send(member.getUserId(), message.toJSONString(), WebsocketMessageType.COMMAND, chairman.getUserId(), chairman.getUserName());
				member.setMessageId(ws.getId());
			}
		}
		
		//自动接听则所有在线的人接听，否则只有主席接听；专向指挥只有主席接听
		String userIdListStr = StringUtils.join(userIdList.toArray(), ",");
		List<UserBO> commandUserBos = resourceService.queryUserListByIds(userIdListStr, TerminalType.QT_ZK);
		List<CommandGroupMemberPO> acceptMembers = null;
		if(enterUserIds != null){
			//如果指定了enterUserIds（通常是级联的全量信息同步），则使用
			acceptMembers = new ArrayListWrapper<CommandGroupMemberPO>().getList();
			for(CommandGroupMemberPO member : members){
				if(enterUserIds.contains(member.getUserId())){
					acceptMembers.add(member);
				}else{
					//不进入的，置为DISCONNECT
					member.setMemberStatus(MemberStatus.DISCONNECT);
				}
			}
		}
		if(autoEnter && !group.getType().equals(GroupType.SECRET)){
//			acceptMembers = members;
			//只给在线的成员自动上线
			acceptMembers = new ArrayListWrapper<CommandGroupMemberPO>().add(chairman).getList();
			for(CommandGroupMemberPO member : members){
				if(member.isAdministrator()) continue;
//				if(OriginType.OUTER.equals(member.getOriginType())){member.setMemberStatus(MemberStatus.DISCONNECT);continue;}
				UserBO commandUserBo = queryUtil.queryUserById(commandUserBos, member.getUserId());
				if(commandUserBo.isLogined()){
					acceptMembers.add(member);
				}else{
					//没在线的，置为DISCONNECT。不用按照“拒绝”处理
					member.setMemberStatus(MemberStatus.DISCONNECT);
				}
			}
		}else{
			acceptMembers = new ArrayList<CommandGroupMemberPO>();
			acceptMembers.add(chairman);			
		}
		
		commandGroupDao.save(group);
		
		membersResponse(group, acceptMembers, null);
		
		if(!OriginType.OUTER.equals(group.getOriginType())){
			if(GroupType.BASIC.equals(groupType)){
				GroupBO groupBO = commandCascadeUtil.startCommand(group);
				commandCascadeService.start(groupBO);
			}else if(GroupType.MEETING.equals(groupType)){
				GroupBO groupBO = commandCascadeUtil.startMeeting(group);
				conferenceCascadeService.start(groupBO);
			}
		}
		
		/*boolean hasOuterMember = false;
		for(CommandGroupMemberPO member : members){
			if(OriginType.OUTER.equals(member.getOriginType())){
				hasOuterMember = true;
				break;
			}
		}
		if(hasOuterMember){
			if(GroupType.BASIC.equals(groupType)){
				Thread.sleep(300);//延时确保其它节点开会已完成
				GroupBO groupBO = commandCascadeUtil.joinCommand(group, null, acceptMembers);//需要把主席去掉吗？
				commandCascadeService.join(groupBO);
			}else if(GroupType.MEETING.equals(groupType)){
				Thread.sleep(300);//延时确保其它节点开会已完成
				GroupBO groupBO = commandCascadeUtil.joinMeeting(group, null, acceptMembers);//需要把主席去掉吗？
				conferenceCascadeService.join(groupBO);
			}
		}*/
		
		result.put("splits", chairSplits);
		operationLogService.send(user.getNickname(), "开启指挥", user.getNickname() + "开启指挥groupId:" + groupId);
		return result;

		}
	}	
	
	/**
	 * 
	 * 停止会议<br/>
	 * <p>详细描述</p>
	 * <b>作者:</b>zsy<br/>
	 * <b>版本：</b>1.0<br/>
	 * <b>日期：</b>2019年10月24日 上午11:04:05
	 * @param userId 操作人
	 * @param groupId
	 * @param stopMode 0为正常停止，1为拒绝专向会议导致的停止，该参数用来在专向会议中发送不同的消息
	 * @return chairSplits 主席需要关闭的播放器序号
	 * @throws Exception
	 */
	public JSONArray stop(Long userId, Long groupId, int stopMode) throws Exception{
		
		UserVO user = userQuery.current();
		
		//通常返回主席的屏幕；在专向会议中，由对方成员停止或拒绝，返回对方成员的屏幕
		JSONArray returnSplits = new JSONArray();
		JSONArray chairSplits = new JSONArray();
		JSONArray secretSplits = new JSONArray();
		
		if(groupId==null || groupId.equals("")){
			log.info("停会操作，会议id有误");
			return chairSplits;
//			throw new BaseException(StatusCode.FORBIDDEN, "停会操作，会议id有误");
		}
		
		synchronized (new StringBuffer().append("command-group-").append(groupId).toString().intern()) {
					
		CommandGroupPO group = commandGroupDao.findOne(groupId);
		if(group == null){
//			throw new BaseException(StatusCode.FORBIDDEN, "会议不存在，id: " + groupId);
			log.info("进行停止操作的会议不存在，id：" + groupId);
			return new JSONArray();
		}
		GroupType groupType = group.getType();
		String commandString = commandCommonUtil.generateCommandString(group.getType());
		if(group.getStatus().equals(GroupStatus.REMIND)){
			if(group.getType().equals(GroupType.BASIC)){
				throw new BaseException(StatusCode.FORBIDDEN, "请先关闭" + commandString + "提醒");
			}else if(group.getType().equals(GroupType.MEETING)){
				throw new BaseException(StatusCode.FORBIDDEN, "请先关闭" + commandString + "提醒");
			}
		}
		
		group.setStatus(GroupStatus.STOP);
		group.setSpeakType(GroupSpeakType.CHAIRMAN);
		group.setStartTime(null);
		Date endTime = new Date();
		group.setEndTime(endTime);
		List<CommandGroupMemberPO> members = group.getMembers();
		List<CommandGroupForwardPO> forwards = group.getForwards();
		List<CommandGroupForwardDemandPO> demands = group.getForwardDemands();
		List<CommandGroupMemberPO> connectMembers = new ArrayList<CommandGroupMemberPO>();
		List<CommandGroupUserPlayerPO> needClosePlayers = new ArrayList<CommandGroupUserPlayerPO>();
		List<CommandGroupUserPlayerPO> playFilePlayers = new ArrayList<CommandGroupUserPlayerPO>();
		List<Long> consumeIds = new ArrayList<Long>();
		List<MessageSendCacheBO> messageCaches = new ArrayList<MessageSendCacheBO>();
		CommandGroupMemberPO chairman = commandCommonUtil.queryChairmanMember(members);
		
		//专向会议中，先获取主席和专向成员的分屏
		CommandGroupMemberPO secretMember = null;
		if(group.getType().equals(GroupType.SECRET)){
			for(CommandGroupMemberPO member : members){
				if(!member.isAdministrator()){
					secretMember = member;
					break;
				}
			}
			
			if(!OriginType.OUTER.equals(chairman.getOriginType())){
				for(CommandGroupUserPlayerPO player : chairman.getPlayers()){
					JSONObject split = new JSONObject();
					split.put("serial", player.getLocationIndex());
					chairSplits.add(split);
				}
				returnSplits = chairSplits;
			}
			
			if(!OriginType.OUTER.equals(secretMember.getOriginType())){
				for(CommandGroupUserPlayerPO player : secretMember.getPlayers()){
					JSONObject split = new JSONObject();
					split.put("serial", player.getLocationIndex());
					secretSplits.add(split);
				}
			}
		}
		
		//处理所有成员，包括主席
		for(CommandGroupMemberPO member : members){
			if(member.getMemberStatus().equals(MemberStatus.CONNECTING)){
				//CONNECTING的，消费消息
				consumeIds.add(member.getMessageId());
				member.setMessageId(null);
			}else if(member.getMemberStatus().equals(MemberStatus.CONNECT)){
				connectMembers.add(member);
			}
			
			//处理协同会议状态
			if(member.getCooperateStatus().equals(MemberStatus.CONNECTING)){
				consumeIds.add(member.getMessageCoId());
				member.setMessageCoId(null);
			}			
			
			if(OriginType.OUTER.equals(member.getOriginType())){
				//状态重置
				member.setMemberStatus(MemberStatus.DISCONNECT);
				member.setCooperateStatus(MemberStatus.DISCONNECT);
				member.setSilenceToHigher(false);
				member.setSilenceToLower(false);
				continue;
			}
			
			//释放播放器，同时统计屏幕序号用于返回split
			JSONArray splits = new JSONArray();
			List<CommandGroupUserPlayerPO> players = member.getPlayers();
			for(CommandGroupUserPlayerPO player : players){
				if(player.playingFile()){
					playFilePlayers.add(player);
				}
				player.setFree();
				JSONObject split = new JSONObject();
				split.put("serial", player.getLocationIndex());
				splits.add(split);
			}
			needClosePlayers.addAll(players);//关闭所有的播放器，包括未呼叫的
			member.getPlayers().removeAll(players);
			if(member.isAdministrator()){
				returnSplits = splits;
			}
			
			//普通指挥/会议，给其它 会议中的 成员发通知
			if(!member.isAdministrator() && group.getType().equals(GroupType.BASIC)
					|| !member.isAdministrator() && group.getType().equals(GroupType.MEETING)){
				if(member.getMemberStatus().equals(MemberStatus.CONNECT)){
					JSONObject message = new JSONObject();
					message.put("businessType", "commandStop");
					message.put("fromUserId", chairman.getUserId());
					message.put("fromUserName", chairman.getUserName());
					message.put("businessId", group.getId().toString());
					message.put("businessInfo", group.getName() + " 停止了");
					message.put("splits", splits);
					
					//发送消息
					messageCaches.add(new MessageSendCacheBO(member.getUserId(), message.toJSONString(), WebsocketMessageType.COMMAND, chairman.getUserId(), chairman.getUserName()));
				}
			}
			
			//专向会议的通知
			if(group.getType().equals(GroupType.SECRET)){
				
				JSONObject message = new JSONObject();
				boolean send = false;
				if(member.isAdministrator()){
					//当前正在处理主席
					//对方成员拒绝导致的停止
					if(stopMode==1 && userId.equals(secretMember.getUserId())){
						message.put("businessType", "secretRefuse");
						message.put("fromUserId", secretMember.getUserId());
						message.put("fromUserName", secretMember.getUserName());
//						message.put("businessId", group.getId().toString());
						message.put("businessInfo", secretMember.getUserName() + " 拒绝与你专向" + commandString);
						if(splits.size() > 0){
							message.put("serial", splits.getJSONObject(0).getInteger("serial"));
						}
						//返回操作人的屏幕
						returnSplits = secretSplits;
						send = true;
					}
					//对方成员停止
					if(stopMode==0 && userId.equals(secretMember.getUserId())){
						message.put("businessType", "secretStop");
						message.put("fromUserId", secretMember.getUserId());
						message.put("fromUserName", secretMember.getUserName());
//						message.put("businessId", group.getId().toString());
						message.put("businessInfo", secretMember.getUserName() + " 停止了专向" + commandString);
						if(splits.size() > 0){
							message.put("serial", splits.getJSONObject(0).getInteger("serial"));
						}
						//返回操作人的屏幕
						returnSplits = secretSplits;
						send = true;
					}
				}else{
					//当前正在处理对方成员
					//主席停止，给对方发通知
					if(stopMode==0 && userId.equals(chairman.getUserId())){
						message.put("businessType", "secretStop");
						message.put("fromUserId", chairman.getUserId());
						message.put("fromUserName", chairman.getUserName());
//						message.put("businessId", group.getId().toString());
						message.put("businessInfo", chairman.getUserName() + " 停止了专向" + commandString);
						if(splits.size() > 0){
							message.put("serial", splits.getJSONObject(0).getInteger("serial"));
						}
						//返回操作人的屏幕
						returnSplits = chairSplits;
						send = true;
					}
				}
				//发送消息
				if(send){
					messageCaches.add(new MessageSendCacheBO(member.getUserId(), message.toJSONString(), WebsocketMessageType.COMMAND, chairman.getUserId(), chairman.getUserName()));
				}
			}
			
			//状态重置（外部系统用户的状态重置写在上头）
			member.setMemberStatus(MemberStatus.DISCONNECT);
			member.setCooperateStatus(MemberStatus.DISCONNECT);
			member.setSilenceToHigher(false);
			member.setSilenceToLower(false);
		}
		
		//删除协同会议的forwardPO
		List<CommandGroupForwardPO> cooperateForwards = new ArrayList<CommandGroupForwardPO>();
		for(CommandGroupForwardPO forward : forwards){
			if(forward.getForwardBusinessType().equals(ForwardBusinessType.COOPERATE_COMMAND)){
				cooperateForwards.add(forward);
			}
		}
		forwards.removeAll(cooperateForwards);
		
		//清楚剩余普通转发的目的，把状态置为UNDONE
		for(CommandGroupForwardPO forward : forwards){
			forward.clearDst();
			forward.setExecuteStatus(ExecuteStatus.UNDONE);
		}
		
		//停止会议转发点播
		List<CommandGroupForwardDemandPO> needDelDemands = new ArrayList<CommandGroupForwardDemandPO>();
		for(CommandGroupForwardDemandPO demand : demands){
			if(demand.getExecuteStatus().equals(ForwardDemandStatus.DONE)){
				needDelDemands.add(demand);
			}
		}
		demands.clear();//全部清除
		
		commandGroupUserPlayerDao.save(needClosePlayers);
		commandGroupDao.save(group);
		
		//disconnect，考虑怎么给不需要挂断的编码器计数
		CommandGroupAvtplGearsPO currentGear = commandCommonUtil.queryCurrentGear(group);
		CodecParamBO codec = new CodecParamBO().set(group.getAvtpl(), currentGear);
		LogicBO logic = closeBundle(connectMembers, needDelDemands, needClosePlayers, codec, chairman.getUserNum());
		LogicBO logicCastDevice = commandCastServiceImpl.closeBundleCastDevice(playFilePlayers, null, null, needClosePlayers, codec, group.getUserId());
		logic.merge(logicCastDevice);
		LogicBO logicStopRecord = commandRecordServiceImpl.stop(null, groupId, false);
		logic.merge(logicStopRecord);
		executeBusiness.execute(logic, group.getName() + " 会议停止");
		
		//级联
		if(!OriginType.OUTER.equals(group.getOriginType())){
			if(GroupType.BASIC.equals(groupType)){
				GroupBO groupBO = commandCascadeUtil.stopCommand(group);
				commandCascadeService.stop(groupBO);
			}else if(GroupType.MEETING.equals(groupType)){
				GroupBO groupBO = commandCascadeUtil.stopMeeting(group);
				conferenceCascadeService.stop(groupBO);
			}
		}
		
		//发消息
		for(MessageSendCacheBO cache : messageCaches){
			WebsocketMessageVO ws = websocketMessageService.send(cache.getUserId(), cache.getMessage(), cache.getType(), cache.getFromUserId(), cache.getFromUsername());
			consumeIds.add(ws.getId());
		}
		websocketMessageService.consumeAll(consumeIds);
		
		operationLogService.send(user.getNickname(), "停止指挥", user.getNickname() + "停止指挥groupId:" + groupId);
		return returnSplits;
		}
	}
	
	/**
	 * 成员进入会议（批量）<br/>
	 * <p>非主席的成员会按照“接听”处理，主席不处理</p>
	 * <b>作者:</b>zsy<br/>
	 * <b>版本：</b>1.0<br/>
	 * <b>日期：</b>2019年10月24日 上午11:05:16
	 * @param userId 操作人的userId
	 * @param groupIds
	 * @return groupInfos 会议信息的数组
	 * @throws Exception 有会议无法进入时，会抛错
	 */
	public JSONArray enter(Long userId, List<Long> groupIds) throws Exception{
		UserVO user = userQuery.current();
		JSONArray groupInfos = new JSONArray();
		
		List<CommandGroupPO> groups = commandGroupDao.findAll(groupIds);
		
		//校验是否都在进行中，否则抛错
		for(CommandGroupPO group : groups){
			if(userId.equals(group.getUserId())){
				//主席不抛错
			}else if(group.getStatus().equals(GroupStatus.STOP)){
				throw new BaseException(StatusCode.FORBIDDEN, group.getName() + " 已停止，无法进入，id: " + group.getId());
			}
		}
		
		for(Long groupId : groupIds){
			
			if(groupId==null || groupId.equals("")){
				log.info("进会操作，会议id有误，groupIds: " + groupIds.toString());
				continue;
			}
			
			synchronized (new StringBuffer().append("command-group-").append(groupId).toString().intern()) {
			
				//判断是否进入其它会议，建议在commandGroupDao写一个新方法，不查自己建的会
//				List<CommandGroupPO> commands = commandGroupDao.findEnteredGroupByMemberUserId(userId);
//				if(commands!=null && commands.size()>0){
//					for(CommandGroupPO command : commands){
//						if(!command.getUserId().equals(userId)){
//							//已经进入其它会议
//						}
//					}
//				}
			CommandGroupPO group = commandGroupDao.findOne(groupId);
			GroupType groupType = group.getType();
			List<CommandGroupMemberPO> members = group.getMembers();
			
			//主席member
			CommandGroupMemberPO chairmanMember = commandCommonUtil.queryChairmanMember(members);
			
			//该用户的member
			CommandGroupMemberPO thisMember = commandCommonUtil.queryMemberByUserId(members, userId);
			if(thisMember == null) continue;
			
			//不是主席，进行“接听”处理
			if(!thisMember.isAdministrator()){
				List<CommandGroupMemberPO> acceptMembers = new ArrayList<CommandGroupMemberPO>();
				acceptMembers.add(thisMember);
				//如果该成员状态为CONNECT则不需处理，否则按接听处理
				if(thisMember.getMemberStatus().equals(MemberStatus.CONNECT)){
					
				}else{
					
					//级联，如果该“进入”成员是本系统成员，则通知外部系统
					if(!OriginType.OUTER.equals(thisMember.getOriginType())){
						if(GroupType.BASIC.equals(groupType)){
							GroupBO groupBO = commandCascadeUtil.joinCommand(group, null, acceptMembers);
							commandCascadeService.join(groupBO);
						}else if(GroupType.MEETING.equals(groupType)){
							GroupBO groupBO = commandCascadeUtil.joinMeeting(group, null, acceptMembers);
							conferenceCascadeService.join(groupBO);
						}
					}
					
//					chosePlayersForMembers(group, acceptMembers);//后选播放器:放进membersResponse
					membersResponse(group, acceptMembers, null);
				}
			}
			
			JSONArray splits = new JSONArray();
			for(CommandGroupUserPlayerPO player : thisMember.getPlayers()){
				
				if(group.getType().equals(GroupType.BASIC) || group.getType().equals(GroupType.MEETING)){
					JSONObject split = new JSONObject();
					split.put("serial", player.getLocationIndex());
					split.put("bundleId", player.getBundleId());
					split.put("bundleNo", player.getCode());
					split.put("businessType", player.getPlayerBusinessType().getCode());
					split.put("businessId", group.getId().toString());
					split.put("businessInfo", player.getBusinessName());
					split.put("status", group.getStatus().getCode());
					splits.add(split);
				}else if(group.getType().equals(GroupType.SECRET)){
					JSONObject split = new JSONObject();
					split.put("serial", player.getLocationIndex());
					split.put("bundleId", player.getBundleId());
					split.put("bundleNo", player.getCode());
					split.put("businessType", player.getPlayerBusinessType().getCode());
					split.put("businessId", group.getId().toString());
					split.put("businessInfo", player.getBusinessName());
					splits.add(split);
				}
			}
			
			JSONObject message = new JSONObject();
			message.put("id", group.getId());
			message.put("name", group.getName());
			message.put("status", group.getStatus().getCode());
			message.put("commander", chairmanMember.getUserId());
			message.put("creator", chairmanMember.getUserId());
			message.put("splits", splits);
			//已经启动的会议，添加作战时间
			GroupStatus groupStatus = group.getStatus();
			if(!groupStatus.equals(GroupStatus.STOP)){
				Date fightTime = commandFightTimeServiceImpl.calculateCurrentFightTime(group);
				if(fightTime != null){
					message.put("fightTime", DateUtil.format(fightTime, DateUtil.dateTimePattern));
				}
			}
			
			groupInfos.add(message);
			
			}
		}
		operationLogService.send(user.getNickname(), "进入指挥", user.getNickname() + "进入指挥groupIds:" + groupIds.toString());
		return groupInfos;
	}
	
	/**
	 * 拒绝加入<br/>
	 * <p>详细描述</p>
	 * <b>作者:</b>zsy<br/>
	 * <b>版本：</b>1.0<br/>
	 * <b>日期：</b>2019年10月30日 下午4:53:14
	 * @param userId 拒绝者
	 * @param groupId
	 * @throws Exception
	 */
	public void refuse(Long userId, Long groupId) throws Exception{
		UserVO user = userQuery.current();
		synchronized (new StringBuffer().append("command-group-").append(groupId).toString().intern()) {
			
		CommandGroupPO group = commandGroupDao.findOne(groupId);
		if(group == null){
			//会议已经被删除
			return;
		}
		
		String commandString = commandCommonUtil.generateCommandString(group.getType());
		
		List<CommandGroupMemberPO> refuseMembers = new ArrayList<CommandGroupMemberPO>();
		for(CommandGroupMemberPO member : group.getMembers()){
			if(member.getUserId().equals(userId)){
				if(member.getMemberStatus().equals(MemberStatus.CONNECT)){
					if(group.getType().equals(GroupType.BASIC)){
						throw new BaseException(StatusCode.FORBIDDEN, "已进入该" + commandString + "会议，可使用“退出" + commandString + "会议”功能，id: " + group.getId());
					}else if(group.getType().equals(GroupType.MEETING)){
						throw new BaseException(StatusCode.FORBIDDEN, "已进入该" + commandString + "会议，可使用“退出" + commandString + "会议”功能，id: " + group.getId());
					}
				}
				refuseMembers.add(member);
				break;
			}
		}
		membersResponse(group, null, refuseMembers);
		
		}
		operationLogService.send(user.getNickname(), "拒绝加入指挥", user.getNickname() + "拒绝加入指挥groupId" + groupId);
	}
	
	public JSONArray pause(Long groupId) throws Exception{
		UserVO user = userQuery.current();
		
		if(groupId==null || groupId.equals("")){
			log.info("暂停会议，会议id有误");
			return new JSONArray();
		}
		
		synchronized (new StringBuffer().append("command-group-").append(groupId).toString().intern()) {
			
			CommandGroupPO group = commandGroupDao.findOne(groupId);
			if(group.getStatus().equals(GroupStatus.STOP)){
				if(!OriginType.OUTER.equals(group.getOriginType())){
					throw new BaseException(StatusCode.FORBIDDEN, group.getName() + " 已停止，无法操作，id: " + group.getId());
				}else{
					return new JSONArray();
				}
			}
			String commandString = commandCommonUtil.generateCommandString(group.getType());
			if(group.getStatus().equals(GroupStatus.REMIND)){
				if(group.getType().equals(GroupType.BASIC)){
					throw new BaseException(StatusCode.FORBIDDEN, "请先关闭" + commandString + "提醒");
				}else if(group.getType().equals(GroupType.MEETING)){
					throw new BaseException(StatusCode.FORBIDDEN, "请先关闭" + commandString + "提醒");
				}
			}
			group.setStatus(GroupStatus.PAUSE);
			
			JSONArray chairSplits = new JSONArray();
			
			Set<CommandGroupForwardPO> needDelForwards = new HashSet<CommandGroupForwardPO>();
			List<CommandGroupForwardPO> forwards = group.getForwards();
			for(CommandGroupForwardPO forward : forwards){
				if(forward.getExecuteStatus().equals(ExecuteStatus.DONE)){
					forward.setExecuteStatus(ExecuteStatus.UNDONE);
					needDelForwards.add(forward);
				}
			}
			
			commandGroupDao.save(group);
			
			//级联
			GroupType groupType = group.getType();
			if(!OriginType.OUTER.equals(group.getOriginType())){
				if(GroupType.BASIC.equals(groupType)){
					GroupBO groupBO = commandCascadeUtil.pauseCommand(group);
					commandCascadeService.pause(groupBO);
				}else if(GroupType.MEETING.equals(groupType)){
					GroupBO groupBO = commandCascadeUtil.pauseMeeting(group);
					conferenceCascadeService.pause(groupBO);
				}
			}
			
			//给成员推送message
			List<Long> consumeIds = new ArrayList<Long>();
			List<CommandGroupMemberPO> members = group.getMembers();
			CommandGroupMemberPO chairman = commandCommonUtil.queryChairmanMember(members);
			for(CommandGroupMemberPO member : members){
				JSONArray splits = new JSONArray();
				if(member.getMemberStatus().equals(MemberStatus.CONNECT)){
					List<CommandGroupUserPlayerPO> players = member.getPlayers();
					for(CommandGroupUserPlayerPO player : players){
						JSONObject split = new JSONObject();
						split.put("serial", player.getLocationIndex());
						splits.add(split);
					}
					if(member.isAdministrator()){
						chairSplits = splits;
					}else{
						JSONObject message = new JSONObject();
						message.put("businessType", "commandPause");
						message.put("businessInfo", group.getName() + " 暂停");
						message.put("businessId", group.getId().toString());
						message.put("splits", splits);
						WebsocketMessageVO ws = websocketMessageService.send(member.getUserId(), message.toJSONString(), WebsocketMessageType.COMMAND, chairman.getUserId(), chairman.getUserName());
						consumeIds.add(ws.getId());
					}
				}
			}
			websocketMessageService.consumeAll(consumeIds);
			
			//生成forwardDel的logic
			CommandGroupAvtplGearsPO currentGear = commandCommonUtil.queryCurrentGear(group);
			CodecParamBO codec = new CodecParamBO().set(group.getAvtpl(), currentGear);
			LogicBO logic = openBundle(null, null, null, null, needDelForwards, codec, chairman.getUserNum());
			LogicBO logicCastDevice = commandCastServiceImpl.openBundleCastDevice(null, null, null, needDelForwards, null, null, codec, group.getUserId());
			logic.merge(logicCastDevice);
			
			//录制更新
			LogicBO logicRecord = commandRecordServiceImpl.update(group.getUserId(), group, 1, false);
			logic.merge(logicRecord);
			
			ExecuteBusinessReturnBO returnBO = executeBusiness.execute(logic, group.getName() + " " + commandString + "暂停");
			commandRecordServiceImpl.saveStoreInfo(returnBO, group.getId());
			operationLogService.send(user.getNickname(), "暂停指挥", user.getNickname() + "暂停指挥groupId:" + groupId);
			return chairSplits;
			
		}
	}
	
	public JSONArray pauseRecover(Long groupId) throws Exception{
		UserVO user = userQuery.current();
		
		if(groupId==null || groupId.equals("")){
			log.info("会议从暂停中恢复，会议id有误");
			return new JSONArray();
		}
		
		synchronized (new StringBuffer().append("command-group-").append(groupId).toString().intern()) {
			
			CommandGroupPO group = commandGroupDao.findOne(groupId);
			if(group.getStatus().equals(GroupStatus.STOP)){
				if(!OriginType.OUTER.equals(group.getOriginType())){
					throw new BaseException(StatusCode.FORBIDDEN, group.getName() + " 已停止，无法操作，id: " + group.getId());
				}else{
					return new JSONArray();
				}
			}
			group.setStatus(GroupStatus.START);
			commandGroupDao.save(group);//需要吗？
			
			//级联
			GroupType groupType = group.getType();
			if(!OriginType.OUTER.equals(group.getOriginType())){
				if(GroupType.BASIC.equals(groupType)){
					GroupBO groupBO = commandCascadeUtil.resumeCommand(group);
					commandCascadeService.resume(groupBO);
				}else if(GroupType.MEETING.equals(groupType)){
					GroupBO groupBO = commandCascadeUtil.resumeMeeting(group);
					conferenceCascadeService.resume(groupBO);
				}
			}
			
			//恢复会中的转发
			startGroupForwards(group, true, true);
			
			//给成员推送message
			JSONArray chairSplits = new JSONArray();
			List<Long> consumeIds = new ArrayList<Long>();
			List<CommandGroupMemberPO> members = group.getMembers();
			CommandGroupMemberPO chairman = commandCommonUtil.queryChairmanMember(members);
			for(CommandGroupMemberPO member : members){
				JSONArray splits = new JSONArray();
				if(member.getMemberStatus().equals(MemberStatus.CONNECT)){
					List<CommandGroupUserPlayerPO> players = member.getPlayers();
					for(CommandGroupUserPlayerPO player : players){
						JSONObject split = new JSONObject();
						split.put("serial", player.getLocationIndex());
						splits.add(split);
					}
					if(member.isAdministrator()){
						chairSplits = splits;
					}else{
						JSONObject message = new JSONObject();
						message.put("businessType", "commandPause");
						message.put("businessInfo", group.getName() + " 暂停恢复");
						message.put("businessId", group.getId().toString());
						message.put("splits", splits);
						WebsocketMessageVO ws = websocketMessageService.send(member.getUserId(), message.toJSONString(), WebsocketMessageType.COMMAND, chairman.getUserId(), chairman.getUserName());
						consumeIds.add(ws.getId());
					}
				}
			}
			websocketMessageService.consumeAll(consumeIds);
			
			log.info(group.getName() + " 会议取消暂停");
			operationLogService.send(user.getNickname(), "恢复指挥", user.getNickname() + "恢复指挥" + groupId);
			return chairSplits;
		}
	}
	
	@Deprecated
	public Object addMembers(Long groupId, List<Long> userIdList) throws Exception{
		UserVO self = userQuery.current();
		JSONArray chairSplits = new JSONArray();
		
		synchronized (new StringBuffer().append("command-group-").append(groupId).toString().intern()) {
					
			CommandGroupPO group = commandGroupDao.findOne(groupId);
			
			//用户管理层的批量接口，根据userIds查询List<UserBO>，由于缺少folderId，所以额外查询queryAllFolders，给UserBO中的folderId赋值
			String userIdListStr = StringUtils.join(userIdList.toArray(), ",");
			List<UserBO> commandUserBos = resourceService.queryUserListByIds(userIdListStr, TerminalType.QT_ZK);
			List<FolderPO> allFolders = resourceService.queryAllFolders();
//			List<UserBO> allUsers = resourceService.queryUserresByUserId(group.getUserId());
//			List<UserBO> commandUserBos = new ArrayList<UserBO>();
//			for(UserBO user : allUsers){
//				if(userIdList.contains(user.getId())){
//					commandUserBos.add(user);
//				}
//			}
			
			//从List<UserBO>取出bundleId列表，注意判空；给UserBO中的folderId赋值
			List<String> bundleIds = new ArrayList<String>();
			for(UserBO user : commandUserBos){
				String encoderId = commonQueryUtil.queryExternalOrLocalEncoderIdFromUserBO(user);
				if(encoderId != null){
					bundleIds.add(encoderId);
				}
				for(FolderPO folder : allFolders){
					if(folder.getUuid().equals(user.getFolderUuid())){
						user.setFolderId(folder.getId());
						break;
					}
				}
			}
			
			//从bundleId列表查询所有的bundlePO
			List<BundlePO> srcBundleEntities = resourceBundleDao.findByBundleIds(bundleIds);
			
			//从bundleId列表查询所有的视频编码1通道
			List<ChannelSchemeDTO> videoEncode1Channels = resourceChannelDao.findByBundleIdsAndChannelId(bundleIds, ChannelType.VIDEOENCODE1.getChannelId());
			//通过视频编码通道来校验编码器是否可用
			if(videoEncode1Channels.size() < commandUserBos.size()){
				for(UserBO user : commandUserBos){
					String encoderId = commonQueryUtil.queryExternalOrLocalEncoderIdFromUserBO(user);
					boolean hasChannel = false;
					for(ChannelSchemeDTO channel : videoEncode1Channels){
						if(channel.getBundleId().equals(encoderId)){
							hasChannel = true;
							break;
						}
					}
					if(!hasChannel){
						throw new UserHasNoAvailableEncoderException(user.getName());
					}
				}
			}
			
			
			//从bundleId列表查询所有的音频编码1通道
			List<ChannelSchemeDTO> audioEncode1Channels = resourceChannelDao.findByBundleIdsAndChannelId(bundleIds, ChannelType.AUDIOENCODE1.getChannelId());
			
			List<CommandGroupMemberPO> members = group.getMembers();
//			Set<CommandGroupForwardPO> forwards = group.getForwards();
			CommandGroupMemberPO chairmanMember = commandCommonUtil.queryChairmanMember(members);
			Long creatorUserId = chairmanMember.getUserId();
			CommandGroupAvtplPO g_avtpl = group.getAvtpl();
			CommandGroupAvtplGearsPO currentGear = commandCommonUtil.queryCurrentGear(group);
			
			List<CommandGroupMemberPO> newMembers = new ArrayList<CommandGroupMemberPO>();
			List<CommandGroupForwardPO> newForwards = new ArrayList<CommandGroupForwardPO>();
			
//			BasicRolePO chairmanRole = basicRoleDao.findByName("主席");
//			BasicRolePO memberRole = basicRoleDao.findByName("会议员");
			for(UserBO user : commandUserBos){
				CommandGroupMemberPO memberPO = new CommandGroupMemberPO();
				//关联会议员角色
//				memberPO.setRoleId(memberRole.getId());
//				memberPO.setRoleName(memberRole.getName());
				
				memberPO.setUserId(user.getId());
				memberPO.setUserName(user.getName());
				memberPO.setUserNum(user.getUserNo());
				memberPO.setFolderId(user.getFolderId());
				
				//遍历bundle
				String encoderId = commonQueryUtil.queryExternalOrLocalEncoderIdFromUserBO(user);
				for(BundlePO bundle : srcBundleEntities){
					if(bundle.getBundleId().equals(encoderId)){
						memberPO.setSrcBundleId(bundle.getBundleId());
						memberPO.setSrcBundleName(bundle.getBundleName());
						memberPO.setSrcBundleType(bundle.getDeviceModel());
						memberPO.setSrcVenusBundleType(bundle.getBundleType());
						memberPO.setSrcLayerId(bundle.getAccessNodeUid());
						break;
					}
				}
				
				//遍历视频通道
				for(ChannelSchemeDTO videoChannel : videoEncode1Channels){
					if(videoChannel.getBundleId().equals(encoderId)){
						memberPO.setSrcVideoChannelId(videoChannel.getChannelId());
						break;
					}
				}			
				
				//遍历音频通道
				for(ChannelSchemeDTO audioChannel : audioEncode1Channels){
					if(audioChannel.getBundleId().equals(encoderId)){
						memberPO.setSrcAudioChannelId(audioChannel.getChannelId());
						break;
					}
				}			
				
				memberPO.setGroup(group);
				newMembers.add(memberPO);
			}
			
			//保存以获得member的id
			group.getMembers().addAll(newMembers);
			commandGroupMemberDao.save(newMembers);
			commandGroupDao.save(group);
			
			List<Long> newMemberIds = new ArrayList<Long>();
			for(CommandGroupMemberPO member : newMembers){
				newMemberIds.add(member.getId());
				if(member.isAdministrator()){
					//建立所有成员到主席的转发（在下边）
					
				}else{
					//建立主席到成员的转发
					CommandGroupForwardPO c2m_forward = new CommandGroupForwardPO(
							ForwardBusinessType.BASIC_COMMAND,
							ExecuteStatus.UNDONE,
							ForwardDstType.USER,
							member.getId(),
							chairmanMember.getId(),
							chairmanMember.getSrcBundleId(),
							chairmanMember.getSrcBundleName(),
							chairmanMember.getSrcVenusBundleType(),
							chairmanMember.getSrcLayerId(),
							chairmanMember.getSrcVideoChannelId(),
							"VenusVideoIn",//videoBaseType,
							chairmanMember.getSrcBundleId(),
							chairmanMember.getSrcBundleName(),
							chairmanMember.getSrcBundleType(),
							chairmanMember.getSrcLayerId(),
							chairmanMember.getSrcAudioChannelId(),
							"VenusAudioIn",//String audioBaseType,
							null,//member.getDstBundleId(),
							null,//member.getDstBundleName(),
							null,//member.getDstBundleType(),
							null,//member.getDstLayerId(),
							null,//member.getDstVideoChannelId(),
							"VenusVideoOut",//String dstVideoBaseType,
							null,//member.getDstAudioChannelId(),
							null,//member.getDstBundleName(),
							null,//member.getDstBundleType(),
							null,//member.getDstLayerId(),
							null,//member.getDstAudioChannelId(),
							"VenusAudioOut",//String dstAudioBaseType,
							creatorUserId,
							g_avtpl.getId(),//Long avTplId,
							currentGear.getId(),//Long gearId,
							DstDeviceType.WEBSITE_PLAYER,
							null,//LiveType type,
							null,//Long osdId,
							null//String osdUsername);
							);
					c2m_forward.setGroup(group);
					newForwards.add(c2m_forward);
					
					//建立成员到主席的转发
					CommandGroupForwardPO m2c_forward = new CommandGroupForwardPO(
							ForwardBusinessType.BASIC_COMMAND,
							ExecuteStatus.UNDONE,
							ForwardDstType.USER,
							chairmanMember.getId(),
							member.getId(),
							member.getSrcBundleId(),
							member.getSrcBundleName(),
							member.getSrcVenusBundleType(),
							member.getSrcLayerId(),
							member.getSrcVideoChannelId(),
							"VenusVideoIn",//videoBaseType,
							member.getSrcBundleId(),
							member.getSrcBundleName(),
							member.getSrcBundleType(),
							member.getSrcLayerId(),
							member.getSrcAudioChannelId(),
							"VenusAudioIn",//String audioBaseType,
							null,//chairmanMember.getDstBundleId(),
							null,//chairmanMember.getDstBundleName(),
							null,//chairmanMember.getDstBundleType(),
							null,//chairmanMember.getDstLayerId(),
							null,//chairmanMember.getDstVideoChannelId(),
							null,//String dstVideoBaseType,
							null,//chairmanMember.getDstAudioChannelId(),
							null,//chairmanMember.getDstBundleName(),
							null,//chairmanMember.getDstBundleType(),
							null,//chairmanMember.getDstLayerId(),
							null,//chairmanMember.getDstAudioChannelId(),
							null,//String dstAudioBaseType,
							creatorUserId,
							g_avtpl.getId(),//Long avTplId,
							currentGear.getId(),//Long gearId,
							DstDeviceType.WEBSITE_PLAYER,
							null,//LiveType type,
							null,//Long osdId,
							null//String osdUsername);
							);
					m2c_forward.setGroup(group);
					newForwards.add(m2c_forward);
					
				}
			}
			
			//协同
			Set<CommandGroupMemberPO> cooperateMembers = new HashSet<CommandGroupMemberPO>();
			for(CommandGroupMemberPO member : members){
				if(member.getCooperateStatus().equals(MemberStatus.CONNECT) || member.getCooperateStatus().equals(MemberStatus.CONNECTING)){
					cooperateMembers.add(member);
				}
			}
			
			//
			for(CommandGroupMemberPO member : newMembers){
				
				if(member.isAdministrator()){
					continue;
				}
				
//				List<CommandGroupUserPlayerPO> players = commandCommonServiceImpl.userChoseUsefulPlayers(member.getUserId(), PlayerBusinessType.COOPERATE_COMMAND, 1+cooperateMembers.size(), false);
//				int usefulPlayersCount = players.size();
//				log.info(new StringBufferWrapper().append("主席+协同成员数为 ").append(1+cooperateMembers.size())
//						.append("， ").append("新成员 ").append(member.getUserName()).append(" 可用的播放器为 ").append(usefulPlayersCount).toString());
				
				for(CommandGroupMemberPO cooperateMember : cooperateMembers){				
					
					//避免自己看自己
					if(member.getUuid().equals(cooperateMember.getUuid())){
						continue;
					}
					
					//协同给新成员的转发
					CommandGroupForwardPO c2m_forward = new CommandGroupForwardPO(
							ForwardBusinessType.COOPERATE_COMMAND,
							ExecuteStatus.UNDONE,
							ForwardDstType.USER,
							member.getId(),
							cooperateMember.getId(),
							cooperateMember.getSrcBundleId(),
							cooperateMember.getSrcBundleName(),
							cooperateMember.getSrcVenusBundleType(),
							cooperateMember.getSrcLayerId(),
							cooperateMember.getSrcVideoChannelId(),
							"VenusVideoIn",//videoBaseType,
							cooperateMember.getSrcBundleId(),
							cooperateMember.getSrcBundleName(),
							cooperateMember.getSrcBundleType(),
							cooperateMember.getSrcLayerId(),
							cooperateMember.getSrcAudioChannelId(),
							"VenusAudioIn",//String audioBaseType,
							null,//member.getDstBundleId(),
							null,//member.getDstBundleName(),
							null,//member.getDstBundleType(),
							null,//member.getDstLayerId(),
							null,//member.getDstVideoChannelId(),
							"VenusVideoOut",//String dstVideoBaseType,
							null,//member.getDstAudioChannelId(),
							null,//member.getDstBundleName(),
							null,//member.getDstBundleType(),
							null,//member.getDstLayerId(),
							null,//member.getDstAudioChannelId(),
							"VenusAudioOut",//String dstAudioBaseType,
							group.getUserId(),
							group.getAvtpl().getId(),//g_avtpl.getId(),//Long avTplId,
							currentGear.getId(),//Long gearId,
							DstDeviceType.WEBSITE_PLAYER,
							null,//LiveType type,
							null,//Long osdId,
							null//String osdUsername);
							);
					c2m_forward.setGroup(group);
					newForwards.add(c2m_forward);
					
					//新成员给的协同转发
					CommandGroupForwardPO m2c_forward = new CommandGroupForwardPO(
							ForwardBusinessType.COOPERATE_COMMAND,
							ExecuteStatus.UNDONE,
							ForwardDstType.USER,
							cooperateMember.getId(),
							member.getId(),
							member.getSrcBundleId(),
							member.getSrcBundleName(),
							member.getSrcVenusBundleType(),
							member.getSrcLayerId(),
							member.getSrcVideoChannelId(),
							"VenusVideoIn",//videoBaseType,
							member.getSrcBundleId(),
							member.getSrcBundleName(),
							member.getSrcBundleType(),
							member.getSrcLayerId(),
							member.getSrcAudioChannelId(),
							"VenusAudioIn",//String audioBaseType,
							null,//cooperateMember.getDstBundleId(),
							null,//cooperateMember.getDstBundleName(),
							null,//cooperateMember.getDstBundleType(),
							null,//cooperateMember.getDstLayerId(),
							null,//cooperateMember.getDstVideoChannelId(),
							"VenusVideoOut",//String dstVideoBaseType,
							null,//cooperateMember.getDstAudioChannelId(),
							null,//cooperateMember.getDstBundleName(),
							null,//cooperateMember.getDstBundleType(),
							null,//cooperateMember.getDstLayerId(),
							null,//cooperateMember.getDstAudioChannelId(),
							"VenusAudioOut",//String dstAudioBaseType,
							group.getUserId(),
							group.getAvtpl().getId(),//g_avtpl.getId(),//Long avTplId,
							currentGear.getId(),//Long gearId,
							DstDeviceType.WEBSITE_PLAYER,
							null,//LiveType type,
							null,//Long osdId,
							null//String osdUsername);
							);
					m2c_forward.setGroup(group);
					newForwards.add(m2c_forward);
				}
			}
			
			group.getForwards().addAll(newForwards);
			
			commandGroupDao.save(group);
			
			//这两个变量用来生成message用
			StringBufferWrapper newMembersNames = new StringBufferWrapper();
			JSONArray newMemberIdsJSONArray = new JSONArray();
			for(CommandGroupMemberPO newMember : newMembers){
				newMembersNames.append(newMember.getUserName()).append("，");
				newMemberIdsJSONArray.add(newMember.getId().toString());
			}
			newMembersNames.append("被邀请到 ").append(group.getName());//.append(" 会议");

			
			//这里去掉
			//发消息给其他人
			List<Long> consumeIds = new ArrayList<Long>();
//			for(CommandGroupMemberPO member : members){
//				if(member.isAdministrator() || newMemberIds.contains(member.getId())){
//					continue;
//				}
//				JSONObject message = new JSONObject();
//				message.put("businessType", "commandAddmembers");
//				message.put("fromUserId", chairmanMember.getUserId());
//				message.put("fromUserName", chairmanMember.getUserName());
//				message.put("businessId", group.getId().toString());
//				message.put("businessInfo", newMembersNames.toString());
//				
//				//发送消息。后续考虑使用CommandGroupPO.startTime作为唯一标识
//				WebsocketMessageVO ws = websocketMessageService.send(member.getUserId(), message.toJSONString(), WebsocketMessageType.COMMAND, chairmanMember.getUserId(), chairmanMember.getUserName());
//				consumeIds.add(ws.getId());
//			}
			
			
			//如果开会了，则选择锁定播放器；不需要下发协议，等成员接听后会处理
			//【注意】此处不呼叫主席的播放器，待成员接听后通过forward呼叫，测试是否可行
			if(group.getStatus().equals(GroupStatus.STOP)){
				websocketMessageService.consumeAll(consumeIds);
				return chairSplits;
			}
			
//			chairSplits = chosePlayersForMembers(group, newMembers);//后选播放器:去掉
			
			//发消息给新成员（这里不用考虑专向指挥）
			if(!autoEnter){
				for(CommandGroupMemberPO member : newMembers){
					String businessType = null;
					if(GroupType.MEETING.equals(group.getType())){
						businessType = "meetingStart";
					}else{
						businessType = "commandStart";
					}
					JSONObject message = new JSONObject();
					message.put("businessType", businessType);
					message.put("fromUserId", chairmanMember.getUserId());
					message.put("fromUserName", chairmanMember.getUserName());
					message.put("businessId", group.getId().toString());
					message.put("businessInfo", "接受到 " + group.getName() + " 邀请，主席：" + chairmanMember.getUserName() + "，是否进入？");
					
					//发送消息。后续考虑使用CommandGroupPO.startTime作为唯一标识
					WebsocketMessageVO ws = websocketMessageService.send(member.getUserId(), message.toJSONString(), WebsocketMessageType.COMMAND, chairmanMember.getUserId(), chairmanMember.getUserName());
					member.setMessageId(ws.getId());
				}
			}else{
				//自动接听，只给在线的成员自动上线
				List<CommandGroupMemberPO> onlineNewMembers = new ArrayList<CommandGroupMemberPO>();
				for(CommandGroupMemberPO newMember : newMembers){
					if(newMember.isAdministrator()) continue;
					UserBO commandUserBo = queryUtil.queryUserById(commandUserBos, newMember.getUserId());
					if(commandUserBo.isLogined()){
						onlineNewMembers.add(newMember);
					}else{
						//没在线的，置为DISCONNECT。不用按照“拒绝”处理
						newMember.setMemberStatus(MemberStatus.DISCONNECT);
					}
				}
				membersResponse(group, onlineNewMembers, null);
			}
			
			websocketMessageService.consumeAll(consumeIds);
			commandGroupDao.save(group);
			
			
			//呼叫主席（此处不呼叫主席的播放器，待成员接听后通过forward呼叫，测试是否可行）
//			List<CommandGroupMemberPO> acceptMembers = new ArrayList<CommandGroupMemberPO>();
//			acceptMembers.add(chairman);
//			membersResponse(group, acceptMembers, null);
			operationLogService.send(self.getNickname(), "添加成员", self.getNickname() + "添加成员userIds:" + userIdList.toString());
			return chairSplits;			
		}
	}

	public Object addOrEnterMembers(Long groupId, List<Long> userIdList) throws Exception{
		UserVO self = userQuery.current();
		JSONArray chairSplits = new JSONArray();
		
		if(groupId==null || groupId.equals("")){
			log.info("会议加人或进入，会议id有误");
			return chairSplits;
		}
		
		synchronized (new StringBuffer().append("command-group-").append(groupId).toString().intern()) {
					
			CommandGroupPO group = commandGroupDao.findOne(groupId);
			GroupType groupType = group.getType();
			List<CommandGroupMemberPO> members = group.getMembers();
			List<CommandGroupMemberPO> enterMembers = new ArrayList<CommandGroupMemberPO>();
			
			//本系统创建的，则鉴权，区分指挥与会议
//			if(!OriginType.OUTER.equals(group.getOriginType())){
//				if(groupType.equals(GroupType.BASIC)){
//					commandCommonServiceImpl.authorizeUsers(userIdList, group.getUserId(), BUSINESS_OPR_TYPE.ZK);
//				}else if(groupType.equals(GroupType.MEETING)){
//					commandCommonServiceImpl.authorizeUsers(userIdList, group.getUserId(), BUSINESS_OPR_TYPE.HY);
//				}
//			}
			
			//记录加人之前的用户列表
			List<MinfoBO> oldMemberInfos = commandCascadeUtil.generateMinfoBOList(members);
			
			//区分出新用户和进入用户
			List<Long> newUserIdList = new ArrayList<Long>();
			List<Long> enterUserIdList = new ArrayList<Long>();
			
			List<Long> existedUserIdList = commandGroupMemberDao.findUserIdsByGroupId(groupId);
			for(Long userId : userIdList){
				if(existedUserIdList.contains(userId)){
					
					CommandGroupMemberPO member = commandCommonUtil.queryMemberByUserId(members, userId);
					if(member.getMemberStatus().equals(MemberStatus.CONNECT)){
						//已经进会的成员，不用再处理
					}else{
						enterUserIdList.add(userId);
						enterMembers.add(member);
					}
					
				}else{
					newUserIdList.add(userId);
				}
			}
			
			//用户管理层的批量接口，根据userIds查询List<UserBO>，由于缺少folderId，所以额外查询queryAllFolders，给UserBO中的folderId赋值
			String newUserIdListStr = StringUtils.join(newUserIdList.toArray(), ",");
			List<UserBO> commandUserBos = resourceService.queryUserListByIds(newUserIdListStr, TerminalType.QT_ZK);
			if(commandUserBos == null) commandUserBos = new ArrayList<UserBO>();
			List<FolderPO> allFolders = resourceService.queryAllFolders();
			List<FolderUserMap> folderUserMaps = folderUserMapDao.findByUserIdIn(newUserIdList);
			String localLayerId = null;
		
			//从List<UserBO>取出bundleId列表，注意判空；给UserBO中的folderId赋值
			List<String> bundleIds = new ArrayList<String>();
			for(UserBO user : commandUserBos){
				String encoderId = commonQueryUtil.queryExternalOrLocalEncoderIdFromUserBO(user);
				if(encoderId != null){
					bundleIds.add(encoderId);
				}
				for(FolderPO folder : allFolders){
					if(folder.getUuid().equals(user.getFolderUuid())){
						user.setFolderId(folder.getId());
						break;
					}
				}
			}
			
			//从bundleId列表查询所有的bundlePO
			List<BundlePO> srcBundleEntities = resourceBundleDao.findByBundleIds(bundleIds);
			if(srcBundleEntities == null) srcBundleEntities = new ArrayList<BundlePO>();
			
			//从bundleId列表查询所有的视频编码1通道
			List<ChannelSchemeDTO> videoEncode1Channels = resourceChannelDao.findByBundleIdsAndChannelId(bundleIds, ChannelType.VIDEOENCODE1.getChannelId());
			if(videoEncode1Channels == null) videoEncode1Channels = new ArrayList<ChannelSchemeDTO>();
			//通过视频编码通道来校验编码器是否可用
			if(videoEncode1Channels.size() < commandUserBos.size()){
				for(UserBO user : commandUserBos){
					
					//外部系统用户则跳过
					if(queryUtil.isLdapUser(user, folderUserMaps)){
						continue;
					}

					boolean hasChannel = false;
					String encoderId = commonQueryUtil.queryExternalOrLocalEncoderIdFromUserBO(user);
					for(ChannelSchemeDTO channel : videoEncode1Channels){
						if(channel.getBundleId().equals(encoderId)){
							hasChannel = true;
							break;
						}
					}
					if(!hasChannel){
						throw new UserHasNoAvailableEncoderException(user.getName());
					}
				}
			}
						
			//从bundleId列表查询所有的音频编码1通道
			List<ChannelSchemeDTO> audioEncode1Channels = resourceChannelDao.findByBundleIdsAndChannelId(bundleIds, ChannelType.AUDIOENCODE1.getChannelId());
			if(audioEncode1Channels == null) audioEncode1Channels = new ArrayList<ChannelSchemeDTO>();
			
//			Set<CommandGroupForwardPO> forwards = group.getForwards();
			CommandGroupMemberPO chairmanMember = commandCommonUtil.queryChairmanMember(members);
			Long creatorUserId = chairmanMember.getUserId();
			CommandGroupAvtplPO g_avtpl = group.getAvtpl();
			CommandGroupAvtplGearsPO currentGear = commandCommonUtil.queryCurrentGear(group);
			
			List<CommandGroupMemberPO> newMembers = new ArrayList<CommandGroupMemberPO>();
			List<CommandGroupForwardPO> newForwards = new ArrayList<CommandGroupForwardPO>();
			
//			BasicRolePO chairmanRole = basicRoleDao.findByName("主席");
//			BasicRolePO memberRole = basicRoleDao.findByName("会议员");
			for(UserBO user : commandUserBos){
				CommandGroupMemberPO memberPO = new CommandGroupMemberPO();
				//关联会议员角色
//				memberPO.setRoleId(memberRole.getId());
//				memberPO.setRoleName(memberRole.getName());
				
				memberPO.setUserId(user.getId());
				memberPO.setUserName(user.getName());
				memberPO.setUserNum(user.getUserNo());
				memberPO.setGroup(group);
				if(user.getFolderId() == null){
					throw new BaseException(StatusCode.FORBIDDEN, memberPO.getUserName() + " 没有组织机构！");
				}
				memberPO.setFolderId(user.getFolderId());
				newMembers.add(memberPO);
				
				//ldap用户，生成一套参数id
				if(queryUtil.isLdapUser(user, folderUserMaps)){
					if(localLayerId == null){
						localLayerId = resourceRemoteService.queryLocalLayerId();
					}
					memberPO.setOriginType(OriginType.OUTER);
					memberPO.setSrcBundleId(memberPO.getUserNum() + "user" + UUID.randomUUID().toString().replace("-", ""));
					memberPO.setSrcLayerId(localLayerId);
					memberPO.setSrcVideoChannelId(ChannelType.VIDEOENCODE1.getChannelId());
					memberPO.setSrcAudioChannelId(ChannelType.AUDIOENCODE1.getChannelId());
					continue;
				}
				
				//遍历bundle
				String encoderId = commonQueryUtil.queryExternalOrLocalEncoderIdFromUserBO(user);
				for(BundlePO bundle : srcBundleEntities){
					if(bundle.getBundleId().equals(encoderId)){
						memberPO.setSrcBundleId(bundle.getBundleId());
						memberPO.setSrcBundleName(bundle.getBundleName());
						memberPO.setSrcBundleType(bundle.getDeviceModel());
						memberPO.setSrcVenusBundleType(bundle.getBundleType());
						memberPO.setSrcLayerId(bundle.getAccessNodeUid());
						break;
					}
				}
				
				//遍历视频通道
				for(ChannelSchemeDTO videoChannel : videoEncode1Channels){
					if(videoChannel.getBundleId().equals(encoderId)){
						memberPO.setSrcVideoChannelId(videoChannel.getChannelId());
						break;
					}
				}			
				
				//遍历音频通道
				for(ChannelSchemeDTO audioChannel : audioEncode1Channels){
					if(audioChannel.getBundleId().equals(encoderId)){
						memberPO.setSrcAudioChannelId(audioChannel.getChannelId());
						break;
					}
				}
			}
			
			//保存以获得member的id
			members.addAll(newMembers);
			commandGroupMemberDao.save(newMembers);
			commandGroupDao.save(group);
			
			List<Long> newMemberIds = new ArrayList<Long>();
			for(CommandGroupMemberPO member : newMembers){
				newMemberIds.add(member.getId());
				if(member.isAdministrator()){
					//建立所有成员到主席的转发（在下边）
					
				}else{
					//建立主席到成员的转发
					CommandGroupForwardPO c2m_forward = new CommandGroupForwardPO(
							ForwardBusinessType.BASIC_COMMAND,
							ExecuteStatus.UNDONE,
							ForwardDstType.USER,
							member.getId(),
							chairmanMember.getId(),
							chairmanMember.getSrcBundleId(),
							chairmanMember.getSrcBundleName(),
							chairmanMember.getSrcVenusBundleType(),
							chairmanMember.getSrcLayerId(),
							chairmanMember.getSrcVideoChannelId(),
							"VenusVideoIn",//videoBaseType,
							chairmanMember.getSrcBundleId(),
							chairmanMember.getSrcBundleName(),
							chairmanMember.getSrcBundleType(),
							chairmanMember.getSrcLayerId(),
							chairmanMember.getSrcAudioChannelId(),
							"VenusAudioIn",//String audioBaseType,
							null,//member.getDstBundleId(),
							null,//member.getDstBundleName(),
							null,//member.getDstBundleType(),
							null,//member.getDstLayerId(),
							null,//member.getDstVideoChannelId(),
							"VenusVideoOut",//String dstVideoBaseType,
							null,//member.getDstAudioChannelId(),
							null,//member.getDstBundleName(),
							null,//member.getDstBundleType(),
							null,//member.getDstLayerId(),
							null,//member.getDstAudioChannelId(),
							"VenusAudioOut",//String dstAudioBaseType,
							creatorUserId,
							g_avtpl.getId(),//Long avTplId,
							currentGear.getId(),//Long gearId,
							DstDeviceType.WEBSITE_PLAYER,
							null,//LiveType type,
							null,//Long osdId,
							null//String osdUsername);
							);
					c2m_forward.setGroup(group);
					newForwards.add(c2m_forward);
					
					//建立成员到主席的转发
					CommandGroupForwardPO m2c_forward = new CommandGroupForwardPO(
							ForwardBusinessType.BASIC_COMMAND,
							ExecuteStatus.UNDONE,
							ForwardDstType.USER,
							chairmanMember.getId(),
							member.getId(),
							member.getSrcBundleId(),
							member.getSrcBundleName(),
							member.getSrcVenusBundleType(),
							member.getSrcLayerId(),
							member.getSrcVideoChannelId(),
							"VenusVideoIn",//videoBaseType,
							member.getSrcBundleId(),
							member.getSrcBundleName(),
							member.getSrcBundleType(),
							member.getSrcLayerId(),
							member.getSrcAudioChannelId(),
							"VenusAudioIn",//String audioBaseType,
							null,//chairmanMember.getDstBundleId(),
							null,//chairmanMember.getDstBundleName(),
							null,//chairmanMember.getDstBundleType(),
							null,//chairmanMember.getDstLayerId(),
							null,//chairmanMember.getDstVideoChannelId(),
							null,//String dstVideoBaseType,
							null,//chairmanMember.getDstAudioChannelId(),
							null,//chairmanMember.getDstBundleName(),
							null,//chairmanMember.getDstBundleType(),
							null,//chairmanMember.getDstLayerId(),
							null,//chairmanMember.getDstAudioChannelId(),
							null,//String dstAudioBaseType,
							creatorUserId,
							g_avtpl.getId(),//Long avTplId,
							currentGear.getId(),//Long gearId,
							DstDeviceType.WEBSITE_PLAYER,
							null,//LiveType type,
							null,//Long osdId,
							null//String osdUsername);
							);
					m2c_forward.setGroup(group);
					newForwards.add(m2c_forward);
					
				}
			}
			
			//协同或发言人
			Set<CommandGroupMemberPO> cooperateMembers = new HashSet<CommandGroupMemberPO>();
			GroupSpeakType speakType = group.getSpeakType();
			if(GroupSpeakType.DISCUSS.equals(speakType)){
				//讨论模式下，除了主席，所有进会成员都是“发言人”
				for(CommandGroupMemberPO member : members){
					if(member.isAdministrator()) continue;
					if(member.getMemberStatus().equals(MemberStatus.CONNECT)){
						cooperateMembers.add(member);
					}
				}
			}else{
				for(CommandGroupMemberPO member : members){
					if(member.getCooperateStatus().equals(MemberStatus.CONNECT) || member.getCooperateStatus().equals(MemberStatus.CONNECTING)){
						cooperateMembers.add(member);
					}
				}
			}
			
			//生成协同或发言的转发
			for(CommandGroupMemberPO member : newMembers){
				
				if(member.isAdministrator()){
					continue;
				}
				
				for(CommandGroupMemberPO cooperateMember : cooperateMembers){
					
					//避免自己看自己
					if(member.getUuid().equals(cooperateMember.getUuid())){
						continue;
					}
					
					//协同给新成员的转发
					CommandGroupForwardPO c2m_forward = new CommandGroupForwardPO(
							ForwardBusinessType.COOPERATE_COMMAND,
							ExecuteStatus.UNDONE,
							ForwardDstType.USER,
							member.getId(),
							cooperateMember.getId(),
							cooperateMember.getSrcBundleId(),
							cooperateMember.getSrcBundleName(),
							cooperateMember.getSrcVenusBundleType(),
							cooperateMember.getSrcLayerId(),
							cooperateMember.getSrcVideoChannelId(),
							"VenusVideoIn",//videoBaseType,
							cooperateMember.getSrcBundleId(),
							cooperateMember.getSrcBundleName(),
							cooperateMember.getSrcBundleType(),
							cooperateMember.getSrcLayerId(),
							cooperateMember.getSrcAudioChannelId(),
							"VenusAudioIn",//String audioBaseType,
							null,//member.getDstBundleId(),
							null,//member.getDstBundleName(),
							null,//member.getDstBundleType(),
							null,//member.getDstLayerId(),
							null,//member.getDstVideoChannelId(),
							"VenusVideoOut",//String dstVideoBaseType,
							null,//member.getDstAudioChannelId(),
							null,//member.getDstBundleName(),
							null,//member.getDstBundleType(),
							null,//member.getDstLayerId(),
							null,//member.getDstAudioChannelId(),
							"VenusAudioOut",//String dstAudioBaseType,
							group.getUserId(),
							group.getAvtpl().getId(),//g_avtpl.getId(),//Long avTplId,
							currentGear.getId(),//Long gearId,
							DstDeviceType.WEBSITE_PLAYER,
							null,//LiveType type,
							null,//Long osdId,
							null//String osdUsername);
							);
					c2m_forward.setGroup(group);
					newForwards.add(c2m_forward);
					
					//会议模式下，发言人不用看新成员
					if(GroupType.MEETING.equals(groupType)){
						continue;
					}
					
					//新成员给协同成员的转发
					CommandGroupForwardPO m2c_forward = new CommandGroupForwardPO(
							ForwardBusinessType.COOPERATE_COMMAND,
							ExecuteStatus.UNDONE,
							ForwardDstType.USER,
							cooperateMember.getId(),
							member.getId(),
							member.getSrcBundleId(),
							member.getSrcBundleName(),
							member.getSrcVenusBundleType(),
							member.getSrcLayerId(),
							member.getSrcVideoChannelId(),
							"VenusVideoIn",//videoBaseType,
							member.getSrcBundleId(),
							member.getSrcBundleName(),
							member.getSrcBundleType(),
							member.getSrcLayerId(),
							member.getSrcAudioChannelId(),
							"VenusAudioIn",//String audioBaseType,
							null,//cooperateMember.getDstBundleId(),
							null,//cooperateMember.getDstBundleName(),
							null,//cooperateMember.getDstBundleType(),
							null,//cooperateMember.getDstLayerId(),
							null,//cooperateMember.getDstVideoChannelId(),
							"VenusVideoOut",//String dstVideoBaseType,
							null,//cooperateMember.getDstAudioChannelId(),
							null,//cooperateMember.getDstBundleName(),
							null,//cooperateMember.getDstBundleType(),
							null,//cooperateMember.getDstLayerId(),
							null,//cooperateMember.getDstAudioChannelId(),
							"VenusAudioOut",//String dstAudioBaseType,
							group.getUserId(),
							group.getAvtpl().getId(),//g_avtpl.getId(),//Long avTplId,
							currentGear.getId(),//Long gearId,
							DstDeviceType.WEBSITE_PLAYER,
							null,//LiveType type,
							null,//Long osdId,
							null//String osdUsername);
							);
					m2c_forward.setGroup(group);
					newForwards.add(m2c_forward);
				}
			}
			
			group.getForwards().addAll(newForwards);
			
			commandGroupDao.save(group);
			
			//这两个变量用来生成message用
			StringBufferWrapper newMembersNames = new StringBufferWrapper();
			JSONArray newMemberIdsJSONArray = new JSONArray();
			for(CommandGroupMemberPO newMember : newMembers){
				newMembersNames.append(newMember.getUserName()).append("，");
				newMemberIdsJSONArray.add(newMember.getId().toString());
			}
			newMembersNames.append("被邀请到 ").append(group.getName());//.append(" 会议");			
			List<Long> consumeIds = new ArrayList<Long>();
			List<MessageSendCacheBO> messageCaches = new ArrayList<MessageSendCacheBO>();
			
			//如果开会了，则选择锁定播放器；不需要下发协议，等成员接听后会处理
			//此处不呼叫主席的播放器，待成员接听后通过forward呼叫	
			if(!group.getStatus().equals(GroupStatus.STOP)){
				
	//			chairSplits = chosePlayersForMembers(group, newMembers);//后选播放器:去掉
				
				//新的在线的成员自动上线，还有“进入”的成员
				List<CommandGroupMemberPO> loginNewAndEnterMembers = new ArrayList<CommandGroupMemberPO>();
				
				//发消息给新成员（这里不用考虑专向指挥）
				if(!autoEnter){
					for(CommandGroupMemberPO member : newMembers){
						
						if(OriginType.OUTER.equals(group.getOriginType())){
							continue;
						}
						
						String businessType = null;
						if(GroupType.MEETING.equals(groupType)){
							businessType = "meetingStart";
						}else{
							businessType = "commandStart";
						}
						JSONObject message = new JSONObject();
						message.put("businessType", businessType);
						message.put("fromUserId", chairmanMember.getUserId());
						message.put("fromUserName", chairmanMember.getUserName());
						message.put("businessId", group.getId().toString());
						message.put("businessInfo", "接受到 " + group.getName() + " 邀请，主席：" + chairmanMember.getUserName() + "，是否进入？");
						
						//发送消息
						messageCaches.add(new MessageSendCacheBO(member.getUserId(), message.toJSONString(), WebsocketMessageType.COMMAND));
					}
				}else{
					//自动接听
					for(CommandGroupMemberPO newMember : newMembers){
						if(newMember.isAdministrator()) continue;
						UserBO commandUserBo = queryUtil.queryUserById(commandUserBos, newMember.getUserId());
						if(commandUserBo.isLogined()){
							loginNewAndEnterMembers.add(newMember);
						}else{
							//没在线的，置为DISCONNECT。不用按照“拒绝”处理
							newMember.setMemberStatus(MemberStatus.DISCONNECT);
						}
					}
					
					loginNewAndEnterMembers.addAll(enterMembers);				
					membersResponse(group, loginNewAndEnterMembers, null);
				}
				
				//把新成员/进入的成员加入讨论
				if(GroupSpeakType.DISCUSS.equals(speakType)){
					commandMeetingSpeakServiceImpl.speakStart(group, loginNewAndEnterMembers, 2);
				}
			}
			
			//级联，必须在membersResponse之后，这样MemberStatus才是对的
			if(!OriginType.OUTER.equals(group.getOriginType())){
				
				//停会则 groupUpdate
				if(GroupStatus.STOP.equals(group.getStatus())){
					if(GroupType.BASIC.equals(groupType)){
						GroupBO groupBO = commandCascadeUtil.updateCommand(group);
						commandCascadeService.update(groupBO);						
					}else if(GroupType.MEETING.equals(groupType)){
						GroupBO groupBO = commandCascadeUtil.updateMeeting(group);
						conferenceCascadeService.update(groupBO);			
					}
				}
				//开会中则 maddinc maddfull
				else{					
					List<CommandGroupMemberPO> newAndEnterMembers = new ArrayList<CommandGroupMemberPO>();
					newAndEnterMembers.addAll(newMembers);
					newAndEnterMembers.addAll(enterMembers);
					
					//记录新加入的用户列表
					List<MinfoBO> newMemberInfos = commandCascadeUtil.generateMinfoBOList(newAndEnterMembers, chairmanMember);
					//得到位于新节点上的用户列表
					List<MinfoBO> newNodeMemberInfos = commandCascadeUtil.filterAddedNodeMinfo(oldMemberInfos, newMemberInfos);
					
					if(GroupType.BASIC.equals(groupType)){
						
						GroupBO groupBO = commandCascadeUtil.joinCommand(group, oldMemberInfos, newAndEnterMembers);
						commandCascadeService.join(groupBO);
						
						//如果有新节点，则要发maddfull
						if(newNodeMemberInfos.size() > 0){
							GroupBO maddfullBO = commandCascadeUtil.maddfullCommand(group, newNodeMemberInfos);
							commandCascadeService.info(maddfullBO);
							log.info(newNodeMemberInfos.size() + "个成员所在的节点新参与指挥，全量同步该指挥：" + group.getName());
						}
						
					}else if(GroupType.MEETING.equals(groupType)){
						
						GroupBO groupBO = commandCascadeUtil.joinMeeting(group, oldMemberInfos, newAndEnterMembers);
						conferenceCascadeService.join(groupBO);
						
						//如果有新节点，则要发maddfull
						if(newNodeMemberInfos.size() > 0){
							GroupBO maddfullBO = commandCascadeUtil.maddfullMeeting(group, newNodeMemberInfos);
							conferenceCascadeService.info(maddfullBO);
							log.info(newNodeMemberInfos.size() + "个成员所在的节点新参与会议，全量同步该会议：" + group.getName());
						}
					}
				}
			}
			
//			if(group.getStatus().equals(GroupStatus.STOP)){
//				return chairSplits;
//			}
			
			//发消息，主要是“邀请”消息，自动接听则没有
			for(MessageSendCacheBO cache : messageCaches){
				WebsocketMessageVO ws = websocketMessageService.send(cache.getUserId(), cache.getMessage(), cache.getType(), cache.getFromUserId(), cache.getFromUsername());
				consumeIds.add(ws.getId());
			}
			websocketMessageService.consumeAll(consumeIds);
			
			//呼叫主席（此处不呼叫主席的播放器，待成员接听后通过forward呼叫，测试是否可行）
//			List<CommandGroupMemberPO> acceptMembers = new ArrayList<CommandGroupMemberPO>();
//			acceptMembers.add(chairman);
//			membersResponse(group, acceptMembers, null);
			operationLogService.send(self.getNickname(), "成员加入", self.getNickname()
					+ "添加成员userIds:" + newUserIdList.toString()
					+ "进入成员userIds:" + enterUserIdList.toString());
			return chairSplits;			
		}
	}
	
	/**
	 * 给新进入的成员和主席选播放器<br/>
	 * <p>详细描述</p>
	 * <b>作者:</b>zsy<br/>
	 * <b>版本：</b>1.0<br/>
	 * <b>日期：</b>2020年4月2日 下午6:14:46
	 * @param group
	 * @param newMembers 新进入的成员，必须是group.getMembers()的一部分
	 * @return chairSplits 主席新增的播放器。考虑优化成List<BusinessPlayerVO>
	 * @throws Exception
	 */
	private JSONArray chosePlayersForMembers(CommandGroupPO group, List<CommandGroupMemberPO> newMembers) throws Exception{
		//此时forwrad已经建立完毕
		//【注意】此处不呼叫主席的播放器，待成员接听后通过forward呼叫，测试是否可行
		
		JSONArray chairSplits = new JSONArray();
		
		if(newMembers==null || newMembers.size()==0){
			return chairSplits;
		}
		
		List<CommandGroupMemberPO> members = group.getMembers();
		List<CommandGroupForwardPO> forwards = group.getForwards();
		GroupType groupType = group.getType();
//		String commandString = commandCommonUtil.generateCommandString(group.getType());
//		String cooperateString = commandCommonUtil.generateCooperateString(groupType);
		
		//新成员中是否含有主席。
		boolean newMembersContainsChairman = false;
		
		List<Long> newMemberIds = new ArrayList<Long>();
		for(CommandGroupMemberPO newMember : newMembers){
			newMemberIds.add(newMember.getId());
			if(newMember.isAdministrator()){
				newMembersContainsChairman = true;
			}
		}
		
		//以新进入成员为源的转发
		Set<CommandGroupForwardPO> newForwards = commandCommonUtil.queryForwardsBySrcmemberIds(forwards, newMemberIds, null, null);
		
		Set<CommandGroupMemberPO> cooperateMembers = new HashSet<CommandGroupMemberPO>();
		for(CommandGroupMemberPO member : members){
			if(member.getCooperateStatus().equals(MemberStatus.CONNECT)){// || member.getCooperateStatus().equals(MemberStatus.CONNECTING)){//CONNECTING状态不再使用
				cooperateMembers.add(member);
			}
		}
		
		//处理主席
		int chairmanNeedPlayer = newMembers.size();
		if(newMembersContainsChairman) chairmanNeedPlayer--;
		
		//专向指挥特殊处理，先给主席选播放器
		if(group.getType().equals(GroupType.SECRET)){
			//专向指挥需要选取以新进成员为源或目的的转发，因为先给主席选了播放器
			newForwards = commandCommonUtil.queryForwardsByMemberIds(forwards, newMemberIds, null, null);
			if(newMembersContainsChairman){
				chairmanNeedPlayer = 1;
			}else{
				chairmanNeedPlayer = 0;
			}
		}
		
		CommandGroupMemberPO chairmanMember = commandCommonUtil.queryChairmanMember(group.getMembers());
		if(!OriginType.OUTER.equals(chairmanMember.getOriginType())
				&& chairmanNeedPlayer > 0){
			List<CommandGroupUserPlayerPO> cPlayers = commandCommonServiceImpl.userChoseUsefulPlayers(chairmanMember.getUserId(), PlayerBusinessType.BASIC_COMMAND, chairmanNeedPlayer, false);
			int usefulPlayersCount = cPlayers.size();
			if(group.getType().equals(GroupType.SECRET) && chairmanNeedPlayer==1 && usefulPlayersCount==0){
				throw new HasNotUsefulPlayerException();
			}
			log.info(new StringBufferWrapper()
					.append(group.getName())
					.append(" 新进入成员总数（不含主席）为 ").append(chairmanNeedPlayer)
					.append(" 主席可用新的播放器数为 ").append(usefulPlayersCount).toString());
			
			//处理所有【新的】给主席的转发
			for(CommandGroupForwardPO forward : newForwards){
				if(chairmanMember.getId().equals(forward.getDstMemberId())){
					if(usefulPlayersCount > 0){
						CommandGroupUserPlayerPO player = cPlayers.get(cPlayers.size() - usefulPlayersCount);
						CommandGroupMemberPO srcMember = commandCommonUtil.queryMemberById(members, forward.getSrcMemberId());
						player.setBusinessName(group.getName() + "：" + srcMember.getUserName());//添加成员名称
						if(groupType.equals(GroupType.SECRET)){
							player.setBusinessId(group.getId().toString());
							player.setPlayerBusinessType(PlayerBusinessType.SECRET_COMMAND);
						}else{
							player.setBusinessId(group.getId().toString() + "-" + srcMember.getUserId());
							player.setPlayerBusinessType(PlayerBusinessType.CHAIRMAN_BASIC_COMMAND);
						}
						
						//用于返回的分屏信息
						JSONObject split = new JSONObject();
						split.put("serial", player.getLocationIndex());
						split.put("bundleId", player.getBundleId());
						split.put("bundleNo", player.getCode());
						split.put("businessType", "commandMember");
						split.put("businessId", player.getBusinessId());
						split.put("businessInfo", player.getBusinessName());
						split.put("status", "start");
						chairSplits.add(split);
						
						//给转发设置目的
						forward.setDstPlayer(player);
						player.setMember(chairmanMember);
						usefulPlayersCount--;
					}else{
						forward.setExecuteStatus(ExecuteStatus.NO_AVAILABLE_PLAYER);
					}
				}
			}
			if(chairmanMember.getPlayers() == null){
				chairmanMember.setPlayers(new ArrayList<CommandGroupUserPlayerPO>());
				chairmanMember.getPlayers().clear();
			}
			chairmanMember.getPlayers().addAll(cPlayers);
		}
		
		//处理其它新成员
		for(CommandGroupMemberPO member : newMembers){
			if(member.isAdministrator()){
				continue;
			}
			
			member.setMemberStatus(MemberStatus.CONNECTING);//TODO:这个需要吗？
			
			if(OriginType.OUTER.equals(member.getOriginType())){
				continue;
			}

			if(member.getPlayers() == null){
				member.setPlayers(new ArrayList<CommandGroupUserPlayerPO>());
				member.getPlayers().clear();
			}
			
			//查找看主席的播放器
			CommandGroupUserPlayerPO player4c = commandCommonServiceImpl.userChoseUsefulPlayer(member.getUserId(), PlayerBusinessType.BASIC_COMMAND, true);
			if(player4c != null){
				log.info(new StringBufferWrapper().append("新进入会议成员 ").append(member.getUserName()).
						append(" id: ").append(member.getId()).append(" 观看主席的播放器serial为 ").append(player4c.getLocationIndex()).toString());
			}else{
				//没有可用的播放器
				log.info(new StringBufferWrapper().append("新进入会议成员 ").append(member.getUserName()).
						append(" id: ").append(member.getId()).append(" 没有可用的播放器").toString());
			}
			
			//处理主席为源、该成员为目的的普通会议转发
			for(CommandGroupForwardPO forward : forwards){
				if(member.getId().equals(forward.getDstMemberId()) 
						&& forward.getForwardBusinessType().equals(ForwardBusinessType.BASIC_COMMAND)){
					if(null != player4c){
						
						player4c.setBusinessId(group.getId().toString());
						player4c.setBusinessName(group.getName() + "：" + chairmanMember.getUserName());
						player4c.setPlayerBusinessType(PlayerBusinessType.BASIC_COMMAND);
						if(groupType.equals(GroupType.SECRET)){
							player4c.setPlayerBusinessType(PlayerBusinessType.SECRET_COMMAND);
						}else{
							player4c.setPlayerBusinessType(PlayerBusinessType.BASIC_COMMAND);
						}
						
						//给转发设置目的
						forward.setDstPlayer(player4c);
						
						member.getPlayers().add(player4c);
						player4c.setMember(member);
					}else{
						forward.setExecuteStatus(ExecuteStatus.NO_AVAILABLE_PLAYER);
					}
				}
			}

			//给每个新成员查找[协同数]个播放器，建立转发
			List<CommandGroupUserPlayerPO> players = commandCommonServiceImpl.userChoseUsefulPlayers(member.getUserId(), PlayerBusinessType.COOPERATE_COMMAND, cooperateMembers.size(), false);
			int usefulPlayersCount2 = players.size();
			log.info(new StringBufferWrapper().append("（不含主席的）协同成员数为 ").append(cooperateMembers.size())
					.append("， ").append("新成员 ").append(member.getUserName()).append(" 可用的播放器数为 ").append(usefulPlayersCount2).toString());
			
			//处理该成员为目的的协同会议转发
			for(CommandGroupForwardPO forward : forwards){
				if(member.getId().equals(forward.getDstMemberId()) 
						&& forward.getForwardBusinessType().equals(ForwardBusinessType.COOPERATE_COMMAND)){
					//该成员为目的的协同转发，如果有播放器，则设置dst
					if(usefulPlayersCount2 > 0){
						
						//找到协同成员
						CommandGroupMemberPO cooperateMember = commandCommonUtil.queryMemberById(members, forward.getSrcMemberId());
						
						CommandGroupUserPlayerPO player = players.get(players.size() - usefulPlayersCount2);
						
						player.setBusinessId(group.getId().toString() + cooperateMember.getUserId());//如果需要改成c2m_forward.getId()，那么需要先save获得id
						player.setBusinessName(group.getName() + "：" + cooperateMember.getUserName());// + cooperateString);
						if(groupType.equals(GroupType.MEETING)){
							player.setPlayerBusinessType(PlayerBusinessType.SPEAK_MEETING);
						}else{
							player.setPlayerBusinessType(PlayerBusinessType.COOPERATE_COMMAND);
						}
						
//						//用于返回的分屏信息
//						JSONObject split = new JSONObject();
//						split.put("serial", player.getLocationIndex());
//						split.put("bundleId", player.getBundleId());
//						split.put("bundleNo", player.getCode());
//						split.put("businessType", "command");
//						split.put("businessId", group.getId().toString());
//						split.put("businessInfo", player.getBusinessName());
//						split.put("status", group.getStatus().getCode());
//						chairSplits.add(split);
						
						//给转发设置目的
						forward.setDstPlayer(player);
						forward.setExecuteStatus(ExecuteStatus.UNDONE);//UNDONE不行就改成WAITING_FOR_RESPONSE
						player.setMember(member);
						usefulPlayersCount2--;
					}else{
						forward.setExecuteStatus(ExecuteStatus.NO_AVAILABLE_PLAYER);
					}
				}
			}
			member.getPlayers().addAll(players);
		}
		
		//给普通指挥的协同成员查找播放器（在会议中，发言人不需要观看其他人）
		if(groupType.equals(GroupType.BASIC)){			
			for(CommandGroupMemberPO cooperateMember : cooperateMembers){
				
				if(OriginType.OUTER.equals(cooperateMember.getOriginType())){
					continue;
				}
				
				//给每个协同成员查找[新成员数]个播放器，建立转发
				List<CommandGroupUserPlayerPO> players = commandCommonServiceImpl.userChoseUsefulPlayers(cooperateMember.getUserId(), PlayerBusinessType.COOPERATE_COMMAND, newMembers.size(), false);
				int usefulPlayersCount2 = players.size();
				log.info(new StringBufferWrapper().append("新成员数为 ").append(newMembers.size())
						.append("， ").append("协同成员 ").append(cooperateMember.getUserName()).append(" 可用的播放器数为 ").append(usefulPlayersCount2).toString());
				
				//处理该协同成员为目的，新成员为源，的协同会议转发
				for(CommandGroupMemberPO newMember : newMembers){
					
					CommandGroupForwardPO forward = commandCommonUtil.queryForwardBySrcAndDstMemberId(forwards, newMember.getId(), cooperateMember.getId());
									
					//正常情况下不会异常
					if(!forward.getForwardBusinessType().equals(ForwardBusinessType.COOPERATE_COMMAND)){
						log.error("异常！转发不是协同类型 id 源成员 目的成员: " + forward.getId() + " (" + newMember.getId() + ", " + newMember.getUserName() + ") ("
								+ cooperateMember.getId() + ", " + cooperateMember.getUserName() + ")");
					}
					
					//该成员为目的的协同转发，如果有播放器，则设置dst
					if(usefulPlayersCount2 > 0){
						
						CommandGroupUserPlayerPO player = players.get(players.size() - usefulPlayersCount2);
						
						player.setBusinessId(group.getId().toString() + newMember.getUserId());//如果需要改成c2m_forward.getId()，那么需要先save获得id
						player.setBusinessName(group.getName() + "：" + newMember.getUserName());// + cooperateString);
						if(groupType.equals(GroupType.MEETING)){
							player.setPlayerBusinessType(PlayerBusinessType.SPEAK_MEETING);
						}else{
							player.setPlayerBusinessType(PlayerBusinessType.COOPERATE_COMMAND);
						}
						
						//给转发设置目的
						forward.setDstPlayer(player);
						forward.setExecuteStatus(ExecuteStatus.UNDONE);//UNDONE不行就改成WAITING_FOR_RESPONSE
						player.setMember(cooperateMember);
						usefulPlayersCount2--;
					}else{
						forward.setExecuteStatus(ExecuteStatus.NO_AVAILABLE_PLAYER);
					}
				}			
				cooperateMember.getPlayers().addAll(players);
			}
		}
		
		commandGroupDao.save(group);		
		
		return chairSplits;
	}

	/**
	 * 
	 * 删除成员/成员退出<br/>
	 * <p>详细描述</p>
	 * <b>作者:</b>zsy<br/>
	 * <b>版本：</b>1.0<br/>
	 * <b>日期：</b>2019年11月12日 下午6:14:57
	 * @param groupId
	 * @param userIdList
	 * @param mode 1为删人，0为退出（列表和转发都保留）
	 * @return 1删人时给主席返回chairSplits；0退出时给退出成员返回exitMemberSplits；后续优化
	 * @throws Exception
	 */
	@Deprecated
	public Object removeMembers(Long groupId, List<Long> userIdList, int mode) throws Exception{
		UserVO user = userQuery.current();
		//“重复退出会再次挂断编码器”已改好
		
		if(groupId==null || groupId.equals("")){
			throw new BaseException(StatusCode.FORBIDDEN, "退出操作，会议id有误");
		}
		
		synchronized (new StringBuffer().append("command-group-").append(groupId).toString().intern()) {
			
			CommandGroupPO group = commandGroupDao.findOne(groupId);
			
			if(group == null){
//				throw new BaseException(StatusCode.FORBIDDEN, "会议不存在，id: " + groupId);
				log.info("进行退出/删人操作的会议不存在，id：" + groupId);
				return new JSONArray();
			}
			
			//会议停止状态下不能“退出”
			if(mode == 0){
				if(group.getStatus().equals(GroupStatus.STOP)){
//					throw new BaseException(StatusCode.FORBIDDEN, group.getName() + " 已停止，不需退出，id: " + group.getId());
					return new JSONArray();
				}
			}
			
			//防止专向指挥中调用此方法
			if(group.getType().equals(GroupType.SECRET)){
				throw new BaseException(StatusCode.FORBIDDEN, "正在专项，不能退出，只能“停止”");
			}
			
			JSONArray chairSplits = new JSONArray();
			JSONArray exitMemberSplits = new JSONArray();
			List<Long> consumeIds = new ArrayList<Long>();
			List<MessageSendCacheBO> messageCaches = new ArrayList<MessageSendCacheBO>();
			
			List<CommandGroupMemberPO> members = group.getMembers();
			List<CommandGroupForwardPO> forwards = group.getForwards();
			List<CommandGroupForwardDemandPO> demands = group.getForwardDemands();
			CommandGroupMemberPO chairmanMember = commandCommonUtil.queryChairmanMember(members);
			
			if(userIdList.contains(chairmanMember.getUserId())){
				throw new BaseException(StatusCode.FORBIDDEN, group.getName() + "不能删除主席");
			}
			
			List<CommandGroupMemberPO> removeMembers = new ArrayList<CommandGroupMemberPO>();
			List<Long> removeMemberIds = new ArrayList<Long>();
			for(CommandGroupMemberPO member : members){
				if(userIdList.contains(member.getUserId())){
					removeMembers.add(member);
					removeMemberIds.add(member.getId());
				}
			}
			
			//以这些成员为源和目的的转发
			Set<CommandGroupForwardPO> needDelForwards = commandCommonUtil.queryForwardsByMemberIds(forwards, removeMemberIds, null, null);
			
			//以这些成员为目的的转发点播
			List<CommandGroupForwardDemandPO> needDelDemands = commandCommonUtil.queryForwardDemandsByDstmemberIds(demands, removeMemberIds, null, null);
			//已成功，需要关闭编码器的转发点播
			List<CommandGroupForwardDemandPO> needDelDemandsForEncoder = commandCommonUtil.queryForwardDemandsByDstmemberIds(demands, removeMemberIds, null, ForwardDemandStatus.DONE);
			//直接删除转发点播
			demands.removeAll(needDelDemands);
			
			//这两个变量用来生成message用
			StringBufferWrapper removeMembersNames = new StringBufferWrapper();
			JSONArray removeMemberIdsJSONArray = new JSONArray();
			for(CommandGroupMemberPO removeMember : removeMembers){
				removeMembersNames.append(removeMember.getUserName()).append("，");
				removeMemberIdsJSONArray.add(removeMember.getId().toString());
			}
			removeMembersNames.append("被从 ").append(group.getName()).append(" 中移除");
			
			//释放这些退出或删除成员的播放器，同时如果是删人就给被删的成员发消息
			List<CommandGroupUserPlayerPO> needFreePlayers = new ArrayList<CommandGroupUserPlayerPO>();
			List<CommandGroupUserPlayerPO> playFilePlayers = new ArrayList<CommandGroupUserPlayerPO>();
			List<CommandGroupMemberPO> needCloseRemoveMembers = new ArrayList<CommandGroupMemberPO>();
			for(CommandGroupMemberPO removeMember : removeMembers){
				
				//只有进入的成员，才需要挂断编码器
				if(MemberStatus.CONNECT.equals(removeMember.getMemberStatus())){
					needCloseRemoveMembers.add(removeMember);
				}
				
				JSONArray splits = new JSONArray();
				List<CommandGroupUserPlayerPO> thisMemberFreePlayers = new ArrayList<CommandGroupUserPlayerPO>();
				for(CommandGroupUserPlayerPO player : removeMember.getPlayers()){
					if(player.playingFile()){
						playFilePlayers.add(player);
					}
					player.setFree();
					needFreePlayers.add(player);
//					removeMember.getPlayers().remove(player);
					thisMemberFreePlayers.add(player);
					
					//给退出成员返回的splits数组，后续优化
					JSONObject split = new JSONObject();
					split.put("serial", player.getLocationIndex());
					splits.add(split);
					exitMemberSplits.add(split);
				}
				removeMember.getPlayers().removeAll(thisMemberFreePlayers);
				
				//如果是删人，发消息
				if(mode == 1 && !group.getStatus().equals(GroupStatus.STOP)){
					JSONObject message = new JSONObject();
					message.put("businessType", "commandMemberDelete");
					message.put("businessId", group.getId().toString());
					message.put("businessInfo", "您已被移出 " + group.getName());
					message.put("memberIds", removeMemberIdsJSONArray);
					message.put("splits", splits);
					messageCaches.add(new MessageSendCacheBO(removeMember.getUserId(), message.toJSONString(), WebsocketMessageType.COMMAND, chairmanMember.getUserId(), chairmanMember.getUserName()));
				}
			}
			
			//释放其它成员的播放器，同时发消息
			for(CommandGroupMemberPO member : members){
				List<CommandGroupUserPlayerPO> thisMemberFreePlayers = new ArrayList<CommandGroupUserPlayerPO>();
				if(removeMemberIds.contains(member.getId())){
					//这里不处理退出和删除的成员
					continue;
				}
				JSONArray splits = new JSONArray();
				for(CommandGroupForwardPO forward : needDelForwards){
					if(removeMemberIds.contains(forward.getDstMemberId())){
						//该成员的播放器已经在上边释放
						continue;
					}
					if(member.getId().equals(forward.getDstMemberId())){
						//目的是该成员的，找播放器
						for(CommandGroupUserPlayerPO player : member.getPlayers()){
							if(player.getBundleId().equals(forward.getDstVideoBundleId())){
								player.setFree();
								needFreePlayers.add(player);
								thisMemberFreePlayers.add(player);
//								member.getPlayers().remove(player);
								
								JSONObject split = new JSONObject();
								split.put("serial", player.getLocationIndex());
								splits.add(split);
								if(member.isAdministrator()){
									//给主席的split									
									chairSplits.add(split);
								}
							}
						}
					}
				}
				member.getPlayers().removeAll(thisMemberFreePlayers);
				
				//发消息				
				if(mode == 0){
					//退出，成员下线消息，这里默认认为removeMembers只有一个元素
					JSONObject message = new JSONObject();
					message.put("businessType", "commandMemberOffline");
					message.put("businessId", group.getId().toString());
					message.put("businessInfo", removeMembers.get(0).getUserName() + " 成员退出");
					message.put("memberId", removeMembers.get(0).getId().toString());
					message.put("splits", splits);
					messageCaches.add(new MessageSendCacheBO(member.getUserId(), message.toJSONString(), WebsocketMessageType.COMMAND, chairmanMember.getUserId(), chairmanMember.getUserName()));
				}else if(mode ==1){
					//删人，强退成员消息。不用给主席发
					if(!member.isAdministrator()){
						JSONObject message = new JSONObject();
						message.put("businessType", "commandMemberDelete");
						message.put("businessId", group.getId().toString());
						message.put("businessInfo", removeMembersNames.toString());
						message.put("memberIds", removeMemberIdsJSONArray);
						message.put("splits", splits);
						messageCaches.add(new MessageSendCacheBO(member.getUserId(), message.toJSONString(), WebsocketMessageType.COMMAND, chairmanMember.getUserId(), chairmanMember.getUserName()));
					}
				}
			}
			
			if(mode == 0){
				//退出
				List<CommandGroupForwardPO> needRemoveCooForwards = new ArrayList<CommandGroupForwardPO>();
				for(CommandGroupMemberPO removeMember : removeMembers){
					removeMember.setMemberStatus(MemberStatus.DISCONNECT);
					removeMember.setCooperateStatus(MemberStatus.DISCONNECT);
					removeMember.setSilenceToHigher(false);
					removeMember.setSilenceToLower(false);
				}
				//所有的forward都保留（目前有普通和协同2种，协同的需要删除）
				for(CommandGroupForwardPO forward : needDelForwards){
					if(ForwardBusinessType.COOPERATE_COMMAND.equals(forward.getForwardBusinessType())
							&& removeMemberIds.contains(forward.getSrcMemberId())){
						needRemoveCooForwards.add(forward);
					}else{
						forward.clearDst();
						forward.setExecuteStatus(ExecuteStatus.UNDONE);
					}
				}
				forwards.removeAll(needRemoveCooForwards);
			}else if(mode == 1){
				//删人
				members.removeAll(removeMembers);
				forwards.removeAll(needDelForwards);
			}
			
			commandGroupDao.save(group);
			
			//会议进行中，发协议（删转发协议不用发，通过挂断播放器来删）
			if(!group.getStatus().equals(GroupStatus.STOP)){
				CommandGroupAvtplGearsPO currentGear = commandCommonUtil.queryCurrentGear(group);
				CodecParamBO codec = new CodecParamBO().set(group.getAvtpl(), currentGear);
				LogicBO logic = closeBundle(needCloseRemoveMembers, needDelDemandsForEncoder, needFreePlayers, codec, chairmanMember.getUserNum());
				LogicBO logicCastDevice = commandCastServiceImpl.closeBundleCastDevice(playFilePlayers, null, null, needFreePlayers, codec, group.getUserId());
				logic.merge(logicCastDevice);
				StringBufferWrapper description = new StringBufferWrapper().append(group.getName());
				if(mode == 0){//退出
					description.append(removeMembers.get(0).getUserName()).append(" 成员退出");
				}else if(mode == 1){//删人
					description = removeMembersNames;
				}
				
				//录制更新
				LogicBO logicRecord = commandRecordServiceImpl.update(group.getUserId(), group, 1, false);
				logic.merge(logicRecord);
				
				ExecuteBusinessReturnBO returnBO = executeBusiness.execute(logic, description.toString());
				commandRecordServiceImpl.saveStoreInfo(returnBO, group.getId());
			}

			//发消息
			for(MessageSendCacheBO cache : messageCaches){
				WebsocketMessageVO ws = websocketMessageService.send(cache.getUserId(), cache.getMessage(), cache.getType(), cache.getFromUserId(), cache.getFromUsername());
				consumeIds.add(ws.getId());
			}
			websocketMessageService.consumeAll(consumeIds);
			operationLogService.send(user.getNickname(), "删除成员", user.getNickname() + "删除成员groupId:" + groupId + "userIds:" + userIdList.toString());
			//退出，给成员返回exitMemberSplits；删人，给主席返回chairSplits
			if(mode == 0) return exitMemberSplits;
			return chairSplits;
		}
	}
	
	/**
	 * 
	 * 删除成员/成员退出<br/>
	 * <p>详细描述</p>
	 * <b>作者:</b>zsy<br/>
	 * <b>版本：</b>1.0<br/>
	 * <b>日期：</b>2019年11月12日 下午6:14:57
	 * @param groupId
	 * @param userIdList
	 * @param mode 0为主席同意的主动退出（列表和转发都保留），2为主席强退，删人，1为保留字段
	 * @return 1删人时给主席返回chairSplits；0退出时给退出成员返回exitMemberSplits；后续优化
	 * @throws Exception
	 */
	public Object removeMembers2(Long groupId, List<Long> userIdList, int mode) throws Exception{
		UserVO user = userQuery.current();
		//“重复退出会再次挂断编码器”已改好
		
		if(groupId==null || groupId.equals("")){
//			throw new BaseException(StatusCode.FORBIDDEN, "退出操作，会议id有误");
			log.info("退出或删人，会议id有误");
			return new JSONArray();
		}
		
		synchronized (new StringBuffer().append("command-group-").append(groupId).toString().intern()) {
			
			CommandGroupPO group = commandGroupDao.findOne(groupId);
			
			if(group == null){
//				throw new BaseException(StatusCode.FORBIDDEN, "会议不存在，id: " + groupId);
				log.info("进行退出/删人操作的会议不存在，id：" + groupId);
				return new JSONArray();
			}
			
			//会议停止状态下不能“退出”
			if(mode == 0){
				if(group.getStatus().equals(GroupStatus.STOP)){
//					throw new BaseException(StatusCode.FORBIDDEN, group.getName() + " 已停止，不需退出，id: " + group.getId());
					return new JSONArray();
				}
			}
			
			//防止专向指挥中调用此方法
			if(group.getType().equals(GroupType.SECRET)){
				throw new BaseException(StatusCode.FORBIDDEN, "正在专项，不能退出，只能“停止”");
			}
			
			JSONArray chairSplits = new JSONArray();
			JSONArray exitMemberSplits = new JSONArray();
			List<Long> consumeIds = new ArrayList<Long>();
			List<MessageSendCacheBO> messageCaches = new ArrayList<MessageSendCacheBO>();
			
			GroupType groupType = group.getType();
			List<CommandGroupMemberPO> members = group.getMembers();
			List<CommandGroupForwardPO> forwards = group.getForwards();
			List<CommandGroupForwardDemandPO> demands = group.getForwardDemands();
			CommandGroupMemberPO chairmanMember = commandCommonUtil.queryChairmanMember(members);
			
			//记录删人之前的用户列表
			List<MinfoBO> orgMemberInfos = commandCascadeUtil.generateMinfoBOList(members);
			
			if(userIdList.contains(chairmanMember.getUserId())){
				throw new BaseException(StatusCode.FORBIDDEN, group.getName() + "不能删除主席");
			}
			
			List<CommandGroupMemberPO> removeMembers = new ArrayList<CommandGroupMemberPO>();
			List<Long> removeMemberIds = new ArrayList<Long>();
			for(CommandGroupMemberPO member : members){
				if(userIdList.contains(member.getUserId())){
					removeMembers.add(member);
					removeMemberIds.add(member.getId());
				}
			}
			
			//以这些成员为源和目的的转发
			Set<CommandGroupForwardPO> needDelForwards = commandCommonUtil.queryForwardsByMemberIds(forwards, removeMemberIds, null, null);
			
			//以这些成员为目的的转发点播
			List<CommandGroupForwardDemandPO> needDelDemands = commandCommonUtil.queryForwardDemandsByDstmemberIds(demands, removeMemberIds, null, null);
			//已成功，需要关闭编码器的转发点播
			List<CommandGroupForwardDemandPO> needDelDemandsForEncoder = commandCommonUtil.queryForwardDemandsByDstmemberIds(demands, removeMemberIds, null, ForwardDemandStatus.DONE);
			//直接删除转发点播
			demands.removeAll(needDelDemands);
			
			//这两个变量用来生成message用
			StringBufferWrapper removeMembersNames = new StringBufferWrapper();
			JSONArray removeMemberIdsJSONArray = new JSONArray();
			for(CommandGroupMemberPO removeMember : removeMembers){
				removeMembersNames.append(removeMember.getUserName()).append("，");
				removeMemberIdsJSONArray.add(removeMember.getId().toString());
			}
			removeMembersNames.append("被从 ").append(group.getName()).append(" 中移除");
			
			//释放这些退出或删除成员的播放器，同时如果是删人就给被删的成员发消息
			List<CommandGroupUserPlayerPO> needFreePlayers = new ArrayList<CommandGroupUserPlayerPO>();
			List<CommandGroupUserPlayerPO> playFilePlayers = new ArrayList<CommandGroupUserPlayerPO>();
			List<CommandGroupMemberPO> needCloseRemoveMembers = new ArrayList<CommandGroupMemberPO>();
			for(CommandGroupMemberPO removeMember : removeMembers){
				
				//只有进入的成员，才需要挂断编码器
				if(MemberStatus.CONNECT.equals(removeMember.getMemberStatus())){
					needCloseRemoveMembers.add(removeMember);
				}
				
				//会议进行中，统计播放器，给退出成员发消息
				if(!group.getStatus().equals(GroupStatus.STOP)){					
					
//					//如果操作人在本系统
//					if(!OriginType.OUTER.equals(chairmanMember.getOriginType())){
						
						//如果退出人在本系统，统计它的播放器，websocket通知
						if(!OriginType.OUTER.equals(removeMember.getOriginType())){
							JSONArray splits = new JSONArray();
							List<CommandGroupUserPlayerPO> thisMemberFreePlayers = new ArrayList<CommandGroupUserPlayerPO>();
							for(CommandGroupUserPlayerPO player : removeMember.getPlayers()){
								if(player.playingFile()){
									playFilePlayers.add(player);
								}
								player.setFree();
								needFreePlayers.add(player);
//								removeMember.getPlayers().remove(player);
								thisMemberFreePlayers.add(player);
								
								//给退出成员返回的splits数组，后续优化
								JSONObject split = new JSONObject();
								split.put("serial", player.getLocationIndex());
								splits.add(split);
								exitMemberSplits.add(split);
							}
							removeMember.getPlayers().removeAll(thisMemberFreePlayers);
							
							JSONObject message = new JSONObject();
							if(mode == 2){
								message.put("businessInfo", "您已被移出 " + group.getName());
								message.put("businessType", "commandMemberDelete");
							}else{
								message.put("businessInfo", group.getName() + " 主席同意，您已退出");
								message.put("businessType", "exitApplyAgree");
							}
							message.put("businessId", group.getId().toString());
//							message.put("memberIds", removeMemberIdsJSONArray);
							message.put("splits", splits);
							messageCaches.add(new MessageSendCacheBO(removeMember.getUserId(), message.toJSONString(), WebsocketMessageType.COMMAND));								
						}
						
						//如果业务进行中，且操作人在本系统
						if(!OriginType.OUTER.equals(chairmanMember.getOriginType())
								&& !GroupStatus.STOP.equals(group.getStatus())){
							
							//如果是申请退出被同意
							if(mode == 0){							
							
								//如果退出人在外部系统，级联通知发言人所在系统（该通知只发送，收到后不处理，通过“成员主动退出通知”来处理）							
								if(OriginType.OUTER.equals(removeMember.getOriginType())){
									if(GroupType.BASIC.equals(groupType)){
										GroupBO groupBO = commandCascadeUtil.exitCommandResponse(group, removeMember, "1");
										commandCascadeService.exitResponse(groupBO);
									}else if(GroupType.MEETING.equals(groupType)){
										GroupBO groupBO = commandCascadeUtil.exitMeetingResponse(group, removeMember, "1");
										conferenceCascadeService.exitResponse(groupBO);
									}
								}
								
								// “成员主动退出通知”给所有其它系统
								if(GroupType.BASIC.equals(groupType)){
									GroupBO groupBO = commandCascadeUtil.exitCommand(group, removeMember);
									commandCascadeService.exit(groupBO);
								}else if(GroupType.MEETING.equals(groupType)){
									GroupBO groupBO = commandCascadeUtil.exitMeeting(group, removeMember);
									conferenceCascadeService.exit(groupBO);
								}
							}
							//如果是主席强退
							else{
								// “主席强制退出通知”给所有其它系统
								if(GroupType.BASIC.equals(groupType)){
									GroupBO groupBO = commandCascadeUtil.exitCommand(group, removeMember);
									commandCascadeService.kikout(groupBO);
								}else if(GroupType.MEETING.equals(groupType)){
									GroupBO groupBO = commandCascadeUtil.exitMeeting(group, removeMember);
									conferenceCascadeService.kikout(groupBO);
								}
							}
						}
//					}
				}
			}
			
			//释放其它成员的播放器，同时发消息
			for(CommandGroupMemberPO member : members){
				
				if(OriginType.OUTER.equals(member.getOriginType())){
					continue;
				}
				
				List<CommandGroupUserPlayerPO> thisMemberFreePlayers = new ArrayList<CommandGroupUserPlayerPO>();
				if(removeMemberIds.contains(member.getId())){
					//这里不处理退出和删除的成员
					continue;
				}
				JSONArray splits = new JSONArray();
				for(CommandGroupForwardPO forward : needDelForwards){
					if(removeMemberIds.contains(forward.getDstMemberId())){
						//该成员的播放器已经在上边释放
						continue;
					}
					if(member.getId().equals(forward.getDstMemberId())){
						//目的是该成员的，找播放器
						for(CommandGroupUserPlayerPO player : member.getPlayers()){
							if(player.getBundleId().equals(forward.getDstVideoBundleId())){
								player.setFree();
								needFreePlayers.add(player);
								thisMemberFreePlayers.add(player);
//								member.getPlayers().remove(player);
								
								JSONObject split = new JSONObject();
								split.put("serial", player.getLocationIndex());
								splits.add(split);
								if(member.isAdministrator()){
									//给主席的split									
									chairSplits.add(split);
								}
							}
						}
					}
				}
				member.getPlayers().removeAll(thisMemberFreePlayers);
				
				//给其他成员发消息				
				if(!group.getStatus().equals(GroupStatus.STOP)){
					
					//不给主席发
					if(member.isAdministrator()){
						continue;
					}
					
					//退出，成员下线消息，这里默认认为removeMembers只有一个元素
					JSONObject message = new JSONObject();
					if(mode ==2){
						message.put("businessInfo", removeMembersNames.toString());
					}else{
						message.put("businessInfo", removeMembers.get(0).getUserName() + " 成员退出");
					}
					message.put("businessType", "commandMemberOffline");
					message.put("businessId", group.getId().toString());
					message.put("memberId", removeMembers.get(0).getId().toString());
					message.put("splits", splits);
					messageCaches.add(new MessageSendCacheBO(member.getUserId(), message.toJSONString(), WebsocketMessageType.COMMAND, chairmanMember.getUserId(), chairmanMember.getUserName()));
				}
			}
			
			if(mode == 0){
				//成员主动退出
				List<CommandGroupForwardPO> needRemoveCooForwards = new ArrayList<CommandGroupForwardPO>();
				for(CommandGroupMemberPO removeMember : removeMembers){
					removeMember.setMemberStatus(MemberStatus.DISCONNECT);
					removeMember.setCooperateStatus(MemberStatus.DISCONNECT);
					removeMember.setSilenceToHigher(false);
					removeMember.setSilenceToLower(false);
				}
				//所有的forward都保留（目前有普通和协同2种，协同的需要删除）
				for(CommandGroupForwardPO forward : needDelForwards){
					if(ForwardBusinessType.COOPERATE_COMMAND.equals(forward.getForwardBusinessType())
							&& removeMemberIds.contains(forward.getSrcMemberId())){
						needRemoveCooForwards.add(forward);
					}else{
						forward.clearDst();
						forward.setExecuteStatus(ExecuteStatus.UNDONE);
					}
				}
				forwards.removeAll(needRemoveCooForwards);
			}else if(mode == 2){
				//主席强退，删人
				members.removeAll(removeMembers);
				forwards.removeAll(needDelForwards);
			}
			
			commandGroupDao.save(group);
			
			//会议进行中，发协议（删转发协议不用发，通过挂断播放器来删）
			if(!group.getStatus().equals(GroupStatus.STOP)){
				CommandGroupAvtplGearsPO currentGear = commandCommonUtil.queryCurrentGear(group);
				CodecParamBO codec = new CodecParamBO().set(group.getAvtpl(), currentGear);
				LogicBO logic = closeBundle(needCloseRemoveMembers, needDelDemandsForEncoder, needFreePlayers, codec, chairmanMember.getUserNum());
				LogicBO logicCastDevice = commandCastServiceImpl.closeBundleCastDevice(playFilePlayers, null, null, needFreePlayers, codec, group.getUserId());
				logic.merge(logicCastDevice);
				StringBufferWrapper description = new StringBufferWrapper().append(group.getName());
				if(mode == 0){//退出
					description.append(removeMembers.get(0).getUserName()).append(" 成员退出");
				}else if(mode == 2){//删人
					description = removeMembersNames;
				}
				
				//录制更新
				LogicBO logicRecord = commandRecordServiceImpl.update(group.getUserId(), group, 1, false);
				logic.merge(logicRecord);
				
				ExecuteBusinessReturnBO returnBO = executeBusiness.execute(logic, description.toString());
				commandRecordServiceImpl.saveStoreInfo(returnBO, group.getId());
			}
			
			//级联：如果有老节点，则给它停会删会；业务停止时给剩余成员的所有节点发update
			if(!OriginType.OUTER.equals(group.getOriginType())){
				
				//最终的用户列表
				List<MinfoBO> finalMemberInfos = commandCascadeUtil.generateMinfoBOList(members);
				//不再参与业务的节点上的用户列表
				List<MinfoBO> oldNodeMemberInfos = commandCascadeUtil.filterAddedNodeMinfo(finalMemberInfos, orgMemberInfos);
				
				if(GroupType.BASIC.equals(groupType)){
					
					//业务停止时给剩余成员的所有节点发update
					if(group.getStatus().equals(GroupStatus.STOP)){
						GroupBO groupBO = commandCascadeUtil.updateCommand(group);
						commandCascadeService.update(groupBO);
					}
					
					//如果有老节点，则给它停会删会
					if(oldNodeMemberInfos.size() > 0){
						
						if(!group.getStatus().equals(GroupStatus.STOP)){
							//停指挥
							Thread.sleep(300);//延时一下，确保其它节点删人操作已完成
							GroupBO groupBO1 = commandCascadeUtil.stopCommand(group);
							groupBO1.setMlist(oldNodeMemberInfos);
							commandCascadeService.stop(groupBO1);
						}
						
						//删除
						Thread.sleep(300);//延时一下，确保其它节点上一步操作已完成
						GroupBO groupBO = commandCascadeUtil.deleteCommand(group);
						groupBO.setMlist(oldNodeMemberInfos);
						commandCascadeService.delete(groupBO);
						log.info(oldNodeMemberInfos.size() + "个成员所在的节点不再参与指挥，已删除节点上的指挥：" + group.getName());
					}					
				}else if(GroupType.MEETING.equals(groupType)){
					
					//业务停止时给剩余成员的所有节点发update
					if(group.getStatus().equals(GroupStatus.STOP)){						
						GroupBO groupBO = commandCascadeUtil.updateMeeting(group);
						conferenceCascadeService.update(groupBO);
					}
			
					//如果有老节点，则给它停会删会
					if(oldNodeMemberInfos.size() > 0){
						
						if(!group.getStatus().equals(GroupStatus.STOP)){
							//停会
							Thread.sleep(300);//延时一下，确保其它节点删人操作已完成
							GroupBO groupBO2 = commandCascadeUtil.stopMeeting(group);
							groupBO2.setMlist(oldNodeMemberInfos);
							conferenceCascadeService.stop(groupBO2);
						}
						
						//删除
						Thread.sleep(300);//延时一下，确保其它节点上一步操作已完成
						GroupBO groupBO = commandCascadeUtil.deleteMeeting(group);
						groupBO.setMlist(oldNodeMemberInfos);
						conferenceCascadeService.delete(groupBO);
						log.info(oldNodeMemberInfos.size() + "个成员所在的节点不再参与会议，已删除节点上的会议：" + group.getName());
					}					
				}
			}

			//发消息
			for(MessageSendCacheBO cache : messageCaches){
				WebsocketMessageVO ws = websocketMessageService.send(cache.getUserId(), cache.getMessage(), cache.getType(), cache.getFromUserId(), cache.getFromUsername());
				consumeIds.add(ws.getId());
			}
			websocketMessageService.consumeAll(consumeIds);
			operationLogService.send(user.getNickname(), "成员退出", user.getNickname() + "成员退出groupId:" + groupId + "userIds:" + userIdList.toString());
			//所有情况都给主席返回chairSplits
			//if(mode == 0) return exitMemberSplits;
			return chairSplits;
		}
	}

	public void exitApply(Long userId, Long groupId) throws Exception{
		
		UserVO user = userQuery.current();
		if(groupId==null || groupId.equals("")){
			log.info("申请退出，会议id有误");
			return;
		}
		
		synchronized (new StringBuffer().append("command-group-").append(groupId).toString().intern()) {
			
			CommandGroupPO group = commandGroupDao.findOne(groupId);
			GroupType groupType = group.getType();
			
			if(group.getStatus().equals(GroupStatus.STOP)){
				if(!OriginType.OUTER.equals(group.getOriginType())){
					throw new BaseException(StatusCode.FORBIDDEN, group.getName() + " 已停止");
				}else{
					return;
				}
			}
			
			if(group.getUserId().equals(userId)){
				throw new BaseException(StatusCode.FORBIDDEN, "主席不能退出");
			}
			
			List<CommandGroupMemberPO> members = group.getMembers();
			CommandGroupMemberPO chairmanMember = commandCommonUtil.queryChairmanMember(members);
			CommandGroupMemberPO exitMember = commandCommonUtil.queryMemberByUserId(members, userId);
			
			//如果主席和申请人都不在该系统，则不需要处理（正常不会出现）
			if(OriginType.OUTER.equals(chairmanMember.getOriginType())
					&& OriginType.OUTER.equals(exitMember.getOriginType())){
				log.info("主席和申请退出的人都不是该系统用户，主席：" + chairmanMember.getUserName() + " 退出用户：" + exitMember.getUserName());
				return;
			}
			
			if(exitMember.getMemberStatus().equals(MemberStatus.DISCONNECT)){
				throw new BaseException(StatusCode.FORBIDDEN, "您已经退出");
			}
			
			//级联
			if(!OriginType.OUTER.equals(chairmanMember.getOriginType())){
				//主席在该系统
				JSONObject message = new JSONObject();
				message.put("businessType", "exitApply");
				message.put("businessInfo", exitMember.getUserName() + "申请退出" + group.getName());
				message.put("businessId", group.getId().toString() + "-" + exitMember.getUserId());
				
				WebsocketMessageVO ws = websocketMessageService.send(chairmanMember.getUserId(), message.toJSONString(), WebsocketMessageType.COMMAND);
				websocketMessageService.consume(ws.getId());
			}else{
				//主席在外部系统（那么申请人在该系统）
				if(GroupType.BASIC.equals(groupType)){
					GroupBO groupBO = commandCascadeUtil.exitCommandRequest(group, exitMember);
					commandCascadeService.exitRequest(groupBO);
				}else if(GroupType.MEETING.equals(groupType)){
					GroupBO groupBO = commandCascadeUtil.exitMeetingRequest(group, exitMember);
					conferenceCascadeService.exitRequest(groupBO);
				}
			}
			
			log.info(group.getName() + "申请退出");
		}
		operationLogService.send(user.getNickname(), "申请退出", user.getNickname() + "申请退出groupId:" + groupId);
	}
	
	public void exitApplyDisagree(Long userId, Long groupId, List<Long> userIds) throws Exception{
		UserVO user = userQuery.current();
		
		if(groupId==null || groupId.equals("")){
			log.info("拒绝成员退出，会议id有误");
			return;
		}
		
		synchronized (new StringBuffer().append("command-group-").append(groupId).toString().intern()) {
			
			CommandGroupPO group = commandGroupDao.findOne(groupId);
			GroupType groupType = group.getType();
			
			if(group.getStatus().equals(GroupStatus.STOP) || userIds.size()==0){
				return;
			}
			
			List<Long> consumeIds = new ArrayList<Long>();
			List<MessageSendCacheBO> messageCaches = new ArrayList<MessageSendCacheBO>();			
			List<CommandGroupMemberPO> members = group.getMembers();
			CommandGroupMemberPO chairmanMember = commandCommonUtil.queryChairmanMember(members);
			List<CommandGroupMemberPO> exitMembers = commandCommonUtil.queryMembersByUserIds(members, userIds);
			JSONObject message = new JSONObject();
			message.put("businessType", "exitApplyDisagree");
			message.put("businessInfo", "主席不同意您退出");
			message.put("businessId", group.getId().toString());
			for(CommandGroupMemberPO exitMember : exitMembers){
				
				//如果退出人在本系统，websocket通知
				if(!OriginType.OUTER.equals(exitMember.getOriginType())){
					messageCaches.add(new MessageSendCacheBO(exitMember.getUserId(), message.toJSONString(), WebsocketMessageType.COMMAND));								
				}
				
				//如果操作人在本系统
				if(!OriginType.OUTER.equals(chairmanMember.getOriginType())){
					
					//如果退出人在外部系统，级联通知
					if(OriginType.OUTER.equals(exitMember.getOriginType())){
						if(GroupType.BASIC.equals(groupType)){
							GroupBO groupBO = commandCascadeUtil.exitCommandResponse(group, exitMember, "0");
							commandCascadeService.exitResponse(groupBO);
						}else if(GroupType.MEETING.equals(groupType)){
							GroupBO groupBO = commandCascadeUtil.exitMeetingResponse(group, exitMember, "0");
							conferenceCascadeService.exitResponse(groupBO);
						}
					}
				}
			}			
			
			for(MessageSendCacheBO cache : messageCaches){
				WebsocketMessageVO ws = websocketMessageService.send(cache.getUserId(), cache.getMessage(), cache.getType());
				consumeIds.add(ws.getId());
			}
			websocketMessageService.consumeAll(consumeIds);
			
			log.info(group.getName() + " 主席拒绝了 " + exitMembers.get(0).getUserName() + " 等人退出");
		}
		operationLogService.send(user.getNickname(), "拒绝申请退出", user.getNickname() + "拒绝申请退出groupId:" + groupId + ", userIds" + userIds.toString());
	}
	
	/**
	 * 批量处理成员的“接听”和“拒绝”<br/>
	 * <p>注意不能选择自己看自己的播放器，例如主席看主席</p>
	 * <b>作者:</b>zsy<br/>
	 * <b>版本：</b>1.0<br/>
	 * <b>日期：</b>2019年10月24日 上午11:13:43
	 * @param group
	 * @param acceptMembers
	 * @param refuseMembers
	 * @throws Exception
	 */
	private void membersResponse(CommandGroupPO group, List<CommandGroupMemberPO> acceptMembers, List<CommandGroupMemberPO> refuseMembers) throws Exception{
		
		if(null == acceptMembers) acceptMembers = new ArrayList<CommandGroupMemberPO>();
		if(null == refuseMembers) refuseMembers = new ArrayList<CommandGroupMemberPO>();
		List<Long> consumeIds = new ArrayList<Long>();
		List<MessageSendCacheBO> messageCaches = new ArrayList<MessageSendCacheBO>();
		
		//考虑如果停会之后执行，有没有问题
		
		//给新进入成员和主席选播放器
		chosePlayersForMembers(group, acceptMembers);//后选播放器
		
		//消息消费
		try {
			for(CommandGroupMemberPO acceptMember : acceptMembers){
				if(acceptMember.getMessageId() != null){
					consumeIds.add(acceptMember.getMessageId());
					acceptMember.setMessageId(null);
				}
			}			
			for(CommandGroupMemberPO refuseMember : refuseMembers){
				if(refuseMember.getMessageId() != null){
					consumeIds.add(refuseMember.getMessageId());
					refuseMember.setMessageId(null);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		//判断是否在进行
		if(GroupStatus.STOP.equals(group.getStatus())) {
			return;
		}
		
		//处理同意用户，呼叫转发目标成员的播放器
		List<CommandGroupMemberPO> members = group.getMembers();
		CommandGroupMemberPO chairmanMember = commandCommonUtil.queryChairmanMember(members);
		List<CommandGroupForwardPO> forwards = group.getForwards();
		List<Long> acceptMemberIds = new ArrayList<Long>();
		List<CommandGroupUserPlayerPO> allPlayers = new ArrayList<CommandGroupUserPlayerPO>();
		
		List<String> acceptMemberNamesList = new ArrayList<String>();
		
		for(CommandGroupMemberPO acceptMember : acceptMembers){
			acceptMemberIds.add(acceptMember.getId());
			acceptMemberNamesList.add(acceptMember.getUserName());
			if(acceptMember.getPlayers() != null){
				allPlayers.addAll(acceptMember.getPlayers());
			}
		}
		//save CONNECT状态
//		commandGroupDao.save(group);
		
		//自动接听：给新进的人发消息，通知其开会。在会议开启时，也会给主席发，通知split信息。【专向指挥除外】
		if(autoEnter && !group.getType().equals(GroupType.SECRET)){
			for(CommandGroupMemberPO acceptMember : acceptMembers){
				
				if(OriginType.OUTER.equals(acceptMember.getOriginType())){
					continue;
				}
				
				JSONObject message = new JSONObject();
				message.put("id", group.getId());
				message.put("name", group.getName());
				message.put("status", group.getStatus().getCode());
				message.put("commander", chairmanMember.getUserId());
				message.put("creator", chairmanMember.getUserId());
				List<BusinessPlayerVO> splits = new ArrayList<BusinessPlayerVO>();
				for(CommandGroupUserPlayerPO player : acceptMember.getPlayers()){
					BusinessPlayerVO split = new BusinessPlayerVO().set(player);
					splits.add(split);
				}
				message.put("splits", splits);
				message.put("businessId", group.getId().toString());
				String businessType = null;
				if(GroupType.MEETING.equals(group.getType())){
					businessType = "meetingStartNow";//自动接听
				}else{
					businessType = "commandStartNow";//自动接听
				}
				message.put("businessType", businessType);
				message.put("businessInfo", group.getName() + " 开始了，主席：" + chairmanMember.getUserName());
				messageCaches.add(new MessageSendCacheBO(acceptMember.getUserId(), message.toJSONString(), WebsocketMessageType.COMMAND));
			}
		}
		
		
		//给进会的所有人发送消息
//		GroupType groupType = group.getType();
		String acceptMemberNames = StringUtils.join(acceptMemberNamesList.toArray(), ",");
		if(acceptMembers.size() > 0){
			for(CommandGroupMemberPO member : members){
				
				if(OriginType.OUTER.equals(member.getOriginType())){
					continue;
				}
				
				if(!MemberStatus.CONNECT.equals(member.getMemberStatus())){
					continue;
				}
				//接听成员有主席时（通常是开启会议时），不给主席发
				if(acceptMemberIds.contains(chairmanMember.getId()) && member.isAdministrator()){
					continue;
				}
				
				JSONObject message = new JSONObject();
				message.put("businessType", "commandMemberOnline");
				message.put("businessId", group.getId().toString());
				message.put("businessInfo", acceptMemberNames + " 进入" + group.getName());
	//			message.put("memberId", acceptMember.getId().toString());//memberId这个参数并没有使用；况且这里不止一个member
				
				//这里把所有的播放器全部返回，不管是不是本次业务的
				List<BusinessPlayerVO> splits = new ArrayList<BusinessPlayerVO>();
				for(CommandGroupUserPlayerPO player : member.getPlayers()){
					BusinessPlayerVO split = new BusinessPlayerVO().set(player);
					splits.add(split);
				}
				message.put("splits", splits);
				messageCaches.add(new MessageSendCacheBO(member.getUserId(), message.toJSONString(), WebsocketMessageType.COMMAND));
			}
		}
		
		//发完消息再把成员状态置为CONNECT
		for(CommandGroupMemberPO acceptMember : acceptMembers){
			acceptMember.setMemberStatus(MemberStatus.CONNECT);
		}
		
		//处理拒绝用户，释放播放器
		List<Long> refuseMemberIds = new ArrayList<Long>();
		List<CommandGroupUserPlayerPO> needFreePlayers = new ArrayList<CommandGroupUserPlayerPO>();
		for(CommandGroupMemberPO refuseMember : refuseMembers){
			refuseMemberIds.add(refuseMember.getId());
			refuseMember.setMemberStatus(MemberStatus.DISCONNECT);
			
			if(OriginType.OUTER.equals(refuseMember.getOriginType())){
				continue;
			}
			
			List<CommandGroupUserPlayerPO> players = refuseMember.getPlayers();
			for(CommandGroupUserPlayerPO player : players){
				player.setFree();
				needFreePlayers.add(player);
			}
			refuseMember.getPlayers().removeAll(players);
//			commandGroupUserPlayerDao.save(players);//下边优化为批量save
		}
		
		//以拒绝成员为源和目的的转发
		Set<CommandGroupForwardPO> refuseForwards = commandCommonUtil.queryForwardsByMemberIds(forwards, refuseMemberIds, null, null);
		
		//释放其它成员的播放器
		for(CommandGroupForwardPO forward : refuseForwards){
			if(refuseMemberIds.contains(forward.getDstMemberId())){
				//该成员的播放器已经在上边释放
				forward.clearDst();
				continue;
			}
			//以下转发都是以删除的成员为源的
			if(forward.getExecuteStatus().equals(ExecuteStatus.UNDONE)
					|| forward.getExecuteStatus().equals(ExecuteStatus.DONE)){
				CommandGroupMemberPO dstMember = commandCommonUtil.queryMemberById(members, forward.getDstMemberId());
				if(OriginType.OUTER.equals(dstMember.getOriginType())){
					continue;
				}
				JSONArray splits = new JSONArray();
				List<CommandGroupUserPlayerPO> thisMemberFreePlayers = new ArrayList<CommandGroupUserPlayerPO>();
				for(CommandGroupUserPlayerPO player : dstMember.getPlayers()){
					if(player.getBundleId().equals(forward.getDstVideoBundleId())){
//							&& player.getPlayerBusinessType().equals(PlayerBusinessType.COOPERATE_COMMAND)){
						player.setFree();
						needFreePlayers.add(player);
						thisMemberFreePlayers.add(player);
						JSONObject split = new JSONObject();
						split.put("serial", player.getLocationIndex());
						splits.add(split);
					}
				}
				dstMember.getPlayers().removeAll(thisMemberFreePlayers);
				
				//发消息销毁播放器，这里默认认为removeMembers只有一个元素
				JSONObject message = new JSONObject();
				message.put("businessType", "commandMemberOffline");
				message.put("businessId", group.getId().toString());
				message.put("businessInfo", refuseMembers.get(0).getUserName() + " 拒绝进入");
				message.put("memberId", refuseMembers.get(0).getId().toString());				
				message.put("splits", splits);
				messageCaches.add(new MessageSendCacheBO(dstMember.getUserId(), message.toJSONString(), WebsocketMessageType.COMMAND, chairmanMember.getUserId(), chairmanMember.getUserName()));
			}
			forward.clearDst();
		}
		
		//save成员的CONNECT状态
		commandGroupUserPlayerDao.save(needFreePlayers);
		commandGroupDao.save(group);
		
		//查询接听用户的转发：源和目的成员都CONNECT的，且状态UNDONE的，生成logic.forwardSet
		Set<CommandGroupForwardPO> relativeForwards = commandCommonUtil.queryForwardsByMemberIds(forwards, acceptMemberIds, null, ExecuteStatus.UNDONE);
		Set<CommandGroupForwardPO> needForwards = commandCommonUtil.queryForwardsReadyAndCanBeDone(members, relativeForwards);
		
		for(CommandGroupForwardPO needForward : needForwards){
			needForward.setExecuteStatus(ExecuteStatus.DONE);
		}
		
		commandGroupDao.save(group);
		
		//生成connectBundle和disconnectBundle，携带转发信息
		CommandGroupAvtplGearsPO currentGear = commandCommonUtil.queryCurrentGear(group);
		CodecParamBO codec = new CodecParamBO().set(group.getAvtpl(), currentGear);
		LogicBO logic = openBundle(acceptMembers, null, allPlayers, needForwards, null, codec, chairmanMember.getUserNum());
		LogicBO logicDis = closeBundle(null, null, needFreePlayers, codec, chairmanMember.getUserNum());
		LogicBO logicCastDevice = commandCastServiceImpl.openBundleCastDevice(null, null, needForwards, null, null, null, codec, group.getUserId());
		logic.merge(logicDis);
		logic.merge(logicCastDevice);
		
		//停止其它业务观看专向会议的2个成员，在 CommandSecretServiceImpl.accept() 中
		
		//录制更新
		LogicBO logicRecord = commandRecordServiceImpl.update(group.getUserId(), group, 1, false);
		logic.merge(logicRecord);
		
		ExecuteBusinessReturnBO returnBO = executeBusiness.execute(logic, group.getName() + " 会议成员接听和拒绝");
		commandRecordServiceImpl.saveStoreInfo(returnBO, group.getId());

		//发消息
		for(MessageSendCacheBO cache : messageCaches){
			WebsocketMessageVO ws = websocketMessageService.send(cache.getUserId(), cache.getMessage(), cache.getType(), cache.getFromUserId(), cache.getFromUsername());
			consumeIds.add(ws.getId());
		}
		websocketMessageService.consumeAll(consumeIds);
	}
	
	/**
	 * 会议中观看某个成员<br/>
	 * <p>通常应用于“把某个成员关闭”之后再重新观看的场景，如果这个成员已经在播放则会抛错</p>
	 * <b>作者:</b>zsy<br/>
	 * <b>版本：</b>1.0<br/>
	 * <b>日期：</b>2019年12月3日 上午11:31:20
	 * @param userBO 因为可能调用commandVodService.seeOneselfStart()方法，所以使用UserBO做参数
	 * @param groupId
	 * @param memberUserId 被观看的成员
	 * @return
	 * @throws Exception
	 */
	public CommandGroupUserPlayerPO vodMemberStart(UserBO userBO, Long groupId, Long memberUserId) throws Exception{
		UserVO user = userQuery.current();
		synchronized (new StringBuffer().append("command-group-").append(groupId).toString().intern()) {
			
			CommandGroupPO group = commandGroupDao.findOne(groupId);
			Long userId = userBO.getId();
			if(group.getStatus().equals(GroupStatus.STOP)){
				throw new BaseException(StatusCode.FORBIDDEN, group.getName() + " 已停止，无法操作，id: " + group.getId());
			}
			if(!group.getUserId().equals(userId)){
				throw new BaseException(StatusCode.FORBIDDEN, "只有主席才能点播成员");
			}
			List<CommandGroupMemberPO> members = group.getMembers();
			CommandGroupMemberPO chairmanMember = commandCommonUtil.queryChairmanMember(members);
			CommandGroupMemberPO srcMember = commandCommonUtil.queryMemberByUserId(members, memberUserId);
			if(srcMember.getMemberStatus().equals(MemberStatus.DISCONNECT)){
				throw new BaseException(StatusCode.FORBIDDEN, srcMember.getUserName() + " 未进入");
			}
			//如果点播自己，则创建一个看自己的编码器的“本地视频”点播用户业务
			if(memberUserId.equals(userId)){
//				throw new BaseException(StatusCode.FORBIDDEN, "请观看其它成员");
				UserBO admin = new UserBO();admin.setId(-1L);
				return commandVodService.seeOneselfUserStart(userBO, admin, true);
			}
			
			//找到转发，如果已经执行则抛错			
			List<CommandGroupForwardPO> forwards = group.getForwards();
			CommandGroupMemberPO dstMember = commandCommonUtil.queryMemberByUserId(members, userId);
			CommandGroupForwardPO forward = commandCommonUtil.queryForwardBySrcAndDstMemberId(forwards, srcMember.getId(), dstMember.getId());
			ExecuteStatus executeStatus = forward.getExecuteStatus();
			if(executeStatus.equals(ExecuteStatus.DONE)){
				throw new BaseException(StatusCode.FORBIDDEN, "该成员已经在播放");
			}
			
			//查找播放器，找不到则自行抛错
			CommandGroupUserPlayerPO player = commandCommonUtil.queryPlayerByForwardAndDstMember(forward, dstMember);
			if(player == null){
				player = commandCommonServiceImpl.userChoseUsefulPlayer(userId, PlayerBusinessType.CHAIRMAN_BASIC_COMMAND);
				forward.setDstPlayer(player);			
				player.setBusinessId(group.getId().toString() + "-" + srcMember.getUserId());
				player.setBusinessName(group.getName() + "：" + srcMember.getUserName());//添加成员名称
				player.setPlayerBusinessType(PlayerBusinessType.CHAIRMAN_BASIC_COMMAND);
				dstMember.getPlayers().add(player);
				player.setMember(dstMember);
			}
			
			//源成员CONNECT，且没有互斥业务，才可以执行
			boolean canDo = false;
			if(srcMember.getMemberStatus().equals(MemberStatus.CONNECT)
					&& commandCommonServiceImpl.whetherCanBeDone(forward)){
				canDo = true;
			}
			if(canDo){
				forward.setExecuteStatus(ExecuteStatus.DONE);
			}else{
				forward.setExecuteStatus(ExecuteStatus.UNDONE);
				commandGroupDao.save(group);
				log.info(srcMember.getUserName() + " 观看 " + dstMember.getUserName() + "，该转发因为其它业务互斥，暂时没有执行");
				return player;
			}
			
			CommandGroupAvtplGearsPO currentGear = commandCommonUtil.queryCurrentGear(group);
			CodecParamBO codec = new CodecParamBO().set(group.getAvtpl(), currentGear);
			List<CommandGroupUserPlayerPO> allPlayers = new ArrayListWrapper<CommandGroupUserPlayerPO>().add(player).getList();
			Set<CommandGroupForwardPO> needForwards = new HashSetWrapper<CommandGroupForwardPO>().add(forward).getSet();
			LogicBO logic = openBundle(null, null, allPlayers, needForwards, null, codec, chairmanMember.getUserNum());
			LogicBO logicCastDevice = commandCastServiceImpl.openBundleCastDevice(null, null, needForwards, null, null, null, codec, group.getUserId());
			logic.merge(logicCastDevice);			
			executeBusiness.execute(logic, dstMember.getUserName() + " 观看 " + srcMember.getUserName());
			operationLogService.send(user.getNickname(), "主席开始观看成员", user.getNickname() + "主席开始观看成员groupId:" + groupId + ", userId:" + memberUserId);
			return player;
		}
	}
	
	/**
	 * 会议中停止观看某个成员<br/>
	 * <p>详细描述</p>
	 * <b>作者:</b>zsy<br/>
	 * <b>版本：</b>1.0<br/>
	 * <b>日期：</b>2019年12月3日 上午11:33:45
	 * @param userId 操作人
	 * @param groupId
	 * @param memberUserId 被停止观看的成员userId
	 * @return
	 * @throws Exception
	 */
	public CommandGroupUserPlayerPO vodMemberStop(Long userId, Long groupId, Long memberUserId) throws Exception{
		UserVO user = userQuery.current();
		synchronized (new StringBuffer().append("command-group-").append(groupId).toString().intern()) {
			
			CommandGroupPO group = commandGroupDao.findOne(groupId);
			if(group.getStatus().equals(GroupStatus.STOP)){
				throw new BaseException(StatusCode.FORBIDDEN, group.getName() + " 已停止，无法操作，id: " + group.getId());
			}
//			if(!group.getUserId().equals(userId)){
//				throw new BaseException(StatusCode.FORBIDDEN, "只有主席才能关闭成员");
//			}
			
			//找到转发，释放和关闭播放器
			List<CommandGroupUserPlayerPO> needClosePlayers = new ArrayList<CommandGroupUserPlayerPO>();
			List<CommandGroupMemberPO> members = group.getMembers();
			CommandGroupMemberPO chairmanMember = commandCommonUtil.queryChairmanMember(members);
			List<CommandGroupForwardPO> forwards = group.getForwards();
			CommandGroupMemberPO dstMember = commandCommonUtil.queryMemberByUserId(members, userId);
			CommandGroupMemberPO srcMember = commandCommonUtil.queryMemberByUserId(members, memberUserId);
			CommandGroupForwardPO forward = commandCommonUtil.queryForwardBySrcAndDstMemberId(forwards, srcMember.getId(), dstMember.getId());
			CommandGroupUserPlayerPO player = commandCommonUtil.queryPlayerByForwardAndDstMember(forward, dstMember);
			if(player != null){				
				forward.clearDst();
				player.setFree();
				needClosePlayers.add(player);				
			}
			forward.setExecuteStatus(ExecuteStatus.NO_AVAILABLE_PLAYER);
			
			commandGroupDao.save(group);
			commandGroupUserPlayerDao.save(needClosePlayers);			
			
			CommandGroupAvtplGearsPO currentGear = commandCommonUtil.queryCurrentGear(group);
			CodecParamBO codec = new CodecParamBO().set(group.getAvtpl(), currentGear);
			LogicBO logic = closeBundle(null, null, needClosePlayers, codec, chairmanMember.getUserNum());
			LogicBO logicCastDevice = commandCastServiceImpl.closeBundleCastDevice(null, null, null, needClosePlayers, codec, group.getUserId());
			logic.merge(logicCastDevice);
			executeBusiness.execute(logic, dstMember.getUserName() + " 停止观看 " + srcMember.getUserName());
			
			if(needClosePlayers.size() > 0){
				return needClosePlayers.get(0);
			}
			
			operationLogService.send(user.getNickname(), dstMember.getUserName() + "停止观看", dstMember.getUserName() + "停止观看成员groupId:" + groupId + ",userId:" + memberUserId);
			return null;
		}
	}
	
	/**
	 * 判断一条转发是否因为会议的暂停而暂停<br/>
	 * <p>详细描述</p>
	 * <b>作者:</b>zsy<br/>
	 * <b>版本：</b>1.0<br/>
	 * <b>日期：</b>2019年11月5日 下午3:16:37
	 * @param forward
	 * @return
	 */
	public boolean whetherStopForCommandPause(CommandGroupForwardPO forward){		
		if(forward.getGroup().getStatus().equals(GroupStatus.PAUSE)){
			return true;
		}else{
			return false;
		}
	}
	
	/**
	 * 执行一个会议中所有可以执行的转发<br/>
	 * <p>线程不安全，调用处必须使用 command-group-{groupId} 加锁</p>
	 * <b>作者:</b>zsy<br/>
	 * <b>版本：</b>1.0<br/>
	 * <b>日期：</b>2019年11月7日 下午1:24:43
	 * @param group 注意传入前应先把group、members、forwards的状态save正确
	 * @param doPersistence 是否持久化转发的执行状态为UNDONE，通常使用true
	 * @param doProtocol 是否下发协议
	 * @return
	 * @throws Exception
	 */
	public LogicBO startGroupForwards(CommandGroupPO group, boolean doPersistence, boolean doProtocol) throws Exception{
		List<CommandGroupMemberPO> members = group.getMembers();
		CommandGroupMemberPO chairmanMember = commandCommonUtil.queryChairmanMember(members);
		List<CommandGroupForwardPO> forwards = group.getForwards();
		Set<CommandGroupForwardPO> needForwards = commandCommonUtil.queryForwardsReadyAndCanBeDone(members, forwards);
		for(CommandGroupForwardPO needForward : needForwards){
			needForward.setExecuteStatus(ExecuteStatus.DONE);
		}
		
		if(doPersistence) commandGroupDao.save(group);
		
		//生成forwardSet的logic
		CommandGroupAvtplGearsPO currentGear = commandCommonUtil.queryCurrentGear(group);
		CodecParamBO codec = new CodecParamBO().set(group.getAvtpl(), currentGear);
		LogicBO logic = openBundle(null, null, null, needForwards, null, codec, chairmanMember.getUserNum());		
		LogicBO logicCastDevice = commandCastServiceImpl.openBundleCastDevice(null, null, needForwards, null, null, null, codec, group.getUserId());
		logic.merge(logicCastDevice);
		
		//录制更新
		LogicBO logicRecord = commandRecordServiceImpl.update(group.getUserId(), group, 1, false);
		logic.merge(logicRecord);
				
		if(doProtocol){
			ExecuteBusinessReturnBO returnBO = executeBusiness.execute(logic, "执行 " + group.getName() + " 会议中的转发，共" + needForwards.size() + "个");
			commandRecordServiceImpl.saveStoreInfo(returnBO, group.getId());
		}
		
		return logic;
	}
	
	/**
	 * 执行所有会议中所有可以执行的转发<br/>
	 * <p>详细描述</p>
	 * <b>作者:</b>zsy<br/>
	 * <b>版本：</b>1.0<br/>
	 * <b>日期：</b>2019年11月7日 下午2:04:47
	 * @param exceptGroupIds 排除的groupId列表，防止停止自己会议的转发
	 * @param doPersistence 是否持久化转发的执行状态为UNDONE，通常使用true
	 * @param doProtocol 是否下发协议：因为每个会议需要单独下发协议，不能合并，所以此处应使用true
	 * @throws Exception
	 */
	public void startAllGroupForwards(List<Long> exceptGroupIds, boolean doPersistence, boolean doProtocol) throws Exception{
		
		if(null == exceptGroupIds) exceptGroupIds = new ArrayList<Long>();
		List<Long> ids = commandGroupDao.findAllIds();
		for(Long groupId : ids){
			
			if(exceptGroupIds.contains(groupId)){
				continue;
			}
			
			synchronized (new StringBuffer().append("command-group-").append(groupId).toString().intern()) {				
				CommandGroupPO group = commandGroupDao.findOne(groupId);
				GroupStatus status = group.getStatus();
				if(status.equals(GroupStatus.START) || status.equals(GroupStatus.REMIND)){
					startGroupForwards(group, doPersistence, doProtocol);				
				}
			}
		}
	}

	/**
	 * 停止所有会议中，特定源的转发，同时也会停止相关录制<br/>
	 * <p>通常用于专向会议建立时，专向会议的2个成员不能被其它业务看到</p>
	 * <b>作者:</b>zsy<br/>
	 * <b>版本：</b>1.0<br/>
	 * <b>日期：</b>2019年11月7日 上午9:43:29
	 * @param exceptGroupIds 排除的groupId列表，防止停止自己会议的转发
	 * @param srcMemberIds 源成员id列表
	 * @param doPersistence 是否持久化转发的执行状态为UNDONE，通常使用true
	 * @param doProtocol 是否下发协议：因为每个会议需要单独下发协议，不能合并，所以此处应使用true
	 * @return
	 * @throws Exception
	 */
	public void stopAllGroupForwardsBySrcMemberIds(List<Long> exceptGroupIds, List<Long> srcUserIds, boolean doPersistence, boolean doProtocol) throws Exception{
		
		if(null == exceptGroupIds) exceptGroupIds = new ArrayList<Long>();
		List<Long> ids = commandGroupDao.findAllIds();
		for(Long groupId : ids){
			
			if(exceptGroupIds.contains(groupId)){
				continue;
			}
			
			synchronized (new StringBuffer().append("command-group-").append(groupId).toString().intern()) {
				
				CommandGroupPO group = commandGroupDao.findOne(groupId);
				if(group.getStatus().equals(GroupStatus.PAUSE) || group.getStatus().equals(GroupStatus.STOP)){
					continue;
				}
				
				//查找需要停止的转发
				List<CommandGroupForwardPO> forwards = group.getForwards();
				List<CommandGroupMemberPO> members = group.getMembers();
				CommandGroupMemberPO chairmanMember = commandCommonUtil.queryChairmanMember(members);
				List<CommandGroupMemberPO> srcMembers = commandCommonUtil.queryMembersByUserIds(members, srcUserIds);
				List<Long> srcMemberIds = new ArrayList<Long>();
				for(CommandGroupMemberPO srcMember : srcMembers){
					srcMemberIds.add(srcMember.getId());
				}
				Set<CommandGroupForwardPO> needDelForwards = commandCommonUtil.queryForwardsBySrcmemberIds(forwards, srcMemberIds, null, ExecuteStatus.DONE);
				if(needDelForwards.size() == 0){
					continue;
				}
				for(CommandGroupForwardPO needDelForward : needDelForwards){
					needDelForward.setExecuteStatus(ExecuteStatus.UNDONE);
				}
				
				CommandGroupAvtplGearsPO currentGear = commandCommonUtil.queryCurrentGear(group);
				CodecParamBO codec = new CodecParamBO().set(group.getAvtpl(), currentGear);
				LogicBO logic1 = openBundle(null, null, null, null, needDelForwards, codec, chairmanMember.getUserNum());
				LogicBO logicCastDevice = commandCastServiceImpl.openBundleCastDevice(null, null, null, needDelForwards, null, null, codec, group.getUserId());
				logic1.merge(logicCastDevice);
//				logic.merge(logic1);
				
				//录制更新
				LogicBO logicRecord = commandRecordServiceImpl.update(group.getUserId(), group, 1, false);
				logic1.merge(logicRecord);
				
				if(doPersistence) commandGroupDao.save(group);
				
				if(doProtocol){
					ExecuteBusinessReturnBO returnBO = executeBusiness.execute(logic1, "停止 " + group.getName() + "会议 特定源的转发，共" + needDelForwards.size() + "个");
					commandRecordServiceImpl.saveStoreInfo(returnBO, group.getId());
				}
			}
		}
//		if(doProtocol){
//			executeBusiness.execute(logic, "停止所有会议中的 特定源的转发");
//		}		
//		return logic;
	}
	
	/**
	 * 生成携带转发的呼叫协议<br/>
	 * <b>作者:</b>zsy<br/>
	 * <b>版本：</b>1.0<br/>
	 * <b>日期：</b>2019年10月22日
	 * @param membersForEncoder 成员列表，仅用于呼叫编码器。仅应该在成员进会的时候呼叫，否则会多计数
	 * @param demandsForEncoderAndForward 【仅当目的成员是INNER的时候才会处理】转发点播列表，生成2条协议：呼叫其中的编码器，以及生成编码器给解码器的转发forwardSet
	 * @param players 播放器列表，方法中会判断播放器的业务属性，如果不是实时流业务则不生成logic协议
	 * @param forwards 转发列表，生成forwardSet，在逻辑层被转换为connectBundle
	 * @param delForwards 删除转发列表，生成forwardDel，在逻辑层被转换为connectBundle
	 * @param codec
	 * @param chairmanUserNum 主席的号码
	 * @return LogicBO
	 * @throws Exception 
	 */
	public LogicBO openBundle(
			List<CommandGroupMemberPO> membersForEncoder,
			List<CommandGroupForwardDemandPO> demandsForEncoderAndForward,
			List<CommandGroupUserPlayerPO> players,
			Set<CommandGroupForwardPO> forwards,
			Set<CommandGroupForwardPO> delForwards,
			CodecParamBO codec,
			String chairmanUserNum) throws Exception{
		
//		UserBO admin = resourceService.queryUserInfoByUsername(CommandCommonConstant.USER_NAME);
		UserBO admin = new UserBO(); admin.setId(-1L);
		String localLayerId = null;
		
		LogicBO logic = new LogicBO().setUserId(admin.getId().toString())
		 		 .setConnectBundle(new ArrayList<ConnectBundleBO>())
		 		 .setForwardSet(new ArrayList<ForwardSetBO>())
		 		 .setForwardDel(new ArrayList<ForwardDelBO>())
		 		 .setPass_by(new ArrayList<PassByBO>());
		
		//呼叫编码器
		if(null == membersForEncoder) membersForEncoder = new ArrayList<CommandGroupMemberPO>();
		for(CommandGroupMemberPO member : membersForEncoder){
			if(!OriginType.OUTER.equals(member.getOriginType())){
				ConnectBundleBO connectVideoBundle = new ConnectBundleBO().setBusinessType(ConnectBundleBO.BUSINESS_TYPE_VOD)
						          .setOperateType(ConnectBundleBO.OPERATE_TYPE)
								  .setLock_type("write")
								  .setBundleId(member.getSrcBundleId())
								  .setLayerId(member.getSrcLayerId())
								  .setBundle_type(member.getSrcVenusBundleType());
				ConnectBO connectVideoChannel = new ConnectBO().setChannelId(member.getSrcVideoChannelId())
					   .setChannel_status("Open")
					   .setBase_type("VenusVideoIn")
					   .setCodec_param(codec);
				connectVideoBundle.setChannels(new ArrayListWrapper<ConnectBO>().add(connectVideoChannel).getList());
				logic.getConnectBundle().add(connectVideoBundle);
				
				if(member.getSrcAudioChannelId()!=null && !member.getSrcAudioChannelId().equals("")){
						ConnectBO connectAudioChannel = new ConnectBO().setChannelId(member.getSrcAudioChannelId())
																	   .setChannel_status("Open")
																	   .setBase_type("VenusAudioIn")
																	   .setCodec_param(codec);
						connectVideoBundle.getChannels().add(connectAudioChannel);
				}
			}else{
				//呼叫外部系统用户，相当于点播外部用户，passby拉流
				if(localLayerId == null){
					localLayerId = resourceRemoteService.queryLocalLayerId();
				}
				XtBusinessPassByContentBO passByContent = new XtBusinessPassByContentBO().setCmd(XtBusinessPassByContentBO.CMD_LOCAL_SEE_XT_USER)
									 .setOperate(XtBusinessPassByContentBO.OPERATE_START)
									 .setUuid(member.getUuid())
									 .setSrc_user(chairmanUserNum)
									 .setXt_encoder(new HashMapWrapper<String, String>().put("layerid", member.getSrcLayerId())
											 											.put("bundleid", member.getSrcBundleId())
											 											.put("video_channelid", member.getSrcVideoChannelId())
											 											.put("audio_channelid", member.getSrcAudioChannelId())
											 											.getMap())
									 .setDst_number(member.getUserNum())
									 .setVparam(codec);
				
				PassByBO passby = new PassByBO().setLayer_id(localLayerId)
				.setType(XtBusinessPassByContentBO.CMD_LOCAL_SEE_XT_USER)
				.setPass_by_content(passByContent);
				
				logic.getPass_by().add(passby);
			}
		}
		
		//呼叫转发点播编码器，生成转发
		if(null == demandsForEncoderAndForward) demandsForEncoderAndForward = new ArrayList<CommandGroupForwardDemandPO>();
		for(CommandGroupForwardDemandPO demand : demandsForEncoderAndForward){
			//只对设备、用户转发处理；文件转发不用呼叫编解码器
			ForwardDemandBusinessType demandType = demand.getDemandType();
			if((demandType.equals(ForwardDemandBusinessType.FORWARD_DEVICE)
					|| demandType.equals(ForwardDemandBusinessType.FORWARD_USER))
					&& !OriginType.OUTER.equals(demand.getDstOriginType())){
				
				//呼叫转发点播中的源的编码器
				if(!OriginType.OUTER.equals(demand.getSrcOriginType())){
					ConnectBundleBO connectVideoBundle = new ConnectBundleBO().setBusinessType(ConnectBundleBO.BUSINESS_TYPE_VOD)
							          .setOperateType(ConnectBundleBO.OPERATE_TYPE)
									  .setLock_type("write")
									  .setBundleId(demand.getVideoBundleId())
									  .setLayerId(demand.getVideoLayerId())
									  .setBundle_type(demand.getVideoBundleType());
					ConnectBO connectVideoChannel = new ConnectBO().setChannelId(demand.getVideoChannelId())
						   .setChannel_status("Open")
						   .setBase_type("VenusVideoIn")
						   .setCodec_param(codec);
					connectVideoBundle.setChannels(new ArrayListWrapper<ConnectBO>().add(connectVideoChannel).getList());
					logic.getConnectBundle().add(connectVideoBundle);
					
					if(demand.getAudioChannelId()!=null && !demand.getAudioChannelId().equals("")){
							ConnectBO connectAudioChannel = new ConnectBO().setChannelId(demand.getAudioChannelId())
																		   .setChannel_status("Open")
																		   .setBase_type("VenusAudioIn")
																		   .setCodec_param(codec);
							connectVideoBundle.getChannels().add(connectAudioChannel);
					}
				}else{
					//passby呼叫跨系统的源设备
					if(localLayerId == null){
						localLayerId = resourceRemoteService.queryLocalLayerId();
					}
					String cmd = null;
					if(demandType.equals(ForwardDemandBusinessType.FORWARD_DEVICE)){
						cmd = XtBusinessPassByContentBO.CMD_LOCAL_SEE_XT_ENCODER;
					}else{
						cmd = XtBusinessPassByContentBO.CMD_LOCAL_SEE_XT_USER;
					}
					XtBusinessPassByContentBO passByContent = new XtBusinessPassByContentBO().setCmd(cmd)
										 .setOperate(XtBusinessPassByContentBO.OPERATE_START)
										 .setUuid(demand.getUuid())
										 .setSrc_user(chairmanUserNum)//以主席的身份发起点播
										 .setXt_encoder(new HashMapWrapper<String, String>().put("layerid", demand.getVideoLayerId())
												 											.put("bundleid", demand.getVideoBundleId())
												 											.put("video_channelid", demand.getVideoChannelId())
												 											.put("audio_channelid", demand.getAudioChannelId())
												 											.getMap())
										 .setDst_number(demand.getSrcCode())
										 .setVparam(codec);
					
					PassByBO passby = new PassByBO().setLayer_id(localLayerId)
					.setType(cmd)
					.setPass_by_content(passByContent);
					
					logic.getPass_by().add(passby);
				}
				
				//转发
				if(demand.getDstVideoBundleId() != null){
					ForwardSetBO forwardVideo = new ForwardSetBO().set(demand, codec, MediaType.VIDEO);
					logic.getForwardSet().add(forwardVideo);
				}
				if(demand.getDstAudioBundleId() != null){
					ForwardSetBO forwardAudio = new ForwardSetBO().set(demand, codec, MediaType.AUDIO);
					logic.getForwardSet().add(forwardAudio);
				}
			}
		}
		
		//呼叫播放器
		if(null == players) players = new ArrayList<CommandGroupUserPlayerPO>();
		for(CommandGroupUserPlayerPO player : players){
			//播放文件和播放录像不需要呼叫
			if(player.getPlayerBusinessType().equals(PlayerBusinessType.COMMAND_FORWARD_FILE)){
//						|| player.getPlayerBusinessType().equals(PlayerBusinessType.COMMAND_FORWARD_RECORD)){
				continue;
			}
			ConnectBundleBO connectDstVideoBundle = new ConnectBundleBO().setBusinessType(ConnectBundleBO.BUSINESS_TYPE_VOD)
//											 .setOperateType(ConnectBundleBO.OPERATE_TYPE)
									 .setLock_type("write")
								     .setBundleId(player.getBundleId())
								     .setLayerId(player.getLayerId())
								     .setBundle_type(player.getBundleType());
			ConnectBO connectDstVideoChannel = new ConnectBO().setChannelId(player.getVideoChannelId())
					      .setChannel_status("Open")
					      .setBase_type("VenusVideoOut")
					      .setCodec_param(codec);
			
			//设置osd内容
			
			connectDstVideoBundle.setChannels(new ArrayListWrapper<ConnectBO>().add(connectDstVideoChannel).getList());
			logic.getConnectBundle().add(connectDstVideoBundle);
			
			if(player.getAudioChannelId()!=null && player.getAudioChannelId()!=null){
				ConnectBO connectDstAudioChannel = new ConnectBO().setChannelId(player.getAudioChannelId())
															      .setChannel_status("Open")
															      .setBase_type("VenusAudioOut")
															      .setCodec_param(codec);					
				connectDstVideoBundle.getChannels().add(connectDstAudioChannel);
			}
		}
		
		//转发（对于目的在外部系统的转发，因为getDstVideoBundleId和getDstAudioBundleId都是null，所以不会生成转发协议）
		if(null == forwards) forwards = new HashSet<CommandGroupForwardPO>();
		for(CommandGroupForwardPO forward : forwards){
			if(forward.getDstVideoBundleId() != null){
				ForwardSetBO forwardVideo = new ForwardSetBO().set(forward, codec, MediaType.VIDEO);
				logic.getForwardSet().add(forwardVideo);
			}
			if(forward.getDstAudioBundleId() != null){
				ForwardSetBO forwardAudio = new ForwardSetBO().set(forward, codec, MediaType.AUDIO);
				logic.getForwardSet().add(forwardAudio);
			}
		}
		
		//删除转发
		if(null == delForwards) delForwards = new HashSet<CommandGroupForwardPO>();
		for(CommandGroupForwardPO delForward : delForwards){
			if(delForward.getDstVideoBundleId() != null){
				ForwardDelBO forwardVideo = new ForwardDelBO().set(delForward, codec, MediaType.VIDEO);
				logic.getForwardDel().add(forwardVideo);
			}
			if(delForward.getDstAudioBundleId() != null){
				ForwardDelBO forwardAudio = new ForwardDelBO().set(delForward, codec, MediaType.AUDIO);
				logic.getForwardDel().add(forwardAudio);
			}
		}
		
		return logic;
		
	}

	/**
	 * 生成“挂断设备”的logic协议<br/>
	 * <p>详细描述</p>
	 * <b>作者:</b>zsy<br/>
	 * <b>版本：</b>1.0<br/>
	 * <b>日期：</b>2019年10月24日 上午11:14:28
	 * @param membersForEncoder 成员列表，仅用于挂断编码器
	 * @param demandsForEncoder 转发点播列表，仅用于挂断其中的编码器。仅当目的成员是INNER的时候才会处理
	 * @param players 播放器列表，方法中会判断播放器的业务属性，如果不是实时流业务则不生成logic协议
	 * @param codec
	 * @param chairmanUserNum 主席的号码
	 * @return
	 * @throws Exception 
	 */
	public LogicBO closeBundle(
			List<CommandGroupMemberPO> membersForEncoder,
			List<CommandGroupForwardDemandPO> demandsForEncoder,
			List<CommandGroupUserPlayerPO> players,
			CodecParamBO codec,
			String chairmanUserNum) throws Exception{
		
//		UserBO admin = resourceService.queryUserInfoByUsername(CommandCommonConstant.USER_NAME);
		UserBO admin = new UserBO(); admin.setId(-1L);
		String localLayerId = null;
		
		LogicBO logic = new LogicBO().setUserId(admin.getId().toString())
		 		 .setDisconnectBundle(new ArrayList<DisconnectBundleBO>())
		 		 .setPass_by(new ArrayList<PassByBO>());
		
		//挂断成员的编码器
		if(null == membersForEncoder) membersForEncoder = new ArrayList<CommandGroupMemberPO>();
		for(CommandGroupMemberPO member : membersForEncoder){
			if(!OriginType.OUTER.equals(member.getOriginType())){
				DisconnectBundleBO disconnectVideoBundle = new DisconnectBundleBO().setBusinessType(DisconnectBundleBO.BUSINESS_TYPE_VOD)
								       .setOperateType(DisconnectBundleBO.OPERATE_TYPE)
									   .setBundleId(member.getSrcBundleId())
									   .setBundle_type(member.getSrcBundleType())
									   .setLayerId(member.getSrcLayerId());
				
				logic.getDisconnectBundle().add(disconnectVideoBundle);
			}else{
				//挂断外部系统用户，相当于停止点播外部用户，passby拉流
				if(localLayerId == null){
					localLayerId = resourceRemoteService.queryLocalLayerId();
				}
				XtBusinessPassByContentBO passByContent = new XtBusinessPassByContentBO().setCmd(XtBusinessPassByContentBO.CMD_LOCAL_SEE_XT_USER)
									 .setOperate(XtBusinessPassByContentBO.OPERATE_STOP)
									 .setUuid(member.getUuid())
									 .setSrc_user(chairmanUserNum)
									 .setXt_encoder(new HashMapWrapper<String, String>().put("layerid", member.getSrcLayerId())
											 											.put("bundleid", member.getSrcBundleId())
											 											.put("video_channelid", member.getSrcVideoChannelId())
											 											.put("audio_channelid", member.getSrcAudioChannelId())
											 											.getMap())
									 .setDst_number(member.getUserNum())
									 .setVparam(codec);
				
				PassByBO passby = new PassByBO().setLayer_id(localLayerId)
				.setType(XtBusinessPassByContentBO.CMD_LOCAL_SEE_XT_USER)
				.setPass_by_content(passByContent);
				
				logic.getPass_by().add(passby);
			}
		}
		
		//挂断转发点播中的源的编码器
		if(null == demandsForEncoder) demandsForEncoder = new ArrayList<CommandGroupForwardDemandPO>();
		for(CommandGroupForwardDemandPO demand : demandsForEncoder){
			//只对设备、用户转发处理，文件转发不用呼叫编解码器；只对目的为INNER的处理；只对DONE的处理
			ForwardDemandBusinessType demandType = demand.getDemandType();
			if((demandType.equals(ForwardDemandBusinessType.FORWARD_DEVICE)
					|| demandType.equals(ForwardDemandBusinessType.FORWARD_USER))
					&& !OriginType.OUTER.equals(demand.getDstOriginType())
					&& ForwardDemandStatus.DONE.equals(demand.getExecuteStatus())){
				
				if(!OriginType.OUTER.equals(demand.getSrcOriginType())){
					DisconnectBundleBO disconnectVideoBundle = new DisconnectBundleBO().setBusinessType(DisconnectBundleBO.BUSINESS_TYPE_VOD)
									       .setOperateType(DisconnectBundleBO.OPERATE_TYPE)
										   .setBundleId(demand.getVideoBundleId())
										   .setBundle_type(demand.getVideoBundleType())
										   .setLayerId(demand.getVideoLayerId());
					
					logic.getDisconnectBundle().add(disconnectVideoBundle);
				}else{
					//passby停止跨系统的源设备
					if(localLayerId == null){
						localLayerId = resourceRemoteService.queryLocalLayerId();
					}
					String cmd = null;
					if(demandType.equals(ForwardDemandBusinessType.FORWARD_DEVICE)){
						cmd = XtBusinessPassByContentBO.CMD_LOCAL_SEE_XT_ENCODER;
					}else{
						cmd = XtBusinessPassByContentBO.CMD_LOCAL_SEE_XT_USER;
					}
					XtBusinessPassByContentBO passByContent = new XtBusinessPassByContentBO().setCmd(cmd)
										 .setOperate(XtBusinessPassByContentBO.OPERATE_STOP)
										 .setUuid(demand.getUuid())
										 .setSrc_user(chairmanUserNum)//以主席的身份停止点播
										 .setXt_encoder(new HashMapWrapper<String, String>().put("layerid", demand.getVideoLayerId())
												 											.put("bundleid", demand.getVideoBundleId())
												 											.put("video_channelid", demand.getVideoChannelId())
												 											.put("audio_channelid", demand.getAudioChannelId())
												 											.getMap())
										 .setDst_number(demand.getSrcCode())
										 .setVparam(codec);
					
					PassByBO passby = new PassByBO().setLayer_id(localLayerId)
					.setType(cmd)
					.setPass_by_content(passByContent);
					
					logic.getPass_by().add(passby);
				}
			}
		}
		
		//挂断播放器
		if(null == players) players = new ArrayList<CommandGroupUserPlayerPO>();
//		for(CommandGroupMemberPO member : membersForEncoder){
			for(CommandGroupUserPlayerPO player : players){
				
				//清除资源层上的字幕
				resourceServiceClient.removeLianwangPassby(player.getBundleId());
				
				//播放文件和播放录像不需要挂断
				if(player.getPlayerBusinessType().equals(PlayerBusinessType.PLAY_FILE)
						|| player.getPlayerBusinessType().equals(PlayerBusinessType.PLAY_RECORD)
						|| player.getPlayerBusinessType().equals(PlayerBusinessType.COMMAND_FORWARD_FILE)
						|| player.getPlayerBusinessType().equals(PlayerBusinessType.PLAY_COMMAND_RECORD)){
					continue;
				}
				//如果播放器业务不为NONE（这个条件可能不需要）
//				if(!PlayerBusinessType.NONE.equals(player.getPlayerBusinessType())){
					DisconnectBundleBO disconnectDstBundle = new DisconnectBundleBO().setBusinessType(DisconnectBundleBO.BUSINESS_TYPE_VOD)
//											  .setOperateType(DisconnectBundleBO.OPERATE_TYPE)
											  .setBundleId(player.getBundleId())
										      .setBundle_type(player.getBundleType())
										      .setLayerId(player.getLayerId());
					logic.getDisconnectBundle().add(disconnectDstBundle);
//				}
			}
//		}
		
		return logic;
	}
}
