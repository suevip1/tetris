package com.sumavision.tetris.cs.channel.broad.file;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.sumavision.tetris.commons.util.wrapper.ArrayListWrapper;
import com.sumavision.tetris.cs.channel.Adapter;
import com.sumavision.tetris.cs.channel.ChannelBroadStatus;
import com.sumavision.tetris.cs.channel.ChannelDAO;
import com.sumavision.tetris.cs.channel.ChannelPO;
import com.sumavision.tetris.cs.channel.ChannelQuery;
import com.sumavision.tetris.cs.channel.exception.ChannelBroadNoneOutputException;
import com.sumavision.tetris.cs.menu.CsResourceQuery;
import com.sumavision.tetris.cs.menu.CsResourceVO;
import com.sumavision.tetris.cs.program.ProgramQuery;
import com.sumavision.tetris.cs.program.ProgramVO;
import com.sumavision.tetris.cs.program.ScreenVO;
import com.sumavision.tetris.cs.schedule.ScheduleQuery;
import com.sumavision.tetris.cs.schedule.ScheduleVO;
import com.sumavision.tetris.cs.schedule.exception.ScheduleNoneToBroadException;
import com.sumavision.tetris.user.UserEquipType;
import com.sumavision.tetris.websocket.message.WebsocketMessageService;
import com.sumavision.tetris.websocket.message.WebsocketMessageType;

@Service
@Transactional(rollbackFor = Exception.class)
public class BroadFileService {
	@Autowired
	private ChannelQuery channelQuery;
	
	@Autowired
	private ChannelDAO channelDAO;
	
	@Autowired
	private ScheduleQuery scheduleQuery;
	
	@Autowired
	private ProgramQuery programQuery;
	
	@Autowired
	private CsResourceQuery csResourceQuery;
	
	@Autowired
	private Adapter adapter;
	
	@Autowired
	private BroadFileBroadInfoService broadFileBroadInfoService;
	
	@Autowired
	private WebsocketMessageService websocketMessageService;
	
	/**
	 * 文件下载开始播发<br/>
	 * <b>作者:</b>lzp<br/>
	 * <b>版本：</b>1.0<br/>
	 * <b>日期：</b>2019年11月27日 下午3:07:26
	 * @param channelId 频道id
	 */
	public void startFileBroadcast(Long channelId) throws Exception {
		ChannelPO channel = channelQuery.findByChannelId(channelId);
		List<ScheduleVO> scheduleVOs = scheduleQuery.getByChannelId(channelId);
		if (scheduleVOs == null || scheduleVOs.isEmpty()) throw new ScheduleNoneToBroadException(channel.getName());
		
		List<BroadFileBroadInfoVO> broadInfoVOs = broadFileBroadInfoService.queryFromChannelId(channelId);
		if (broadInfoVOs == null || broadInfoVOs.isEmpty()) throw new ChannelBroadNoneOutputException();
		
		sendWebsocket(broadInfoVOs, channel);
		
		if (channel.getAutoBroad() == null || !channel.getAutoBroad()){
			channel.setBroadcastStatus(ChannelBroadStatus.CHANNEL_BROAD_STATUS_BROADED);
		} else {
			channel.setBroadcastStatus(ChannelBroadStatus.CHANNEL_BROAD_STATUS_BROADING);
		}
		channelDAO.save(channel);
	}
	
	public void stopFileBroadcast(Long channelId) throws Exception {
		ChannelPO channel = channelQuery.findByChannelId(channelId);
		channel.setBroadcastStatus(ChannelBroadStatus.CHANNEL_BROAD_STATUS_BROADED);
		channelDAO.save(channel);
	}
	
