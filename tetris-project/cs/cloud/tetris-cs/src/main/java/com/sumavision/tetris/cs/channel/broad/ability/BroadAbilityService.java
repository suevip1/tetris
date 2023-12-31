package com.sumavision.tetris.cs.channel.broad.ability;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.sumavision.tetris.auth.token.TerminalType;
import com.sumavision.tetris.capacity.server.PushService;
import com.sumavision.tetris.commons.util.binary.ByteUtil;
import com.sumavision.tetris.commons.util.date.DateUtil;
import com.sumavision.tetris.commons.util.httprequest.HttpRequestUtil;
import com.sumavision.tetris.commons.util.wrapper.ArrayListWrapper;
import com.sumavision.tetris.commons.util.wrapper.HashMapWrapper;
import com.sumavision.tetris.commons.util.wrapper.StringBufferWrapper;
import com.sumavision.tetris.cs.bak.AbilityInfoSendDAO;
import com.sumavision.tetris.cs.bak.AbilityInfoSendPO;
import com.sumavision.tetris.cs.channel.Adapter;
import com.sumavision.tetris.cs.channel.BroadWay;
import com.sumavision.tetris.cs.channel.ChannelBroadStatus;
import com.sumavision.tetris.cs.channel.ChannelDAO;
import com.sumavision.tetris.cs.channel.ChannelPO;
import com.sumavision.tetris.cs.channel.ChannelQuery;
import com.sumavision.tetris.cs.channel.ChannelService;
import com.sumavision.tetris.cs.channel.ChannelType;
import com.sumavision.tetris.cs.channel.ChannelVO;
import com.sumavision.tetris.cs.channel.SetOutputBO;
import com.sumavision.tetris.cs.channel.autoBroad.ChannelAutoBroadInfoDAO;
import com.sumavision.tetris.cs.channel.autoBroad.ChannelAutoBroadInfoPO;
import com.sumavision.tetris.cs.channel.broad.ability.request.BroadAbilityBroadRequestExchangeVO;
import com.sumavision.tetris.cs.channel.broad.ability.request.BroadAbilityBroadRequestInputPrevFileVO;
import com.sumavision.tetris.cs.channel.broad.ability.request.BroadAbilityBroadRequestInputPrevStreamVO;
import com.sumavision.tetris.cs.channel.broad.ability.request.BroadAbilityBroadRequestInputPrevVO;
import com.sumavision.tetris.cs.channel.broad.ability.request.BroadAbilityBroadRequestInputVO;
import com.sumavision.tetris.cs.channel.broad.ability.request.BroadAbilityBroadRequestOutputVO;
import com.sumavision.tetris.cs.channel.broad.ability.request.BroadAbilityBroadRequestVO;
import com.sumavision.tetris.cs.channel.broad.ability.transcode.BroadTranscodeOutputDAO;
import com.sumavision.tetris.cs.channel.broad.ability.transcode.BroadTranscodeOutputPO;
import com.sumavision.tetris.cs.channel.broad.ability.transcode.BroadTranscodeTaskDAO;
import com.sumavision.tetris.cs.channel.broad.ability.transcode.BroadTranscodeTaskPO;
import com.sumavision.tetris.cs.channel.broad.ability.transcode.BroadTranscodeType;
import com.sumavision.tetris.cs.channel.broad.file.BroadFileBroadInfoService;
import com.sumavision.tetris.cs.channel.exception.ChannelBroadNoneOutputException;
import com.sumavision.tetris.cs.program.ProgramQuery;
import com.sumavision.tetris.cs.program.ProgramVO;
import com.sumavision.tetris.cs.program.ScreenVO;
import com.sumavision.tetris.cs.program.TemplateScreenVO;
import com.sumavision.tetris.cs.program.TemplateVO;
import com.sumavision.tetris.cs.schedule.ScheduleQuery;
import com.sumavision.tetris.cs.schedule.ScheduleVO;
import com.sumavision.tetris.cs.schedule.exception.ScheduleNoneToBroadException;
import com.sumavision.tetris.easy.process.stream.transcode.FileDealVO;
import com.sumavision.tetris.easy.process.stream.transcode.OutParamVO;
import com.sumavision.tetris.easy.process.stream.transcode.StreamTranscodeProfileVO;
import com.sumavision.tetris.easy.process.stream.transcode.StreamTranscodeQuery;
import com.sumavision.tetris.easy.process.stream.transcode.TaskVO;
import com.sumavision.tetris.mims.app.media.encode.MediaEncodeQuery;
import com.sumavision.tetris.mims.app.media.stream.audio.MediaAudioStreamService;
import com.sumavision.tetris.mims.app.media.stream.video.MediaVideoStreamService;
import com.sumavision.tetris.mvc.wrapper.CopyHeaderHttpServletRequestWrapper;
import com.sumavision.tetris.orm.exception.ErrorTypeException;
import com.sumavision.tetris.resouce.feign.bundle.BundleFeignService;
import com.sumavision.tetris.resouce.feign.bundle.BundleFeignVO;
import com.sumavision.tetris.user.UserQuery;
import com.sumavision.tetris.user.UserVO;
import com.sumavision.tetris.websocket.message.WebsocketMessageService;
import com.sumavision.tetris.websocket.message.WebsocketMessageType;

@Service
@Transactional(rollbackFor = Exception.class)
public class BroadAbilityService {
	@Autowired
	private BroadAbilityQuery broadAbilityQuery;
	
	@Autowired
	private ChannelDAO channelDao;
	
	@Autowired
	private ChannelService channelService;
	
	@Autowired
	private ChannelQuery channelQuery;
	
	@Autowired
	private ProgramQuery programQuery;
	
	@Autowired
	private ScheduleQuery scheduleQuery;
	
	@Autowired
	private BroadAbilityBroadInfoService broadAbilityBroadInfoService;
	
	@Autowired
	private BroadFileBroadInfoService broadFileBroadInfoService;
	
	@Autowired
	private BroadAbilityRemoteDAO broadAbilityRemoteDAO;
	
	@Autowired
	private BroadTranscodeTaskDAO broadTranscodeTaskDAO;
	
	@Autowired
	private BroadTranscodeOutputDAO broadTranscodeOutputDAO;
	
	@Autowired
	private ChannelAutoBroadInfoDAO channelAutoBroadInfoDAO;
	
	@Autowired
	private AbilityInfoSendDAO abilityInfoSendDAO;
	
	@Autowired
	private Adapter adapter;
	
	@Autowired
	private UserQuery userQuery;
	
	@Autowired
	private MediaEncodeQuery mediaEncodeQuery;
	
	@Autowired
	private MediaVideoStreamService mediaVideoStreamService;
	
	@Autowired
	private MediaAudioStreamService mediaAudioStreamService;
	
	@Autowired
	private WebsocketMessageService websocketMessageService;
	
	@Autowired
	private PushService pushService;
	
	@Autowired
	private StreamTranscodeQuery streamTranscodeQuery;
	
	@Autowired
	private BundleFeignService bundleFeignService;
	
	private Map<Long, List<ScheduledFuture<?>>> channelScheduleMap = new HashMapWrapper<Long, List<ScheduledFuture<?>>>().getMap();
	
	private ScheduledExecutorService channelScheduledExecutorService = Executors.newScheduledThreadPool(6);
	
	/**
	 * 添加频道<br/>
	 * <b>作者:</b>lzp<br/>
	 * <b>版本：</b>1.0<br/>
	 * <b>日期：</b>2019年6月25日 上午11:06:57
	 * @param String name 频道名称
	 * @param String date 日期
	 * @param String broadWay 播发方式(参考BroadWay枚举)
	 * @param ChannelTyep type 平台添加还是其他服务添加
	 * @param Boolean encryption 是否加密播发(仅限轮播推流)
	 * @param Boolean autoBroad 是否智能播发
	 * @param Boolean autoBroadShuffle 是否乱序(仅限智能播发)
	 * @param Integer autoBroadDuration 生效时长(仅限智能播发，单位：天)
	 * @param String autoBroadStart 智能播发生效时间(仅限智能播发)
	 * @param String outputUserPort 能力的输出地址port(仅限轮播推流)
	 * @param List<UserVO> outputUserList 预播发的用户列表
	 * List<BroadAbilityBroadInfoVO> abilityBroadInfoVOs 预播发的ip和port对(仅限轮播推流)
	 * @param String remark 备注
	 * @return ChannelPO 频道
	 */
	public ChannelPO add(
			UserVO user,
			String name,
			String date,
			String broadWay,
			String remark,
			ChannelType type,
			Boolean encryption,
			Boolean autoBroad,
			Boolean autoBroadShuffle,
			Integer autoBroadDuration,
			String autoBroadStart,
			String outputUserPort,
			List<UserVO> outputUserList,
			List<BroadAbilityBroadInfoVO> abilityBroadInfoVOs) throws Exception {
		BroadWay channelBroadWay = BroadWay.fromName(broadWay);
		ChannelPO channel = new ChannelPO();
		channel.setName(name);
		channel.setRemark(remark);
		channel.setDate(date);
		channel.setBroadWay(channelBroadWay.getName());
		channel.setBroadcastStatus(ChannelBroadStatus.CHANNEL_BROAD_STATUS_INIT.getName());
		channel.setGroupId(user.getGroupId());
		channel.setUpdateTime(new Date());
		channel.setEncryption(encryption);
		channel.setAutoBroad(autoBroad);
		channel.setType(type.toString());
		
		//校验用户是否被占用
		broadAbilityBroadInfoService.checkUserUse(null, outputUserList);
		broadFileBroadInfoService.checkUserUse(null, outputUserList);
		
		//校验ip和端口对是否被占用，设置轮播推流id
		broadAbilityBroadInfoService.checkIpAndPortExists(null, abilityBroadInfoVOs);
		channel.setAbilityBroadId(adapter.getNewId(channelDao.getAllAbilityId()));

		channelDao.save(channel);
		
		//保存播发输出信息
		List<BroadAbilityBroadInfoVO> saveinInfoVOs = new ArrayList<BroadAbilityBroadInfoVO>();
		if (abilityBroadInfoVOs != null && !abilityBroadInfoVOs.isEmpty()) saveinInfoVOs.addAll(abilityBroadInfoVOs);
		saveinInfoVOs.addAll(broadAbilityBroadInfoService.changeVO(new SetOutputBO().setOutputUsers(outputUserList).setOutputUserPort(outputUserPort)));
		broadAbilityBroadInfoService.saveInfoList(channel.getId(), saveinInfoVOs);
		
		//保存智能播发信息
		if (autoBroad) {
			ChannelAutoBroadInfoPO autoBroadInfoPO = new ChannelAutoBroadInfoPO();
			autoBroadInfoPO.setUpdateTime(new Date());
			autoBroadInfoPO.setChannelId(channel.getId());
			autoBroadInfoPO.setShuffle(autoBroadShuffle);
			autoBroadInfoPO.setDuration(autoBroadDuration);
			autoBroadInfoPO.setStartTime(autoBroadStart);
			autoBroadInfoPO.setStartDate(DateUtil.getYearmonthDay(new Date()));
			channelAutoBroadInfoDAO.save(autoBroadInfoPO);
		}

		return channel;
	}
	
