package com.nihalsoft.passman.task;

import com.nihalsoft.passman.Command;
import com.nihalsoft.passman.data.DataStore;

public class TaskFactory {

	public static Task get(String taskName) {

		Command cmd = Command.valueOf(taskName.toUpperCase());

		if (cmd.isDataFileRequired() && !DataStore.getInstance().hasDataFile()) {
			throw new IllegalStateException("No data file is being loaded");
		}

		return switch (cmd) {

			case ADD, UPDATE, DELETE, RENAME -> new EntryTask();

			case CREATE, LOAD, RESETPASS, PURGE, CLOSE, INFO -> new FileTask();

			case LIST, GET -> new GetTask();

			default -> throw new IllegalStateException("Unexpected value: " + taskName);
		};

	}
}
