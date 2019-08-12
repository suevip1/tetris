package com.sumavision.tetris.mims.app.media.audio;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
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
import com.sumavision.tetris.mims.app.media.video.MediaVideoItemType;
import com.sumavision.tetris.mims.app.media.video.MediaVideoPO;
import com.sumavision.tetris.mvc.listener.ServletContextListener.Path;
import com.sumavision.tetris.user.UserQuery;
import com.sumavision.tetris.user.UserVO;

/**
 * 音频媒资查询操作<br/>
 * <b>作者:</b>lvdeyang<br/>
 * <b>版本：</b>1.0<br/>
 * <b>日期：</b>2019年1月28日 上午10:38:08
 */
@Component
public class MediaAudioQuery {

	@Autowired
	private MediaAudioDAO mediaAudioDao;
	
	@Autowired
	private UserQuery userQuery;
	
	@Autowired
	private FolderDAO folderDao;
	
	@Autowired
	private FolderQuery folderQuery;
	
	@Autowired
	private Path path;
	
	/**
	 * 根据文件夹id查询文件夹以及音频媒资<br/>
	 * <p>
	 * 	-如果folderId是0：查询有权限的根目录，只返回目录列表
	 * 	-如果folderId不是0：查询当前文件夹下有权限的目录以及目录下所有的媒资
	 * </p>
	 * <b>作者:</b>lvdeyang<br/>
	 * <b>版本：</b>1.0<br/>
	 * <b>日期：</b>2019年2月26日 下午5:14:37
	 * @param UserVO user 用户
	 * @param Long folderId 当前文件夹id
	 * @return rows List<MediaAudioVO> 媒资项目列表
	 * @return breadCrumb FolderBreadCrumbVO 面包屑数据
	 */
	public Map<String, Object> load(Long folderId) throws Exception{
		
		UserVO user = userQuery.current();
		
		List<MediaAudioVO> rows = null;
		
		//处理根面包屑
		FolderBreadCrumbVO breadCrumb = new FolderBreadCrumbVO().setId(0l)
																.setUuid("0")
																.setName("根目录")
																.setType(FolderType.COMPANY_AUDIO.toString());
		
		if(user.getBusinessRoles() == null){
			return new HashMapWrapper<String, Object>().put("rows", rows).put("breadCrumb", breadCrumb).getMap();
		}
		
		if(folderId.equals(0l)){
			List<FolderPO> folders = folderQuery.findPermissionCompanyTree(FolderType.COMPANY_AUDIO.toString());
			if(folders==null || folders.size()<=0){
				return new HashMapWrapper<String, Object>().put("rows", rows).put("breadCrumb", breadCrumb).getMap();
			}
			List<FolderPO> rootFolders = folderQuery.findRoots(folders);
			rows = new ArrayList<MediaAudioVO>();
			for(FolderPO folder:rootFolders){
				MediaAudioVO row = new MediaAudioVO().set(folder);
				rows.add(row);
			}
			return new HashMapWrapper<String, Object>().put("rows", rows).put("breadCrumb", breadCrumb).getMap();
		}else{
			FolderPO current = folderDao.findOne(folderId);
			if(current == null) throw new FolderNotExistException(folderId);
			
			rows = new ArrayList<MediaAudioVO>();
			
			//子文件夹
			List<FolderPO> folders = folderQuery.findPermissionCompanyFolderByParentIdOrderByNameAsc(current.getId());
			if(folders!=null && folders.size()>0){
				for(FolderPO folder:folders){
					MediaAudioVO row = new MediaAudioVO().set(folder);
					rows.add(row);
				}
			}
			
			//文件夹内音频
			List<MediaAudioPO> audios = mediaAudioDao.findByFolderIdInAndUploadStatusAndReviewStatusNotInOrAuthorId(
					new ArrayListWrapper<Long>().add(current.getId()).getList(), 
					UploadStatus.COMPLETE.toString(), 
					new ArrayListWrapper<String>().add(ReviewStatus.REVIEW_UPLOAD_WAITING.toString()).add(ReviewStatus.REVIEW_UPLOAD_REFUSE.toString()).getList(),
					user.getId().toString());
			if(audios!=null && audios.size()>0){
				for(MediaAudioPO audio:audios){
					rows.add(new MediaAudioVO().set(audio));
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
				List<FolderPO> breadCrumbFolders = folderQuery.findPermissionCompanyFolderByIdIn(parentIds, FolderType.COMPANY_AUDIO.toString());
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
	 * 加载所有的音频媒资<br/>
	 * <b>作者:</b>lvdeyang<br/>
	 * <b>版本：</b>1.0<br/>
	 * <b>日期：</b>2018年12月6日 下午4:03:27
	 * @return List<MediaAudioVO> 视频媒资列表
	 */
	public List<MediaAudioVO> loadAll() throws Exception{
		
		//TODO 权限校验
		List<FolderPO> folderTree = folderQuery.findPermissionCompanyTree(FolderType.COMPANY_AUDIO.toString());
		
		List<Long> folderIds = new ArrayList<Long>();
		for(FolderPO folderPO: folderTree){
			folderIds.add(folderPO.getId());
		}
		
		List<MediaAudioPO> audios = mediaAudioDao.findByFolderIdIn(folderIds);
		
		List<FolderPO> roots = folderQuery.findRoots(folderTree);
		
		List<MediaAudioVO> medias = new ArrayList<MediaAudioVO>();
		
		for(FolderPO root:roots){
			medias.add(new MediaAudioVO().set(root));
		}
		
		packMediaAudioTree(medias, folderTree, audios);
		
		return medias;
	}
	
	/**
	 * 加载所有的音频媒资目录<br/>
	 * <b>作者:</b>lzp<br/>
	 * <b>版本：</b>1.0<br/>
	 * <b>日期：</b>2019年6月27日 下午4:03:27
	 * @return List<MediaAudioVO> 视频媒资列表
	 */
	public List<MediaAudioVO> loadAllFolder() throws Exception{
		
		List<FolderPO> folderTree = folderQuery.findPermissionCompanyTree(FolderType.COMPANY_AUDIO.toString());
		
		List<Long> folderIds = new ArrayList<Long>();
		for(FolderPO folderPO: folderTree){
			folderIds.add(folderPO.getId());
		}
		
		List<FolderPO> roots = folderQuery.findRoots(folderTree);
		List<MediaAudioVO> medias = new ArrayList<MediaAudioVO>();
		for(FolderPO root:roots){
			medias.add(new MediaAudioVO().set(root));
		}
		
		packMediaAudioTree(medias, folderTree, new ArrayList<MediaAudioPO>());
		
		return medias;
	}
	
	/**
	 * 加载所有的音频媒资<br/>
	 * <b>作者:</b>lzp<br/>
	 * <b>版本：</b>1.0<br/>
	 * <b>日期：</b>2019年8月11日 下午4:03:27
	 * @return List<MediaAudioVO> 视频媒资列表
	 */
	public List<MediaAudioVO> loadAllByTags() throws Exception{
		//TODO 权限校验
		UserVO user = userQuery.current();
		
		List<FolderPO> folderTree = folderQuery.findPermissionCompanyTree(FolderType.COMPANY_AUDIO.toString());
		
		List<String> tags = user.getTags();
		
		if (tags == null || tags.isEmpty()) return null;
		
		List<Long> folderIds = new ArrayList<Long>();
		for(FolderPO folderPO: folderTree){
			folderIds.add(folderPO.getId());
		}
		
		List<MediaAudioVO> audios = new ArrayListWrapper<MediaAudioVO>().getList();
		
		for (String tag : tags) {
			List<MediaAudioPO> childAudios = mediaAudioDao.findByFolderIdInAndTag(folderIds, tag);
			if (childAudios == null || childAudios.isEmpty()) continue;
			MediaAudioVO audio = new MediaAudioVO();
			audio.setName(tag);
			audio.setChildren(MediaAudioVO.getConverter(MediaAudioVO.class).convert(childAudios, MediaAudioVO.class));
			audios.add(audio);
		}
		
		return audios;
	}
	
	/**
	 * 查询文件夹下上传完成的音频媒资<br/>
	 * <b>作者:</b>lvdeyang<br/>
	 * <b>版本：</b>1.0<br/>
	 * <b>日期：</b>2019年1月28日 上午10:38:53
	 * @param Long folderId 文件夹id
	 * @return List<MediaPicturePO> 音频媒资
	 */
	public List<MediaAudioPO> findCompleteByFolderId(Long folderId){
		return mediaAudioDao.findByFolderIdAndUploadStatusOrderByName(folderId, UploadStatus.COMPLETE);
	}
	
	/**
	 * 查询文件夹下上传完成的音频媒资（批量）<br/>
	 * <b>作者:</b>lvdeyang<br/>
	 * <b>版本：</b>1.0<br/>
	 * <b>日期：</b>2019年1月29日 下午3:36:37
	 * @param Collection<Long> folderIds 文件夹id列表
	 * @return List<MediaPicturePO> 音频媒资
	 */
	public List<MediaAudioPO> findCompleteByFolderIds(Collection<Long> folderIds){
		return mediaAudioDao.findByFolderIdInAndUploadStatus(folderIds, UploadStatus.COMPLETE);
	}
	
	/**
	 * 获取文件夹（多个）下的音频媒资上传任务（上传未完成的）<br/>
	 * <b>作者:</b>lvdeyang<br/>
	 * <b>版本：</b>1.0<br/>
	 * <b>日期：</b>2018年11月29日 下午1:25:31
	 * @param Collection<Long> folderIds 文件夹id列表
	 * @return List<MediaPicturePO> 上传任务列表
	 */
	public List<MediaAudioPO> findTasksByFolderIds(Collection<Long> folderIds){
		return mediaAudioDao.findByFolderIdInAndUploadStatus(folderIds, UploadStatus.UPLOADING);
	}
	
	/**
	 * 根据uuid查找媒资音频（内存循环）<br/>
	 * <b>作者:</b>lvdeyang<br/>
	 * <b>版本：</b>1.0<br/>
	 * <b>日期：</b>2018年11月26日 上午11:52:58
	 * @param String uuid 图片uuid
	 * @param Collection<MediaAudioPO> pictures 查找范围
	 * @return MediaPicturePO 查找结果
	 */
	public MediaAudioPO loopForUuid(String uuid, Collection<MediaAudioPO> audios){
		if(audios==null || audios.size()<=0) return null;
		for(MediaAudioPO audio:audios){
			if(audio.getUuid().equals(uuid)){
				return audio;
			}
		}
		return null;
	}
	
	/**
	 * 根据uuid查找媒资音频<br/>
	 * <b>作者:</b>lzp<br/>
	 * <b>版本：</b>1.0<br/>
	 * <b>日期：</b>2019年8月6日 上午11:52:58
	 * @param List<String> uuids 音频uuids
	 * @return List<MediaAudioPO> 查找结果
	 */
	public List<MediaAudioPO> questByUuid(List<String> uuids){
		if(uuids==null || uuids.size()<=0) return null;
		return mediaAudioDao.findByUuidIn(uuids);
	}
	
	/**
	 * 生成媒资音频树<br/>
	 * <b>作者:</b>ldy<br/>
	 * <b>版本：</b>1.0<br/>
	 * <b>日期：</b>2019年3月31日 上午11:29:34
	 * @param List<MediaAudioVO> roots 根
	 * @param List<FolderPO> folders 所有文件夹
	 * @param List<MediaAudioPO> medias 所有视频媒资
	 */
	public void packMediaAudioTree(List<MediaAudioVO> roots, List<FolderPO> folders, List<MediaAudioPO> medias) throws Exception{
		if(roots == null || roots.size() <= 0){
			return;
		}
		for(MediaAudioVO root: roots){
			if(root.getType().equals(MediaVideoItemType.FOLDER.toString())){
				if(root.getChildren() == null) root.setChildren(new ArrayList<MediaAudioVO>());
				for(FolderPO folder: folders){
					if(folder.getParentId() != null && folder.getParentId().equals(root.getId())){
						root.getChildren().add(new MediaAudioVO().set(folder));
					}
				}
				for(MediaAudioPO media: medias){
					if(media.getFolderId() != null && media.getFolderId().equals(root.getId())){
						root.getChildren().add(new MediaAudioVO().set(media));
					}
				}
				if(root.getChildren().size() > 0){
					packMediaAudioTree(root.getChildren(), folders, medias);
				}
			}
		}
	}
	
	
	/**
	 * 生成文件存储预览路径(云转码使用)<br/>
	 * <b>作者:</b>lzp<br/>
	 * <b>版本：</b>1.0<br/>
	 * <b>日期：</b>2019年8月7日 下午4:03:27
	 * @return String 预览路径
	 * @throws Exception 
	 */
	public String buildUrl(String name, String folderUuid) throws Exception {
		UserVO user = userQuery.current();
		
		String separator = File.separator;
		String webappPath = path.webappPath();
		String basePath = new StringBufferWrapper().append(webappPath)
												   .append("upload")
												   .append(separator)
												   .append("tmp")
												   .append(separator).append(user.getGroupName())
												   .append(separator).append(folderUuid)
												   .toString();
		Date date = new Date();
		String version = new StringBufferWrapper().append(MediaVideoPO.VERSION_OF_ORIGIN).append(".").append(date.getTime()).toString();
		String folderPath = new StringBufferWrapper().append(basePath).append(separator).append(name).append(separator).append(version).toString();
		File file = new File(folderPath);
		if(!file.exists()) file.mkdirs();
		//这个地方保证每个任务的路径都不一样
		Thread.sleep(1);
		
		return new StringBufferWrapper().append("upload/tmp/")
												     .append(user.getGroupName())
												     .append("/")
												     .append(folderUuid)
												     .append("/")
												     .append(name)
												     .append("/")
												     .append(version)
												     .toString();
	}
}
