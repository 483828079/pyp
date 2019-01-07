package com.pinyougou.sellergoods.service;

import com.pinyougou.pojo.TbBrand;
import entity.PageResult;

import java.util.List;
import java.util.Map;

public interface BrandService {
	List<TbBrand> findAll();

	PageResult findPage(TbBrand brand, Integer page, Integer pageSize);

	void add(TbBrand brand);

	void update(TbBrand brand);

	TbBrand findOne(Long id);

	boolean isNameInBrandNames(String name);

	void deleteByIds(Long[] ids);

	List<Map> selectOptionList();
}
