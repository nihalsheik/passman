package com.nihalsoft.passman.model;

public class Entry {

	// userName (30), password (30), notes (100) = 160 + 16 = 176 + name (20) = 196
	public static final int SIZE = 196;

	private String name;
	private String userName;
	private char[] password;
	private String notes;

	public Entry(String name, String userName, char[] password, String notes) {
		this.name = name;
		this.userName = userName;
		this.password = password;
		this.notes = notes;
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

}
