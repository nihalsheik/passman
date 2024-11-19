package com.nihalsoft.passman;


import com.nihalsoft.passman.model.Entry;
import com.nihalsoft.passman.model.MetaData;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;

public class EntryParser {

	private static volatile EntryParser INSTANCE = null;

	private EntryParser() {
	}

	public static EntryParser getInstance() {
		if (INSTANCE == null) {
			synchronized (EntryParser.class) {
				if (INSTANCE == null) {
					INSTANCE = new EntryParser();
				}
			}
		}
		return INSTANCE;
	}

	public void trim(Entry entry) {
		entry.setName(_trim(entry.getName(), 20));
		entry.setUserName(_trim(entry.getUserName(), 30));
		entry.setPassword(_trim(new String(entry.getPassword()), 30).toCharArray());
		entry.setNotes(_trim(entry.getNotes(), 150));
	}

	public boolean isDeleted(byte[] bytes) {
		return bytes[0] == 0;
	}

	public String getName(byte[] data) {
		return new String(Arrays.copyOfRange(data, 0, 20), StandardCharsets.UTF_8).trim();
	}

	public String getUpdatedTime(byte[] data) {
		ByteBuffer bb = ByteBuffer.wrap(Arrays.copyOfRange(data, 20, 28));
		DateFormat df = new SimpleDateFormat("dd-MM-yyyy hh:mm aa");
		return df.format(bb.getLong());
	}

	private String decryptPassword(byte[] data, MetaData metaData) throws Exception {

		byte[] dec = AESUtil.decrypt(
				Arrays.copyOfRange(data, 28, 74),
				getCombinedPassword(metaData.getPassword(), metaData.getSecurityPin()),
				metaData.getSalt(),
				metaData.getIv()
		);

		return new String(dec, StandardCharsets.UTF_8).trim();
	}

	private byte[] encryptPassword(char[] password, MetaData metaData) throws Exception {
		// 30 + 16 = 46 bytes
		return AESUtil.encrypt(
				ByteBuffer.allocate(30).put(new String(password).getBytes()).array(),
				getCombinedPassword(metaData.getPassword(), metaData.getSecurityPin()),
				metaData.getSalt(),
				metaData.getIv()
		);
	}

	public Entry toEntry(byte[] data, MetaData metaData) {
		return toEntry(data, metaData, false);
	}

	public Entry toEntry(byte[] data, MetaData metaData, boolean includePassword) {

		try {
			if (data[0] == 0) {
				return null;
			}
			byte[] dec = AESUtil.decrypt(
					Arrays.copyOfRange(data, 74, data.length),
					metaData.getPassword(),
					metaData.getSalt(),
					metaData.getIv()
			);

			byte[] bytes = ByteBuffer.allocate(dec.length + 20)
					.put(Arrays.copyOfRange(data, 0, 20))
					.put(20, dec)
					.array();

			String pwd = includePassword ? decryptPassword(data, metaData) : "";
			String str = new String(bytes, StandardCharsets.UTF_8);
			Entry e = new Entry(str.substring(0, 20).trim(),
					str.substring(20, 50).trim(),
					pwd.toCharArray(),
					str.substring(50, 150).trim());

			ByteBuffer bb = ByteBuffer.wrap(Arrays.copyOfRange(data, 20, 28));
			e.setUpdatedTime(bb.getLong());

			return e;

		} catch (Exception e) {
			throw new RuntimeException(e);
		}


	}

	public byte[] toBytes(Entry entry, MetaData metaData) {
		try {

			trim(entry);

			byte[] pwd = encryptPassword(entry.getPassword(), metaData);

			byte[] data = ByteBuffer.allocate(180)
					.put(entry.getUserName().getBytes())
					.put(30, entry.getNotes().getBytes())
					.array();

			// 30 + 150 + 16 = 196 bytes
			byte[] enc = AESUtil.encrypt(data,
					metaData.getPassword(),
					metaData.getSalt(),
					metaData.getIv()
			);

			// 20 + 46 + 196 = 262
			return ByteBuffer.allocate(Entry.SIZE)
					.put(entry.getName().getBytes())
					.putLong(20, entry.getUpdatedTime())
					.put(28, pwd)
					.put(74, enc)
					.array();

		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public byte[] encryptMetaData(MetaData metaData) {
		try {
			return AESUtil.encrypt(metaData.toBytes(), getCombinedPassword(metaData.getPassword(), metaData.getSecurityPin()));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public MetaData decryptMetaData(byte[] metaDataBytes, char[] password, int securityPin) throws Exception {
		byte[] dec = AESUtil.decrypt(metaDataBytes, getCombinedPassword(password, securityPin));
		return new MetaData(password, securityPin,
				Arrays.copyOfRange(dec, 0, 16),
				Arrays.copyOfRange(dec, 16, 28));
	}

	private String _trim(String str, int limit) {
		return str.substring(0, Math.min(limit, str.length()));
	}

	private char[] getCombinedPassword(char[] password, int securityPin) {
		StringBuilder sb = new StringBuilder();
		sb.append(password);
		sb.append(securityPin);
		return sb.toString().toCharArray();
	}
}
