package com.sumavision.tetris.mims.app.media.stream.audio;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.sumavision.tetris.commons.util.date.DateUtil;
import com.sumavision.tetris.commons.util.wrapper.ArrayListWrapper;
import com.sumavision.tetris.commons.util.wrapper.HashMapWrapper;
import com.sumavision.tetris.commons.util.wrapper.StringBufferWrapper;
import com.sumavision.tetris.mims.app.folder.FolderBreadCrumbVO;
import com.sumavision.tetris.mims.app.folder.FolderDAO;
import com.sumavision.tetris.mims.app.folder.FolderPO;
import com.sumavision.tetris.mims.app.folder.FolderQuery;
import com.sumavision.tetris.mims.app.folder.FolderType;
import com.sumavision.tetris.mims.app.folder.exception.FolderNotExistException;
import com.sumavision.tetris.mims.app.media.ReviewStatus;
import com.sumavision.tetris.mims.app.media.UploadStatus;
import com.sumavision.tetris.mims.app.media.audio.MediaAudioItemType;
import com.sumavision.tetris.mims.app.media.tag.TagDAO;
import com.sumavision.tetris.mims.app.media.tag.TagPO;
import com.sumavision.tetris.user.UserQuery;
import com.sumavision.tetris.user.UserVO;

/**
 * 视频流流媒资查询操作<br/>
 * <b>作者:</b>lvdeyang<br/>
 * <b>版本：</b>1.0<br/>
 * <b>日期：</b>2019年1月28日 上午10:38:08
 */
@Component
public class MediaAudioStreamQuery {

	@Autowired
	private MediaAudioStreamDAO mediaAudioStreamDao;
	
	@Autowired
	private TagDAO tagDAO;
	
	@Autowired
	private UserQuery userQuery;
	
	@Autowired
	private FolderDAO folderDao;
	
	@Autowired
	private FolderQuery folderQuery;
	
