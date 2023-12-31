package com.sumavision.bvc.control.device.monitor.live;

import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.suma.venus.resource.base.bo.UserBO;
import com.sumavision.bvc.control.device.monitor.device.MonitorDeviceController;
import com.sumavision.bvc.control.device.monitor.osd.MonitorOsdVO;
import com.sumavision.bvc.control.utils.UserUtils;
import com.sumavision.bvc.control.welcome.UserVO;
import com.sumavision.bvc.device.monitor.live.DstDeviceType;
import com.sumavision.bvc.device.monitor.live.MonitorLiveService;
import com.sumavision.bvc.device.monitor.live.MonitorLiveSplitConfigDAO;
import com.sumavision.bvc.device.monitor.live.MonitorLiveSplitConfigPO;
import com.sumavision.bvc.device.monitor.live.device.MonitorLiveDeviceDAO;
import com.sumavision.bvc.device.monitor.live.device.MonitorLiveDevicePO;
import com.sumavision.bvc.device.monitor.live.device.MonitorLiveDeviceQuery;
import com.sumavision.bvc.device.monitor.live.device.MonitorLiveDeviceService;
import com.sumavision.bvc.device.monitor.live.user.MonitorLiveUserDAO;
import com.sumavision.bvc.device.monitor.live.user.MonitorLiveUserPO;
import com.sumavision.bvc.device.monitor.live.user.MonitorLiveUserQuery;
import com.sumavision.bvc.device.monitor.live.user.MonitorLiveUserService;
import com.sumavision.bvc.device.monitor.osd.MonitorOsdDAO;
import com.sumavision.bvc.device.monitor.osd.MonitorOsdPO;
import com.sumavision.tetris.auth.token.TerminalType;
import com.sumavision.tetris.commons.util.wrapper.HashMapWrapper;
import com.sumavision.tetris.mvc.ext.response.json.aop.annotation.JsonBody;

@Controller
@RequestMapping(value = "/monitor/live")
public class MonitorLiveController {

	@Autowired
	private MonitorLiveService monitorLiveService;
	
	@Autowired
	private MonitorLiveDeviceDAO monitorLiveDeviceDao;
	
	@Autowired
	private MonitorLiveUserDAO monitorLiveUserDao;
	
	@Autowired
	private MonitorLiveDeviceQuery monitorLiveDeviceQuery;
	
	@Autowired
	private MonitorLiveUserQuery monitorLiveUserQuery;
	
	@Autowired
	private UserUtils userUtils;
	
	@Autowired
	private MonitorLiveDeviceService monitorLiveDeviceService;
	
	@Autowired
	private MonitorLiveUserService monitorLiveUserService;
	
	@Autowired
	private MonitorOsdDAO monitorOsdDao;
	
	@Autowired
	private MonitorLiveSplitConfigDAO monitorLiveSplitConfigDAO;
	
	@RequestMapping(value = "/terminal/index")
	public ModelAndView terminalIndex(String token){
		
		ModelAndView mv = new ModelAndView("web/bvc/monitor/live/terminalLive");
		mv.addObject("token", token);
		
		return mv;
	}
	
	@RequestMapping(value = "/monitor/index")
	public ModelAndView monitorIndex(String token){
		
		ModelAndView mv = new ModelAndView("web/bvc/monitor/live/monitorLive");
		mv.addObject("token", token);
		
		return mv;
	}
	
	@RequestMapping(value = "/channel/index")
	public ModelAndView channelIndex(String token){
		
		ModelAndView mv = new ModelAndView("web/bvc/monitor/live/channelLive");
		mv.addObject("token", token);
		
		return mv;
	}
	
	@RequestMapping(value = "/list/index")
	public ModelAndView listIndex(String token){
		
		ModelAndView mv = new ModelAndView("web/bvc/monitor/live-list/live-list");
		mv.addObject("token", token);
		
		return mv;
	}
	
