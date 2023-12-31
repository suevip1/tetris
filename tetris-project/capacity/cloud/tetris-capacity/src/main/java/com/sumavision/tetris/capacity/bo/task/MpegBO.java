package com.sumavision.tetris.capacity.bo.task;

/**
 * mpeg编码参数<br/>
 * <b>作者:</b>wjw<br/>
 * <b>版本：</b>1.0<br/>
 * <b>日期：</b>2019年11月7日 下午4:00:45
 */
public class MpegBO extends BaseAudioEncodeBO<MpegBO>{
	
	private String sample_fmt = "s16";

	public String getSample_fmt() {
		return sample_fmt;
	}

	public MpegBO setSample_fmt(String sample_fmt) {
		this.sample_fmt = sample_fmt;
		return this;
	}
	
	public MpegBO setMpeg(){
		this.setBitrate(192000)
			.setSample_rate(48000)
			.setSample_byte(2)
			.setSample_fmt("s16")
			.setChannel_layout("mono");
		return this;
	}
	
}
