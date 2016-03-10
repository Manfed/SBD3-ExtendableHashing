package sbd3;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import com.google.common.primitives.Ints;

import sbd3.commands.CommandParser;
import sbd3.dictionary.Dictionary;
import sbd3.files.DataFile;
import sbd3.files.DictionaryFile;
import sbd3.files.MetadataFile;

public class App {

	public static void main(String[] args) throws IOException {
		
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		System.out.println("1 - NOWY PLIK\n2 - OTWÓRZ ISTNIEJ¥CY");
		String fileName = "";
		Dictionary dictionary = new Dictionary(1);
		//pliki
		DataFile df = null;
		DictionaryFile dictFile = null;
		MetadataFile mf = null;
		boolean commandPhase = true;
		String command = "";
		
		int mode = Integer.parseInt(in.readLine());
		switch (mode) {
		case 1:
			System.out.println("Nazwa pliku: ");
			fileName = in.readLine();
			System.out.println("Liczba rekordów na stronie: ");
			Consts.RECORDS_ON_PAGE = Integer.parseInt(in.readLine());
			
			//inicjalizacja plików
			mf = new MetadataFile(fileName, true);
			df = new DataFile(fileName, true);
			dictFile = new DictionaryFile(fileName, true);
			List<Integer> dictionaryValues = DictionaryFile.readDictionary();
			dictionary.setPages(Ints.toArray(dictionaryValues));
			break;
		case 2:
			System.out.println("Nazwa pliku: ");
			fileName = in.readLine();
			
			//inicjalizacja plików
			mf = new MetadataFile(fileName);
			df = new DataFile(fileName, false);
			dictFile = new DictionaryFile(fileName, false);
			
			dictionary.setDepth(DictionaryFile.getDictionaryDepth());
			break;
		default:
			System.out.println("Nie ma takiej komendy");
			return;
		}

		System.out.println("HELP - aby uzyskaæ listê poleceñ");
		while(commandPhase) {
			command = in.readLine();
			commandPhase = CommandParser.ParseCommand(dictionary, command);
		}
		
		in.close();
		mf.close();
		df.close();
		dictFile.close();
	}

}
