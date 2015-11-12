package mud.foe.misc;

import java.util.List;

/* uniform presentation of devices?
 * 
 * note: all interface methods are by default abstract
 */
public interface Device {
	enum DeviceType { PIPBUCK, PWR_ARMOR, PRINTER, TERMINAL };

	public String getDeviceName();
	public DeviceType getDeviceType();
	
	public List<Module> getModules();
}