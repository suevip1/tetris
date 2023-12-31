package com.sumavision.tetris.cs.channel.api;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONArray;
import com.sumavision.tetris.commons.util.binary.ByteUtil;
import com.sumavision.tetris.commons.util.date.DateUtil;
import com.sumavision.tetris.commons.util.wrapper.ArrayListWrapper;
import com.sumavision.tetris.commons.util.wrapper.HashMapWrapper;
import com.sumavision.tetris.commons.util.wrapper.StringBufferWrapper;
import com.sumavision.tetris.cs.bak.VersionSendPO;
import com.sumavision.tetris.cs.bak.VersionSendQuery;
import com.sumavision.tetris.cs.channel.BroadWay;
import com.sumavision.tetris.cs.channel.ChannelPO;
import com.sumavision.tetris.cs.channel.ChannelQuery;
import com.sumavision.tetris.cs.channel.ChannelService;
import com.sumavision.tetris.cs.channel.ChannelType;
import com.sumavision.tetris.cs.channel.ChannelVO;
import com.sumavision.tetris.cs.channel.broad.ChannelServerType;
import com.sumavision.tetris.cs.channel.broad.ability.BroadAbilityBroadInfoService;
import com.sumavision.tetris.cs.channel.broad.ability.BroadAbilityBroadInfoVO;
import com.sumavision.tetris.cs.channel.broad.ability.BroadAbilityRemoteDAO;
import com.sumavision.tetris.cs.channel.broad.ability.BroadAbilityRemotePO;
import com.sumavision.tetris.cs.channel.broad.ability.BroadAbilityService;
import com.sumavision.tetris.cs.schedule.ScheduleService;
import com.sumavision.tetris.easy.process.stream.transcode.StreamTranscodeProfileVO;
import com.sumavision.tetris.easy.process.stream.transcode.StreamTranscodeQuery;
import com.sumavision.tetris.mvc.ext.response.json.aop.annotation.JsonBody;
import com.sumavision.tetris.mvc.wrapper.MultipartHttpServletRequestWrapper;

@Controller
@RequestMapping(value = "/api/server/cs/channel")
public class ApiServerChannelController {
	@Autowired
	private ChannelService channelService;
	
	@Autowired
	private ChannelQuery channelQuery;
	
	@Autowired
	private ScheduleService scheduleService;
	
	@Autowired
	private VersionSendQuery VersionSendQuery;
	
	@Autowired
	private BroadAbilityService broadAbilityService;
	
	@Autowired
	private BroadAbilityBroadInfoService broadAbilityBroadInfoService;
	
	@Autowired
	private BroadAbilityRemoteDAO broadAbilityRemoteDAO;
	
	@Autowired
	private StreamTranscodeQuery streamTranscodeQuery;
	
	/**
	 * 分页获取频道列表<br/>
	 * <b>作者:</b>lzp<br/>
	 * <b>版本：</b>1.0<br/>
	 * <b>日期：</b>2019年6月25日 上午11:06:57
	 * @param Integer currentPage 当前页
	 * @param Integer pageSize 分页大小
	 * @return List<ChannelVO> channels 频道列表
	 * @return Long total 频道总数
	 */
	@JsonBody
	@ResponseBody
	@RequestMapping(value = "/list/page")
	public Object channelList(Integer currentPage, Integer pageSize, HttpServletRequest request) throws Exception {

		return channelQuery.findAll(currentPage, pageSize, ChannelType.REMOTE);
	}
	
	/**
	 * 获取频道列表<br/>
	 * <b>作者:</b>lzp<br/>
	 * <b>版本：</b>1.0<br/>
	 * <b>日期：</b>2019年6月25日 上午11:06:57
	 * @return List<ChannelVO> channels 频道列表
	 * @return Long total 频道总数
	 */
	@JsonBody
	@ResponseBody
	@RequestMapping(value = "/list")
	public Object channelList(HttpServletRequest request) throws Exception {

		return channelQuery.findAll(null, null, ChannelType.REMOTE);
	}
	
	/**
	 * 根据id查询频道信息<br/>
	 * <b>作者:</b>lzp<br/>
	 * <b>版本：</b>1.0<br/>
	 * <b>日期：</b>2019年6月25日 上午11:06:57
	 * @return ChannelVO channel 频道信息
	 */
	@JsonBody
	@ResponseBody
	@RequestMapping(value = "/quest")
	public Object quest(Long id, HttpServletRequest request) throws Exception {

		return new ChannelVO().set(channelQuery.findByChannelId(id));
	}

