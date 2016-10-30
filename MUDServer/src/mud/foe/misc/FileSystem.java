package mud.foe.misc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import mud.utils.Utils;

// TODO actual storage part needs rework
public class FileSystem {
	private static final int MAX_FILES = 100;
	
	private ArrayList<File> files;
	private Hashtable<String, Integer> fileTable;
	
	// File System structure
	// /                    root directory
	// /<file>              file in root dir
	// /<user>              user directory
	// /<user>/<file>       file in user dir
	// /<user>/<dir>/<file> user sub-directory
	//
	// "/admin/notes/this one.txt", [ string, string, string, string, string ]
	
	private int firstUnused;
	
	// ERRORS
	// -1 max files reached
	// copy failed
	// create file failed
	// delete file failed
	// no such file
	public static final int ERR_MAX_FILES_REACHED = -1;
	public static final int ERR_FILE_DOES_NOT_EXIST = -2;
	public static final int ERR_NONEMPTY_DIR = -3;
	
	// directories are files containing a list of filenames
	/*public class Directory extends File {
		private Hashtable<String, File> files;
		
		//private List<String> 

		public Directory(final String name) {
			super(name, null);

			this.isDir = true;
			this.files = new Hashtable<String, File>();
		}
		
		@Override
		public String[] getContents() {
			final Set<String> filenames = files.keySet();
			
			return filenames.toArray( new String[filenames.size()] );
		}
	}*/

	public FileSystem() {
		this.files = new ArrayList<File>();
		this.fileTable = new Hashtable<String, Integer>();
		
		this.firstUnused = 0;
		
		// TODO resolve - this is a kludge because the IO assumes a top level directory
		this.newDir("/");
	}
	
	public int newFile(final String fileName) {
		return newFile(fileName, false, new String[0]);
	}
	
	public int newFile(final String fileName, final String[] contents) {
		return newFile(fileName, false, contents);
	}
	
	/**
	 * 
	 * @param directory
	 * @param name
	 * @param data
	 * @return true if we successfully created the file
	 */
	/*public boolean newFile(final String directory, final String name, final String[] data) {
	 * 
	 */
	
	private int newFile(final String fileName, final boolean isDir, final String[] contents) {
		int error = 0;
		
		if( files.size() < MAX_FILES ) {
			final File f = new File(fileName, isDir, contents);
			
			int fileNode = 0;
			
			if( firstUnused != -1 ) {
				fileNode = firstUnused;
				
				firstUnused = -1;
				
				for(int n = fileNode + 1; n < files.size(); n++) {
					if( files.get(n) == null ) {
						firstUnused = n;
						break;
					}
				}
			}
			else {
				fileNode = files.size();
			}
			
			if( fileNode < files.size() ) files.set(fileNode, f);
			else                          files.add(f);
			
			fileTable.put(fileName, fileNode);
		}
		else {
			error = ERR_MAX_FILES_REACHED;
		}
		
		return error;
	}
	
	public int newDir(final String fileName) {
		return newFile(fileName, true, new String[0] );
	}
	
	public int newDir(final String fileName, final String[] filenames) {
		return newFile(fileName, true, filenames);
	}
	
	public int deleteFile(final String directory, final String name) {
		final String path = directory + "/" + name; 
		
		int error = 0;
		
		final Integer fileNode = fileTable.containsKey(path) ? fileTable.get(path) : -1;

		if( fileNode != null && fileNode != -1 && Utils.range(fileNode, 0, MAX_FILES) ) {
			final File file = files.get(fileNode);

			boolean delete_ok = true;

			// assuming the file isn't null
			if( file.isDir ) {
				if( file.getContents().length != 0 ){
					delete_ok = false;
					error = ERR_NONEMPTY_DIR;
				}
			}

			if( delete_ok ) {
				fileTable.remove(path);
				files.set(fileNode, null);

				for(int n = 0; n < files.size(); n++) {
					if( files.get(n) == null ) {
						firstUnused = n;
						break;
					}
				}
			}
		}
		else {
			error = ERR_FILE_DOES_NOT_EXIST;
		}

		return error;
	}
	
	public void copyFile(final String directory, final String name, final String directory1) {
		String[] fileData;
		
		if( hasDir(directory) && hasFile(directory, name) ) {
			fileData = getFile(directory, name).getContents();
			
			newFile(directory + "/" + name, fileData);
		}
	}
	
	public int write(final String fileName, final String[] data, final char mode) {
		int error = -5;
		
		// TODO resolve this kludge
		File file = getFile(fileName);
		
		// TODO maybe files should have lists and simply present their contents as string arrays
		switch(mode) {
		case 'w':
			file.setContents( data );

			error = 0;
			
			break;
		case 'a':
			List<String> new_contents = new LinkedList<String>();
			
			new_contents.addAll( Arrays.asList( file.getContents() ) );
			new_contents.addAll( Arrays.asList( data ) );
			
			file.setContents( new_contents.toArray(new String[0]) );
			
			error = 0;
			
			break;
		default:  break;
		}
		
		return error;
	}
	
	public boolean hasFile(final String directory, final String name) {
		return hasFile(directory + "/" + name);
	}
	
	// requires an absolute path starting with root
	public boolean hasFile(final String path) {
		final Integer fileNode = fileTable.containsKey(path) ? fileTable.get(path) : -1;

		if( fileNode != null && fileNode != -1 && Utils.range(fileNode, 0, MAX_FILES) ) {
			final File file = files.get(fileNode);
			
			if( file != null ) return true;
		}
		
		return false;
	}
	
	// requires an absolute path starting with root
	public boolean hasDir(final String path) {
		final Integer fileNode = fileTable.containsKey(path) ? fileTable.get(path) : -1;

		if( fileNode != null && fileNode != -1 && Utils.range(fileNode, 0, MAX_FILES) ) {
			final File file = files.get(fileNode);
			
			if( file != null && file.isDir ) return true;
		}
		
		return false;
	}
	
	public File getFile(final String directory, final String name) {
		return getFile(directory + "/" + name);
	}
	
	public File getFile(final String path) {
		final Integer fileNode = fileTable.containsKey(path) ? fileTable.get(path) : -1;

		if( fileNode != null && fileNode != -1 && Utils.range(fileNode, 0, MAX_FILES) ) {
			final File file = files.get(fileNode);
			
			if( file != null ) return file;
		}
		
		return null;
	}
	
	public File getDirectory(final String directory) {
		final Integer fileNode = fileTable.containsKey(directory) ? fileTable.get(directory) : -1;

		if( fileNode != null && fileNode != -1 && Utils.range(fileNode, 0, MAX_FILES) ) {
			final File file = files.get(fileNode);
			
			if( file != null ) return file;
		}
		
		return null;
	}

	public String getDirectoryNames(final String directory) {
		final StringBuilder sb = new StringBuilder();
		
		File dir = getDirectory(directory);
		
		for(String filename : dir.getContents()) {
			sb.append(filename + " ");
		}

		return sb.toString();
	}

	private int countFiles() {
		int count = 0;

		final List<File> fileList = new LinkedList<File>();
		
		for(final String s : this.fileTable.keySet()) {
			if( s!= null ) count++;
		}

		return count;
	}
}