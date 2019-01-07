package com.pinyougou.manager.controller;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/login")
public class LoginController {

	@RequestMapping("/name")
	public Map name() {/*获取当前登录的用户名，然后显示到页面上*/
		String name= SecurityContextHolder.getContext()
				.getAuthentication().getName();
		Map map = new HashMap();
		map.put("loginName", name);
		return map ;
	}
}
