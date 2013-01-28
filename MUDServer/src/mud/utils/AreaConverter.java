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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Scanner;

/*
 * Converter for MUD Area Files for:
 * - Circle, Diku, etc
 * 
 * to use:
 * AreaConverter ac = new AreaConverter();
 * 
 * ac.convert(file)
 */
public class AreaConverter {
	
	private static final String MAIN_DIR = new File("").getAbsolutePath() + "\\"; // Program Directory
	private static final String DATA_DIR = MAIN_DIR + "data\\";                   // Data Directory
	
	String directory = DATA_DIR + "test\\"; // the default area file directory
	String extension = ".are";              // area file extension we use (.are)
	
	public AreaConverter(String areaDirectory, String areaExtension) {
	}
	
	public AreaFile convert(String filename) {
		// CircleMUD Area File
		if(filename.regionMatches(true, filename.length() - 3, ".are", 0, 3) == true) {
			String[] fileArray = loadStrings(DATA_DIR + "test\\" + filename);
		}
		return null;
	}
	
	/**
	 * convert all files in the area directory to our format.
	 */
	public void convertAll() {
	}
	
	public void parse(String[] data) {
	}
	
	public static class AreaFile {
		File file;
		PrintWriter out;
		
		/**
		 * Creates a new "area file" with the specified filename
		 * @param filename
		 */
		public AreaFile(String filename) {
			try {
				this.out = new PrintWriter(new BufferedWriter(new FileWriter("foo.out")));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		public void close() {
		}
		
		public void open() {
		}
		
		public String read(int line) {
			return "";
		}
		
		/* write strings to file at a given position */
		public void write(int position) {
		}
		
		/* write a line to a file at a given position */
		public void writeln(int position) {
		}
		
		/* write a line at the end of a file */
		public void append() {
		}
		
		/* the size of the file in number of: bytes?, strings?, etc */
		public int length() {
			return 0;
		}
	}
	

	// kludges to temporarily make up for not importing the processing.core.* library
	public static String[] loadStrings(String filename) {
		File file = null;
		
		try {
			file = new File(filename);
			
			if(!(file instanceof File)) {
				throw new FileNotFoundException("Invalid File!");
			}
			else {
				Scanner input = null;
				
				ArrayList<String> output;
				
				if(file.isFile() && file.canRead()) {
					try {
						input = new Scanner(file);
						
						output = new ArrayList<String>();
						
						while(input.hasNextLine()) {
							output.add(input.nextLine());
						}
						
						return (String[]) output.toArray( new String[ output.size() ] );
					}
					catch(FileNotFoundException fnfe) {
						fnfe.printStackTrace();
					}
				}
			}
		}
		catch(FileNotFoundException fnfe) {
			fnfe.printStackTrace();
		}
		return null;
	}
}