	/**
	 * 分页查询点播设备任务<br/>
	 * <b>作者:</b>lvdeyang<br/>
	 * <b>版本：</b>1.0<br/>
	 * <b>日期：</b>2019年5月4日 下午4:23:50
	 * @param int currentPage 当前页码
	 * @param int pageSize 每页数据量
	 * @return total int 总数据量
	 * @return rows List<MonitorLiveDeviceVO> 任务列表
	 */
	@JsonBody
	@ResponseBody
	@RequestMapping(value = "/load/device/lives")
	public Object loadDeviceLives(
			int currentPage,
			int pageSize,
			HttpServletRequest request) throws Exception{
		
		Long userId = userUtils.getUserIdFromSession(request);
		
		long total = 0;
		
		List<MonitorLiveDevicePO> entities = null;
		
		if(userId.longValue() == 1l){
			total = monitorLiveDeviceDao.count();
			entities = monitorLiveDeviceQuery.findAll(currentPage, pageSize);
		}else{
			total = monitorLiveDeviceDao.countByUserId(userId);
			entities = monitorLiveDeviceQuery.findByUserId(userId, currentPage, pageSize);
		}
		
		List<MonitorLiveDeviceVO> rows = MonitorLiveVO.getConverter(MonitorLiveDeviceVO.class).convert(entities, MonitorLiveDeviceVO.class);
		
		if(rows!=null && rows.size()>0){
			Set<Long> osdIds = new HashSet<Long>();
			for(MonitorLiveDeviceVO row:rows){
				osdIds.add(row.getOsdId()==null?0l:row.getOsdId());
			}
			List<MonitorOsdPO> osdEntities = monitorOsdDao.findAll(osdIds);
			if(osdEntities!=null && osdEntities.size()>0){
				for(MonitorLiveDeviceVO row:rows){
					for(MonitorOsdPO osdEntity:osdEntities){
						if(osdEntity.getId().equals(row.getOsdId())){
							row.setOsdName(osdEntity.getName())
							   .setOsdUsername(osdEntity.getUsername());
							break;
						}
					}
				}
			}
		}
		
		return new HashMapWrapper<String, Object>().put("total", total)
												   .put("rows", rows)
												   .getMap();
	}
	
	/**
	 * 分页查询点播用户任务<br/>
	 * <b>作者:</b>lvdeyang<br/>
	 * <b>版本：</b>1.0<br/>
	 * <b>日期：</b>2019年5月4日 下午4:23:50
	 * @param int currentPage 当前页码
	 * @param int pageSize 每页数据量
	 * @return total int 总数据量
	 * @return rows List<MonitorLiveUserVO> 任务列表
	 */
	@JsonBody
	@ResponseBody
	@RequestMapping(value = "/load/user/lives")
	public Object loadUserLives(
			int currentPage,
			int pageSize,
			HttpServletRequest request) throws Exception{
		
		Long userId = userUtils.getUserIdFromSession(request);
		
		long total = 0;
		
		List<MonitorLiveUserPO> entities = null;
		
		if(userId.longValue() == 1l){
			total = monitorLiveUserDao.count();
			entities = monitorLiveUserQuery.findAll(currentPage, pageSize);
		}else{
			total = monitorLiveUserDao.countByUserId(userId);
			entities = monitorLiveUserQuery.findByUserId(userId, currentPage, pageSize);
		}
		
		List<MonitorLiveUserVO> rows = MonitorLiveVO.getConverter(MonitorLiveUserVO.class).convert(entities, MonitorLiveUserVO.class);
		
		return new HashMapWrapper<String, Object>().put("total", total)
												   .put("rows", rows)
												   .getMap();
	}
	
