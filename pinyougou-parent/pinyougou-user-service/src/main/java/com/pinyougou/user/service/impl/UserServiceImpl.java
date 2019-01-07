package com.pinyougou.user.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pinyougou.common.StringUtls;
import com.pinyougou.mapper.TbAddressMapper;
import com.pinyougou.mapper.TbUserMapper;
import com.pinyougou.pojo.TbAddress;
import com.pinyougou.pojo.TbAddressExample;
import com.pinyougou.pojo.TbUser;
import com.pinyougou.pojo.TbUserExample;
import com.pinyougou.pojo.TbUserExample.Criteria;
import com.pinyougou.user.service.UserService;
import entity.PageResult;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.transaction.annotation.Transactional;

import javax.jms.*;
import java.util.*;

/**
 * 服务实现层
 * @author Administrator
 *
 */
@Service
@Transactional
public class UserServiceImpl implements UserService {

	@Autowired
	private TbUserMapper userMapper;
	@Autowired
	private RedisTemplate<String , Object> redisTemplate;
	@Autowired
	private JmsTemplate jmsTemplate;
	@Autowired
	private Destination smsDestination;
	@Autowired
	private TbAddressMapper addressMapper;

	@Value("${template_name_code}")
	private String template_name_code;
	@Value("${template_number_code}")
	private String template_number_code;

	@Value("${sign_name}")
	private String sign_name;

	/**
	 * 查询全部
	 */
	@Override
	public List<TbUser> findAll() {
		return userMapper.selectByExample(null);
	}

