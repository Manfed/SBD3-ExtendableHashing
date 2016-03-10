package sbd3.files;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import sbd3.Consts;
import sbd3.dictionary.Dictionary;

public class DictionaryFile {

	private static RandomAccessFile dictionaryFile;
	private static int readsCount;
	private static int savesCount;
	
	public DictionaryFile(String name, boolean isNewFile) throws FileNotFoundException {
		File file = new File(name + ".dictionary");
		if(isNewFile || (!isNewFile && file.exists())) {
			dictionaryFile = new RandomAccessFile(file, "rw");
			readsCount = 0;
			savesCount = 0;
		} else {
			throw new FileNotFoundException("Nie znaleziono pliku " + file.getName());
		}
		if(isNewFile) {
			try {
				dictionaryFile.setLength(0);
				dictionaryFile.seek(0);
				dictionaryFile.writeInt(-1);
				dictionaryFile.writeInt(-1);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Czyta ca³y skorowidz - zwraca go w postaci listy
	 * @return
	 * @throws IOException 
	 */
	public static List<Integer> readDictionary() throws IOException {
		List<Integer> dictionary = new ArrayList<>();
		byte[] fileContent = new byte[(int) getDictionaryFile().length()];
		
		getDictionaryFile().seek(0);
		getDictionaryFile().readFully(fileContent);
		
		ByteBuffer wrapper = ByteBuffer.wrap(fileContent);
		
		for(int i = 0; i < fileContent.length; i += Consts.DICTIONARY_ENTRY_SIZE) {
			dictionary.add(wrapper.getInt());
		}
		setReadsCount(getReadsCount() + 1);
		return dictionary;
	}
	
	public static int readDataPageAddress(int index) throws IOException {
		getDictionaryFile().seek((index * Consts.DICTIONARY_ENTRY_SIZE));
		setReadsCount(getReadsCount() + 1);
		return getDictionaryFile().readInt();
	}
	
	public static void writeDictionary(Dictionary dict) {
		byte[] newDictionary = new byte[dict.getPages().length * Integer.BYTES];
		ByteBuffer wrapper = ByteBuffer.wrap(newDictionary);
		for(int d : dict.getPages()) {
			wrapper.putInt(d);
		}
		try{
			getDictionaryFile().seek(0);
			getDictionaryFile().write(newDictionary);
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
	/**
	 * Edytuje adres na strone pod podanym indeksem skorowidza
	 * @param index
	 * @param value
	 * @throws IOException
	 */
	public static void editIndex(int index, int value) throws IOException {
		getDictionaryFile().seek((index * Consts.DICTIONARY_ENTRY_SIZE) + Integer.BYTES);
		getDictionaryFile().writeInt(value);
		setSavesCount(getSavesCount() + 1);
	}
	
	public static int getDictionaryDepth() throws IOException {
		return (int) Math.sqrt((getDictionaryFile().length() / Consts.DICTIONARY_ENTRY_SIZE));
	}
	
	public void close() {
		try {
			getDictionaryFile().close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static RandomAccessFile getDictionaryFile() {
		return dictionaryFile;
	}
	
	public static void resetCounters() {
		setReadsCount(0);
		setSavesCount(0);
	}

	public static int getReadsCount() {
		return readsCount;
	}

	public static int getSavesCount() {
		return savesCount;
	}

	private static void setReadsCount(int readsCount) {
		DictionaryFile.readsCount = readsCount;
	}

	private static void setSavesCount(int savesCount) {
		DictionaryFile.savesCount = savesCount;
	}
}
