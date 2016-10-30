package mud.foe.misc;

public class File {
	public boolean isDir = false;

	private String name;
	private String[] contents = null;

	public File(final String name, final boolean isDir, final String[] contents) {
		this.name = name;
		this.isDir = isDir;
		this.contents = contents;
	}

	public String getName() {
		return this.name;
	}

	public String[] getContents() {
		return this.contents;
	}
	
	public void setContents(final String[] newContents) {
		this.contents = newContents;
	}
}