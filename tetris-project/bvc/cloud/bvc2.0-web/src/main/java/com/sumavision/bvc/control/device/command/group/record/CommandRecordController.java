package com.sumavision.bvc.control.device.command.group.record;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONArray;
import com.suma.venus.resource.base.bo.AccessNodeBO;
import com.suma.venus.resource.service.ResourceService;
import com.sumavision.bvc.command.group.dao.CommandGroupRecordDAO;
import com.sumavision.bvc.command.group.dao.CommandGroupRecordFragmentDAO;
import com.sumavision.bvc.command.group.record.CommandGroupRecordFragmentPO;
import com.sumavision.bvc.command.group.record.CommandGroupRecordPO;
import com.sumavision.bvc.command.group.user.layout.player.CommandGroupUserPlayerPO;
import com.sumavision.bvc.control.device.command.group.vo.record.GroupVO;
import com.sumavision.bvc.control.device.command.group.vo.user.CommandGroupUserPlayerSettingVO;
import com.sumavision.bvc.control.device.monitor.record.MonitorRecordTaskVO;
import com.sumavision.bvc.control.utils.UserUtils;
import com.sumavision.bvc.control.welcome.UserVO;
import com.sumavision.bvc.device.command.basic.CommandBasicServiceImpl;
import com.sumavision.bvc.device.command.record.CommandRecordServiceImpl;
import com.sumavision.bvc.device.command.record.CommandVodRecordParser;
import com.sumavision.bvc.device.command.secret.CommandSecretServiceImpl;
import com.sumavision.bvc.device.monitor.playback.exception.AccessNodeIpMissingException;
import com.sumavision.bvc.device.monitor.playback.exception.AccessNodeNotExistException;
import com.sumavision.bvc.device.monitor.playback.exception.AccessNodePortMissionException;
import com.sumavision.bvc.device.monitor.record.MonitorRecordPO;
import com.sumavision.tetris.commons.exception.BaseException;
import com.sumavision.tetris.commons.exception.code.StatusCode;
import com.sumavision.tetris.commons.util.wrapper.ArrayListWrapper;
import com.sumavision.tetris.commons.util.wrapper.HashMapWrapper;
import com.sumavision.tetris.commons.util.wrapper.StringBufferWrapper;
import com.sumavision.tetris.mvc.ext.response.json.aop.annotation.JsonBody;

@Controller
@RequestMapping(value = "/command/record")
public class CommandRecordController {

	@Autowired
	private UserUtils userUtils;

	@Autowired
	private CommandGroupRecordDAO commandGroupRecordDao;

	@Autowired
	private CommandGroupRecordFragmentDAO commandGroupRecordFragmentDao;
	
	@Autowired
	CommandBasicServiceImpl commandBasicServiceImpl;

	
	@Autowired
	CommandRecordServiceImpl commandRecordServiceImpl;

	
	@Autowired
	CommandSecretServiceImpl commandSecretServiceImpl;

	
	@Autowired
	ResourceService resourceService;

	
	@Autowired
	CommandVodRecordParser commandVodRecordParser;
	
	/**
	 * 开始录制<br/>
	 * <p>详细描述</p>
	 * <b>作者:</b>zsy<br/>
	 * <b>版本：</b>1.0<br/>
	 * <b>日期：</b>2019年11月26日 上午10:19:46
	 * @param id
	 * @param request
	 * @return
	 * @throws Exception
	 */
	@JsonBody
	@ResponseBody
	@RequestMapping(value = "/start")
	public Object start(
			String id,
			HttpServletRequest request) throws Exception{
		
		UserVO user = userUtils.getUserFromSession(request);
		
		commandRecordServiceImpl.start(user.getId(), Long.parseLong(id));
		
		return null;
	}
	
	/**
	 * 停止录制<br/>
	 * <p>详细描述</p>
	 * <b>作者:</b>zsy<br/>
	 * <b>版本：</b>1.0<br/>
	 * <b>日期：</b>2019年11月26日 上午10:19:22
	 * @param id
	 * @param request
	 * @return
	 * @throws Exception
	 */
	@JsonBody
	@ResponseBody
	@RequestMapping(value = "/stop")
	public Object stop(
			String id,
			HttpServletRequest request) throws Exception{
		
		UserVO user = userUtils.getUserFromSession(request);
		
		commandRecordServiceImpl.stop(user.getId(), Long.parseLong(id), true);
		return null;
		
	}
	
