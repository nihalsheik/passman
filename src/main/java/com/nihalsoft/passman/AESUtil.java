package com.nihalsoft.passman;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Base64;

public class AESUtil {

	private static final String AES_ALGORITHM = "AES/GCM/NoPadding";
	private static final int KEY_LENGTH = 128;
	private static final int ITERATION_COUNT = 10000;
	private static final int TAG_LENGTH = 128;

	public static String encryptToString(byte[] value, char[] password) throws Exception {
		return Base64.getEncoder().encodeToString(encrypt(value, password));
	}

	public static byte[] encrypt(byte[] value, char[] password) throws Exception {
		Cipher cipher = Cipher.getInstance("AES");
		cipher.init(Cipher.ENCRYPT_MODE, getKey(password));
		return cipher.doFinal(value);
	}

	public static String decryptToString(byte[] value, char[] password) throws Exception {
		byte[] k = Base64.getDecoder().decode(value);
		byte[] n = decrypt(k, password);
		return n == null ? "" : new String(n, StandardCharsets.UTF_8);
	}

	public static byte[] decrypt(byte[] value, char[] password) throws Exception {
		Cipher cipher = Cipher.getInstance("AES");
		cipher.init(Cipher.DECRYPT_MODE, getKey(password));
		return cipher.doFinal(value);
	}

	public static String encryptToString(byte[] value, char[] password, byte[] salt, byte[] iv) throws Exception {
		return Base64.getEncoder().encodeToString(encrypt(value, password, salt, iv));
	}

	public static byte[] encrypt(byte[] value, char[] password, byte[] salt, byte[] iv) throws Exception {
		SecretKey secretKey = getKey(password, salt);
		Cipher cipher = Cipher.getInstance(AES_ALGORITHM);
		cipher.init(Cipher.ENCRYPT_MODE, secretKey, new GCMParameterSpec(TAG_LENGTH, iv));
		return cipher.doFinal(value);

	}

	public static byte[] decrypt(byte[] value, char[] password, byte[] salt, byte[] iv) throws Exception {
		SecretKey secretKey = getKey(password, salt);
		Cipher cipher = Cipher.getInstance(AES_ALGORITHM);
		cipher.init(Cipher.DECRYPT_MODE, secretKey, new GCMParameterSpec(TAG_LENGTH, iv));
		return cipher.doFinal(value);
	}

	public static SecretKey getKey(char[] password, byte[] salt) throws NoSuchAlgorithmException, InvalidKeySpecException {
		SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
		KeySpec spec = new PBEKeySpec(password, salt, ITERATION_COUNT, KEY_LENGTH);
		return new SecretKeySpec(factory.generateSecret(spec).getEncoded(), "AES");
	}

	public static SecretKey getKey(char[] password) throws NoSuchAlgorithmException, InvalidKeySpecException {
		byte[] b = new byte[16];
		System.arraycopy(new String(password).getBytes(StandardCharsets.UTF_8), 0, b, 0, password.length);
		return new SecretKeySpec(b, "AES");
	}

}
