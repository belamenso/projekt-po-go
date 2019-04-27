package client;

import javafx.application.Platform;

public class ConnectListener implements ClientListener {
    ConnectionPrompt cp;

    ConnectListener(ConnectionPrompt cp) {
        this.cp = cp;
    }

    @Override
    public void unknownHost() {
        Platform.runLater(() -> cp.setMessage("Nieznany host"));
    }

    @Override
    public void couldNotConnect() {
        Platform.runLater(() -> cp.setMessage("Nie udało się nawiązać połączenia"));
    }

    @Override
    public void receivedInput(String msg) {

    }

    @Override
    public void serverClosed() {

    }

    @Override
    public void disconnected() {

    }

    @Override
    public void connectedToServer() {
        Platform.runLater(() -> cp.setMessage("Polączono z serwerem"));

        cp.switchToLobby();
    }
}
