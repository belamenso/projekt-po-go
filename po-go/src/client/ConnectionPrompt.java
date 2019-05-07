package client;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;

import java.net.URL;
import java.util.ResourceBundle;

public class ConnectionPrompt implements Initializable {
    private Client client;
    private Settings settings;

    @FXML private Label messageLabel;
    @FXML private TextField ipField;
    @FXML private TextField portField;

    public void setClient(Client client) {
        this.client = client;
        client.setListener(new ConnectListener(this));
    }

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

    void switchToLobby()  {
        SceneManager.loadLobbyScreen();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        Settings.assertConfigurationExists();
        settings = Settings.readSettings();
        ipField.setText(settings.host);
        portField.setText(settings.port);
    }
}
