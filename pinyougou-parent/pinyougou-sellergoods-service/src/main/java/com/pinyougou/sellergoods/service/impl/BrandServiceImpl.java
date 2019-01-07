package com.pinyougou.sellergoods.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pinyougou.common.StringUtls;
import com.pinyougou.mapper.TbBrandMapper;
import com.pinyougou.pojo.TbBrand;
import com.pinyougou.pojo.TbBrandExample;
import com.pinyougou.sellergoods.service.BrandService;
import entity.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class BrandServiceImpl implements BrandService {
	@Autowired
	TbBrandMapper brandMapper;

	@Override
	public List<TbBrand> findAll() {
		// 按条件查询如果不写条件，就去查所有的表
		return brandMapper.selectByExample(null);
	}

	@Override
	public PageResult findPage(TbBrand brand, Integer page, Integer pageSize) {
		TbBrandExample example = new TbBrandExample();
		TbBrandExample.Criteria criteria = example.createCriteria();

		if (brand != null) {
			if (!StringUtls.isEmpty(brand.getName())) {
				criteria.andNameLike("%"+brand.getName()+"%");
			}

			if (!StringUtls.isEmpty(brand.getFirstChar())) {
				criteria.andFirstCharLike("%"+brand.getFirstChar()+"%");
			}
		}

		PageHelper.startPage(page, pageSize);// 开启分页查询
		// page封装了分页查询的一些信息
		Page<TbBrand> pageInfo = (Page<TbBrand>) brandMapper.selectByExample(example);
		// 将总页数和当前页数据作为返回值
		return new PageResult(pageInfo.getTotal(),pageInfo.getResult());
	}

	@Override
	public void add(TbBrand brand) {
		brandMapper.insert(brand);
	}

	@Override
	public void update(TbBrand brand) {
		// 根据id进行修改
		brandMapper.updateByPrimaryKey(brand);
	}

	@Override
	public TbBrand findOne(Long id) {
		return brandMapper.selectByPrimaryKey(id);
	}

	@Override
	public boolean isNameInBrandNames(String name) {
		TbBrandExample brandExample = new TbBrandExample();
		TbBrandExample.Criteria criteria = brandExample.createCriteria();
		criteria.andNameEqualTo(name);
		// 无论什么条件查询到的只会是List,逆向工程只能查询整张表不能查询某个字段,如果查询不到就是空的List
		List<TbBrand> tbBrands = brandMapper.selectByExample(brandExample);
		if (tbBrands.size() > 0) {
			return true;
		}
		return false;
	}

	@Override
	public void deleteByIds(Long[] ids) {
		TbBrandExample brandExample = new TbBrandExample();
		TbBrandExample.Criteria criteria = brandExample.createCriteria();
		criteria.andIdIn(Arrays.asList(ids));
		brandMapper.deleteByExample(brandExample);
	}

	@Override
	public List<Map> selectOptionList() {
		return brandMapper.selectOptionList();
	}
}
