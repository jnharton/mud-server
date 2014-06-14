package mud.interfaces;

import mud.utils.Point;

public interface Mobile {
	public void setMoving(boolean isMoving);
	public boolean isMoving();
	
	public Point getPosition();
	
	public void setDestination(Point newDest);
	public Point getDestination();
	
	public void changePosition(int cX, int cY, int cZ);
}