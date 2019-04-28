package client;

import go.Stone;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class RoomGUI implements Initializable {
    Client client;
    Scene scene;

    @FXML private TextField input;
    @FXML private Label messageLabel;

    public void setClient(Client client, Stone color) {
        this.client = client;
        client.setListener(new ClientRoomListener(client, color, this));
    }

    public void setScene(Scene scene) {
        this.scene = scene;
    }

    public void setMessage(String msg) {
        messageLabel.setText(msg);
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

    }

    public void send() {
        String msg = input.getText();
        input.clear();
        client.sendMessage(msg);
    }

}
