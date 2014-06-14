package mud.objects.creatures;

import mud.interfaces.Mobile;
import mud.interfaces.Ridable;
import mud.objects.Creature;
import mud.objects.CreatureType;

public class Horse extends Creature implements Ridable, Mobile {
	
	Size size = Size.MEDIUM;
	
	public Horse() {
		super();
		this.ctype = CreatureType.HORSE;
	}
	
	@Override
	public String getName() {
		return this.ctype.getName().toLowerCase();
	}
	
	@Override
	public void mount() {
		// TODO Auto-generated method stub
	}

	@Override
	public void unmount() {
		// TODO Auto-generated method stub
	}

	@Override
	public boolean isLargeEnough(Size riderSize) {
		if( size.ordinal() >= riderSize.ordinal() ) {
			return true;
		}
		
		return false;
	}
}