	/**
	 * 添加频道<br/>
	 * <b>作者:</b>lzp<br/>
	 * <b>版本：</b>1.0<br/>
	 * <b>日期：</b>2019年6月25日 上午11:06:57
	 * @param String name 频道名称
	 * @param String date 日期
	 * @param String broadWay 播发方式(参考BroadWay枚举)
	 * @param String previewUrlIp 能力的输出地址ip(仅限能力播发)
	 * @param String previewUrlPort 能力的输出地址port(仅限能力播发)
	 * @param String remark 备注
	 * @return ChannelVO 频道
	 */
	@JsonBody
	@ResponseBody
	@RequestMapping(value = "/add")
	public Object add(String channelUrl, String date, String casts, HttpServletRequest request) throws Exception {
		
		String name = "yjgbBroad";
		String dateNow = DateUtil.now();
		String broadWay = BroadWay.ABILITY_BROAD.getName();
		String remark = "";
		Boolean encryption = false;
		
		StreamTranscodeProfileVO streamTranscodeProfileVO = streamTranscodeQuery.getProfile();
		String ip = streamTranscodeProfileVO.getToolIp();
		Long port = broadAbilityBroadInfoService.queryLocalPort(ip, Long.parseLong(streamTranscodeProfileVO.getUdpStartPort()));
		
		List<BroadAbilityBroadInfoVO> infoVOs = new ArrayListWrapper<BroadAbilityBroadInfoVO>()
				.add(new BroadAbilityBroadInfoVO().setPreviewUrlIp(ip).setPreviewUrlPort(port.toString()))
				.getList();

		ChannelPO channel = channelService.addYjbgChannel(
				name,
				dateNow,
				broadWay,
				remark,
				encryption,
				false,
				null,
				null,
				null,
				null,
				null,
				infoVOs);
		
		BroadAbilityRemotePO remotePO = new BroadAbilityRemotePO();
		remotePO.setChannelId(channel.getId());
		remotePO.setTranscodeInfo(channelUrl);
		broadAbilityRemoteDAO.save(remotePO);
		
		if (casts != null && !casts.isEmpty()) {
			List<ApiServerScheduleCastVO> castVOs = JSONArray.parseArray(casts, ApiServerScheduleCastVO.class);
			scheduleService.addSchedulesFromCast(channel.getId(), castVOs);
		}
		return new HashMapWrapper<String, Long>().put("channelID", channel.getId()).getMap();
	}

	/**
	 * 编辑频道<br/>
	 * <b>作者:</b>lzp<br/>
	 * <b>版本：</b>1.0<br/>
	 * <b>日期：</b>2019年6月25日 上午11:06:57
	 * @param Long id 频道id
	 * @param String name 频道名称
	 * @param String broadWay 播发方式(参考BroadWay枚举)
	 * @param String previewUrlIp 能力的输出地址ip(仅限能力播发)
	 * @param String previewUrlPort 能力的输出地址port(仅限能力播发)
	 * @param String remark 备注
	 * @return ChannelVO 频道
	 */
	@JsonBody
	@ResponseBody
	@RequestMapping(value = "/edit")
	public Object rename(Long channelID, String date, String casts, HttpServletRequest request) throws Exception {
		scheduleService.removeByChannelId(channelID);
		if (casts != null && !casts.isEmpty()) {
			List<ApiServerScheduleCastVO> castVOs = JSONArray.parseArray(casts, ApiServerScheduleCastVO.class);
			scheduleService.addSchedulesFromCast(channelID, castVOs);
		}
		return null;
	}

	/**
	 * 删除频道<br/>
	 * <b>作者:</b>lzp<br/>
	 * <b>版本：</b>1.0<br/>
	 * <b>日期：</b>2019年6月25日 上午11:06:57
	 * @param Long id 频道id
	 */
	@JsonBody
	@ResponseBody
	@RequestMapping(value = "/remove")
	public Object remove(Long channelID, HttpServletRequest request) throws Exception {

		channelService.remove(channelID);

		return "";
	}
	
	/**
	 * 开始播发<br/>
	 * <b>作者:</b>lzp<br/>
	 * <b>版本：</b>1.0<br/>
	 * <b>日期：</b>2019年6月25日 上午11:06:57
	 * @param Long channelId 频道id
	 * @param String deviceIp 转换服务IP
	 * @param String task 转码参数
	 */
	@JsonBody
	@ResponseBody
	@RequestMapping(value = "/broadcast/start")
	public Object broadcastStart(Long channelID, String deviceIp, String task, HttpServletRequest request) throws Exception {
		BroadAbilityRemotePO remotePO = broadAbilityRemoteDAO.findByChannelId(channelID);
		if (remotePO == null) remotePO = new BroadAbilityRemotePO();
		remotePO.setChannelId(channelID);
		remotePO.setTranscodeInfo(task);
		remotePO.setDeviceIp(deviceIp);
		broadAbilityRemoteDAO.save(remotePO);
		
		channelService.startBroadcast(channelID, null);
		
		return "";
	}
	
