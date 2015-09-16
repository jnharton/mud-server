package mud.misc;

import java.util.Arrays;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;

import mud.objects.Player;
import mud.utils.Utils;

/**
 * The CombatManager helps to handle turn-based combat
 * 
 * - on initiating combat (CombatManager instance created) the players, etc
 * involved in combat roll initiative (or rather it is rolled for them). This
 * dictates turn order in a sense.
 * - when a new combatant enters they roll initiative and are added into the list
 * where appropriate
 * 
 * @author Jeremy
 *
 */
public class CombatManager {
	public List<Player> combatants = new LinkedList<Player>();
	
	public CombatManager() {}
	
	public CombatManager(Player...players) {
		this.combatants.addAll(Arrays.asList(players));
	}
	
	public void addCombatant(Player player) {
		this.addCombatant(player);
		this.calculateAndSortByInitiative();
	}
	
	public void removeCombatant(Player player) {
		combatants.remove(player);
	}
	
	private void calculateAndSortByInitiative() {
		Hashtable<Player, Integer> plrInits = new Hashtable<Player, Integer>(combatants.size());
		List<Player> temp = new LinkedList<Player>();
		
		for(Player p : combatants) {
			// roll initiative
			int initiative = Utils.roll(1, 20);
			
			// store in the hashtable
			plrInits.put(p, initiative);
			
			// put them into temp in initiative order (largest = first, smallest = last)
			if( temp.size() > 0 ) {
				for(int n = 0; n < temp.size(); n++) {
					Player p1 = temp.get(n);
					
					if( initiative > plrInits.get(p1) ) {
						temp.add( temp.indexOf( p1 ), p );
						break;
					}
				}
				
				if( !temp.contains(p) ) temp.add( p );
			}
			else temp.add(p);
		}
		
		combatants.clear();
		combatants.addAll( temp );
	}
}