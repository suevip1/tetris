package com.sumavision.tetris.websocket.message;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.websocket.Session;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sumavision.tetris.commons.util.date.DateUtil;
import com.sumavision.tetris.commons.util.wrapper.ArrayListWrapper;
import com.sumavision.tetris.commons.util.wrapper.HashMapWrapper;
import com.sumavision.tetris.commons.util.wrapper.StringBufferWrapper;
import com.sumavision.tetris.mvc.constant.HttpConstant;
import com.sumavision.tetris.user.UserQuery;
import com.sumavision.tetris.user.UserVO;
import com.sumavision.tetris.websocket.core.SessionQueue;
import com.sumavision.tetris.websocket.core.config.ApplicationConfig;
import com.sumavision.tetris.websocket.core.load.balance.SessionMetadataDAO;
import com.sumavision.tetris.websocket.core.load.balance.SessionMetadataPO;
import com.sumavision.tetris.websocket.message.exception.TargetUserNotFoundException;
import com.sumavision.tetris.websocket.message.exception.WebsocketMessageLoadBalacePostConsumeFailException;
import com.sumavision.tetris.websocket.message.exception.WebsocketMessageLoadBalacePostSendFailException;

@Service
@Transactional(rollbackFor = Exception.class)
public class WebsocketMessageService {

	@Autowired
	private UserQuery userQuery;
	
	@Autowired
	private SessionMetadataDAO sessionMetadataDao;
	
	@Autowired
	private ApplicationConfig applicationConfig;
	
	@Autowired
	private WebsocketMessageDAO websocketMessageDao;
	
	/**
	 * 处理离线消息<br/>
	 * <b>作者:</b>lvdeyang<br/>
	 * <b>版本：</b>1.0<br/>
	 * <b>日期：</b>2019年9月11日 下午1:19:18
	 * @param Long userId 用户id
	 */
	@Deprecated
	public void offlineMessage(Long userId) throws Exception{
		List<WebsocketMessagePO> messages = websocketMessageDao.findByUserIdAndConsumed(userId, false);
		if(messages!=null && messages.size()>0){
			SessionQueue queue = SessionQueue.getInstance();
			Session session = queue.get(userId);
			for(WebsocketMessagePO message:messages){
				session.getBasicRemote().sendText(message.getMessage());
				message.setConsumed(true);
			}
			websocketMessageDao.save(messages);
		}
	} 
	
