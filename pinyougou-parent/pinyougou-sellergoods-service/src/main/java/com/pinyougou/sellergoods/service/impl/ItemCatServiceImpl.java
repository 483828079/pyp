package com.pinyougou.sellergoods.service.impl;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pinyougou.mapper.TbItemCatMapper;
import com.pinyougou.pojo.TbItemCat;
import com.pinyougou.pojo.TbItemCatExample;
import com.pinyougou.pojo.TbItemCatExample.Criteria;
import com.pinyougou.sellergoods.service.ItemCatService;
import entity.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 服务实现层
 * @author Administrator
 *
 */
@Service
@Transactional
public class ItemCatServiceImpl implements ItemCatService {

	@Autowired
	private TbItemCatMapper itemCatMapper;
	@Autowired
	private RedisTemplate redisTemplate;
	
	/**
	 * 查询全部
	 */
	@Override
	public List<TbItemCat> findAll() {
		return itemCatMapper.selectByExample(null);
	}

	/**
	 * 按分页查询
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);		
		Page<TbItemCat> page=   (Page<TbItemCat>) itemCatMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 增加
	 */
	@Override
	public void add(TbItemCat itemCat) {
		itemCatMapper.insert(itemCat);
	}

	
	/**
	 * 修改
	 */
	@Override
	public void update(TbItemCat itemCat){
		/*只修改要修改的*/
		itemCatMapper.updateByPrimaryKeySelective(itemCat);
	}	
	
	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	@Override
	public TbItemCat findOne(Long id){
		return itemCatMapper.selectByPrimaryKey(id);
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {
		for (Long itemCatId : ids) {
			TbItemCatExample example = new TbItemCatExample();
			Criteria criteria = example.createCriteria();
			criteria.andParentIdEqualTo(itemCatId);
			List<TbItemCat> itemCatList = itemCatMapper.selectByExample(example);
			if (itemCatList.size() > 0) {
				throw new RuntimeException("不能够删除有子集目录的商品分类");
			}
		}

		for (Long itemCatId : ids) {
			/*删除当前id对应数据*/
			itemCatMapper.deleteByPrimaryKey(itemCatId);
		}
	}
	
	
		@Override
	public PageResult findPage(TbItemCat itemCat, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		
		TbItemCatExample example=new TbItemCatExample();
		Criteria criteria = example.createCriteria();
		
		if(itemCat!=null){			
						if(itemCat.getName()!=null && itemCat.getName().length()>0){
				criteria.andNameLike("%"+itemCat.getName()+"%");
			}
	
		}
		
		Page<TbItemCat> page= (Page<TbItemCat>)itemCatMapper.selectByExample(example);		
		return new PageResult(page.getTotal(), page.getResult());
	}

	@Override
	public List<TbItemCat> findByParentId(Long parentId) {
		TbItemCatExample example = new TbItemCatExample();
		Criteria criteria = example.createCriteria();
		criteria.andParentIdEqualTo(parentId);
		List<TbItemCat> itemCatList = itemCatMapper.selectByExample(example);
		/*if (itemCatList.size() == 0) {
			throw new RuntimeException("没有下级目录");
		}*/

		// 每次加载分类页面的时候更新缓存到数据库
		saveToRedis();
		return itemCatList;
	}

	private void saveToRedis() {
		// 为什么要使用到缓存？
		// 因为最多能够从redis中通过分组获取到itemCat的Name。
		// 不能够直接获取品牌和规格。
		// 所以需要将所有itemCat的name和typeId放入缓存
		// 而通过分组查询出来的分类名称可以获取到typeId。
		// 再将所有的品牌和规格以及规格的具体值也放入缓存
		// typeId作为key。
		// 最后也就是可以通过分类名称获取到分类名称对应的品牌和规格信息。
		// 而使用redis是因为在查询页有大量的访问这时候放到redis更加方便。

		// 每次查询的时候更新redis中的缓存
		// 因为增删改之后都会调用查询，重新将改变的信息显示到页面上
		// 而itemCat使用的是findByParentId(0),将一级分类显示到页面上
		// 将所有的分类的name作为id，模板id作为value。
		// 为了能够使用solr中分组的分类名称获取到对应模板id
		// 再通过模板id，能够获取到商品信息，商品规格信息
		List<TbItemCat> itemCats = findAll();
		for (TbItemCat itemCat : itemCats) {
			// 将所有的itemCat的name和typeId放入redis缓存中。
			redisTemplate.boundHashOps("itemCat").put(itemCat.getName(), itemCat.getTypeId());
		}
	}
}
