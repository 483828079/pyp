package com.pinyougou.common;

public class StringUtls {
	public static boolean isEmpty(String str) {
		return str == null || "".equals(str.trim());
	}
}
