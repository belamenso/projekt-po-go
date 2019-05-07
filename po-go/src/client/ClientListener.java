package client;

import server.Message;

/**
 * Interfejs umozliwiajacy komunikacje miedzy serwerem a klientem
 * Klient informuje listenera o zdarzeniach i odebranych wiadomosciach
 */
public interface ClientListener{
    void couldNotConnect();
    void receivedInput(Message msg);
    void disconnected();
    void connectedToServer();
}