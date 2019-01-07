package com.pinyougou.content.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pinyougou.content.service.ContentService;
import com.pinyougou.mapper.TbContentMapper;
import com.pinyougou.pojo.TbContent;
import com.pinyougou.pojo.TbContentExample;
import com.pinyougou.pojo.TbContentExample.Criteria;
import entity.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.List;

/**
 * 服务实现层
 * @author Administrator
 *
 */
@Service
public class ContentServiceImpl implements ContentService {

	@Autowired
	private TbContentMapper contentMapper;
	@Autowired
	private RedisTemplate redisTemplate;
	
	/**
	 * 查询全部
	 */
	@Override
	public List<TbContent> findAll() {
		return contentMapper.selectByExample(null);
	}

	/**
	 * 按分页查询
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);		
		Page<TbContent> page=   (Page<TbContent>) contentMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 增加
	 */
	@Override
	public void add(TbContent content) {
		// 需要在新增后清除缓存
		// 如果新增前清除缓存，有人访问首页，查询数据库放入缓存。
		// 再新增，这时候新增的数据就不再缓存中。
		contentMapper.insert(content);
		// 新增广告后清除缓存，再去查询改变的数据然后进行缓存
		// 通过删除对应的广告分类
		redisTemplate.boundHashOps("content").delete(content.getCategoryId());
	}

	
	/**
	 * 修改
	 */
	@Override
	public void update(TbContent content){
		contentMapper.updateByPrimaryKey(content);
		// 修改后清空缓存
		redisTemplate.boundHashOps("content").delete(content.getCategoryId());
		// 看修改后的分类是否改变。如果改变说明两个分类对应的广告都进行了改变
		// 清空两个分类下的广告列表
		TbContent contentO = contentMapper.selectByPrimaryKey(content.getId());
		if (! contentO.getCategoryId().equals(content.getCategoryId())) {
			redisTemplate.boundHashOps("content").delete(contentO.getCategoryId());
		}
	}

	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	@Override
	public TbContent findOne(Long id){
		return contentMapper.selectByPrimaryKey(id);
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {
		for (Long contentId : ids) {
			contentMapper.deleteByPrimaryKey(contentId);
			// 删除之后清空对应分类缓存
			TbContent content = contentMapper.selectByPrimaryKey(contentId);
			redisTemplate.boundHashOps("content").delete(content.getCategoryId());
		}
	}
	
	
	@Override
	public PageResult findPage(TbContent content, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		
		TbContentExample example=new TbContentExample();
		Criteria criteria = example.createCriteria();
		
		if(content!=null){			
						if(content.getTitle()!=null && content.getTitle().length()>0){
				criteria.andTitleLike("%"+content.getTitle()+"%");
			}
			if(content.getUrl()!=null && content.getUrl().length()>0){
				criteria.andUrlLike("%"+content.getUrl()+"%");
			}
			if(content.getPic()!=null && content.getPic().length()>0){
				criteria.andPicLike("%"+content.getPic()+"%");
			}
			if(content.getStatus()!=null && content.getStatus().length()>0){
				criteria.andStatusLike("%"+content.getStatus()+"%");
			}
	
		}
		
		Page<TbContent> page= (Page<TbContent>)contentMapper.selectByExample(example);		
		return new PageResult(page.getTotal(), page.getResult());
	}

	@Override
	public void updateStatus(Long[] ids, String status) {
		TbContent content = new TbContent();
		for (Long contentId : ids) {
			content.setId(contentId);
			content.setStatus(status);
			contentMapper.updateByPrimaryKeySelective(content);

			// 修改后清空当前分类的缓存
			TbContent tbContent = contentMapper.selectByPrimaryKey(contentId);
			redisTemplate.boundHashOps("content").delete(tbContent.getCategoryId());
		}
	}

	@Override
	public List<TbContent> findByCategoryId(Long categoryId) {
		// 因为图片信息在首页展示，所以访问量会比较大
		// 所以使用redis进行缓存，如果是第一次访问查询数据库，以后访问直接从redis取数据
		// 使用hash来存储，key为content，filed为分类id，对应的value就是广告列表
		List<TbContent> contentList = (List<TbContent>) redisTemplate.boundHashOps("content").get(categoryId);

		// 如果key不存在返回null，如果filed或者对应的值不存在返回null
		if (contentList != null) { // 存在直接从缓存中获取
			return contentList;
		}

		TbContentExample example = new TbContentExample();
		// 设置排序方式,`index` ASC,id ASC
		// 按照index先排序，如果index相同就按照id排序
		// 这里按照sort_order倒序排序
		example.setOrderByClause("sort_order DESC");
		Criteria criteria = example.createCriteria();
		criteria.andCategoryIdEqualTo(categoryId);
		// 广告的状态必须是开启
		criteria.andStatusEqualTo("1");
		contentList = contentMapper.selectByExample(example);
		// 如果不存在于缓存之中，查询之后放入缓存
		redisTemplate.boundHashOps("content").put(categoryId, contentList);
		return contentList;
	}
}
