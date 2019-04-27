package client;

/**
 * Listener obsugujacy pokoj po stronie gracza
 */
public class ClientRoomListener implements ClientListener {
    private Client client;

    ClientRoomListener(Client client) {
        this.client = client;
    }

    @Override
    public void unknownHost() {

    }

    @Override
    public void couldNotConnect() {

    }

    @Override
    public void receivedInput(String msg) {
        System.out.println(msg);
        if(msg.startsWith("exitedRoom")) {
            client.setListener(new ClientLobbyListener(client));
        }
    }

    @Override
    public void serverClosed() {
        System.out.println("serverClosed");
    }

    @Override
    public void disconnected() {

    }

    @Override
    public void connectedToServer() {

    }
}
