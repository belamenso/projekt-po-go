package client;

import go.Stone;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class LobbyScreen implements Initializable {
    Client client;
    Scene scene;

    @FXML private TableView<RoomData> rooms;
    @FXML private TextField nameField;
    @FXML private Label messageLabel;

    public void setClient(Client client) {
        this.client = client;
        client.setListener(new ClientLobbyListener(this));
        update();
    }

    public void setScene(Scene scene) {
        this.scene = scene;
    }

    @FXML
    public void createRoom() {
        String name = nameField.getText();
        nameField.clear();

        for(int i = 0; i < name.length(); ++ i) {
            if(!Character.isLetter(name.charAt(i))) {
                setMessage("Nazwa moze skladać się tylko z liter");
                return;
            }
        }

        client.sendMessage("create " + name);
    }

    private void sendJoinRoomRequest(String roomName) {
        client.sendMessage("join " + roomName);
    }

    @FXML
    public void joinRoom() {
        String name = rooms.getSelectionModel().getSelectedItems().get(0).getName();
        sendJoinRoomRequest(name);
    }

    @FXML
    public void update() {
        client.sendMessage("list");
    }

    public void updateList(List<RoomData> data) {
        ObservableList<RoomData> list = FXCollections.observableArrayList();
        list.addAll(data);
        rooms.setItems(list);
    }

    public void setMessage(String msg) {
        messageLabel.setText(msg);
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        TableColumn<RoomData, String> nameColumn = new TableColumn<>("Name");
        nameColumn.setMinWidth(200);
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));

        TableColumn<RoomData, String> stanColumn = new TableColumn<>("Stan");
        stanColumn.setMinWidth(150);
        stanColumn.setCellValueFactory(new PropertyValueFactory<>("stan"));

        rooms.setRowFactory(tv -> {
            TableRow<RoomData> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    RoomData rowData = row.getItem();
                    sendJoinRoomRequest(rowData.name);
                }
            });
            return row;
        });

        rooms.getColumns().clear();
        rooms.getColumns().addAll(nameColumn, stanColumn);
    }

    public void returnToConnecting() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("ConnectionPrompt.fxml"));
        Parent root = loader.load();
        ConnectionPrompt controller = loader.getController();
        controller.setScene(scene);
        controller.setClient(client);
        scene.setRoot(root);
    }

    public void moveToRoom(Stone color) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("RoomGUI.fxml"));
        final Parent root = loader.load();
        RoomGUI controller = loader.getController();
        controller.setup(scene, client, color);
        Platform.runLater(() -> scene.setRoot(root));
    }
}