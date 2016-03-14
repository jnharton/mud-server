package mud.objects.creatures;

import mud.interfaces.Mobile;
import mud.interfaces.Ridable;
import mud.objects.Creature;
import mud.objects.CreatureType;

public class Horse extends Creature implements Ridable, Mobile {	
	public Horse() {
		super();
		this.ctype = CreatureType.HORSE;
		this.race = "horse";
		this.size = Size.MEDIUM;
		
		this.ridable = true;
	}
	
	public Horse(final String hName, final String hDesc) {
		super(hName, hDesc);
		
		this.ctype = CreatureType.HORSE;
		this.race = "horse";
		this.size = Size.MEDIUM;
		
		this.ridable = true;
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
	public boolean isLargeEnough(final Size riderSize) {
		if( size.ordinal() >= riderSize.ordinal() ) {
			return true;
		}
		
		return false;
	}
}