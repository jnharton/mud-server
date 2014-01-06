package mud.misc;

import java.awt.Point;

public class Edge {
	private Point start;
	private Point end;
	private int length;
	
	public Edge(Point startP, Point endP, int length) {
		this.start = startP;
		this.end = endP;
		this.length = length;
	}
	
	public Point getStartPoint() {
		return start;
	}
	
	public Point getEndPoint() {
		return end;
	}
	
	public int length() {
		return this.length;
	}
}