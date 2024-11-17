package com.nihalsoft.passman.model;

import java.nio.ByteBuffer;
import java.security.SecureRandom;

public class MetaData {

	private final char[] password;
	private final byte[] salt;
	private final byte[] iv;

	public static final int SIZE = 1024;

	public MetaData(char[] password) {

		this.password = password;

		var secRand = new SecureRandom();
		this.salt = new byte[16];
		secRand.nextBytes(salt);

		this.iv = new byte[12];
		secRand.nextBytes(iv);
	}

	public MetaData(char[] password, byte[] salt, byte[] iv) {
		this.password = password;
		this.salt = salt;
		this.iv = iv;
	}

	public char[] getPassword() {
		return password;
	}

	public byte[] getSalt() {
		return salt;
	}

	public byte[] getIv() {
		return iv;
	}

	public byte[] toBytes() {
		return ByteBuffer
				.allocate(MetaData.SIZE - 1)
				.put(salt)
				.put(iv)
				.array();
	}

}
