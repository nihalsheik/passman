package com.nihalsoft.passman.task;

import com.nihalsoft.passman.Command;
import com.nihalsoft.passman.Util;
import com.nihalsoft.passman.data.DataStore;
import com.nihalsoft.passman.model.Entry;

import java.util.List;
import java.util.Map;

public class GetTask implements Task {

	@Override
	public void exec(Command cmd, String[] args) throws Exception {
		switch (cmd) {
			case GET:
				get(args);
				break;
			case GETPASS:
				showPass(args);
				break;
			case LIST:
				list();
				break;
		}
	}

	private void list() {
		List<Map<String, Object>> list = DataStore.getInstance().getEntryList();
		if (list.isEmpty()) {
			Util.println("No entries found");
			return;
		}
		Util.println("-".repeat(44));
		Util.println(Util.padRight(" Name", 24) + "Date");
		Util.println("-".repeat(44));
		int i = 1;
		for (Map<String, Object> e : list) {
			Util.println(" " + Util.padRight(e.get("name").toString(), 23) + e.get("updatedTime"));
			i++;
		}
		Util.println("-".repeat(44));
		Util.println("Total entries : " + list.size());
	}

	private void get(String[] args) throws Exception {
		validateEntryName(args);
		Entry e = DataStore.getInstance().getEntry(args[0]);
		Util.displayEntry(e);
	}

	private void showPass(String[] args) throws Exception {
		validateEntryName(args);
		char[] sPin = System.console().readPassword("%11s%-18s : ", " ", "Security Pin");
		if (sPin.length == 0) {
			throw new RuntimeException("Empty pin, doesn't allow");
		}
		int pin = Integer.parseInt(new String(sPin));
		Entry e = DataStore.getInstance().getEntry(args[0], pin);
		System.out.printf("%11s%-18s : %s%n", " ", "Password", new String(e.getPassword()));
	}

	private void validateEntryName(String[] args) {

		if (args.length == 0) {
			throw new IllegalArgumentException("Entry name is required");
		}

		if (!DataStore.getInstance().entryExist(args[0])) {
			throw new IllegalArgumentException("No entry");
		}
	}

}
