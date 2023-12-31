package com.sumavision.tetris.capacity.bo.input;

/**
 * 视频参数<br/>
 * <b>作者:</b>wjw<br/>
 * <b>版本：</b>1.0<br/>
 * <b>日期：</b>2019年10月28日 下午3:37:06
 */
public class ProgramVideoBO {

	/** 视频pid 0~8191 */
	private Integer pid;
	
	/** 视频编码格式 mp1v/mp2v/mp4/h264/h265/cavs/cavs.plus/cavs2 */
	private String type;
	
	private Integer width;
	
	private Integer height;
	
	/** 帧率 */
	private String fps;
	
	/** 比特率 */
	private Integer bitrate;
	
	/** 解码方式 cpu/msdk/cuda */
	private String decode_mode;
	
	private Integer nv_card_idx;
	
	private String deinterlace_mode;
	
	private String backup_mode;
	
	private Integer cutoff_time;
	
	private String pattern_path;

	public Integer getPid() {
		return pid;
	}

	public ProgramVideoBO setPid(Integer pid) {
		this.pid = pid;
		return this;
	}

	public String getType() {
		return type;
	}

	public ProgramVideoBO setType(String type) {
		this.type = type;
		return this;
	}

	public Integer getWidth() {
		return width;
	}

	public ProgramVideoBO setWidth(Integer width) {
		this.width = width;
		return this;
	}

	public Integer getHeight() {
		return height;
	}

	public ProgramVideoBO setHeight(Integer height) {
		this.height = height;
		return this;
	}

	public String getFps() {
		return fps;
	}

	public ProgramVideoBO setFps(String fps) {
		this.fps = fps;
		return this;
	}

	public Integer getBitrate() {
		return bitrate;
	}

	public ProgramVideoBO setBitrate(Integer bitrate) {
		this.bitrate = bitrate;
		return this;
	}

	public String getDecode_mode() {
		return decode_mode;
	}

	public ProgramVideoBO setDecode_mode(String decode_mode) {
		this.decode_mode = decode_mode;
		return this;
	}

	public Integer getNv_card_idx() {
		return nv_card_idx;
	}

	public ProgramVideoBO setNv_card_idx(Integer nv_card_idx) {
		this.nv_card_idx = nv_card_idx;
		return this;
	}

	public String getDeinterlace_mode() {
		return deinterlace_mode;
	}

	public ProgramVideoBO setDeinterlace_mode(String deinterlace_mode) {
		this.deinterlace_mode = deinterlace_mode;
		return this;
	}

	public String getBackup_mode() {
		return backup_mode;
	}

	public ProgramVideoBO setBackup_mode(String backup_mode) {
		this.backup_mode = backup_mode;
		return this;
	}

	public Integer getCutoff_time() {
		return cutoff_time;
	}

	public ProgramVideoBO setCutoff_time(Integer cutoff_time) {
		this.cutoff_time = cutoff_time;
		return this;
	}

	public String getPattern_path() {
		return pattern_path;
	}

	public ProgramVideoBO setPattern_path(String pattern_path) {
		this.pattern_path = pattern_path;
		return this;
	}
}
