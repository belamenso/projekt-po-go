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
import server.LobbyListener;

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
        setMessage("");

        for(int i = 0; i < name.length(); ++ i) {
            if(!Character.isLetter(name.charAt(i))) {
                setMessage("Nazwa moze skladać się tylko z liter");
                return;
            }
        }

        client.sendMessage(new LobbyListener.LobbyMsg.CreateMessage(name)); // CREATE name -> LobbyListener
    }

    private void sendJoinRoomRequest(String roomName) {
        client.sendMessage(new LobbyListener.LobbyMsg.JoinMsg(roomName)); // JOIN name -> LobbyListener
    }

    @FXML
    public void joinRoom() {
        String name = rooms.getSelectionModel().getSelectedItems().get(0).getName();
        sendJoinRoomRequest(name);
    }

    @FXML
    public void update() {
        client.sendMessage(new LobbyListener.LobbyMsg(LobbyListener.LobbyMsg.Type.LIST_REQUEST)); // LIST_REQUEST -> LobbyListener
    }

    void updateList(List<RoomData> data) {
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

        TableColumn<RoomData, String> stanColumn = new TableColumn<>("State");
        stanColumn.setMinWidth(150);
        stanColumn.setCellValueFactory(new PropertyValueFactory<>("state"));

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
        rooms.setPlaceholder(new Label("No active rooms available"));
    }

    void returnToConnecting() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("ConnectionPrompt.fxml"));
            final Parent root = loader.load();
            ConnectionPrompt controller = loader.getController();
            controller.setScene(scene);
            controller.setClient(client);
            Platform.runLater(() -> scene.setRoot(root));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void moveToRoom(Stone color) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("RoomGUI.fxml"));
            final Parent root = loader.load();
            RoomGUI controller = loader.getController();
            controller.setup(scene, client, color);
            Platform.runLater(() -> scene.setRoot(root));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}