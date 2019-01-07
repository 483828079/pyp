package com.pinyougou.page.service.impl;

import com.pinyougou.page.service.ItemPageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;

@Component("pageDeleteListener")
public class PageDeleteListener implements MessageListener {
	@Autowired
	private ItemPageService itemPageService;
	@Override
	public void onMessage(Message message) {
		try {
			Long[] goodsIds = (Long[]) ((ObjectMessage)message).getObject();
			// 在删除商品后，发送goodsIds到队列。
			// 监听到消息后将goodsIds对应的商品详情页删除。
			itemPageService.deleteItemHtml(goodsIds);
		} catch (JMSException e) {
			e.printStackTrace();
		}
	}
}
