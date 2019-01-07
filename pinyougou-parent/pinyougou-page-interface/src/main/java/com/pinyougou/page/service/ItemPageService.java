package com.pinyougou.page.service;

public interface ItemPageService {
	/**
	 * 通过商品id生成商品详情页
	 * @return 如果生成成功返回true，生成失败返回false。
	 * */
	boolean genItemHtml(Long goodsId);

	/**
	 * 通过商品id删除已经生成的详情页
	 * */
	boolean deleteItemHtml(Long[] goodsIds);
}