	/**
	 * 添加直播任务<br/>
	 * <b>作者:</b>lvdeyang<br/>
	 * <b>版本：</b>1.0<br/>
	 * <b>日期：</b>2019年4月26日 下午5:21:39
	 * @param Long osdId osd显示id
	 * @param String realType 设备类型
	 * @param String videoBundleId 视频源设备id
	 * @param String videoBundleName 视频源设备名称
	 * @parma String videoBundleType 视频源设备类型
	 * @param String videoLayerId 视频源设备接入层
	 * @param String videoChannelId 视频源通道id
	 * @param String videoBaseType 视频源通道类型
	 * @param String audioBundleId 音频源设备id
	 * @param String audioBundleName 音频源设备名称
	 * @param String audioBundleType 音频源设备类型
	 * @param String audioLayerId 音频源设备接入层id
	 * @param String audioChannelId 音频源通道id
	 * @param String audioBaseType 音频源通道类型
	 * @param String dstVideoBundleId 目标视频设备id
	 * @param String dstVideoBundleName 目标视频设备名称
	 * @param String dstVideoBundleType 目标视频设备类型
	 * @param String dstVideoLayerId 目标视频设备接入层
	 * @param String dstVideoChannelId 目标视频设备通道id
	 * @param String dstVideoBaseType 目标视频设备通道类型
	 * @param String dstAudioBundleId 目标音频设备id
	 * @param String dstAudioBundleName 目标音频设备名称
	 * @param String dstAudioBundleType 目标音频设备类型
	 * @param String dstAudioLayerId 目标音频设备接入层
	 * @param String dstAudioChannelId 目标音频设备通道id
	 * @param String dstAudioBaseType 目标音频设备通道类型
	 * @param String type 业务类型 WEBSITE_PLAYER, DEVICE
	 * @return MonitorLiveVO 直播任务
	 */
	@JsonBody
	@ResponseBody
	@RequestMapping(value = "/add")
	public Object add(
			Long osdId,
			String realType,
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
			String type,
			HttpServletRequest request) throws Exception{
		
		UserVO user =  userUtils.getUserFromSession(request);
		
		MonitorLiveDevicePO entity = null;
		
		if("BVC".equals(realType)){
			entity = monitorLiveDeviceService.startLocalSeeLocal(
					osdId, 
					videoBundleId, videoBundleName, videoBundleType, videoLayerId, videoChannelId, videoBaseType, 
					audioBundleId, audioBundleName, audioBundleType, audioLayerId, audioChannelId, audioBaseType, 
					dstVideoBundleId, dstVideoBundleName, dstVideoBundleType, dstVideoLayerId, dstVideoChannelId, dstVideoBaseType, 
					dstAudioBundleId, dstAudioBundleName, dstAudioBundleType, dstAudioLayerId, dstAudioChannelId, dstAudioBaseType, 
					type, user.getId(), user.getUserno(), 
					false, null);
		}else if("XT".equals(realType)){
			entity = monitorLiveDeviceService.startLocalSeeXt(
					osdId, 
					videoBundleId, videoBundleName, videoBundleType, videoLayerId, videoChannelId, videoBaseType, 
					audioBundleId, audioBundleName, audioBundleType, audioLayerId, audioChannelId, audioBaseType, 
					dstVideoBundleId, dstVideoBundleName, dstVideoBundleType, dstVideoLayerId, dstVideoChannelId, dstVideoBaseType, 
					dstAudioBundleId, dstAudioBundleName, dstAudioBundleType, dstAudioLayerId, dstAudioChannelId, dstAudioBaseType, 
					type, user.getId(), user.getUserno());
		}
		
		return new MonitorLiveDeviceVO().set(entity);
	}
	
