package com.baima;

import com.aliyuncs.exceptions.ClientException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class SmsListener {
	@Autowired
	private SmsUtil smsUtil;

	@JmsListener(destination = "sms") // 监听queue模式名称为sms的队列。
	public void sendSms(Map<String,String> messageMap){ // 队列中消息的类型为map
		try { // 监听队列中的消息，队列中有消息之后就使用它来发送短信。所谓的短信微服务。
			// 这里的短信可以有两种发送验证码和注册成功。
			// 模板不同，param模板参数不同。模板参数为json类型。
			// 这里是个通用的短信服务，只要提供手机号，签名，模板名称，模板参数就能发送短信。
			// 只是用来从队列中拿到消息。开启状态就可以。默认是queue模式的队列。
			smsUtil.sendSms(messageMap.get("mobile"),
					messageMap.get("template_code"),
					messageMap.get("sign_name"),
					messageMap.get("param"));
		} catch (ClientException e) {
			e.printStackTrace();
		}
	}
}
