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
	
	public default boolean isCompatible(final Device dev) {
		return true;
	}
	
	//
	public default boolean requiresCharging() {
		return false;
	}
	
	public default int getCharge() {
		return -1;
	}
	
	public default TimerTask charge() {
		return null;
	}
	
	public default boolean isCharged() {
		return false;
	}
	
	default public boolean isCharging() {
		return false;
	}
}