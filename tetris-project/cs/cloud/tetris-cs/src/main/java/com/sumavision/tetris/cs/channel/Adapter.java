package com.sumavision.tetris.cs.channel;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.sumavision.tetris.commons.util.wrapper.HashMapWrapper;
import com.sumavision.tetris.commons.util.wrapper.StringBufferWrapper;
import com.sumavision.tetris.cs.channel.exception.ChannelUdpBroadCountIsFullException;
import com.sumavision.tetris.mims.config.server.MimsServerPropsQuery;
import com.sumavision.tetris.mims.config.server.ServerProps;

@Service
@Transactional(rollbackFor = Exception.class)
public class Adapter {
	@Autowired
	private MimsServerPropsQuery mimsServerPropsQuery;
	
	/**
	 * 读配置文件获取分屏模板<br/>
	 * <b>作者:</b>lzp<br/>
	 * <b>版本：</b>1.0<br/>
	 * <b>日期：</b>2019年12月11日 下午5:33:21
	 * @param Long channelId 频道Id
	 * @return JSONArray 分屏模板数组
	 */
	public JSONArray getAllTemplate(BroadWay broadWay) throws Exception{
		JSONArray template = new JSONArray();
		if (broadWay == BroadWay.ABILITY_BROAD) {
			template.add(oneScreen());
		} else {
			String templateString = readTemplate();
			if (templateString == null || templateString.isEmpty()){
				template.add(oneScreen());
			}  else {
				template = JSONArray.parseArray(templateString);
			}
		}
		return template;
	}
	
	/**
	 * 如没有配置文件或使用推流，则仅提供默认一分屏<br/>
	 * <b>作者:</b>lzp<br/>
	 * <b>版本：</b>1.0<br/>
	 * <b>日期：</b>2020年1月3日 下午3:44:08
	 * @return
	 */
	private JSONObject oneScreen() {
		JSONObject object = new JSONObject();
		object.put("id", 1);
		object.put("name", "一分屏");
		object.put("screenNum", 1);
		JSONArray screen = new JSONArray();
		JSONObject screenObject = new JSONObject();
		screenObject.put("no", 1);
		screenObject.put("width", "100%");
		screenObject.put("height", "100%");
		screenObject.put("top", "0%");
		screenObject.put("left", "0%");
		screen.add(screenObject);
		object.put("screen", screen);
		return object;
	}
	
	/**
	 * 根据分屏数获取分屏信息<br/>
	 * <b>作者:</b>lzp<br/>
	 * <b>版本：</b>1.0<br/>
	 * <b>日期：</b>2020年1月3日 下午3:50:30
	 * @param Long screenNum 分屏数
	 * @return JSONObject 分屏信息
	 */
	public JSONObject screenTemplate(Long screenNum) throws Exception {
		JSONArray templates = getAllTemplate(BroadWay.FILE_DOWNLOAD_BROAD);
		JSONObject useTemplate = null;
		for (Object object : templates) {
			JSONObject template = JSONObject.parseObject(JSONObject.toJSONString(object));
			if (template.getLong("screenNum") == screenNum) {
				useTemplate = template;
				break;
			}
		}
		return useTemplate;
	}
	
	/**
	 * 获取模板指定位置分屏分辨率<br/>
	 * <b>作者:</b>lzp<br/>
	 * <b>版本：</b>1.0<br/>
	 * <b>日期：</b>2020年1月3日 下午3:53:59
	 * @param JSONObject template 模板
	 * @param Integer serialNum 指定分屏位置
	 * @return
	 */
	public JSONObject serial(JSONObject template, Integer serialNum) {
		JSONObject returnObject = null;
		JSONArray screens = template.getJSONArray("screen");
		for (Object object : screens) {
			JSONObject screen = JSONObject.parseObject(JSONObject.toJSONString(object));
			if (screen.getInteger("no") == serialNum){
				returnObject = new JSONObject();
				returnObject.put("width", screen.getString("width"));
				returnObject.put("height", screen.getString("height"));
				returnObject.put("top", screen.getString("top"));
				returnObject.put("left", screen.getString("left"));
				return returnObject;
			}
		}
		return returnObject;
	}
	
