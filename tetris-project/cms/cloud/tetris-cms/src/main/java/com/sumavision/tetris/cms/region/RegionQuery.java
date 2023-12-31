package com.sumavision.tetris.cms.region;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.sumavision.tetris.commons.util.wrapper.StringBufferWrapper;
import com.sumavision.tetris.user.UserVO;

/**
 * 地区操作<br/>
 * <b>作者: </b>ldy<br/>
 * <b>版本: </b>1.0<br/> 
 * <b>日期: </b>2019年2月28日 下午2:25:18
 */
@Component
public class RegionQuery {
	
	@Autowired
	private RegionDAO regionDao;
	
	@Autowired
	private RegionUserPermissionDAO regionUserPermissionDao;

	/**
	 * 根据用户查询地区树<br/>
	 * <p>1.普通用户根据userId查询
	 * 	  2.企业用户根据企业id查询</p>
	 * <b>作者:</b>ldy<br/>
	 * <b>版本：</b>1.0<br/>
	 * <b>日期：</b>2019年3月26日 下午2:36:00
	 * @param user 用户信息
	 * @return List<RegionVO> 地区树
	 */
	public List<RegionVO> queryRegionTree(UserVO user) throws Exception {

		List<RegionPO> regions = null;
		if(user.getGroupId() != null){
			regions = regionDao.findByGroupId(user.getGroupId());
		}else if(user.getUuid() != null){
			regions = regionDao.findByUserId(user.getUuid());
		}

		List<RegionVO> rootRegions = generateRootRegions(regions);

		packRegionTree(rootRegions, regions);

		return rootRegions;
	}

	/**
	 * 获得根地区<br/>
	 * <b>作者:</b>ldy<br/>
	 * <b>版本：</b>1.0<br/>
	 * <b>日期：</b>2019年2月28日下午2:40:48
	 * @param regions 所有地区
	 * @return List<RegionVO> 所有根地区
	 */
	private List<RegionVO> generateRootRegions(Collection<RegionPO> regions) throws Exception {
		if (regions == null || regions.size() <= 0)
			return null;
		List<RegionVO> rootRegions = new ArrayList<RegionVO>();
		for (RegionPO region : regions) {
			if (region.getParentId() == null) {
				rootRegions.add(new RegionVO().set(region));
			}
		}
		return rootRegions;
	}

	/**
	 * 根地区节点加子地区方法<br/>
	 * <b>作者:</b>ldy<br/>
	 * <b>版本：</b>1.0<br/>
	 * <b>日期：</b>2019年2月28日下午2:52:06
	 * @param rootRegions 根地区
	 * @param totalRegions 所有地区
	 * @return void
	 */
	public void packRegionTree(List<RegionVO> rootRegions, List<RegionPO> totalRegions) throws Exception {
		if (rootRegions == null || rootRegions.size() <= 0)
			return;
		for (int i = 0; i < rootRegions.size(); i++) {
			RegionVO rootRegion = rootRegions.get(i);
			for (int j = 0; j < totalRegions.size(); j++) {
				RegionPO region = totalRegions.get(j);
				if (region.getParentId() != null && region.getParentId() == rootRegion.getId()) {
					if (rootRegion.getSubRegions() == null)
						rootRegion.setSubRegions(new ArrayList<RegionVO>());
					rootRegion.getSubRegions().add(new RegionVO().set(region));
				}
			}
			if (rootRegion.getSubRegions() != null && rootRegion.getSubRegions().size() > 0) {
				packRegionTree(rootRegion.getSubRegions(), totalRegions);
			}
		}
	}
	
	/**
	 * 查询地区下所有子地区<br/>
	 * <b>作者:</b>ldy<br/>
	 * <b>版本：</b>1.0<br/>
	 * <b>日期：</b>2019年2月28日下午3:04:47
	 * @param id 父地区id
	 * @return List<RegionPO> 子地区
	 */
	public List<RegionPO> findAllSubTags(Long id) throws Exception{
		return regionDao.findAllSubRegions(new StringBufferWrapper().append("%/")
															          .append(id)
															          .toString(), 
											 new StringBufferWrapper().append("%/")
																      .append(id)
																      .append("/%")
																      .toString());
	}
	
	/**
	 * 校验地区和用户是否匹配<br/>
	 * <b>作者:</b>ldy<br/>
	 * <b>版本：</b>1.0<br/>
	 * <b>日期：</b>2019年3月28日 下午2:12:12
	 * @param regionId 地区id
	 * @param user 用户
	 */
	public boolean hasPermission(Long regionId, UserVO user) throws Exception{
		RegionUserPermissionPO permission = null;
		if(user.getGroupId() != null){
			permission = regionUserPermissionDao.findByRegionIdAndGroupId(regionId, user.getGroupId());
		}else if(user.getUuid() != null){
			permission = regionUserPermissionDao.findByRegionIdAndUserId(regionId, user.getUuid());
		}
		
		if(permission == null) return false;
		return true;
	}
	
	/**
	 * 根据地区信息获取最底层有效ip<br/>
	 * <b>作者:</b>lzp<br/>
	 * <b>版本：</b>1.0<br/>
	 * <b>日期：</b>2019年12月17日 下午2:55:31
	 * @param UserVO user 用户信息
	 * @param String province 省份
	 * @param String city 城市
	 * @param String district 地区
	 * @return RegionVO 有效ip的地区信息
	 */
	public RegionVO queryIp(UserVO user,  String province, String city, String district) throws Exception {
		String groupId = user.getGroupId();
		if (groupId == null) return null;
		
		if (district != null && !district.isEmpty()) {
			RegionPO regionPO = queryIp(groupId, district);
			return regionPO == null ? queryIp(user, province, city, null) : new RegionVO().set(regionPO);
		} else if (city != null && !city.isEmpty()) {
			RegionPO regionPO = queryIp(groupId, city);
			return regionPO == null ? queryIp(user, province, null, null) : new RegionVO().set(regionPO);
		} else if (province != null && !province.isEmpty()) {
			RegionPO regionPO = queryIp(groupId, province);
			return regionPO == null ? null : new RegionVO().set(regionPO);
		}else {
			return null;
		}
	}
	
	private RegionPO queryIp(String groupId, String regionName) throws Exception {
		RegionPO region = null;
		
		if (regionName != null && !regionName.isEmpty()) {
			List<RegionPO> regions = regionDao.findByGroupIdAndName(groupId, regionName);
			if (!regions.isEmpty()) {
				RegionPO first = regions.get(0);
				if (regions.size() == 1) {
					if (first.getIp() != null && !first.getIp().isEmpty()){
						region = first;
					}
				} else {
					for (RegionPO regionPO : regions) {
						if (regionPO.getIp() != null && !regionPO.getIp().isEmpty()) {
							region = regionPO;
						}
					}
				}
			}
		}
		
		return region;
	}
}
