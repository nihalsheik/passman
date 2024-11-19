package com.nihalsoft.passman.model;

import java.nio.ByteBuffer;
import java.security.SecureRandom;

public class MetaData {

	private final char[] password;
	private int securityPin = 0;
	private final byte[] salt;
	private final byte[] iv;

	public static final int SIZE = 1024;

	public MetaData(char[] password, int securityPin) {

		this.password = password;
		this.securityPin = securityPin;

		var secRand = new SecureRandom();
		this.salt = new byte[16];
		secRand.nextBytes(salt);

		this.iv = new byte[12];
		secRand.nextBytes(iv);
	}

	public MetaData(char[] password, int securityPin, byte[] salt, byte[] iv) {
		this.password = password;
		this.salt = salt;
		this.iv = iv;
		this.securityPin = securityPin;
	}

	public char[] getPassword() {
		return password;
	}

	public int getSecurityPin() {
		return securityPin;
	}

	public void setSecurityPin(int securityPin) {
		this.securityPin = securityPin;
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
