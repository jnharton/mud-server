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
	enum Type { PT_2D, PT_3D };
	
	private Integer x = 0;
	private Integer y = 0;
	private Integer z = 0;
	
	private Type type;

	public Point(int x, int y) {
		this.x = x;
		this.y = y;
		
		this.type = Type.PT_2D;
	}

	public Point(int x, int y, int z) {
		this(x, y);
		this.z = z;
		
		this.type = Type.PT_3D;
	}

	public int getX() { return x; }

	public void setX(int newX) { this.x = newX; }

	public void incX() { this.x++; }
	
	public void changeX(int dx) { this.x += dx; }

	public int getY() { return y; }

	public void setY(int newY) { this.y = newY; }

	public void incY() { this.y++; }
	
	public void changeY(int dy) { this.y += dy; }
	
	public int getZ() {
		if( this.type == Type.PT_2D ) return -1;
		
		return z;
	}

	public void setZ(int newZ) {
		if( this.type == Type.PT_2D ) this.type = Type.PT_3D;
		this.z = newZ;
	}
	
	public void changeZ( int dz ) {
		if( this.type == Type.PT_2D ) this.type = Type.PT_3D;
		this.z += dz;
	}

	public void incZ() {
		if( this.type == Type.PT_2D ) this.type = Type.PT_3D;
		this.z++;
	}

	public boolean equals(Object object)
	{
		// if the two objects are equal in reference, they are equal
		if (this == object) { return true; }
		// if the object is a Point, it must have the same coordinates to be equal
		else if (object instanceof Point) {
			Point point = (Point) object;
			
			if( this.type == Type.PT_2D || point.type == Type.PT_2D  ) {
				if( ( getX() == point.getX() ) && ( getY() == point.getY() ) ) {
					return true;
				}
				else { return false; }
			}
			else if( this.type == Type.PT_3D && point.type == Type.PT_3D ) {
				if( ( getX() == point.getX() ) && ( getY() == point.getY() ) && (getZ() == point.getZ() ) ) {
					return true;  
				}
				else { return false; }
			}
			else { return false; }
		}
		// if the object isn't a Point, then it can't be equal to it
		else { return false; }
	}

	public String toString() {
		if( type == Type.PT_2D ) return "(" + x + "," + y + ")";
		else return "(" + x + "," + y + "," + z + ")";
	}
}