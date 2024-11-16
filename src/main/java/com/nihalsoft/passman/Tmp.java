package com.nihalsoft.passman;

import java.util.ArrayList;
import java.util.List;

public class Tmp {


	public static void fileTest() {
		List<String> list = new ArrayList<>();
		list.add("a");
		list.add("b");
		list.add("c");
		list.add("");
		int rec = 0;

		for (String s : list) {
			if (s.isEmpty())
				break;
			rec++;
		}

		if(rec == list.size())
			return;

		for (int i = rec + 1; i < list.size(); i++) {
			String s = list.get(i);
			if (s.isEmpty())
				continue;

			list.set(rec, s);
			list.set(i, "");
			rec++;
		}

		System.out.println(rec);
		System.out.println(list);

	}

	public static void main(String[] args) throws Exception {
		fileTest();
//		char[] pwd = "123".toCharArray();
//		String text = "1234567890123456";
//		var md = new MetaData(pwd);
//		byte[] enc = AESUtil.encrypt(text.getBytes(), md.getPassword(), md.getSalt(), md.getIv());
//		System.out.println(enc.length);

//		DataStore.getInstance().load("f", "1".toCharArray());
//		for (int i = 0; i < 1000; i++) {
//			DataStore.getInstance().addEntry(new Entry("name" + i, "u", "p".toCharArray(), ""));
//			System.out.println(i);
//		}
//		DataStore.getInstance().purge();
	}


}
