package com.sumavision.bvc.common.group.dao;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.RepositoryDefinition;

import com.sumavision.bvc.common.group.po.CommonChannelForwardPO;
import com.sumavision.bvc.device.group.po.ChannelForwardPO;
import com.sumavision.tetris.orm.dao.MetBaseDAO;

@RepositoryDefinition(domainClass = CommonChannelForwardPO.class, idClass = long.class)
public interface CommonChannelForwardDAO extends MetBaseDAO<CommonChannelForwardPO> {

	/**
	 * @Title: 查询设备组中的转发
	 * @param groupId 设备组id
	 * @return List<ChannelForwardPO> 转发
	 * @throws
	 */
	@Query("select forward from CommonChannelForwardPO forward where forward.group.id=?1")
	public Page<CommonChannelForwardPO> findByGroupId(Long groupId, Pageable page);
	
	/**
	 * @Title: 根据combineUuid查询设备组中的转发
	 * @param groupId combineUuid
	 * @return List<CommonChannelForwardPO> 转发
	 * @throws
	 */
	@Query("select forward from CommonChannelForwardPO forward where forward.combineUuid=?1")
	public Page<CommonChannelForwardPO> findByCombineUuid(String combineUuid, Pageable page);
	
	/**
	 * @Title: 根据combineUuid查询设备组中的转发
	 * @param groupId combineUuid
	 * @return List<ChannelForwardPO> 转发
	 * @throws
	 */
	@Query("select forward from CommonChannelForwardPO forward where forward.combineUuid=?1")
	public List<CommonChannelForwardPO> findByCombineUuid(String combineUuid);
}
