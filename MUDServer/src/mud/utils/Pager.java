package mud.utils;

import java.util.Arrays;

/**
 * This class is intended to provide simple 'pager' functionality,
 * much like the 'less' or 'more' commands in the Linux console.
 * 
 * @author Jeremy
 *
 */
public class Pager
{
	final private String[] content;
	private int displayRows = 25;
	private int topLine;

	/**
	 * Create a new pager with the supplied string
	 * array as the content.
	 * 
	 * @param file
	 */
	public Pager(final String[] file) {
		content = file;
		topLine = 0;
	}

	public int getRows() {
		return displayRows;
	}

	public void setRows(final int newRows) {
		displayRows = newRows;
	}
    
    public int getBottom() {
        final int end = topLine + displayRows;
        return end > content.length ? content.length : end;
    }

	public String[] getView() {
		return Arrays.copyOfRange(content, topLine, getBottom());
	}
	
	public int getTop() {
		return this.topLine;
	}
	
	public String[] getContent() {
		return this.content;
	}
	
	public String[] scrollUp() {
        topLine = topLine >= displayRows ? topLine - displayRows : 0;
		return getView();
	}
	
	public String[] scrollDown() {
		if (topLine <= content.length - displayRows) {
			topLine += displayRows;
        }

		// topline, end, view size, amount to scroll down (view range)
		// 0, 50, 25, 0 (25-50)
		// 0, 50, 25, 25 (25-50)
		// 25, 50, 25, 25 (50-50)

		return getView();
	}
}