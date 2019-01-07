package com.pinyougou.search.service.impl;

import com.alibaba.fastjson.JSON;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.search.service.ItemSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import java.util.List;
import java.util.Map;

@Component("itemSearchListener")
public class ItemSearchListener implements MessageListener {
	@Autowired
	private ItemSearchService itemSearchService;
	@Override
	public void onMessage(Message message) {
		// 拿到监听目标队列中的消息
		try {
			// 拿到消息队列中itemList的json形式字符串
			String itemListStr = ((TextMessage)message).getText();
			// 将json字符串重新转换为itemList
			List<TbItem> itemList = JSON.parseArray(itemListStr, TbItem.class);
			for (TbItem item : itemList) {
				// 设置动态域匹配属性的值
				// 将json格式的spec转换为Map，然后设置到specMap属性中。
				// 让其在导入索引的时候也能够导入item_spec_*的动态字段的值。
				item.setSpecMap(JSON.parseObject(item.getSpec(), Map.class));
			}
			// 将itemList导入索引库。
			itemSearchService.importList(itemList);
		} catch (JMSException e) {
			e.printStackTrace();
		}
	}
}
