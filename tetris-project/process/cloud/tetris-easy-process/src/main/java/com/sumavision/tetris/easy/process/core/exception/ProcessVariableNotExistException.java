package com.sumavision.tetris.easy.process.core.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.sumavision.tetris.commons.exception.BaseException;
import com.sumavision.tetris.commons.exception.code.StatusCode;
import com.sumavision.tetris.commons.util.date.DateUtil;
import com.sumavision.tetris.commons.util.wrapper.StringBufferWrapper;

public class ProcessVariableNotExistException extends BaseException{

	private static final Logger LOG = LoggerFactory.getLogger(ProcessVariableNotExistException.class);
	
	private static final long serialVersionUID = 1L;

	public ProcessVariableNotExistException(Long id) {
		super(StatusCode.FORBIDDEN, "流程变量不存在！");
		LOG.error(DateUtil.now());
		LOG.error(new StringBufferWrapper().append("流程变量不存在！变量id：")
										   .append(id)
										   .toString());
	}
	
}
