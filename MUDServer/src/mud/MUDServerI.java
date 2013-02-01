package mud;

import mud.net.Client;

public interface MUDServerI {
    void clientConnected(final Client someClient);
}