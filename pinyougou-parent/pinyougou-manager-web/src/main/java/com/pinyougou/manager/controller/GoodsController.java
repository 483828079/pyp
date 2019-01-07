package com.pinyougou.manager.controller;
import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.pinyougou.pojo.TbGoods;
import com.pinyougou.pojogroup.Goods;
import com.pinyougou.sellergoods.service.GoodsService;
import entity.PageResult;
import entity.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import java.util.List;
/**
 * controller
 * @author Administrator
 *
 */
@RestController
@RequestMapping("/goods")
public class GoodsController {

	@Reference
	private GoodsService goodsService;
	@Autowired
	private JmsTemplate jmsTemplate;
	@Autowired
	@Qualifier("queueSolrDestination")
	private Destination queueSolrDestination;
	@Autowired
	@Qualifier("queueSolrDeleteDestination")
	private Destination queueSolrDeleteDestination;
	@Autowired
	@Qualifier("topicPageDestination")
	private Destination topicPageDestination;
	@Autowired
	@Qualifier("topicPageDeleteDestination")
	private Destination topicPageDeleteDestination;
	/**
	 * 返回全部列表
	 * @return
	 */
	@RequestMapping("/findAll")
	public List<TbGoods> findAll(){			
		return goodsService.findAll();
	}
	
	
	/**
	 * 返回全部列表
	 * @return
	 */
	@RequestMapping("/findPage")
	public PageResult  findPage(int page,int rows){			
		return goodsService.findPage(page, rows);
	}
	
	/**
	 * 增加
	 * @param goods
	 * @return
	 */
	@RequestMapping("/add")
	public Result add(@RequestBody Goods goods){
		try {
			goodsService.add(goods);
			return new Result(true, "增加成功");
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "增加失败");
		}
	}
	
	/**
	 * 修改
	 * @param goods
	 * @return
	 */
	@RequestMapping("/update")
	public Result update(@RequestBody Goods goods){
		try {
			goodsService.update(goods);
			return new Result(true, "修改成功");
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "修改失败");
		}
	}	
	
	/**
	 * 获取实体
	 * @param id
	 * @return
	 */
	@RequestMapping("/findOne")
	public Goods findOne(Long id){
		return goodsService.findOne(id);		
	}
	
	/**
	 * 批量删除
	 * @param ids
	 * @return
	 */
	@RequestMapping("/delete")
	public Result delete(final Long [] ids){
		try {
			// 删除商品后更新商品索引
			// 调用搜索服务将需要删除id对应的goods对应的item从索引库删除
			//itemSearchService.deleteByGoodsIds(Arrays.asList(ids));

			// 发送消息到队列
			// 发送消息到队列。搜索服务监听队列从队列中拿到消息，
			// 对item_goodsid等于队列中多个goodsId匹配，然后进行索引删除。
			jmsTemplate.send(queueSolrDeleteDestination, new MessageCreator() {
				@Override
				public Message createMessage(Session session) throws JMSException {
					// 使用对象类型的消息。Long是个对象并且继承了Serializable。
					// 因为是存储Long类型的数组所以不好转换为json传输。
					return session.createObjectMessage(ids);
				}
			});

			// 发送goodsIds到生成详情页服务，通过goodsIds删除对应的已经生成的goodsIds
			jmsTemplate.send(topicPageDeleteDestination, new MessageCreator() {
				@Override
				public Message createMessage(Session session) throws JMSException {
					// 发送对象类型的消息到目标
					return session.createObjectMessage(ids);
				}
			});

			goodsService.delete(ids);

			return new Result(true, "删除成功"); 
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "删除失败");
		}
	}
	
		/**
	 * 查询+分页
	 * @param goods
	 * @param page
	 * @param rows
	 * @return
	 */
	@RequestMapping("/search")
	public PageResult search(@RequestBody TbGoods goods, int page, int rows  ){
		return goodsService.findPage(goods, page, rows);		
	}

	@RequestMapping("/updateStatus")
	public Result updateStatus(Long[] ids, String status){
		try {
			goodsService.updateStatus(ids, status);
			if ("1".equals(status)) {
				// 商品审核通过时更新索引库中的索引
				// search-service 提供索引的操作
				// sellergoods-service 提供通过sup的id查询sku列表
				List itemList = goodsService.findItemListByGoodsIdandStatus(ids, status);
				// 调用搜索服务执行将审核通过的goods对应的item导入索引库
				/*if (itemList.size() > 0) {
					itemSearchService.importList(itemList);
				}*/
				// 发送消息到队列
				// 再来分析一遍这里的业务。
				// 修改spu状态，也就是审核通过后。根据goods查询item将itemList集合放入索引。
				// 使用activeMQ之后将itemList作为消息发送到队列。让SearchService监听消息。
				// 消息中itemList将其重新转化为对象再进行索引。
				if (itemList.size() > 0) {
					final String itemListStr = JSON.toJSONString(itemList);
					jmsTemplate.send(queueSolrDestination, new MessageCreator() {
						@Override
						public Message createMessage(Session session) throws JMSException {
							// 使用session创建发送的消息类型对象和设置发送的消息。
							// 这里发送的是item集合。为了方便与发送可以将其转换为json用字符串形式发送
							return session.createTextMessage(itemListStr);
						}
					});
				}
			}

			// 审核通过后生成对应商品页面。
			// 因为该权限只有管理后台能够使用，所以放在Controller中生成，
			// 而不是service。service用来处理相同业务。
			for (final Long goodsId : ids) {
				// 调用页面生成服务，通过goodsId生成对应商品详情页。
				// itemPageService.genItemHtml(goodsId);
				// 将goodsId作为消息发送，然后页面生成服务订阅队列。
				// 获取队列中的goodsId生成页面。
				jmsTemplate.send(topicPageDestination, new MessageCreator() {
					@Override
					public Message createMessage(Session session) throws JMSException {
						// 将goodsId作为文本消息发送到队列。
						return session.createTextMessage(goodsId + "");
					}
				});
			}
			return new Result(true, "状态更新成功");
		} catch (Exception e) {
			return new Result(false, "操作失败");
		}
	}

	// 通过商品id生成商品详情页。
/*	@RequestMapping("/genHtml")
	public void genHtml(Long goodsId){
		itemPageService.genItemHtml(goodsId);
	}*/
}
