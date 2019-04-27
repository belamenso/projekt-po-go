package server;

/**
 * Intefrfers uzywany do komunikacji miedzy klientem a
 * czescia serwera - lobby, pokój
 */
public interface ServerListener  {
    boolean clientConnected(ServerClient client);
    void clientDisconnected(ServerClient client);
    void receivedInput(ServerClient client, String msg);
    void serverClosed();
}
