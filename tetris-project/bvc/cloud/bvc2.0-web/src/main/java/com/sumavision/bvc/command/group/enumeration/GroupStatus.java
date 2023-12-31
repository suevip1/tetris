package com.sumavision.bvc.command.group.enumeration;

import com.sumavision.tetris.orm.exception.ErrorTypeException;

/**
 * @ClassName: 指挥组状态 
 * @author zsy
 * @date 2019年9月20日 下午1:22:00
 */
public enum GroupStatus {

	START("已开始", "start"),
	REMIND("提醒中", "remind"),//相当于START的子状态，需要限制只能在START时切换到REMIND
	PAUSE("暂停", "pause"),
	STOP("已停止", "stop");
	
	private String name;
	
	private String code;
	
	private GroupStatus(String name, String code){
		this.name = name;
		this.code = code;
	}
	
	public String getName(){
		return this.name;
	}
	
	public String getCode(){
		return this.code;
	}
	
	public static GroupStatus fromName(String name) throws Exception{
		GroupStatus[] values = GroupStatus.values();
		for(GroupStatus value:values){
			if(value.getName().equals(name)){
				return value;
			}
		}
		throw new ErrorTypeException("name", name);
	}
	
}
