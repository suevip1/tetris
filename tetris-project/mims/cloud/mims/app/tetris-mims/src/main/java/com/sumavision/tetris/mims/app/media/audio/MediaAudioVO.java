package com.sumavision.tetris.mims.app.media.audio;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import com.alibaba.fastjson.JSONObject;
import com.sumavision.tetris.commons.context.SpringContext;
import com.sumavision.tetris.commons.util.date.DateUtil;
import com.sumavision.tetris.commons.util.wrapper.StringBufferWrapper;
import com.sumavision.tetris.mims.app.folder.FolderPO;
import com.sumavision.tetris.mims.app.folder.FolderType;
import com.sumavision.tetris.mims.app.media.StoreType;
import com.sumavision.tetris.mims.app.media.editor.MediaFileEditorDAO;
import com.sumavision.tetris.mims.app.media.editor.MediaFileEditorPO;
import com.sumavision.tetris.mims.app.media.editor.MediaFileEditorVO;
import com.sumavision.tetris.mims.config.server.ServerProps;
import com.sumavision.tetris.mvc.converter.AbstractBaseVO;

public class MediaAudioVO extends AbstractBaseVO<MediaAudioVO, MediaAudioPO>{

	private String name;
	
	private String authorName;
	
	private String size;
	
	private String createTime;
	
	private String duration;
	
	private String version;
	
	private String remarks;
	
	private StoreType storeType;
	
	private String uploadTmpPath;
	
	private Long folderId;
	
	private List<String> tags;
	
	private List<String> keyWords;
	
	private String thumbnail;
	
	private Long downloadCount;
	
	/** 计算热门权重 */
	private Long hotWeight;
	
	private String type;
	
	private String resourceType;
	
	private boolean removeable;
	
	private String icon;
	
	private String style;
	
	private String mimetype;
	
	private Integer progress;
	
	private String previewUrl;
	
	private String reviewStatus;
	
	private String processInstanceId;
	
	private Boolean encryption;
	
	private String encryptionUrl;
	
	private String addition;
	
	private MediaFileEditorVO editorInfo;
	
	private JSONObject tagToHotWeight;
	
	private List<MediaAudioVO> children;
	
	public String getName() {
		return name;
	}

	public MediaAudioVO setName(String name) {
		this.name = name;
		return this;
	}

	public String getAuthorName() {
		return authorName;
	}

	public MediaAudioVO setAuthorName(String authorName) {
		this.authorName = authorName;
		return this;
	}

	public String getSize() {
		return size;
	}

	public MediaAudioVO setSize(String size) {
		this.size = size;
		return this;
	}

	public String getCreateTime() {
		return createTime;
	}

	public MediaAudioVO setCreateTime(String createTime) {
		this.createTime = createTime;
		return this;
	}

	public String getDuration() {
		return duration;
	}

	public MediaAudioVO setDuration(String duration) {
		this.duration = duration;
		return this;
	}

	public String getVersion() {
		return version;
	}

	public MediaAudioVO setVersion(String version) {
		this.version = version;
		return this;
	}

	public String getRemarks() {
		return remarks;
	}

	public MediaAudioVO setRemarks(String remarks) {
		this.remarks = remarks;
		return this;
	}

	public StoreType getStoreType() {
		return storeType;
	}

	public MediaAudioVO setStoreType(StoreType storeType) {
		this.storeType = storeType;
		return this;
	}

	public String getUploadTmpPath() {
		return uploadTmpPath;
	}

	public MediaAudioVO setUploadTmpPath(String uploadTmpPath) {
		this.uploadTmpPath = uploadTmpPath;
		return this;
	}

	public Long getFolderId() {
		return folderId;
	}

	public MediaAudioVO setFolderId(Long folderId) {
		this.folderId = folderId;
		return this;
	}

	public List<String> getTags() {
		return tags;
	}

	public MediaAudioVO setTags(List<String> tags) {
		this.tags = tags;
		return this;
	}

	public List<String> getKeyWords() {
		return keyWords;
	}

