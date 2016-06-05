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
	
	private int x = 0;
	private int y = 0;
	private int z = 0;

	public Point(final int x, final int y) {
		this.type = Type.PT_2D;
		
		this.x = x;
		this.y = y;
	}

	public Point(final int x, final int y, final int z) {
		this(x, y);
		
		this.type = Type.PT_3D;
		
		this.z = z;
	}
	
	public Point(final Point toCopy) {
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
	public int getX() {
		return x;
	}

	public void setX(final int newX) {
		this.x = newX;
	}
	
	public void changeX(int dx) {
		this.x += dx;
	}
	
	public int getY() {
		return y;
	}

	public void setY(int newY) {
		this.y = newY;
	}
	
	public void changeY(int dy) {
		this.y += dy;
	}
	
	//
	public int getZ() {
		if( this.type == Type.PT_2D ) return -1;
		else                          return z;
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
		if( this.type == Type.PT_2D ) {
			this.type = Type.PT_3D;
			this.z = 0;
		}
		
		this.z += dz;
	}
	
	public boolean equals(Object object)
	{
		boolean equivalent = false;

		// if the two objects are equal in reference, they are equal
		if (this == object) {
			equivalent = true;
		}
		else {
			// if the object is a Point, it must have the same coordinates to be equal
			// for points with different types:
			// if either point is 2D, then we only compare the X and Y coordinates
			// if both are 3D then we compare the X,Y, and Z coordinates
			if (object instanceof Point) {
				final Point point = (Point) object;
				
				boolean sameX = ( this.getX() == point.getX() );
				boolean sameY = ( this.getY() == point.getY() );
				
				if( this.isType(Type.PT_2D) && point.isType(Type.PT_2D) ) {
					if( sameX && sameY ) equivalent = true;
				}
				else if( this.isType(Type.PT_3D) && point.isType(Type.PT_3D) ) {
					boolean sameZ = ( this.getZ() == point.getZ() );
					
					if( sameX && sameY && sameZ ) equivalent = true;
				}
			}
		}

		return equivalent;
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
}