	/**
	 * 按分页查询
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);		
		Page<TbUser> page=   (Page<TbUser>) userMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 增加
	 */
	@Override
	public void add(final TbUser user) {
		// 注册
		user.setCreated(new Date()); // 注册时间
		user.setUpdated(new Date()); // 更新时间
		user.setSourceType("1"); // 来源
		// org.apache.commons 提供的工具类
		// 对密码进行md5加密
		user.setPassword(DigestUtils.md5Hex(user.getPassword()));
		userMapper.insert(user);

		// 注册成功后把数据库中手机号对应的key删除
		redisTemplate.boundHashOps("smscode").delete(user.getPhone());

		// 发送消息到队列
		jmsTemplate.send(smsDestination, new MessageCreator() {
			@Override
			public Message createMessage(Session session){
				// 短信微服务需要一个map。存放着四个参数。手机号，模板code，标识，模板param
				MapMessage mapMessage = null;
				try {
					mapMessage = session.createMapMessage();
					// 不同于text和object可以将发送的消息作为参数，map需要手动设置map的key和value
					mapMessage.setString("mobile", user.getPhone());//手机号
					mapMessage.setString("template_code", template_name_code);//模板编号
					mapMessage.setString("sign_name", sign_name);//签名
					// param作为json字符串
					// 有效明确做法是使用map集合key存放模板变量，value存放模板变量对应值
					// 再转换为json就是json对象
					Map m=new HashMap<>();
					m.put("name", user.getUsername());
					mapMessage.setString("param", JSON.toJSONString(m));//参数
				} catch (JMSException e) {
					e.printStackTrace();
				}

				return mapMessage;
			}
		});
	}

	
	/**
	 * 修改
	 */
	@Override
	public void update(TbUser user){
		userMapper.updateByPrimaryKey(user);
	}	
	
	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	@Override
	public TbUser findOne(Long id){
		return userMapper.selectByPrimaryKey(id);
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {	
		TbUserExample userExample = new TbUserExample();
		Criteria criteria = userExample.createCriteria();
		criteria.andIdIn(Arrays.asList(ids));
		userMapper.deleteByExample(userExample);
	}
	
	
		@Override
	public PageResult findPage(TbUser user, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		
		TbUserExample example=new TbUserExample();
		Criteria criteria = example.createCriteria();
		
		if(user!=null){			
						if(user.getUsername()!=null && user.getUsername().length()>0){
				criteria.andUsernameLike("%"+user.getUsername()+"%");
			}
			if(user.getPassword()!=null && user.getPassword().length()>0){
				criteria.andPasswordLike("%"+user.getPassword()+"%");
			}
			if(user.getPhone()!=null && user.getPhone().length()>0){
				criteria.andPhoneLike("%"+user.getPhone()+"%");
			}
			if(user.getEmail()!=null && user.getEmail().length()>0){
				criteria.andEmailLike("%"+user.getEmail()+"%");
			}
			if(user.getSourceType()!=null && user.getSourceType().length()>0){
				criteria.andSourceTypeLike("%"+user.getSourceType()+"%");
			}
			if(user.getNickName()!=null && user.getNickName().length()>0){
				criteria.andNickNameLike("%"+user.getNickName()+"%");
			}
			if(user.getName()!=null && user.getName().length()>0){
				criteria.andNameLike("%"+user.getName()+"%");
			}
			if(user.getStatus()!=null && user.getStatus().length()>0){
				criteria.andStatusLike("%"+user.getStatus()+"%");
			}
			if(user.getHeadPic()!=null && user.getHeadPic().length()>0){
				criteria.andHeadPicLike("%"+user.getHeadPic()+"%");
			}
			if(user.getQq()!=null && user.getQq().length()>0){
				criteria.andQqLike("%"+user.getQq()+"%");
			}
			if(user.getIsMobileCheck()!=null && user.getIsMobileCheck().length()>0){
				criteria.andIsMobileCheckLike("%"+user.getIsMobileCheck()+"%");
			}
			if(user.getIsEmailCheck()!=null && user.getIsEmailCheck().length()>0){
				criteria.andIsEmailCheckLike("%"+user.getIsEmailCheck()+"%");
			}
			if(user.getSex()!=null && user.getSex().length()>0){
				criteria.andSexLike("%"+user.getSex()+"%");
			}
	
		}
		
		Page<TbUser> page= (Page<TbUser>)userMapper.selectByExample(example);		
		return new PageResult(page.getTotal(), page.getResult());
	}

	// 判断手机号是否存在于数据库
	public boolean iSPhoneOnly(String phone) {
		// 发送验证码->发送验证码到手机和存储验证码和手机号到redis。
		// 输入验证码，提交表单。校验验证码和对应手机号是否存在于redis。
		// 在发送验证码之前判断手机号是否存在于数据库，因为redis将手机号作为key
		// 不管是从业务上还是功能上都要求手机号唯一
		TbUserExample example = new TbUserExample();
		Criteria criteria = example.createCriteria();
		criteria.andPhoneEqualTo(phone);
		List<TbUser> userList = userMapper.selectByExample(example);
		if (userList.size() > 0) {
			return false;
		} else {
			return true;
		}
	}

	// 生成短信验证码
	@Override
	public void createSmsCode(final String phone) {
		// 获取验证码：
		// 电话号码作为key随机生成的6位验证码作为value存储在redis。
		// 将9位验证码发送到对应手机。
		// 所以要求手机号唯一.

		// 提交：
		// 校验表单上验证码是否和redis中一致。

		// 生成6位随机数
		final String code =  (long) (Math.random()*1000000)+"";// random生成0-1之间的随机小数
		// 存入redis
		redisTemplate.boundHashOps("smscode").put(phone, code);

		// 发送消息到队列
		jmsTemplate.send(smsDestination, new MessageCreator() {
			@Override
			public Message createMessage(Session session){
				// 短信微服务需要一个map。存放着四个参数。手机号，模板code，标识，模板param
				MapMessage mapMessage = null;
				try {
					mapMessage = session.createMapMessage();
					// 不同于text和object可以将发送的消息作为参数，map需要手动设置map的key和value
					mapMessage.setString("mobile", phone);//手机号
					mapMessage.setString("template_code", template_number_code);//模板编号
					mapMessage.setString("sign_name", sign_name);//签名
					// param作为json字符串
					// 有效明确做法是使用map集合key存放模板变量，value存放模板变量对应值
					// 再转换为json就是json对象
					Map m=new HashMap<>();
					m.put("number", code);
					mapMessage.setString("param", JSON.toJSONString(m));//参数
				} catch (JMSException e) {
					e.printStackTrace();
				}

				return mapMessage;
			}
		});
	}

	@Override
	public boolean checkSmsCode(String phone, String code) {
		if (StringUtls.isEmpty(phone) || StringUtls.isEmpty(code)) {
			return false;
		}

		// 从缓存中拿出手机号对应验证码
		String cacheCode = (String) redisTemplate.boundHashOps("smscode").get(phone);
		if (cacheCode == null || !code.equals(cacheCode)) {
			return false;
		}

		// redis中验证码就是发送给用户的验证码，如果相同就是正确验证码
		return true;
	}
}
