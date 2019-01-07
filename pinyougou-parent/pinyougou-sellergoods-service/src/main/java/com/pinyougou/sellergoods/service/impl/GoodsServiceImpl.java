package com.pinyougou.sellergoods.service.impl;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pinyougou.mapper.*;
import com.pinyougou.pojo.*;
import com.pinyougou.pojo.TbGoodsExample.Criteria;
import com.pinyougou.pojogroup.Goods;
import com.pinyougou.sellergoods.service.GoodsService;
import entity.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 服务实现层
 * @author Administrator
 *
 */
@Service
@Transactional
public class GoodsServiceImpl implements GoodsService {

	@Autowired
	private TbGoodsMapper goodsMapper;
	@Autowired
	private TbGoodsDescMapper goodsDescMapper;
	@Autowired
	private TbBrandMapper brandMapper;
	@Autowired
	private TbItemCatMapper itemCatMapper;
	@Autowired
	private TbItemMapper itemMapper;
	@Autowired
	private TbSellerMapper sellerMapper;


	/**
	 * 查询全部
	 */
	@Override
	public List<TbGoods> findAll() {
		return goodsMapper.selectByExample(null);
	}

	/**
	 * 按分页查询
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);		
		Page<TbGoods> page=   (Page<TbGoods>) goodsMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 增加
	 */
	@Override
	public void add(Goods goods) {
		// 插入商品表的时候需要将商品拓展表数据也插入
		// 商品表id=商品拓展表id，一对一的关系。
		// 商品表自动生成
		TbGoods gods = goods.getGoods();
		// 设置该商品未审核
		gods.setAuditStatus("0");
		// 设置该商品已上架。新增商品默认上架
		gods.setIsMarketable("1");
		goodsMapper.insert(gods);
		TbGoodsDesc goodsDesc = goods.getGoodsDesc();
		goodsDesc.setGoodsId(gods.getId());
		goodsDescMapper.insert(goodsDesc);
		saveItemList(goods);//插入商品SKU列表数据

	}

	/*一个SPU对应着多个SKU*/
	// 启用规格
	private void saveItemList(Goods goods) {
		if("1".equals(goods.getGoods().getIsEnableSpec())){
			for(TbItem item :goods.getItemList()){
				//标题
				String title= goods.getGoods().getGoodsName();
				Map<String,Object> specMap = JSON.parseObject(item.getSpec());
				for(String key:specMap.keySet()){
					title+=" "+ specMap.get(key);
				}
				item.setTitle(title);
				setItemValus(goods,item);
				itemMapper.insert(item);
			}
		}else{ // 未启用规格，使用默认规格
			TbItem item=new TbItem();
			item.setTitle(goods.getGoods().getGoodsName());//商品KPU+规格描述串作为SKU名称
			item.setPrice( goods.getGoods().getPrice() );//价格
			item.setStatus("1");//状态
			item.setIsDefault("1");//是否默认
			item.setNum(99999);//库存数量
			item.setSpec("{}");
			setItemValus(goods,item);
			itemMapper.insert(item);
		}
	}

