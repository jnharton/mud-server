package mud.misc;

import mud.events.SayEvent;

public class Agressive extends Strategy {

	@Override
	public void handleSayEvent(SayEvent se) {
		String whatWasSaid = se.getMessage();
	}

	@Override
	public void onRoomChange() {
		// look for enemies and attack if there are any
	}

	@Override
	public void onDetectHostile() {
		// TODO Auto-generated method stub
		
	}

}