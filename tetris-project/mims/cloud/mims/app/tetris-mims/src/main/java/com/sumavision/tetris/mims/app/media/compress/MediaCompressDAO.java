package com.sumavision.tetris.mims.app.media.compress;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.RepositoryDefinition;

import com.sumavision.tetris.mims.app.media.UploadStatus;
import com.sumavision.tetris.orm.dao.BaseDAO;

@RepositoryDefinition(domainClass = MediaCompressPO.class, idClass = Long.class)
public interface MediaCompressDAO extends BaseDAO<MediaCompressPO>{

	/**
	 * 获取文件夹下特定上传状态以及特定例外审核状态的压缩文件（批量）<br/>
	 * <b>作者:</b>lvdeyang<br/>
	 * <b>版本：</b>1.0<br/>
	 * <b>日期：</b>2019年1月29日 下午3:33:50
	 * @param Collection<Long> folderIds 文件夹id列表
	 * @param UploadStatus status 上传状态
	 * @param Collection<ReviewStatus> reviewStatus 例外审核状态列表
	 * @param String authorId 作者id
	 * @return List<MediaCompressPO> 压缩文件列表
	 */
	@Query(value = "SELECT * FROM MIMS_MEDIA_COMPRESS WHERE FOLDER_ID IN ?1 AND ((UPLOAD_STATUS=?2 AND (REVIEW_STATUS IS NULL OR REVIEW_STATUS NOT IN ?3)) OR AUTHORID=?4)", nativeQuery = true)
	public List<MediaCompressPO> findByFolderIdInAndUploadStatusAndReviewStatusNotInOrAuthorId(Collection<Long> folderIds, String status, Collection<String> reviewStatus, String authorId);
	
	
	/**
	 * 获取文件夹下特定上传状态的压缩媒资<br/>
	 * <b>作者:</b>ldy<br/>
	 * <b>版本：</b>1.0<br/>
	 * <b>日期：</b>2019年3月31日 下午2:50:17
	 * @param Long folderId 文件夹id
	 * @param UploadStatus uploadStatus 
	 * @return List<MediaPicturePO> 图片媒资
	 */
	public List<MediaCompressPO> findByFolderIdAndUploadStatusOrderByName(Long folderId, UploadStatus uploadStatus);
	
	/**
	 * 获取文件夹下特定上传状态的压缩媒资（批量）<br/>
	 * <b>作者:</b>ldy<br/>
	 * <b>版本：</b>1.0<br/>
	 * <b>日期：</b>2019年3月31日 下午2:50:35
	 * @param Collection<Long> folderIds 文件夹id列表
	 * @param UploadStatus status 上传状态
	 * @return List<MediaCompressPO> 媒资图片列表
	 */
	public List<MediaCompressPO> findByFolderIdInAndUploadStatus(Collection<Long> folderIds, UploadStatus status);
	
	/**
	 * 获取文件夹下的压缩媒资（批量）<br/>
	 * <b>作者:</b>ldy<br/>
	 * <b>版本：</b>1.0<br/>
	 * <b>日期：</b>2019年3月31日 下午2:51:17
	 * @param Long folderId 文件夹id
	 * @return List<MediaCompressPO> 压缩媒资列表
	 */
	public List<MediaCompressPO> findByFolderIdIn(Collection<Long> folderId);
	
	/**
	 * 判断临时文件是否可以删除<br/>
	 * <b>作者:</b>ldy<br/>
	 * <b>版本：</b>1.0<br/>
	 * <b>日期：</b>2019年3月31日 下午2:52:31
	 * @param String tmpPath 临时文件路径
	 * @param Collection<Long> ids 例外素材id
	 * @return List<MaterialFilePO> 查询结果
	 */
	public List<MediaCompressPO> findByUploadTmpPathAndIdNotIn(String tmpPath, Collection<Long> ids);
	
}
