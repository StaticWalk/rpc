package com.iot.utils;

/**
 * Created by xiongxiaoyu
 * Data:2018/6/23
 * Time:20:59
 */
public class StringUtil {

	/**
	 * 判断字符串是否非空
	 */
	public static boolean isNotEmpty(String str) {
		return !isEmpty(str);
	}

	/**
	 * 判断字符串是否为空
	 */
	public static boolean isEmpty(String str) {
		return str == null || str.length() == 0;
	}
}
