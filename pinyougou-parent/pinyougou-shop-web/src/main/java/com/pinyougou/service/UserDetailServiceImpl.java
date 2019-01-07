package com.pinyougou.service;

import com.pinyougou.pojo.TbSeller;
import com.pinyougou.sellergoods.service.SellerService;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.ArrayList;
import java.util.List;

/*@Service*/
public class UserDetailServiceImpl implements UserDetailsService {
	/*@Reference*/
	private SellerService sellerService;

	public void setSellerService(SellerService sellerService) {
		this.sellerService = sellerService;
	}

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

		List<GrantedAuthority> grantedAuths = new ArrayList<GrantedAuthority>();
		/*添加权限*/
		grantedAuths.add(new SimpleGrantedAuthority("ROLE_SELLER"));
		// 返回用户名，密码，和权限集合。
		// 提供username，自己去查询password和状态信息。
		// 并查询username对应的权限。
		// 全部符合条件才能够通过验证
		TbSeller seller = sellerService.findOne(username);

		// 自己需要判断的是用户名对应的用户是否存在
		// 用户状态是否开启
		if (seller == null || !"1".equals(seller.getStatus())) {
			return null;
		}

		return new User(username, seller.getPassword(), grantedAuths);
	}
}
