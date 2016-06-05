package mud.foe.misc;

import mud.foe.misc.Device.DeviceType;

/* class to represent ports on a terminal */
public class Port {
	private final Integer id;
	private final DeviceType type;

	public Port(final Integer id, final DeviceType deviceType) {
		this.id = id;
		this.type = deviceType;
	}
	
	public DeviceType getPortType() {
		return this.type;
	}

	@Override
	public String toString() {
		return type.toString() + "(" + id + ")";
	}
}