package client;

import go.Board;
import go.GameplayManager;
import go.Stone;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import shared.RoomData;
import shared.RoomEvent;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class LobbyScreen implements Initializable {
    private ClientLobbyListener cl;

    @FXML private TableView<RoomData> rooms;
    @FXML private TextField nameField;
    @FXML private Label messageLabel;
    @FXML private ChoiceBox<String> sizeDropdown;
    @FXML private ChoiceBox<String> colorDropdown;

    public void setup(ClientLobbyListener cl) {
        this.cl = cl;
        update();
    }

    @FXML
    public void createRoom() {
        String name = nameField.getText();
        nameField.clear();
        setMessage("");

        if(name == null || name.equals("")) {
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
    public void spectateRoom() {
        String name = rooms.getSelectionModel().getSelectedItem().getName();
        cl.sendSpectateRequest(name);
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

        TableColumn<RoomData, Integer> specColumn = new TableColumn<>("Spectators");
        specColumn.setMinWidth(100);
        specColumn.setCellValueFactory(new PropertyValueFactory<>("spectatorNo"));

        rooms.setRowFactory(tv -> {
            TableRow<RoomData> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    RoomData rowData = row.getItem();
                    switch (rowData.getState()) {
                        case "EmptyRoom":
                            cl.sendJoinRoomRequest(rowData.name, getColorSelection());
                            break;
                        case "WaitingForWhite":
                            cl.sendJoinRoomRequest(rowData.name, Stone.White);
                            break;
                        default:
                            cl.sendJoinRoomRequest(rowData.name, Stone.Black);
                    }

                }
            });
            return row;
        });

        rooms.getColumns().clear();
        rooms.getColumns().addAll(nameColumn, stateColumn, sizeColumn, specColumn);
        rooms.setPlaceholder(new Label("No active rooms available"));

        sizeDropdown.getItems().addAll("9x9", "13x13", "19x19");
        sizeDropdown.getSelectionModel().clearAndSelect(0);

        colorDropdown.getItems().addAll("Black", "White");
        colorDropdown.getSelectionModel().clearAndSelect(0);
    }

    Stone getColorSelection() {
        return colorDropdown.getValue().equals("White") ? Stone.White : Stone.Black;
    }

    void returnToConnecting() {
        SceneManager.loadConnectionScreen();
    }

    void moveToRoom(Stone color, Board.BoardSize size) {
        SceneManager.loadRoomScreen(color, size);
    }

    void moveToRoomSpectator(Board.BoardSize size, List<GameplayManager.Move> moves, List<RoomEvent> events) {
        SceneManager.loadRoomScreenAsSpectator(size, moves, events);
    }
}