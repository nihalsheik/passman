package com.nihalsoft.passman.task;

import com.nihalsoft.passman.Command;
import com.nihalsoft.passman.data.DataStore;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

public class FileTask implements Task {

	public FileTask() {
	}

	@Override
	public void exec(Command cmd, String[] args) throws Exception {
		switch (cmd) {
			case CREATE -> create(args);
			case LOAD -> load(args);
			case RESETPASS -> resetPass();
			case PURGE -> purge();
			case CLOSE -> DataStore.getInstance().close();
			case INFO -> showInfo();
		}
	}

	private void showInfo() {
		DataStore.getInstance().getFileInfo().forEach((k, v) -> System.out.printf("%11s%-18s : %s%n", " ", k, v));
	}

	private void create(String[] args) throws Exception {

		if (DataStore.getInstance().hasDataFile()) {
			throw new Exception("Please close current file and try");
		}

		String fileName = getFileName(args);
		char[] pwd = new char[0];
		if (!Files.exists(Path.of(fileName))) {
			pwd = readPassword("Password");
			char[] pwd2 = readPassword("Confirm");
			if (!Arrays.equals(pwd, pwd2)) {
				throw new RuntimeException("Passwords doesn't match");
			}
		}
		DataStore.getInstance().create(fileName, pwd);
	}

	private void load(String[] args) throws Exception {
		String fileName = getFileName(args);
		if (!Files.exists(Path.of(fileName))) {
			throw new RuntimeException("File not found: " + fileName);
		}
		char[] pwd = readPassword("Password");
		DataStore.getInstance().load(fileName, pwd);
	}

	private void resetPass() throws Exception {
		char[] pwd = readPassword("Password");
		char[] pwd2 = readPassword("Confirm");
		if (!Arrays.equals(pwd, pwd2)) {
			throw new RuntimeException("Passwords doesn't match");
		}
		DataStore.getInstance().resetPassword(pwd);
	}


	private void purge() {
		DataStore.getInstance().purge();
	}

	private String getFileName(String[] args) {
		if (args.length == 0 || args[0].isEmpty()) {
			throw new IllegalStateException("Invalid file name");
		}
		return args[0].trim();
	}

	private char[] readPassword(String label) {
		char[] pwd = System.console().readPassword("%11s%-18s : ", " ", label);
		if (pwd.length == 0) {
			throw new RuntimeException("Empty password, doesn't allow");
		}
		return pwd;
	}

}
