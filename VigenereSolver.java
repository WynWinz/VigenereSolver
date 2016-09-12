/*
 * Edwin Mellett
 * 4/10/16
 * Vigenere Solver
*/

import java.util.*;
import java.io.*;
import java.lang.Math.*;

public class VigenereSolver {

	private static int keyIndex = 0;
	private static ArrayList<String> cipherText = new ArrayList<String>();
	private static ArrayList<String> firstLine = new ArrayList<String>();
	private static BufferedReader br;
	private static char alphabet[] = "abcdefghijklmnopqrstuvwxyz".toCharArray();
	private static String output;

	public static void main(String[] args) throws IOException {
		if(args.length != 2) {
			System.out.println("java vigenereSolver <cipher file> <output file>");
			System.exit(-1);
		}
		output = args[1];
		String cipher = readFile(args[0]);
		int keyLength = determineKeyLength(cipher);
		String key = analyzeCipherText(cipher, keyLength);
		decryptFile(key);

		System.out.println("Key found: " + key);
	}

	private static String decrypt(String word, String key) {
		char plainText[] = word.toCharArray();
		char keyChars[] = key.toCharArray();
		for(int i = 0; i < plainText.length; i++) {
			if(plainText[i] < 97 || plainText[i] > 122) {
				//no decrypt needed
				continue;
			}
			else {
				int shift = keyChars[keyIndex] - 97;
				int index = (plainText[i] - 97 - shift) % 26;
				if(index < 0) {
					index = 26 + index;
				}
				plainText[i] = (char)(97 + index);
				keyIndex = (keyIndex + 1) % keyChars.length;
			}
		}

		return new String(plainText);
	}

	private static void writeFile(ArrayList<String> plainText) throws IOException {
		BufferedWriter bw = new BufferedWriter(new FileWriter(output));
		for(int i = 0; i < plainText.size(); i++) {
			String word = plainText.get(i);
			bw.write(word + " ");
		}
		bw.close();
	}

	private static String readFile(String fileName) throws IOException, FileNotFoundException {
		br = new BufferedReader(new FileReader(fileName));
		StringBuilder sb = new StringBuilder();
		String line;
		boolean first = true;
		while((line = br.readLine()) != null) {
			String[] words;
			words = line.split(" ");
			for(int i = 0; i < words.length; i++) {
				if(first) {
					firstLine.add(words[i]);
				}
				cipherText.add(words[i]);
				char[] letters = words[i].toCharArray();
				for(char letter : letters) {
					if(letter >= 97 && letter <= 122) {
						sb.append(letter);
					}
				}	
			}
			first = false;
		}
		br.close();
		return sb.toString();
	}

	private static void decryptFile(String key) throws IOException {
		ArrayList<String> plainText = new ArrayList<String>();
		for(int i = 0; i < cipherText.size(); i++) {
			plainText.add(decrypt(cipherText.get(i), key));
		}
		writeFile(plainText);
	}

	private static int determineKeyLength(String cipher) {
		int index = 0;
		char cipherOriginal[] = cipher.toCharArray();
		int numMatches[] = new int[15];

		//shift determines key length from 2 to 15
		for(int shift = 2; shift < 15; shift++) {
			for(int i = 0; i < cipherOriginal.length; i++) {

				if(i+shift+2 >= cipherOriginal.length) {
					break;
				}
				//compare shifted message with original message
				if(cipherOriginal[i] == cipherOriginal[i+shift]) {
					numMatches[shift] += 1;
				}
			}
		}
		int keyLength = 0;
		int matches = 0;
		for(int j = 2; j < numMatches.length; j++) {
			//find key length by most matches
			if(matches < numMatches[j]) {
				keyLength = j;
				matches = numMatches[j];
			}
		}
		return keyLength;
	}

	private static String analyzeCipherText(String cipher, int keyLength) {
		Hashtable<Integer, StringBuilder> letterColumns = new Hashtable<Integer, StringBuilder>();
		StringBuilder key = new StringBuilder();
		char cipherArray[] = cipher.toCharArray();

		//split ciphertext into n columns, where n = key length
		for(int col = 0; col < keyLength; col++) {
			StringBuilder column = new StringBuilder();
			//copy every nth letter into column
			for(int i = col; i < cipherArray.length; i+=keyLength) {
				column.append(cipherArray[i]);
			}
			letterColumns.put(col, column);
		}

		//find each letter of the key
		for(int i = 0; i < keyLength; i++) {
			char keyChar = determineShift(letterColumns.get(i));
			key.append(keyChar);
		}

		return key.toString();
	}

	private static char determineShift(StringBuilder columnText) {
		Hashtable<Character, Double> letterFrequency = buildFrequencyTable();
		Hashtable<Character, Double> cipherFrequency = computeFrequency(columnText);
		char keyChar;

		double minError = Double.MAX_VALUE;
		int shiftValue = -1;
		//try each caesar shift
		for(int shift = 0; shift < alphabet.length; shift++) {
			double error = 0;
			for(char c : alphabet) {
				char shifted = (char)(c+shift);
				//essentially modular division for ASCII lowercase alphabet
				if(shifted > 122) {
					shifted = (char) (shifted - 122);
					shifted = (char) (shifted + 97);
				}
				//calculates error of caesar shift vs english letter frequency
				double diff = Math.abs(letterFrequency.get(c) - cipherFrequency.get(shifted));
				error += diff;
			}
			//find shift with minimum error
			if(error < minError) {
				shiftValue = shift;
				minError = error;
			}
		}

		keyChar = (char)('a' + shiftValue);
		return keyChar;
	}

	private static Hashtable<Character, Double> computeFrequency(StringBuilder columnText) {
		Hashtable<Character, Double> letterFrequency = new Hashtable<Character, Double>();
		char cipherArray[] = columnText.toString().toCharArray();
		double frequency = 0;
		double count = 0;
		double totalChars = cipherArray.length;

		//counts the frequency of each letter per column of ciphertext
		for(char c : alphabet) {
			for(char cipher : cipherArray) {
				if(cipher == c) {
					count++;
				}
			}
			frequency = count / totalChars;
			letterFrequency.put(c, frequency);
			count = 0;
			frequency = 0;
		}

		return letterFrequency;
	}

	private static Hashtable<Character, Double> buildFrequencyTable() {
		Hashtable<Character, Double> letterFrequency = new Hashtable<Character, Double>();
		//these values come from an english letter frequency chart
		letterFrequency.put('a', .08167);
		letterFrequency.put('b', .01492);
		letterFrequency.put('c', .02782);
		letterFrequency.put('d', .04253);
		letterFrequency.put('e', .12702);
		letterFrequency.put('f', .0228);
		letterFrequency.put('g', .02015);
		letterFrequency.put('h', .06094);
		letterFrequency.put('i', .06966);
		letterFrequency.put('j', .00153);
		letterFrequency.put('k', .00772);
		letterFrequency.put('l', .04025);
		letterFrequency.put('m', .02406);
		letterFrequency.put('n', .06749);
		letterFrequency.put('o', .07507);
		letterFrequency.put('p', .01929);
		letterFrequency.put('q', .00095);
		letterFrequency.put('r', .05987);
		letterFrequency.put('s', .06327);
		letterFrequency.put('t', .09056);
		letterFrequency.put('u', .02758);
		letterFrequency.put('v', .00978);
		letterFrequency.put('w', .02361);
		letterFrequency.put('x', .00150);
		letterFrequency.put('y', .01974);
		letterFrequency.put('z', .00074);

		return letterFrequency;
	}
}