	/**
	 * 只推送消息无数据持久化<br/>
	 * <b>作者:</b>lvdeyang<br/>
	 * <b>版本：</b>1.0<br/>
	 * <b>日期：</b>2020年3月3日 上午8:56:13
	 * @param String targetId 目标id
	 * @param String businessId 业务id
	 * @param JSON content 业务内容
	 * @param String fromId 消息发布者id
	 * @param String fromName 消息发布者名称
	 */
	public void push(
			String targetId,
			String businessId,
			JSON content,
			String fromId,
			String fromName) throws Exception{
		
		SessionQueue queue = SessionQueue.getInstance();
		Session session = queue.get(targetId);
		if(session != null){
			JSONObject message = new JSONObject();
			message.put("targetId", targetId);
			message.put("businessId", businessId);
			message.put("content", content);
			message.put("fromId", fromId);
			message.put("fromName", fromName);
			try{
				session.getBasicRemote().sendText(message.toJSONString());
			} catch(Exception e) {
				System.out.println(new StringBufferWrapper().append("当前用户websocket链接异常，userId:").append(targetId).toString());
				e.printStackTrace();
			}
		}else{
			SessionMetadataPO metadata = sessionMetadataDao.findByUserId(targetId);
			if(metadata != null){
				//防止死循环
				if(!(metadata.getServerIp().equals(applicationConfig.getIp()) && 
						metadata.getServerPort().equals(applicationConfig.getPort()))){
					StringBuffer url = new StringBuffer();
					url.append("http://").append(metadata.getServerIp()).append(":").append(metadata.getServerPort()).append("/message/push");
					CloseableHttpClient httpclient = null;
					CloseableHttpResponse response = null;
					BufferedReader bReader = null;
					try{
						httpclient = HttpClients.createDefault();
						HttpPost httpPost = new HttpPost(url.toString());
						httpPost.setHeader(HttpConstant.HEADER_FEIGN_CLIENT, HttpConstant.HEADER_FEIGN_CLIENT_KEY);
						List<NameValuePair> keyValues = new ArrayList<NameValuePair>();  
						keyValues.add(new BasicNameValuePair("targetId", targetId));
						keyValues.add(new BasicNameValuePair("businessId", businessId));	
						keyValues.add(new BasicNameValuePair("content", content.toJSONString()));
						keyValues.add(new BasicNameValuePair("fromId", fromId));
						keyValues.add(new BasicNameValuePair("fromName", fromName));
						httpPost.setEntity(new UrlEncodedFormEntity(keyValues, "UTF-8")); 
					    response = httpclient.execute(httpPost);
					    if(response.getStatusLine().getStatusCode() != 200){
				        	throw new WebsocketMessageLoadBalacePostSendFailException(metadata.getServerIp(), metadata.getServerPort(), fromName, targetId, content.toJSONString());
					    }
					}finally{
						if(bReader != null) bReader.close();
						if(response != null) response.close();
						if(httpclient != null) httpclient.close();
					}
				}else{
					System.out.println(new StringBufferWrapper().append(targetId).append("获取到websocket元数据，但未获取到session！").toString());
				}
			}else{
				System.out.println(new StringBufferWrapper().append(targetId).append("未注册websocket链接！").toString());
			}
		}
		
	}
	
	/**
	 * 发送websocket消息<br/>
	 * <b>作者:</b>lvdeyang<br/>
	 * <b>版本：</b>1.0<br/>
	 * <b>日期：</b>2019年9月11日 下午1:07:05
	 * @param Long userId 用户id
	 * @param String message 推送消息内容
	 * @param WebsocketMessageType type 消息类型
	 * @param Long fromUserId 消息来源用户id
	 * @param String fromUsername 消息来源用户名称
	 */
	public WebsocketMessageVO send(
			Long userId, 
			String message, 
			WebsocketMessageType type, 
			Long fromUserId,
			String fromUsername) throws Exception{
		
		SessionMetadataPO metadata = sessionMetadataDao.findByUserId(userId.toString());
		List<UserVO> users = userQuery.findByIdIn(new ArrayListWrapper<Long>().add(userId).getList());
		if(users==null || users.size()<=0) throw new TargetUserNotFoundException();
		UserVO user = users.get(0);
		if(metadata == null){
			//离线消息
			WebsocketMessagePO messageEntity = new WebsocketMessagePO();
			messageEntity.setUserId(user.getId());
			messageEntity.setUsername(user.getNickname());
			messageEntity.setMessage(message);
			messageEntity.setMessageType(WebsocketMessageType.valueOf(type.toString()));
			messageEntity.setConsumed(false);
			messageEntity.setUpdateTime(new Date());
			messageEntity.setFromUserId(fromUserId);
			messageEntity.setFromUsername(fromUsername);
			websocketMessageDao.save(messageEntity);
			return new WebsocketMessageVO().set(messageEntity);
		}else{
			if(metadata.getServerIp().equals(applicationConfig.getIp()) && 
					metadata.getServerPort().equals(applicationConfig.getPort())){
				WebsocketMessagePO messageEntity = new WebsocketMessagePO();
				messageEntity.setUserId(user.getId());
				messageEntity.setUsername(user.getNickname());
				messageEntity.setMessage(message);
				messageEntity.setMessageType(WebsocketMessageType.valueOf(type.toString()));
				messageEntity.setConsumed(false);
				messageEntity.setUpdateTime(new Date());
				messageEntity.setFromUserId(fromUserId);
				messageEntity.setFromUsername(fromUsername);
				websocketMessageDao.save(messageEntity);
				try{
					SessionQueue queue = SessionQueue.getInstance();
					Session session = queue.get(userId);
					if(session != null){
						session.getBasicRemote().sendText(message);
						messageEntity.setConsumed(true);
						websocketMessageDao.save(messageEntity);
					}
					return new WebsocketMessageVO().set(messageEntity);
				}catch(Exception e){
					throw e;
				}
			}else{
				return postSend(userId, user.getNickname(), message, type, fromUserId, fromUsername, metadata.getServerIp(), metadata.getServerPort());
			}
		}
	}
	
