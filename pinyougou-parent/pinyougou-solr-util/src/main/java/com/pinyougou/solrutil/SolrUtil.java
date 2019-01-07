package com.pinyougou.solrutil;

import com.alibaba.fastjson.JSON;
import com.pinyougou.mapper.TbItemMapper;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.pojo.TbItemExample;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class SolrUtil {
	@Autowired
	private TbItemMapper itemMapper;
	@Autowired
	private SolrTemplate solrTemplate;

	// 导入item数据到solr
	public void importItemData() {
		TbItemExample example = new TbItemExample();
		TbItemExample.Criteria criteria = example.createCriteria();
		criteria.andStatusEqualTo("1"); //已审核
		List<TbItem> itemList = itemMapper.selectByExample(example);
		for (TbItem item : itemList) {
			try {
				// 将json字符串转换为map集合。
				// json中有多个对象，会将对象的属性作为key，属性值作为value
				item.setSpecMap(JSON.parseObject(item.getSpec(), Map.class));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		// 批量保存
		solrTemplate.saveBeans(itemList);
		solrTemplate.commit();
	}

	public static void main(String[] args) {
		ApplicationContext applicationContext = new ClassPathXmlApplicationContext("classpath*:spring/applicationContext*.xml");
		SolrUtil solrUtil = applicationContext.getBean(SolrUtil.class);
		solrUtil.importItemData();
	}
}
