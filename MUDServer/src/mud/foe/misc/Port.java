package mud.foe.misc;

import mud.foe.misc.Device.DeviceType;

/* class to represent ports on a terminal */
public class Port {
	private Integer id;
	public final DeviceType type;
	private Boolean working;

	public Port( Integer id, DeviceType deviceType, Boolean working  ) {
		this.id = id;
		this.type = deviceType;
		this.working = working;
	}

	@Override
	public String toString() {
		return type.toString() + "(" + id + ")";
	}
}