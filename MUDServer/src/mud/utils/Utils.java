package mud.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
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

	public static String reverseString(final String temp)
	{
        final StringBuilder buf = new StringBuilder();
		for (int j = temp.length(); j >= 0; j--) {
            buf.append(temp.charAt(j));
		}
		return buf.toString();
	}

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
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return new byte[0];
	}

	public static String join(final String[] in, final String sep) {
        return join(Arrays.asList(in), sep);
	}

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
		for (int l = begin; l < in.length; l++) {
			out[index] = in[l];
			index++;
		}
		return out;
	}

	public static String trim(final String s) {
		return s == null ? null : s.trim();
	}

	/**
	 * d20 dice roller
	 * 
	 * @param sides
	 * @param number
	 * @return
	 */
	public static int roll(final int number, final int sides) { // roll(3, 4) for 3d4
		int i = 0;
		for (int n = 0; n < number; n++) {
			i += 1 + (int)(Math.random() * sides);
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
    
    public static int toInt(final String str, final int alt) {
        try {
            return Integer.parseInt(str);
        } catch (Exception e) {
            return alt;
        }
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
		for (int l = 0; l < out.length; l++) {
			if (one.length - 1 > l) {
				out[l] = one[l];
			}
			else if (two.length - 1 > l - (one.length - 1)) {
				out[l] = two[l - (one.length - 1)];
			}
		}
		return out;
	}
	*/
}