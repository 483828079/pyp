package com.pinyougou.search.service.impl;

import com.pinyougou.search.service.ItemSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import java.util.Arrays;

@Component("itemDeleteListener")
public class ItemDeleteListener implements MessageListener {
	@Autowired
	private ItemSearchService itemSearchService;
	@Override
	public void onMessage(Message message) {
		try {
			Long[] goodsIds = (Long[]) ((ObjectMessage)message).getObject();
			itemSearchService.deleteByGoodsIds(Arrays.asList(goodsIds));
		} catch (JMSException e) {
			e.printStackTrace();
		}
	}
}