	/**
	 * 根据文件夹id查询文件夹以及音频流媒资<br/>
	 * <p>
	 * 	-如果folderId是0：查询有权限的根目录，只返回目录列表
	 * 	-如果folderId不是0：查询当前文件夹下有权限的目录以及目录下所有的媒资
	 * </p>
	 * <b>作者:</b>lvdeyang<br/>
	 * <b>版本：</b>1.0<br/>
	 * <b>日期：</b>2019年2月26日 下午5:14:37
	 * @param UserVO user 用户
	 * @param Long folderId 当前文件夹id
	 * @return rows List<MediaAudioStreamVO> 媒资项目列表
	 * @return breadCrumb FolderBreadCrumbVO 面包屑数据
	 */
	public Map<String, Object> load(Long folderId) throws Exception{

		UserVO user = userQuery.current();
		
		List<MediaAudioStreamVO> rows = null;
		
		//处理根面包屑
		FolderBreadCrumbVO breadCrumb = new FolderBreadCrumbVO().setId(0l)
																.setUuid("0")
																.setName("根目录")
																.setType(FolderType.COMPANY_AUDIO_STREAM.toString());
		
		if(user.getBusinessRoles() == null){
			return new HashMapWrapper<String, Object>().put("rows", rows).put("breadCrumb", breadCrumb).getMap();
		}
		
		if(folderId.equals(0l)){
			List<FolderPO> folders = folderQuery.findPermissionCompanyTree(FolderType.COMPANY_AUDIO_STREAM.toString());
			if(folders==null || folders.size()<=0){
				return new HashMapWrapper<String, Object>().put("rows", rows).put("breadCrumb", breadCrumb).getMap();
			}
			List<FolderPO> rootFolders = folderQuery.findRoots(folders);
			rows = new ArrayList<MediaAudioStreamVO>();
			for(FolderPO folder:rootFolders){
				MediaAudioStreamVO row = new MediaAudioStreamVO().set(folder);
				rows.add(row);
			}
			return new HashMapWrapper<String, Object>().put("rows", rows).put("breadCrumb", breadCrumb).getMap();
		}else{
			FolderPO current = folderDao.findOne(folderId);
			if(current == null) throw new FolderNotExistException(folderId);
			
			rows = new ArrayList<MediaAudioStreamVO>();
			
			//子文件夹
			List<FolderPO> folders = folderQuery.findPermissionCompanyFolderByParentIdOrderByNameAsc(current.getId());
			if(folders!=null && folders.size()>0){
				for(FolderPO folder:folders){
					MediaAudioStreamVO row = new MediaAudioStreamVO().set(folder);
					rows.add(row);
				}
			}
			
			//文件夹内音频
			List<MediaAudioStreamPO> audioStreams = mediaAudioStreamDao.findByFolderIdInAndUploadStatusAndReviewStatusNotInOrAuthorId(
					new ArrayListWrapper<Long>().add(current.getId()).getList(), 
					UploadStatus.COMPLETE.toString(), 
					new ArrayListWrapper<String>().add(ReviewStatus.REVIEW_UPLOAD_WAITING.toString()).add(ReviewStatus.REVIEW_UPLOAD_REFUSE.toString()).getList(),
					user.getId().toString());
			if(audioStreams!=null && audioStreams.size()>0){
				for(MediaAudioStreamPO audioStream:audioStreams){
					rows.add(new MediaAudioStreamVO().set(audioStream));
				}
			}
			
			FolderBreadCrumbVO subBreadCrumb = null;
			if(current.getParentPath() == null){
				subBreadCrumb = folderQuery.generateFolderBreadCrumb(new ArrayListWrapper<FolderPO>().add(current).getList());
			}else{
				List<Long> parentIds = JSON.parseArray(new StringBufferWrapper().append("[")
																			    .append(current.getParentPath().substring(1, current.getParentPath().length()).replaceAll("/", ","))
																			    .append("]")
																			    .toString(), Long.class);
				List<FolderPO> breadCrumbFolders = folderQuery.findPermissionCompanyFolderByIdIn(parentIds, FolderType.COMPANY_AUDIO_STREAM.toString());
				if(breadCrumbFolders == null){
					breadCrumbFolders = new ArrayList<FolderPO>();
				}
				breadCrumbFolders.add(current);
				subBreadCrumb = folderQuery.generateFolderBreadCrumb(breadCrumbFolders);
			}
			breadCrumb.setNext(subBreadCrumb);
			return new HashMapWrapper<String, Object>().put("rows", rows).put("breadCrumb", breadCrumb).getMap();
		}
	}
	
	/**
	 * 加载所有的音频流媒资<br/>
	 * <b>作者:</b>lzp<br/>
	 * <b>版本：</b>1.0<br/>
	 * <b>日期：</b>2019年12月11日 上午11:24:24
	 * @return List<MediaAudioStreamVO> 音频流媒资列表
	 */
	public List<MediaAudioStreamVO> loadAll(Long ... id) throws Exception{
		
		//TODO 权限校验		
		List<FolderPO> folderTree = folderQuery.findPermissionCompanyTree(FolderType.COMPANY_AUDIO_STREAM.toString());
		if (id != null && id.length > 0) {
			folderTree = folderQuery.findSubFolders(id[0]);
			folderTree.add(folderDao.findOne(id[0]));
		}
		
		if (folderTree.isEmpty()) return new ArrayList<MediaAudioStreamVO>();
		
		List<Long> folderIds = new ArrayList<Long>();
		for(FolderPO folderPO: folderTree){
			folderIds.add(folderPO.getId());
		}
		
		List<MediaAudioStreamPO> videos = mediaAudioStreamDao.findByFolderIdIn(folderIds, new ArrayListWrapper<String>().add(ReviewStatus.REVIEW_UPLOAD_WAITING.toString()).add(ReviewStatus.REVIEW_UPLOAD_REFUSE.toString()).getList());
		
		List<FolderPO> roots = folderQuery.findRoots(folderTree);
		List<MediaAudioStreamVO> medias = new ArrayList<MediaAudioStreamVO>();
		for(FolderPO root:roots){
			medias.add(new MediaAudioStreamVO().set(root));
		}
		
		packMediaAudioStreamTree(medias, folderTree, videos);
		
		return id != null && id.length > 0 ? medias.get(0).getChildren() : medias;
	}
	
