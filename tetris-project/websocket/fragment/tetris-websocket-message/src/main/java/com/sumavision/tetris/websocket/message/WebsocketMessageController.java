package com.sumavision.tetris.websocket.message;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.sumavision.tetris.mvc.ext.response.json.aop.annotation.JsonBody;
import com.sumavision.tetris.user.UserQuery;
import com.sumavision.tetris.user.UserVO;

@Controller
@RequestMapping(value = "/message")
public class WebsocketMessageController {

	@Autowired
	private WebsocketMessageService websocketMessageService;
	
	@Autowired
	private WebsocketMessageDAO websocketMessageDao;
	
	@Autowired
	private UserQuery userQuery;
	
	/**
	 * 发送消息<br/>
	 * <b>作者:</b>lvdeyang<br/>
	 * <b>版本：</b>1.0<br/>
	 * <b>日期：</b>2019年9月11日 下午1:35:30
	 * @param Long userId 用户id
	 * @param String message 推送消息
	 * @param String type 消息类型
	 * @param Long fromUserId 消息来源用户id
	 * @param String fromUsername 消息来源用户名称
	 */
	@JsonBody
	@ResponseBody
	@RequestMapping(value = "/send")
	public Object send(
			Long userId,
			String message,
			String type,
			Long fromUserId,
			String fromUsername,
			HttpServletRequest request) throws Exception{
		if(fromUserId == null){
			UserVO user = userQuery.current();
			fromUserId = user.getId();
		}
		websocketMessageService.send(userId, message, WebsocketMessageType.valueOf(type), fromUserId, fromUsername);
		return null;
	}
	
	/**
	 * 重发消息<br/>
	 * <b>作者:</b>lvdeyang<br/>
	 * <b>版本：</b>1.0<br/>
	 * <b>日期：</b>2019年10月17日 下午4:04:43
	 * @param Long id 消息id
	 */
	public Object resend(Long id, HttpServletRequest request) throws Exception{
		websocketMessageService.resend(id);
		return null;
	}
	
	/**
	 * 消费一个消息<br/>
	 * <b>作者:</b>lvdeyang<br/>
	 * <b>版本：</b>1.0<br/>
	 * <b>日期：</b>2019年10月14日 下午5:00:24
	 * @param Long id 消息id
	 */
	@JsonBody
	@ResponseBody
	@RequestMapping(value = "/consume")
	public Object consume(
			Long id, 
			HttpServletRequest request) throws Exception{
		websocketMessageService.consume(id);
		return null;
	}
	
	/**
	 * 根据发送用户查询离线消息<br/>
	 * <b>作者:</b>lvdeyang<br/>
	 * <b>版本：</b>1.0<br/>
	 * <b>日期：</b>2019年10月14日 下午3:08:25
	 * @param Long fromUserId 发送用户id
	 * @return List<WebsocketMessageVO> 消息列表
	 */
	@JsonBody
	@ResponseBody
	@RequestMapping(value = "/find/unconsumed/instant/message/by/from/user/id")
	public Object findUnconsumedInstantMessageByFromUserId(
			Long fromUserId,
			HttpServletRequest request) throws Exception{
		
		UserVO user = userQuery.current();
		
		List<WebsocketMessagePO> entities = websocketMessageDao.findByUserIdAndFromUserIdAndMessageTypeAndConsumed(user.getId(), fromUserId, WebsocketMessageType.INSTANT_MESSAGE, false);
		List<WebsocketMessageVO> messages = new ArrayList<WebsocketMessageVO>();
		if(entities!=null && entities.size()>0){
			for(WebsocketMessagePO entity:entities){
				messages.add(new WebsocketMessageVO().set(entity));
			}
		}
		return messages;
	}
	/**
	 * 分用户统计当前用户的离线消息<br/>
	 * <b>作者:</b>lvdeyang<br/>
	 * <b>版本：</b>1.0<br/>
	 * <b>日期：</b>2019年10月14日 下午4:05:47
	 * @return List<StatisticsInstantMessageResultVO> 统计结果
	 */
	@JsonBody
	@ResponseBody
	@RequestMapping(value = "/statistics/unconsumed/instant/message/munber")
	public Object statisticsUnconsumedInstantMessageNumber(HttpServletRequest request) throws Exception{
		UserVO user = userQuery.current();
		List<StatisticsInstantMessageResultDTO> entities = websocketMessageDao.statisticsUnconsumedInstantMessageNumber(user.getId());
		List<StatisticsInstantMessageResultVO> messages = new ArrayList<StatisticsInstantMessageResultVO>();
		if(entities!=null && entities.size()>0){
			for(StatisticsInstantMessageResultDTO entity:entities){
				messages.add(new StatisticsInstantMessageResultVO().set(entity));
			}
		}
		return messages;
	}
	
	/**
	 * 查询当前用户未消费的命令消息<br/>
	 * <b>作者:</b>lvdeyang<br/>
	 * <b>版本：</b>1.0<br/>
	 * <b>日期：</b>2019年10月14日 下午1:41:56
	 * @return List<WebsocketMessageVO> 命令列表
	 */
	@JsonBody
	@ResponseBody
	@RequestMapping(value = "/find/unconsumed/commands")
	public Object findUnconsumedCommands(HttpServletRequest request) throws Exception{
		UserVO user = userQuery.current();
		List<WebsocketMessagePO> entities = websocketMessageDao.findByUserIdAndMessageTypeAndConsumed(user.getId(), WebsocketMessageType.COMMAND, false);
		List<WebsocketMessageVO> messages = new ArrayList<WebsocketMessageVO>();
		if(entities!=null && entities.size()>0){
			for(WebsocketMessagePO entity:entities){
				messages.add(new WebsocketMessageVO().set(entity));
			}
		}
		return messages;
	}
	
	/**
	 * 查询当前用户未消费的命令数量<br/>
	 * <b>作者:</b>lvdeyang<br/>
	 * <b>版本：</b>1.0<br/>
	 * <b>日期：</b>2019年10月14日 下午1:41:56
	 * @return Long 命令数量
	 */
	@JsonBody
	@ResponseBody
	@RequestMapping(value = "/count/by/unconsumed/commands")
	public Object countByUnconsumedCommands(HttpServletRequest request) throws Exception{
		UserVO user = userQuery.current();
		return websocketMessageDao.countByUserIdAndMessageTypeAndConsumed(user.getId(), WebsocketMessageType.COMMAND, false);
	}
	
}