package com.sumavision.tetris.transcoding;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONObject;
import com.sumavision.tetris.commons.util.httprequest.HttpRequestUtil;
import com.sumavision.tetris.commons.util.wrapper.HashMapWrapper;
import com.sumavision.tetris.commons.util.xml.XmlUtil;
import com.sumavision.tetris.easy.process.core.ProcessService;
import com.sumavision.tetris.media.editor.task.MediaEditorTaskQuery;
import com.sumavision.tetris.media.editor.task.MediaEditorTaskRatePermissionQuery;
import com.sumavision.tetris.media.editor.task.MediaEditorTaskRatePermissionService;
import com.sumavision.tetris.media.editor.task.MediaEditorTaskRatePermissionVO;
import com.sumavision.tetris.media.editor.task.MediaEditorTaskVO;
import com.sumavision.tetris.mims.app.media.MediaFeign;
import com.sumavision.tetris.mims.app.media.MediaQuery;
import com.sumavision.tetris.mvc.ext.response.json.aop.annotation.JsonBody;
import com.sumavision.tetris.mvc.wrapper.CachedHttpServletRequestWrapper;
import com.sumavision.tetris.transcoding.addTask.AddTaskService;
import com.sumavision.tetris.transcoding.getStatus.GetStatusService;
import com.sumavision.tetris.transcoding.getTemplates.TemplatesRequest;
import com.sumavision.tetris.transcoding.getTemplates.VO.TemplatesResponseVO;
import com.sumavision.tetris.user.UserQuery;
import com.sumavision.tetris.user.UserVO;

@Controller
@RequestMapping(value = "/api/qt/transcoding")
public class TranscodingController {
	
	@Autowired
	private UserQuery userQuery;

	@Autowired
	private AddTaskService addTaskService;
	
	@Autowired
	private GetStatusService getStatusService;

	@Autowired
	private TemplatesRequest templatesRequest;
	
	@Autowired
	private ProcessService processService;
	
	@Autowired
	private MediaEditorTaskQuery mediaEditorTaskQuery;
	
	@Autowired
	private MediaEditorTaskRatePermissionService mediaEditorTaskRatePermissionService;
	
	@Autowired
	private MediaEditorTaskRatePermissionQuery mediaEditorTaskRatePermissionQuery;
	
	@Autowired
	private Adapter adapter;

	/**
	 * 获取云转码模板列表<br/>
	 * <b>作者:</b>lzp<br/>
	 * <b>版本：</b>1.0<br/>
	 * <b>日期：</b>2019年6月25日 上午11:06:57
	 * 
	 * @return List<String>
	 */
	@JsonBody
	@ResponseBody
	@RequestMapping(value = "/template/list")
	public Object getTemplates(HttpServletRequest request) throws Exception {
		return templatesRequest.getTemplates();
	}

	/**
	 * 添加转码任务<br/>
	 * <b>作者:</b>lzp<br/>
	 * <b>版本：</b>1.0<br/>
	 * <b>日期：</b>2019年6月25日 上午11:06:57
	 * @param transcodeJobs 转码任务信息
	 * @param __processInstanceId__ 流程任务id
	 * @param __accessPointId__ 流程节点id
	 * @return transcodeIds 流程任务id
	 */
	@JsonBody
	@ResponseBody
	@RequestMapping(value = "/task/add")
	public Object addTask(String transcodeJob, String param, String name, Long folderId, String tags, String __processInstanceId__,
			Long __accessPointId__, HttpServletRequest request) throws Exception {

		HashMapWrapper<String, MediaEditorTaskRatePermissionVO> ids = addTaskService.add(__processInstanceId__, __accessPointId__, transcodeJob, param, name, folderId, tags);
		
		return ids != null && ids.size() > 0 ? new HashMapWrapper<String, HashMapWrapper<String, MediaEditorTaskRatePermissionVO>>().put("transcodeIds", ids)
				   .getMap() : null;
	}
	
	/**
	 * 流程任务完成回调<br/>
	 * <b>作者:</b>lzp<br/>
	 * <b>版本：</b>1.0<br/>
	 * <b>日期：</b>2019年6月25日 上午11:06:57
	 */
	@JsonBody
	@ResponseBody
	@RequestMapping(value = "/complete/notify")
	public Object addTask(HttpServletRequest request) throws Exception{

		CachedHttpServletRequestWrapper requestWrapper = new CachedHttpServletRequestWrapper(request);
		String completeString = new String(requestWrapper.getBytes(), "utf-8");
		
		adapter.completeNotiry(completeString);

		return null;
	}
	
	/**
	 * 添加转码任务<br/>
	 * <b>作者:</b>lzp<br/>
	 * <b>版本：</b>1.0<br/>
	 * <b>日期：</b>2019年6月25日 上午11:06:57
	 * @param transcodeJobs 转码任务信息
	 * @return processId 流程任务id
	 * @return mediaTask 流程任务
	 */
	@JsonBody
	@ResponseBody
	@RequestMapping(value = "/start/process")
	public Object start(String transcodeJob,String param, String name, Long folderId, String tags, HttpServletRequest request) throws Exception{
		JSONObject variables = new JSONObject();
		variables.put("_pa3_transcodeJob", transcodeJob);
		variables.put("_pa3_param", param);
		variables.put("_pa3_name", name);
		variables.put("_pa3_folderId", folderId);
		variables.put("_pa3_tags", (tags == null || tags.isEmpty()) ? "" :StringUtils.join(JSONObject.parseArray(tags, String.class).toArray(), ","));
		
		String processInstanceId = processService.startByKey("_media_editor_transcoding_by_qt", variables.toJSONString(), null, null);
		
		MediaEditorTaskVO task = mediaEditorTaskQuery.getByProcessId(processInstanceId);
		
		return new HashMapWrapper<String, Object>().put("processId", processInstanceId)
				.put("mediaTask", task)
				.getMap();
	}
	
	/**
	 * 根据用户获取流程任务列表列表<br/>
	 * <b>作者:</b>lzp<br/>
	 * <b>版本：</b>1.0<br/>
	 * <b>日期：</b>2019年6月25日 上午11:06:57
	 * @return List<MediaEditorTaskVO> 流程任务列表
	 */
	@JsonBody
	@ResponseBody
	@RequestMapping(value = "/status/list")
	public Object getStatusList(HttpServletRequest request) throws Exception {
		UserVO userVO = userQuery.current();
		
		return mediaEditorTaskQuery.getTaskTree(userVO);
	}
	
	@JsonBody
	@ResponseBody
	@RequestMapping(value = "/test")
	public Object test(String xml, Boolean ifToFirst, HttpServletRequest request) throws Exception {
//		HttpRequestUtil.httpXmlPost(RequestUrlType.GET_TEMPLETE_NAME_LIST_URL.getUrl(),xml);
//		return null;
		JSONObject variables = new JSONObject();
		variables.put("_pa58_firstFirst", 1);
		variables.put("_pa58_firstSecond", 2);
		variables.put("_pa59_secondFirst", 3);
		variables.put("_pa59_secondSecond", 4);
		variables.put("ifToFirst", ifToFirst);
		
		String processInstanceId = processService.startByKey("_lzp_test_process", variables.toJSONString(), null, null);
		
		return new HashMapWrapper<String, Object>().put("processId", processInstanceId)
				.getMap();
	}
}
