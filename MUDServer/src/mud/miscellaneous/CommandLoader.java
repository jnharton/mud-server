package mud.miscellaneous;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class CommandLoader extends ClassLoader {

	public CommandLoader() {
		super(CommandLoader.class.getClassLoader());
	}

	protected Class<?> findClass(String name) throws ClassNotFoundException {
		byte[] c = loadClassData(name);
		return defineClass(name, c, 0, c.length);
	}

	private byte[] loadClassData(String name) {
		try {
			File file = new File(name);

			if(file.exists() && file.isFile()) {
				InputStream is = new FileInputStream(file);

				// Get the size of the file
				long length = file.length();

				// You cannot create an array using a long type.
				// It needs to be an int type.
				// Before converting to an int type, check
				// to ensure that file is not larger than Integer.MAX_VALUE.
				if (length > Integer.MAX_VALUE) {
					// File is too large
					throw new NumberFormatException();
				}

				// Create the byte array to hold the data
				byte[] bytes = new byte[(int)length];

				// Read in the bytes
				int offset = 0;
				int numRead = 0;
				while (offset < bytes.length
						&& (numRead=is.read(bytes, offset, bytes.length-offset)) >= 0) {
					offset += numRead;
				}

				// Ensure all the bytes have been read in
				if (offset < bytes.length) {
					throw new IOException("Could not completely read file " + file.getName());
				}

				// Close the input stream and return bytes
				is.close();
				return bytes;
			}
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		
		return null;
	}

	/*protected final Class<?> findLoadedClass(String name) {
		return null;
	}*/
}