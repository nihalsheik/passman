package com.nihalsoft.passman.task;

import com.nihalsoft.passman.Command;
import com.nihalsoft.passman.Util;
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
		DataStore.getInstance().getFileInfo().forEach((k, v) -> {
			System.out.printf("%12s%-18s : %s%n", " ", k, v);
		});
	}

	private void create(String[] args) throws Exception {

		if (DataStore.getInstance().hasDataFile()) {
			throw new Exception("Please close current file and try");
		}

		String fileName = getFileName(args);
		char[] pwd = new char[0];
		int pin = 0;

		if (!Files.exists(Path.of(fileName))) {
			Object[] k = readPassword2(true);
			pwd = (char[]) k[0];
			pin = (Integer) k[1];
		}

		DataStore.getInstance().create(fileName, pwd, pin);
		Util.println(fileName + " created successfully");
	}

	private void load(String[] args) throws Exception {
		String fileName = getFileName(args);
		if (!Files.exists(Path.of(fileName))) {
			throw new RuntimeException("File not found: " + fileName);
		}
		Object[] k = readPassword2(false);
		DataStore.getInstance().load(fileName, (char[]) k[0], (Integer) k[1]);
		Util.println("Entries Loaded");
	}

	private void resetPass() throws Exception {
		Object[] k = readPassword2(false);
		long len = DataStore.getInstance().getTotalRecords();
		System.out.printf("%12s%-18s : %s%n", " ", "Total Records", len);
		System.out.printf("%12s%-18s : ", " ", "Percentage");
		DataStore.getInstance().resetPassword((char[]) k[0], (Integer) k[1], rec -> {
			float percent = ((float) rec) / len * 100;
			if (percent % 10 == 0) {
				System.out.print(percent + "%  > ");
			}
		});
		System.out.print("100%  ");
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
		char[] pwd = System.console().readPassword("%12s%-18s : ", " ", label);
		if (pwd.length == 0) {
			throw new RuntimeException("Invalid credentials");
		}
		return pwd;
	}

	private Object[] readPassword2(boolean confirm) {
		char[] pwd = readPassword("Password");
		if (confirm) {
			char[] pwd2 = readPassword("Confirm");
			if (!Arrays.equals(pwd, pwd2)) {
				throw new RuntimeException("Passwords doesn't match");
			}
			System.out.println();
		}
		char[] sPin = readPassword("Security Pin");
		if (confirm) {
			char[] sPin2 = readPassword("Confirm");
			if (!Arrays.equals(sPin, sPin2)) {
				throw new RuntimeException("Security pin doesn't match");
			}
		}
		return new Object[]{pwd, Integer.parseInt(new String(sPin))};
	}
}
