package sbd3.data;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import sbd3.Consts;
import sbd3.dictionary.Dictionary;
import sbd3.files.DataFile;
import sbd3.files.MetadataFile;

public class DataPage {

	private GeometricSequence[] records;
	private int size;
	private int depth;
	private int position;
	private String key;

	public DataPage(int size, int depth, int position, GeometricSequence[] records) {
		this.size = size;
		this.records = records;
		this.depth = depth;
		this.position = position;
	}
	
	public DataPage(int size, int depth, int position, String key, GeometricSequence[] records) {
		this.size = size;
		this.records = records;
		this.depth = depth;
		this.position = position;
		this.key = key;
	}
	
	public DataPage(int depth, int position) {
		this.depth = depth;
		this.position = position;
	}
	
	public DataPage(int recordsOnPage) {
		this.records = new GeometricSequence[recordsOnPage];
		this.depth = 1;
		this.size = 0;
	}
	
	public GeometricSequence find(int id) {
		for(int i = 0; i < getRecords().length; i++) {
			if(getRecords()[i] != null && getRecords()[i].getId() == id) {
				return getRecords()[i];
			}
		}
		return null;
	}
	
	//----------------------------------------------------------------------------------
	//DODAWANIE
	
	public void insert(GeometricSequence gs, Dictionary dictionary) throws IOException {
		if(getSize() < Consts.RECORDS_ON_PAGE) {
			try{
				getRecords()[getSize()] = gs;
				setSize(getSize() + 1);
				Arrays.sort(getRecords(), 0, getSize());
				if(getPosition() == -1) {
					setPosition(MetadataFile.getLastFreeAddress());
				}
				DataFile.writeDataPageAt(this, getPosition());
				if(getKey() == null || getKey().isEmpty()) {
					setKey(Consts.hash(gs.getId(), getDepth()));
				}
				dictionary.updateDictionary(getKey(), getPosition());
			} catch(IOException e) {
				e.printStackTrace();
			}
		} else {
			DataPage newPage = splitPage(gs, dictionary);
			if(getDepth() > dictionary.getDepth()) {
				dictionary.doubleDictionary(this, newPage);
			} else if(getDepth() <= dictionary.getDepth()) {
				dictionary.assignAddressToPages(this, newPage);
			}
		}
		DataFile.writeDataPageAt(this, getPosition());
	}
	
	private DataPage splitPage(GeometricSequence newValue, Dictionary dictionary) throws IOException {
		List<GeometricSequence> firstPage = new ArrayList<>(), 
				secondPage = new ArrayList<>();
		int firstIndex = Consts.getDictionaryIndex(getRecords()[0].getId(), getDepth() + 1);
		for(int i = 0; i < getSize(); i++) {
			if(Consts.getDictionaryIndex(getRecords()[i].getId(), getDepth() + 1) == firstIndex) {
				firstPage.add(getRecords()[i]);
			} else {
				secondPage.add(getRecords()[i]);
			}
		}
		//dodanie do w³aœciwej listy dodawanej wartoœci
		if(Consts.getDictionaryIndex(newValue.getId(), getDepth() + 1) == firstIndex) {
			firstPage.add(newValue);
		} else {
			secondPage.add(newValue);
		}
		DataPage newPage = new DataPage(secondPage.size(), getDepth() + 1,
				0, secondPage.toArray(new GeometricSequence[secondPage.size()]));
		newPage.setPosition(DataFile.writeDataPage(newPage));
		
		setDepth(getDepth() + 1);
		setRecords(firstPage.toArray(new GeometricSequence[firstPage.size()]));
		setSize(firstPage.size());
		
		return newPage;
	}
	
	
	
	//----------------------------------------------------------------------------------
	//USUWANIE
	
	public void delete(int id, Dictionary dictionary) {
		for(int i = 0; i < getSize(); i++) {
			if(getRecords()[i].getId() == id) {
				getRecords()[i] = getRecords()[getSize() - 1];
				getRecords()[getSize() - 1] = null;
				setSize(getSize() - 1);
				Arrays.sort(getRecords(), 0, getSize());
			}
		}
		if(getSize() == 0) {
			try {
				DataFile.deleteDataPage(getPosition());
				if(getKey() == null || getKey().isEmpty()) {
					setKey(Consts.hash(id, getDepth()));
				}
				dictionary.updateDictionary(getKey(), -1);
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			try {
				DataFile.writeDataPageAt(this, getPosition());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
	}
	
	//----------------------------------------------------------------------------------
	//MODYFIKACJA
	
	public void modify(GeometricSequence gs) {
		for(int i = 0; i < getSize(); i++) {
			if(gs.getId() == getRecords()[i].getId()) {
				getRecords()[i] = gs;
				try {
					DataFile.writeDataPageAt(this, getPosition());
				} catch (IOException e) {
					e.printStackTrace();
				}
				return;
			}
		}
		
	}
	
	//----------------------------------------------------------------------------------
	
	public void printDataPage() {
		for(int i = 0; i < getRecords().length; i++) {
			if(getRecords()[i] != null) {
				System.out.println("\t" + getRecords()[i].getId() + 
						"(" + Consts.hash(getRecords()[i].getId(), getDepth()) + ")"
						+ "\t" + getRecords()[i].getFirstTerm()
						+ "\t" + getRecords()[i].getMultiplier());
			}
		}
	}
	
	public boolean isEmpty() {
		return getSize() == 0;
	}
	
	public GeometricSequence[] getRecords() {
		return records;
	}

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}

	public int getDepth() {
		return depth;
	}

	public int getPosition() {
		return position;
	}

	private void setRecords(GeometricSequence[] records) {
		this.records = records;
	}

	private void setDepth(int depth) {
		this.depth = depth;
	}

	public void setPosition(int position) {
		this.position = position;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}
	
}
