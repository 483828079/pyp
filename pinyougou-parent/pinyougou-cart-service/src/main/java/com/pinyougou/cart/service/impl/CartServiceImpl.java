package com.pinyougou.cart.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.cart.service.CartService;
import com.pinyougou.mapper.TbItemMapper;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.pojo.TbOrderItem;
import com.pinyougou.pojogroup.Cart;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class CartServiceImpl implements CartService {
	@Autowired
	private TbItemMapper itemMapper;
	@Autowired
	private RedisTemplate redisTemplate;

	@Override
	public List<Cart> addGoodsToCartList(List<Cart> cartList, Long itemId, Integer num) {
		// 添加购物车时提供的是商品sku的id，商品数量。
		// 购物车需要 商家id，商家name，商品明细。
		// 通过sku的id查询出sku表信息.
		TbItem item = itemMapper.selectByPrimaryKey(itemId);
		if (item == null) {
			throw new RuntimeException("该商品不存在");
		}

		if (! item.getStatus().equals("1")) {
			throw new RuntimeException("商品状态无效");
		}

		// 商家id，购物车对象的唯一标志
		String sellerId = item.getSellerId();
		Cart cart = searchCartBySellerId(cartList, sellerId);
		// 购物车列表中对应的购物车不存在，创建新的购物车对象添加到购物车列表
		if (cart == null) {
			cart = new Cart();
			// 添加购物车对象到购物车列表
			cartList.add(cart);

			// 添加商家id，商家名称
			cart.setSellerId(sellerId);
			cart.setSellerName(item.getSeller());

			// 添加商品详情，因为只有一个商品所以新建商品详情
			List<TbOrderItem> orderItemList = new ArrayList<>();
			cart.setOrderItemList(orderItemList);
			orderItemList.add(createOrderItem(item,num));

		} else { // 购物车列表中对应的购物车存在，更新购物车商品明细列表中对应商品个数和总价
			TbOrderItem orderItem = searchOrderItemByItemId(cart.getOrderItemList(), itemId);
			if (orderItem == null) { // 如果商品详情不存在，创建新的商品详情到商品详情列表
				cart.getOrderItemList().add(createOrderItem(item, num));
			} else { // 商品详情存在更新商品信息
				// 增加的个数+原来的个数=现在的个数
				orderItem.setNum(orderItem.getNum() + num);
				// 个数 * 单价 = 总价
				orderItem.setTotalFee(new BigDecimal(orderItem.getPrice().doubleValue() *  orderItem.getNum()));

				// 如果商品明细存在并且更改数量成功

				// 如果商品明细中的商品数量为0，将对应商品从商品明细列表删除。
				if (orderItem.getNum() <= 0) {
					cart.getOrderItemList().remove(orderItem); // 将商品明细从商品明细列表删除。
				}
				// 如果购物车对象中的商品为空，将购物车对象从购物车列表移除
				if (cart.getOrderItemList().size() == 0) {
					cartList.remove(cart); // 从购物车列表中删除购物车对象
				}
			}
		}

		return cartList;
	}

	// 根据sku的id匹配商品详情列表中的商品详情
	private TbOrderItem searchOrderItemByItemId(List<TbOrderItem> orderItemList, Long itemId) {
		for (TbOrderItem orderItem : orderItemList) {
			if (orderItem.getItemId().equals(itemId)) {
				return orderItem;
			}
		}

		return null;
	}

	// 根据item和商品数量生成商品详情
	private TbOrderItem createOrderItem(TbItem item, Integer num) {
		if (num <= 0) {
			throw new RuntimeException("商品数量非法");
		}

		TbOrderItem orderItem = new TbOrderItem();
		orderItem.setGoodsId(item.getGoodsId());
		orderItem.setItemId(item.getId());
		orderItem.setNum(num);
		orderItem.setPicPath(item.getImage());
		orderItem.setPrice(item.getPrice());
		orderItem.setTitle(item.getTitle());
		orderItem.setSellerId(item.getSellerId());
		// 总价：单价*商品数量
		orderItem.setTotalFee(new BigDecimal(item.getPrice().doubleValue() * num));
		return orderItem;
	}

	/**
	 * 购物车列表中是否存在某个购物车对象，返回购物车对象
	 * @param cartList 购物车列表
	 * @param sellerId 商家id，购物车对象的唯一标志
	 * @return 如果匹配到购物车对象返回购物车对象，否则返回null
	 */
	private Cart searchCartBySellerId(List<Cart> cartList, String sellerId) {
		for (Cart cart : cartList) {
			if (cart.getSellerId().equals(sellerId)) {
				return cart;
			}
		}
		return null;
	}

	@Override
	public List<Cart> findCartListFromRedis(String username) {
		List<Cart> cartList = (List<Cart>) redisTemplate.boundHashOps("cartList").get(username);
		if (cartList==null) {
			cartList = new ArrayList<>();
		}
		return cartList;
	}

	@Override
	public void saveCartListToRedis(String username, List<Cart> cartList) {
		redisTemplate.boundHashOps("cartList").put(username, cartList);
	}

	@Override
	public List<Cart> mergeCartList(List<Cart> cartList1, List<Cart> cartList2) {
		// 可以获取一个cartList的，itemid，num。添加到另一个购物车
		for (Cart cart : cartList2) {
			for (TbOrderItem orderItem : cart.getOrderItemList()) {
				cartList1 = addGoodsToCartList(cartList1, orderItem.getItemId(), orderItem.getNum());
			}
		}
		return cartList1;
	}

	@Override
	public void insertCheckOrderItemList(String username, List<Cart> cartList) {
		redisTemplate.boundHashOps("checkOrderItemList").put(username, cartList);
	}

	@Override
	public List<Cart> findCheckOrderItemList(String username) {
		return (List<Cart>) redisTemplate.boundHashOps("checkOrderItemList").get(username);
	}
}
