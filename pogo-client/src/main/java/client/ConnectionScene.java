package client;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;

import java.net.URL;
import java.util.ResourceBundle;

public class ConnectionScene implements Initializable {
    private ConnectListener cl;
    private Settings settings;

    @FXML private Label messageLabel;
    @FXML private TextField ipField;
    @FXML private TextField portField;

    void setup(ConnectListener cl) { this.cl = cl; }

    void setMessage(String msg) { messageLabel.setText(msg); }

    @FXML
    void connectButtonClicked() {
        try {
            String ip = ipField.getText();
            int port = Integer.parseInt(portField.getText());
            cl.attemptConnection(ip, port);

        } catch(RuntimeException ex) {
            ex.printStackTrace();
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