	/**
	 * 添加带转码的直播任务。参数与add()方法相同<br/>
	 * <b>作者:</b>zsy<br/>
	 * <b>版本：</b>1.0<br/>
	 * <b>日期：</b>2019年12月17日 下午5:21:39
	 * @param Long osdId osd显示id
	 * @param String realType 设备类型
	 * @param String videoBundleId 视频源设备id
	 * @param String videoBundleName 视频源设备名称
	 * @parma String videoBundleType 视频源设备类型
	 * @param String videoLayerId 视频源设备接入层
	 * @param String videoChannelId 视频源通道id
	 * @param String videoBaseType 视频源通道类型
	 * @param String audioBundleId 音频源设备id
	 * @param String audioBundleName 音频源设备名称
	 * @param String audioBundleType 音频源设备类型
	 * @param String audioLayerId 音频源设备接入层id
	 * @param String audioChannelId 音频源通道id
	 * @param String audioBaseType 音频源通道类型
	 * @param String dstVideoBundleId 目标视频设备id
	 * @param String dstVideoBundleName 目标视频设备名称
	 * @param String dstVideoBundleType 目标视频设备类型
	 * @param String dstVideoLayerId 目标视频设备接入层
	 * @param String dstVideoChannelId 目标视频设备通道id
	 * @param String dstVideoBaseType 目标视频设备通道类型
	 * @param String dstAudioBundleId 目标音频设备id
	 * @param String dstAudioBundleName 目标音频设备名称
	 * @param String dstAudioBundleType 目标音频设备类型
	 * @param String dstAudioLayerId 目标音频设备接入层
	 * @param String dstAudioChannelId 目标音频设备通道id
	 * @param String dstAudioBaseType 目标音频设备通道类型
	 * @param String type 业务类型 WEBSITE_PLAYER, DEVICE
	 * @return MonitorLiveVO 直播任务
	 */
	@JsonBody
	@ResponseBody
	@RequestMapping(value = "/add/transcord")
	public Object addTranscord(
			Long osdId,
			String realType,
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
			String type,
			HttpServletRequest request) throws Exception{
		
		UserVO user =  userUtils.getUserFromSession(request);
		
		MonitorLiveDevicePO entity = null;
		
		String clientIp = request.getHeader("X-Real-IP");
		clientIp = (clientIp==null||"".equals(clientIp))?request.getRemoteAddr():clientIp;
		String udpPortStart = getPlayerUdpPortStart();
		int port = Integer.parseInt(udpPortStart) + 2*Integer.parseInt(dstVideoBundleName.split("_")[1]);
		String udpUrl = "udp://" + clientIp + ":" + port;
		
		if("BVC".equals(realType)){
			entity = monitorLiveDeviceService.startLocalSeeLocal(
					osdId, 
					videoBundleId, videoBundleName, videoBundleType, videoLayerId, videoChannelId, videoBaseType, 
					audioBundleId, audioBundleName, audioBundleType, audioLayerId, audioChannelId, audioBaseType, 
					dstVideoBundleId, dstVideoBundleName, dstVideoBundleType, dstVideoLayerId, dstVideoChannelId, dstVideoBaseType, 
					dstAudioBundleId, dstAudioBundleName, dstAudioBundleType, dstAudioLayerId, dstAudioChannelId, dstAudioBaseType, 
					type, user.getId(), user.getUserno(), 
					true, udpUrl);
		}else if("XT".equals(realType)){
			entity = monitorLiveDeviceService.startLocalSeeXt(
					osdId, 
					videoBundleId, videoBundleName, videoBundleType, videoLayerId, videoChannelId, videoBaseType, 
					audioBundleId, audioBundleName, audioBundleType, audioLayerId, audioChannelId, audioBaseType, 
					dstVideoBundleId, dstVideoBundleName, dstVideoBundleType, dstVideoLayerId, dstVideoChannelId, dstVideoBaseType, 
					dstAudioBundleId, dstAudioBundleName, dstAudioBundleType, dstAudioLayerId, dstAudioChannelId, dstAudioBaseType, 
					type, user.getId(), user.getUserno());
		}
		
		MonitorLiveDeviceVO vo = new MonitorLiveDeviceVO().set(entity);
		vo.setUdpUrl(udpUrl);
		
		return vo;
	}
	
