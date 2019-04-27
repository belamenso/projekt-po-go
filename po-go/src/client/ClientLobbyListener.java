package client;

import javafx.application.Platform;

/**
 * Listener obsulugujacy lobby po stronie gracza
 * póki co wypisuje otrzymane wiadomosci
 * w finalnej wersji ma wyswietlac na gui liste pokoi
 * i inofrmacje o tym czy dalo sie polaczyc
 */
public class ClientLobbyListener implements ClientListener {
    private Client client;
    private LobbyScreen ls;

    ClientLobbyListener(LobbyScreen ls) {
        this.ls = ls;
        this.client = ls.client;
    }

    @Override
    public void unknownHost() {}

    @Override
    public void couldNotConnect() {}

    @Override
    public void receivedInput(String msg) {
        System.out.println("receivedInput >"+msg+"<");
        if(msg.startsWith("connectedToRoom")) {
            client.setListener(new ClientRoomListener(client));
        }
    }

    @Override
    public void serverClosed() {
        System.out.println("serverClosed");
    }

    @Override
    public void disconnected() {
        System.out.println("disconnected");
    }

    @Override
    public void connectedToServer() {
        System.out.println("connectedToServer");
    }
}
