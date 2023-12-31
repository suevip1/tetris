package com.sumavision.tetris.user.exception;

import com.sumavision.tetris.commons.exception.BaseException;
import com.sumavision.tetris.commons.exception.code.StatusCode;

public class UserColumnLengthException extends BaseException{

	private static final long serialVersionUID = 1L;

	public UserColumnLengthException(String message) {
		super(StatusCode.FORBIDDEN, message);
	}

}
