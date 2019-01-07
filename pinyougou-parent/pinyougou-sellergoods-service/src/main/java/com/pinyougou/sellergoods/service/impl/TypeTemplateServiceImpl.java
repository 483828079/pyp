package com.pinyougou.sellergoods.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pinyougou.mapper.TbSpecificationOptionMapper;
import com.pinyougou.mapper.TbTypeTemplateMapper;
import com.pinyougou.pojo.TbSpecificationOption;
import com.pinyougou.pojo.TbSpecificationOptionExample;
import com.pinyougou.pojo.TbTypeTemplate;
import com.pinyougou.pojo.TbTypeTemplateExample;
import com.pinyougou.pojo.TbTypeTemplateExample.Criteria;
import com.pinyougou.sellergoods.service.TypeTemplateService;
import entity.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * 服务实现层
 *
 * @author Administrator
 */
@Service
@Transactional
public class TypeTemplateServiceImpl implements TypeTemplateService {

	@Autowired
	private TbTypeTemplateMapper typeTemplateMapper;
	@Autowired
	private RedisTemplate redisTemplate;

	/**
	 * 查询全部
	 */
	@Override
	public List<TbTypeTemplate> findAll() {
		return typeTemplateMapper.selectByExample(null);
	}

	/**
	 * 按分页查询
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		Page<TbTypeTemplate> page = (Page<TbTypeTemplate>) typeTemplateMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 增加
	 */
	@Override
	public void add(TbTypeTemplate typeTemplate) {
		typeTemplateMapper.insert(typeTemplate);
	}


	/**
	 * 修改
	 */
	@Override
	public void update(TbTypeTemplate typeTemplate) {
		typeTemplateMapper.updateByPrimaryKey(typeTemplate);
	}

	/**
	 * 根据ID获取实体
	 *
	 * @param id
	 * @return
	 */
	@Override
	public TbTypeTemplate findOne(Long id) {
		return typeTemplateMapper.selectByPrimaryKey(id);
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {
		TbTypeTemplateExample typeTemplateExample = new TbTypeTemplateExample();
		Criteria criteria = typeTemplateExample.createCriteria();
		criteria.andIdIn(Arrays.asList(ids));
		typeTemplateMapper.deleteByExample(typeTemplateExample);
	}

	// 将商品名称，商品规格和规格具体的值放入redis
	private void saveToRedis() {
		List<TbTypeTemplate> typeTemplateList = findAll();
		// 对所有模板对应商品，商品规格进行缓存。可以通过模板id获取缓存数据。
		for (TbTypeTemplate typeTemplate : typeTemplateList) {
			// 商品集合
			List<Map> brandList = JSON.parseArray(typeTemplate.getBrandIds(), Map.class);
			// 更新到缓存
			redisTemplate.boundHashOps("brandList").put(typeTemplate.getId(), brandList);

			// findSpecList根据templateId查询所有模板对应的所有的规格，规格列表信息。
			List<Map> specList = findSpecList(typeTemplate.getId());
			redisTemplate.boundHashOps("specList").put(typeTemplate.getId(), specList);
		}
	}

	@Override
	public PageResult findPage(TbTypeTemplate typeTemplate, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);

		TbTypeTemplateExample example = new TbTypeTemplateExample();
		Criteria criteria = example.createCriteria();

		if (typeTemplate != null) {
			if (typeTemplate.getName() != null && typeTemplate.getName().length() > 0) {
				criteria.andNameLike("%" + typeTemplate.getName() + "%");
			}
			if (typeTemplate.getSpecIds() != null && typeTemplate.getSpecIds().length() > 0) {
				criteria.andSpecIdsLike("%" + typeTemplate.getSpecIds() + "%");
			}
			if (typeTemplate.getBrandIds() != null && typeTemplate.getBrandIds().length() > 0) {
				criteria.andBrandIdsLike("%" + typeTemplate.getBrandIds() + "%");
			}
			if (typeTemplate.getCustomAttributeItems() != null && typeTemplate.getCustomAttributeItems().length() > 0) {
				criteria.andCustomAttributeItemsLike("%" + typeTemplate.getCustomAttributeItems() + "%");
			}

		}

		Page<TbTypeTemplate> page = (Page<TbTypeTemplate>) typeTemplateMapper.selectByExample(example);
		// 在加载页面的时候进行缓存。
		saveToRedis();
		return new PageResult(page.getTotal(), page.getResult());
	}

	@Override
	public List<Map> findTypeTemplateList() {
		return typeTemplateMapper.findTypeTemplateList();
	}

	@Autowired
	private TbSpecificationOptionMapper specificationOptionMapper;
	@Override
	public List<Map> findSpecList(Long typeId) {
		TbTypeTemplate typeTemplate = typeTemplateMapper.selectByPrimaryKey(typeId);
		// 将specIds转换为List集合,然后每个对象存放在map中,。
		// [{"id":33,"text":"电视屏幕尺寸"}]

		List<Map> specs = JSON.parseArray(typeTemplate.getSpecIds(), Map.class);
		for (Map spec : specs) {
			// 通过specId查询tb_specification_option的其他信息
			TbSpecificationOptionExample example = new TbSpecificationOptionExample();
			TbSpecificationOptionExample.Criteria criteria = example.createCriteria();
			criteria.andSpecIdEqualTo(new Long((Integer)spec.get("id")));
			List<TbSpecificationOption> options = specificationOptionMapper.selectByExample(example);
			// 这里options代表的是spec和specOptions。
			// 因为spec和specOptions是一对多，所以这里是多种组合形式。
			//[{"id":33,"text":"电视屏幕尺寸","options":"[{},{}]"}]
			spec.put("options", options);
		}
		return specs;
	}
}
