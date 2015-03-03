package mud.misc;

import java.util.*;
import java.util.Map.Entry;

import mud.objects.Player;

/**
 * A class to handle mapping players to an NPC they are currently
 * in control of (instead of their Player).
 * 
 * replaced original DMControlMap (an ordinary hashmap) in MUDServer
 * 
 * @author joshgit
 *
 */
public class PlayerControlMap {

    final private Map<Player, Player> map = new HashMap<Player, Player>();
    
    /**
     * Add control mapping for Player p and set permissions of slave
     * to match that of Player p.
     * 
     * @param controller
     * @param newSlave
     * @return
     */
    public Player control(final Player controller, final Player newSlave) {
        final Player oldSlave = map.get(controller);
        
        map.put(controller, newSlave);
        
        newSlave.setAccess(controller.getAccess());
        controller.setController(true);
        
        return oldSlave;
    }
    
    /**
     * Remove control mapping for Player p and reset permissions
     * of the "slave" NPC
     * 
     * @param p
     */
    public void stopControllingAnyone(final Player p) {
        final Player oldSlave = map.get(p);
        
        // if player was controlling an NPC
        if( oldSlave != null ) {
        	oldSlave.setAccess(0);  // clear permissions
        	p.setController(false); // clear controller "flag"
        	map.remove(p);          // remove the mapping
        }
    }
    
    public Player getController(final Player p) {
    	for( Entry<Player, Player> e : map.entrySet() ) {
    		if( e.getValue() == p ) {
    			return e.getKey();
    		}
    	}
    	
    	return null;
    }
    
    /**
     * Get the NPC "slave" that Player p is controlling.
     * 
     * @param p
     * @return
     */
    public Player getSlave(final Player p) {
        return map.get(p);
    }

    public String toString() {
        return map.toString();
    }
}