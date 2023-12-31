package com.sumavision.tetris.mims.app.media.stream.audio.feign;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.sumavision.tetris.mims.app.media.stream.audio.MediaAudioStreamQuery;
import com.sumavision.tetris.mims.app.media.stream.audio.MediaAudioStreamService;
import com.sumavision.tetris.mvc.ext.response.json.aop.annotation.JsonBody;

@Controller
@RequestMapping(value = "/media/audio/stream/feign")
public class MediaAudioStreamFeignController {

	@Autowired
	private MediaAudioStreamQuery mediaAudioStreamQuery;
	
	@Autowired
	private MediaAudioStreamService mediaAudioStreamService;
	
	/**
	 * 加载文件夹下的音频流媒资<br/>
	 * <b>作者:</b>lvdeyang<br/>
	 * <b>版本：</b>1.0<br/>
	 * <b>日期：</b>2018年12月6日 下午4:03:27
	 * @param folderId 文件夹id
	 * @return rows List<MediaAudioStreamVO> 音频流媒资列表
	 * @return breadCrumb FolderBreadCrumbVO 面包屑数据
	 */
	@JsonBody
	@ResponseBody
	@RequestMapping(value = "/load")
	public Object load(
			Long folderId,
			HttpServletRequest request) throws Exception{
		
		return mediaAudioStreamQuery.load(folderId);
	}
	
	/**
	 * 加载所有音频流媒资<br/>
	 * <b>作者:</b>lzp<br/>
	 * <b>版本：</b>1.0<br/>
	 * <b>日期：</b>2018年12月6日 下午4:03:27
	 * @return List<MediaAudioStreamVO> 视频流媒资列表
	 */
	@JsonBody
	@ResponseBody
	@RequestMapping(value = "/load/all")
	public Object loadAll(
			HttpServletRequest request) throws Exception{
		
		return mediaAudioStreamQuery.loadAll();
	}
	
	/**
	 * 根据id查询音频流<br/>
	 * <b>作者:</b>lvdeyang<br/>
	 * <b>版本：</b>1.0<br/>
	 * <b>日期：</b>2019年11月5日 上午11:15:05
	 * @param JSONString previewUrls 预览地址列表
	 * @return List<MediaAudioStreamVO> 音频流列表
	 */
	@JsonBody
	@ResponseBody
	@RequestMapping(value = "/find/by/id")
	public Object findById(
			Long id, 
			HttpServletRequest request) throws Exception{
		return mediaAudioStreamQuery.findById(id);
	}
	
	/**
	 * 根据预览地址查询音频流<br/>
	 * <b>作者:</b>lvdeyang<br/>
	 * <b>版本：</b>1.0<br/>
	 * <b>日期：</b>2019年11月5日 上午11:15:05
	 * @param JSONString previewUrls 预览地址列表
	 * @return List<MediaAudioStreamVO> 音频流列表
	 */
	@JsonBody
	@ResponseBody
	@RequestMapping(value = "/find/by/preview/url/in")
	public Object findByPreviewUrlIn(
			String previewUrls, 
			HttpServletRequest request) throws Exception{
		return mediaAudioStreamQuery.findByPreviewUrlIn(JSON.parseArray(previewUrls, String.class));
	}
	
	/**
	 * 添加上传音频流媒资任务<br/>
	 * <b>作者:</b>lzp<br/>
	 * <b>版本：</b>1.0<br/>
	 * <b>日期：</b>2018年11月29日 下午1:44:06
	 * @param String previewUrl 流地址
	 * @param String name 媒资名称
	 * @return MediaVideoStreamVO 任务列表 
	 */
	@JsonBody
	@ResponseBody
	@RequestMapping(value = "/task/add")
	public Object addTask(
			String previewUrl, 
			String name,
			String streamType,
			HttpServletRequest request) throws Exception{
		
		return  mediaAudioStreamService.addAudioStreamTask(previewUrl, "", name, streamType);
	}
	
	/**
	 * 视频流媒资删除<br/>
	 * <b>作者:</b>lvdeyang<br/>
	 * <b>版本：</b>1.0<br/>
	 * <b>日期：</b>2019年7月17日 下午3:43:03
	 * @param Long mediaId 视频流媒资id
	 */
	@JsonBody
	@ResponseBody
	@RequestMapping(value = "/remove")
	public Object remove(
			String mediaIds, 
			HttpServletRequest request) throws Exception{
		List<Long> mediaIdList = JSONArray.parseArray(mediaIds, Long.class);
		mediaAudioStreamService.removeByIds(mediaIdList);
		return null;
	}
}
