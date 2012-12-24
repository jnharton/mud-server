package mud;

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

/**
 * Enumeration of Seasons with the months
 * that they start and end
 * 
 * @author Jeremy
 *
 */
public enum Seasons {
	SPRING("Spring", 3, 6),
	SUMMER("Summer", 6, 9),
	AUTUMN("Autumn", 9, 12),
	WINTER("Winter", 12, 3);

	private String name;
	public int beginMonth;
	public int endMonth;

	private Seasons(String name, int beginMonth, int endMonth) {
		this.name = name;
		this.beginMonth = beginMonth;
		this.endMonth = endMonth;
	}

	public String getName() {
		return this.name;
	}

	public String toString() {
		return this.name;
	}
}