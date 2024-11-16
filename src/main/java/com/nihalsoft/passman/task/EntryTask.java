package com.nihalsoft.passman.task;

import com.nihalsoft.passman.Command;
import com.nihalsoft.passman.Util;
import com.nihalsoft.passman.data.DataStore;
import com.nihalsoft.passman.model.Entry;

public class EntryTask implements Task {


	public EntryTask() {
	}

	public void exec(Command cmd, String[] args) throws Exception {

		var dataStore = DataStore.getInstance();

		String name = "";
		switch (cmd) {
			case UPDATE:
			case DELETE:
			case RENAME:
				if (args.length == 0 || args[0] == null || args[0].isEmpty()) {
					throw new Exception("Provide entry name");
				}

				name = args[0].trim();
				Entry e = dataStore.getEntry(name);
				if (e == null) {
					throw new Exception("Entry not found");
				}
				if (cmd.equals(Command.DELETE)) {
					delete(name);
					return;
				}
				if (cmd.equals(Command.RENAME)) {
					String newName = System.console().readLine("%11s%-12s  [20] : ", " ", "New name");
					//DataStore.getInstance().rename(name, newName);
					return;
				}
				Util.displayEntry(e);
				break;
			case ADD:
				name = System.console().readLine("%11s%-12s  [20] : ", " ", "Name");
				if (name.isEmpty()) {
					throw new Exception("Invalid entry name");
				}
				if (dataStore.entryExist(name)) {
					throw new Exception("Entry already exist");
				}
				break;
		}

		String un = System.console().readLine("%11s%-12s  [30] : ", " ", "Username");
		String pw = System.console().readLine("%11s%-12s  [30] : ", " ", "Password");
		String no = System.console().readLine("%11s%-12s [100] : ", " ", "Notes");

		Entry e = new Entry(name, un, pw.toCharArray(), no);

		if (cmd.equals(Command.UPDATE)) {
			dataStore.updateEntry(e);
		} else {
			dataStore.addEntry(e);
		}

		System.out.println();
		Util.println("Successfully saved");
	}

	private void delete(String name) throws Exception {
		String confirm = System.console().readLine("%11s%-18s : ", " ", "Are you sure want to delete the entry (y/n)?");
		if (confirm.equalsIgnoreCase("y")) {
			DataStore.getInstance().deleteEntry(name);
		}
	}
}
