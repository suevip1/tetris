package com.sumavision.tetris.mims.app.operation.mediaPackage.mediaPermission;

import java.util.List;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.RepositoryDefinition;

import com.sumavision.tetris.orm.dao.BaseDAO;

@RepositoryDefinition(domainClass = OperationPackageMediaPermissionPO.class, idClass = Long.class)
public interface OperationPackageMediaPermissionDAO extends BaseDAO<OperationPackageMediaPermissionPO>{
	
	/**
	 * 根据套餐id获取关联数组<br/>
	 * <b>作者:</b>lzp<br/>
	 * <b>版本：</b>1.0<br/>
	 * <b>日期：</b>2020年2月19日 上午11:02:21
	 * @param Long packageId 套餐id
	 * @return List<OperationMediaPackagePermissionPO>
	 */
	public List<OperationPackageMediaPermissionPO> findByPackageId(Long packageId);
	
	/**
	 * 根据套餐id和资源uuid查询绑定信息<br/>
	 * <b>作者:</b>lzp<br/>
	 * <b>版本：</b>1.0<br/>
	 * <b>日期：</b>2020年3月6日 上午9:31:32
	 * @param Long packageId 套餐id
	 * @param String mimsUuid 资源uuid
	 * @return
	 */
	public OperationPackageMediaPermissionPO findByPackageIdAndMimsUuid(Long packageId, String mimsUuid);
	
	/**
	 * 根据套餐id删除关联<br/>
	 * <b>作者:</b>lzp<br/>
	 * <b>版本：</b>1.0<br/>
	 * <b>日期：</b>2020年2月21日 上午10:17:23
	 * @param packageId
	 */
	@Modifying
	@Query(value = "delete from MIMS_OPERATION_PACKAGE_MEDIA_PERMISSION where package_id=?1 ", nativeQuery = true)
	public void removeByPackageId(Long packageId);
}
