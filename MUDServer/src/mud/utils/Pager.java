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
public class Pager {
	
	private static int MAX_ROWS = 25; // maximum rows to display
	private int rows = 25;            // rows to display, less than or equal to 25
	private int scrollDistance = 25;  // number of rows to scroll up or down
	
	private int topLine;
	private int bottomLine;
	
	private String[] content;
	
	/**
	 * Create a new pager with the supplied string
	 * array as the content.
	 * 
	 * @param file
	 */
	public Pager(String[] file) {
		this.content = file;
		
		this.topLine = 0;
		this.bottomLine = topLine + rows;
	}
	
	public int getRows() {
		return this.rows;
	}
	
	public void setRows(int newRows) {
		this.rows = newRows;
	}
	
	public String[] getView() {
		String[] output;
		
		if(this.content.length >= MAX_ROWS) {
			output = Arrays.copyOfRange(this.content, this.topLine, bottomLine);
		}
		else {
			output = Arrays.copyOfRange(this.content, this.topLine, this.content.length);
		}
		
		return output;
	}
	
	public int getTop() {
		return this.topLine;
	}
	
	public int getBottom() {
		return this.bottomLine;
	}
	
	public String[] getContent() {
		return this.content;
	}
	
	public String[] scrollUp() {
		if(topLine >= scrollDistance) {
			this.topLine = topLine - scrollDistance;
			this.bottomLine = topLine + rows;
		}
		
		return getView();
	}
	
	public String[] scrollDown() {
		if(topLine + scrollDistance <= this.content.length) {
			this.topLine = topLine + scrollDistance;
			
			// if the top line doesn't exceed the length and the topline + rows doesn't exceed the length
			if(topLine < this.content.length && !(topLine + rows > this.content.length) ) {
				this.bottomLine = topLine + rows;
			}
			else {
				this.bottomLine = topLine + (this.content.length - this.topLine);
			}
		}
		
		// topline, end, view size, amount to scroll down (view range)
		// 0, 50, 25, 0 (25-50)
		// 0, 50, 25, 25 (25-50)
		// 25, 50, 25, 25 (50-50)
		
		return getView();
	}
}