	/**
	 * 消息转发<br/>
	 * <b>作者:</b>lvdeyang<br/>
	 * <b>版本：</b>1.0<br/>
	 * <b>日期：</b>2019年9月11日 上午11:38:24
	 * @param Long userId 用户id
	 * @param String message 推送消息内容
	 * @param WebsocketMessageType type 消息类型
	 * @param Long fromUserId 消息来源用户id
	 * @param String fromUsername 消息来源用户名称
	 * @param String ip 目标ip
	 * @param String port 目标端口
	 */
	public WebsocketMessageVO postSend(
			Long userId, 
			String username,
			String message, 
			WebsocketMessageType type, 
			Long fromUserId, 
			String fromUsername,
			String ip, 
			String port) throws Exception{
		HttpServletRequest request = ((ServletRequestAttributes)RequestContextHolder.getRequestAttributes()).getRequest();
		String token = request.getHeader(HttpConstant.HEADER_AUTH_TOKEN);
		StringBuffer url = new StringBuffer();
		url.append("http://").append(ip).append(":").append(port).append("/message/send");
		CloseableHttpClient httpclient = null;
		CloseableHttpResponse response = null;
		BufferedReader bReader = null;
		try{
			httpclient = HttpClients.createDefault();
			HttpPost httpPost = new HttpPost(url.toString());
			httpPost.setHeader(HttpConstant.HEADER_AUTH_TOKEN, token);
			List<NameValuePair> keyValues = new ArrayList<NameValuePair>();  
			keyValues.add(new BasicNameValuePair("userId", userId.toString()));
			keyValues.add(new BasicNameValuePair("message", message));	
			keyValues.add(new BasicNameValuePair("type", type.toString()));
			keyValues.add(new BasicNameValuePair("fromUserId", fromUserId.toString()));
			keyValues.add(new BasicNameValuePair("fromUsername", fromUsername));
			httpPost.setEntity(new UrlEncodedFormEntity(keyValues, "UTF-8")); 
		    response = httpclient.execute(httpPost);
		    if(response.getStatusLine().getStatusCode() != 200){
	        	throw new WebsocketMessageLoadBalacePostSendFailException(ip, port, fromUsername, username, message);
		    }else{
		    	StringBuffer jsonBodyResponse = new StringBuffer();
		    	HttpEntity entity = response.getEntity();
	 	        InputStream in = entity.getContent();
	 	        String line = "";
	 	        bReader = new BufferedReader(new InputStreamReader(in, "utf-8"));
	 	        while ((line=bReader.readLine()) != null) {
	 	        	jsonBodyResponse.append(line);
	 			}
	 	        EntityUtils.consume(entity);
	 	        return JSON.parseObject(jsonBodyResponse.toString()).getObject("data", WebsocketMessageVO.class);
		    }
		}finally{
			if(bReader != null) bReader.close();
			if(response != null) response.close();
			if(httpclient != null) httpclient.close();
		}
	}
	
