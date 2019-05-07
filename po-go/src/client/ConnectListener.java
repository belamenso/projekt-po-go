package client;

import javafx.application.Platform;
import server.Message;

public class ConnectListener implements ClientListener {
    ConnectionPrompt cp;

    ConnectListener(ConnectionPrompt cp) {
        this.cp = cp;
    }

    @Override
    public void couldNotConnect() {
        Platform.runLater(() -> cp.setMessage("Nie udało się nawiązać połączenia"));
    }

    @Override
    public void receivedInput(Message message) {}

    @Override
    public void disconnected() {}

    @Override
    public void connectedToServer() {
        Platform.runLater(() -> cp.setMessage("Polączono z serwerem"));

        cp.switchToLobby();
    }
}
