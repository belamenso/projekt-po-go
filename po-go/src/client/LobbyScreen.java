package client;

import go.Board;
import go.Stone;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class LobbyScreen implements Initializable {
    ClientLobbyListener cl;

    @FXML private TableView<RoomData> rooms;
    @FXML private TextField nameField;
    @FXML private Label messageLabel;
    @FXML private ChoiceBox<String> sizeDropdown;

    public void setup(ClientLobbyListener cl) {
        this.cl = cl;
        update();
    }

    @FXML
    public void createRoom() {
        String name = nameField.getText();
        nameField.clear();
        setMessage("");

        if(name == null || name == "") {
            setMessage("Nazwa nie może być pusta");
            return;
        }

        if(!name.matches("[a-zA-Z0-9 _]+")) {
            setMessage("Nazwa może składać się jedynie z liter, cyfr i spacji");
            return;
        }

        Board.BoardSize size = null;
        switch (sizeDropdown.getValue()) {
            case   "9x9" : size = Board.BoardSize.Size9;  break;
            case "13x13" : size = Board.BoardSize.Size13; break;
            case "19x19" : size = Board.BoardSize.Size19; break;
        }

        cl.sendCreateRequest(name, size);
    }



    @FXML
    public void joinRoom() {
        String name = rooms.getSelectionModel().getSelectedItems().get(0).getName();
        cl.sendJoinRoomRequest(name);
    }

    @FXML
    public void update() {
        cl.sendUpdateRequest();
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

        TableColumn<RoomData, String> stateColumn = new TableColumn<>("State");
        stateColumn.setMinWidth(150);
        stateColumn.setCellValueFactory(new PropertyValueFactory<>("state"));

        TableColumn<RoomData, Integer> sizeColumn = new TableColumn<>("Board size");
        sizeColumn.setMinWidth(100);
        sizeColumn.setCellValueFactory(new PropertyValueFactory<>("size"));

        rooms.setRowFactory(tv -> {
            TableRow<RoomData> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    RoomData rowData = row.getItem();
                    cl.sendJoinRoomRequest(rowData.name);
                }
            });
            return row;
        });

        rooms.getColumns().clear();
        rooms.getColumns().addAll(nameColumn, stateColumn, sizeColumn);
        rooms.setPlaceholder(new Label("No active rooms available"));

        sizeDropdown.getItems().addAll("9x9", "13x13", "19x19");
        sizeDropdown.getSelectionModel().clearAndSelect(0);
    }

    void returnToConnecting() {
        SceneManager.loadConnectionScreen();
    }

    void moveToRoom(Stone color, Board.BoardSize size) {
        SceneManager.loadRoomScreen(color, size);
    }
}