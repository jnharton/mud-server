package mud.foe.misc;

import java.util.TimerTask;

public interface Module {
	// worth noting that all methods of an interface are abstract by default
	public abstract String getModuleName();
	
	public abstract int getVersion();
	
	public abstract int getPowerReq();
	
	public abstract void enable();
	
	public abstract void disable();
	
	public abstract boolean isEnabled();
	
	//
	public abstract boolean requiresCharging();
	
	default public int getCharge() {
		return -1;
	}
	
	default public TimerTask charge() {
		return null;
	}
	
	default public boolean isCharged() {
		return false;
	}
	
	default public boolean isCharging() {
		return false;
	}
}