	/**
	 * 直播任务联动<br/>
	 * <b>作者:</b>lvdeyang<br/>
	 * <b>版本：</b>1.0<br/>
	 * <b>日期：</b>2019年10月30日 下午5:29:37
	 * @param Integer serial 屏幕序号
	 * @param Long osdId osd显示id
	 * @param String realType 设备类型
	 * @param String videoBundleId 视频源设备id
	 * @param String videoBundleName 视频源设备名称
	 * @parma String videoBundleType 视频源设备类型
	 * @param String videoLayerId 视频源设备接入层
	 * @param String videoChannelId 视频源通道id
	 * @param String videoBaseType 视频源通道类型
	 * @param String audioBundleId 音频源设备id
	 * @param String audioBundleName 音频源设备名称
	 * @param String audioBundleType 音频源设备类型
	 * @param String audioLayerId 音频源设备接入层id
	 * @param String audioChannelId 音频源通道id
	 * @param String audioBaseType 音频源通道类型
	 */
	@JsonBody
	@ResponseBody
	@RequestMapping(value = "/add/decode/bind")
	public Object addDecodeBind(
			Integer serial,
			Long osdId,
			String realType,
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
			HttpServletRequest request) throws Exception{
		UserVO user = userUtils.getUserFromSession(request);
		List<MonitorLiveSplitConfigPO> configs = monitorLiveSplitConfigDAO.findByUserIdAndSerial(user.getId(), serial);
		if(configs!=null && configs.size()>0){
			for(MonitorLiveSplitConfigPO config:configs){
				String dstVideoBundleId = config.getDstVideoBundleId();
				String dstVideoBundleName = config.getDstVideoBundleName();
				String dstVideoBundleType = config.getDstVideoBundleType();
				String dstVideoLayerId = config.getDstVideoLayerId();
				String dstVideoChannelId = config.getDstVideoChannelId();
				String dstVideoBaseType = config.getDstVideoBaseType();
				String dstAudioBundleId = config.getDstAudioBundleId();
				String dstAudioBundleName = config.getDstAudioBundleName();
				String dstAudioBundleType = config.getDstAudioBundleType();
				String dstAudioLayerId = config.getDstAudioLayerId();
				String dstAudioChannelId = config.getDstAudioChannelId();
				String dstAudioBaseType = config.getDstAudioBaseType(); 
				if("BVC".equals(realType)){
					monitorLiveDeviceService.startLocalSeeLocal(
							osdId, 
							videoBundleId, videoBundleName, videoBundleType, videoLayerId, videoChannelId, videoBaseType, 
							audioBundleId, audioBundleName, audioBundleType, audioLayerId, audioChannelId, audioBaseType, 
							dstVideoBundleId, dstVideoBundleName, dstVideoBundleType, dstVideoLayerId, dstVideoChannelId, dstVideoBaseType, 
							dstAudioBundleId, dstAudioBundleName, dstAudioBundleType, dstAudioLayerId, dstAudioChannelId, dstAudioBaseType, 
							DstDeviceType.DEVICE.toString(), user.getId(), user.getUserno(), 
							false, null);
				}else if("XT".equals(realType)){
					monitorLiveDeviceService.startLocalSeeXt(
							osdId, 
							videoBundleId, videoBundleName, videoBundleType, videoLayerId, videoChannelId, videoBaseType, 
							audioBundleId, audioBundleName, audioBundleType, audioLayerId, audioChannelId, audioBaseType, 
							dstVideoBundleId, dstVideoBundleName, dstVideoBundleType, dstVideoLayerId, dstVideoChannelId, dstVideoBaseType, 
							dstAudioBundleId, dstAudioBundleName, dstAudioBundleType, dstAudioLayerId, dstAudioChannelId, dstAudioBaseType, 
							DstDeviceType.DEVICE.toString(), user.getId(), user.getUserno());
				}
			}
		}
		return null;
	}
	
	
	/**
	 * 点播本地用户<br/>
	 * <b>作者:</b>lvdeyang<br/>
	 * <b>版本：</b>1.0<br/>
	 * <b>日期：</b>2019年6月3日 下午2:42:56
	 * @param osdId osd显示id
	 * @param localUserId 用户id
	 * @param String dstVideoBundleId 目标视频设备id
	 * @param String dstVideoBundleName 目标视频设备名称
	 * @param String dstVideoBundleType 目标视频设备类型
	 * @param String dstVideoLayerId 目标视频设备接入层
	 * @param String dstVideoChannelId 目标视频设备通道id
	 * @param String dstVideoBaseType 目标视频设备通道类型
	 * @param String dstAudioBundleId 目标音频设备id
	 * @param String dstAudioBundleName 目标音频设备名称
	 * @param String dstAudioBundleType 目标音频设备类型
	 * @param String dstAudioLayerId 目标音频设备接入层
	 * @param String dstAudioChannelId 目标音频设备通道id
	 * @param String dstAudioBaseType 目标音频设备通道类型
	 * @param String type 业务类型 WEBSITE_PLAYER, DEVICE
	 * @return MonitorLiveUserVO 点播用户任务
	 */
	@JsonBody
	@ResponseBody
	@RequestMapping(value = "/local/user/on/demand")
	public Object localUserOnDemand(
			Long osdId,
			Long localUserId,
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
			String type,
			HttpServletRequest request) throws Exception{
		
		UserBO localUser = userUtils.queryUserById(localUserId, TerminalType.PC_PLATFORM);
		
		UserVO user = userUtils.getUserFromSession(request);
		
		MonitorLiveUserPO entity = monitorLiveUserService.startLocalSeeLocal(
				localUser, 
				dstVideoBundleId, dstVideoBundleName, dstVideoBundleType, dstVideoLayerId, dstVideoChannelId, dstVideoBaseType, 
				dstAudioBundleId, dstAudioBundleName, dstAudioBundleType, dstAudioLayerId, dstAudioChannelId, dstAudioBaseType, 
				type, user.getId(), user.getName(), user.getUserno());
		
		return new MonitorLiveUserVO().set(entity);
	}
	
