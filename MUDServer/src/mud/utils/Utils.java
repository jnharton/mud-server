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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.security.spec.KeySpec;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public final class Utils {

	private static SecretKeyFactory f;
	static {
		try {
			f = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}
	final private static byte[] salt = new byte[16];
	final private static Random random = new Random(6456856);
	static {
		random.nextBytes(salt);
	}

	/**
	 * Hash Function
	 * from http://stackoverflow.com/questions/2860943/suggestions-for-library-to-hash-passwords-in-java
	 * 
	 * @param input a string of non-zero length
	 * @return a hashed string, a string generated based on the input that is single, and unique to the input combination
	 */
	public static String hash(final String password) {
		final KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, 65536, 128);
		try {
			final byte[] hash = f.generateSecret(spec).getEncoded();
			return new BigInteger(1, hash).toString(16);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
		return null;
	}

	/**
	 * Hash Function (old)
	 * 
	 * hash function (simple alphabetical shift and a reverse line for testing)
	 * some kind of hash function for password shadowing, that is: we'll store the
	 * hashed version and check the input password by hashing it and comparing it
	 * to the stored hash.
	 * 
	 * @param input a string of non-zero length
	 * @return a hashed string, a string generated based on the input that is single, and unique to the input combination
	 */
	public static String hash_old(String input)
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
			rtn = rtn + alphabet1.charAt(alphabet.indexOf(working.charAt(c)));
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

	public static String reverseString(final String temp)
	{
		final StringBuilder buf = new StringBuilder();

		for (int j = temp.length() - 1; j >= 0; j--) {
			buf.append(temp.charAt(j));
		}
		return buf.toString();
	}

	// defunct, make sure to clear any extant references
	/*public static String str(Object o) {
		return o.toString();
	}*/

	/**
	 * Saves string arrays to files (the assumption being that said file is a
	 * collection/list of arbitrary strings in a particular layout
	 * 
	 * @param filename
	 * @param sArray
	 */
	public static void saveStrings(final String filename, final String[] sArray) {
		try {
			final File file = new File(filename);
			final PrintWriter output = new PrintWriter(file);
			for (final String s : sArray) {
				output.println(s);
			}
			output.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	// old
	/**
	 * Saves string arrays to files (the assumption being that said file is a
	 * collection/list of arbitrary strings in a particular layout
	 * 
	 * @param filename
	 * @param sArray
	 */
	/*public static void saveStrings(String filename, String[] sArray) {
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
	}*/

	/**
	 * Load Strings
	 * 
	 * Load strings from a file into a string array.
	 * 
	 * @param filename
	 * @return
	 */
	public static String[] loadStrings(final String filename) {
		try {
			final File file = new File(filename);
			final BufferedReader br = new BufferedReader(new FileReader(file));
			final ArrayList<String> output = new ArrayList<String>();
			String line;
			while ((line = br.readLine()) != null) {
				output.add(line);
			}
			br.close();
			return output.toArray(new String[0]);
		}
		catch(Exception e) {
			System.out.println("--- Stack Trace ---");
			e.printStackTrace();
		}
		return new String[0];
	}

	// old
	/**
	 * Load Strings
	 * 
	 * Load strings from a file into a string array.
	 * 
	 * @param filename
	 * @return
	 */
	/*public static String[] loadStrings(String filename) {
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
	}*/

	public static byte[] loadBytes(final String filename) {
		try {
			final File file = new File(filename);
			final FileInputStream fis = new FileInputStream(file);
			final int FILE_LEN = (int) file.length();
			final byte[] byte_array = new byte[FILE_LEN]; // create a new byte array to hold the file
			int index = 0;
			while (index < FILE_LEN) {
				final int numRead = fis.read(byte_array, index, FILE_LEN - index);
				index += numRead;
			}
			fis.close();
			return byte_array;
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		finally {
		}

		return new byte[0];
	}

	/*public static byte[] loadBytes(String filename) {
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
	}*/

	public static String join(final String[] in, final String sep) {
		return join(Arrays.asList(in), sep);
	}

	// old
	/*public static String join(String[] in, String sep) {
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
	}*/

	public static String join(final List<String> in, String sep) {
		if (sep == null) sep = "";
		if (in.size() == 0) return "";
		final StringBuilder out = new StringBuilder();
		for (final String s : in) {
			out.append(sep).append(s);
		}
		return out.delete(0, sep.length()).toString();
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

	public static int[] stringsToInts(final String[] in) {
		int[] result = new int[in.length];
		int index = 0;

		for (final String str : in) {
			result[index] = Utils.toInt(str, 0);
			index++;
		}

		return result;
	}

	// old
	/*public static int[] stringsToInts(String[] in) {
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
	}*/

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

	/**
	 * @author joshit?
	 * 
	 * @param s
	 * @param n
	 * @return
	 */
	public static String center(final String s, final int n) {
		String work = s;
		int str = work.length();
		int remain = n - work.length();

		System.out.println("str: " + str + " remain: " + remain);

		int left = 0;
		int right = 0;
		
		System.out.println("left: " + left + " right: " + right);
				
		if( remain > 0 ) {
			left = (int) (0.5 * remain);
			right = (int) (0.5 * remain);
			return padLeft("", ' ', left) +  s + padRight("", ' ', right + ( remain - (left + right) ));
		}
		else {
			return truncate(s, n);
		}
	}

	public static String truncate(final String s, final int n) {
		if( s.length() > n ) return s.substring(0, n);
		else return s;
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

	public static String trim(final String s) {
		return s == null ? null : s.trim();
	}

	// old
	/*public static String trim(String s) {
		return s.trim();
	}*/

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
	public static String[] listToStringArray(List<String> list) {
		String[] result = new String[list.size()];

		int index = 0;

		for(String s : list) {
			result[index] = s;
			index++;
		}

		return result;
	}
	
	public static String listToString(List<Integer> list) {
		StringBuilder sb = new StringBuilder();
		
		for(Integer i : list) {
			if( list.indexOf(i) != (list.size() - 1) ) sb.append( i + "," );
			else sb.append( i );
		}
		
		return sb.toString();
	}

	public static ArrayList<String> stringToArrayList(String s, String sep) {
		String[] stringArray = s.split(sep);

		ArrayList<String> stringList = new ArrayList<String>();

		for(String string : stringArray) {
			stringList.add(string);
		}

		return stringList;
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

	public static int roll(final int number, final int sides, final int bonus) { // roll(3, 4, 7) for 3d4+7
		return bonus + roll(number, sides);
	}

	public static int roll(final String input) { // roll(3, 4, 7) for 3d4+7
		final String[] words = input.split("d");

		final int num = Utils.toInt(words[0], 1);

		int sides = 0, bonus = 0;
		try {
			final String[] second_words = words[1].split("[-+]");
			sides = Utils.toInt(second_words[0], 0);
			bonus = Utils.toInt(second_words[1], 0);
		} catch (Exception e) {}

		return bonus + roll(num, sides);
	}

	// old roll
	/*public static int roll(String dice) { // roll(3d4+1)
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
	}*/

	/**
	 * A safe way to parse an integer elsewhere in the code. A default 
	 * value is specified in the parameters, which will be returned if
	 * a valid integer cannot be parsed from the input string. This
	 * catches and handles the exception that may arise
	 * 
	 * @param str the input string, which should contain a parsable integer
	 * @param alt the integer to return if the string does not contain a parsable integer
	 * @return
	 */
	public static int toInt(final String str, final int alt) {
		try {
			return Integer.parseInt(str);
		} catch(NumberFormatException nfe) {
			return alt;
		}
	}

	public static Point toPoint(final String ptData) {
		// (1,1)
		int fP = ptData.indexOf('('); // first left parentheses "(" 

		if(fP > -1) {
			int[] coordinates = stringsToInts(ptData.substring(fP + 1, ptData.indexOf(')', fP)).split(","));

			if(coordinates.length == 2) { // 2D Coordinate
				Point point = new Point(coordinates[0], coordinates[1]);
				return point;
			}
			else if(coordinates.length == 3) { // 3D Coordinate
				Point point = new Point(coordinates[0], coordinates[1], coordinates[2]);
				return point;
			}
			else {
				return new Point(0, 0, 0); // 2D/3D coordinate at 0,0 or 0,0,0 (blank) 
			}
		}

		return null;
	}

	public static List<Point> toPoints(final String ptData) {
		// params - two points in the form '(x,y)' separated by a ';'.
		// ex. (1,1),(4,4)

		List<String> params = new ArrayList<String>();

		StringBuffer sb = new StringBuffer();

		boolean check = true;

		// isolate coordinate points
		for(int c = 0; c < ptData.length(); c++) {
			char ch = ptData.charAt(c);

			if( check ) {
				if(ch == ')') {
					System.out.println("Current: " + sb.toString());
					System.out.println("Added character to sb");
					sb.append(ch);
					System.out.println("Final: " + sb.toString());
					System.out.println("finished point dec");
					check = false;
					params.add(sb.toString());
					sb.delete(0, sb.length());
				}
				else {
					System.out.println("Current: " + sb.toString());
					System.out.println("Added character to sb");
					sb.append(ch);
				}
			}
			else {
				if(ch == '(') {
					sb.delete(0, sb.length());
					System.out.println("started point dec");
					System.out.println("Current: " + sb.toString());
					System.out.println("Added character to sb");
					sb.append(ch);
					check = true;
				}
				else if(ch == ',') {

				}
			}
		}

		if(params.size() > 0) {

			List<Point> ptList = new ArrayList<Point>();

			for(String param : params) {
				ptList.add(Utils.toPoint(param));
			}

			return ptList;
		}

		return null;
	}

	/**
	 * Merge Sort (operates on lists)
	 * 
	 * "translated" from pseudo-code at the site below
	 * https://en.wikipedia.org/wiki/Merge_sort
	 * 
	 * text comments from the site copied as well
	 * 
	 * @param list
	 * @return
	 */
	public static List<Integer> merge_sort(List<Integer> list) {
		// if list size is 0 (empty) or 1, consider it sorted and return it
		// (using less than or equal prevents infinite recursion for a zero length m)
		if( list.size() <= 1) return list;

		// else list size is > 1, so split the list into two sublists
		List<Integer> left = new LinkedList<Integer>();
		List<Integer> right = new LinkedList<Integer>();
		int middle = list.size() / 2;

		int counter = 0;

		for(Integer i : list) {
			if( counter < middle ) left.add(i);
			else right.add(i);
			counter++;
		}

		// recursively call merge_sort() to further split each sublist
		// until sublist size is 1
		left = merge_sort(left);
		right = merge_sort(right);

		// merge the sublists returned from prior calls to merge_sort()
		// and return the resulting merged sublist
		return merge(left, right);
	}

	/**
	 * Merge (merges lists of integers)
	 * 
	 * "translated" from pseudo-code at the site below
	 * https://en.wikipedia.org/wiki/Merge_sort
	 * 
	 * text comments from the site copied as well
	 * 
	 * @param leftList
	 * @param rightList
	 * @return
	 */
	public static List<Integer> merge(List<Integer> leftList, List<Integer> rightList) {
		List<Integer> result = new LinkedList<Integer>();

		while( leftList.size() > 0 || rightList.size() > 0 ) {
			if( leftList.size() > 0 && rightList.size() > 0 ) {
				if( leftList.get(0) <= rightList.get(0) ) {
					result.add( leftList.get(0) );
					leftList = leftList.subList(1, leftList.size());
				}
				else {
					result.add( rightList.get(0) );
					rightList = rightList.subList(1, rightList.size());
				}
			}
			else if( leftList.size() > 0 ) {
				result.add( leftList.get(0) );
				leftList = leftList.subList(1, leftList.size());
			}
			else if( rightList.size() > 0 ) {
				result.add( rightList.get(0) );
				rightList = rightList.subList(1, rightList.size());
			}
		}

		return result;
	}

	/**
	 * unixToDate
	 * 
	 * 
	 * @param unix_timestamp
	 * @return
	 * @throws ParseException 
	 */
	public static String unixToDate(final long timestamp) throws ParseException {
		SimpleDateFormat sdf = new SimpleDateFormat("MMMM d, yyyy 'at' h:mm a");
		String date = sdf.format(timestamp);

		return date.toString();
	}

	/**
	 * Calculate straight line distance between two Point(s) on the cartesian
	 * plane X-Y.
	 * 
	 * @param start Point to start at
	 * @param end   Point to end at
	 * @return
	 */
	public static float distance(Point start, Point end) {
		if( start != null && end != null) {
			if( start != end ) {
				// use pythagorean theorem to get straight line distance
				// pythagorean theorem (simplified): a^2 + b^2 = c^2
				int rise = Math.abs( start.getY() - end.getY() ); // x (a)
				int run = Math.abs( start.getX() - end.getX() );  // y (b)

				//int distance = (int) Math.sqrt( square(rise) + square(run) ); // z (c)
				float distance = (float) Math.sqrt( square(rise) + square(run) ); // z (c)

				return distance;
				// calculate travel distance going at right angles
			}
			else {
				return 0;
			}
		}

		return -1;
	}
	
	public static int square(int num) {
		return num * num;
	}
	
	public static <T> List<T> mkList(T...objects) {
		List<T> result = new LinkedList<T>();
		for(T obj : objects) {
			result.add(obj);
		}
		
		return result;
		
	}
}