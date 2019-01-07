package com.pinyougou.pojogroup;

import com.pinyougou.pojo.TbGoods;
import com.pinyougou.pojo.TbGoodsDesc;
import com.pinyougou.pojo.TbItem;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Goods implements Serializable {
	private static final long serialVersionUID = 2668830850567377853L;
	private TbGoods goods;//商品SPU
	private TbGoodsDesc goodsDesc;//商品扩展
	private List<TbItem> itemList;//商品SKU列表

	public TbGoods getGoods() {
		return goods;
	}

	public void setGoods(TbGoods goods) {
		this.goods = goods;
	}

	public TbGoodsDesc getGoodsDesc() {
		return goodsDesc;
	}

	public void setGoodsDesc(TbGoodsDesc goodsDesc) {
		this.goodsDesc = goodsDesc;
	}

	public List<TbItem> getItemList() {
		return itemList;
	}

	public void setItemList(List<TbItem> itemList) {
		this.itemList = itemList;
	}

	@Override
	public String toString() {
		Map map = new HashMap();
		map.put("id", 27);
		map.put("text", "网络");
		map.put("options", "网络");
		return "Goods{" +
				"goods=" + goods +
				", goodsDesc=" + goodsDesc +
				", itemList=" + itemList +
				'}';

	}
}
