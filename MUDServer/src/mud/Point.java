package mud;

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
}