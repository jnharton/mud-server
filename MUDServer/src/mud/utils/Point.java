package mud.utils;

/*
 * Copyright (c) 2012 Jeremy N. Harton
 * 
 * Released under the MIT License:
 * LICENSE.txt, http://opensource.org/licenses/MIT
 * 
 * NOTE: license provided with code controls, if any
 * changes are made to the one referred to.
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