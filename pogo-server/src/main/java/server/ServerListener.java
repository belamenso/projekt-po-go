package server;

import shared.Message;

/**
 * Intefrfers uzywany do komunikacji miedzy klientem a
 * czescia serwera - lobby, pokój
 */
public interface ServerListener  {
    boolean clientConnected(ServerClient client);
    void clientDisconnected(ServerClient client);
    void receivedInput(ServerClient client, Message msg);
    void serverClosed();
}