	/**
	 * 适配：http地址转ftp地址(ftp账号密码在配置文件，默认ftp根目录为../webapps/ROOT)<br/>
	 * <b>作者:</b>lzp<br/>
	 * <b>版本：</b>1.0<br/>
	 * <b>日期：</b>2019年8月1日 上午11:06:57
	 */
	public String changeHttpToFtp(String httpUrl) throws Exception {		
		System.out.println("httpUrl:" + httpUrl + ";" + httpUrl.endsWith(".m3u8"));
		if (httpUrl.endsWith(".m3u8")) return httpUrl;

		String ftpPath = null;

		String[] split = httpUrl.split(":");
		
		ServerProps serverProps = mimsServerPropsQuery.queryProps();

		//没有端口的Url地址
		if (split.length == 2) {
			String[] splite2 = split[1].split("//");
			
			String path = splite2[1].substring(splite2[1].indexOf("/"));
			
			ftpPath = new StringBufferWrapper().append("ftp://").append(serverProps.getFtpUsername())
					  .append(":")
					  .append(serverProps.getFtpPassword())
					  .append("@")
					  .append(serverProps.getFtpIp())
					  .append(":")
					  .append(serverProps.getFtpPort())
					  .append(path)
					  .toString();
		//有端口的url地址
		} else if (split.length == 3) {

			String path = split[2].substring(split[2].indexOf("/"));

			ftpPath = new StringBufferWrapper().append("ftp://").append(serverProps.getFtpUsername())
					  .append(":")
					  .append(serverProps.getFtpPassword())
					  .append("@")
					  .append(serverProps.getFtpIp())
					  .append(":")
					  .append(serverProps.getFtpPort())
					  .append(path)
					  .toString();
		}

		return ftpPath;
	}
	
	/**
	 * 适配：根据ip和port生成udp的url<br/>
	 * <b>作者:</b>lzp<br/>
	 * <b>版本：</b>1.0<br/>
	 * <b>日期：</b>2019年8月1日 上午11:06:57
	 */
	public String getUdpUrlFromIpAndPort(String ip,String port){
		if (ip == null || port == null) return null;
		
		return new StringBufferWrapper().append("udp://@").append(ip).append(":").append(port).toString();
	}
	
	/**
	 * 适配：根据udp的url获取ip和port<br/>
	 * <b>作者:</b>lzp<br/>
	 * <b>版本：</b>1.0<br/>
	 * <b>日期：</b>2019年8月1日 上午11:06:57
	 * @throws Exception 
	 */
	public HashMapWrapper<String, String> getIpAndPortFromUdpUrl(String url) throws Exception{
		if (url == null || url.isEmpty()) return null;
		
		String[] split = url.split(":");
		
		String ip = split[1].split("@")[1];
		
		String port = split[2];
		
		return new HashMapWrapper<String, String>().put("ip", ip)
				.put("port", port);
	}
	
	/**
	 * 适配：获取新的能力播发id<br/>
	 * <b>作者:</b>lzp<br/>
	 * <b>版本：</b>1.0<br/>
	 * <b>日期：</b>2019年8月1日 上午11:06:57
	 */
	public int getNewId(List<Integer> ids) throws Exception{
		List<Integer> parentList = new ArrayList<Integer>();
		for (int i = 0; i < 256; i++) {
			parentList.add(i);
		}
		parentList.removeAll(ids);
		if (parentList.isEmpty()) throw new ChannelUdpBroadCountIsFullException();
		return parentList.get(0);
	}
	
	/**
	 * 获取template.json配置<br/>
	 * <b>作者:</b>lzp<br/>
	 * <b>版本：</b>1.0<br/>
	 * <b>日期：</b>2019年9月5日 下午2:26:47
	 * @return String json
	 */
	public String readTemplate() throws Exception{
		BufferedReader in = null;
		InputStreamReader reader = null;
		String json = null;
		try{
			in = new BufferedReader(new InputStreamReader(this.getClass().getClassLoader().getResourceAsStream("template.json")));
			StringBuffer buffer = new StringBuffer();
			String line = "";
			while ((line = in.readLine()) != null){
			    buffer.append(line);
			}
			json = buffer.toString();
		}finally{
			if(in != null) in.close();
			if(reader != null) reader.close();
		}
		return json;
	}
}
