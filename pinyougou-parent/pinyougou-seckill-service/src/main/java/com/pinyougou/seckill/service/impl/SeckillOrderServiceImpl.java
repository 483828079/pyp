package com.pinyougou.seckill.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pinyougou.common.IdWorker;
import com.pinyougou.mapper.TbSeckillGoodsMapper;
import com.pinyougou.mapper.TbSeckillOrderMapper;
import com.pinyougou.pojo.TbSeckillGoods;
import com.pinyougou.pojo.TbSeckillOrder;
import com.pinyougou.pojo.TbSeckillOrderExample;
import com.pinyougou.pojo.TbSeckillOrderExample.Criteria;
import com.pinyougou.seckill.service.SeckillOrderService;
import entity.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * 服务实现层
 * @author Administrator
 *
 */
@Service
public class SeckillOrderServiceImpl implements SeckillOrderService {

	@Autowired
	private TbSeckillOrderMapper seckillOrderMapper;
	@Autowired
	private RedisTemplate redisTemplate;
	@Autowired
	private TbSeckillGoodsMapper seckillGoodsMapper;
	@Autowired
	private IdWorker idWorker;
	
	/**
	 * 查询全部
	 */
	@Override
	public List<TbSeckillOrder> findAll() {
		return seckillOrderMapper.selectByExample(null);
	}

