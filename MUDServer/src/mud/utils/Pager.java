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