package com.pinyougou.pojogroup;

import com.pinyougou.pojo.TbOrderItem;

import java.io.Serializable;
import java.util.List;

/**
 * 购物车对象，购物车列表里的一个实体。
 * */
public class Cart implements Serializable {
	private static final long serialVersionUID = -4845564704299074702L;

	// 一个购物车对象对应着一个商家，多一个购物车对象组成一个购物车列表。
	private String sellerId;//商家ID，标志购物车对象唯一。
	private String sellerName;//商家名称，用来显示到页面。
	// 一个购物车对象拥有多个同一个商家的商品
	private List<TbOrderItem> orderItemList;//购物车明细，用来显示到页面。

	public String getSellerId() {
		return sellerId;
	}

	public void setSellerId(String sellerId) {
		this.sellerId = sellerId;
	}

	public String getSellerName() {
		return sellerName;
	}

	public void setSellerName(String sellerName) {
		this.sellerName = sellerName;
	}

	public List<TbOrderItem> getOrderItemList() {
		return orderItemList;
	}

	public void setOrderItemList(List<TbOrderItem> orderItemList) {
		this.orderItemList = orderItemList;
	}

	@Override
	public String toString() {
		return "Cart{" +
				"sellerId='" + sellerId + '\'' +
				", sellerName='" + sellerName + '\'' +
				", orderItemList=" + orderItemList +
				'}';
	}
}
