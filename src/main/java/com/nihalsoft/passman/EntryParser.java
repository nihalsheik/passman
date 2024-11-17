package com.nihalsoft.passman;


import com.nihalsoft.passman.model.Entry;
import com.nihalsoft.passman.model.MetaData;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
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
		entry.setNotes(_trim(entry.getNotes(), 100));
	}

	public String getName(byte[] data) {
		return new String(Arrays.copyOfRange(data, 0, 20), StandardCharsets.UTF_8).trim();
	}

	public Entry toEntry(byte[] data, MetaData metaData) {

		try {
			if (data[0] == 0) {
				return null;
			}
			byte[] dec = AESUtil.decrypt(
					Arrays.copyOfRange(data, 20, data.length),
					metaData.getPassword(),
					metaData.getSalt(),
					metaData.getIv()
			);
			byte[] bytes = ByteBuffer.allocate(dec.length + 20)
					.put(Arrays.copyOfRange(data, 0, 20))
					.put(20, dec)
					.array();
			String str = new String(bytes, StandardCharsets.UTF_8);
			Entry e = new Entry(str.substring(0, 20).trim(),
					str.substring(20, 50).trim(),
					str.substring(50, 80).toCharArray(),
					str.substring(80).trim());
			return e;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}


	}

	public byte[] toBytes(Entry entry, MetaData metaData) {
		try {

			trim(entry);

			String str = String.format("%-30s%-30s%-100s",
					entry.getUserName(),
					new String(entry.getPassword()),
					entry.getNotes());

			byte[] enc = AESUtil.encrypt(str.getBytes(),
					metaData.getPassword(),
					metaData.getSalt(),
					metaData.getIv());

			return ByteBuffer.allocate(Entry.SIZE)
					.put(entry.getName().getBytes())
					.put(20, enc)
					.array();

		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public byte[] encryptMetaData(MetaData metaData) {
		try {
			return AESUtil.encrypt(metaData.toBytes(), metaData.getPassword());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private String _trim(String str, int limit) {
		return str.substring(0, Math.min(limit, str.length()));
	}
}
