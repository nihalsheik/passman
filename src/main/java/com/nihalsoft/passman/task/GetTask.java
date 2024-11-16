package com.nihalsoft.passman.task;

import com.nihalsoft.passman.Command;
import com.nihalsoft.passman.Util;
import com.nihalsoft.passman.data.DataStore;
import com.nihalsoft.passman.model.Entry;

import java.util.Set;

public class GetTask implements Task {

	@Override
	public void exec(Command cmd, String[] args) throws Exception {
		if (cmd.equals(Command.LIST)) {
			list();
		} else if (cmd.equals(Command.GET)) {
			get(args);
		}
	}

	private void list() {
		Set<String> list = DataStore.getInstance().getEntryNames();
		if (list.isEmpty()) {
			Util.println("No entries found");
		}
		Util.println("-".repeat(24));
		int i = 1;
		for (String name : list) {
			Util.println("  " + name);
			i++;
		}
		Util.println("-".repeat(24));
		Util.println("Total entries : " + list.size());
	}

	private void get(String[] args) throws Exception {

		if (args.length == 0) {
			throw new IllegalArgumentException("Entry name is required");
		}
		var ds = DataStore.getInstance();

		Entry e = ds.getEntry(args[0]);

		if (e == null) {
			throw new IllegalArgumentException("No entry");
		}
		Util.displayEntry(e);
	}

}
