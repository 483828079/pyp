package com.pinyougou.search.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.common.StringUtls;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.search.service.ItemSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.*;
import org.springframework.data.solr.core.query.result.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service(timeout = 5000)
public class ItemSearchServiceImpl implements ItemSearchService {

	@Autowired
	private SolrTemplate solrTemplate;
	@Autowired
	private RedisTemplate redisTemplate;
	// 在solr中存储的是item的相关信息

	// 根据关键字，获取查询结果。并对关键字进行高亮。
	private Map<String, Object> searchList(Map searchMap) {
		/*Query query = new SimpleQuery("*:*"); // 查询所有字段
		// 通过keywords进行匹配，查询条件Map集合中的keywords
		Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
		query.addCriteria(criteria);
		ScoredPage<TbItem> page = solrTemplate.queryForPage(query, TbItem.class);
		Map<String, Object> map = new HashMap<>();
		// 将查询结果封装到map集合中。key为rows。
		map.put("rows", page.getContent());
		return map;*/

		HighlightQuery query = new SimpleHighlightQuery();
		HighlightOptions highlightOptions = new HighlightOptions();
		// 设置高亮的域
		highlightOptions.addField("item_title");
		// 设置需要高亮域的值需要的效果,如果高亮的域和查询域的值匹配上就加上。
		highlightOptions.setSimplePrefix("<em style='color:red'>");
		highlightOptions.setSimplePostfix("</em>");
		query.setHighlightOptions(highlightOptions);
		String keywords = (String) searchMap.get("keywords");
		if (! StringUtls.isEmpty(keywords)) {
			keywords = keywords.replace(" ", "");
			// 设置查询条件，根据keywords进行匹配
			Criteria criteria = new Criteria("item_keywords").is(keywords);
			query.addCriteria(criteria);

			// 设置过滤。过滤是在通过关键字查询出来结果之上进行对查询结果再次根据某些域进行过滤
			// 根据分组对结果进行过滤
			if (! StringUtls.isEmpty((String) searchMap.get("category"))) {
				FilterQuery filterQuery = new SimpleFacetQuery();
				Criteria categoryCriteria = new Criteria("item_category").is(searchMap.get("category"));
				filterQuery.addCriteria(categoryCriteria);
				query.addFilterQuery(filterQuery);
			}

			// 根据品牌进行过滤
			if (! StringUtls.isEmpty((String) searchMap.get("brand"))) {
				FilterQuery filterQuery = new SimpleFacetQuery();
				Criteria categoryCriteria = new Criteria("item_brand").is(searchMap.get("brand"));
				filterQuery.addCriteria(categoryCriteria);
				query.addFilterQuery(filterQuery);
			}

			// 根据规格列表进行过滤,规格列表使用Map存储
			// 因为key和value不一样。查询的条件一定会是String。
			Map<String, String> specMap = (Map) searchMap.get("spec");
			if (searchMap.size() > 0) {
				for (String key : specMap.keySet()) {
					FilterQuery filterQuery = new SimpleFacetQuery();
					Criteria categoryCriteria = new Criteria("item_spec_" + key).is(specMap.get(key));
					filterQuery.addCriteria(categoryCriteria);
					query.addFilterQuery(filterQuery);
				}
			}

			// 根据价格区间进行过滤
			if (!StringUtls.isEmpty((String) searchMap.get("price"))) {
				// 索引为0是起始价格,1是结束价格。*为没有结束价格
				String[] price = ((String)searchMap.get("price")).split("-");
				if (! price[0].equals('0')) { // 如果为0不用考虑最小价格
					FilterQuery filterQuery = new SimpleFacetQuery();				// 小于等于最小区间
					Criteria categoryCriteria = new Criteria("item_price").greaterThanEqual(price[0]);
					filterQuery.addCriteria(categoryCriteria);
					query.addFilterQuery(filterQuery);
				}

				if (! price[1].equals("*")) {// 如果为*不用考虑最大价格
					FilterQuery filterQuery = new SimpleFacetQuery();					// 大于等于最大区间
					Criteria categoryCriteria = new Criteria("item_price").lessThanEqual(price[1]);
					filterQuery.addCriteria(categoryCriteria);
					query.addFilterQuery(filterQuery);
				}
			}

			// 分页查询
			// 当前页
			String pageNoStr = (String) searchMap.get("pageNo");
			Integer pageNo = 1;
			if (! StringUtls.isEmpty(pageNoStr)) {
				pageNo = Integer.parseInt(pageNoStr); // 如果没有当前页码，默认为第一页
			}

			String pageSizeStr = (String) searchMap.get("pageSize");
			Integer pageSize = 10;
			if (! StringUtls.isEmpty(pageSizeStr)) {
				pageSize = Integer.parseInt(pageSizeStr); // 默认每页显示10条数据
			}
			query.setOffset((pageNo - 1) * pageSize); // 设置起始行
			query.setRows(pageSize); // 设置每页显示数据

			// 排序
			String sortValue = (String) searchMap.get("sort");
			String sortField = (String) searchMap.get("sortField");
			if (! StringUtls.isEmpty(sortValue) && ! StringUtls.isEmpty(sortField)) {
				// 升序排序
				if (sortValue.equals("ASC")) {
					Sort sort = new Sort(Sort.Direction.ASC, "item_" + sortField);
					query.addSort(sort);
				}

				// 降序排序
				if (sortValue.equals("DESC")) {
					Sort sort = new Sort(Sort.Direction.DESC, "item_" + sortField);
					query.addSort(sort);
				}
			}


			HighlightPage<TbItem> page = solrTemplate.queryForHighlightPage(query, TbItem.class); // page代表着封装了关键字查询的结果和一些其他信息
			// 获取高亮域对象的入口集合。封装了item和匹配高亮域相关信息。
			List<HighlightEntry<TbItem>> highlighted = page.getHighlighted();
			for (HighlightEntry<TbItem> entry : highlighted) {
				TbItem item = entry.getEntity();
				if (entry.getHighlights().size() > 0 && entry.getHighlights().get(0).getSnipplets().size() > 0) {
					// 设置查询域的值中匹配到高亮域的item
					item.setTitle(entry.getHighlights().get(0).getSnipplets().get(0));
				}
			}
			Map<String, Object> map = new HashMap<>();
			map.put("rows", page.getContent());// 设置高亮域配置好的item集合到map
			map.put("totalPages", page.getTotalPages());
			map.put("total", page.getTotalElements());
			return map;
		}
		Map<String, Object> map = new HashMap<>();
		map.put("keywords", null);
		map.put("totalPages", 0);
		map.put("total", 0);
		return map;
	}

