package com.pinyougou.seckill.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pinyougou.mapper.TbSeckillGoodsMapper;
import com.pinyougou.pojo.TbSeckillGoods;
import com.pinyougou.pojo.TbSeckillGoodsExample;
import com.pinyougou.pojo.TbSeckillGoodsExample.Criteria;
import com.pinyougou.seckill.service.SeckillGoodsService;
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
public class SeckillGoodsServiceImpl implements SeckillGoodsService {

	@Autowired
	private TbSeckillGoodsMapper seckillGoodsMapper;
	@Autowired
	private RedisTemplate redisTemplate;
	
	/**
	 * 查询全部
	 */
	@Override
	public List<TbSeckillGoods> findAll() {
		return seckillGoodsMapper.selectByExample(null);
	}

	/**
	 * 按分页查询
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		Page<TbSeckillGoods> page=   (Page<TbSeckillGoods>) seckillGoodsMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 增加
	 */
	@Override
	public void add(TbSeckillGoods seckillGoods) {
		seckillGoodsMapper.insert(seckillGoods);		
	}

	
	/**
	 * 修改
	 */
	@Override
	public void update(TbSeckillGoods seckillGoods){
		seckillGoodsMapper.updateByPrimaryKey(seckillGoods);
	}	
	
	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	@Override
	public TbSeckillGoods findOne(Long id){
		return seckillGoodsMapper.selectByPrimaryKey(id);
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {	
		TbSeckillGoodsExample seckillGoodsExample = new TbSeckillGoodsExample();
		Criteria criteria = seckillGoodsExample.createCriteria();
		criteria.andIdIn(Arrays.asList(ids));
		seckillGoodsMapper.deleteByExample(seckillGoodsExample);
	}
	
	
		@Override
	public PageResult findPage(TbSeckillGoods seckillGoods, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		
		TbSeckillGoodsExample example=new TbSeckillGoodsExample();
		Criteria criteria = example.createCriteria();
		
		if(seckillGoods!=null){			
						if(seckillGoods.getTitle()!=null && seckillGoods.getTitle().length()>0){
				criteria.andTitleLike("%"+seckillGoods.getTitle()+"%");
			}
			if(seckillGoods.getSmallPic()!=null && seckillGoods.getSmallPic().length()>0){
				criteria.andSmallPicLike("%"+seckillGoods.getSmallPic()+"%");
			}
			if(seckillGoods.getSellerId()!=null && seckillGoods.getSellerId().length()>0){
				criteria.andSellerIdLike("%"+seckillGoods.getSellerId()+"%");
			}
			if(seckillGoods.getStatus()!=null && seckillGoods.getStatus().length()>0){
				criteria.andStatusLike("%"+seckillGoods.getStatus()+"%");
			}
			if(seckillGoods.getIntroduction()!=null && seckillGoods.getIntroduction().length()>0){
				criteria.andIntroductionLike("%"+seckillGoods.getIntroduction()+"%");
			}
	
		}
		
		Page<TbSeckillGoods> page= (Page<TbSeckillGoods>)seckillGoodsMapper.selectByExample(example);
		return new PageResult(page.getTotal(), page.getResult());
	}


	// 查询还在秒杀中的商品列表。
	// 活动开始时间->活动结束时间内。商品数量>0。商品状态审核通过。
	@Override
	public List<TbSeckillGoods> findList() {
		// 秒杀商品需要放入缓存中缓解压力，等到商品秒杀时间结束，或者商品数量为0
		// 清除缓存并且实例化到数据库。

		// 将key对应的所有goodsList取出。
		List seckillGoodList = redisTemplate.boundHashOps("seckillGoods").values();
		// 如果缓存中不存在秒杀商品，查询数据库。
		if (seckillGoodList.size() == 0) {
			TbSeckillGoodsExample example = new TbSeckillGoodsExample();
			Criteria criteria = example.createCriteria();
			criteria.andStatusEqualTo("1"); // 状态开启。
			criteria.andNumGreaterThan(0); // 商品数量大于0
			criteria.andStartTimeLessThan(new Date()); // 开始时间小于当前时间
			criteria.andEndTimeGreaterThan(new Date()); // 结束时间大于当前时间
			List<TbSeckillGoods> tbSeckillGoods = seckillGoodsMapper.selectByExample(example);
			// 依次将goods放入缓存，filed就是goodsId。因为具有唯一性。
			for (TbSeckillGoods tbSeckillGood : tbSeckillGoods) {
				redisTemplate.boundHashOps("seckillGoods").put(tbSeckillGood.getId(), tbSeckillGood);
			}
			return tbSeckillGoods;
		}

		return seckillGoodList;

	}

	@Override
	public TbSeckillGoods findOneFromRedis(Long id) {
		return (TbSeckillGoods) redisTemplate.boundHashOps("seckillGoods").get(id);
	}

}
