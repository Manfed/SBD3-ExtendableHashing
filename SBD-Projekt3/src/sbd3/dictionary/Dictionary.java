package sbd3.dictionary;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import sbd3.Consts;
import sbd3.data.DataPage;
import sbd3.data.GeometricSequence;
import sbd3.files.DataFile;
import sbd3.files.DictionaryFile;

public class Dictionary {

	private int depth;
	private int[] pages;
	
	public Dictionary(int depth) {
		this.depth = depth;
		this.pages = new int[(int) Math.pow(2, depth)];
	}
	
	public void insert(GeometricSequence gs) throws IOException {
		DataFile.resetCounters();
		DictionaryFile.resetCounters();
		int dictPosition = Consts.getDictionaryIndex(gs.getId(), getDepth());
		DataPage page = DataFile.readDataPage(DictionaryFile.readDataPageAddress(dictPosition));
		if(page.find(gs.getId()) == null) {
			page.insert(gs, this);
		} else {
			System.out.println("Istnieje ju¿ rekord o ID " + gs.getId());
		}
		System.out.println("Odczyty: " + (DataFile.getReadsCount() + DictionaryFile.getReadsCount())
				+  "\tZapisy: " + (DataFile.getSavesCount() + DictionaryFile.getSavesCount()));
	}
	