	public MediaAudioVO setKeyWords(List<String> keyWords) {
		this.keyWords = keyWords;
		return this;
	}
	
	public String getThumbnail() {
		return thumbnail;
	}

	public MediaAudioVO setThumbnail(String thumbnail) {
		this.thumbnail = thumbnail;
		return this;
	}

	public Long getDownloadCount() {
		return downloadCount;
	}

	public MediaAudioVO setDownloadCount(Long downloadCount) {
		this.downloadCount = downloadCount;
		return this;
	}

	public Long getHotWeight() {
		return hotWeight;
	}

	public MediaAudioVO setHotWeight(Long hotWeight) {
		this.hotWeight = hotWeight;
		return this;
	}

	public String getType() {
		return type;
	}

	public MediaAudioVO setType(String type) {
		this.type = type;
		return this;
	}
	
	public String getResourceType() {
		return resourceType;
	}

	public MediaAudioVO setResourceType(String resourceType) {
		this.resourceType = resourceType;
		return this;
	}

	public boolean isRemoveable() {
		return removeable;
	}

	public MediaAudioVO setRemoveable(boolean removeable) {
		this.removeable = removeable;
		return this;
	}

	public String getIcon() {
		return icon;
	}

	public MediaAudioVO setIcon(String icon) {
		this.icon = icon;
		return this;
	}

	public String getStyle() {
		return style;
	}

	public MediaAudioVO setStyle(String style) {
		this.style = style;
		return this;
	}
	
	public String getMimetype() {
		return mimetype;
	}

	public MediaAudioVO setMimetype(String mimetype) {
		this.mimetype = mimetype;
		return this;
	}

	public Integer getProgress() {
		return progress;
	}

	public MediaAudioVO setProgress(Integer progress) {
		this.progress = progress;
		return this;
	}
	
	public String getPreviewUrl() {
		return previewUrl;
	}

	public MediaAudioVO setPreviewUrl(String previewUrl) {
		this.previewUrl = previewUrl;
		return this;
	}
	
	public String getReviewStatus() {
		return reviewStatus;
	}

	public MediaAudioVO setReviewStatus(String reviewStatus) {
		this.reviewStatus = reviewStatus;
		return this;
	}

	public String getProcessInstanceId() {
		return processInstanceId;
	}

	public MediaAudioVO setProcessInstanceId(String processInstanceId) {
		this.processInstanceId = processInstanceId;
		return this;
	}

	public Boolean getEncryption() {
		return encryption;
	}

	public MediaAudioVO setEncryption(Boolean encryption) {
		this.encryption = encryption;
		return this;
	}

	public String getEncryptionUrl() {
		return encryptionUrl;
	}

	public MediaAudioVO setEncryptionUrl(String encryptionUrl) {
		this.encryptionUrl = encryptionUrl;
		return this;
	}

	public String getAddition() {
		return addition;
	}

	public MediaAudioVO setAddition(String addition) {
		this.addition = addition;
		return this;
	}

	public MediaFileEditorVO getEditorInfo() {
		return editorInfo;
	}

	public MediaAudioVO setEditorInfo(MediaFileEditorVO editorInfo) {
		this.editorInfo = editorInfo;
		return this;
	}

	public JSONObject getTagToHotWeight() {
		return tagToHotWeight;
	}

	public MediaAudioVO setTagToHotWeight(JSONObject tagToHotWeight) {
		this.tagToHotWeight = tagToHotWeight;
		return this;
	}

	public List<MediaAudioVO> getChildren() {
		return children;
	}

	public MediaAudioVO setChildren(List<MediaAudioVO> children) {
		this.children = children;
		return this;
	}

