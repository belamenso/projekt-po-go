package client;

import go.Stone;

/**
 * Listener obsulugujacy lobby po stronie gracza
 * póki co wypisuje otrzymane wiadomosci
 * w finalnej wersji ma wyswietlac na gui liste pokoi
 * i inofrmacje o tym czy dalo sie polaczyc
 */
public class ClientLobbyListener implements ClientListener {
    private Client client;

    ClientLobbyListener(Client client) {
        this.client = client;
    }

    @Override
    public void unknownHost() {
        System.out.println("unknownHost");
    }

    @Override
    public void couldNotConnect() {
        System.out.println("couldNotConnect");
    }

    @Override
    public void receivedInput(String msg) {
        System.out.println("receivedInput >"+msg+"<");

        if(msg.startsWith("CONNECTED ")) {
            String[] parts = msg.split(" ");
            assert parts.length == 2;
            client.setListener(new ClientRoomListener(client, parts[1].equals("WHITE") ? Stone.White : Stone.Black));
        } else {

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
