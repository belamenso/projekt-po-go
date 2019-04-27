package client;

/**
 * Interfejs umozliwiajacy komunikacje miedzy serwerem a klientem
 * Klient informuje listenera o zdarzeniach i odebranych wiadomosciach
 */
public interface ClientListener{
    void unknownHost();
    void couldNotConnect();
    void receivedInput(String msg);
    void serverClosed();
    void disconnected();
    void connectedToServer();
}