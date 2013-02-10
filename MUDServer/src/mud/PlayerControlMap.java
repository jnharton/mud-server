package mud;

import java.util.*;

import mud.objects.Player;

public class PlayerControlMap {

    final private Map<Player, Player> map = new HashMap<Player, Player>();

    public Player control(final Player controller, final Player newSlave) {
        final Player oldSlave = map.get(controller);
        map.put(controller, newSlave);
        newSlave.setAccess(controller.getAccess());
        return oldSlave;
    }

    public void stopControllingAnyone(final Player p) {
        final Player oldSlave = map.get(p);
        oldSlave.setAccess(0);
        p.setController(false);
        map.remove(p);
    }

    public Player getSlave(final Player p) {
        return map.get(p);
    }

    public String toString() {
        return map.toString();
    }

}
