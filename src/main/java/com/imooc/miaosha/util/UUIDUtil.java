package com.imooc.miaosha.util;

import java.util.UUID;

/**
 * 该方法产生UUID
 */
public class UUIDUtil {
	public static String uuid() {
		return UUID.randomUUID().toString().replace("-", "");
	}
}
