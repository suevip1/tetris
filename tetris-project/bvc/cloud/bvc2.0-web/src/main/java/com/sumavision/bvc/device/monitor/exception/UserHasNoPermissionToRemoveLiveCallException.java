package com.sumavision.bvc.device.monitor.exception;

import com.sumavision.tetris.commons.exception.BaseException;
import com.sumavision.tetris.commons.exception.code.StatusCode;
import com.sumavision.tetris.commons.util.wrapper.StringBufferWrapper;

public class UserHasNoPermissionToRemoveLiveCallException extends BaseException{

	private static final long serialVersionUID = 1L;

	public UserHasNoPermissionToRemoveLiveCallException(Long userId, Long liveId) {
		super(StatusCode.FORBIDDEN, new StringBufferWrapper().append("您没有权限删除此呼叫用户任务！用户id：")
															 .append(userId)
															 .append("，任务id：")
															 .append(liveId)
															 .toString());
	}
	
	public UserHasNoPermissionToRemoveLiveCallException(Long userId, String liveUuid) {
		super(StatusCode.FORBIDDEN, new StringBufferWrapper().append("您没有权限删除此呼叫用户任务！用户id：")
															 .append(userId)
															 .append("，任务uuid：")
															 .append(liveUuid)
															 .toString());
	}

}
