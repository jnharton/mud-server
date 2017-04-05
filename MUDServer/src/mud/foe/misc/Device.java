package mud.foe.misc;

/* uniform presentation of devices?
 * 
 * note: all interface methods are by default abstract
 */
public interface Device {
	public enum DeviceType { PIPBUCK, POWER_ARMOR, PRINTER, TERMINAL };

	public String getDeviceName();
	
	public DeviceType getDeviceType();
}