	public void delete(int id) {
		DataFile.resetCounters();
		DictionaryFile.resetCounters();
		int dictPosition = Consts.getDictionaryIndex(id, getDepth());
		DataPage page;
		try {
			page = DataFile.readDataPage(DictionaryFile.readDataPageAddress(dictPosition));
			if(page.find(id) != null) {
				page.delete(id, this);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("Odczyty: " + (DataFile.getReadsCount() + DictionaryFile.getReadsCount())
				+  "\tZapisy: " + (DataFile.getSavesCount() + DictionaryFile.getSavesCount()));
	}
	
	public void modify(int id, double newFirstTerm, double newMultiplier) {
		DataFile.resetCounters();
		DictionaryFile.resetCounters();
		int dictPosition = Consts.getDictionaryIndex(id, getDepth());
		try {
			DataPage page = DataFile.readDataPage(DictionaryFile.readDataPageAddress(dictPosition));
			if(page.find(id) != null) {
				page.modify(new GeometricSequence(id, newFirstTerm, newMultiplier));
			}
		} catch(IOException e) {
			e.printStackTrace();
		}
		System.out.println("Odczyty: " + (DataFile.getReadsCount() + DictionaryFile.getReadsCount())
				+  "\tZapisy: " + (DataFile.getSavesCount() + DictionaryFile.getSavesCount()));
	}
	
	public void printDict() {
		try {
			DataFile.resetCounters();
			DictionaryFile.resetCounters();
			List<Integer> dictionary = DictionaryFile.readDictionary();
			if(dictionary != null && !dictionary.isEmpty()) {
				int lastAddress = dictionary.get(0), index = 0;
				
				for(int de : dictionary) {
					if(lastAddress != de) {
						DataFile.readDataPage(lastAddress).printDataPage();
						lastAddress = de;
					}
					System.out.println(index + "(" + Consts.getBinaryRepr(index, getDepth())
						+")\t" + de + "\t");
					index++;
				}
				DataFile.readDataPage(lastAddress).printDataPage();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		System.out.println("Odczyty: " + (DataFile.getReadsCount() + DictionaryFile.getReadsCount())
				+  "\tZapisy: " + (DataFile.getSavesCount() + DictionaryFile.getSavesCount()));
	}
	
	public GeometricSequence find(int id) throws IOException {
		DataFile.resetCounters();
		DictionaryFile.resetCounters();
		int dictPosition = Consts.getDictionaryIndex(id, getDepth());
		DataPage page = DataFile.readDataPage(DictionaryFile.readDataPageAddress(dictPosition));
		
		System.out.println("Odczyty: " + (DataFile.getReadsCount() + DictionaryFile.getReadsCount())
				+  "\tZapisy: " + (DataFile.getSavesCount() + DictionaryFile.getSavesCount()));
		
		return page.find(id);
	}
	
	/**
	 * Aktualizuje adresy w skorowidzu podczas dodawania rekordu
	 */
	public void assignAddressToPages(DataPage firstPage, DataPage secondPage) {
		String firstPagePrefix = Consts.hash(firstPage.getRecords()[0].getId(), 
				firstPage.getDepth()),
			secondPagePrefix = Consts.hash(secondPage.getRecords()[0].getId(),
				secondPage.getDepth()),
			dictionaryIndexPrefix = "";
		for(int i = 0; i < getPages().length; i++) {
			dictionaryIndexPrefix = Consts.getBinaryRepr(i, getDepth())
					.substring(0, firstPage.getDepth());
			if(dictionaryIndexPrefix.equals(firstPagePrefix)) {
				getPages()[i] = firstPage.getPosition();
			} else if(dictionaryIndexPrefix.equals(secondPagePrefix)) {
				getPages()[i] = secondPage.getPosition();
			}
		}
		DictionaryFile.writeDictionary(this);
	}
	
	/**
	 * Po podwojeniu skorowidza - przypisuje adresy dla ka¿dej strony
	 * @param firstPage - pierwsza strona - powinna to byæ strona do która by³a dzielona
	 * @param secondPage - nowa strona utworzona po podziale
	 */
	public void reorganizeDirectory(DataPage firstPage, DataPage secondPage) {
		Map<Integer, Integer> distinctAddresses = new HashMap<>();
		List<DataPage> pages = new ArrayList<>();
		int pageAddressCount = 0; //liczba adresów która musi byæ przypisana do strony
		String dictIndex = "", pageIndex = "";
		
		for(int i = 0; i < getPages().length / 2; i++) {
			distinctAddresses.put(getPages()[i], i);
		}
		for(Map.Entry<Integer, Integer> entry : distinctAddresses.entrySet()) {
			try {
				if(entry.getKey() != firstPage.getPosition()) {
					pages.add(DataFile.readDataPage(entry.getKey(), entry.getValue()));
				} else {
					pages.add(firstPage);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		pages.add(secondPage);
		
		for(DataPage page : pages) {
			pageAddressCount = (int) Math.pow(2, getDepth() - page.getDepth());
			if(page.getRecords()[0] != null) {
				pageIndex = Consts.hash(page.getRecords()[0].getId()
						, getDepth()).substring(0, page.getDepth());
			}
			
			for(int i = 0; i < getPages().length; i++) {
				if(pageAddressCount == 0) {
					break;
				}
				dictIndex = Consts.getBinaryRepr(i, getDepth()).substring(0, page.getDepth());
				
				if(pageIndex.equals(dictIndex)) {
					getPages()[i] = page.getPosition();
					pageAddressCount--;
				}
			}
		}
		DictionaryFile.writeDictionary(this);
	}
	
	public void doubleDictionary(DataPage firstPage, DataPage secondPage) {
		int[] newPages = new int[getPages().length * 2];
		setDepth(getDepth() + 1);
		for(int i = 0; i < getPages().length; i++) {
			newPages[i] = getPages()[i];
		}
		setPages(newPages);
		reorganizeDirectory(firstPage, secondPage);
	}
	
	/**
	 * Uzupe³nia skorowidz na pozycjach odpowiadaj¹cych podanemu kluczowi podan¹ wartoœci¹
	 * @param key
	 * @param value
	 */
	public void updateDictionary(String key, int value) {
		int modificationsCount = (int) Math.pow(2, getDepth() - key.length());
		boolean modified = false;
		
		for(int i = 0; i < getPages().length; i++) {
			if(modificationsCount == 0) {
				break;
			} else if(Consts.getBinaryRepr(i, getDepth()).substring(0, key.length()).equals(key)) {
				if(getPages()[i] != value) {
					getPages()[i] = value;
					modified = true;
				}
				modificationsCount--;
			}
		}
		if(modified) {
			DictionaryFile.writeDictionary(this);
		}
	}
	
	public int getDepth() {
		return depth;
	}

	public int[] getPages() {
		return pages;
	}

	public void setPages(int[] pages) {
		this.pages = pages;
	}

	public void setDepth(int depth) {
		this.depth = depth;
	}
	
}