	/**
	 * 生成媒资树<br/>
	 * <b>作者:</b>lzp<br/>
	 * <b>版本：</b>1.0<br/>
	 * <b>日期：</b>2019年3月31日 上午11:29:34
	 * @param roots 根
	 * @param folders 所有文件夹
	 * @param medias 所有视频媒资
	 */
	public void packMediaAudioStreamTree(List<MediaAudioStreamVO> roots, List<FolderPO> folders, List<MediaAudioStreamPO> medias) throws Exception{
		if(roots == null || roots.size() <= 0){
			return;
		}
		
		for(MediaAudioStreamVO root: roots){
			if(root.getType().equals(MediaAudioItemType.FOLDER.toString())){
				if(root.getChildren() == null) root.setChildren(new ArrayList<MediaAudioStreamVO>());
				for(FolderPO folder: folders){
					if(folder.getParentId() != null && folder.getParentId().equals(root.getId())){
						root.getChildren().add(new MediaAudioStreamVO().set(folder));
					}
				}
				for(MediaAudioStreamPO media: medias){
					if(media.getFolderId() != null && media.getFolderId().equals(root.getId())){
						root.getChildren().add(new MediaAudioStreamVO().set(media));
					}
				}
				if(root.getChildren().size() > 0){
					packMediaAudioStreamTree(root.getChildren(), folders, medias);
				}
			}
		}
	}
	
	/**
	 * 根据id查询<br/>
	 * <b>作者:</b>lzp<br/>
	 * <b>版本：</b>1.0<br/>
	 * <b>日期：</b>2019年12月11日 下午1:48:42
	 * @param Long id 音频流媒资id
	 * @return MediaAudioStreamVO 音频流媒资信息
	 */
	public MediaAudioStreamVO findById(Long id) throws Exception {
		MediaAudioStreamPO videoStreamPO = mediaAudioStreamDao.findOne(id);
		
		if (videoStreamPO == null) return null;
		
		return new MediaAudioStreamVO().set(videoStreamPO);
	}
	
	/**
	 * 根据创建时间筛选<br/>
	 * <b>作者:</b>lzp<br/>
	 * <b>版本：</b>1.0<br/>
	 * <b>日期：</b>2019年9月5日 下午3:08:38
	 * @param Long startTime 筛选起始时间
	 * @param Long endTime 筛选终止时间
	 * @return List<MediaAudioStreamVO> 筛选结果
	 */
	public List<MediaAudioStreamVO> loadByCreateTime(Long startTime, Long endTime) throws Exception{
		return loadByCondition(null, null, DateUtil.format(DateUtil.getDateByMillisecond(startTime), DateUtil.dateTimePattern), DateUtil.format(DateUtil.getDateByMillisecond(endTime), DateUtil.dateTimePattern), null);
	}
	
	/**
	 * 根据条件查询媒资<br/>
	 * <b>作者:</b>lzp<br/>
	 * <b>版本：</b>1.0<br/>
	 * <b>日期：</b>2019年9月19日 下午3:17:30
	 * @param Long id 媒资id
	 * @param String name 名称(模糊匹配)
	 * @param String startTime updateTime起始查询
	 * @param Stinrg endTime updateTime终止查询
	 * @param Long tagId 标签id
	 * @return List<MediaTxtVO> 查询结果
	 */
	public List<MediaAudioStreamVO> loadByCondition(Long id, String name, String startTime, String endTime, Long tagId) throws Exception{
		UserVO user = userQuery.current();
		
		//TODO 权限校验		
		List<FolderPO> folderTree = folderQuery.findPermissionCompanyTree(FolderType.COMPANY_AUDIO_STREAM.toString());
		
		List<Long> folderIds = new ArrayList<Long>();
		for(FolderPO folderPO: folderTree){
			folderIds.add(folderPO.getId());
		}
		
		TagPO tag = tagDAO.findByIdAndGroupId(tagId, user.getGroupId());
		String tagName = tag != null ? tag.getName() : null;
		
		List<MediaAudioStreamPO> medias = mediaAudioStreamDao.findByCondition(id, name, startTime, endTime, tagName, folderIds, new ArrayListWrapper<String>().add(ReviewStatus.REVIEW_UPLOAD_WAITING.toString()).add(ReviewStatus.REVIEW_UPLOAD_REFUSE.toString()).getList());
		
		return MediaAudioStreamVO.getConverter(MediaAudioStreamVO.class).convert(medias, MediaAudioStreamVO.class);
	}
	