	public void startAbilityBroadcast(Long channelId) throws Exception {
		ChannelPO channel = channelQuery.findByChannelId(channelId);
		if (ChannelType.YJGB.toString().equals(channel.getType())) {
			startAbilityYjgbBroad(channelId);
		} else if (ChannelType.REMOTE.toString().equals(channel.getType())){
			startAbilityBroadTimer(channelId);
		} else {
			startAbilityBroadSchedule(channelId);
		}
	}
	
	public void seekAbilityBroadcast(ChannelPO channel, Long duration) throws Exception {
		broadAbilityQuery.sendAbilityRequest(BroadAbilityQueryType.SEEK, channel, duration);
	}
	
	/**
	 * 停止播发(能力播发)<br/>
	 * <b>作者:</b>lzp<br/>
	 * <b>版本：</b>1.0<br/>
	 * <b>日期：</b>2019年6月25日 上午11:06:57
	 * @param Long channelId 频道id
	 */
	public void stopAbilityBroadcast(Long channelId) throws Exception{
		ChannelPO channel = channelQuery.findByChannelId(channelId);
		if (channelScheduleMap.containsKey(channelId)) {
			List<ScheduledFuture<?>> scheduledFutures = channelScheduleMap.get(channelId);
			for (ScheduledFuture<?> scheduledFuture : scheduledFutures) {
				scheduledFuture.cancel(true);
			}
		}
		
		Boolean stop = false;
		if (channel.getType().equals(ChannelType.REMOTE.toString())) {
			BroadAbilityRemotePO remotePO = broadAbilityRemoteDAO.findByChannelId(channelId);
			if (remotePO != null && remotePO.getBroadStreamWay() == BroadStreamWay.ABILITY_FILE_STREAM_TRANSCODE) stop = true;
		} else {
			//停止所有流转码能力任务
			List<AbilityInfoSendPO> abilityBroadInfoPOs = abilityInfoSendDAO.findByChannelId(channelId);
			List<String> taskIds = new ArrayList<String>();
			for (AbilityInfoSendPO infoSendPO : abilityBroadInfoPOs) {
				String taskId = infoSendPO.getTaskId();
				if (taskId != null && !taskIds.contains(taskId)) {
					taskIds.add(taskId);
				}
			}
			if (!taskIds.isEmpty()) {
				System.out.println(new Date() + "; delete: " + JSONArray.toJSONString(taskIds));
				pushService.batchDeleteTask(taskIds);
				abilityInfoSendDAO.deleteInBatch(abilityBroadInfoPOs);
			}
		}
		
		stop = true;
//		if (!stop && broadAbilityQuery.sendAbilityRequest(BroadAbilityQueryType.STOP, channel, null, null)) stop = true;
		if (stop) {
			channel.setBroadcastStatus(ChannelBroadStatus.CHANNEL_BROAD_STATUS_BROADED.getName());
			channelDao.save(channel);
			if (channel.getType().equals(ChannelType.REMOTE.toString())) {
				channelService.remove(channelId);
			}
		}
	}
	
	/**
	 * 播发时添加<br/>
	 * <b>作者:</b>lzp<br/>
	 * <b>版本：</b>1.0<br/>
	 * <b>日期：</b>2019年11月28日 上午11:02:56
	 * @param channelId
	 * @throws Exception
	 */
	public void addScheduleDeal(Long channelId) throws Exception {
//		stopAbilityBroadcast(channelId);
//		startAbilityBroadcast(channelId);
	}
	
