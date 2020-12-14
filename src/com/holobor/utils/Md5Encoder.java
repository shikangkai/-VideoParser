package com.holobor.utils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Md5Encoder {

	public static String genFileMd5(String filePath) {
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			InputStream is = Files.newInputStream(Paths.get(filePath));
			byte[] buffer = new byte[1024 * 1024 * 4];
			int numRead;
			while ((numRead = is.read(buffer)) != -1) {
				md.update(buffer, 0, numRead);
			}
			byte[] digest = md.digest();
			is.close();
			String result = "";
		    for (int i=0; i < digest.length; i++) {
		    	result += Integer.toString((digest[i] & 0xff) + 0x100, 16).substring(1);
		    }
		    return result;
			
		} catch (IOException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}

		return null;
	}
}
