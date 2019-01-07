package com.pinyougou.task;

import com.pinyougou.mapper.TbSeckillGoodsMapper;
import com.pinyougou.pojo.TbSeckillGoods;
import com.pinyougou.pojo.TbSeckillGoodsExample;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Component
public class SeckillTask {
	@Autowired
	private RedisTemplate redisTemplate;
	@Autowired
	private TbSeckillGoodsMapper seckillGoodsMapper;
	/**
	 * 刷新秒杀商品
	 */
	@Scheduled(cron="0 * * * * ?")
	public void refreshSeckillGoods(){
		// 对秒杀商品的更新。会发现之前做的秒杀没有秒杀商品更新。
		// 第一次加载页面的时候将mysql中的数据存放于redis中。
		// 直到销售完毕才针对该商品更新。
		// 如果有商家提交了秒杀商品申请，并且运营商通过并不能出现在页面上。
		// 使用springTask每分钟更新到redis中mysql中审核通过
		// 活动时间未过期(活动时间过期也应该重新回到mysql中)
		// 并且不存在于redis中的商品。

		//查询存在于redis中所有秒杀商品id
		List ids = new ArrayList( redisTemplate.boundHashOps("seckillGoods").keys());
		// 查询所有审核通过，数量大于0，活动时间未过期，不存在于redis中的秒杀商品
		TbSeckillGoodsExample example = new TbSeckillGoodsExample();
		TbSeckillGoodsExample.Criteria criteria = example.createCriteria();
		criteria.andStatusEqualTo("1"); // 审核通过
		criteria.andNumGreaterThan(0); // 秒杀商品数量大于0
		criteria.andStartTimeLessThan(new Date());// 开始时间小于当前时间
		criteria.andEndTimeGreaterThan(new Date());// 结束时间大于当前时间
		criteria.andIdNotIn(ids); // 不存在于redis
		List<TbSeckillGoods> seckillGoodsList = seckillGoodsMapper.selectByExample(example);
		// 放入缓存
		for (TbSeckillGoods seckillGoods : seckillGoodsList) {
			redisTemplate.boundHashOps("seckillGoods").put(seckillGoods.getId(), seckillGoods);
		}
	}

	/**
	 * 移除秒杀商品
	 */
	@Scheduled(cron="* * * * * ?")
	public void removeSeckillGoods(){
		// 之前做的也是没有移除过期商品。前端页面根据redis存放的结束时间对比当前时间倒计时。
		// 只是在提交订单的时候做了判断，如果过期不能够创建订单。
		// 实际上如果过期应该不让该商品显示在页面上，那就是将商品从redis中移除。
		// 查询存在于redis中所有秒杀商品id
		List<TbSeckillGoods> seckillGoodsList = (List<TbSeckillGoods>) redisTemplate.boundHashOps("seckillGoods");
		for (TbSeckillGoods seckillGoods : seckillGoodsList) {
			// 如果结束时间大于当前时间就是该商品秒杀活动到期
			if (seckillGoods.getEndTime().getTime() > new Date().getTime()) {
				// 从redis中移除该商品
				redisTemplate.boundHashOps("seckillGoods").delete(seckillGoods.getId());
			}
		}

	}

	public static void main(String[] args) {
		ApplicationContext app = new ClassPathXmlApplicationContext("classpath*:spring/applicationContext-*.xml");
		try {
			System.in.read();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}