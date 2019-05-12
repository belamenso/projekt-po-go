package client;

import go.*;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import util.Pair;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.Optional;
import java.util.ResourceBundle;

import static go.GameLogic.gameLogic;

public class RoomGUI implements Initializable {
    private ClientRoomListener crl;

    @FXML private Pane boardPane;
    private GoBoardGUI board;

    @FXML private TableView<Message> messageTable;
    @FXML private Label infoLabel;
    @FXML private TextField chatField;
    private ObservableList<Message> messages;

    @FXML private Label capturedLabel;
    @FXML private Label lostLabel;

    @FXML Slider historySlider;
    @FXML private Label current;
    SimpleIntegerProperty historyCount;

    private Optional<GameplayManager.Result> cachedGameResult = Optional.empty();

    private String colorToCapturedClass(Stone color) {
        return color == Stone.White ? "stone-potentially-captured-white" : "stone-potentially-captured-black";
    }

    private String colorToTerritoryClass(Stone color) {
        return color == Stone.White ? "white-territory" : "black-territory";
    }

    public void setup(ClientRoomListener clientRoomListener) {
        this.crl = clientRoomListener;

        board = new GoBoardGUI(boardPane, crl.getBoard());
        boardPane.setMinHeight(300);
        boardPane.setMinWidth(300);
        boardPane.prefHeightProperty().bind(boardPane.prefWidthProperty());
        boardPane.prefWidthProperty().bind(Bindings.min(SceneManager.getWidthProperty().multiply(0.7),
                                                        SceneManager.getHeightProperty().subtract(100)));

        historyCount = new SimpleIntegerProperty(1);
        historySlider.setMin(1);
        historySlider.setBlockIncrement(1);
        historySlider.setMajorTickUnit(1);
        historySlider.setMinorTickCount(0);
        historySlider.setShowTickMarks(true);
        historySlider.setSnapToTicks(true);
        historySlider.maxProperty().bind(historyCount);
        historySlider.valueProperty().addListener(observable -> renderBoard());
        historySlider.prefWidthProperty().bind(boardPane.prefWidthProperty().subtract(60));
        historySlider.visibleProperty().bind(Bindings.greaterThan(historyCount, 1));
        current.visibleProperty().bind(Bindings.greaterThan(historyCount, 1));
        current.textProperty().bind(Bindings.format(
                "%.0f / %d",
                historySlider.valueProperty(),
                historyCount
        ));

        for(int i = 0; i < board.getSize(); ++ i) {
            for(int j = 0; j < board.getSize(); ++ j) {

                final int x = j;
                final int y = i;

                Rectangle rect = board.getArea(x, y);

                rect.setOnMouseClicked(e -> {
                    if((int) historySlider.getValue() != historyCount.getValue()) return;

                    System.out.println("click " + y + " " + x);
                    crl.attemptedToMakeMove(x, y);
                });

                rect.setOnMouseEntered(e -> {
                    if((int) historySlider.getValue() != historyCount.getValue()) return;

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

        capturedLabel.setText(Integer.toString(crl.manager.getCapturedBy(crl.getColor())));
        lostLabel    .setText(Integer.toString(crl.manager.getCapturedBy(crl.getColor().opposite)));

        int boardNum = (int) historySlider.getValue();
        System.out.println("board " + boardNum);

        if(crl.manager.finished() && boardNum == historyCount.getValue()) markTerritories();

        board.render(crl.manager.getBoardByNumber(boardNum));
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
        crl.sendQuitRequest();
    }

    @FXML
    public void sendChat() {
        String msg = chatField.getText();
        chatField.clear();

        crl.sendChat(msg);
    }

    public void returnToLobby() {
        SceneManager.loadLobbyScreen();
    }

    public class Message {
        private String name, time;

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
