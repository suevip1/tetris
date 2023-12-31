package com.sumavision.tetris.mims.app.media.audio.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sumavision.tetris.commons.exception.BaseException;
import com.sumavision.tetris.commons.exception.code.StatusCode;
import com.sumavision.tetris.commons.util.date.DateUtil;
import com.sumavision.tetris.commons.util.wrapper.StringBufferWrapper;

public class MediaAudioCannotMatchException extends BaseException{
	private final Logger LOG = LoggerFactory.getLogger(MediaAudioCannotMatchException.class);

	private static final long serialVersionUID = 1L;

	public MediaAudioCannotMatchException(
			String uuid, 
			String pName, 
			long pLastModified,
			long pSize,
			String pType,
			String name, 
			long lastModified,
			long size,
			String type) {
		super(StatusCode.FORBIDDEN, "上传文件不匹配！");
		LOG.error(DateUtil.now());
		LOG.error(new StringBufferWrapper().append("任务:")
										   .append(uuid)
										   .append(", 参数: name=").append(pName)
										   .append(", lastModified=").append(pLastModified)
										   .append(", size=").append(pSize)
										   .append(", type=").append(pType)
										   .append("; 数据: name=").append(name)
										   .append(", lastModified=").append(lastModified)
										   .append(", size=").append(size)
										   .append(", type=").append(type)
										   .toString());
	}
}
