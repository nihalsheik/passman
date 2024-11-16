package com.nihalsoft.passman;

import com.nihalsoft.passman.model.Entry;

public class Util {

	public static void print(String data) {
		System.out.printf("%11s%s", " ", data);
	}

	public static void println(String data) {
		System.out.printf("%11s%s%n", " ", data);
	}

	public static String padRight(String s, int n) {
		return String.format("%-" + n + "s", s);
	}

	public static String padLeft(String s, int n) {
		return String.format("%" + n + "s", s);
	}

	public static void displayEntry(Entry e) {
		Util.println("-".repeat(50));
		System.out.printf("%13s%-18s : %s%n", " ", "Name", e.getName());
		System.out.printf("%13s%-18s : %s%n", " ", "Username", e.getUserName());
		System.out.printf("%13s%-18s : %s%n", " ", "Password", new String(e.getPassword()));
		System.out.printf("%13s%-18s : %s%n", " ", "Notes", e.getNotes());
		Util.println("-".repeat(50));
	}
}
