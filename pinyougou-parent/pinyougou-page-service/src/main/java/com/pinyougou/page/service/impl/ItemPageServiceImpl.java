package com.pinyougou.page.service.impl;

import com.pinyougou.mapper.TbGoodsDescMapper;
import com.pinyougou.mapper.TbGoodsMapper;
import com.pinyougou.mapper.TbItemCatMapper;
import com.pinyougou.mapper.TbItemMapper;
import com.pinyougou.page.service.ItemPageService;
import com.pinyougou.pojo.TbGoods;
import com.pinyougou.pojo.TbGoodsDesc;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.pojo.TbItemExample;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfig;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ItemPageServiceImpl implements ItemPageService {

	@Value("${pagedir}")
	private String pagedir;
	@Autowired
	private FreeMarkerConfig freeMarkerConfig;
	@Autowired
	private TbGoodsMapper goodsMapper;
	@Autowired
	private TbGoodsDescMapper goodsDescMapper;
	@Autowired
	private TbItemCatMapper itemCatMapper;
	@Autowired
	private TbItemMapper itemMapper;

	/*模板自己提供，根据指定goodsId生成商品列表到某个位置。
	*
	* */
	@Override
	public boolean genItemHtml(Long goodsId) {// 根据商品id生成商品详情页
		Configuration configuration = freeMarkerConfig.getConfiguration();
		try {											/*bean中设置模板所在文件夹，这里只用写文件名*/
			Template template = configuration.getTemplate("item.ftl");
			Map map = new HashMap();
			TbGoods goods = goodsMapper.selectByPrimaryKey(goodsId);
			map.put("goods", goods);
			TbGoodsDesc goodsDesc = goodsDescMapper.selectByPrimaryKey(goodsId);
			map.put("goodsDesc", goodsDesc);
			String itemCat1 = itemCatMapper.selectByPrimaryKey(goods.getCategory1Id()).getName();
			String itemCat2 = itemCatMapper.selectByPrimaryKey(goods.getCategory2Id()).getName();
			String itemCat3 = itemCatMapper.selectByPrimaryKey(goods.getCategory3Id()).getName();
			map.put("itemCat1", itemCat1);
			map.put("itemCat2", itemCat2);
			map.put("itemCat3", itemCat3);
			// sku
			TbItemExample example = new TbItemExample();
			// 按照默认sku进行倒序排序，默认的sku会排在前面
			example.setOrderByClause("is_default DESC");
			TbItemExample.Criteria criteria = example.createCriteria();
			// 查询spu对应的sku
			criteria.andGoodsIdEqualTo(goodsId);
			// 状态必须为有效
			criteria.andStatusEqualTo("1");
			List<TbItem> itemList = itemMapper.selectByExample(example);
			map.put("itemList", itemList);
			// 指定详情页输出的位置。
			String dir = pagedir + goodsId + ".html";
			// 使用转换流，将编码设置为utf-8
			OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(dir), "UTF-8");
			PrintWriter printWriter = new PrintWriter(writer);
			template.process(map, printWriter);
			printWriter.close();
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	@Override
	public boolean deleteItemHtml(Long[] goodsIds) {
		try {
			for (Long goodsId : goodsIds) {
				// 生成商品详情页的具体地址
				String dir = pagedir + goodsId + ".html";
				// 从磁盘上物理删除该文件
				new File(dir).delete();
			}
			return true; // 删除成功
		} catch (Exception e) {
			return false; // 删除失败
		}

	}
}
