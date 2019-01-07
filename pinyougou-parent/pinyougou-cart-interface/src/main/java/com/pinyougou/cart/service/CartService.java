package com.pinyougou.cart.service;

import com.pinyougou.pojogroup.Cart;

import java.util.List;

public interface CartService {
	/**
	 * 添加商品到购物车
	 * @param cartList 已经存在的购物车
	 * @param itemId sku id
	 * @param num sku 个数
	 * @return 添加商品后新的购物车
	 * */
	List<Cart> addGoodsToCartList(List<Cart> cartList, Long itemId, Integer num);


	/**
	 * 从redis中查询购物车
	 * @param username
	 * @return
	 */
	List<Cart> findCartListFromRedis(String username);

	/**
	 * 将购物车保存到redis
	 * @param username
	 * @param cartList
	 */
	void saveCartListToRedis(String username,List<Cart> cartList);

	/**
	 * 合并购物车列表
	 * */
	List<Cart> mergeCartList(List<Cart> cartList1,List<Cart> cartList2);

	void insertCheckOrderItemList(String username, List<Cart> cartList);

	List<Cart> findCheckOrderItemList(String username);
}
