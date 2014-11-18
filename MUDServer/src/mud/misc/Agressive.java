package mud.misc;

import mud.events.SayEvent;

public class Agressive extends Strategy {

	@Override
	public void handleSayEvent(SayEvent se) {
		String whatWasSaid = se.getMessage();
	}

	@Override
	public void onRoomChange() {
		// TODO Auto-generated method stub
	}

}