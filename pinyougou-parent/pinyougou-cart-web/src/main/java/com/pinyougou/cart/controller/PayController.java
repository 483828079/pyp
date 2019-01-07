package com.pinyougou.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.order.service.OrderService;
import com.pinyougou.pay.service.WeixinPayService;
import com.pinyougou.pojo.TbPayLog;
import entity.Result;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/pay")
public class PayController {
	@Reference
	private WeixinPayService weixinPayService;
	@Reference
	private OrderService orderService;

	@RequestMapping("/createNative")
	public Map createNative() {
		String username = SecurityContextHolder.getContext().getAuthentication().getName();
		TbPayLog payLog = orderService.searchPayLogFromRedis(username);
		// 如果redis中的payLog存在，调用微信服务获取支付连接，总金额，订单号。
		if (payLog != null) {
			return weixinPayService.createNative(payLog.getOutTradeNo(),payLog.getTotalFee() + "");
		} else {
			// 直接返回一个空的集合，不能够显示支付二维码，订单号，总金额。
			return new HashMap();
		}
	}

	/**
	 * 根据订单号查询支付状态
	 * */
	@RequestMapping("/queryPayStatus")
	public Result queryPayStatus(String out_trade_no){
		Map<String, String> map = weixinPayService.queryPayStatus(out_trade_no);
		int sum = 0;
		while (true) {
			if (sum >= 100) {
				return new  Result(false, "二维码超时");
			}

			if (map == null) {
				return new Result(false, "支付出错。");
				// 状态为SUCCESS表明支付成功
			} else if ("SUCCESS".equals(map.get("return_code"))) {
				//修改订单状态
				orderService.updateOrderStatus(out_trade_no, map.get("transaction_id"));
				return new  Result(true, "支付成功");
			}

			try {
				// 3s循环一次。
				Thread.sleep(3000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			sum ++;
		}
	}
}