	/**
	 * 点播本地用户任务联动<br/>
	 * <b>作者:</b>lvdeyang<br/>
	 * <b>版本：</b>1.0<br/>
	 * <b>日期：</b>2019年10月30日 下午5:33:32
	 * @param Integer serial 屏幕序号
	 * @param Long osdId 字幕id
	 * @param Long localUserId 本地用户id
	 */
	@JsonBody
	@ResponseBody
	@RequestMapping(value = "/local/user/on/demand/decode/bind")
	public Object localUserOnDemandDecodeBind(
			Integer serial,
			Long osdId,
			Long localUserId,
			HttpServletRequest request) throws Exception{
		
		UserBO localUser = userUtils.queryUserById(localUserId, TerminalType.PC_PLATFORM);
		
		UserVO user = userUtils.getUserFromSession(request);
		
		List<MonitorLiveSplitConfigPO> configs = monitorLiveSplitConfigDAO.findByUserIdAndSerial(user.getId(), serial);
		if(configs!=null && configs.size()>0){
			for(MonitorLiveSplitConfigPO config:configs){
				String dstVideoBundleId = config.getDstVideoBundleId();
				String dstVideoBundleName = config.getDstVideoBundleName();
				String dstVideoBundleType = config.getDstVideoBundleType();
				String dstVideoLayerId = config.getDstVideoLayerId();
				String dstVideoChannelId = config.getDstVideoChannelId();
				String dstVideoBaseType = config.getDstVideoBaseType();
				String dstAudioBundleId = config.getDstAudioBundleId();
				String dstAudioBundleName = config.getDstAudioBundleName();
				String dstAudioBundleType = config.getDstAudioBundleType();
				String dstAudioLayerId = config.getDstAudioLayerId();
				String dstAudioChannelId = config.getDstAudioChannelId();
				String dstAudioBaseType = config.getDstAudioBaseType(); 
				monitorLiveUserService.startLocalSeeLocal(
						localUser, 
						dstVideoBundleId, dstVideoBundleName, dstVideoBundleType, dstVideoLayerId, dstVideoChannelId, dstVideoBaseType, 
						dstAudioBundleId, dstAudioBundleName, dstAudioBundleType, dstAudioLayerId, dstAudioChannelId, dstAudioBaseType, 
						DstDeviceType.DEVICE.toString(), user.getId(), user.getName(), user.getUserno());
			}
		}
		return null;
	}
	
	/**
	 * 点播xt用户<br/>
	 * <b>作者:</b>lvdeyang<br/>
	 * <b>版本：</b>1.0<br/>
	 * <b>日期：</b>2019年6月3日 下午2:51:31
	 * @param osdId osd显示id
	 * @param xtUserId xt用户id
	 * @param String dstVideoBundleId 目标视频设备id
	 * @param String dstVideoBundleName 目标视频设备名称
	 * @param String dstVideoBundleType 目标视频设备类型
	 * @param String dstVideoLayerId 目标视频设备接入层
	 * @param String dstVideoChannelId 目标视频设备通道id
	 * @param String dstVideoBaseType 目标视频设备通道类型
	 * @param String dstAudioBundleId 目标音频设备id
	 * @param String dstAudioBundleName 目标音频设备名称
	 * @param String dstAudioBundleType 目标音频设备类型
	 * @param String dstAudioLayerId 目标音频设备接入层
	 * @param String dstAudioChannelId 目标音频设备通道id
	 * @param String dstAudioBaseType 目标音频设备通道类型
	 * @param String type 业务类型 WEBSITE_PLAYER, DEVICE
	 * @return MonitorLiveUserVO 点播用户任务
	 */
	@JsonBody
	@ResponseBody
	@RequestMapping(value = "/xt/user/on/demand")
	public Object xtUserOnDemand(
			Long osdId,
			Long xtUserId,
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
			String type,
			HttpServletRequest request) throws Exception{
		
		UserBO xtUser = userUtils.queryUserById(xtUserId, TerminalType.PC_PLATFORM);
		
		UserVO user = userUtils.getUserFromSession(request);
		
		MonitorLiveUserPO entity = monitorLiveUserService.startLocalSeeXt(
				xtUser, 
				dstVideoBundleId, dstVideoBundleName, dstVideoBundleType, dstVideoLayerId, dstVideoChannelId, dstVideoBaseType, 
				dstAudioBundleId, dstAudioBundleName, dstAudioBundleType, dstAudioLayerId, dstAudioChannelId, dstAudioBaseType, 
				type, user.getId(), user.getName(), user.getUserno());
		
		return new MonitorLiveUserVO().set(entity);
	}
	
