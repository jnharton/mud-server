package mud.foe.misc;

import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;

public class FileSystem {
	private static final int MAX_DIRS = 5;
	private static final int MAX_FILES = MAX_DIRS * 20; // 20 files per directory

	// directory name -> (file name -> file)
	//private Hashtable<String, Hashtable<String, String[]>> directories;
	public Hashtable<String, File> files;
	
	// File System structure
	// /                    root directory
	// /<file>              file in root dir
	// /<user>              user directory
	// /<user>/<file>       file in user dir
	// /<user>/<dir>/<file> user sub-directory
	//
	
	private int error;
	
	// ERRORS
	// max files reached
	// max dirs reached
	// copy failed
	// create file failed
	// delete file failed
	// no such file
	
	public class File {
		public boolean isDir = false;

		private String name;
		private String[] contents = null;

		public File(String name, String[] contents) {
			this.contents = contents;
		}

		public String getName() {
			return this.name;
		}

		public String[] getContents() {
			return this.contents;
		}
	}

	public class Directory extends File {
		public Hashtable<String, File> files;

		public Directory(final String name) {
			super(name, null);

			this.isDir = true;
			this.files = new Hashtable<String, File>();
		}
	}

	public FileSystem() {
		this.files = new Hashtable<String, File>();
		this.error = 0;
	}

	public Directory getDirectory(String directory) {
		final File file = this.files.get(directory);

		if( file != null ) {
			if( file.isDir ) {
				return (Directory) file;
			}
		}

		return null;
	}

	public File getFile(final String dirName, String fileName) {
		final File file = this.files.get(dirName);

		if( file != null ) {
			if( file.isDir ) {
				final Directory dir = (Directory) file;
				
				return dir.files.get(fileName);
			}
		}

		return null;
	}

	public String getDirectoryNames(String directory) {
		final StringBuilder sb = new StringBuilder();

		if( directory.equals("/") ) {
			for(String str : this.files.keySet()) {
				if( files.get(str).isDir ) {
					sb.append(str + " ");
				}
			}
		}
		/*else {
			for(String str : this.files.keySet()) {
				sb.append(str + " ");
			}
		}*/

		return sb.toString();
	}

	public boolean newDir(final String directory) {
		boolean directory_created = false;

		if( countDirs() < MAX_DIRS ) {
			this.files.put(directory, new Directory(directory));

			directory_created = true;
		}

		return directory_created;
	}

	/**
	 * 
	 * @param directory
	 * @param name
	 * @param data
	 * @return true if we successfully created the file
	 */
	public boolean newFile(final String directory, final String name, final String[] data) {
		boolean file_created = false;

		if( !hasDir(directory) ) {
			newDir(directory);
		}

		final Directory dir = getDirectory(directory);

		if( dir != null ) {
			///final Hashtable<String, File> fileTable = dir.files;
			
			if( countFiles() < MAX_FILES ) {
				if( !hasFile(directory, name) ) {
					files.put( name, new File(name, data));
					file_created = true;
				}
			}
		}

		return file_created;
	}

	public boolean hasDir(final String directory) {
		return this.files.containsKey(directory) && this.files.get(directory).isDir;
	}

	public boolean hasFile(final String directory, final String name) {
		boolean fileExists = false;
		
		if( hasDir(directory) ) {
			final Directory dir = getDirectory(directory);
			
			fileExists = (dir.files.get(name) != null);
		}

		return fileExists;
	}

	public void copyFile(String directory, String name, String directory1) {
		String[] fileData;
		
		if( hasDir(directory) && hasFile(directory, name) ) {
			final Directory dir1 = getDirectory(directory);
			
			fileData = getDirectory(directory).files.get(name).getContents();
			
			newFile(directory1, name, fileData);
		}
	}

	public void deleteFile(final String directory, final String name) {
		if( hasDir(directory) && hasFile(directory, name) ) {
			getDirectory(directory).files.remove(name);
		}
	}

	private int countFiles() {
		int count = 0;

		final List<File> fileList = new LinkedList<File>();

		for(final String s : this.files.keySet()) fileList.add( files.get(s) );

		for(final File file : fileList) {
			if( !file.isDir ) count++;
		}

		return count;
	}

	private int countDirs() {
		int count = 0;

		final List<File> fileList = new LinkedList<File>();

		for(final String s : this.files.keySet()) fileList.add( files.get(s) );

		for(final File file : fileList) {
			if( file.isDir ) count++;
		}

		return count;
	}
}