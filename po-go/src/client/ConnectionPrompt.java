package client;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class ConnectionPrompt {
    Client client;
    Scene scene;

    @FXML private Label messageLabel;
    @FXML private TextField ipField;
    @FXML private TextField portField;

    public void setClient(Client client) {
        this.client = client;
        client.setListener(new ConnectListener(this));
    }

    public void setScene(Scene scene) {
        this.scene = scene;
    }

    void setMessage(String msg) {
        messageLabel.setText(msg);
    }

    @FXML
    void connectButtonClicked() {
        String ip = ipField.getText();
        int port = 0;
        try {
            port = Integer.parseInt(portField.getText());
        } catch(RuntimeException ex) {
            messageLabel.setText("Port musi być liczbą");
            return;
        }
        //ipField.clear();
        //portField.clear();
        client.startConnection(ip, port);
    }

    void switchToLobby() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("LobbyScreen.fxml"));
        Parent root = loader.load();
        LobbyScreen controller = loader.getController();
        controller.setScene(scene);
        controller.setClient(client);
        scene.setRoot(root);
    }
}
