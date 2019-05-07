package client;

import javafx.application.Platform;
import server.Message;

public class ConnectListener implements ClientListener {
    ConnectionPrompt cp;
    Client client;

    ConnectListener(ConnectionPrompt cp, Client client) {
        this.cp = cp;
        this.client = client;
        client.setListener(this);
    }

    void attemptConnection(String ip, int port) {
        client.startConnection(ip, port);
    }

    @Override
    public void couldNotConnect() {
        Platform.runLater(() -> cp.setMessage("Nie udało się nawiązać połączenia"));
    }

    @Override
    public void connectedToServer() {
        Platform.runLater(() -> cp.setMessage("Polączono z serwerem"));

        cp.switchToLobby();
    }

    @Override
    public void receivedInput(Message message) {}

    @Override
    public void disconnected() {}
}
