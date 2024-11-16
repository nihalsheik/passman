package com.nihalsoft.passman.data;

import com.nihalsoft.passman.EntryParser;
import com.nihalsoft.passman.Util;
import com.nihalsoft.passman.model.Entry;
import com.nihalsoft.passman.model.MetaData;

import java.io.IOException;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class DataStore {

	private static volatile DataStore INSTANCE = null;

	private Map<String, Integer> entries;
	private String fileName;
	private char[] password;

	private DataStore() {
	}

	public static DataStore getInstance() {
		if (INSTANCE == null) {
			synchronized (DataStore.class) {
				if (INSTANCE == null) {
					INSTANCE = new DataStore();
				}
			}
		}
		return INSTANCE;
	}

	public boolean hasDataFile() {
		return fileName != null && !fileName.isEmpty();
	}

	public String getFileName() {
		return fileName;
	}

	public void create(String fileName, char[] password) throws Exception {

		if (Path.of(fileName).toFile().exists()) {
			throw new Exception("File already exists");
		}

		this.fileName = fileName;
		this.password = password;

		try (var file = openFile("rw")) {
			entries = new LinkedHashMap<>();
			file.create();
			Util.println(fileName + " created successfully");
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public void load(String fileName, char[] password) throws Exception {
		if (this.fileName != null && fileName.equalsIgnoreCase(this.fileName.toLowerCase())) {
			throw new Exception("File already loaded");
		}

		this.fileName = fileName;
		this.password = password;

		try (var file = openFile("rw")) {

			entries = new LinkedHashMap<>();
			int delEntries = 0;
			int totalEntries = 0;

			while (file.hasNext()) {
				byte[] rowData = file.next();
				if (rowData[0] == 0) {
					System.out.println(totalEntries);
					delEntries++;
				} else {
					entries.put(EntryParser.getInstance().getName(rowData), totalEntries);
				}
				totalEntries++;
			}

			Util.println(entries.size() + " - Entries Loaded");
			if (delEntries > 0) {
				Util.println(delEntries + " - Deleted entries");
			}
		} catch (Exception e) {
			this.close();
			throw new RuntimeException(e);
		}
	}

	public boolean entryExist(String name) {
		return entries.containsKey(name);
	}


	public void addEntry(Entry entry) throws Exception {
		EntryParser.getInstance().trim(entry); // TODO move to EntryFile
		if (entry.getName().isEmpty()) {
			throw new RuntimeException("Invalid entry name");
		}
		if (entries.containsKey(entry.getName())) {
			throw new RuntimeException("Entry already exist");
		}
		try (var file = openFile("rw")) {
			file.write(entry);
			entries.put(entry.getName(), entries.size());
		}
	}

	public void updateEntry(Entry entry) throws Exception {
		checkEntryAvailability(entry.getName());
		//TODO: Trim ?
		try (var file = openFile("rw")) {
			file.write(entry, entries.get(entry.getName()));
		}
	}


	public void deleteEntry(String name) throws Exception {
		checkEntryAvailability(name);
		try (var file = openFile("rw")) {
			byte[] data = new byte[Entry.SIZE];
			file.write(data, entries.get(name));
			entries.remove(name);
			Util.println("Deleted successfully");
		}
	}

	public void rename(String name, String newName) throws Exception {
		checkEntryAvailability(name);
		try (var file = openFile("rw")) {
			int idx = entries.get(name);
			byte[] rowData = file.get(idx);
			System.arraycopy(newName.getBytes(), 0, rowData, 0, newName.length());
			file.write(rowData, idx);
			entries.remove(name);
			entries.put(newName, idx);
			Util.println("Renamed to " + newName);
		}
	}

	public Entry getEntry(String name) {
		if (!entries.containsKey(name)) {
			throw new RuntimeException("Entry not found");
		}
		try (var file = openFile("rw")) {
			return file.getEntry(entries.get(name));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public void purge() {
		try (var file = openFile("rw")) {
			file.purge();
			int counter = 0;
			while (file.hasNext()) {
				byte[] rowData = file.next();
				entries.put(EntryParser.getInstance().getName(rowData), counter++);
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public Set<String> getEntryNames() {
		return entries.keySet();
	}

	public void resetPassword(char[] password) throws IOException {
		try (var file = openFile("rw")) {
			file.resetPassword(password);
		}
	}

	public void close() {
		this.entries = null;
		this.fileName = null;
	}

	private void checkEntryAvailability(String name) {
		if (!entries.containsKey(name)) {
			throw new RuntimeException("Entry not found");
		}
	}

	public Map<String, Object> getFileInfo() {
		int delEntries = 0;
		int totalEntries = 0;
		try (var file = openFile("rw")) {
			while (file.hasNext()) {
				byte[] rowData = file.next();
				if (rowData[0] == 0) {
					delEntries++;
				} else {
					totalEntries++;
				}
			}
			Map<String, Object> map = new LinkedHashMap<>();
			map.put("Total entries", totalEntries);
			map.put("Deleted entries", delEntries);
			float t = (totalEntries + delEntries) * Entry.SIZE + MetaData.SIZE;
			map.put("File size", String.format("%.02f KB", t / 1024));
			return map;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public EntryFile openFile(String mode) throws IOException {
		return new EntryFile(this.fileName, mode, this.password);
	}
}