	@JsonBody
	@ResponseBody
	@RequestMapping(value = "/remove")
	public Object remove(
			String recordId,
			HttpServletRequest request) throws Exception{
		commandRecordServiceImpl.remove(Long.parseLong(recordId));
		return null;
	}
	
	/**
	 * 查询指挥的录像<br/>
	 * <p>详细描述</p>
	 * <b>作者:</b>zsy<br/>
	 * <b>版本：</b>1.0<br/>
	 * <b>日期：</b>2019年11月26日 上午10:12:18
	 * @param request
	 * @return GroupVO 数组
	 * @throws Exception
	 */
	@JsonBody
	@ResponseBody
	@RequestMapping(value = "/query")
	public Object query(HttpServletRequest request) throws Exception{
		
		UserVO user = userUtils.getUserFromSession(request);
		//根据userId查找record
		List<CommandGroupRecordPO> records = commandGroupRecordDao.findByRecordUserIdAndRun(user.getId(), false);
		HashMap<Long, List<CommandGroupRecordPO>> groupRecordMap = new HashMap<Long, List<CommandGroupRecordPO>>();
		for(CommandGroupRecordPO record : records){
			List<CommandGroupRecordPO> groupRecords = groupRecordMap.get(record.getGroupId());
			if(groupRecords == null){
				groupRecords = new ArrayListWrapper<CommandGroupRecordPO>().add(record).getList();
				groupRecordMap.put(record.getGroupId(), groupRecords);
			}else{
				groupRecords.add(record);
			}
		}
		
		JSONArray groups = new JSONArray();
		for(List<CommandGroupRecordPO> groupRecords : groupRecordMap.values()){
			GroupVO group = new GroupVO().set(groupRecords);
			groups.add(group);
		}
		
		return new HashMapWrapper<String, Object>().put("groups", groups).getMap();
	}
	
	/**
	 * 开始播放指挥录像<br/>
	 * <p>完整的一段指挥录像，包含多个片段</p>
	 * <b>作者:</b>zsy<br/>
	 * <b>版本：</b>1.0<br/>
	 * <b>日期：</b>2019年11月26日 上午10:13:01
	 * @param id
	 * @param request
	 * @return
	 * @throws Exception
	 */
	@JsonBody
	@ResponseBody
	@RequestMapping(value = "/start/playback")
	public Object startPlayback(
			String recordId,
			HttpServletRequest request) throws Exception{
		
		UserVO user = userUtils.getUserFromSession(request);
		
		List<CommandGroupUserPlayerPO> players = commandRecordServiceImpl.startPlayGroupRecord(user.getId(), Long.parseLong(recordId));
		JSONArray result = new JSONArray();
		for(CommandGroupUserPlayerPO player : players){
			result.add(new CommandGroupUserPlayerSettingVO().set(player));
		}
		return result;
		
	}
	
	/**
	 * 开始播放多段指挥录像<br/>
	 * <p>任选的多个指挥录像片段</p>
	 * <b>作者:</b>zsy<br/>
	 * <b>版本：</b>1.0<br/>
	 * <b>日期：</b>2020年4月13日 上午10:13:01
	 * @param id
	 * @param request
	 * @return
	 * @throws Exception
	 */
	@JsonBody
	@ResponseBody
	@RequestMapping(value = "/start/playback/fragments")
	public Object startPlaybackFragments(
			String fragmentIds,
			HttpServletRequest request) throws Exception{
		
		UserVO user = userUtils.getUserFromSession(request);
		List<Long> fragmentIdsArray = JSONArray.parseArray(fragmentIds, Long.class);
		
		List<CommandGroupUserPlayerPO> players = commandRecordServiceImpl.startPlayFragments(user.getId(), fragmentIdsArray);
		JSONArray result = new JSONArray();
		for(CommandGroupUserPlayerPO player : players){
			result.add(new CommandGroupUserPlayerSettingVO().set(player));
		}
		return result;
		
	}

