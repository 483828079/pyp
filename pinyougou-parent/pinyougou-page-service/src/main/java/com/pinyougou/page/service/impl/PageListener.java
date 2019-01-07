package com.pinyougou.page.service.impl;

import com.pinyougou.page.service.ItemPageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

@Component("pageListener")
public class PageListener implements MessageListener {
	@Autowired
	private ItemPageService itemPageService;
	@Override
	public void onMessage(Message message) {
		try {
			String goodsIdStr = ((TextMessage)message).getText();
			// 监听队列，从队列中拿到goodsId，通过goodsId生成商品详情页。
			itemPageService.genItemHtml(Long.parseLong(goodsIdStr));
		} catch (JMSException e) {
			e.printStackTrace();
		}
	}
}
