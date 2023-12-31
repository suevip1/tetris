package com.sumavision.tetris.mims.event.listener;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSON;
import com.sumavision.tetris.mims.event.UserImportFeign;
import com.sumavision.tetris.user.event.UserImportEvent;

/**
 * 用户导入事件监听<br/>
 * <b>作者:</b>lvdeyang<br/>
 * <b>版本：</b>1.0<br/>
 * <b>日期：</b>2019年1月25日 下午4:02:30
 */
/*@Service
@Transactional(rollbackFor = Exception.class)
public class UserImportListener  implements ApplicationListener<UserImportEvent> {

	@Autowired
	private UserImportFeign userImportFeign;

	*//**
	 * 用户导入事件监听<br/>
	 * <b>作者:</b>lvdeyang<br/>
	 * <b>版本：</b>1.0<br/>
	 * <b>日期：</b>2019年1月17日 下午1:05:03
	 *//*
	@Override
	public void onApplicationEvent(UserImportEvent event) {
		try {
			userImportFeign.userImport(JSON.toJSONString(event.getUsers()));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}*/
