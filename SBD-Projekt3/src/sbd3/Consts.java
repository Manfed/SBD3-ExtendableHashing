package sbd3;

public class Consts {
	
	/**
	 * Rozmiar rekordu w skorowidzu - 2 inty(hash i adres strony)
	 */
	public static final  int DICTIONARY_ENTRY_SIZE = 4;
	
	/**
	 * Rozmiar rekordu w pliku danych - int(id) + 2*double(dane ci¹gu geom.)
	 */
	public static final  int DATA_RECORD_SIZE = Integer.BYTES + 2*Double.BYTES;
	
	/**
	 * Liczba rekordów na stronê danych
	 */
	public static int RECORDS_ON_PAGE;
	
	/**
	 * Rozmiar strony danych
	 */
	public static int getDataPageSize(){
		return DATA_RECORD_SIZE*RECORDS_ON_PAGE  + Integer.BYTES;
	}

	
	public static String hash(int value, int depth) {
		String binaryValue = Integer.toBinaryString(value);
		if(binaryValue.length() < depth) {
			binaryValue = String.format("%" + depth + "s", binaryValue).replace(" ", "0");
		}
		binaryValue = new StringBuffer(binaryValue).reverse().toString();
		binaryValue = binaryValue.substring(0, depth);
		
		return binaryValue;
	}
	
	public static int getDictionaryIndex(int value, int depth) {
		
		String hashValue = hash(value, depth);
		return Integer.parseInt(hashValue, 2);
	}
	
	public static String getBinaryRepr(int value, int depth) {
		String binaryValue = Integer.toBinaryString(value);
		if(binaryValue.length() < depth) {
			binaryValue = String.format("%" + depth + "s", binaryValue).replace(" ", "0");
		}
		binaryValue = binaryValue.substring(0, depth);
		
		return binaryValue;
	}
	
}
