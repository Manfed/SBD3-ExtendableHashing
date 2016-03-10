package sbd3.files;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

import sbd3.Consts;

public class MetadataFile {

	private RandomAccessFile metadataFile;
	
	/**
	 * lista zawieraj¹ca wolne adresy w pliku danych
	 */
	private static List<Integer> freeDataFileAddresses;
	
	
	public MetadataFile(String name) throws IOException {
		File file = new File(name + ".meta");
		if(file.exists()) {
			freeDataFileAddresses = new ArrayList<>();
			metadataFile = new RandomAccessFile(file, "rw");
			metadataFile.seek(0);
			Consts.RECORDS_ON_PAGE = metadataFile.readInt();
			int size = metadataFile.readInt();
			for(int i = 0; i < size; i++) {
				freeDataFileAddresses.add(metadataFile.readInt());
			}
		} else {
			throw new FileNotFoundException(file.getName());
		}
	}
	
	/**
	 * Konstruktor dla nowego pliku
	 * @param name
	 * @param isNewFile
	 */
	public MetadataFile(String name, boolean isNewFile) {
		File file = new File(name + ".meta");
		
		try {
			metadataFile = new RandomAccessFile(file, "rw");
			metadataFile.setLength(0);
		} catch (IOException e) {
			System.out.println("Nie zaleziono pliku metadanych");
			e.printStackTrace();
		}

		freeDataFileAddresses = new ArrayList<>();
	}
	
	public void close() throws IOException {
		getMetadataFile().seek(0);
		getMetadataFile().writeInt(Consts.RECORDS_ON_PAGE);
		getMetadataFile().writeInt(getFreeDataFileAddresses().size());
		for(int i = 0; i < getFreeDataFileAddresses().size(); i++) {
			getMetadataFile().writeInt(getFreeDataFileAddresses().get(i));
		}
		getMetadataFile().close();
	}

	public static List<Integer> getFreeDataFileAddresses() {
		return freeDataFileAddresses;
	}
	
	public static int getLastFreeAddress() {
		if(!getFreeDataFileAddresses().isEmpty()) {
			int addr = getFreeDataFileAddresses().get(0);
			getFreeDataFileAddresses().remove(0);
			return addr;
		}
		return -1;
	}

	private RandomAccessFile getMetadataFile() {
		return metadataFile;
	}
}
