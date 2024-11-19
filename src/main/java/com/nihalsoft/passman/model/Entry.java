package com.nihalsoft.passman.model;

public class Entry {

	// name (20), updatedTime(8), userName (30), password (30), notes (150) + padding 16 + 16 = 32
	public static final int SIZE = 270;

	private String name;
	private String userName;
	private char[] password;
	private String notes;
	private long updatedTime;

	public Entry(String name, String userName, char[] password, String notes) {
		this.name = name;
		this.userName = userName;
		this.password = password;
		this.notes = notes;
		this.updatedTime = System.currentTimeMillis();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public char[] getPassword() {
		return password;
	}

	public void setPassword(char[] password) {
		this.password = password;
	}

	public String getNotes() {
		return notes;
	}

	public void setNotes(String notes) {
		this.notes = notes;
	}

	public long getUpdatedTime() {
		return updatedTime;
	}

	public void setUpdatedTime(long updatedTime) {
		this.updatedTime = updatedTime;
	}

}
