package com.sumavision.tetris.cms.article;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.RepositoryDefinition;

import com.sumavision.tetris.orm.dao.BaseDAO;

@RepositoryDefinition(domainClass = ArticleUserPermissionPO.class, idClass = Long.class)
public interface ArticleUserPermissionDAO extends BaseDAO<ArticleUserPermissionPO>{

	/**
	 * 根据文章id列表删除关联<br/>
	 * <b>作者:</b>ldy<br/>
	 * <b>版本：</b>1.0<br/>
	 * <b>日期：</b>2019年3月27日 下午5:20:39
	 * @param articleIds 文章id列表
	 */
	public void deleteByArticleIdIn(Collection<Long> articleIds);
	
	/**
	 * 根据文章id和组织di查关联<br/>
	 * <b>作者:</b>ldy<br/>
	 * <b>版本：</b>1.0<br/>
	 * <b>日期：</b>2019年3月28日 下午2:51:01
	 * @param articleId 文章id
	 * @param groupId 组织id
	 * @return ArticleUserPermissionPO 关联
	 */
	public ArticleUserPermissionPO findByArticleIdAndGroupId(Long articleId, String groupId);
	
	/**
	 * 根据文章id和用户id查关联<br/>
	 * <b>作者:</b>ldy<br/>
	 * <b>版本：</b>1.0<br/>
	 * <b>日期：</b>2019年3月28日 下午2:51:51
	 * @param articleId 文章id
	 * @param userId 用户id
	 * @return ArticleUserPermissionPO 关联
	 */
	public ArticleUserPermissionPO findByArticleIdAndUserId(Long articleId, String userId);
	
	/**
	 * 根据用户id查直播文章id列表<br/>
	 * <b>作者:</b>lzp<br/>
	 * <b>版本：</b>1.0<br/>
	 * <b>日期：</b>2019年6月10日 下午2:51:51
	 * @param groupId 组织id
	 * @return List<Long> 文章id列表
	 */
	@Query(value = "SELECT article_id from tetris_cms_article_user_permission where group_id=?1", nativeQuery = true)
	public List<Long> findArticleIdsByGroupId(Long groupId);
}
