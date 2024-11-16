package com.nihalsoft.passman;

public enum Command {

	CREATE(false),

	LOAD(false),

	RESETPASS(true),

	PURGE(true),

	CLOSE(true),

	INFO(false),

	LIST(true),

	ADD(true),

	UPDATE(true),

	GET(true),

	DELETE(true),

	RENAME(true),

	EXIT(false),

	CLEAR(false);

	private final boolean dataFileRequired;

	Command(boolean dataFileRequired) {
		this.dataFileRequired = dataFileRequired;
	}

	public boolean isDataFileRequired() {
		return dataFileRequired;
	}

}
