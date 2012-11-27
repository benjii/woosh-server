package com.luminos.woosh.security;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * 
 * @author Ben
 */
public class Md5PasswordEncoder {

	/**
	 * 
	 * @param password
	 * @return
	 */
	public static final String hashPassword(String password) {
		try {
			MessageDigest md5 = MessageDigest.getInstance("MD5");
			
			byte[] data = password.getBytes();
			md5.update(data, 0, data.length);
			
			BigInteger hash = new BigInteger(1, md5.digest());
			
			return String.format("%1$032X", hash).toLowerCase();
			
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
	}

}
