package com.pinyougou.search.service;

import java.util.List;
import java.util.Map;

public interface ItemSearchService {
	/**
	 * @param searchMap 查询条件，封装在map中。
	 * @return 查询结果，封装在map中。
	 * */
	Map<String,Object> search(Map searchMap);

	/**
	 * 导入sku列表
	 * */
	void importList(List list);

	/**
	 * 通过goods删除数据
	 * */
	void deleteByGoodsIds(List goodsIdList);
}
