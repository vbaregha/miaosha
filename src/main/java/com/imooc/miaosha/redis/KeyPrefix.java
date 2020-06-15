package com.imooc.miaosha.redis;

public interface KeyPrefix {


	public int expireSeconds();

	/**
	 * 获取redis key 值的拼接
	 * @return
	 */
	public String getPrefix();
	
}
