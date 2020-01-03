package com.sumavision.tetris.omms.graph;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.sumavision.tetris.commons.util.wrapper.ArrayListWrapper;
import com.sumavision.tetris.commons.util.wrapper.HashMapWrapper;
import com.sumavision.tetris.mvc.ext.response.json.aop.annotation.JsonBody;
import com.sumavision.tetris.spring.eureka.application.ApplicationQuery;
import com.sumavision.tetris.spring.eureka.application.ApplicationStatus;
import com.sumavision.tetris.spring.eureka.application.ApplicationVO;

@Controller
@RequestMapping(value = "/graph")
public class GraphController {

	@Autowired
	private ApplicationQuery applicationQuery;
	
	/**
	 * 3d图表数据<br/>
	 * <b>作者:</b>lvdeyang<br/>
	 * <b>版本：</b>1.0<br/>
	 * <b>日期：</b>2019年12月26日 上午11:06:34
	 * @return isDanger boolean 是否告警
	 * @return levels 拓补图元数据 List<LevelVO>
	 */
	@JsonBody
	@ResponseBody
	@RequestMapping(value = "/data")
	public Object data(HttpServletRequest request) throws Exception{
		boolean isDanger = false;
		List<LevelVO> levels = new ArrayListWrapper<LevelVO>().add(new LevelVO().setId("level0").setName("第一层").setGroups(new ArrayList<GroupVO>()))
															  .add(new LevelVO().setId("level1").setName("第二层").setGroups(new ArrayList<GroupVO>()))
															  .add(new LevelVO().setId("level2").setName("第三层").setGroups(new ArrayList<GroupVO>()))
															  .getList();
		levels.get(0).getGroups().add(new GroupVO().set(GroupType.HEART));
		levels.get(1).getGroups().add(new GroupVO().set(GroupType.TETRIS));
		levels.get(2).getGroups().add(new GroupVO().set(GroupType.PROTOCOL_CONVERSION));
		List<TypeVO> loopTypes = new ArrayList<TypeVO>();
		for(int i=0; i<levels.size(); i++){
			for(int j=0; j<levels.get(i).getGroups().size(); j++){
				GroupVO group = levels.get(i).getGroups().get(j);
				ServerType[] serverTypes = ServerType.values();
				for(ServerType serverType:serverTypes){
					if(serverType.getGroupType().getId().equals(group.getId())){
						TypeVO type = new TypeVO().set(serverType);
						group.getTypes().add(type);
						loopTypes.add(type);
					}
				}
			}
		}
		List<ApplicationVO> applications = applicationQuery.findAll();
		ServerVO eureka = null;
		ServerVO capacity = null;
		if(applications!=null && applications.size()>0){
			for(ApplicationVO application:applications){
				if(!isDanger && application.getStatus().equals(ApplicationStatus.DOWN.toString())) isDanger = true;
				for(TypeVO type:loopTypes){
					if(type.getId().equals(application.getName())){
						ServerVO server = new ServerVO().setId(application.getInstanceId()).setIp(application.getIp()).setIcon(type.getServerType().getIcon()).setStatus(application.getStatus()).setRefs(new ArrayList<String>());
						type.getServers().add(server);
						if(type.getId().equals(ServerType.EUREKA.getId()) && type.getServers().size()==1 && application.getStatus().equals("UP")){
							eureka = server;
						}else if(type.getId().equals(ServerType.CAPACITY.getId()) && type.getServers().size()==1 && application.getStatus().equals("UP")){
							capacity = server;
						}
						break;
					}
				}
			}
		}
		if(eureka != null){
			GroupVO targetGroup = levels.get(1).getGroups().get(0);
			for(int i=0; i<targetGroup.getTypes().size(); i++){
				TypeVO type = targetGroup.getTypes().get(i);
				for(int j=0; j<type.getServers().size(); j++){
					eureka.getRefs().add(type.getServers().get(j).getId());
				}
			}
		}
		if(capacity != null){
			GroupVO targetGroup = levels.get(2).getGroups().get(0);
			for(int i=0; i<targetGroup.getTypes().size(); i++){
				TypeVO type = targetGroup.getTypes().get(i);
				for(int j=0; j<type.getServers().size(); j++){
					capacity.getRefs().add(type.getServers().get(j).getId());
				}
			}
		}
		return new HashMapWrapper<String, Object>().put("isDanger", isDanger)
												   .put("levels", levels)
												   .getMap();
	}
	
	/**
	 * 刷新服务状态<br/>
	 * <b>作者:</b>lvdeyang<br/>
	 * <b>版本：</b>1.0<br/>
	 * <b>日期：</b>2020年1月3日 上午11:51:26
	 * @return isDanger boolean 是否告警
	 * @return servers 服务列表 List<ServerVO>
	 */
	@JsonBody
	@ResponseBody
	@RequestMapping(value = "/refresh/status")
	public Object refreshStatus(HttpServletRequest request) throws Exception{
		boolean isDanger = false;
		List<ServerVO> servers = new ArrayList<ServerVO>();
		List<ApplicationVO> applications = applicationQuery.findAll();
		if(applications!=null && applications.size()>0){
			for(ApplicationVO application:applications){
				if(!isDanger && application.getStatus().equals(ApplicationStatus.DOWN.toString())) isDanger = true;
				ServerVO server = new ServerVO().setId(application.getInstanceId()).setStatus(application.getStatus());
				servers.add(server);
			}
		}
		return new HashMapWrapper<String, Object>().put("isDanger", isDanger)
												   .put("servers", servers)
												   .getMap();
	}
	
}