	/**
	 * 重发消息<br/>
	 * <b>作者:</b>lvdeyang<br/>
	 * <b>版本：</b>1.0<br/>
	 * <b>日期：</b>2019年10月17日 下午4:05:16
	 * @param Long id 消息id
	 */
	public void resend(Long id) throws Exception{
		WebsocketMessagePO message = websocketMessageDao.findOne(id);
		SessionMetadataPO metadata = sessionMetadataDao.findByUserId(message.getUserId().toString());
		if(metadata == null) return;
		if(metadata.getServerIp().equals(applicationConfig.getIp()) && 
				metadata.getServerPort().equals(applicationConfig.getPort())){
			try{
				SessionQueue queue = SessionQueue.getInstance();
				Session session = queue.get(message.getUserId());
				session.getBasicRemote().sendText(message.getMessage());
			}catch(Exception e){
				throw e;
			}
		}else{
			postResend(id, metadata.getServerIp(), metadata.getServerPort());
		}
	}
	
	/**
	 * 负载均衡重发消息转发<br/>
	 * <b>作者:</b>lvdeyang<br/>
	 * <b>版本：</b>1.0<br/>
	 * <b>日期：</b>2019年10月17日 下午4:05:39
	 * @param Long id 消息id
	 * @param String ip 目标服务器ip
	 * @param String port 目标服务器端口
	 */
	public void postResend(
			Long id,
			String ip,
			String port) throws Exception{
		HttpServletRequest request = ((ServletRequestAttributes)RequestContextHolder.getRequestAttributes()).getRequest();
		String token = request.getHeader(HttpConstant.HEADER_AUTH_TOKEN);
		StringBuffer url = new StringBuffer();
		url.append("http://").append(ip).append(":").append(port).append("/message/resend");
		CloseableHttpClient httpclient = null;
		CloseableHttpResponse response = null;
		BufferedReader bReader = null;
		try{
			httpclient = HttpClients.createDefault();
			HttpPost httpPost = new HttpPost(url.toString());
			httpPost.setHeader(HttpConstant.HEADER_AUTH_TOKEN, token);
			List<NameValuePair> keyValues = new ArrayList<NameValuePair>();  
			keyValues.add(new BasicNameValuePair("id", id.toString()));
			httpPost.setEntity(new UrlEncodedFormEntity(keyValues, "UTF-8")); 
		    response = httpclient.execute(httpPost);
		    if(response.getStatusLine().getStatusCode() != 200){
	        	throw new WebsocketMessageLoadBalacePostSendFailException(ip, port, id);
		    }
		}finally{
			if(bReader != null) bReader.close();
			if(response != null) response.close();
			if(httpclient != null) httpclient.close();
		}
	}
	
	/**
	 * 批量消费临时消息,消费后不会再向前端推消息<br/>
	 * <b>作者:</b>lvdeyang<br/>
	 * <b>版本：</b>1.0<br/>
	 * <b>日期：</b>2019年10月21日 下午4:16:56
	 * @param Collection<Long> ids 消息id列表
	 */
	public void consumeAll(Collection<Long> ids) throws Exception{
		List<WebsocketMessagePO> messages = websocketMessageDao.findAll(ids);
		if(messages!=null && messages.size()>0){
			for(WebsocketMessagePO message:messages){
				message.setConsumed(true);
			}
			websocketMessageDao.save(messages);
		}
	}

	/**
	 * 消费一个消息<br/>
	 * <b>作者:</b>lvdeyang<br/>
	 * <b>版本：</b>1.0<br/>
	 * <b>日期：</b>2019年10月14日 下午5:03:01
	 * @param Long id 消息id
	 */
	public void consume(Long id) throws Exception{
		WebsocketMessagePO entity = websocketMessageDao.findOne(id);
		SessionMetadataPO metadata = sessionMetadataDao.findByUserId(entity.getUserId().toString());
		if(metadata == null){
			entity.setConsumed(true);
			websocketMessageDao.save(entity);
		}else{
			if(metadata.getServerIp().equals(applicationConfig.getIp()) && 
					metadata.getServerPort().equals(applicationConfig.getPort())){
				try{
					SessionQueue queue = SessionQueue.getInstance();
					Session session = queue.get(entity.getUserId());
					session.getBasicRemote().sendText(JSON.toJSONString(new HashMapWrapper<String, String>().put("cmd", "messageConsumed")
																											.put("messageId", id.toString())
																											.getMap()));
					entity.setConsumed(true);
				}catch(Exception e){
					e.printStackTrace();
					entity.setConsumed(true);
					websocketMessageDao.save(entity);
				}
				
			}else{
				postConsume(id, metadata.getServerIp(), metadata.getServerPort());
			}
		}
	}
	
