package client;

import go.*;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import util.Pair;

import java.io.IOException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Optional;
import java.util.ResourceBundle;

import static go.GameLogic.gameLogic;

public class RoomGUI implements Initializable {
    private Client client;
    private Scene scene;
    private ClientRoomListener crl;

    @FXML private Pane boardPane;
    private GoBoardGUI board;

    @FXML private TableView<Message> messageTable;
    @FXML private Label infoLabel;
    @FXML private TextField chatField;
    private ObservableList<Message> messages;

    private Optional<GameplayManager.Result> cachedGameResult = Optional.empty();

    private String colorToCapturedClass(Stone color) {
        return color == Stone.White ? "stone-potentially-captured-white" : "stone-potentially-captured-black";
    }

    private String colorToTerritoryClass(Stone color) {
        return color == Stone.White ? "white-territory" : "black-territory";
    }

    public void setup(Scene scene, Client client, Stone color) {
        this.scene = scene;
        this.client = client;
        crl = new ClientRoomListener(client, color, this);
        client.setListener(crl);

        board = new GoBoardGUI(boardPane, crl.getBoard());
        boardPane.setMinHeight(300);
        boardPane.setMinWidth(300);
        boardPane.prefHeightProperty().bind(boardPane.prefWidthProperty());
        boardPane.prefWidthProperty().bind(Bindings.min(scene.widthProperty().multiply(0.7), scene.heightProperty().subtract(80)));

        for(int i = 0; i < board.getSize(); ++ i) {
            for(int j = 0; j < board.getSize(); ++ j) {

                final int x = j;
                final int y = i;

                Rectangle rect = board.getArea(x, y);

                rect.setOnMouseClicked(e -> {
                    System.out.println("click " + y + " " + x);
                    crl.attemptedToMakeMove(x, y);
                });

                rect.setOnMouseEntered(e -> {
                    if (crl.myTurn()) {
                        Optional<ReasonMoveImpossible> reason = gameLogic.movePossible(crl.getBoard(), x, y, crl.getColor());
                        if (reason.isEmpty()) {
                            Board boardCopy = crl.getBoard().cloneBoard();
                            boardCopy.getBoard()[x][y] = Optional.of(crl.getColor());
                            ArrayList<Pair<Integer, Integer>> potentiallyCaptured = gameLogic.captured(boardCopy, crl.getColor());
                            for (Pair<Integer, Integer> pos : potentiallyCaptured) {
                                Platform.runLater(() -> {
                                    board.colorStone(pos.x, pos.y, colorToCapturedClass(crl.getColor().opposite));
                                });
                            }
                        }
                    }
                });

                rect.setOnMouseExited(e -> renderBoard());
            }
        }
    }

    void markTerritories() {
        assert crl.manager.finished() && !crl.wasInterruped();

        ArrayList<GameLogic.Territory> ts = gameLogic.capturedTerritories(crl.getBoard());
        for (GameLogic.Territory t : ts) {
            for (Pair<Integer, Integer> pos : t.territory) {
                Platform.runLater(() -> {
                    board.colorStone(pos.x, pos.y, colorToTerritoryClass(t.captor.get())); // TODO zmienić to optional tam
                });
            }
        }
    }

    // Renderuje planszę
    void renderBoard() {

        infoLabel.setTextFill(Color.BLACK);
        if (crl.myTurn()) {
            infoLabel.setText("Your move");
        } else if (crl.wasInterruped()) {
            infoLabel.setText("Opponent has left the game");
        } else if (crl.manager.finished()) {
            if (!cachedGameResult.isPresent())
                cachedGameResult = Optional.of(crl.manager.result());
            if (cachedGameResult.get().winner == crl.getColor()) {
                infoLabel.setText("You won");
                infoLabel.setTextFill(Color.DARKGREEN);
            } else {
                infoLabel.setText("You lost");
                infoLabel.setTextFill(Color.CRIMSON);
            }
            infoLabel.setText(infoLabel.getText()
                    + "\nWhite " + cachedGameResult.get().whitePoints + ", Black: " + cachedGameResult.get().blackPoints);
        } else if (crl.manager.inProgress()) {
            infoLabel.setText("Waiting for opponent's move");
        } else assert false;

        final Board toRender = crl.getBoard();
        Platform.runLater(() -> {
            board.render(toRender);
        });

        if (crl.manager.finished()) markTerritories();
    }

    public void setInfo(String info) {
        infoLabel.setText(info);
    }

    public void addMessage(String msg, Date start) {
        if (start == null) {
            messages.add(new Message(msg, ""));
        } else {
            long interval = (new Date().getTime() - start.getTime()) / 1000;
            String seconds = Long.toString(interval % 60);
            if (seconds.length() == 1) seconds = "0" + seconds;
            String timestamp = interval / 60 + ":" + seconds;
            messages.add(new Message(msg, timestamp));
        }
        messageTable.scrollTo(messageTable.getItems().size() - 1);
    }

    @FXML
    public void passButtonPressed() { crl.attemptedToPass(); }

    @FXML
    public void quitButtonPressed() { // TODO
        System.out.println("quit");
        client.sendMessage("quit");
    }

    @FXML
    public void sendChat() {
        String msg = chatField.getText();
        chatField.clear();

        crl.sendChat(msg);
    }

    public void returnToLobby() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("LobbyScreen.fxml"));
        Parent root = loader.load();
        LobbyScreen controller = loader.getController();
        controller.setScene(scene);
        controller.setClient(client);
        Platform.runLater(()->scene.setRoot(root));
    }

    public class Message {
        private String name;
        private String time;
        public Message(){ this.name = ""; this.time=""; }
        public Message(String name, String time){ this.name = name; this.time = time; }
        public String getName() { return name; }
        public String getTime() { return time; }
        public void setName(String name) { this.name = name; }
        public void setTime(String time) { this.time = time; }
    }


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        TableColumn<Message, String> nameColumn = new TableColumn<>("Messages");
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameColumn.setSortable(false);
        nameColumn.setEditable(false);
        nameColumn.setResizable(false);

        TableColumn<Message, String> timeColumn = new TableColumn<>("Time");
        timeColumn.setMinWidth(50);
        timeColumn.setPrefWidth(50);
        timeColumn.setCellValueFactory(new PropertyValueFactory<>("time"));
        timeColumn.setSortable(false);
        timeColumn.setEditable(false);
        timeColumn.setResizable(false);

        messages = FXCollections.observableArrayList();
        messageTable.setItems(messages);
        messageTable.getColumns().addAll(timeColumn, nameColumn);
        messageTable.setPlaceholder(new Label("No messages"));
        messageTable.setSelectionModel(null);

        nameColumn.prefWidthProperty().bind(messageTable.widthProperty().subtract(54));
        chatField.prefWidthProperty().bind(messageTable.widthProperty().subtract(60));
    }
}