	/**
	 * 点播xt用户任务联动<br/>
	 * <b>作者:</b>lvdeyang<br/>
	 * <b>版本：</b>1.0<br/>
	 * <b>日期：</b>2019年10月31日 上午8:27:54
	 * @param Integer serial 屏幕序号
	 * @param Long osdId 字幕id
	 * @param Long xtUserId xt用户id
	 */
	@JsonBody
	@ResponseBody
	@RequestMapping(value = "/xt/user/on/demand/decode/bind")
	public Object xtUserOnDemandDecodeBind(
			Integer serial,
			Long osdId,
			Long xtUserId,
			HttpServletRequest request) throws Exception{
		UserBO xtUser = userUtils.queryUserById(xtUserId, TerminalType.PC_PLATFORM);
		UserVO user = userUtils.getUserFromSession(request);
		List<MonitorLiveSplitConfigPO> configs = monitorLiveSplitConfigDAO.findByUserIdAndSerial(user.getId(), serial);
		if(configs!=null && configs.size()>0){
			for(MonitorLiveSplitConfigPO config:configs){
				String dstVideoBundleId = config.getDstVideoBundleId();
				String dstVideoBundleName = config.getDstVideoBundleName();
				String dstVideoBundleType = config.getDstVideoBundleType();
				String dstVideoLayerId = config.getDstVideoLayerId();
				String dstVideoChannelId = config.getDstVideoChannelId();
				String dstVideoBaseType = config.getDstVideoBaseType();
				String dstAudioBundleId = config.getDstAudioBundleId();
				String dstAudioBundleName = config.getDstAudioBundleName();
				String dstAudioBundleType = config.getDstAudioBundleType();
				String dstAudioLayerId = config.getDstAudioLayerId();
				String dstAudioChannelId = config.getDstAudioChannelId();
				String dstAudioBaseType = config.getDstAudioBaseType(); 
				monitorLiveUserService.startLocalSeeXt(
						xtUser, 
						dstVideoBundleId, dstVideoBundleName, dstVideoBundleType, dstVideoLayerId, dstVideoChannelId, dstVideoBaseType, 
						dstAudioBundleId, dstAudioBundleName, dstAudioBundleType, dstAudioLayerId, dstAudioChannelId, dstAudioBaseType, 
						DstDeviceType.DEVICE.toString(), user.getId(), user.getName(), user.getUserno());
			}
		}
		return null;
	}
	
