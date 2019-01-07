package com.pinyougou.pay.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.wxpay.sdk.WXPayUtil;
import com.pinyougou.common.HttpClient;
import com.pinyougou.pay.service.WeixinPayService;
import org.springframework.beans.factory.annotation.Value;

import java.util.HashMap;
import java.util.Map;

@Service(timeout = 10000)
public class WeixinPayServiceImpl implements WeixinPayService {
	@Value("${appid}")
	private String appid; // 公众账号ID

	@Value("${partner}")
	private String partner; // 商户号

	@Value("${partnerkey}")
	private String partnerkey; // 商户秘钥

	@Override
	public Map createNative(String out_trade_no, String total_fee) {
		// 创建参数
		Map<String, String> param = new HashMap<>();
		param.put("appid", appid); // 公众号ID
		param.put("mch_id", partner); // 商户号
		param.put("nonce_str", WXPayUtil.generateNonceStr()); // 生成随机字符串。用来混淆生成的签名。
		param.put("body", "品优购"); // 商品描述
		param.put("total_fee", total_fee); // 标价金额
		param.put("out_trade_no", out_trade_no); // 商户订单号
		param.put("spbill_create_ip", "127.0.0.1"); // ip地址
		param.put("notify_url", "http://www.4399.com");// 回调地址。第二种方式用不到回调地址，但是必须写。
		param.put("trade_type", "NATIVE"); // 交易方式，扫码支付。

		try {
			// 生成要发送的xml
			String paramXml = WXPayUtil.generateSignedXml(param, partnerkey);// 会同时生成签名。
			// 设置请求地址
			HttpClient httpClient = new HttpClient("https://api.mch.weixin.qq.com/pay/unifiedorder");
			httpClient.setHttps(true); // 允许https协议
			httpClient.setXmlParam(paramXml); // 设置请求的xml
			httpClient.post(); // post请求
			String content = httpClient.getContent(); // 获取响应的字符串类型的xml
			Map<String, String> resultMap = new HashMap<>();
			// 设置响应回来的支付地址。用来生成二维码。
			resultMap.put("code_url", WXPayUtil.xmlToMap(content).get("code_url"));
			resultMap.put("out_trade_no", out_trade_no); // 商户订单
			resultMap.put("total_fee", total_fee); // 总金额
			return resultMap;
		} catch (Exception e) {
			e.printStackTrace();
			return new HashMap(); // 如果失败返回一个空的map
		}
	}

	/**
	 * 查询订单状态。看有没有对生成的支付链接进行付款。
	 * 因为生成支付链接需要提供订单号，对于同一个商户订单号唯一。
	 * */
	@Override
	public Map queryPayStatus(String out_trade_no) {
		Map param = new HashMap();
		param.put("appid", appid); // 商户ID
		param.put("mch_id", partner); // 商户号
		param.put("out_trade_no", out_trade_no); // 商户订单号
		param.put("nonce_str", WXPayUtil.generateNonceStr()); // 随机字符串
		try {
			// 将map转换为xml并且生成签名
			String xmlParam = WXPayUtil.generateSignedXml(param, partnerkey);
			HttpClient httpClient = new HttpClient("https://api.mch.weixin.qq.com/pay/orderquery");
			httpClient.setHttps(true);
			httpClient.setXmlParam(xmlParam);
			httpClient.post();
			Map<String, String> resultMap = WXPayUtil.xmlToMap(httpClient.getContent());
			return resultMap;
		} catch (Exception e) {
			e.printStackTrace();
			return new HashMap();
		}
	}

	@Override
	public Map closePay(String out_trade_no) {
		Map param=new HashMap();
		param.put("appid", appid);//公众账号ID
		param.put("mch_id", partner);//商户号
		param.put("out_trade_no", out_trade_no);//订单号
		param.put("nonce_str", WXPayUtil.generateNonceStr());//随机字符串
		String url="https://api.mch.weixin.qq.com/pay/closeorder";
		try {
			String xmlParam = WXPayUtil.generateSignedXml(param, partnerkey);
			HttpClient client=new HttpClient(url);
			client.setHttps(true);
			client.setXmlParam(xmlParam);
			client.post();
			String result = client.getContent();
			Map<String, String> map = WXPayUtil.xmlToMap(result);
			System.out.println(map);
			return map;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
}