	private void setItemValus(Goods goods,TbItem item) {
		item.setGoodsId(goods.getGoods().getId());//商品SPU编号
		item.setSellerId(goods.getGoods().getSellerId());//商家编号
		item.setCategoryid(goods.getGoods().getCategory3Id());//商品分类编号（3级）
		item.setCreateTime(new Date());//创建日期
		item.setUpdateTime(new Date());//修改日期
		if (goods.getGoods()!= null && goods.getGoods().getBrandId() != null) {
			//品牌名称
			TbBrand brand = brandMapper.selectByPrimaryKey(goods.getGoods().getBrandId());
			if (brand != null) {
				item.setBrand(brand.getName());
			}
		}


		if (goods.getGoods()!= null && goods.getGoods().getCategory3Id() != null) {
			//分类名称
			TbItemCat itemCat = itemCatMapper.selectByPrimaryKey(goods.getGoods().getCategory3Id());
			if (itemCat != null) {
				item.setCategory(itemCat.getName());
			}
		}


		//商家名称
		if (goods.getGoods() != null && goods.getGoods().getSellerId() != null) {
			TbSeller seller = sellerMapper.selectByPrimaryKey(goods.getGoods().getSellerId());
			if (seller != null) {
				item.setSeller(seller.getNickName());
			}
		}

		//图片地址（取spu的第一个图片）
		if (goods.getGoodsDesc() != null && goods.getGoodsDesc().getItemImages() != null) {
			List<Map> imageList = JSON.parseArray(goods.getGoodsDesc().getItemImages(), Map.class) ;
			if(imageList.size()>0){
				item.setImage ( (String)imageList.get(0).get("url"));
			}
		}
	}
	/**
	 * 修改
	 */
	@Override
	public void update(Goods goods){
		//修改spu
		goodsMapper.updateByPrimaryKey(goods.getGoods());
		// 修改sup拓展
		goodsDescMapper.updateByPrimaryKey(goods.getGoodsDesc());
		// 删除原有sku，增加新的sku
		TbItemExample example = new TbItemExample();
		TbItemExample.Criteria criteria = example.createCriteria();
		criteria.andGoodsIdEqualTo(goods.getGoods().getId());
		itemMapper.deleteByExample(example);

		// 添加
		saveItemList(goods);
	}	
	
	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	@Override
	public Goods findOne(Long id){
		Goods goods = new Goods();
		goods.setGoods(goodsMapper.selectByPrimaryKey(id));
		goods.setGoodsDesc(goodsDescMapper.selectByPrimaryKey(id));
		// 查询spu 对应的sku
		TbItemExample example = new TbItemExample();
		TbItemExample.Criteria criteria = example.createCriteria();
		criteria.andGoodsIdEqualTo(id);
		goods.setItemList(itemMapper.selectByExample(example));
		return goods;
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {	
		// 关于商品的删除，只能使用商品信息某个字段表名该商品状态
		for (Long goodsId : ids) {
			TbGoods goods = new TbGoods();
			goods.setId(goodsId);
			// 设置为1表名已删除，如果是null就表名默认状态
			goods.setIsDelete("1");
			goodsMapper.updateByPrimaryKeySelective(goods);
		}
	}
	
	
		@Override
	public PageResult findPage(TbGoods goods, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		
		TbGoodsExample example=new TbGoodsExample();
		Criteria criteria = example.createCriteria();
		// 删除状态为空表明未删除,应该在Service作出判断
			// 因为无论是运营商删除商品后，运营商和商家都不应该再显示已删除商品
		criteria.andIsDeleteIsNull();
		if(goods!=null){
						// 如果是要通过商家id查询不能够模糊查询
						if(goods.getSellerId()!=null && goods.getSellerId().length()>0){
				criteria.andSellerIdLike(goods.getSellerId());
			}
			if(goods.getGoodsName()!=null && goods.getGoodsName().length()>0){
				criteria.andGoodsNameLike("%"+goods.getGoodsName()+"%");
			}
			if(goods.getAuditStatus()!=null && goods.getAuditStatus().length()>0){
				criteria.andAuditStatusLike("%"+goods.getAuditStatus()+"%");
			}
			if(goods.getIsMarketable()!=null && goods.getIsMarketable().length()>0){
				criteria.andIsMarketableLike("%"+goods.getIsMarketable()+"%");
			}
			if(goods.getCaption()!=null && goods.getCaption().length()>0){
				criteria.andCaptionLike("%"+goods.getCaption()+"%");
			}
			if(goods.getSmallPic()!=null && goods.getSmallPic().length()>0){
				criteria.andSmallPicLike("%"+goods.getSmallPic()+"%");
			}
			if(goods.getIsEnableSpec()!=null && goods.getIsEnableSpec().length()>0){
				criteria.andIsEnableSpecLike("%"+goods.getIsEnableSpec()+"%");
			}
			if(goods.getIsDelete()!=null && goods.getIsDelete().length()>0){
				criteria.andIsDeleteLike("%"+goods.getIsDelete()+"%");
			}
	
		}
		
		Page<TbGoods> page= (Page<TbGoods>)goodsMapper.selectByExample(example);		
		return new PageResult(page.getTotal(), page.getResult());
	}

	/*
	* 根据商品spu更新商品状态
	* */
	@Override
	public void updateStatus(Long[] ids, String status) {
		TbGoods goods = new TbGoods();
		for (Long goodsId : ids) {
			goods.setId(goodsId);
			goods.setAuditStatus(status);
			// 用封装了id和状态的对象
			// 会将id作为条件，更新状态信息
			goodsMapper.updateByPrimaryKeySelective(goods);
		}
	}

	/*
	* 更新商品上架状态
	* */
	@Override
	public void updateMarketable(Long[] ids, String status) {
		TbGoods goods = new TbGoods();
		for (Long goodsId : ids) {
			goods.setId(goodsId);
			goods.setIsMarketable(status);
			// 通过id更新sup对应的商品上架状态
			goodsMapper.updateByPrimaryKeySelective(goods);
		}
	}

	// 根据spu的id查询sku
	@Override
	public List<TbItem> findItemListByGoodsIdandStatus(Long[] goodsIds, String status) {
		TbItemExample example = new TbItemExample();
		TbItemExample.Criteria criteria = example.createCriteria();
		criteria.andStatusEqualTo(status); // 状态
		criteria.andGoodsIdIn(Arrays.asList(goodsIds)); // 根据spu的id查询对应的sku
		List<TbItem> itemList = itemMapper.selectByExample(example);
		return itemList;
	}
}
