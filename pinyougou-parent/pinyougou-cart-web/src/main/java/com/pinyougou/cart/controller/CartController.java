package com.pinyougou.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.pinyougou.cart.service.CartService;
import com.pinyougou.common.CookieUtil;
import com.pinyougou.common.StringUtls;
import com.pinyougou.pojogroup.Cart;
import entity.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/cart")
public class CartController {
	@Reference
	private CartService cartService;

	@Autowired
	private HttpServletRequest req;

	@Autowired
	private HttpServletResponse resp;

	@RequestMapping("/findCartList")
	public List<Cart> findCartList() { // 获取购物车列表
		String name = SecurityContextHolder.getContext().getAuthentication().getName();
		if ("anonymousUser".equals(name)) { // 未登录，从cookie中获取cartList
			// 因为只能获取cookie列表，该工具类是遍历cookie，根据cookieName匹配对应cookie
			// 将cookieValue以utf-8进行url解码。
			String cartListStr = CookieUtil.getCookieValue(req, "cartList", "UTF-8");
			if (StringUtls.isEmpty(cartListStr)) {
				cartListStr = "[]"; // 如果cartList不存在于cookie就返回一个空的集合。
			}

			// 以List集合形式转换，适用于[]包裹的json。[]会被转换为空集合。
			return JSON.parseArray(cartListStr, Cart.class);
		} else {
			// 如果是登录状态，将cookie中的cartList合并在redis中。
			// 并且删除cookie中的cartList，不然会造成多次合并。
			// 合并的时机：
			// 1. 登录之后将cookie中的cartList合并在redis中。
			   // 这种方式只会在登录的时候执行一次合并操作。
			// 2. 每次访问cartList如果是登录状态将cookie中的cartList合并在redis。
			  // 但是效率较为低.可以判断cookie中的cartList是否存在，存在才进行合并。
			  // 采用第一种方式，因为第二种不够优雅。
			return cartService.findCartListFromRedis(name); // 已登录从redis中获取cartList
		}
	}

	// 使用CORS如果不设置响应头，不会响应。
	@RequestMapping("/addGoodsToCartList")  // 使用注解设置
	@CrossOrigin(origins="http://localhost:5001"/*,allowCredentials="true"*/)
	public Result addGoodsToCartList(Long itemId, Integer num){ // 添加商品到购物车
		// 允许某个域来使用该资源,可以使用*代表所有域
		/*resp.setHeader("Access-Control-Allow-Origin", "http://localhost:5001");*/
		// 允许发送cookie和http认证信息，Access-Control-Allow-Origin不能设置为*。
		// 因为cookie是绑定某个域名的。
		/*resp.setHeader("Access-Control-Allow-Credentials", "true");*/
		String name = SecurityContextHolder.getContext().getAuthentication().getName();
		// 如果是登录状态去redis中获取购物车列表，如果是未登录状态去cookie中获取购物车列表
		List<Cart> cartList = cartService.addGoodsToCartList(findCartList(), itemId, num);
		try {
			if ("anonymousUser".equals(name)) { // 未登录，将cartList存储在cookie中
				// 购物车列表以json字符串形式存储在cookie中。key为cartList。
				// 从cookie中获取购物车列表,将当前商品信息添加到购物车列表
				// 将更新后的购物车列表重新放入cookie中。
				CookieUtil.setCookie(req, resp, "cartList", JSON.toJSONString(cartList), 3600 * 24, "UTF-8");
			} else { // 已登录，使用新的cookieList覆盖掉原来的cookieList
				cartService.saveCartListToRedis(name, cartList);
			}
			return new Result(true, "添加商品到购物车成功");
		} catch (Exception e) {
			return new Result(false, "添加商品到购物车失败," + e.getMessage() + "。");
		}
	}

	@RequestMapping("/mergerCartList")
	public Result mergerCartList() {
		try {
			// 从cookie获取cartList
			String cartListStr = CookieUtil.getCookieValue(req, "cartList", "UTF-8");
			// 如果cookie中存在cartList才进行合并
			if (cartListStr != null) {
				List<Cart> cookie_cartList = JSON.parseArray(cartListStr, Cart.class);
				String username = SecurityContextHolder.getContext().getAuthentication().getName();
				List<Cart> redis_cartList = cartService.findCartListFromRedis(username);
				// 合并购物车
				List<Cart> cartList = cartService.mergeCartList(redis_cartList, cookie_cartList);
				// 使用新的cartList替换掉redis中原来的购物车
				cartService.saveCartListToRedis(username, cartList);
				// 从cookie中删除购物车,其实是设置cookie的存活时间为-1
				CookieUtil.deleteCookie(req, resp, "cartList");
				return new Result(true, "合并购物车成功");
			} else {
				return new Result(true, "cookie中并不存在购物车");
			}
		} catch (Exception e) {
			return new Result(false, "合并购物车失败");
		}
	}

	@RequestMapping("/insertCheckOrderItemList")
	public Result insertCheckOrderItemList (Long itemId, Integer num) {
		try {
			String username = SecurityContextHolder.getContext().getAuthentication().getName();
			List<Cart> checkOrderItemList = findCheckOrderItemList();
			if (checkOrderItemList == null) {
				checkOrderItemList = new ArrayList<>();
			}
			cartService.insertCheckOrderItemList(username, cartService.addGoodsToCartList(checkOrderItemList, itemId, num));
			return new Result(true, "选中的商品存入redis中成功");
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "选中的商品存入redis失败");
		}
	}

	@RequestMapping("/findCheckOrderItemList")
	public List<Cart> findCheckOrderItemList() {
		String username = SecurityContextHolder.getContext().getAuthentication().getName();
		return cartService.findCheckOrderItemList(username);
	}
}