	/**
	 * 查询文件夹下上传完成的视频流媒资<br/>
	 * <b>作者:</b>lvdeyang<br/>
	 * <b>版本：</b>1.0<br/>
	 * <b>日期：</b>2019年1月28日 上午10:38:53
	 * @param Long folderId 文件夹id
	 * @return List<MediaPicturePO> 视频流媒资
	 */
	public List<MediaAudioStreamPO> findCompleteByFolderId(Long folderId){
		return mediaAudioStreamDao.findByFolderIdAndUploadStatusOrderByName(folderId, UploadStatus.COMPLETE);
	}
	
	/**
	 * 查询文件夹下上传完成的视频流媒资（批量）<br/>
	 * <b>作者:</b>lvdeyang<br/>
	 * <b>版本：</b>1.0<br/>
	 * <b>日期：</b>2019年1月29日 下午3:36:37
	 * @param Collection<Long> folderIds 文件夹id列表
	 * @return List<MediaPicturePO> 视频流媒资
	 */
	public List<MediaAudioStreamPO> findCompleteByFolderIds(Collection<Long> folderIds){
		return mediaAudioStreamDao.findByFolderIdInAndUploadStatus(folderIds, UploadStatus.COMPLETE);
	}
	
	/**
	 * 获取文件夹（多个）下的视频流媒资上传任务（上传未完成的）<br/>
	 * <b>作者:</b>lvdeyang<br/>
	 * <b>版本：</b>1.0<br/>
	 * <b>日期：</b>2018年11月29日 下午1:25:31
	 * @param Collection<Long> folderIds 文件夹id列表
	 * @return List<MediaPicturePO> 上传任务列表
	 */
	public List<MediaAudioStreamPO> findTasksByFolderIds(Collection<Long> folderIds){
		return mediaAudioStreamDao.findByFolderIdInAndUploadStatus(folderIds, UploadStatus.UPLOADING);
	}
	
	/**
	 * 根据uuid查找媒资视频流（内存循环）<br/>
	 * <b>作者:</b>lvdeyang<br/>
	 * <b>版本：</b>1.0<br/>
	 * <b>日期：</b>2018年11月26日 上午11:52:58
	 * @param String uuid 图片uuid
	 * @param Collection<MediaAudioStreamPO> audioStreams 查找范围
	 * @return MediaAudioStreamPO 查找结果
	 */
	public MediaAudioStreamPO loopForUuid(String uuid, Collection<MediaAudioStreamPO> audioStreams){
		if(audioStreams==null || audioStreams.size()<=0) return null;
		for(MediaAudioStreamPO audioStream:audioStreams){
			if(audioStream.getUuid().equals(uuid)){
				return audioStream;
			}
		}
		return null;
	}
	
	/**
	 * 根据预览地址查询音频流<br/>
	 * <b>作者:</b>lvdeyang<br/>
	 * <b>版本：</b>1.0<br/>
	 * <b>日期：</b>2019年11月5日 上午11:15:05
	 * @param Collection<String> previewUrls 预览地址列表
	 * @return List<MediaAudioStreamVO> 音频流列表
	 */
	public List<MediaAudioStreamVO> findByPreviewUrlIn(Collection<String> previewUrls) throws Exception{
		List<MediaAudioStreamPO> entities = mediaAudioStreamDao.findByPreviewUrlIn(previewUrls);
		return MediaAudioStreamVO.getConverter(MediaAudioStreamVO.class).convert(entities, MediaAudioStreamVO.class);
	}
	
}
