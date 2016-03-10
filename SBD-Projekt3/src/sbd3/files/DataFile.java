package sbd3.files;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;

import sbd3.Consts;
import sbd3.data.DataPage;
import sbd3.data.GeometricSequence;

public class DataFile {

	private static RandomAccessFile dataFile;
	private static int readsCount;
	private static int savesCount;
	
	public DataFile(String name, boolean isNewFile) throws FileNotFoundException {
		File file = new File(name + ".data");
		if(isNewFile || (!isNewFile && file.exists())) {
			dataFile = new RandomAccessFile(file, "rw");
			readsCount = 0;
			savesCount = 0;
		} else {
			throw new FileNotFoundException("Nie znaleziono pliku " + file.getName());
		}
		if(isNewFile) {
			try {
				dataFile.setLength(0);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public static DataPage readDataPage(int address) throws IOException {
		//jesli adres > wielkosc pliku lub proba odczytania usunietej strony
		// -> zwróæ pust¹ stronê
		if(address >= getDataFile().length() || 
				MetadataFile.getFreeDataFileAddresses().contains(address) ||
				address == -1) {
			DataPage page = new DataPage(Consts.RECORDS_ON_PAGE);
			/*if(address == -1) {
				page.setPosition((int) (MetadataFile.getFreeDataFileAddresses().isEmpty() ?
						getDataFile().length() : MetadataFile.getFreeDataFileAddresses().get(0)));
			} else {
				*/page.setPosition(address);
			//}
			return page;
		}
		GeometricSequence[] pageRecords = new GeometricSequence[Consts.RECORDS_ON_PAGE];
		byte[] pageBytes = new byte[getPageSize()];
		int size = 0;
		String key = "";
		
		getDataFile().seek(address);
		
		getDataFile().read(pageBytes);
		
		ByteBuffer wrapper = ByteBuffer.wrap(pageBytes);
		int depth = wrapper.getInt();
		depth = (depth == 0) ? 1 : depth;
		
		for(int i = 0; i < Consts.RECORDS_ON_PAGE; i++) {
			int id = wrapper.getInt();
			if(id < 1) {
				pageRecords[i] = null;
				wrapper.position(wrapper.position() + 2*Double.BYTES);
				continue;
			} else {
				//je¿eli id jest ró¿ne od -1, zak³adamy, ¿e rekord istnieje
				pageRecords[i] = new GeometricSequence(id, wrapper.getDouble(),
						wrapper.getDouble());
				size++;
			}
		}
		if(size != 0) {
			key = Consts.hash(pageRecords[0].getId(), depth);
		}
		setReadsCount(getReadsCount() + 1);
		return new DataPage(size, depth, address, key, pageRecords);
	}
	
	public static DataPage readDataPage(int address, int dictPosition) throws IOException {
		DataPage dp = readDataPage(address);
		if(dp.getSize() == 0) {
			dp.setKey(Consts.getBinaryRepr(dictPosition, dp.getDepth()));
		}
		return dp;
	}
	
	/**
	 * zapisuje strone danych do pliku pod wskazanym adresem
	 * Przed danymi strony, zapisana jest g³êbokosc strony
	 * @param dp
	 * @throws IOException 
	 */
	public static void writeDataPageAt(DataPage dp, int address) throws IOException {
		if(address == -1) {
			address = (int) (MetadataFile.getFreeDataFileAddresses().isEmpty() ? getDataFile().length()
					: MetadataFile.getLastFreeAddress());
			dp.setPosition(address);
		}
		getDataFile().seek(address);
		getDataFile().writeInt(dp.getDepth());
		
		for(int i = 0; i < Consts.RECORDS_ON_PAGE; i++) {
			if(i < dp.getRecords().length && dp.getRecords()[i] != null) {
				getDataFile().writeInt(dp.getRecords()[i].getId());
				getDataFile().writeDouble(dp.getRecords()[i].getFirstTerm());
				getDataFile().writeDouble(dp.getRecords()[i].getMultiplier());
			} else {
				getDataFile().writeInt(-1);
				getDataFile().writeDouble(-1.0);
				getDataFile().writeDouble(-1.0);
			}
		}
		setSavesCount(getSavesCount() + 1);
	}
	
	/**
	 * Zapisuje now¹ strone z danymi, albo na koncu pliku, albo w wolnym miejscu zapisanym w liscie
	 * @param dp
	 * @return
	 * @throws IOException
	 */
	public static int writeDataPage(DataPage dp) throws IOException {
		int writeAddr = 0;
		if(MetadataFile.getFreeDataFileAddresses().isEmpty()) {
			writeAddr = (int) getDataFile().length();
		} else {
			writeAddr = MetadataFile.getLastFreeAddress();
		}
		writeDataPageAt(dp, writeAddr);
		return writeAddr;
	}
	
	public static void deleteDataPage(int address) throws IOException {
		if(address == getDataFile().length() - Consts.getDataPageSize()) {
			getDataFile().setLength(getDataFile().length() - Consts.getDataPageSize());
		} else {
			MetadataFile.getFreeDataFileAddresses().add(address);
		}
	}
	
	/**
	 * Drukuje zawartoœæ pliku na wyjœcie programu
	 */
	public static void printFile() {
		try {
			for(int i = 0; i < getDataFile().length(); i += Consts.getDataPageSize()) {
				if(!MetadataFile.getFreeDataFileAddresses().contains(i)) {
					DataPage readedPage = readDataPage(i);
					for(int j = 0; j < readedPage.getSize(); j++) {
						System.out.println(readedPage.getRecords()[j].getId() + "\t" 
								+ readedPage.getRecords()[j].getFirstTerm() + "\t" + 
								readedPage.getRecords()[j].getMultiplier());
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void close() throws IOException {
		getDataFile().close();
	}
	
	public static void resetCounters() {
		setReadsCount(0);
		setSavesCount(0);
	}
	
	public static int getFileLength() {
		try {
			return (int) getDataFile().length();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return 0;
	}
	
	private static int getPageSize() {
		return Consts.DATA_RECORD_SIZE * Consts.RECORDS_ON_PAGE + 4/*depth*/;
	}
	
	private static RandomAccessFile getDataFile() {
		return dataFile;
	}
	private static void setReadsCount(int readsCount) {
		DataFile.readsCount = readsCount;
	}
	private static void setSavesCount(int savesCount) {
		DataFile.savesCount = savesCount;
	}
	public static int getReadsCount() {
		return readsCount;
	}
	public static int getSavesCount() {
		return savesCount;
	}
}
