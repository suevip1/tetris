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

import com.sumavision.tetris.commons.util.binary.ByteUtil;
import com.sumavision.tetris.commons.util.date.DateUtil;
import com.sumavision.tetris.commons.util.wrapper.ArrayListWrapper;
import com.sumavision.tetris.commons.util.wrapper.StringBufferWrapper;
import com.sumavision.tetris.cs.bak.VersionSendPO;
import com.sumavision.tetris.cs.bak.VersionSendQuery;
import com.sumavision.tetris.cs.channel.ChannelDAO;
import com.sumavision.tetris.cs.channel.ChannelPO;
import com.sumavision.tetris.cs.channel.ChannelQuery;
import com.sumavision.tetris.cs.channel.ChannelService;
import com.sumavision.tetris.cs.channel.ChannelType;
import com.sumavision.tetris.cs.channel.ChannelVO;
import com.sumavision.tetris.cs.channel.broad.ability.BroadAbilityBroadInfoVO;
import com.sumavision.tetris.mvc.ext.response.json.aop.annotation.JsonBody;
import com.sumavision.tetris.mvc.wrapper.MultipartHttpServletRequestWrapper;
import com.sumavision.tetris.user.UserQuery;

@Controller
@RequestMapping(value = "/api/server/cs/channel")
public class ApiServerChannelController {
	@Autowired
	private ChannelService channelService;
	
	@Autowired
	private ChannelQuery channelQuery;
	
	@Autowired
	private ChannelDAO channelDao;
	
	@Autowired
	private VersionSendQuery VersionSendQuery;
	
	@Autowired
	private UserQuery userQuery;
	
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
	@RequestMapping(value = "/list")
	public Object channelList(Integer currentPage, Integer pageSize, HttpServletRequest request) throws Exception {

		return channelQuery.findAll(currentPage, pageSize, ChannelType.REMOTE);
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
	public Object add(String name, String date, String broadWay, String previewUrlIp, String previewUrlPort, String remark, Boolean encryption, HttpServletRequest request) throws Exception {
		
		if (date == null) date = DateUtil.now();
		if (broadWay == null) broadWay = "轮播推流";
		if (remark == null) remark = "";
		
		List<BroadAbilityBroadInfoVO> infoVOs = new ArrayListWrapper<BroadAbilityBroadInfoVO>()
				.add(new BroadAbilityBroadInfoVO().setPreviewUrlIp(previewUrlIp).setPreviewUrlPort(previewUrlPort))
				.getList();

		ChannelPO channel = channelService.add(name, date, broadWay, remark, null, null, ChannelType.REMOTE, encryption, false, null, null, null, null, null, infoVOs);

		return new ChannelVO().set(channel);
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
	public Object rename(Long id, String name, String previewUrlIp, String previewUrlPort, String remark, Boolean encryption, HttpServletRequest request) throws Exception {
		
		if (remark == null) remark = "";

		List<BroadAbilityBroadInfoVO> infoVOs = new ArrayListWrapper<BroadAbilityBroadInfoVO>()
				.add(new BroadAbilityBroadInfoVO().setPreviewUrlIp(previewUrlIp).setPreviewUrlPort(previewUrlPort))
				.getList();
		
		ChannelPO channel = channelService.edit(id, name, remark, null, null, encryption, false, null, null, null, null, null, infoVOs);

		return new ChannelVO().set(channel);
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
	public Object remove(Long id, HttpServletRequest request) throws Exception {

		channelService.remove(id);

		return "";
	}
	
	/**
	 * 开始播发<br/>
	 * <b>作者:</b>lzp<br/>
	 * <b>版本：</b>1.0<br/>
	 * <b>日期：</b>2019年6月25日 上午11:06:57
	 * @param Long channelId 频道id
	 */
	@JsonBody
	@ResponseBody
	@RequestMapping(value = "/broadcast/start")
	public Object broadcastStart(Long id, HttpServletRequest request) throws Exception {

		ChannelPO channelPO = channelDao.findOne(id);
		if (channelPO == null) return null;
		
		channelService.startBroadcast(id, null);
		
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
	public Object broadcastStop(Long id, HttpServletRequest request) throws Exception {
		
		ChannelPO channelPO = channelDao.findOne(id);
		if (channelPO == null) return null;
		
		channelService.stopBroadcast(id);
		
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
	public Object broadcastStatus(Long id, HttpServletRequest request) throws Exception {
		
		return channelQuery.getBroadstatus(id);
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
}
