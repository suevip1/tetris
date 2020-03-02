package com.sumavision.tetris.websocket.message;

import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.sumavision.tetris.mvc.ext.response.parser.JsonBodyResponseParser;

@Service
@Transactional(rollbackFor = Exception.class)
public class WebsocketMessageService {

	@Autowired
	private WebsocketMessageFeign websocketMessageFeign;
	
	/**
	 * 发送websocket消息<br/>
	 * <b>作者:</b>zsy<br/>
	 * <b>版本：</b>1.0<br/>
	 * <b>日期：</b>2019年9月11日 下午1:07:05
	 * @param Long userId 用户id
	 * @param String message 推送消息内容
	 * @param WebsocketMessageType type 消息类型
	 * @param Long fromUserId 无用参数，兼容老版本
	 * @param String fromUsername 无用参数，兼容老版本
	 * @return
	 * @throws Exception
	 */
	public WebsocketMessageVO send(
			Long userId, 
			String message, 
			WebsocketMessageType type, 
			Long fromUserId,
			String fromUsername) throws Exception{

		return send(userId, message, type);
	}
	
	/**
	 * 发送websocket消息<br/>
	 * <b>作者:</b>lvdeyang<br/>
	 * <b>版本：</b>1.0<br/>
	 * <b>日期：</b>2019年9月11日 下午1:07:05
	 * @param Long userId 用户id
	 * @param String message 推送消息内容
	 * @param WebsocketMessageType type 消息类型
	 */
	public WebsocketMessageVO send(
			Long userId, 
			String message, 
			WebsocketMessageType type) throws Exception{
		return JsonBodyResponseParser.parseObject(websocketMessageFeign.send(userId, message, type.toString()), WebsocketMessageVO.class);
	}
	
	/**
	 * 重发消息<br/>
	 * <b>作者:</b>zsy<br/>
	 * <b>版本：</b>1.0<br/>
	 * <b>日期：</b>2020年1月13日 上午9:29:09
	 * @param Long id 消息id
	 */
	public void resend(Long id) throws Exception{
		websocketMessageFeign.resend(id);
	}
	
	/**
	 * 消费消息<br/>
	 * <b>作者:</b>wjw<br/>
	 * <b>版本：</b>1.0<br/>
	 * <b>日期：</b>2019年10月23日 上午9:29:09
	 * @param Long id 消息id
	 */
	public void consume(Long id) throws Exception{
		websocketMessageFeign.consume(id);
	}
	
	/**
	 * 批量消费消息<br/>
	 * <p>详细描述</p>
	 * <b>作者:</b>zsy<br/>
	 * <b>版本：</b>1.0<br/>
	 * <b>日期：</b>2020年2月25日 下午2:48:08
	 * @param idsList
	 * @throws Exception
	 */
	public void consumeAll(Collection<Long> idsList) throws Exception{
		String ids = JSON.toJSONString(idsList);
		websocketMessageFeign.consumeAll(ids);
	}
	
	
	public Object findUnconsumedInstantMessageByFromUserId(Long fromUserId){
		return websocketMessageFeign.findUnconsumedInstantMessageByFromUserId(fromUserId);
	}
	
	
	public Object statisticsUnconsumedInstantMessageNumber(){
		return websocketMessageFeign.statisticsUnconsumedInstantMessageNumber();
	}
	
	
	public Object findUnconsumedCommands(){
		return websocketMessageFeign.findUnconsumedCommands();
	}
	
	
	public Object countByUnconsumedCommands(){
		return websocketMessageFeign.countByUnconsumedCommands();
	}
	
	
	public void broadcastMeetingMessage(
			Long commandId,
			Collection<Long> userIds,
			String message,
			Long fromUserId,
			String fromUsername){
		String userIdsStr = JSON.toJSONString(userIds);
		websocketMessageFeign.broadcastMeetingMessage(commandId, userIdsStr, message, fromUserId, fromUsername);
	}
	
	public Object queryHistoryInstantMessage(
			Long commandId,
			int currentPage,
			int pageSize){
		return websocketMessageFeign.queryHistoryInstantMessage(commandId, currentPage, pageSize);
	}
	
}
