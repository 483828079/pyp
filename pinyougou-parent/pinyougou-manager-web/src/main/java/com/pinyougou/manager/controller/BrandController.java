package com.pinyougou.manager.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.pojo.TbBrand;
import com.pinyougou.sellergoods.service.BrandService;
import entity.PageResult;
import entity.Result;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;

@Controller
@ResponseBody
@RequestMapping("/brand")
public class BrandController {
	@Reference(timeout = 5000) /*超时时间*/
	private BrandService brandService;

	@RequestMapping("/findAll")
	public List<TbBrand> findAll(){
		return brandService.findAll();
	}

	@RequestMapping("/findPage")
	public PageResult findPage(@RequestBody TbBrand brand, Integer page, Integer pageSize) {
		return brandService.findPage(brand, page, pageSize);
	}

	@RequestMapping("/add")
	public Result add(@RequestBody TbBrand brand) {
		if (brandService.isNameInBrandNames(brand.getName())) {
			return new Result(false, "添加失败，品牌已存在");
		}

		try {
			brandService.add(brand);
			return new Result(true, "添加成功");
 		} catch (Exception e) {
			return new Result(false, "添加失败");
		}
	}

	@RequestMapping("/findOne")
	public TbBrand findOne(Long id) {
		return brandService.findOne(id);
	}

	@RequestMapping("/update")
	public Result update(@RequestBody TbBrand brand) {
		if (!brandService.findOne(brand.getId()).getName().equals(brand.getName()) && brandService.isNameInBrandNames(brand.getName())) {
			return new Result(false, "修改失败，品牌已存在");
		}

		try {
			brandService.update(brand);
			return new Result(true, "修改成功");
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "修改失败");
		}
	}

	@RequestMapping("/delete")
	public Result delete(Long[] ids) {
		try {
			brandService.deleteByIds(ids);
			return new Result(true, "删除成功");
		} catch (Exception e) {
			return new Result(false, "删除失败");
		}
	}

	@RequestMapping("/selectOptionList")
	public List<Map> selectOptionList(){
		return brandService.selectOptionList();
	}
}
