package com.imooc.miaosha.util;

import org.apache.commons.codec.digest.DigestUtils;

public class MD5Util {
//	MD5产生函数
	public static String md5(String src) {
		return DigestUtils.md5Hex(src);
	}
//	固定颜值
	private static final String salt = "1a2b3c4d";

//	将输入的密码进行第一次MD5
	public static String inputPassToFormPass(String inputPass) {
		String str = ""+salt.charAt(0)+salt.charAt(2) + inputPass +salt.charAt(5) + salt.charAt(4);
		System.out.println(str);
		return md5(str);
	}
//	进行第二次MD5
	public static String formPassToDBPass(String formPass, String salt) {
		String str = ""+salt.charAt(0)+salt.charAt(2) + formPass +salt.charAt(5) + salt.charAt(4);
		return md5(str);
	}
	
	public static String inputPassToDbPass(String inputPass, String saltDB) {
		String formPass = inputPassToFormPass(inputPass);
		String dbPass = formPassToDBPass(formPass, saltDB);
		return dbPass;
	}
	
}
