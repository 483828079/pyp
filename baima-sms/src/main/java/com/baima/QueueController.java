package com.baima;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsMessagingTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class QueueController {
	@Autowired
	private JmsMessagingTemplate jmsMessagingTemplate;

	@RequestMapping("/sendsms")
	public void sendSms(){
		Map map=new HashMap<>();
		map.put("mobile", "111111111");
		map.put("template_code", "SMS_125018877");
		map.put("sign_name", "K品优购");
		map.put("param", "{\"name\":\"小孔\"}");
		jmsMessagingTemplate.convertAndSend("sms",map);
	}
}
