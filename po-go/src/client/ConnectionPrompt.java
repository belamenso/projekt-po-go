package client;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class ConnectionPrompt implements Initializable {
    private Client client;
    private Scene scene;
    private Settings settings;

    @FXML private Label messageLabel;
    @FXML private TextField ipField;
    @FXML private TextField portField;

    public void setClient(Client client) {
        this.client = client;
        client.setListener(new ConnectListener(this));
    }

    void setScene(Scene scene) { this.scene = scene; }

    void setMessage(String msg) { messageLabel.setText(msg); }

    @FXML
    void connectButtonClicked() {
        try {
            String ip = ipField.getText();
            int port = Integer.parseInt(portField.getText());
            client.startConnection(ip, port);
        } catch(RuntimeException ex) {
            messageLabel.setText("Port musi być liczbą");
            messageLabel.setTextFill(Color.CRIMSON);
        }
    }

    void switchToLobby() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("LobbyScreen.fxml"));
        Parent root = loader.load();
        LobbyScreen controller = loader.getController();
        controller.setScene(scene);
        controller.setClient(client);
        scene.setRoot(root);
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        Settings.assertConfigurationExists();
        settings = Settings.readSettings();
        ipField.setText(settings.host);
        portField.setText(settings.port);
    }
}
