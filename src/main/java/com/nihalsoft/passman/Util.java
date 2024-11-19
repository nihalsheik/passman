package com.nihalsoft.passman;

import com.nihalsoft.passman.model.Entry;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;

public class Util {

	public static void print(String data) {
		System.out.printf("%12s%s", " ", data);
	}

	public static void println(String data) {
		System.out.printf("%12s%s%n", " ", data);
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
		System.out.printf("%13s%-18s : %s%n", " ", "Password", "******");

		String[] n = e.getNotes().split(System.lineSeparator());
		int i = 0;
		for (String s : n) {
			System.out.printf("%13s%-18s : %s%n", " ", i == 0 ? "Notes" : "", s);
			i++;
		}

		DateFormat df = new SimpleDateFormat("dd-MM-yyyy hh:mm aa");
		System.out.printf("%13s%-18s : %s%n", " ", "Updated", df.format(e.getUpdatedTime()));
		Util.println("-".repeat(50));
	}
}
