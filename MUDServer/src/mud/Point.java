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

public class Point {
	private int x;
	private int y;
	private int z;
	
	public Point(int x, int y) {
		this.x = x;
		this.y = y;
	}
	
	public Point(int x, int y, int z) {
		this(x, y);
		this.z = z;
	}
	
	public int getX() { return x; }
	
	public void setX(int newX) { this.x = newX; }
	
	public void incX(int increment) { this.x += increment; } 
	
	public int getY() { return y; }
	
	public void setY(int newY) { this.y = newY; }
	
	public void incY(int increment) { this.y += increment; }
	
	public int getZ() { return z; }
	
	public void setZ(int newZ) { this.z = newZ; }
	
	public void incZ(int increment) { this.z += increment; }
	
	public boolean equals(Object object)
	  {
	    // if the two objects are equal in reference, they are equal
	    if (this == object) { return true; }
	    // if the object is a Point, it must have the same coordinates to be equal
	    else if (object instanceof Point) {
	      Point point = (Point) object;
	      
	      if( ( getX() == point.getX() ) && ( getY() == point.getY() ) && (getZ() == point.getZ() ) ) {
	    	return true;  
	      }
	      else { return false; }
	    }
	    // if the object isn't a Point, then it can't be equal to it
	    else { return false; }
	  }
	
	public String toString() {
		return "(" + x + "," + y + ")";
	}
}