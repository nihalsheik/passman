package com.nihalsoft.passman.data;

import com.nihalsoft.passman.AESUtil;
import com.nihalsoft.passman.EntryParser;
import com.nihalsoft.passman.model.Entry;
import com.nihalsoft.passman.model.MetaData;

import javax.crypto.BadPaddingException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;

public class EntryFile extends RandomAccessFile implements AutoCloseable {

	private long index = 0;
	private long totalRecords = 0;
	private MetaData metaData;

	public EntryFile(String fileName, String mode, char[] password) throws IOException {

		super(fileName, mode);

		if (this.length() == 0) {
			this.metaData = new MetaData(password);
			return;
		}

		try {

			totalRecords = (this.length() - MetaData.SIZE) / Entry.SIZE;
			byte[] dec = AESUtil.decrypt(getMetadataBytes(), password);
			this.metaData = new MetaData(password, Arrays.copyOfRange(dec, 0, 16), Arrays.copyOfRange(dec, 16, 28));

		} catch (Exception e) {
			if (e instanceof BadPaddingException) {
				throw new RuntimeException("Incorrect password");
			}
			throw new RuntimeException(e);
		}

	}

	public void create() throws IOException {
		this.seek(0);
		this.write(EntryParser.getInstance().encryptMetaData(metaData));
	}

	public boolean hasNext() {
		return index < totalRecords;
	}

	public byte[] next() throws IOException {
		byte[] bytes = get(index);
		index++;
		return bytes;
	}

	public Entry nextEntry() throws IOException {
		Entry e = EntryParser.getInstance().toEntry(get(index), metaData);
		index++;
		return e;
	}

	public MetaData getMetadata() throws IOException {
		return metaData;
	}

	public byte[] getMetadataBytes() throws IOException {
		this.seek(0);
		byte[] rowData = new byte[MetaData.SIZE];
		this.read(rowData);
		return rowData;
	}

	public Entry getEntry(long index) throws IOException {
		return EntryParser.getInstance().toEntry(get(index), metaData);
	}

	public byte[] get(long index) throws IOException {
		this.seek(getPos(index));
		byte[] rowData = new byte[Entry.SIZE];
		this.read(rowData);
		return rowData;
	}


	public void write(Entry entry) throws IOException {
		byte[] bytes = EntryParser.getInstance().toBytes(entry, metaData);
		this.seek(this.length());
		this.write(bytes);
	}

	public void write(Entry entry, int pos) throws IOException {
		byte[] bytes = EntryParser.getInstance().toBytes(entry, metaData);
		this.seek(getPos(pos));
		this.write(bytes);
		this.totalRecords++;
	}

	public void write(byte[] entry, int pos) throws IOException {
		this.seek(getPos(pos));
		this.write(entry);
	}

	private long getPos(long index) {
		return MetaData.SIZE + index * Entry.SIZE;
	}


	public void purge() throws IOException {

		int rec = 0;
		while (this.hasNext()) {
			byte[] rowData = this.next();
			if (rowData[0] == 0)
				break;
			rec++;
		}

		if (rec == totalRecords) {
			moveFirst();
			return;
		}

		while (this.hasNext()) {
			byte[] rowData = this.next();
			if (rowData[0] == 0)
				continue;
			this.write(rowData, rec);
			rec++;
		}

		this.totalRecords = rec;
		long newLength = MetaData.SIZE + ((long) rec * Entry.SIZE);
		this.setLength(newLength);
		moveFirst();
	}

	public void moveFirst() throws IOException {
		this.index = 0;
		this.seek(0);
	}

	public void resetPassword(char[] password) throws IOException {
		int idx = 0;
		var md = new MetaData(password);
		var ep = EntryParser.getInstance();
		while (this.hasNext()) {
			Entry entry = this.nextEntry();
			if (entry != null) {
				this.write(ep.toBytes(entry, md), idx);
			}
			idx++;
		}
		this.seek(0);
		this.write(EntryParser.getInstance().encryptMetaData(metaData));
		this.metaData = md;
		this.moveFirst();
	}

	@Override
	public void close() throws IOException {
		super.close();
	}
}
