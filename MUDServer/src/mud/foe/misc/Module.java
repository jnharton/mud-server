package mud.foe.misc;

public interface Module {
	// worth noting that all methods of an interface are abstract by default
	public abstract String getName();
	
	public abstract int getVersion();
	
	public abstract int getPowerReq();
	
	public abstract void enable();
	
	public abstract void disable();
	
	public abstract void init();
	
	public abstract void deinit();
	
	public abstract boolean isEnabled();
}