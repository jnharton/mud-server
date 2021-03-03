package mud.foe;

import mud.foe.items.PipBuck;
import mud.misc.Slot;
import mud.objects.Player;

public final class FOEGameUtils {
	public static final PipBuck getPipBuck(final Player player) {
		PipBuck p = null;

		final Slot slot = player.getSlots().get("special");
		final Slot slot1 = player.getSlots().get("special2");
		
		if( slot != null ) {
			if( !slot.isEmpty() )  p = (PipBuck) slot.getItem();
		}
		else if( slot1 != null ) {
			if( !slot1.isEmpty() ) p = (PipBuck) slot1.getItem();
		}

		return p;
	}
}