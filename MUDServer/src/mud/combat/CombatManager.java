package mud.combat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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
	public List<Player> combatants;
	
	public CombatManager() {
		this.combatants = new LinkedList<Player>();
	}
	
	public CombatManager(final Player...players) {
		this.combatants = new LinkedList<Player>( Arrays.asList(players) );
		this.sortByInitiative();
	}
	
	public void addCombatant(final Player player) {
		this.addCombatant(player);
		this.sortByInitiative();
	}
	
	public void removeCombatant(final Player player) {
		combatants.remove(player);
	}
	
	private void sortByInitiative() {
		Map<Player, Integer> plrInits = new Hashtable<Player, Integer>();
		
		List<Player> temp = new ArrayList<Player>(this.combatants.size());
		
		for(final Player p : this.combatants) {
			int initiative = Utils.roll(1, 20); // roll initiative
			plrInits.put(p, initiative);        // store in the hashtable
			
			if( temp.isEmpty() ) temp.add(p);
			else {
				int n = 0;
				
				boolean done = false;
				
				for(final Player p1 : temp) {
					if( initiative > plrInits.get(p1) ) {
						temp.add(n, p);
						done = true;
					}
					
					if( done ) break;
					else       n++;
				}
				
				if( !done ) temp.add(p);
			}
		}
		
		this.combatants.clear();
		this.combatants.addAll( temp );
	}
}