	/**
	 * 给终端用户webSocket下节目单<br/>
	 * <b>作者:</b>lzp<br/>
	 * <b>版本：</b>1.0<br/>
	 * <b>日期：</b>2019年11月3日 下午3:24:34
	 * @param broadInfoVOs 预播发信息列表
	 * @param channel 频道信息
	 */
	public void sendWebsocket(List<BroadFileBroadInfoVO> broadInfoVOs, ChannelPO channel) throws Exception {
		List<BroadFileBroadInfoVO> qtUsers = new ArrayList<BroadFileBroadInfoVO>();
		List<BroadFileBroadInfoVO> terminalUsers = new ArrayList<BroadFileBroadInfoVO>();
		for (BroadFileBroadInfoVO infoVO : broadInfoVOs) {
			if (infoVO.getUserEquipType().equals(UserEquipType.QT.toString())) {
				qtUsers.add(infoVO);
			} else if (infoVO.getUserEquipType().equals(UserEquipType.PUSH.toString())) {
				terminalUsers.add(infoVO);
			}
		}
		if (!qtUsers.isEmpty()) {
			List<Long> qtUserIds = qtUsers.stream().map(BroadFileBroadInfoVO::getUserId).collect(Collectors.toList());
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("channelId", channel.getId());
			for (Long id : qtUserIds) {
				websocketMessageService.send(id, jsonObject.toJSONString(), WebsocketMessageType.COMMAND);
			}
		}
//		if (!terminalUsers.equals(UserEquipType.PUSH.toString())) {
//			List<Long> teminalUserIds = terminalUsers.stream().map(BroadFileBroadInfoVO::getUserId).collect(Collectors.toList());
//			JSONObject jsonObject = new JSONObject();
//			jsonObject.put("channelId", channel.getId());
//			for (Long id : teminalUserIds) {
//				websocketMessageService.send(id, jsonObject.toJSONString(), WebsocketMessageType.COMMAND);
//			}
//		}
	}
	
	public void addScheduleDeal(Long channelId) throws Exception {
		ChannelPO channel = channelQuery.findByChannelId(channelId);
		
		if (channel.getBroadcastStatus().equals(ChannelBroadStatus.CHANNEL_BROAD_STATUS_BROADING)) {
			startFileBroadcast(channelId);
		}
	}
	
	public Long getChannelIdFromUser(Long userId) throws Exception {
		List<BroadFileBroadInfoVO> broadInfoVOs = broadFileBroadInfoService.queryFromUserIds(new ArrayListWrapper<Long>().add(userId).getList());
		return (broadInfoVOs == null || broadInfoVOs.isEmpty()) ? null : broadInfoVOs.get(0).getChannelId();
	}
	
	public JSONObject getNewBroadJSON(Long channelId) throws Exception {
		ChannelPO channel = channelQuery.findByChannelId(channelId);
		JSONObject textJson = new JSONObject();

//		textJson.put("file", "");
//		textJson.put("fileSize", "");
		JSONArray scheduleJsons = new JSONArray();
		List<ScheduleVO> schedules = scheduleQuery.getByChannelId(channelId);
		if (schedules == null || schedules.isEmpty()) throw new ScheduleNoneToBroadException(channel.getName());
		for (ScheduleVO schedule : schedules) {
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("effectTime", schedule.getBroadDate());
			jsonObject.put("screens", this.programText(channel, programQuery.getProgram(schedule.getId())));
			scheduleJsons.add(jsonObject);
		}
		
		textJson.put("schedules", scheduleJsons);
		return textJson;
	}
	
	/**
	 * 播发时媒资排表字段内容(终端播发)<br/>
	 * <b>作者:</b>lzp<br/>
	 * <b>版本：</b>1.0<br/>
	 * <b>日期：</b>2019年6月25日 上午11:06:57
	 * @param ProgramVO program 分屏信息
	 */
	private List<JSONObject> programText(ChannelPO channel, ProgramVO program) throws Exception {
		List<JSONObject> returnList = new ArrayList<JSONObject>();
		if (program != null) {
			JSONObject useTemplate = adapter.screenTemplate(program.getScreenNum());
			if (useTemplate == null) return null;
			for (int i = 1; i <= program.getScreenNum(); i++) {
				JSONObject returnItem = adapter.serial(useTemplate, i);
				List<JSONObject> scheduleList = new ArrayList<JSONObject>();
				if (program.getScreenInfo() != null && program.getScreenInfo().size() > 0) {
					for (ScreenVO item : program.getScreenInfo()) {
						if (item.getSerialNum() != i)
							continue;
						JSONObject schedule = new JSONObject();
						CsResourceVO resource = csResourceQuery.queryResourceById(item.getResourceId());
						schedule.put("name", resource.getName());
						schedule.put("previewUrl", resource.getPreviewUrl());
						schedule.put("index", item.getIndex());
						scheduleList.add(schedule);
					}
				}
				returnItem.put("program", scheduleList);
				returnList.add(returnItem);
			}
		}
		return returnList;
	}
}