	/**
	 * 转发消费一个消息<br/>
	 * <b>作者:</b>lvdeyang<br/>
	 * <b>版本：</b>1.0<br/>
	 * <b>日期：</b>2019年10月14日 下午5:21:18
	 * @param Long id 消息id
	 * @param String ip 目标ip
	 * @param String port 目标端口
	 */
	private void postConsume(Long id, String ip, String port) throws Exception{
		HttpServletRequest request = ((ServletRequestAttributes)RequestContextHolder.getRequestAttributes()).getRequest();
		String token = request.getHeader(HttpConstant.HEADER_AUTH_TOKEN);
		StringBuffer url = new StringBuffer();
		url.append("http://").append(ip).append(":").append(port).append("/message/consume");
		CloseableHttpClient httpclient = null;
		CloseableHttpResponse response = null;
		BufferedReader bReader = null;
		try{
			httpclient = HttpClients.createDefault();
			HttpPost httpPost = new HttpPost(url.toString());
			httpPost.setHeader(HttpConstant.HEADER_AUTH_TOKEN, token);
			List<NameValuePair> keyValues = new ArrayList<NameValuePair>();  
			keyValues.add(new BasicNameValuePair("id", id.toString()));
			httpPost.setEntity(new UrlEncodedFormEntity(keyValues, "UTF-8")); 
		    response = httpclient.execute(httpPost);
		    if(response.getStatusLine().getStatusCode() != 200){
	        	throw new WebsocketMessageLoadBalacePostConsumeFailException(ip, port, id);
		    }
		}finally{
			if(bReader != null) bReader.close();
			if(response != null) response.close();
			if(httpclient != null) httpclient.close();
		}
	}
	
	/**
	 * 广播会议消息<br/>
	 * <b>作者:</b>lvdeyang<br/>
	 * <b>版本：</b>1.0<br/>
	 * <b>日期：</b>2020年2月21日 下午7:00:52
	 * @param Long commandId 会议id
	 * @param Collection<Long> userIds 会议成员用户id列表
	 * @param String message 消息内容
	 * @param Long fromUserId 发起人id
	 * @param String fromUsername fromUserName名称
	 */
	public void broadcastMeetingMessage(
			Long commandId,
			Collection<Long> userIds,
			String message,
			Long fromUserId,
			String fromUsername) throws Exception{
		
		//广播消息
		WebsocketMessagePO messageEntity = new WebsocketMessagePO();
		messageEntity.setUserId(commandId);
		messageEntity.setUsername("会议广播");
		messageEntity.setMessage(message);
		messageEntity.setMessageType(WebsocketMessageType.INSTANT_MESSAGE);
		messageEntity.setConsumed(true);
		messageEntity.setUpdateTime(new Date());
		messageEntity.setFromUserId(fromUserId);
		messageEntity.setFromUsername(fromUsername);
		websocketMessageDao.save(messageEntity);
		
		SessionQueue queue = SessionQueue.getInstance();
		
		if(userIds!=null && userIds.size()>0){
			for(Long userId:userIds){
				if(userId.equals(fromUserId)) continue;
				Session session = queue.get(userId);
				if(session != null){
					message = new StringBufferWrapper().append(fromUsername).append("（").append(DateUtil.format(new Date(), DateUtil.dateTimePattern)).append("）：").append(message).toString();
					
					message = JSON.toJSONString(new HashMapWrapper<String, Object>().put("businessType", "receiveInstantMessage")
																				    .put("message", message)
																				    .put("commandId", commandId)
																				    .getMap());
					session.getBasicRemote().sendText(message);
				}
			}
		}
		
	}
	
}
