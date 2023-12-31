package com.sumavision.tetris.mims.app.media.picture;

import com.sumavision.tetris.commons.util.date.DateUtil;
import com.sumavision.tetris.mvc.converter.AbstractBaseVO;

public class MediaPictureTaskVO  extends AbstractBaseVO<MediaPictureTaskVO, MediaPicturePO>{

	private Long lastModified;
	
	private String name;
	
	private Long size;
	
	private String mimetype;
	
	private String type;
	
	private String icon;
	
	private String style;
	
	private Long folderId;

	private String folderName;
	
	private String folderNamePath;
	
	private String uploadStatus;
	
	private String previewUrl;
	
	private Integer progress;
	
	public Long getLastModified() {
		return lastModified;
	}

	public MediaPictureTaskVO setLastModified(Long lastModified) {
		this.lastModified = lastModified;
		return this;
	}

	public String getName() {
		return name;
	}

	public MediaPictureTaskVO setName(String name) {
		this.name = name;
		return this;
	}

	public Long getSize() {
		return size;
	}

	public MediaPictureTaskVO setSize(Long size) {
		this.size = size;
		return this;
	}

	public String getMimetype() {
		return mimetype;
	}

	public MediaPictureTaskVO setMimetype(String mimetype) {
		this.mimetype = mimetype;
		return this;
	}

	public String getIcon() {
		return icon;
	}

	public MediaPictureTaskVO setIcon(String icon) {
		this.icon = icon;
		return this;
	}

	public String getStyle() {
		return style;
	}

	public MediaPictureTaskVO setStyle(String style) {
		this.style = style;
		return this;
	}

	public String getType() {
		return type;
	}

	public MediaPictureTaskVO setType(String type) {
		this.type = type;
		return this;
	}

	public Long getFolderId() {
		return folderId;
	}

	public MediaPictureTaskVO setFolderId(Long folderId) {
		this.folderId = folderId;
		return this;
	}

	public String getFolderName() {
		return folderName;
	}

	public MediaPictureTaskVO setFolderName(String folderName) {
		this.folderName = folderName;
		return this;
	}

	public String getFolderNamePath() {
		return folderNamePath;
	}

	public MediaPictureTaskVO setFolderNamePath(String folderNamePath) {
		this.folderNamePath = folderNamePath;
		return this;
	}

	public String getUploadStatus() {
		return uploadStatus;
	}

	public MediaPictureTaskVO setUploadStatus(String uploadStatus) {
		this.uploadStatus = uploadStatus;
		return this;
	}

	public String getPreviewUrl() {
		return previewUrl;
	}

	public MediaPictureTaskVO setPreviewUrl(String previewUrl) {
		this.previewUrl = previewUrl;
		return this;
	}
	
	public Integer getProgress() {
		return progress;
	}

	public MediaPictureTaskVO setProgress(Integer progress) {
		this.progress = progress;
		return this;
	}
	
	@Override
	public MediaPictureTaskVO set(MediaPicturePO entity) throws Exception {
		this.setId(entity.getId())
			.setUuid(entity.getUuid())
			.setUpdateTime(entity.getUpdateTime()==null?"":DateUtil.format(entity.getUpdateTime(), DateUtil.dateTimePattern))
			.setLastModified(entity.getLastModified())
			.setName(entity.getName())
			.setSize(entity.getSize())
			.setMimetype(entity.getMimetype())
			.setType(MediaPictureItemType.PICTURE.toString())
			.setIcon(MediaPictureItemType.PICTURE.getIcon())
			//.setStyle(entity.getType().getStyle()[1])
			.setStyle(MediaPictureItemType.PICTURE.getStyle()[0])
			.setFolderId(entity.getFolderId())
			.setUploadStatus(entity.getUploadStatus().toString())
			.setProgress(0);
		return this;
	}

}
