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
	public enum Type { PT_2D, PT_3D };
	
	private Type type;
	
	private Integer x = 0;
	private Integer y = 0;
	private Integer z = 0;

	public Point(int x, int y) {
		this.type = Type.PT_2D;
		
		this.x = x;
		this.y = y;
	}

	public Point(int x, int y, int z) {
		this(x, y);
		
		this.type = Type.PT_3D;
		
		this.z = z;
	}
	
	protected Point(final Point toCopy) {
		switch(toCopy.type) {
		case PT_2D:
			this.type = Type.PT_2D;
			
			this.x = toCopy.x;
			this.y = toCopy.y;
			break;
		case PT_3D:
			this.type = Type.PT_3D;
			
			this.x = toCopy.x;
			this.y = toCopy.y;
			this.z = toCopy.z;
			break;
		default:
			break;
		}
	}
	
	//
	public int getX() { return x; }

	public void setX(int newX) { this.x = newX; }

	public void incX() { this.x++; }
	
	public void changeX(int dx) { this.x += dx; }
	
	//
	public int getY() { return y; }

	public void setY(int newY) { this.y = newY; }

	public void incY() { this.y++; }
	
	public void changeY(int dy) { this.y += dy; }
	
	//
	public int getZ() {
		if( this.type == Type.PT_2D ) return -1;
		
		return z;
	}
	
	/**
	 * Set z coordinate of this point.
	 * 
	 * NOTE: Setting or modifying the z coordinate of a
	 * Point of Type PT_2D will result in an automatic upgrade
	 * to a Type of PT_3D.
	 * 
	 * @param newZ
	 */
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
		// for points with different types:
		// if either point is 2D, then we only compare the X and Y coordinates
		// if both are 3D then we compare the X,Y, and Z coordinates
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
	
	public boolean isType(Type ptType) {
		return this.type == ptType;
	}
	
	/**
	 * Creates and returns a String representation of a Point
	 * ex.
	 * Type: PT_2D
	 * x = 5, y = 6
	 * String -> (5,6)
	 * 
	 * Type: PT_3D
	 * x = 5, y = 6, z = 0
	 * String -> (5,6,0)
	 */
	@Override
	public String toString() {
		switch(type) {
		case PT_2D: return "(" + x + "," + y + ")";
		case PT_3D: return "(" + x + "," + y + "," + z + ")";
		default:    return "";
		}
	}
	
	public Point getCopy() {
		return new Point(this);
	}
}