	private void startAbilityBroadSchedule(Long channelId) throws Exception {
		ChannelPO channel = channelQuery.findByChannelId(channelId);
		String forwardStatus = channel.getBroadcastStatus();
		if (ChannelBroadStatus.CHANNEL_BROAD_STATUS_BROADING.getName().equals(forwardStatus)) {
			stopAbilityBroadcast(channelId);
		}
		
		List<ScheduleVO> scheduleVOs = scheduleQuery.getByChannelId(channelId);
		if (scheduleVOs == null || scheduleVOs.isEmpty()) throw new ScheduleNoneToBroadException(channel.getName());
		List<BroadAbilityBroadInfoVO> broadAbilityBroadInfoVOs = broadAbilityBroadInfoService.queryFromChannelId(channelId);
		if (broadAbilityBroadInfoVOs == null || broadAbilityBroadInfoVOs.isEmpty()) return;
		
		//发webSocket通知
		List<UserVO> webSocketSendUsers = new ArrayList<UserVO>();
		for (BroadAbilityBroadInfoVO broadAbilityBroadInfoVO : broadAbilityBroadInfoVOs) {
			Long userId = broadAbilityBroadInfoVO.getUserId();
			if (userId != null) {
				List<UserVO> userVOs = userQuery.findByIdInAndType(new ArrayListWrapper<Long>().add(userId).getList(), TerminalType.QT_MEDIA_EDITOR.getName());
				if (userVOs != null && !userVOs.isEmpty()) {
					UserVO user = userVOs.get(0);
					webSocketSendUsers.add(user);
					broadAbilityBroadInfoVO.setPreviewUrlIp(user.getIp());
				}
			}
		}
		if (!webSocketSendUsers.isEmpty()) sendWebSocket(webSocketSendUsers, channel);
		
//		ServletRequestAttributes attributes = (ServletRequestAttributes)RequestContextHolder.getRequestAttributes();
//		final CopyHeaderHttpServletRequestWrapper request = new CopyHeaderHttpServletRequestWrapper(attributes.getRequest());
		
		//从流转码feign获取能力ip
		StreamTranscodeProfileVO streamTranscodeProfileVO = streamTranscodeQuery.getProfile();
		String abilityIp = streamTranscodeProfileVO.getToolIp();
//		从资源微服务feign获取能力ip
//		List<BundleFeignVO> abilityList = bundleFeignService.queryTranscodeDevice();
//		if (abilityList != null && !abilityList.isEmpty()) abilityIp = abilityList.get(0).getDeviceIp();
		
		Boolean broad = false;
		//遍历排期单
		for (int i = 0; i < scheduleVOs.size(); i++) {
			final ScheduleVO scheduleVO = scheduleVOs.get(i);
			//当前long时间
			Long now = DateUtil.getLongDate();
			//开始播发"yyyy-MM-dd HH:mm:ss"时间
			Date broadDate = DateUtil.parse(scheduleVO.getBroadDate(), DateUtil.dateTimePattern);
			//开始播发long时间
			final Long broadDateLong = broadDate.getTime();
			//单节目单结束时间
			Long finishTime = scheduleVO.getEndDate() != null && !scheduleVO.getEndDate().isEmpty() ? DateUtil.parse(scheduleVO.getEndDate(), DateUtil.dateTimePattern).getTime()
					: broadDateLong + querySchedulePlayTime(scheduleVO.getId());
			
			if (finishTime < now) continue;
			
			//获取分屏信息
//			JSONObject useTemplate = adapter.screenTemplate(scheduleVO.getProgram().getScreenId());
//			JSONArray screens = useTemplate.getJSONArray("screen");
			TemplateVO template = programQuery.getScreen2Template(scheduleVO.getId(), adapter.getAllTemplate());
			if (template == null) return;
			
			List<BroadAbilityBroadRequestVO> broadRequestVOs = new ArrayList<BroadAbilityBroadRequestVO>();
			Integer outputIndex = 0;
			//遍历分屏，获取各分屏信息和节目单数组
			List<TemplateScreenVO> templateScreenVOs = template.getScreen();
			for (TemplateScreenVO templateVO : templateScreenVOs) {
				List<ScreenVO> screenVOs = templateVO.getData();
				BroadAbilityBroadRequestVO broadRequestVO = new BroadAbilityBroadRequestVO();
				List<BroadAbilityBroadRequestInputPrevVO> requestInputPrevVOs = new ArrayList<BroadAbilityBroadRequestInputPrevVO>();
				if (screenVOs.isEmpty()) continue;
				Collections.sort(screenVOs, new ScreenVO.ScreenVOOrderComparator());
				String mediaType = "";
				//遍历节目单数组，添加任务输入
				for (ScreenVO screenVO : screenVOs) {
					String type = screenVO.getType();
					String screenDuration = screenVO.getDuration();
					if (screenDuration == null || screenDuration.isEmpty() || screenDuration.equals("-")) continue;
					if (type != null) {
						switch (type) {
						case "AUDIO":
						case "VIDEO":
							BroadAbilityBroadRequestInputPrevFileVO inputPrevFileVO = new BroadAbilityBroadRequestInputPrevFileVO();
							inputPrevFileVO.setCount(1)
							.setUrl(adapter.changeHttpToFtp(screenVO.getPreviewUrl()))
							.setDuration(Long.parseLong(screenDuration))
							.setSeek(0l);
							requestInputPrevVOs.add(new BroadAbilityBroadRequestInputPrevVO().setType("file").setFile(inputPrevFileVO));
							break;
						case "AUDIO_STREAM":
						case "VIDEO_STREAM":
							BroadAbilityBroadRequestInputPrevStreamVO inputStreamVO = new BroadAbilityBroadRequestInputPrevStreamVO().setPcm("udp").setDuration(Long.parseLong(screenDuration));
							if (type.equals("AUDIO_STREAM")) {
								inputStreamVO.setUrl(screenVO.getPreviewUrl());
							} else {
								String url = screenVO.getPreviewUrl();
								if (url.isEmpty()) continue;
								List<String> urls = JSONArray.parseArray(url, String.class);
								if (urls.isEmpty()) continue;
								inputStreamVO.setUrl(urls.get(0));
							}
							requestInputPrevVOs.add(new BroadAbilityBroadRequestInputPrevVO().setType("stream").setStream(inputStreamVO));
						default:
							break;
						}
						if (mediaType.isEmpty()) {
							if ("AUDIO".equals(type) || "AUDIO_STREAM".equals(type)) mediaType = "audio";
							if ("VIDEO".equals(type) || "VIDEO_STREAM".equals(type)) mediaType = "video";
						}
					}
				}
				if (!requestInputPrevVOs.isEmpty()) {
					broadRequestVO.setSources(requestInputPrevVOs);
					List<BroadAbilityBroadRequestOutputVO> outputVOs = new ArrayList<BroadAbilityBroadRequestOutputVO>();
					//获取输出信息
					for (BroadAbilityBroadInfoVO broadAbilityBroadInfoVO : broadAbilityBroadInfoVOs) {
						String ip = broadAbilityBroadInfoVO.getPreviewUrlIp();
						String port = "";
						if (outputIndex == 0) {
							port = broadAbilityBroadInfoVO.getPreviewUrlPort();
						} else {
							String endPortString = broadAbilityBroadInfoVO.getPreviewUrlEndPort();
							if (endPortString == null || endPortString.isEmpty()) continue;
							Integer startPort = Integer.parseInt(broadAbilityBroadInfoVO.getPreviewUrlPort());
							Integer endPort = Integer.parseInt(endPortString);
							if (startPort + outputIndex <= endPort) {
								port = "" + (startPort + outputIndex);
							}
						}
						if (ip != null && !ip.isEmpty() && !port.isEmpty()) {
							outputVOs.add(new BroadAbilityBroadRequestOutputVO()
									.setUrl(new StringBufferWrapper().append("udp://@").append(ip).append(":").append(port).toString())
									.setType("udp"));
						}
					}
					if (!outputVOs.isEmpty()) {
						broadRequestVO.setOutput(outputVOs).setMediaType(mediaType).setDeviceIp(abilityIp);
						broadRequestVOs.add(broadRequestVO);
						outputIndex++;
					}
				}
			}
			
			now = DateUtil.getLongDate();
			if (finishTime < now || broadRequestVOs.isEmpty()) continue;
			List<Long> previewIdList = new ArrayList<Long>();
			//遍历每个分屏任务
			for (BroadAbilityBroadRequestVO requestVO : broadRequestVOs) {
				List<BroadAbilityBroadRequestInputPrevVO> inputVOs = requestVO.getSources();
				requestVO.setSources(null);
				
				//是否是第一个定时器，第一个定时器计算是否跳过和seek，发送添加任务
				Boolean first = true;
				int firstIndex = 0;
				Long firstDuration = 0l;
				//记录各节目切换预留时间
				Long forwardDealTime = 0l;
				//所有时长相加，用于计算流的开始结束绝对时间
				Long total = 0l;
				//下一个切换节目线程等待时间
				Long nextDelayTime = DateUtil.getLongDate() > broadDateLong ? 0l : broadDateLong - DateUtil.getLongDate();
				//当前排期单开始时间早于当前时间
				Long passed = nextDelayTime == 0 ? DateUtil.getLongDate() - broadDateLong : 0;
				//切换源预留时间
				Long dealTime = 2000l;
				//标记当前任务
				Long previewId = DateUtil.getLongDate() + broadRequestVOs.indexOf(requestVO);
				previewIdList.add(previewId);
				//遍历该分屏下的所有节目
				for (int j = 0; j < inputVOs.size(); j++) {
					BroadAbilityBroadRequestInputPrevVO inputPrevVO = inputVOs.get(j);
					BroadAbilityBroadRequestInputPrevFileVO file = inputPrevVO.getFile();
					BroadAbilityBroadRequestInputPrevStreamVO stream = inputPrevVO.getStream();
					Long duration = 0l;
					if (file != null) {
						duration = file.getDuration(); 
					} else if (stream != null){
						duration = stream.getDuration();
					}
					if (duration != null && duration != 0) {
						if (first) {
							if (passed > 0 && duration <= passed) {
								passed = passed - duration;
							} else {
								//实际播发的第一个节目
								broad = true;
								firstDuration = duration - passed;
								if (file != null) {
									file.setDuration(firstDuration).setSeek(passed);
									file.setStartTime(DateUtil.format(DateUtil.getDateByMillisecond(broadDateLong + total), DateUtil.dateTimePattern));
								} else {
									stream.setStartTime(DateUtil.format(DateUtil.getDateByMillisecond(broadDateLong + total), DateUtil.dateTimePattern));
									stream.setEndTime(DateUtil.format(DateUtil.getDateByMillisecond(broadDateLong + total + duration), DateUtil.dateTimePattern));
								}
								requestVO.setInput(new BroadAbilityBroadRequestInputVO().setPrev(inputPrevVO));
								firstIndex = j;
								first = false;
							}
							total += duration;
						} else {
							//第二个节目
							Long newForwardDealTime = duration > 10000 ? 10000 : (duration / 2);
							if (firstIndex + 1 == j) {
								if (stream != null) {
									stream.setStartTime(DateUtil.format(DateUtil.getDateByMillisecond(broadDateLong + total), DateUtil.dateTimePattern));
									stream.setEndTime(DateUtil.format(DateUtil.getDateByMillisecond(broadDateLong + total + duration), DateUtil.dateTimePattern));
								}
								requestVO.getInput().setNext(inputPrevVO);
								requestAddTask(channelId, 0l, JSONObject.toJSONString(requestVO), previewId, abilityIp);
								nextDelayTime = nextDelayTime + firstDuration + duration - newForwardDealTime;
							} else {
								if (stream != null) {
									stream.setStartTime(DateUtil.format(DateUtil.getDateByMillisecond(broadDateLong + total), DateUtil.dateTimePattern));
									stream.setEndTime(DateUtil.format(DateUtil.getDateByMillisecond(broadDateLong + total + duration), DateUtil.dateTimePattern));
								}
								BroadAbilityBroadRequestExchangeVO exchangeVO = new BroadAbilityBroadRequestExchangeVO()
										.setMediaType(requestVO.getMediaType())
										.setProgram(inputPrevVO);
								requestExchangeTask(channelId, nextDelayTime, exchangeVO, previewId);
								nextDelayTime = nextDelayTime + duration + forwardDealTime - newForwardDealTime;
							}
							total += duration;
							forwardDealTime = newForwardDealTime;
						}
					} else {
						if (firstIndex + 1 == j) firstIndex++;
					}
					
					//单源节目单
					if (j == inputVOs.size() - 1 && requestVO.getInput() != null &&  requestVO.getInput().getPrev() != null && requestVO.getInput().getNext() == null) {
						requestAddTask(channelId, 0l, JSONObject.toJSONString(requestVO), previewId, abilityIp);
					}
				}
			}
			
			if (broad && !previewIdList.isEmpty()) {
				requestRemoveTask(channelId, finishTime, previewIdList, i == scheduleVOs.size() - 1);
			}
		}
		
		if (broad) {
			channel.setBroadcastStatus(ChannelBroadStatus.CHANNEL_BROAD_STATUS_BROADING.getName());
			channelDao.save(channel);
		}
	}
	
