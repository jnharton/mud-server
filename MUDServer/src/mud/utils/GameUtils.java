package mud.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import mud.misc.BBEntry;
import mud.misc.BulletinBoard;
import mud.objects.Item;

public class GameUtils {
	// try to do same as other function without exposing the t
	public static List<String> loadListDatabase(final String filename) {
		final String[] string_array;     // create string array
		final ArrayList<String> strings; // create arraylist of strings

		string_array = Utils.loadStrings(filename);

		strings = new ArrayList<String>(string_array.length);

		for (int line = 0; line < string_array.length; line++) {
			// we will completely drop any empty or -commented out lines
			if( !string_array[line].isEmpty() && string_array[line].charAt(0) != '#') {
				strings.add(string_array[line]);
			}
			//else debug("-- Skip - Line Commented Out --", 2);
		}
		
		strings.trimToSize(); // trim off any unused space

		return strings;
	}
	
	// NOTE function as extracted from MUDServer.java (3.3.2021)
	/*public ArrayList<String> loadListDatabase(String filename) {
		String[] string_array; // create string array
		ArrayList<String> strings; // create arraylist of strings

		string_array = Utils.loadStrings(filename);

		strings = new ArrayList<String>(string_array.length);

		for (int line = 0; line < string_array.length; line++) {
			// we will completely drop any empty lines, as they are irrelevant
			if( !string_array[line].isEmpty() ) {
				if (string_array[line].charAt(0) != '#')
					strings.add(string_array[line]);
				else
					debug("-- Skip - Line Commented Out --", 2);
			}
		}

		return strings;
	}*/
	
	private BulletinBoard loadBoard(final String fileName, final List<String> boardData) {
		// TODO should derive board name from board file
		// create bulletin board object
		final BulletinBoard bb = new BulletinBoard("temp", fileName);

		bb.setInitialCapacity( boardData.size());
		
		boolean name_set = false;
		int count = 0;

		for (final String line : boardData) {
			final String[] entryInfo = line.split("#");

			if( line.startsWith("!") ) {
				if( !name_set ) {
					final String nameLine = line.substring(1).trim();

					final int dot = nameLine.indexOf('.');

					if( dot != -1 ) {
						bb.setName( nameLine.substring(0, dot) );
						bb.setShortName( nameLine.substring(dot + 1, nameLine.length()) );
					}
					else {
						bb.setName(nameLine);
					}

					System.out.println("Board name set to: " + bb.getName());

					name_set = true;
				}
			}
			else {
				try {
					final int id = Integer.parseInt(entryInfo[0]);
					final String author = entryInfo[1];
					final String subject = entryInfo[2];
					final String message = entryInfo[3];			

					bb.loadEntry( new BBEntry(id, author, subject, message) );
				}
				catch (final NumberFormatException nfe) {
					//debug( String.format("line %d:", count) );
					//debug("setup(): loading bulletin board entries (number format exception)");

					//debug( nfe );
					
					System.out.println( nfe );
				}
				catch (final ArrayIndexOutOfBoundsException aioobe) {
					//debug( String.format("line %d:", count) );
					//debug("setup(): loading bulletin board entries (index out of bounds)");
					
					//debug( aioobe );
					
					System.out.println( aioobe );
				}
			}

			count++;
		}

		return bb;
	}
	
	public static Predicate<Item> itemIsUnique() {
	    return p -> p.isUnique();
	}
	
	public static Predicate<Item> itemIsNotUnique() {
	    return p -> !p.isUnique();
	}
	
}
