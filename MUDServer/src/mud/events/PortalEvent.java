package mud.events;

/*
 * Copyright (c) 2013 Jeremy N. Harton
 * Released under the MIT License:
 * LICENSE.txt, http://opensource.org/licenses/MIT
 */

import java.util.EventObject;

import mud.objects.exits.Portal;

@SuppressWarnings("serial")
public class PortalEvent extends EventObject {
	private String message;
	private EventType eventType;
	
	public enum EventType { ACTIVATE, DEACTIVATE, USE, NONE };
	
	public PortalEvent(final Portal source) {
		super(source);
		this.eventType = EventType.NONE;
		this.message = "";
	}
	
	public PortalEvent(final Portal source, final EventType eType) {
		this(source, eType, "");
	}
	
	public PortalEvent(final Portal source, final EventType eType, final String message) {
		super(source);
		this.eventType = eType;
		this.message = message;
	}
	
	@Override
	public Portal getSource() {
		return (Portal) super.getSource();
	}
	
	public String getMessage() {
		return this.message;
	}
	
	public EventType getType() {
		return this.eventType;
	}
}