	// 对关键字查询结果按照分类进行分组，得到所有查询结果的分类。
	private List searchCategoryList(Map searchMap) {
		Query query = new SimpleQuery();    // 根据keywords对应的页面输入keywords关键字进行查询。
		Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
		query.addCriteria(criteria); // 添加查询条件
		GroupOptions groupOptions = new GroupOptions();
		groupOptions.addGroupByField("item_category"); // 设置分组域，可以设置多个
		query.setGroupOptions(groupOptions);
		GroupPage<TbItem> page = solrTemplate.queryForGroupPage(query, TbItem.class); // 因为可以有多个分组，所以该page对于分组并无实际意义。
		GroupResult<TbItem> result = page.getGroupResult("item_category"); // 获取分组域的分组结果集
		Page<GroupEntry<TbItem>> groupEntries = result.getGroupEntries();// 封装了分组信息的page
		List<GroupEntry<TbItem>> content = groupEntries.getContent(); // 分组结果集
		List list = new ArrayList();
		for (GroupEntry<TbItem> entry : content) {
			list.add(entry.getGroupValue());
		}
		return list;
	}

	// 根据分类名称查询商品和规格列表
	public Map<String, Object> searchBrandAndSpecList(String category) {
		// 从redis中拿出分类名称对应的模板Id
		Long typeId = (Long) redisTemplate.boundHashOps("itemCat").get(category);
		Map map = new HashMap();
		// 从redis中拿出商品信息放入结果集
		map.put("brandList", redisTemplate.boundHashOps("brandList").get(typeId));
		// 从redis中拿出规格列表放入结果集
		map.put("specList", redisTemplate.boundHashOps("specList").get(typeId));
		return map;
	}

	@Override
	public Map<String, Object> search(Map searchMap) {
		if (! StringUtls.isEmpty((String) searchMap.get("keywords"))) {
			searchMap.put("keywords", ((String) searchMap.get("keywords")).replace(" ", ""));
			Map<String, Object> map = new HashMap<>();
			// 将searchList返回值中的key和value添加到map
			map.putAll(searchList(searchMap));
			// 将按照关键字查询结果对分类的分组进行保存
			List<String> categoryList = searchCategoryList(searchMap);
			map.put("categoryList", categoryList);
			String category = (String) searchMap.get("category");
			// 如果查询条件有分类，就使用查询条件的分类初始化 品牌和规格列表
			if (category != null && !"".equals(category)) {
				map.putAll(searchBrandAndSpecList(category));
			} else {
				if (categoryList.size() > 0) { // 如果分组结果存在
					// 默认情况下，在页面显示的是根据关键字查询的第一个分类对应的商品和规格列表
					map.putAll(searchBrandAndSpecList(categoryList.get(0)));
				}
			}
			return map;
		}
		return new HashMap<>();
	}

	@Override
	public void importList(List list) {
		solrTemplate.saveBeans(list);
		solrTemplate.commit();
	}

	@Override
	public void deleteByGoodsIds(List goodsIdList) {
		Query query = new SimpleQuery();
		Criteria criteria = new Criteria("item_goodsid").in(goodsIdList);
		query.addCriteria(criteria);
		solrTemplate.delete(query);
		solrTemplate.commit();
	}
}
