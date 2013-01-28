package mud.utils;

/*
Copyright (c) 2012 Jeremy N. Harton

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit
persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the
Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class Utils {

	/**
	 * Hash Function
	 * 
	 * hash function (simple alphabetical shift and a reverse line for testing)
	 * some kind of hash function for password shadowing, that is: we'll store the
	 * hashed version and check the input password by hashing it and comparing it
	 * to the stored hash.
	 * 
	 * @param input a string of non-zero length
	 * @return a hashed string, a string generated based on the input that is single, and unique to the input combination
	 */
	public static String hash(String input)
	{
		// consider replacing strings with character arrays while working?

		// two character forward alphabetical shift in set
		String alphabet =  "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
		String alphabet1 = "cdefghijklmnopqrstuvwxyzabCDEFGHIJKLMNOPQRSTUVWXYZAB";

		// letters for words swap
		String[] special = new String[] { "ARENDIA", "BORDER", "CAIPHAS", "DESTRUCTION", "EMINENT",
				"FAILURE", "GRENADE", "HEXAGON", "IDRIS", "JARL", "KRPYTOS", "LAMELLAR", "MYSTERIOUS",
				"NONAGON", "OCTAVE", "PRINCESS", "QUEUE", "RATIONAL", "SINISTER", "TORRENTIAL", "UBUNTU",
				"VIVACIOUS", "WATER", "XYLOPHONE", "YEOMAN", "ZOSTER" };


		String working = "";
		String rtn = "";

		// letters for words swap (case is preserved)
		for(int c = 0; c < input.length(); c++)
		{
			Character ch = input.charAt(c); // get the character at the current position in the input string
			String replace = "";            // place to store the replacement word for this character

			if(Character.isLowerCase(ch) == true) {
				replace = special[alphabet.indexOf(ch)].toLowerCase();
			}
			else if(Character.isUpperCase(ch) == true) {
				replace = special[alphabet.indexOf(ch)].toUpperCase();
			}

			working += replace;
		}

		// swap each character with the one two spaces ahead of it in the alphabet (case included)
		for(int c = 0; c < working.length(); c++)
		{
			rtn = rtn + str(alphabet1.charAt(alphabet.indexOf(working.charAt(c))));
		}

		// take the shift crypto and reverse the line for additional obfuscation
		rtn = flipLine(rtn);

		// drop the second half of it (future feature)?
		//rtn = rtn.substring(0, rtn.length() / 2);

		// return the outputs
		return rtn;
	}

	/**
	 * flipLine
	 * 
	 * flip a line around and return a reversed copy
	 * 
	 * @param temp the string to be flipped
	 * @return the flipped string
	 */
	public static String flipLine(String temp)
	{
		StringBuffer arga = new StringBuffer(temp.length());

		// loop through the string, character by character
		for(int c = 0; c < temp.length(); c++)
		{
			// add each character back to the string, reversing it completely
			arga.append(temp.charAt(temp.length() - c - 1));
		}

		// return a string as the result
		return arga.toString();
	}

	public static String str(Object o) {
		return o.toString();
	}

	/**
	 * Saves string arrays to files (the assumption being that said file is a
	 * collection/list of arbitrary strings in a particular layout
	 * 
	 * @param filename
	 * @param sArray
	 */
	public static void saveStrings(String filename, String[] sArray) {
		File file = null;
		PrintWriter output = null;

		try {
			file = new File(filename);
			if( !(file instanceof File) ) {
				throw new FileNotFoundException("Invalid File!");
			}
			else {
				output = new PrintWriter(file);
				for(String s : sArray) {
					output.println(s);
				}
				output.flush();
				output.close();
			}
		}
		catch(FileNotFoundException fnfe) {
			fnfe.printStackTrace();
		}
	}

	/**
	 * Load Strings
	 * 
	 * Load strings from a file into a string array.
	 * 
	 * @param filename
	 * @return
	 */
	public static String[] loadStrings(String filename) {
		File file;
		FileReader fReader;

		try {
			file = new File(filename); // open the specified file

			if(!(file instanceof File)) {
				throw new FileNotFoundException("Invalid File!");
			}
			else {
				Scanner input;
				ArrayList<String> output;

				if(file.isFile() && file.canRead()) {
					fReader = new FileReader(file);   // pass the file to the file reader

					input = new Scanner( fReader );   // pass the file reader to the scanner
					output = new ArrayList<String>(); // create the output arraylist

					while(input.hasNextLine()) {
						output.add(input.nextLine());
					}

					input.close();                    // close the scanner, and implicity the filereader and file

					return (String[]) output.toArray( new String[ output.size() ] ); // return the new thing as an array
				}
			}
		}
		catch(FileNotFoundException fnfe) {
			System.out.println( fnfe.getMessage() );
			System.out.println("--- Stack Trace ---");
			fnfe.printStackTrace();
		}
		catch(NullPointerException npe) {
			System.out.println("--- Stack Trace ---");
			npe.printStackTrace();
		}

		return new String[0]; // return an non-empty,non-full zero size String[]
	}

	public static byte[] loadBytes(String filename) {
		File file;
		FileInputStream fis;

		byte[] byte_array = null;
		int index = 0;

		try {
			file = new File(filename);         // create a new file object to refer to the file
			fis = new FileInputStream(file);   // create a new file input stream to read from the file

			byte_array = new byte[(int) file.length()]; // create a new byte array to hold the file

			try {
				while(fis.available() > 0) {
					byte_array[index] = (byte) fis.read();
					index++;
				}
			}
			catch (IOException ioe) {
				ioe.printStackTrace();
			}
		}
		catch (FileNotFoundException fnfe) {
			fnfe.printStackTrace();
		}

		return byte_array;
	}

	public static String join(String[] in, String sep) {
		String out = "";

		for(int l = 0; l < in.length; l++) {
			if(l < in.length - 1) {
				out = out + in[l] + sep;
			}
			else {
				out = out + in[l];
			}
		}

		return out;
	}

	/**
	 * convert string array to integer array
	 * 
	 * @param in a string array
	 * @return an integer array 
	 */
	public static Integer[] stringsToIntegers(String[] in) {
		Integer[] out = new Integer[in.length];
		for (int l = 0; l < in.length; l++)
		{
			try {
				out[l] = Integer.parseInt(in[l]);
			}
			catch(NumberFormatException nfe) {
				nfe.printStackTrace();
				System.out.println("Cannot parse element");
			}
		}
		return out;
	}
	
	public static int[] stringsToInts(String[] in) {
		int[] result = new int[in.length];

		int index = 0;

		for(String string : in) {
			try {
				result[index] = Integer.parseInt(string);
				index++;
			}
			catch(NumberFormatException nfe) {
				nfe.printStackTrace();
				System.out.println("Cannot parse element");
			}
		}
		
		return result;
	}


	public static String[] parseArray(String s, String sep) {
		String[] strings = s.split(sep);

		return strings;
	}
	
	public static String padRight(String s) {
		return String.format("%1$-70s", s);
	}
	
	public static String padLeft(String s) {
		return String.format("%1$70s", s);
	}

	public static String padRight(String s, int n) {
		return String.format("%1$-" + n + 's', s);
	}

	public static String padLeft(String s, int n) {
		return String.format("%1$" + n + 's', s);
	}

	public static String padRight(String s, char padChar, int n) {
		return String.format("%1$-" + n + 's', s).replace(' ', padChar);
	}

	public static String padLeft(String s, char padChar, int n) {
		return String.format("%1$" + n + "s", s).replace(' ', padChar);
	}

	public static <T> T[] expand(T[] objects) {
		return Arrays.copyOfRange(objects, 0, objects.length + 10);
	}
	
	public static String[] subset(String[] in, int begin) {
		String[] out;
		int index = 0;
		System.out.println(in.length);
		out = new String[in.length - begin];
		for(int l = begin; l < in.length; l++) {
			out[index] = in[l];
			index++;
		}
		return out;
	}

	public static String[] subset(String[] in, int begin, int end) {
		String[] out;
		out = new String[end - begin];
		for(int l = begin; l < end; l++) {
			if(in[l] != null) {
				out[l] = in[l];
			}
		}
		return out;
	}

	public static String trim(String s) {
		return s.trim();
	}
	
	/**
	 * string matching
	 * 
	 * @param input
	 * @param regexp
	 * @return
	 */
	public static boolean match(String input, String regexp) {
		Pattern p = Pattern.compile(regexp);
		Matcher m = p.matcher(input);
		boolean test = m.find();
		return test;
	}
	
	/**
	 * Convert an arraylist of strings to a string array
	 * 
	 * @param list the arraylist to converted
	 * @return the converted arraylist (a string array)
	 */
	public static String[] arraylistToString(ArrayList<String> list) {
		String[] result = new String[list.size()];

		int index = 0;

		for(String s : list) {
			result[index] = s;
			index++;
		}

		return result;
	}
	
	
	/**
	 * d20 dice roller
	 * 
	 * @param sides
	 * @param number
	 * @return
	 */
	public static int roll(int number, int sides) { // roll(3, 4) for 3d4
		int i = 0;

		for(int n = 0; n < number; n++) {
			int result = ((int)(Math.random() * sides));
			i += result;
		}

		return i;
	}

	public static int roll(String dice) { // roll(3d4+1)
		int number = 0;
		int sides = 0;
		int modifier = 0;
		
		int roll = 0;
		int result = 0;

		// 3d4 = 3 d 4
		// 3d4+1 = 3 d 4 + 1
		try {

			number = Integer.parseInt(dice.substring(0, dice.indexOf("d")));
			
			if(dice.indexOf("+") == -1) {
				sides = Integer.parseInt(dice.substring(dice.indexOf("d") + 1, dice.length()));
			}
			else {
				sides = Integer.parseInt(dice.substring(dice.indexOf("d") + 1, dice.indexOf("+")));
				modifier = Integer.parseInt(dice.substring(dice.indexOf("+") + 1, dice.length()));
			}
			
			System.out.println(number);
			System.out.println(sides);
			System.out.println(modifier);
			System.out.println("" + number + "d" + sides + "+" + modifier);
			
			System.out.println("Rolling " + number + "d" + sides);
			
			roll = roll(number, sides);
			System.out.println("roll: " + roll);
			
			result = roll + modifier;
			System.out.println("result: " + result);
		}
		catch(NumberFormatException nfe) {
		}
		
		return result;
	}
	
	/* Unused */
	
	/*
	// array expansions functions for specific array types (Unneeded now, due to arraylist use)
	public static Object[] expand(Object[] in, int newSize) {
		Object[] out;
		out = Arrays.copyOf(in, newSize);
		return out;
	}

	public static int[] expand(int[] in, int newSize) {
		int[] out;
		out = Arrays.copyOf(in, newSize);
		return out;
	}

	public static Integer[] expand(Integer[] in, int newSize) {
		Integer[] out;
		out = Arrays.copyOf(in, newSize);
		return out;
	}

	public static String[] expand(String[] in, int newSize) {
		String[] out;
		out = Arrays.copyOf(in, newSize);
		return out;
	}
	
	// other
	public static Client[] expand(Client[] in) {
		Client[] out;
		out = Arrays.copyOf(in, in.length + 1);
		return out;
	}

	public static Integer[] shorten(Integer[] in) {
		Integer[] out;
		out = Arrays.copyOf(in, in.length - 1);
		return out;
	}

	public static String[] shorten(String[] in) {
		String[] out;
		out = Arrays.copyOf(in, in.length - 1);
		return out;
	}

	public static String[] concat(String[] one, String[] two) {
		String[] out;
		out = new String[one.length + two.length];
		for(int l = 0; l < out.length; l++) {
			if(one.length - 1 > l) {
				out[l] = one[l];
			}
			else if(two.length - 1 > l - (one.length - 1)) {
				out[l] = two[l - (one.length - 1)];
			}
		}
		return out;
	}
	*/
}