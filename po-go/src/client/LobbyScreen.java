package client;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

public class LobbyScreen {
    Client client;
    Scene scene;

    @FXML private TableView rooms;
    @FXML private TextField nameField;
    @FXML private Label messageLabel;

    public void setClient(Client client) {
        this.client = client;
        client.setListener(new ClientLobbyListener(this));
    }

    public void setScene(Scene scene) {
        this.scene = scene;
    }

    @FXML
    public void createRoom() {

    }

    @FXML
    public void joinRoom() {

    }

    public void setMessage(String msg) {

    }
}