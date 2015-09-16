package mud.misc;

import mud.utils.Utils;
import mud.utils.Point;

public class Edge {
	private Point start;
	private Point end;
	private Double length;
	
	public Edge(Point startP, Point endP) {
		this.start = startP;
		this.end = endP;
		this.length = (double) Utils.distance(this.start, this.end);
	}
	
	public Point getStartPoint() {
		return start;
	}
	
	public Point getEndPoint() {
		return end;
	}
	
	public int getLength() {
		return (int) Math.floor(length);
	}
}