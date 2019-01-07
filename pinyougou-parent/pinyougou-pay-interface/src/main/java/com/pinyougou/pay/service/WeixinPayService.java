package com.pinyougou.pay.service;

import java.util.Map;

public interface WeixinPayService {

	/**
	 * @param out_trade_no 订单号，唯一。
	 * @param total_fee 支付金额。
	 * */
	Map createNative(String out_trade_no, String total_fee);

	/**
	 * 查询支付状态
	 * @param out_trade_no
	 */
	Map queryPayStatus(String out_trade_no);

	/**
	 * 关闭支付
	 * @param out_trade_no
	 * @return
	 */
	public Map closePay(String out_trade_no);
}