	/**
	 * 停止点播xt用户<br/>
	 * <b>作者:</b>lvdeyang<br/>
	 * <b>版本：</b>1.0<br/>
	 * <b>日期：</b>2019年6月3日 下午2:51:31
	 * @param xtUser xt用户
	 * @param String dstVideoBundleId 目标视频设备id
	 * @param String dstVideoBundleName 目标视频设备名称
	 * @param String dstVideoBundleType 目标视频设备类型
	 * @param String dstVideoLayerId 目标视频设备接入层
	 * @param String dstVideoChannelId 目标视频设备通道id
	 * @param String dstVideoBaseType 目标视频设备通道类型
	 * @param String dstAudioBundleId 目标音频设备id
	 * @param String dstAudioBundleName 目标音频设备名称
	 * @param String dstAudioBundleType 目标音频设备类型
	 * @param String dstAudioLayerId 目标音频设备接入层
	 * @param String dstAudioChannelId 目标音频设备通道id
	 * @param String dstAudioBaseType 目标音频设备通道类型
	 */
	@Deprecated
	@JsonBody
	@ResponseBody
	@RequestMapping(value = "/stop/xt/user/on/demand")
	public Object stopXtUserOnDemand(
			Long xtUserId,
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
			HttpServletRequest request) throws Exception{
		
		UserBO xtUser = userUtils.queryUserById(xtUserId, TerminalType.PC_PLATFORM);
		
		UserVO user = userUtils.getUserFromSession(request);
		
		monitorLiveService.stopXtUserOnDemand(
				xtUser,
				dstVideoBundleId, dstVideoBundleName, dstVideoBundleType, dstVideoLayerId, dstVideoChannelId, dstVideoBaseType,
				dstAudioBundleId, dstAudioBundleName, dstAudioBundleType, dstAudioLayerId, dstAudioChannelId, dstAudioBaseType,
				user.getId(), user.getName(), user.getUserno());
		
		return null;
	}
	
	/**
	 * 停止点播设备<br/>
	 * <b>作者:</b>lvdeyang<br/>
	 * <b>版本：</b>1.0<br/>
	 * <b>日期：</b>2019年4月27日 上午10:03:40
	 * @param @PathVariable Long id 点播设备任务id
	 */
	@JsonBody
	@ResponseBody
	@RequestMapping(value = "/stop/live/device/{id}")
	public Object stopLiveDevice(
			@PathVariable Long id,
			HttpServletRequest request) throws Exception{
		
		UserVO user = userUtils.getUserFromSession(request);
		
		monitorLiveDeviceService.stop(id, user.getId(), user.getUserno());
		
		return null;
	}
	
	/**
	 * 停止点播用户<br/>
	 * <b>作者:</b>lvdeyang<br/>
	 * <b>版本：</b>1.0<br/>
	 * <b>日期：</b>2019年6月24日 下午1:12:31
	 * @param @PathVariable Long id 点播用户任务id
	 */
	@JsonBody
	@ResponseBody
	@RequestMapping(value = "/stop/live/user/{id}")
	public Object stopLiveUser(
			@PathVariable Long id,
			HttpServletRequest request) throws Exception{
		
		UserVO user = userUtils.getUserFromSession(request);
		
		monitorLiveUserService.stop(id, user.getId(), user.getUserno());
		
		return null;
	}
	
	/**
	 * 清除用户播放器所有直播调阅任务<br/>
	 * <p>用户页面销毁</p>
	 * <b>作者:</b>lvdeyang<br/>
	 * <b>版本：</b>1.0<br/>
	 * <b>日期：</b>2019年5月9日 上午11:32:24
	 */
	@JsonBody
	@ResponseBody
	@RequestMapping(value = "/remove/all/webplayer/live")
	public Object removeAllWebplayerLive(HttpServletRequest request) throws Exception{
		
		UserVO user = userUtils.getUserFromSession(request);
		
		monitorLiveService.removeAllWebplayerLive(user.getId(), user.getUserno());
		
		return null;
	}
	
	/**
	 * 修改直播任务字幕<br/>
	 * <b>作者:</b>lvdeyang<br/>
	 * <b>版本：</b>1.0<br/>
	 * <b>日期：</b>2019年5月28日 下午5:19:04
	 * @param Long liveId 直播任务id
	 * @param Long osdId osd id
	 * @return MonitorOsdVO 修改的字幕
	 */
	@JsonBody
	@ResponseBody
	@RequestMapping(value = "/change/osd")
	public Object changeOsd(
			Long liveId,
			Long osdId,
			HttpServletRequest request) throws Exception{
		
		Long userId = userUtils.getUserIdFromSession(request);
		MonitorOsdPO entity = monitorLiveDeviceService.changeOsd(liveId, osdId, userId);
		
		return new MonitorOsdVO().set(entity);
	}
	
	private String getPlayerUdpPortStart() throws Exception{
		Properties prop = new Properties();
		prop.load(MonitorDeviceController.class.getClassLoader().getResourceAsStream("properties/player.properties"));
		return prop.getProperty("udpPortStart");
	}
	
}
