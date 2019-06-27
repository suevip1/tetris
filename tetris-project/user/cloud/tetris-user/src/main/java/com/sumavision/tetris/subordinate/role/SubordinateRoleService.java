package com.sumavision.tetris.subordinate.role;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.druid.sql.visitor.functions.If;
import com.sumavision.tetris.user.UserQuery;
import com.sumavision.tetris.user.UserVO;

/**
 * 隶属角色操作（主增删改）<br/>
 * <b>作者:</b>lzp<br/>
 * <b>版本：</b>1.0<br/>
 * <b>日期：</b>2019年5月29日 上午9:58:25
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class SubordinateRoleService {
	@Autowired
	private UserQuery userserQuery;
	@Autowired
	private SubordinateRoleDAO subordinateRoleDAO;

	public SubordinateRoleVO addRole(Long companyId, String roleName) throws Exception {
		SubordinateRolePO rolePO = new SubordinateRolePO();

		UserVO user = userserQuery.current();

		rolePO.setName(roleName);
		rolePO.setCompanyId(companyId);
		if (user.getId() != null) {
			rolePO.setUserId(user.getId().toString());
		} else if (user.getUuid() != null && !user.getUuid().isEmpty()) {
			rolePO.setUserId(user.getUuid());
		} else {
			return null;
		}

		subordinateRoleDAO.save(rolePO);

		return new SubordinateRoleVO().set(rolePO);
	}

	public SubordinateRoleVO editRole(Long roleId, String roleName) throws Exception {
		SubordinateRolePO rolePO = subordinateRoleDAO.findOne(roleId);

		if (rolePO != null) {
			rolePO.setName(roleName);
			subordinateRoleDAO.save(rolePO);
		}

		return rolePO != null ? new SubordinateRoleVO().set(rolePO) : null;
	}
	
	public SubordinateRoleVO removeRole(Long roleId) throws Exception{
		SubordinateRolePO rolePO = subordinateRoleDAO.findOne(roleId);
		
		if (rolePO != null) {
			subordinateRoleDAO.delete(rolePO);
		}
		
		return rolePO != null ? new SubordinateRoleVO().set(rolePO) : null;
	}
}