	/**
	 * 停止一个片段播放<br/>
	 * <p>通常点击页面上播放器的叉子触发</p>
	 * <b>作者:</b>zsy<br/>
	 * <b>版本：</b>1.0<br/>
	 * <b>日期：</b>2019年11月26日 上午10:18:15
	 * @param businessId
	 * @param request
	 * @return
	 * @throws Exception
	 */
	@JsonBody
	@ResponseBody
	@RequestMapping(value = "/stop/playback/fragment")
	public Object stopPlaybackFragment(
			String businessId,
			HttpServletRequest request) throws Exception{
		
		UserVO user = userUtils.getUserFromSession(request);
		
		List<CommandGroupUserPlayerPO> players = commandRecordServiceImpl.stopPlayFragments(user.getId(), new ArrayListWrapper<String>().add(businessId).getList());
		if(players.size() > 0){
			CommandGroupUserPlayerPO player = players.get(0);
			return new CommandGroupUserPlayerSettingVO().set(player);
		}
		return null;
		
	}
	
	/**
	 * 获取片段的下载地址<br/>
	 * <p>参考了MonitorRecordController.downloadUrl()和CommandRecordServiceImpl.playFragments()的抛错</p>
	 * <b>作者:</b>zsy<br/>
	 * <b>版本：</b>1.0<br/>
	 * <b>日期：</b>2020年5月5日 上午10:55:58
	 * @param fragmentId
	 * @param request
	 * @return
	 * @throws Exception
	 */
	@JsonBody
	@ResponseBody
	@RequestMapping(value = "/download/url/{fragmentId}")
	public Object downloadUrl(
			@PathVariable Long fragmentId,
			HttpServletRequest request) throws Exception{
		
		CommandGroupRecordFragmentPO fragment = commandGroupRecordFragmentDao.findOne(fragmentId);
		if(fragment == null){
			throw new BaseException(StatusCode.FORBIDDEN, "未找到该录制，id：" + fragmentId);
		}
		
		List<AccessNodeBO> layers =resourceService.queryAccessNodeByNodeUids(new ArrayListWrapper<String>().add(fragment.getStoreLayerId()).getList());
		if(layers==null || layers.size()<=0){
			throw new AccessNodeNotExistException(fragment.getStoreLayerId());
		}
		AccessNodeBO targetLayer = layers.get(0);
		if(targetLayer.getIp() == null){
			throw new AccessNodeIpMissingException(fragment.getStoreLayerId());
		}
		if(targetLayer.getPort() == null){
			throw new AccessNodePortMissionException(fragment.getStoreLayerId());
		}
		
		StringBufferWrapper downloadUrl = new StringBufferWrapper();
		downloadUrl.append("http://")
				   .append(targetLayer.getIp())
				   .append(":")
				   .append(targetLayer.getDownloadPort())
				   .append("/action/download?file=")
				   .append(fragment.getPreviewUrl());
		
		Date startTime = fragment.getStartTime();
		Date endTime = fragment.getEndTime()==null?new Date():fragment.getEndTime();
		
		long duration = (endTime.getTime()-startTime.getTime())/1000;
		
		return new HashMapWrapper<String, Object>().put("downloadUrl", downloadUrl.toString())
											       .put("duration", duration)
											       .getMap();
	}
	
	/**
	 * 按播放器开始录制（与指挥无关）<br/>
	 * <p>详细描述</p>
	 * <b>作者:</b>zsy<br/>
	 * <b>版本：</b>1.0<br/>
	 * <b>日期：</b>2020年4月29日 下午6:57:47
	 * @param serial
	 * @param mode
	 * @param fileName
	 * @param startTime
	 * @param endTime
	 * @param request
	 * @return
	 * @throws Exception
	 */
	@JsonBody
	@ResponseBody
	@RequestMapping(value = "/player/start")
	public Object playerStart(
			int serial,
			String mode, 
			String fileName, 
			String startTime, 
			String endTime,
			HttpServletRequest request) throws Exception{
		
		UserVO user = userUtils.getUserFromSession(request);
		
		MonitorRecordPO task = commandVodRecordParser.playerStartRecord(user, serial, mode, fileName, startTime, endTime);
		
		return new MonitorRecordTaskVO().set(task);
	}
	
}
