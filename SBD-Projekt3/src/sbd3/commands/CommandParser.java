package sbd3.commands;

import java.io.IOException;

import sbd3.data.GeometricSequence;
import sbd3.dictionary.Dictionary;
import sbd3.files.DataFile;

public class CommandParser {

	public static boolean ParseCommand(Dictionary dictionary, String command) throws IOException {
		String[] commandWords = command.split(" ");
		if(commandWords.length == 1 && commandWords[0].equals("exit")) {
			return false;
		} else if(commandWords.length == 4 && commandWords[0].equals("+")) {
			int id = Integer.parseInt(commandWords[1]);
			double firstTerm = Double.parseDouble(commandWords[2]),
					multiplier = Double.parseDouble(commandWords[3]);
			if(id >= 0) {
				dictionary.insert(new GeometricSequence(id, firstTerm, multiplier));
			} else {
				System.out.println("ID nie mo�e by� liczb� ujemn�");
			}
		} else if(commandWords.length == 2 && commandWords[0].equals("-")) {
			int id = Integer.parseInt(commandWords[1]);
			dictionary.delete(id);
		} else if(commandWords.length == 4 && commandWords[0].equals("*")) {
			int id = Integer.parseInt(commandWords[1]);
			double firstTerm = Double.parseDouble(commandWords[2]),
					multiplier = Double.parseDouble(commandWords[3]);
			if(id >= 0) {
				dictionary.modify(id, firstTerm, multiplier);
			} else {
				System.out.println("ID nie mo�e by� liczb� ujemn�");
			}
		} else if(commandWords.length == 1 && commandWords[0].toLowerCase().equals("showdc")) {
			dictionary.printDict();
		} else if(commandWords.length == 1 && commandWords[0].toLowerCase().equals("showdt")) {
			DataFile.printFile();
		} else if(commandWords.length == 1 && commandWords[0].toLowerCase().equals("help")) {
			System.out.println("Dost�pne polecenia:\nDodanie rekordu:\t+ <id> <1. wyraz ci�gu> <iloraz ci�gu>\nUsuwanie rekordu:\t- <id>"
					+ "\nModyfikacja rekordu:\t* <id> <nowy 1. wyraz ci�gu> <nowy iloraz ci�gu>\nWyszukiwanie rekordu:\tf <id>\n"
					+ "Wydruk skorowidza:\tshowDc (wielko�� liter nie ma znaczenia)\nWydruk danych:\t\tshowDt (wielko�� liter nie ma znaczenia)"
					+ "\nPomoc:\t\t\thelp (wielko�� liter nie ma znaczenia)\nWyj�cie z programu\texit (wielko�� liter nie ma znaczenia)");
		} else if(commandWords.length == 2 && commandWords[0].toLowerCase().equals("f")) {
			int id = Integer.parseInt(commandWords[1]);
			GeometricSequence foundedRecord = dictionary.find(id);
			if(foundedRecord != null) {
				System.out.println("ID: " + foundedRecord.getId() + "\tPierwszy wyraz ci�gu: " +
						foundedRecord.getFirstTerm() + "\tIloraz: " + foundedRecord.getMultiplier());
			} else {
				System.out.println("Nie znaleziono rekordu o ID " + id);
			}
		} else {
			System.out.println("Nie ma takiej komendy");
		}
		return true;
	}
	
}
