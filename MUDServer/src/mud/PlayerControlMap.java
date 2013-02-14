package mud;

import java.util.*;

import mud.objects.Player;

/**
 * A class to handle mapping players to an NPC they are currently
 * in control of (instead of their Player).
 * 
 * @author Jeremy
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
        //controller.setController(true);
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
        oldSlave.setAccess(0);
        p.setController(false);
        map.remove(p);
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