	@Override
	public MediaAudioVO set(MediaAudioPO entity) throws Exception {
		ServerProps serverProps = SpringContext.getBean(ServerProps.class);
		MediaFileEditorDAO mediaFileEditorDAO = SpringContext.getBean(MediaFileEditorDAO.class);
		MediaFileEditorPO mediaEditorPO = mediaFileEditorDAO.findByMediaIdAndMediaType(entity.getId(), FolderType.COMPANY_AUDIO);
		this.setId(entity.getId())
			.setUuid(entity.getUuid())
			.setUpdateTime(entity.getUpdateTime()==null?"":DateUtil.format(entity.getUpdateTime(), DateUtil.dateTimePattern))
			.setName(entity.getName())
			.setAuthorName(entity.getAuthorName())
			.setSize(entity.getSize() != null ? entity.getSize().toString() : "-")
			.setCreateTime(entity.getCreateTime()==null?"":DateUtil.format(entity.getCreateTime(), DateUtil.dateTimePattern))
			.setDuration(entity.getDuration()==null?"-":entity.getDuration().toString())
			.setVersion(entity.getVersion())
			.setFolderId(entity.getFolderId())
			.setThumbnail(entity.getThumbnail())
			.setRemarks(entity.getRemarks())
			.setType(MediaAudioItemType.AUDIO.toString())
			.setRemoveable(true)
			.setIcon(MediaAudioItemType.AUDIO.getIcon())
			.setStyle(MediaAudioItemType.AUDIO.getStyle()[0])
			.setMimetype(entity.getMimetype())
			.setStoreType(entity.getStoreType())
			.setUploadTmpPath(entity.getUploadTmpPath())
			.setDownloadCount(entity.getDownloadCount())
			.setProgress(0)
			.setEncryption(entity.getEncryption() != null && entity.getEncryption() ? true : false)
			.setPreviewUrl((entity.getStoreType() == StoreType.REMOTE) ? entity.getPreviewUrl() : new StringBufferWrapper().append("http://").append(serverProps.getIp()).append(":").append(serverProps.getPort()).append("/").append(entity.getPreviewUrl()).toString())
			.setReviewStatus(entity.getReviewStatus()==null?"":entity.getReviewStatus().getName())
			.setProcessInstanceId(entity.getProcessInstanceId())
			.setAddition(entity.getAddition())
			.setTags(entity.getTags() != null && !entity.getTags().isEmpty() ? Arrays.asList(entity.getTags().split(MediaAudioPO.SEPARATOR_TAG)) : new ArrayList<String>());
		if(entity.getKeyWords() != null) this.setKeyWords(Arrays.asList(entity.getKeyWords().split(MediaAudioPO.SEPARATOR_KEYWORDS)));
		if(mediaEditorPO != null) this.setEditorInfo(new MediaFileEditorVO().set(mediaEditorPO));
		return this;
	}
	
	public MediaAudioVO set(FolderPO entity) throws Exception {
		this.setId(entity.getId())
			.setUuid(entity.getUuid())
			.setUpdateTime(entity.getUpdateTime()==null?"":DateUtil.format(entity.getUpdateTime(), DateUtil.dateTimePattern))
			.setName(entity.getName())
			.setAuthorName(entity.getAuthorName())
			.setSize("-")
			.setCreateTime(entity.getUpdateTime()==null?"":DateUtil.format(entity.getUpdateTime(), DateUtil.dateTimePattern))
			.setDuration("-")
			.setVersion("-")
			.setRemarks("-")
			.setType(MediaAudioItemType.FOLDER.toString())
			.setResourceType(entity.getType().toString())
			.setRemoveable(entity.getDepth().intValue()==2?false:true)
			.setIcon(MediaAudioItemType.FOLDER.getIcon())
			.setStyle(MediaAudioItemType.FOLDER.getStyle()[0])
			.setReviewStatus("-");
		return this;
	}
	
	@Override
	public boolean equals(Object obj) {
		MediaAudioVO vo=(MediaAudioVO)obj;
		return this.getId().equals(vo.getId());
	}
	
	/**
	 * 排序(根据热度权重从大到小)
	 * @author lzp
	 *
	 */
	public static final class MediaAudioHotOrderComparator implements Comparator<MediaAudioVO>{
		@Override
		public int compare(MediaAudioVO o1, MediaAudioVO o2) {
			if(o1.getHotWeight() > o2.getHotWeight()){
				return -1;
			}
			if(o1.getHotWeight() == o2.getHotWeight()){
				return 0;
			}
			return 1;
		}
	}
}
