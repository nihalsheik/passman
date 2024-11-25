package com.nihalsoft.passman.data;

import com.nihalsoft.passman.EntryParser;
import com.nihalsoft.passman.Util;
import com.nihalsoft.passman.model.Entry;
import com.nihalsoft.passman.model.MetaData;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Consumer;

public class DataStore {

	private static volatile DataStore INSTANCE = null;

	private Map<String, Integer> indexMap;
	private String fileName;
	private char[] password;
	private int securityPin;

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

	public void create(String fileName, char[] password, int securityPin) throws Exception {

		if (Path.of(fileName).toFile().exists()) {
			throw new Exception("File already exists");
		}

		this.fileName = fileName;
		this.password = password;
		this.securityPin = securityPin;

		try (var file = openFileForWrite()) {
			indexMap = new LinkedHashMap<>();
			file.create();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public void load(String fileName, char[] password, int securityPin) throws Exception {
		if (this.fileName != null && fileName.equalsIgnoreCase(this.fileName.toLowerCase())) {
			throw new Exception("File already loaded");
		}

		this.fileName = fileName;
		this.password = password;
		this.securityPin = securityPin;

		try (var file = openFileForRead()) {

			indexMap = new LinkedHashMap<>();
			int i = 0;

			var ep = EntryParser.getInstance();
			while (file.hasNext()) {
				byte[] rowData = file.next();
				if (!ep.isDeleted(rowData)) {
					indexMap.put(ep.getName(rowData), i);
				}
				i++;
			}

		} catch (Exception e) {
			this.close();
			throw new RuntimeException(e);
		}
	}

	public boolean entryExist(String name) {
		return indexMap.containsKey(name);
	}


	public void addEntry(Entry entry) throws Exception {
		EntryParser.getInstance().trim(entry); // TODO move to EntryFile
		if (entry.getName().isEmpty()) {
			throw new RuntimeException("Invalid entry name");
		}
		if (indexMap.containsKey(entry.getName())) {
			throw new RuntimeException("Entry already exist");
		}
		try (var file = openFileForWrite()) {
			file.write(entry);
			indexMap.put(entry.getName(), indexMap.size());
		}
	}

	public void updateEntry(Entry entry) throws Exception {
		checkEntryAvailability(entry.getName());
		//TODO: Trim ?
		try (var file = openFileForWrite()) {
			file.write(entry, indexMap.get(entry.getName()));
		}
	}


	public boolean deleteEntry(String name) throws Exception {
		checkEntryAvailability(name);
		try (var file = openFileForWrite()) {
			byte[] data = new byte[Entry.SIZE];
			file.write(data, indexMap.get(name));
			indexMap.remove(name);
		}
		return true;
	}

	public void rename(String name, String newName) throws Exception {
		checkEntryAvailability(name);
		try (var file = openFileForWrite()) {
			byte[] newNameBytes = new byte[20];
			System.arraycopy(newName.getBytes(), 0, newNameBytes, 0, newName.length());
			int idx = indexMap.get(name);
			byte[] rowData = file.get(idx);
			System.arraycopy(newNameBytes, 0, rowData, 0, 20);
			file.write(rowData, idx);
			indexMap.remove(name);
			indexMap.put(newName, idx);
			Util.println("Renamed to " + newName);
		}
	}

	public Entry getEntry(String name) {
		return getEntry(name, -1);
	}

	public Entry getEntry(String name, int securityPin) {
		if (!indexMap.containsKey(name)) {
			throw new RuntimeException("Entry not found");
		}
		try (var file = openFileForRead()) {
			int idx = indexMap.get(name);
			return (securityPin == -1) ?
					file.getEntry(idx) :
					file.getEntry(idx, securityPin);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public void purge() {
		try (var file = openFileForWrite()) {
			file.purge();
			int counter = 0;
			indexMap = new LinkedHashMap<>();
			while (file.hasNext()) {
				byte[] rowData = file.next();
				indexMap.put(EntryParser.getInstance().getName(rowData), counter++);
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public List<Map<String, Object>> getEntryList() {
		var list = new ArrayList<Map<String, Object>>();

		try (var file = openFileForRead()) {
			if (file.getTotalRecords() == 0) {
				return list;
			}
			Map<String, Object> map;
			var ep = EntryParser.getInstance();
			while (file.hasNext()) {
				byte[] data = file.next();
				if (ep.isDeleted(data)) {
					continue;
				}
				map = new HashMap<>();
				map.put("name", ep.getName(data));
				map.put("updatedTime", ep.getUpdatedTime(data));
				list.add(map);
			}
			return list;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public Set<String> getEntryNames() {
		return indexMap.keySet();
	}

	public long getTotalRecords() throws IOException {
		try (var file = openFileForRead()) {
			return file.getTotalRecords();
		}
	}

	public void resetPassword(char[] password, int securityPin, Consumer<Integer> consumer) throws IOException {
		try (var file = openFileForWrite()) {
			file.resetPassword(password, securityPin, consumer);
			this.password = password;
			this.securityPin = securityPin;
		}
	}

	public void close() {
		this.indexMap = null;
		this.fileName = null;
	}

	private void checkEntryAvailability(String name) {
		if (!indexMap.containsKey(name)) {
			throw new RuntimeException("Entry not found");
		}
	}

	public Map<String, Object> getFileInfo() {
		int delEntries = 0;
		int totalEntries = 0;
		try (var file = openFileForRead()) {
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

	private EntryFile openFileForRead() throws IOException {
		return new EntryFile(this.fileName, "r", this.password, this.securityPin);
	}

	private EntryFile openFileForWrite() throws IOException {
		return new EntryFile(this.fileName, "rw", this.password, this.securityPin);
	}

}