	/**
	 * 按分页查询
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);		
		Page<TbSeckillOrder> page=   (Page<TbSeckillOrder>) seckillOrderMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 增加
	 */
	@Override
	public void add(TbSeckillOrder seckillOrder) {
		seckillOrderMapper.insert(seckillOrder);		
	}

	
	/**
	 * 修改
	 */
	@Override
	public void update(TbSeckillOrder seckillOrder){
		seckillOrderMapper.updateByPrimaryKey(seckillOrder);
	}	
	
	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	@Override
	public TbSeckillOrder findOne(Long id){
		return seckillOrderMapper.selectByPrimaryKey(id);
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {	
		TbSeckillOrderExample seckillOrderExample = new TbSeckillOrderExample();
		Criteria criteria = seckillOrderExample.createCriteria();
		criteria.andIdIn(Arrays.asList(ids));
		seckillOrderMapper.deleteByExample(seckillOrderExample);
	}
	
	
		@Override
	public PageResult findPage(TbSeckillOrder seckillOrder, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		
		TbSeckillOrderExample example=new TbSeckillOrderExample();
		Criteria criteria = example.createCriteria();
		
		if(seckillOrder!=null){			
						if(seckillOrder.getUserId()!=null && seckillOrder.getUserId().length()>0){
				criteria.andUserIdLike("%"+seckillOrder.getUserId()+"%");
			}
			if(seckillOrder.getSellerId()!=null && seckillOrder.getSellerId().length()>0){
				criteria.andSellerIdLike("%"+seckillOrder.getSellerId()+"%");
			}
			if(seckillOrder.getStatus()!=null && seckillOrder.getStatus().length()>0){
				criteria.andStatusLike("%"+seckillOrder.getStatus()+"%");
			}
			if(seckillOrder.getReceiverAddress()!=null && seckillOrder.getReceiverAddress().length()>0){
				criteria.andReceiverAddressLike("%"+seckillOrder.getReceiverAddress()+"%");
			}
			if(seckillOrder.getReceiverMobile()!=null && seckillOrder.getReceiverMobile().length()>0){
				criteria.andReceiverMobileLike("%"+seckillOrder.getReceiverMobile()+"%");
			}
			if(seckillOrder.getReceiver()!=null && seckillOrder.getReceiver().length()>0){
				criteria.andReceiverLike("%"+seckillOrder.getReceiver()+"%");
			}
			if(seckillOrder.getTransactionId()!=null && seckillOrder.getTransactionId().length()>0){
				criteria.andTransactionIdLike("%"+seckillOrder.getTransactionId()+"%");
			}
	
		}
		
		Page<TbSeckillOrder> page= (Page<TbSeckillOrder>)seckillOrderMapper.selectByExample(example);		
		return new PageResult(page.getTotal(), page.getResult());
	}
	@Override
	public void submitOrder(Long seckillId, String userId) {
		// 对于秒杀商品的时间处理。。
		// 这里用的方式不是那么的难。首先前端使用定时器计时结束时间-当前时间。
		// 后端，因为信息存在于redis中。时间到期或者商品被抢购完才会持久化数据库。
		// 所以在提交订单的时候判断当前时间是否大于商品到期时间，如果大于说明活动结束。
		// 但是可能有多个人提交订单。。每次都会持久化数据库一次。。所以这里只用判断是否过期。
		// 如果进行持久化有些不合理了，如果过期提示。
		// 等到商品数量为0的时候持久化。。但是如果活动到期数量也没为0不就没更新数据库信息吗。。。
		// 哎嘿嘿，想到了。如果活动时间结束或者数量为0的时候持久化redis信息到数据库。
		// 然后删除redis中商品信息。那么持久化之前可以先判断redis中是否存在该商品信息，如果存在持久化。
		// 不存在说明活动到期或者商品数量为0,已经删除过了,这次只用提示不用持久化。

		// 所以关于商品列表到订单详情再到提交订单的分析
		// 将商品列表存入redis为了缓解压力。之后的订单详情用的就是redis中的商品信息。抢购后跟新商品
		// 数量用的也是redis。
		// 本质是将mysql中的表信息临时拿到redis中，等使用完毕后将更新后的数据重新同步到mysql中。
		// 持久化的时机就是该商品不用在秒杀这个业务中使用了。也就是秒杀活动结束，或者商品数量为0.


		//从缓存中查询秒杀商品
		TbSeckillGoods seckillGoods =(TbSeckillGoods) redisTemplate.boundHashOps("seckillGoods").get(seckillId);
		if(seckillGoods==null){
			throw new RuntimeException("商品不存在");
		}

		/* 	用increment控制库存*/
		Long sellNum = redisTemplate.opsForValue().increment("seckillGoods_"+seckillId, 1);
		//此处是getNum，不能是stockCount，因为它会减少  10个商品如果此处用getStockCount只能卖出5个
		if(sellNum.intValue() > seckillGoods.getNum().intValue()){
			seckillGoodsMapper.updateByPrimaryKey(seckillGoods);//同步到数据库
			redisTemplate.boundHashOps("seckillGoods").delete(seckillId);
			throw new RuntimeException("商品已抢购一空");
		}

		if (seckillGoods.getEndTime().getTime() <= new Date().getTime()) {
			throw new RuntimeException("抢购已经结束");
		}

		// 此出因列表页要显示库存不能注释
		seckillGoods.setStockCount(seckillGoods.getStockCount() - 1);
		redisTemplate.boundHashOps("seckillGoods").put(seckillId, seckillGoods);// 放回缓存

		// 保存（redis）订单, 保存订单到redis。等到支付完成持久化到数据库。
		// 如果能够成功对商品作出处理，生成商品对应的订单到redis中。
		long orderId = idWorker.nextId();
		TbSeckillOrder seckillOrder = new TbSeckillOrder();
		seckillOrder.setId(orderId);
		seckillOrder.setCreateTime(new Date());
		seckillOrder.setMoney(seckillGoods.getCostPrice());// 秒杀价格
		seckillOrder.setSeckillId(seckillId);
		seckillOrder.setSellerId(seckillGoods.getSellerId());
		seckillOrder.setUserId(userId);// 设置用户ID
		seckillOrder.setStatus("0");// 状态，未支付。
		// 一个用户只能有一个秒杀的订单。
		redisTemplate.boundHashOps("seckillOrder").put(userId, seckillOrder);
	}

	@Override
	public TbSeckillOrder searchOrderFromRedisByUserId(String userId) {
		return (TbSeckillOrder) redisTemplate.boundHashOps("seckillOrder").get(userId);
	}

	@Override
	public void saveOrderFromRedisToDb(String userId, Long orderId, String transactionId) {
		//根据用户ID查询日志
		TbSeckillOrder seckillOrder = (TbSeckillOrder) redisTemplate.boundHashOps("seckillOrder").get(userId);
		if(seckillOrder==null){
			throw new RuntimeException("订单不存在");
		}
		//如果与传递过来的订单号不符
		if(seckillOrder.getId().longValue()!=orderId.longValue()){
			throw new RuntimeException("订单不相符");
		}
		// 如果支付成功才会将订单实例化。其他时间订单都在redis中。
		seckillOrder.setTransactionId(transactionId);//交易流水号
		seckillOrder.setPayTime(new Date());//支付时间
		seckillOrder.setStatus("1");//状态
		seckillOrderMapper.insert(seckillOrder);//保存到数据库
		redisTemplate.boundHashOps("seckillOrder").delete(userId);//从redis中清除
	}

	@Override
	public void deleteOrderFromRedis(String userId, Long orderId) {
		//根据用户ID查询日志
		TbSeckillOrder seckillOrder = (TbSeckillOrder) redisTemplate.boundHashOps("seckillOrder").get(userId);
		if(seckillOrder!=null &&
				seckillOrder.getId().longValue()== orderId.longValue() ){
			redisTemplate.boundHashOps("seckillOrder").delete(userId);//删除缓存中的订单
			//恢复库存
			//1.从缓存中提取秒杀商品
			TbSeckillGoods seckillGoods=(TbSeckillGoods)redisTemplate.boundHashOps("seckillGoods").get(seckillOrder.getSeckillId());
			if(seckillGoods!=null){
				seckillGoods.setStockCount(seckillGoods.getStockCount()+1);
				redisTemplate.boundHashOps("seckillGoods").put(seckillOrder.getSeckillId(), seckillGoods);//存入缓存
			}
		}
	}


}
