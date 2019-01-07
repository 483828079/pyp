package com.pinyougou.common;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordUtils {
	private PasswordUtils(){}

	public static String passwordEncoder (String password) {
		// 对密码进行加密
		return new BCryptPasswordEncoder().encode(password);
	}

	public static void main(String[] args) {
		System.out.println(passwordEncoder("zzz"));
	}
}