	public void requestAddTask(Long channelId,
			Long delay,
			String requestString,
			Long previewId,
			String abilityIp) {
		if (!channelScheduleMap.containsKey(channelId)) {
			channelScheduleMap.put(channelId, new ArrayList<ScheduledFuture<?>>());
		}
		List<ScheduledFuture<?>> futures = channelScheduleMap.get(channelId);
		
		ServletRequestAttributes attributes = (ServletRequestAttributes)RequestContextHolder.getRequestAttributes();
		final CopyHeaderHttpServletRequestWrapper request = new CopyHeaderHttpServletRequestWrapper(attributes.getRequest());
		System.out.println(new Date() + "; delay: " + delay + "; create: " + requestString);
		futures.add(channelScheduledExecutorService.schedule(new Runnable() {
			@Override
			public void run() {
				try {
					RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
					//下任务
					System.out.println(new Date() + ";create: " + requestString);
					String taskId = pushService.addPushTask(requestString);
					//保存任务信息
					AbilityInfoSendPO abilityInfoSendPO = new AbilityInfoSendPO();
					abilityInfoSendPO.setTaskId(taskId);
					abilityInfoSendPO.setPreviewId(previewId);
					abilityInfoSendPO.setChannelId(channelId);
					abilityInfoSendPO.setAbilityIp(abilityIp);
					abilityInfoSendDAO.save(abilityInfoSendPO);
	//					List<BroadAbilityBroadRequestOutputVO> broadRequestOutputVOs = broadAbilityBroadRequestVO.getOutput();
	//					for (BroadAbilityBroadRequestOutputVO outputVO : broadRequestOutputVOs) {
	//						AbilityInfoSendPO abilityInfoSendPO = new AbilityInfoSendPO();
	//						abilityInfoSendPO.setChannelId(channelId);
	//						abilityInfoSendPO.setBroadUrl(outputVO.getUrl());
	//						abilityInfoSendPO.setMediaType(broadAbilityBroadRequestInputVO.getType());
	//						MediaVideoStreamVO mediaVideoStream = mediaVideoStreamService.addVideoStreamTask(adapter.getUdpUrlFromIpAndPort(previewIp, previewPort), channelPO.getName());
	//						abilityInfoSendPO.setMediaId();
	//					}
					
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}, delay, TimeUnit.MILLISECONDS));
	}
	
	public void requestExchangeTask(Long channelId,
			Long delay,
			BroadAbilityBroadRequestExchangeVO exchangeVO,
			Long previewId) {
		if (!channelScheduleMap.containsKey(channelId)) {
			channelScheduleMap.put(channelId, new ArrayList<ScheduledFuture<?>>());
		}
		List<ScheduledFuture<?>> futures = channelScheduleMap.get(channelId);
		
		ServletRequestAttributes attributes = (ServletRequestAttributes)RequestContextHolder.getRequestAttributes();
		final CopyHeaderHttpServletRequestWrapper request = new CopyHeaderHttpServletRequestWrapper(attributes.getRequest());
		System.out.println(new Date() + "; delay: " + delay + "; exchange: " + JSONObject.toJSONString(exchangeVO));
		futures.add(channelScheduledExecutorService.schedule(new Runnable() {
			@Override
			public void run() {
				try {
					RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
					//下任务
					List<AbilityInfoSendPO> abilityInfoSendPOs = abilityInfoSendDAO.findByPreviewId(previewId);
					if (!abilityInfoSendPOs.isEmpty()) {
						String taskId = abilityInfoSendPOs.get(0).getTaskId();
						exchangeVO.setTaskId(taskId);
						//发送请求
						System.out.println(new Date() + ";exchange: " + JSONArray.toJSONString(exchangeVO));
						pushService.changePushBackup(JSONObject.toJSONString(exchangeVO));
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}, delay, TimeUnit.MILLISECONDS));
	}

	
	public void requestRemoveTask(Long channelId, Long finishTime, List<Long> previewIds, Boolean stopChannel) {
		if (!channelScheduleMap.containsKey(channelId)) {
			channelScheduleMap.put(channelId, new ArrayList<ScheduledFuture<?>>());
		}
		List<ScheduledFuture<?>> futures = channelScheduleMap.get(channelId);
		
		Long dealTime = 5000l;
		Long delay = finishTime - DateUtil.getLongDate();
		if (delay > 0) {
			ServletRequestAttributes attributes = (ServletRequestAttributes)RequestContextHolder.getRequestAttributes();
			final CopyHeaderHttpServletRequestWrapper request = new CopyHeaderHttpServletRequestWrapper(attributes.getRequest());
			System.out.println(new Date() + "; delay: " + delay + "; delete: " + JSONArray.toJSONString(previewIds));
			futures.add(channelScheduledExecutorService.schedule(new Runnable() {
				
				@Override
				public void run() {
					try {
						RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
						List<String> taskIds = new ArrayList<String>();
						for (Long previewId : previewIds) {
							List<AbilityInfoSendPO> abilityInfoSendPOs = abilityInfoSendDAO.findByPreviewId(previewId);
							if (!abilityInfoSendPOs.isEmpty()) {
								taskIds.add(abilityInfoSendPOs.get(0).getTaskId());
							}
						}
						System.out.println(new Date() + ";delete: " + JSONArray.toJSONString(taskIds));
						//发停止
						pushService.batchDeleteTask(taskIds);
						//置状态
						if (stopChannel != null && stopChannel) {
							//停止所有流转码能力任务
							List<AbilityInfoSendPO> abilityBroadInfoPOs = abilityInfoSendDAO.findByChannelId(channelId);
							List<String> channelTaskIds = new ArrayList<String>();
							for (AbilityInfoSendPO infoSendPO : abilityBroadInfoPOs) {
								String taskId = infoSendPO.getTaskId();
								if (taskId != null && !channelTaskIds.contains(taskId)) {
									channelTaskIds.add(taskId);
								}
							}
							if (!channelTaskIds.isEmpty()) {
								System.out.println(new Date() + "; delete: " + JSONArray.toJSONString(taskIds));
								pushService.batchDeleteTask(taskIds);
								abilityInfoSendDAO.deleteInBatch(abilityBroadInfoPOs);
							}
							ChannelPO channel = channelQuery.findByChannelId(channelId);
							channel.setBroadcastStatus(ChannelBroadStatus.CHANNEL_BROAD_STATUS_BROADED.getName());
							channelDao.save(channel);
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}, delay + dealTime, TimeUnit.MILLISECONDS));
		}
	}
	
	/**
	 * 循环获取节目单下发<br/>
	 * <b>作者:</b>lzp<br/>
	 * <b>版本：</b>1.0<br/>
	 * <b>日期：</b>2019年11月1日 下午4:51:09
	 * @param channelId
	 */
	private void startAbilityBroadTimer(Long channelId) throws Exception {
		ChannelPO channel = channelQuery.findByChannelId(channelId);
		String forwardStatus = channel.getBroadcastStatus();
		if (ChannelBroadStatus.CHANNEL_BROAD_STATUS_BROADING.getName().equals(forwardStatus)) {
			stopAbilityBroadcast(channelId);
		}
		
		//以防用户选择"此刻"为播发时间的时候，由于手动点击"播发"和程序执行时间导致播发不生效的预留时间
		Long dealTime = 2000l;
		//转换能力添加任务处理时间
		Long abilityDealTime = 10000l;
		
		List<ScheduleVO> scheduleVOs = scheduleQuery.getByChannelId(channelId);
		if (scheduleVOs == null || scheduleVOs.isEmpty()) throw new ScheduleNoneToBroadException(channel.getName());
		List<BroadAbilityBroadInfoVO> broadAbilityBroadInfoVOs = broadAbilityBroadInfoService.queryFromChannelId(channelId);
		if (broadAbilityBroadInfoVOs == null || broadAbilityBroadInfoVOs.isEmpty()) return;
		
		//发webSocket通知
		List<UserVO> webSocketSendUsers = new ArrayList<UserVO>();
		String streamUrlPort = "";
		for (BroadAbilityBroadInfoVO broadAbilityBroadInfoVO : broadAbilityBroadInfoVOs) {
			Long userId = broadAbilityBroadInfoVO.getUserId();
			if (userId != null) {
				List<UserVO> userVOs = userQuery.findByIdInAndType(new ArrayListWrapper<Long>().add(userId).getList(), TerminalType.QT_MEDIA_EDITOR.getName());
				if (userVOs != null && !userVOs.isEmpty()) {
					UserVO user = userVOs.get(0);
					webSocketSendUsers.add(user);
					if (streamUrlPort.isEmpty()) streamUrlPort = broadAbilityBroadInfoVO.getPreviewUrlPort();
					broadAbilityBroadInfoVO.setPreviewUrlIp(user.getIp());
				}
			}
		}
		if (!webSocketSendUsers.isEmpty()) sendWebSocket(webSocketSendUsers, scheduleVOs, streamUrlPort, channel);
		
		//查看使用的能力
		BroadStreamWay broadStreamWay = BroadStreamWay.ABILITY_STREAM;
		if (channel.getType().equals(ChannelType.REMOTE.toString())) {
			BroadAbilityRemotePO remotePO = broadAbilityRemoteDAO.findByChannelId(channelId);
			if (remotePO != null && remotePO.getBroadStreamWay() != null) broadStreamWay = remotePO.getBroadStreamWay();
		}
		
		ServletRequestAttributes attributes = (ServletRequestAttributes)RequestContextHolder.getRequestAttributes();
		final CopyHeaderHttpServletRequestWrapper request = new CopyHeaderHttpServletRequestWrapper(attributes.getRequest());
		//是否是播发的第一个节目单标记，头一次下发需立即向能力发送请求，避免删除的时候能力没有该条任务存在
		Boolean first = true;
		for (int i = 0; i < scheduleVOs.size(); i++) {
			final ScheduleVO scheduleVO = scheduleVOs.get(i);
			List<ScreenVO> screenVOs = scheduleVO.getProgram().getScreenInfo();
			if (screenVOs == null || screenVOs.isEmpty()) continue;
			if (!channelScheduleMap.containsKey(channelId)) {
				channelScheduleMap.put(channelId, new ArrayList<ScheduledFuture<?>>());
			}
			List<ScheduledFuture<?>> futures = channelScheduleMap.get(channelId);
			//当前long时间
			Long now = DateUtil.getLongDate();
			//开始播发"yyyy-MM-dd HH:mm:ss"时间
			Date broadDate = DateUtil.parse(scheduleVO.getBroadDate(), DateUtil.dateTimePattern);
			//开始播发long时间
			final Long broadDateLong = broadDate.getTime();
			//单节目单结束时间
			Long finishTime = scheduleVO.getEndDate() != null ? DateUtil.parse(scheduleVO.getEndDate(), DateUtil.dateTimePattern).getTime()
					: broadDateLong + querySchedulePlayTime(scheduleVO.getId());
			
			if (finishTime < now) continue;
			if (broadStreamWay != BroadStreamWay.ABILITY_FILE_STREAM_TRANSCODE){
				final BroadAbilityQueryType type;
				if (first) {
					switch (ChannelBroadStatus.fromName(forwardStatus)) {
					case CHANNEL_BROAD_STATUS_BROADED:
						type = BroadAbilityQueryType.CHANGE;
						break;
					case CHANNEL_BROAD_STATUS_INIT:
						type = BroadAbilityQueryType.NEW;
						break;
					default:
						throw new ErrorTypeException("播发状态", forwardStatus);
					}
				} else {
					type = BroadAbilityQueryType.COVER;
				}
				
				futures.add(channelScheduledExecutorService.schedule(new Runnable() {
					@Override
					public void run() {
						try {
							RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
							System.out.println(channelId + ":" + broadDate + ";" + type.getCmd());
							startSendSchedule(channelId, scheduleVO.getId(), broadAbilityBroadInfoVOs, type);
							Long nNow = DateUtil.getLongDate();
							if (broadDateLong <= nNow) {
								startSendStream(channelId);
								if (nNow - broadDateLong > dealTime) {
									seekAbilityBroadcast(channel, (nNow - broadDateLong) / 1000);
								}
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}, first ? 0 : broadDateLong - DateUtil.getLongDate(), TimeUnit.MILLISECONDS));
				
				if (first) {
					//如果首个任务是当前时间往后的，则添加定时器执行开始
					if (broadDateLong > now) {
						futures.add(channelScheduledExecutorService.schedule(new Runnable() {
							
							@Override
							public void run() {
								try {
									RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
									System.out.println(channelId + ":" + broadDate + ";start");
									startSendStream(channelId);
								} catch (Exception e) {
									e.printStackTrace();
								}
							}
						}, broadDateLong - now, TimeUnit.MILLISECONDS));
					}
				}
			}
			
			first = false;
			
			//只有REMOTE类型频道才会自动停止，其他频道继续监听节目单
			if (i == scheduleVOs.size() - 1) {
				if (finishTime > now) {
					if (channel.getType().equals(ChannelType.REMOTE.toString())) {
						futures.add(channelScheduledExecutorService.schedule(new Runnable() {
							
							@Override
							public void run() {
								try {
									System.out.println("排期单遍历结束");
									BroadAbilityRemotePO remotePO = broadAbilityRemoteDAO.findByChannelId(channelId);
									if (remotePO == null) return;
									String url = remotePO.getStopCallbackUrl();
									System.out.println("回调：" + url);
									if (url != null && !url.isEmpty()) {
										HttpRequestUtil.httpGet(new StringBufferWrapper().append(url).toString());
									}
								} catch (Exception e) {
									e.printStackTrace();
								}
							}
						}, finishTime - now + abilityDealTime, TimeUnit.MILLISECONDS));
					}
				}
			}

		}
		
		if (!first) {
			channel.setBroadcastStatus(ChannelBroadStatus.CHANNEL_BROAD_STATUS_BROADING.getName());
			channelDao.save(channel);
		}
	}
	
	/**
	 * 停止播发(线程池使用测试)<br/>
	 * <b>作者:</b>lzp<br/>
	 * <b>版本：</b>1.0<br/>
	 * <b>日期：</b>2019年6月25日 上午11:06:57
	 * @param Long channelId 频道id
	 */
	public void stopAbilityYjgbBroadcast(Long channelId) throws Exception{
		ChannelPO channel = channelQuery.findByChannelId(channelId);
		if (channelScheduleMap.containsKey(channelId)) {
			List<ScheduledFuture<?>> scheduledFutures = channelScheduleMap.get(channelId);
			for (ScheduledFuture<?> scheduledFuture : scheduledFutures) {
				scheduledFuture.cancel(true);
			}
		}
		//停止文件转流任务
		BroadAbilityRemotePO abilityRemotePO = broadAbilityRemoteDAO.findByChannelId(channelId);
		String taskId = abilityRemotePO.getProcessInstanceId();
		if (taskId != null && !taskId.isEmpty()) {
			streamTranscodeQuery.deleteTask(taskId);
		}
		//停止流转码任务
		deleteStreamTaskByChannelId(channelId);
		if (abilityRemotePO.getBroadStreamWay() != BroadStreamWay.ABILITY_STREAM
				|| broadAbilityQuery.sendAbilityRequest(BroadAbilityQueryType.STOP, channel, null, null)) {
			channel.setBroadcastStatus(ChannelBroadStatus.CHANNEL_BROAD_STATUS_BROADED.getName());
			channelDao.save(channel);
		}
	}
	
	/**
	 * 播发应急广播平台下发的排期单(线程池使用测试)<br/>
	 * <b>作者:</b>lzp<br/>
	 * <b>版本：</b>1.0<br/>
	 * <b>日期：</b>2020年2月28日 下午4:56:51
	 * @param channelId
	 * @throws Exception
	 */
	public void startAbilityYjgbBroad(Long channelId) throws Exception {
		ChannelPO channel = channelQuery.findByChannelId(channelId);
		String forwardStatus = channel.getBroadcastStatus();
		if (ChannelBroadStatus.CHANNEL_BROAD_STATUS_BROADING.getName().equals(forwardStatus)) {
			stopAbilityYjgbBroadcast(channelId);
		}
		
		//以防用户选择"此刻"为播发时间的时候，由于手动点击"播发"和程序执行时间导致播发不生效的预留时间
		Long dealTime = 2000l;
		//转换能力添加任务处理时间
		Long abilityDealTime = 10000l;
		
		List<ScheduleVO> scheduleVOs = scheduleQuery.getByChannelId(channelId);
		if (scheduleVOs == null || scheduleVOs.isEmpty()) throw new ScheduleNoneToBroadException(channel.getName());
		List<BroadAbilityBroadInfoVO> broadAbilityBroadInfoVOs = broadAbilityBroadInfoService.queryFromChannelId(channelId);
		if (broadAbilityBroadInfoVOs == null || broadAbilityBroadInfoVOs.isEmpty()) return;
		
		ServletRequestAttributes attributes = (ServletRequestAttributes)RequestContextHolder.getRequestAttributes();
		final CopyHeaderHttpServletRequestWrapper request = new CopyHeaderHttpServletRequestWrapper(attributes.getRequest());
		//是否是播发的第一个节目单标记，头一次下发需立即向能力发送请求，避免删除的时候能力没有该条任务存在
		Boolean first = true;
		//标记前一个任务类型，是流转码还是文件转流转码
		Boolean streamBroad = false;
		Boolean fileBroad = false;
		for (int i = 0; i < scheduleVOs.size(); i++) {
			final ScheduleVO scheduleVO = scheduleVOs.get(i);
			List<ScreenVO> screenVOs = scheduleVO.getProgram().getScreenInfo();
			if (screenVOs == null || screenVOs.isEmpty()) continue;
			ScreenVO screen = screenVOs.get(0);
			if (!channelScheduleMap.containsKey(channelId)) {
				channelScheduleMap.put(channelId, new ArrayList<ScheduledFuture<?>>());
			}
			List<ScheduledFuture<?>> futures = channelScheduleMap.get(channelId);
			//当前long时间
			Long now = DateUtil.getLongDate();
			//开始播发"yyyy-MM-dd HH:mm:ss"时间
			Date broadDate = DateUtil.parse(scheduleVO.getBroadDate(), DateUtil.dateTimePattern);
			//开始播发long时间
			final Long broadDateLong = broadDate.getTime();
			//单节目单结束时间
			Long finishTime = scheduleVO.getEndDate() != null ? DateUtil.parse(scheduleVO.getEndDate(), DateUtil.dateTimePattern).getTime()
					: broadDateLong + querySchedulePlayTime(scheduleVO.getId());
			if (finishTime < now) continue;
			
			final BroadStreamWay broadWay;
			final FileDealVO fileDealVO;
			if (screen.getType().equals("AUDIO") || screen.getType().equals("VIDEO")) {
				fileDealVO = streamTranscodeQuery.checkEdit(screen.getMimsUuid());
				broadWay = fileDealVO.getIfFileStream() ? BroadStreamWay.ABILITY_FILE_STREAM_TRANSCODE : BroadStreamWay.ABILITY_STREAM;
			} else {
				fileDealVO = null;
				broadWay = BroadStreamWay.ABILITY_STREAM_TRANSCODE;
			}
			if (broadWay != BroadStreamWay.ABILITY_STREAM) {
				Long delay = broadDateLong - DateUtil.getLongDate();
				System.out.println("添加流转码线程：" + broadDate + ";延时:" + delay);
				futures.add(channelScheduledExecutorService.schedule(new Runnable() {
					
					@Override
					public void run() {
						try {
							System.out.println(new Date() + ": prepare to create");
							RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
							//删除文件转流转码输出
							BroadAbilityRemotePO abilityRemotePO = broadAbilityRemoteDAO.findByChannelId(channelId);
							if (abilityRemotePO != null) {
								String fileStreamTaskId = abilityRemotePO.getProcessInstanceId();
								System.out.println(new Date() + ": delete all out put: " + fileStreamTaskId);
								if (fileStreamTaskId != null && !fileStreamTaskId.isEmpty()) streamTranscodeQuery.deleteAllOutput(fileStreamTaskId);
							}
							//删除流转码任务
							deleteStreamTaskByChannelId(channelId);
							//添加流转码任务
							String assetPath = screen.getPreviewUrl();
							TaskVO taskVO = broadAbilityQuery.queryTaskFromChannelId(channelId);
							
							String taskId = null;
							if (broadWay == BroadStreamWay.ABILITY_FILE_STREAM_TRANSCODE) {
								Long nNow = DateUtil.getLongDate();
								Integer seekPlayTime = 0;
								Long firstFileSeek = 0l;
								Long different = nNow - broadDateLong;
								if (different > dealTime) {
									String screenDuration = screen.getDuration();
									Long duration = screenDuration != null && !screenDuration.isEmpty() && !screenDuration.equals("-")
											? Long.parseLong(screenDuration) : fileDealVO.getDuration();
									if (duration == null || duration == 0l) return;
									seekPlayTime = (int)(different / duration);
									firstFileSeek = different - (duration * seekPlayTime);
								}
								taskId = streamTranscodeQuery.addFileTask(fileDealVO.getFileUrl(), screenVOs.size() - seekPlayTime, firstFileSeek, false, null, screen.getType().equals("AUDIO_STREAM") ? "audio" : "video", null, null, abilityRemotePO != null ? abilityRemotePO.getDeviceIp() : "", JSONObject.toJSONString(taskVO), null);
								System.out.println(new Date() + ": create finish: " + taskId);
							} else if (broadWay == BroadStreamWay.ABILITY_STREAM_TRANSCODE) {
								taskId = streamTranscodeQuery.addTask(null, assetPath, false, null, screen.getType().equals("AUDIO_STREAM") ? "audio" : "video", null, null, abilityRemotePO != null ? abilityRemotePO.getDeviceIp() : "", JSONObject.toJSONString(taskVO), null);
								System.out.println(new Date() + ": create finish: " + taskId);
							}
							if (taskId == null) return;
							BroadTranscodeTaskPO broadTranscodeTaskPO = new BroadTranscodeTaskPO();
							broadTranscodeTaskPO.setChannelId(channelId);
							broadTranscodeTaskPO.setTaskId(taskId);
							broadTranscodeTaskPO.setTranscodeType(BroadTranscodeType.STREAM_TRANSCODE.toString());
							broadTranscodeTaskDAO.save(broadTranscodeTaskPO);

						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}, delay > 0 ? delay : 0, TimeUnit.MILLISECONDS));
				streamBroad = true;
				fileBroad = false;
			} else {
				final BroadAbilityQueryType type;
				if (first) {
					switch (ChannelBroadStatus.fromName(forwardStatus)) {
					case CHANNEL_BROAD_STATUS_BROADED:
						type = BroadAbilityQueryType.CHANGE;
						break;
					case CHANNEL_BROAD_STATUS_INIT:
						type = BroadAbilityQueryType.NEW;
						break;
					default:
						throw new ErrorTypeException("播发状态", forwardStatus);
					}
				} else {
					type = BroadAbilityQueryType.COVER;
				}
				final Boolean finalFirst = first;
				final Boolean finalStreamBroad = streamBroad;
				final Boolean delayStart = first && broadDateLong > now;
				System.out.println("添加文件转码线程：" + broadDate + ";延时:" + (broadDateLong - DateUtil.getLongDate()));
				
				//标记使用了9000推流能力
				BroadAbilityRemotePO abilityRemotePO = broadAbilityRemoteDAO.findByChannelId(channelId);
				if (abilityRemotePO != null && abilityRemotePO.getBroadStreamWay() != BroadStreamWay.ABILITY_STREAM) {
					abilityRemotePO.setBroadStreamWay(BroadStreamWay.ABILITY_STREAM);
					broadAbilityRemoteDAO.save(abilityRemotePO);
				}
				
				if (first) {
					//添加文件流转码任务
					BroadAbilityBroadInfoVO broadInfoVO = broadAbilityBroadInfoVOs.get(0);
					String assetPath = new StringBufferWrapper()
							.append("udp://@")
							.append(broadInfoVO.getPreviewUrlIp())
							.append(":")
							.append(broadInfoVO.getPreviewUrlPort())
							.toString();
					TaskVO taskVO = broadAbilityQuery.queryTaskFromChannelId(channelId);
					taskVO.setOutParam(new ArrayList<OutParamVO>());
					String taskId = streamTranscodeQuery.addTask(null, assetPath, false, null, screen.getType().toLowerCase(), null, null, abilityRemotePO != null ? abilityRemotePO.getDeviceIp() : "", JSONObject.toJSONString(taskVO), null);
					if (abilityRemotePO != null) {
						abilityRemotePO.setProcessInstanceId(taskId);
						broadAbilityRemoteDAO.save(abilityRemotePO);
					}
				}
				
				futures.add(channelScheduledExecutorService.schedule(new Runnable() {
					
					@Override
					public void run() {
						try {
							RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
							//删除流转码任务
							if (!delayStart) {
								deleteStreamTaskByChannelId(channelId);
								addAllOutPutToFileTranscode(channelId);
							}
							if (finalStreamBroad && !finalFirst) {
								//如果不是第一个播发，则文件转流转码任务存在，且上一个播发是流转码播发，需增加流转码输出，包括原播发下的输出和后面接口请求增加的输出
								addAllOutPutToFileTranscode(channelId);
							}
							System.out.println(channelId + ":" + broadDate + ";" + type.getCmd());
							startSendSchedule(channelId, scheduleVO.getId(), broadAbilityBroadInfoVOs, type);
							Long nNow = DateUtil.getLongDate();
							if (!delayStart) {
								startSendStream(channelId);
								if (nNow - broadDateLong > dealTime) {
									seekAbilityBroadcast(channel, (nNow - broadDateLong) / 1000);
								}
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}, first ? 0 : broadDateLong - DateUtil.getLongDate(), TimeUnit.MILLISECONDS));
				
				//如果首个任务是当前时间往后的，则添加定时器执行开始
				if (delayStart) {
					futures.add(channelScheduledExecutorService.schedule(new Runnable() {
						
						@Override
						public void run() {
							try {
								RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
								deleteStreamTaskByChannelId(channelId);
								addAllOutPutToFileTranscode(channelId);
								System.out.println(channelId + ":" + broadDate + ";start");
								startSendStream(channelId);
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					}, broadDateLong - now, TimeUnit.MILLISECONDS));
				}
				
				fileBroad = true;
				streamBroad = false;
				first = false;
			}
			
			if (i == scheduleVOs.size() - 1) {
				if (finishTime > now) {
					futures.add(channelScheduledExecutorService.schedule(new Runnable() {
						
						@Override
						public void run() {
							try {
								System.out.println("排期单遍历结束");
								stopAbilityYjgbBroadcast(channelId);
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					}, finishTime - now + abilityDealTime, TimeUnit.MILLISECONDS));
				}
			}

		}
		
		if (fileBroad || streamBroad) {
			channel.setBroadcastStatus(ChannelBroadStatus.CHANNEL_BROAD_STATUS_BROADING.getName());
			channelDao.save(channel);
		}
	}
	
	/**
	 * 增加播发输出处理<br/>
	 * <b>作者:</b>lzp<br/>
	 * <b>版本：</b>1.0<br/>
	 * <b>日期：</b>2020年3月4日 下午3:04:45
	 * @param Long channelId 频道id
	 * @param List<OutParamVO> outParamVOs 输出信息
	 */
	public void addOutputCallback(Long channelId, String outParams) throws Exception {
		System.out.println("add out params:" + outParams);
		List<OutParamVO> outParamVOs = JSONArray.parseArray(outParams, OutParamVO.class);
		if (channelId == null || outParamVOs == null || outParamVOs.isEmpty()) return;
		//向数据库中添加输出
		List<BroadTranscodeOutputPO> outputPOs = new ArrayList<BroadTranscodeOutputPO>(); 
		for (OutParamVO outParamVO : outParamVOs) {
			BroadTranscodeOutputPO outputPO = new BroadTranscodeOutputPO();
			outputPO.setChannelId(channelId);
			outputPO.setOutputUrl(outParamVO.getOutputUrl());
			outputPO.setLocalIp(outParamVO.getLocalIp());
			outputPOs.add(outputPO);
		}
		broadTranscodeOutputDAO.save(outputPOs);
		
		//获取当前正在执行任务，给任务添加输出
		List<BroadTranscodeTaskPO> transcodePOs = broadTranscodeTaskDAO.findByChannelIdAndTranscodeType(channelId, BroadTranscodeType.STREAM_TRANSCODE.toString());
		if (transcodePOs == null || transcodePOs.isEmpty()) {
			BroadAbilityRemotePO abilityRemotePO = broadAbilityRemoteDAO.findByChannelId(channelId);
			String taskId = abilityRemotePO.getProcessInstanceId();
			if (taskId == null || taskId.isEmpty()) return;
			streamTranscodeQuery.addOutput(taskId, outParams);
		} else {
			for (BroadTranscodeTaskPO broadTranscodeTaskPO : transcodePOs) {
				String taskId = broadTranscodeTaskPO.getTaskId();
				System.out.println("quest add out params,taskId:" + taskId);
				streamTranscodeQuery.addOutput(taskId, outParams);
			}
		}
		System.out.println("finish add out params.");
	}
	
	/**
	 * 删除播发输出处理<br/>
	 * <b>作者:</b>lzp<br/>
	 * <b>版本：</b>1.0<br/>
	 * <b>日期：</b>2020年3月4日 下午3:04:45
	 * @param Long channelId 频道id
	 * @param List<OutParamVO> outParamVOs 输出信息
	 */
	public void deleteOutputCallback(Long channelId, String outParams) throws Exception {
		List<OutParamVO> outParamVOs = JSONArray.parseArray(outParams, OutParamVO.class);
		if (channelId == null || outParamVOs == null || outParamVOs.isEmpty()) return;
		//删除数据库中的输出项
		List<String> outputs = new ArrayList<String>();
		for (OutParamVO outParamVO : outParamVOs) {
			String outputUrl = outParamVO.getOutputUrl();
			if (outputUrl != null && !outputUrl.isEmpty()) outputs.add(outputUrl);
		}
		broadTranscodeOutputDAO.deleteByChannelIdAndOutputUrlIn(channelId, outputs);
		
		//获取当前正在执行任务，给任务删除输出
		List<BroadTranscodeTaskPO> transcodePOs = broadTranscodeTaskDAO.findByChannelIdAndTranscodeType(channelId, BroadTranscodeType.STREAM_TRANSCODE.toString());
		if (transcodePOs == null || transcodePOs.isEmpty()) {
			BroadAbilityRemotePO abilityRemotePO = broadAbilityRemoteDAO.findByChannelId(channelId);
			String taskId = abilityRemotePO.getProcessInstanceId();
			if (taskId == null || taskId.isEmpty()) return;
			streamTranscodeQuery.deleteOutput(taskId, outParams);
		} else {
			for (BroadTranscodeTaskPO broadTranscodeTaskPO : transcodePOs) {
				String taskId = broadTranscodeTaskPO.getTaskId();
				streamTranscodeQuery.deleteOutput(taskId, outParams);
			}
		}
	}
	
	/**
	 * 处理能力重启，把所有正在执行的任务全部停止再发起<br/>
	 * <b>作者:</b>lzp<br/>
	 * <b>版本：</b>1.0<br/>
	 * <b>日期：</b>2020年3月5日 上午9:52:08
	 */
	public void abilityReboot(String serverIp) throws Exception {
		List<ChannelVO> channelVOs = channelQuery.queryByBroadWayAndStatus(BroadWay.ABILITY_BROAD, ChannelBroadStatus.CHANNEL_BROAD_STATUS_BROADING);
		for (ChannelVO channelVO : channelVOs) {
			Long channelId = channelVO.getId();
			if (channelId == null) continue;
			if (ChannelType.YJGB.getName().equals(channelVO.getType())) {
				stopAbilityYjgbBroadcast(channelId);
				startAbilityYjgbBroad(channelId);
			} else {
				if (serverIp != null && !serverIp.isEmpty()) {
					List<AbilityInfoSendPO> abilityBroadInfoPOs = abilityInfoSendDAO.findByChannelIdAndAbilityIp(channelId, serverIp);
					if (!abilityBroadInfoPOs.isEmpty()) {
						stopAbilityBroadcast(channelId);
						startAbilityBroadcast(channelId);
					}
				} else {
					stopAbilityBroadcast(channelId);
					startAbilityBroadcast(channelId);
				}
			}
		}
	}
	
	/**
	 * 给文件转流转码添加所有输出<br/>
	 * <b>作者:</b>lzp<br/>
	 * <b>版本：</b>1.0<br/>
	 * <b>日期：</b>2020年3月11日 上午11:23:48
	 * @param Long channelId 频道id
	 */
	private void addAllOutPutToFileTranscode(Long channelId) throws Exception {
		BroadAbilityRemotePO abilityRemotePO = broadAbilityRemoteDAO.findByChannelId(channelId);
		if (abilityRemotePO != null) {
			String fileStreamTaskId = abilityRemotePO.getProcessInstanceId();
			TaskVO taskVO = broadAbilityQuery.queryTaskFromChannelId(channelId);
			streamTranscodeQuery.addOutput(fileStreamTaskId, JSONArray.toJSONString(taskVO.getOutParam()));
		}
	}
	
	/**
	 * 根据频道id删除流转码任务<br/>
	 * <b>作者:</b>lzp<br/>
	 * <b>版本：</b>1.0<br/>
	 * <b>日期：</b>2020年3月4日 下午2:24:07
	 * @param Long channelId 频道id
	 */
	private void deleteStreamTaskByChannelId(Long channelId) throws Exception {
		List<BroadTranscodeTaskPO> transcodePOs = broadTranscodeTaskDAO.findByChannelIdAndTranscodeType(channelId, BroadTranscodeType.STREAM_TRANSCODE.toString());
		for (BroadTranscodeTaskPO broadTranscodeTaskPO : transcodePOs) {
			System.out.println(new Date() + ": delete task: " + broadTranscodeTaskPO.getTaskId());
			streamTranscodeQuery.deleteTask(broadTranscodeTaskPO.getTaskId());
			broadTranscodeTaskDAO.deleteByTaskId(broadTranscodeTaskPO.getTaskId());
		}
	}
	
	/**
	 * 给PC端用户webSocket下节目单<br/>
	 * <b>作者:</b>lzp<br/>
	 * <b>版本：</b>1.0<br/>
	 * <b>日期：</b>2019年11月3日 下午3:24:34
	 * @param userVOs 用户列表
	 * @param scheduleVOs 节目单列表
	 */
	private void sendWebSocket(List<UserVO> userVOs, ChannelPO channel) throws Exception {
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("channelId", channel.getId());
		for (UserVO userVO : userVOs) {
			websocketMessageService.send(userVO.getId(), jsonObject.toJSONString(), WebsocketMessageType.COMMAND);
		}
	}
	
	/**
	 * 给PC端用户webSocket下节目单<br/>
	 * <b>作者:</b>lzp<br/>
	 * <b>版本：</b>1.0<br/>
	 * <b>日期：</b>2019年11月3日 下午3:24:34
	 * @param userVOs 用户列表
	 * @param scheduleVOs 节目单列表
	 */
	private void sendWebSocket(List<UserVO> userVOs, List<ScheduleVO> scheduleVOs, String port, ChannelPO channel) throws Exception {
		if (userVOs == null || userVOs.isEmpty() || scheduleVOs == null || scheduleVOs.isEmpty()) return;
//		List<Long> scheduleIds = scheduleVOs.stream().map(ScheduleVO::getId).collect(Collectors.toList());
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("channelId", channel.getId());
		for (UserVO userVO : userVOs) {
			websocketMessageService.send(userVO.getId(), jsonObject.toJSONString(), WebsocketMessageType.COMMAND);
		}
		System.out.println(JSONArray.toJSONString(scheduleVOs));
	}
	
	/**
	 * 下发准备的流的信息<br/>
	 * <b>作者:</b>lzp<br/>
	 * <b>版本：</b>1.0<br/>
	 * <b>日期：</b>2019年11月1日 下午4:50:02
	 * @param channelId
	 * @param scheduleId
	 * @param type
	 */
	private void startSendSchedule(Long channelId, Long scheduleId, List<BroadAbilityBroadInfoVO> broadAbilityBroadInfoVOs, BroadAbilityQueryType type) throws Exception {
		ChannelPO channel = channelQuery.findByChannelId(channelId);
		
		JSONObject output = new JSONObject();
		output.put("proto-type", "udp-ts");
		List<JSONObject> destList = new ArrayList<JSONObject>();
		String localIp = adapter.getServerUrl(BroadWay.ABILITY_BROAD).split(":")[0];
		if (broadAbilityBroadInfoVOs == null || broadAbilityBroadInfoVOs.isEmpty()) throw new ChannelBroadNoneOutputException();
		for (BroadAbilityBroadInfoVO broadAbilityBroadInfoVO : broadAbilityBroadInfoVOs) {
			if (broadAbilityBroadInfoVO.getPreviewUrlIp() == null || broadAbilityBroadInfoVO.getPreviewUrlIp().isEmpty()) continue;
			JSONObject dest = new JSONObject();
			dest.put("local_ip", localIp);
			dest.put("ipv4", broadAbilityBroadInfoVO.getPreviewUrlIp());
			dest.put("port", broadAbilityBroadInfoVO.getPreviewUrlPort());
			dest.put("vport", "");
			dest.put("aport", "");
			destList.add(dest);
		}
		if (destList.isEmpty()) {
			channel.setBroadcastStatus(ChannelBroadStatus.CHANNEL_BROAD_STATUS_INIT.getName());
			channelDao.save(channel);
			throw new ChannelBroadNoneOutputException();
		}
		output.put("dest_list", destList);
		
		output.put("scramble", channel.getEncryption() != null && channel.getEncryption() ? "AES-128" : "none");
		output.put("key", channel.getEncryption() != null && channel.getEncryption() ? getKey() : "");
		
		if (broadAbilityQuery.sendAbilityRequest(type, channel, abilityProgramText(programQuery.getProgram(scheduleId)), output)) {
			if(type != BroadAbilityQueryType.COVER) broadAbilityQuery.saveBroad(channelId, broadAbilityBroadInfoVOs);
		}
	}
	
	/**
	 * 下发开始推流命令<br/>
	 * <b>作者:</b>lzp<br/>
	 * <b>版本：</b>1.0<br/>
	 * <b>日期：</b>2019年11月1日 下午4:49:06
	 * @param channelId 频道id
	 */
	private void startSendStream(Long channelId) throws Exception {
		ChannelPO channel = channelQuery.findByChannelId(channelId);
		broadAbilityQuery.sendAbilityRequest(BroadAbilityQueryType.START, channel);
	}
	
	/**
	 * 播发时媒资排表字段内容(能力播发)<br/>
	 * <b>作者:</b>lzp<br/>
	 * <b>版本：</b>1.0<br/>
	 * <b>日期：</b>2019年6月25日 上午11:06:57
	 * @param ProgramVO program 分屏信息
	 */
	private List<String> abilityProgramText(ProgramVO program) throws Exception{
		List<String> returnList = new ArrayList<String>();
		if (program != null) {
			for (int i = 1; i <= program.getScreenNum(); i++) {
				if (program.getScreenInfo() != null && program.getScreenInfo().size() > 0) {
					List<ScreenVO> screens = program.getScreenInfo();
					Collections.sort(screens, new ScreenVO.ScreenVOOrderComparator());
					for (ScreenVO item : program.getScreenInfo()) {
						if (item.getSerialNum() != i)
							continue;
						returnList.add(adapter.changeHttpToFtp(item.getPreviewUrl()));
					}
				}
			}
		}
		return returnList;
	}
	
	/**
	 * 计算文件推流但个节目单播放时长<br/>
	 * <b>作者:</b>lzp<br/>
	 * <b>版本：</b>1.0<br/>
	 * <b>日期：</b>2020年3月4日 下午3:00:59
	 * @param Long schedulId 节目单id
	 * @return Long 节目单播放时长
	 */
	private Long querySchedulePlayTime(Long schedulId) throws Exception {
		Long playTime = 0l;
		ProgramVO programVO = programQuery.getProgram(schedulId);
		List<ScreenVO> screenVOs = programVO.getScreenInfo();
		if (screenVOs == null || screenVOs.isEmpty()) return playTime;
		Map<Long, Long> playTimeMap = new HashMap<Long, Long>();
		for (ScreenVO screenVO : screenVOs) {
			Long serialNum = screenVO.getSerialNum();
			if (screenVO.getDuration() != null && !screenVO.getDuration().isEmpty() && !screenVO.getDuration().equals("-")) {
				Long value = 0l;
				if (playTimeMap.containsKey(serialNum)) {
					value = playTimeMap.get(serialNum);
				}
				value += Long.parseLong(screenVO.getDuration());
				playTimeMap.put(serialNum, value);
			}
		}
		
		for (Long value : playTimeMap.values()) {
			playTime = value > playTime ? value : playTime;
		}
		return playTime;
	}
	
	/**
	 * 获取加密密钥(密钥在这里再加密)<br/>
	 * <b>作者:</b>lzp<br/>
	 * <b>版本：</b>1.0<br/>
	 * <b>日期：</b>2019年11月1日 下午4:49:26
	 * @return
	 */
	private String getKey() {
		byte[] bytes = {0x0d, 0x5d, (byte) 0xe3, 0x45, (byte) 0xd4, 0x00, 0x60, 0x7d, (byte) 0xc9, 0x3f, (byte) 0xe5, (byte) 0xd7, 0x17, 0x49, 0x3a, (byte) 0xf4};
		try{
            SecretKeySpec skeySpec = new SecretKeySpec(bytes, "AES");
            Cipher cipher = Cipher.getInstance("AES/ECB/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
            byte[] encrypted = cipher.doFinal(mediaEncodeQuery.queryKey().getBytes("utf-8"));
            return ByteUtil.bytesToHexString(encrypted).toUpperCase();
        }catch(Exception ex){
        	return null;
        }
	}
	
//	public void addScheduleDeal(Long channelId) throws Exception {
//	Long now = DateUtil.getLongDate();
//	ChannelPO channel = channelQuery.findByChannelId(channelId);
//	if (!channel.getBroadcastStatus().equals(ChannelBroadStatus.CHANNEL_BROAD_STATUS_BROADING)) return;
//	Map<Long, Timer> timerMap = channelService.getTimerMap();
//	if (!timerMap.containsKey(channelId)) return;
//	List<ScheduleVO> scheduleVOs = scheduleQuery.getByChannelId(channelId);
//	if (scheduleVOs == null || scheduleVOs.isEmpty()) throw new ScheduleNoneToBroadException(channel.getName());
//	int size = scheduleVOs.size();
//	ScheduleVO lastButOne = scheduleVOs.get(size - 2);
//	Long broadTime = DateUtil.parse(lastButOne.getBroadDate(), DateUtil.dateTimePattern).getTime();
//	if (broadTime > now) return;
//	Long playTime = querySchedulePlayTime(lastButOne.getId());
//	Long finishTime = playTime + broadTime;
//	ScheduleVO last = scheduleVOs.get(size - 1);
//	Long lastBroadTime = DateUtil.parse(last.getBroadDate(), DateUtil.dateTimePattern).getTime();
//	if (lastBroadTime - 5000l < finishTime) {
//		timerMap.get(channelId).cancel();
//		Timer nTimer = new Timer();
//		TimerTask timerTask = new TimerTask() {
//			
//			@Override
//			public void run() {
//				try {
//					startAbilityBroadTimer(channelId);
//				} catch (Exception e) {
//					e.printStackTrace();
//				}
//			}
//		};
//		Long nNow = DateUtil.getLongDate();
//		if (lastBroadTime - 5000l > nNow) {
//			nTimer.schedule(timerTask, lastBroadTime - nNow - 5000l);
//		} else {
//			nTimer.schedule(timerTask, 0l);
//		}
//	}
//}
	
//	public void stopAbilityBroadcast(Long channelId) throws Exception{
//		ChannelPO channel = channelQuery.findByChannelId(channelId);
//		Map<Long, Timer> timerMap = channelService.getTimerMap();
//		Timer timer = timerMap.get(channelId);
//		if (timer != null) {
//			timer.cancel();
//		}
//		if (broadAbilityQuery.sendAbilityRequest(BroadAbilityQueryType.STOP, channel, null, null)) {
//			channel.setBroadcastStatus(ChannelBroadStatus.CHANNEL_BROAD_STATUS_BROADED);
//			channelDao.save(channel);	
//			if (channel.getType().equals(ChannelType.REMOTE.toString())) {
//				channelService.remove(channelId);
//			}
//		}
//	}
	
//	private void startAbilityBroadTimer(Long channelId) throws Exception {
//	Map<Long, Timer> timerMap = channelService.getTimerMap();
//	//以防用户选择"此刻"为播发时间的时候，由于手动点击"播发"和程序执行时间导致播发不生效的预留时间
//	Long dealTime = 15000l;
//	BroadAbilityQueryType type = BroadAbilityQueryType.COVER;
//	
//	Long now = DateUtil.getLongDate();
//	ChannelPO channel = channelQuery.findByChannelId(channelId);
//	List<ScheduleVO> scheduleVOs = scheduleQuery.getByChannelId(channelId);
//	if (scheduleVOs == null || scheduleVOs.isEmpty()) throw new ScheduleNoneToBroadException(channel.getName());
//	String forwardStatus = channel.getBroadcastStatus();
//	
//	List<BroadAbilityBroadInfoVO> broadAbilityBroadInfoVOs = broadAbilityBroadInfoService.queryFromChannelId(channelId);
//	List<UserVO> webSocketSendUsers = new ArrayList<UserVO>();
//	String streamUrlPort = "";
//	for (BroadAbilityBroadInfoVO broadAbilityBroadInfoVO : broadAbilityBroadInfoVOs) {
//		Long userId = broadAbilityBroadInfoVO.getUserId();
//		if (userId != null) {
//			List<UserVO> userVOs = userQuery.findByIdInAndType(new ArrayListWrapper<Long>().add(userId).getList(), TerminalType.QT_MEDIA_EDITOR.getName());
//			if (userVOs != null && !userVOs.isEmpty()) {
//				UserVO user = userVOs.get(0);
//				webSocketSendUsers.add(user);
//				if (streamUrlPort.isEmpty()) streamUrlPort = broadAbilityBroadInfoVO.getPreviewUrlPort();
//				broadAbilityBroadInfoVO.setPreviewUrlIp(user.getIp());
//			}
//		}
//	}
//	if (!forwardStatus.equals(ChannelBroadStatus.CHANNEL_BROAD_STATUS_BROADING)) {
//		switch (forwardStatus) {
//		case ChannelBroadStatus.CHANNEL_BROAD_STATUS_BROADED:
//			type = BroadAbilityQueryType.CHANGE;
//			break;
//		case ChannelBroadStatus.CHANNEL_BROAD_STATUS_INIT:
//			type = BroadAbilityQueryType.NEW;
//			break;
//		default:
//			throw new ErrorTypeException("播发状态", forwardStatus);
//		}
//		channel.setBroadcastStatus(ChannelBroadStatus.CHANNEL_BROAD_STATUS_BROADING);
//		channelDao.save(channel);
//		if (!webSocketSendUsers.isEmpty()) sendWebSocket(webSocketSendUsers, scheduleVOs, streamUrlPort, channel);
//	}
//	
//	ServletRequestAttributes attributes = (ServletRequestAttributes)RequestContextHolder.getRequestAttributes();
//	final CopyHeaderHttpServletRequestWrapper request = new CopyHeaderHttpServletRequestWrapper(attributes.getRequest());
//	if (timerMap.containsKey(channelId)) timerMap.get(channelId).cancel();
//	
//	for (ScheduleVO scheduleVO : scheduleVOs) {
//		Date broadDate = DateUtil.parse(scheduleVO.getBroadDate(), DateUtil.dateTimePattern);
//		Long broadDateLong = broadDate.getTime();
//		//以防用户选择"此刻"为播发时间的时候，由于手动点击"播发"和程序执行时间导致播发不生效，因此预留10秒。
//		if (broadDateLong + dealTime > now || scheduleVOs.indexOf(scheduleVO) == scheduleVOs.size() - 1) {
//			Timer timer = timerMap.get(channelId);
//			if (timer != null) timer.cancel();
//			Timer nTimer = new Timer();
//			if (broadDateLong + dealTime  > now) {
//				final BroadAbilityQueryType queryType = type;
//				TimerTask timerTask = new TimerTask() {
//					
//					@Override
//					public void run() {
//						try {
//							RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
//							if (channel.getBroadcastStatus().equals(ChannelBroadStatus.CHANNEL_BROAD_STATUS_BROADING)){
//								System.out.println(channelId + ":" + scheduleVO.getBroadDate());
//								startSendSchedule(channelId, scheduleVO.getId(), broadAbilityBroadInfoVOs, queryType);
//								Long nNow = DateUtil.getLongDate();
//								if (broadDateLong > nNow) Thread.sleep(broadDateLong - nNow);
//								startSendStream(channelId);
//								//如果当前节目单结束时间超过下一个节目单开始时间，则睡眠到下一个节目单开始前5s,给能力解析节目单的时间;否则睡眠到当前节目单结束
//								Long playTime = querySchedulePlayTime(scheduleVO.getId());
//								Long finishTime = broadDateLong + playTime;
//								int next = scheduleVOs.indexOf(scheduleVO) + 1;
//								if (next < scheduleVOs.size()) {
//									Long nextBroadTime = DateUtil.parse(scheduleVOs.get(next).getBroadDate(), DateUtil.dateTimePattern).getTime();
//									if (nextBroadTime - 5000l < finishTime) {
//										Thread.sleep(nextBroadTime - 5000l);
//									} else {
//										Thread.sleep(playTime > dealTime ? playTime : dealTime);
//									}
//								} else {
//									Thread.sleep(playTime > dealTime ? playTime : dealTime);
//								}
//								startAbilityBroadTimer(channelId);
//							}
//						} catch (Exception e) {
//							e.printStackTrace();
//						}
//					}
//				};
//				Long nNow = DateUtil.getLongDate();
//				//提前1分钟下发，给推流服务器解析命令的时间
//				Long delayTime = 1l*60*1000;
//				//头一次下发则立即向能力发送请求，避免删除的时候能力没有该条任务存在
//				if (broadDateLong > nNow + delayTime && type != BroadAbilityQueryType.NEW){
//					nTimer.schedule(timerTask, broadDateLong - nNow - delayTime);
//				} else {
//					nTimer.schedule(timerTask, 0);
//				}
//			} else {
//				System.out.println("排期单遍历结束");
//				if (channel.getType().equals(ChannelType.REMOTE.toString())) {
//					BroadAbilityRemotePO remotePO = broadAbilityRemoteDAO.findByChannelId(channelId);
//					if (remotePO == null) return;
//					String url = remotePO.getStopCallbackUrl();
//					System.out.println("回调：" + url);
//					if (url != null && !url.isEmpty()) {
//						HttpRequestUtil.httpGet(new StringBufferWrapper().append(url).toString());
//					}
//				} else if (channel.getAutoBroad()){
//					channelService.stopBroadcast(channelId);
//				} else {
////					TimerTask timerTask = new TimerTask() {
////						
////						@Override
////						public void run() {
////							try {
////								startAbilityBroadTimer(channelId);
////							} catch (Exception e) {
////								e.printStackTrace();
////							}
////						}
////					};
////					nTimer.schedule(timerTask, dealTime - 5000l);
//					if (forwardStatus.equals(ChannelBroadStatus.CHANNEL_BROAD_STATUS_INIT)) {
//						channel.setBroadcastStatus(ChannelBroadStatus.CHANNEL_BROAD_STATUS_INIT);
//						channelDao.save(channel);
//						throw new ChannelBroadNoneScheduleException();
//					}
//				}
//			}
//			timerMap.put(channelId, nTimer);
//			break;
//		}
//	}
//}
}
