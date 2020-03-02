package com.sumavision.tetris.cs.menu;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.sumavision.tetris.mims.app.media.avideo.MediaAVideoQuery;
import com.sumavision.tetris.mims.app.media.avideo.MediaAVideoVO;
import com.sumavision.tetris.mims.app.media.live.MediaPushLiveQuery;
import com.sumavision.tetris.mims.app.media.live.MediaPushLiveVO;

@Component
public class CsResourceQuery {
	@Autowired
	CsResourceDAO csSourceDao;

	@Autowired
	MediaAVideoQuery mediaAVideoQuery;
	
	@Autowired
	MediaPushLiveQuery mediaPushLiveQuery;

	/**
	 * 根据cs目录id获取媒资列表<br/>
	 * <b>作者:</b>lzp<br/>
	 * <b>版本：</b>1.0<br/>
	 * <b>日期：</b>2019年6月25日 上午11:06:57
	 * @param Long menuId 目录id
	 * @return List<CsResourceVO> cs媒资列表
	 */
	public List<CsResourceVO> queryMenuResources(Long menuId) throws Exception {
		List<CsResourcePO> resources = csSourceDao.findByParentId(menuId);

		List<CsResourceVO> menuResources = new ArrayList<CsResourceVO>();
		if (resources != null && resources.size() > 0) {
			for (CsResourcePO item:resources) {
				menuResources.add(new CsResourceVO().set(item));
			}
		}
		return menuResources;
	}

	/**
	 * 根据cs媒资id获取媒资<br/>
	 * <b>作者:</b>lzp<br/>
	 * <b>版本：</b>1.0<br/>
	 * <b>日期：</b>2019年6月25日 上午11:06:57
	 * @param Long resourceId 媒资id
	 * @return CsResourceVO 媒资
	 */
	public CsResourceVO queryResourceById(Long resourceId) throws Exception {
		CsResourcePO resource = csSourceDao.findOne(resourceId);

		return new CsResourceVO().set(resource);
	}
	
	/**
	 * 根据id批量获取媒资<br/>
	 * <b>作者:</b>lzp<br/>
	 * <b>版本：</b>1.0<br/>
	 * <b>日期：</b>2020年1月8日 上午9:19:28
	 * @param List<Long> resourceIds 媒资id数组
	 */
	public List<CsResourceVO> queryResourceByIds(List<Long> resourceIds) throws Exception {
		List<CsResourcePO> resourcePOs = csSourceDao.findAll(resourceIds);
		return CsResourceVO.getConverter(CsResourceVO.class).convert(resourcePOs, CsResourceVO.class);
	}

	/**
	 * 根据频道id获取媒资列表<br/>
	 * <b>作者:</b>lzp<br/>
	 * <b>版本：</b>1.0<br/>
	 * <b>日期：</b>2019年6月25日 上午11:06:57
	 * @param Long channelId 频道id
	 * @return List<CsResourceVO> cs媒资列表
	 */
	public List<CsResourceVO> getResourcesFromChannelId(Long channelId) throws Exception {
		List<CsResourcePO> resourcePOList = csSourceDao.findByChannelId(channelId);
		return CsResourceVO.getConverter(CsResourceVO.class).convert(resourcePOList, CsResourceVO.class);
	}

	/**
	 * 获取mims的所有音视频媒资<br/>
	 * <b>作者:</b>lzp<br/>
	 * <b>版本：</b>1.0<br/>
	 * <b>日期：</b>2019年6月25日 上午11:06:57
	 * @param Long id cs目录id
	 * @return List<MediaAVideoVO> mims媒资列表
	 */
	public List<MediaAVideoVO> getMIMSResources(Long id) throws Exception {

		List<MediaAVideoVO> mimsVideoList = mediaAVideoQuery.loadAll();
		
		return mimsVideoList;
	}
	
	public List<MediaPushLiveVO> getMIMSLiveResources() throws Exception {
		List<MediaPushLiveVO> mimsLiveList = mediaPushLiveQuery.loadAll();
		
		return mimsLiveList;
	}
}