	/**
	 * 停止播发<br/>
	 * <b>作者:</b>lzp<br/>
	 * <b>版本：</b>1.0<br/>
	 * <b>日期：</b>2019年6月25日 上午11:06:57
	 * @param Long channelId 频道id
	 */
	@JsonBody
	@ResponseBody
	@RequestMapping(value = "/broadcast/stop")
	public Object broadcastStop(Long channelID, HttpServletRequest request) throws Exception {
		
		channelService.stopBroadcast(channelID);
		
		return "";
	}
	
	/**
	 * 查询播发状态<br/>
	 * <b>作者:</b>lzp<br/>
	 * <b>版本：</b>1.0<br/>
	 * <b>日期：</b>2019年6月25日 上午11:06:57
	 * @param Long channelId 频道id
	 */
	@JsonBody
	@ResponseBody
	@RequestMapping(value = "/broadcast/status")
	public Object broadcastStatus(Long channelID, HttpServletRequest request) throws Exception {
		
		return channelQuery.getBroadstatus(channelID);
	}
	
	/**
	 * 添加输出<br/>
	 * <b>作者:</b>lzp<br/>
	 * <b>版本：</b>1.0<br/>
	 * <b>日期：</b>2020年3月4日 下午3:16:46
	 * @param Long channelID 频道id
	 * @param String channelUrl 输出信息
	 */
	@JsonBody
	@ResponseBody
	@RequestMapping(value = "/output/add")
	public Object addOutput(Long channelID, String channelUrl, HttpServletRequest request) throws Exception {
		broadAbilityService.addOutputCallback(channelID, channelUrl);
		return null;
	}
	
	/**
	 * 删除输出<br/>
	 * <b>作者:</b>lzp<br/>
	 * <b>版本：</b>1.0<br/>
	 * <b>日期：</b>2020年3月4日 下午3:16:46
	 * @param Long channelID 频道id
	 * @param String channelUrl 输出信息
	 */
	@JsonBody
	@ResponseBody
	@RequestMapping(value = "/output/delete")
	public Object deleteOutput(Long channelID, String channelUrl, HttpServletRequest request) throws Exception {
		broadAbilityService.deleteOutputCallback(channelID, channelUrl);
		return null;
	}
	
	/**
	 * 推流能力重启调用(例外)<br/>
	 * <b>作者:</b>lzp<br/>
	 * <b>版本：</b>1.0<br/>
	 * <b>日期：</b>2020年3月5日 上午10:40:15
	 */
	@JsonBody
	@ResponseBody
	@RequestMapping(value = "/alarm/reboot")
	public Object alarmRebootRecieve(String serverIp, HttpServletRequest request) throws Exception {
		System.out.println("推流能力重启");
		channelService.rebootServer(ChannelServerType.ABILITY_STREAM, serverIp);
		return null;
	}
	
	/**
	 * 上传分片<br/>
	 * <b>作者:</b>lzp<br/>
	 * <b>版本：</b>1.0<br/>
	 * <b>日期：</b>2020年2月18日 下午3:43:12
	 * @param secName taskid和section标记组成
	 * @param block 分片数据
	 */
	@JsonBody
	@ResponseBody
	@RequestMapping(value = "/upload")
	public Object upload(HttpServletRequest nativeRequest) throws Exception{
		
		MultipartHttpServletRequestWrapper request = new MultipartHttpServletRequestWrapper(nativeRequest);
		
		String secName = request.getString("secName");
		String taskId = secName.split("_")[0];
		VersionSendPO versionSendPO = VersionSendQuery.getFromBroadId(taskId);
		if (versionSendPO == null) return null;
		String filePath = new StringBufferWrapper()
				.append(versionSendPO.getZoneStorePath())
				.append(File.separator)
				.append(secName)
				.append(".bak")
				.toString();
		
		//文件起始位置错误
		File file = new File(filePath);
		
		//分块
		InputStream block = null;
		FileOutputStream out = null;
		try{
			if(!file.exists()) file.createNewFile();
			block = request.getInputStream("block");
			byte[] blockBytes = ByteUtil.inputStreamToBytes(block);
			out = new FileOutputStream(file, true);
			out.write(blockBytes);
		}finally{
			if(block != null) block.close();
			if(out != null) out.close();
		}
		
        return null;
	}
	
	/**
	 * 停止播发任务<br/>
	 * <b>作者:</b>wjw<br/>
	 * <b>版本：</b>1.0<br/>
	 * <b>日期：</b>2020年5月14日 下午2:42:17
	 * @param String uuid 播发任务uuid
	 */
	@JsonBody
	@ResponseBody
	@RequestMapping(value = "/broadcast/push/stop")
	public Object broadcastStop(String uuid, HttpServletRequest request) throws Exception {
		
		channelService.stopBroadcastByUuid(uuid);
		
		return null;
	}
}
