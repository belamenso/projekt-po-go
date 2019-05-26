package client;

import go.*;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.skin.TableViewSkin;
import javafx.scene.control.skin.VirtualFlow;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import shared.RoomEvent;
import util.Pair;
import java.net.URL;
import java.util.*;

import static go.GameLogic.gameLogic;

public class RoomGUI implements Initializable {
    private ClientRoomListener crl;

    @FXML private Pane boardPane;
    private GoBoardGUI board;

    @FXML private Button passButton;

    @FXML private TableView<RoomEvent> messageTable;
    @FXML private Label infoLabel;
    @FXML private TextField chatField;
    @FXML private Button sendButton;
    private ObservableList<RoomEvent> messages;

    @FXML private Label captureLabelA;
    @FXML private Label captureLabelB;
    @FXML private Label capturedLabel;
    @FXML private Label lostLabel;

    @FXML private HBox sliderBox;
    @FXML private Slider historySlider;
    @FXML private Label current;
    private SimpleIntegerProperty historyCount;

    private SimpleBooleanProperty isPlayer;

    private Optional<GameplayManager.Result> cachedGameResult = Optional.empty();

    public void setup(ClientRoomListener clientRoomListener) {
        this.crl = clientRoomListener;

        isPlayer = new SimpleBooleanProperty(!crl.isSpectator());

        if(crl.isSpectator()) {
            infoLabel.setText("You are spectating");
        }

        board = new GoBoardGUI(boardPane, crl.getBoard());
        boardPane.setMinHeight(300);
        boardPane.setMinWidth(300);
        boardPane.prefHeightProperty().bind(boardPane.prefWidthProperty());
        boardPane.prefWidthProperty().bind(Bindings.min(SceneManager.getWidthProperty().multiply(0.7),
                                                        SceneManager.getHeightProperty().subtract(100)));

        historyCount = new SimpleIntegerProperty(crl.manager.getHistorySize());
        historySlider.maxProperty().bind(historyCount);
        historySlider.setValue(historyCount.get());
        historySlider.valueProperty().addListener(observable -> renderBoard());
        sliderBox.visibleProperty().bind(Bindings.greaterThan(historyCount, 1));
        current.textProperty().bind(Bindings.format(
                "%.0f / %d",
                historySlider.valueProperty(),
                historyCount
        ));

        messageTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if(newSelection != null) historySlider.setValue(newSelection.turnNumber);
        });

        passButton.disableProperty().bind(Bindings.or(
                Bindings.notEqual(historySlider.valueProperty(), historyCount),
                isPlayer.not()
        ));

        chatField.disableProperty().bind(isPlayer.not());
        sendButton.disableProperty().bind(isPlayer.not());

        captureLabelA.setText(isPlayer.get() ? "Captured:" : "Black:");
        captureLabelB.setText(isPlayer.get() ? "Lost:" : "White:");

        for(int i = 0; i < board.getSize(); ++ i) {
            for(int j = 0; j < board.getSize(); ++ j) {

                final int x = j;
                final int y = i;

                Rectangle rect = board.getArea(x, y);

                rect.mouseTransparentProperty().bind(Bindings.or(
                        Bindings.notEqual(historySlider.valueProperty(), historyCount),
                        isPlayer.not()
                ));

                rect.setOnMouseClicked(e -> {
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
                                    board.colorStone(pos.x, pos.y, board.colorToCapturedClass(crl.getColor().opposite));
                                });
                            }
                        }
                    }
                });

                rect.setOnMouseExited(e -> renderBoard());
            }
        }

        renderBoard();
    }

    void markTerritories() {
        assert crl.manager.finished() && !crl.wasInterruped();

        ArrayList<GameLogic.Territory> ts = gameLogic.capturedTerritories(crl.getBoard());
        for (GameLogic.Territory t : ts) {
            for (Pair<Integer, Integer> pos : t.territory) {
                Platform.runLater(() -> {
                    board.colorStone(pos.x, pos.y, board.colorToTerritoryClass(t.captor.get())); // TODO zmienić to optional tam
                });
            }
        }
    }

    /**
     * Renderuje planszę
     */
    void renderBoard() {

        if(historyCount.getValue() != crl.getTurnCount()) {
            boolean changeToNew = ((int) historySlider.getValue()) == crl.getTurnCount() - 1;
            historyCount.set(crl.getTurnCount());
            if (changeToNew) historySlider.setValue(crl.getTurnCount());
        }

        if(!crl.isSpectator()) {
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
        }

        int boardNum = (int) historySlider.getValue();

        if(crl.manager.finished() && boardNum == historyCount.getValue()) markTerritories();

        int idx = messages.size() - 1;
        while(idx > 0 && messages.get(idx).turnNumber > boardNum) -- idx;
        messageTable.getSelectionModel().select(idx);
        messageTable.scrollTo(idx);

        if(crl.isSpectator()) {
            capturedLabel.setText(Integer.toString(crl.manager.getCapturedBy(Stone.Black, boardNum)));
            lostLabel    .setText(Integer.toString(crl.manager.getCapturedBy(Stone.White, boardNum)));
        } else {
            capturedLabel.setText(Integer.toString(crl.manager.getCapturedBy(crl.getColor(), boardNum)));
            lostLabel    .setText(Integer.toString(crl.manager.getCapturedBy(crl.getColor().opposite, boardNum)));
        }

        board.render(crl.manager.getBoardByNumber(boardNum));
    }

    void addMessage(RoomEvent msg) {
        messages.add(msg);
        messageTable.scrollTo(messageTable.getItems().size() - 1);
    }

    @FXML
    void incSlider() {
        //System.out.println("++ " + historySlider.getValue() + " : " + historyCount.getValue());
        if(historySlider.getValue() + 1 <= historyCount.get()) historySlider.setValue(historySlider.getValue() + 1);
    }

    @FXML
    void decSlider() {
        //System.out.println("-- " + historySlider.getValue() + " : " + historyCount.getValue());
        if(historySlider.getValue() - 1 >= 0) historySlider.setValue(historySlider.getValue() - 1);
    }

    @FXML
    public void passButtonPressed() {
        assert !crl.isSpectator();

        crl.attemptedToPass();
    }

    @FXML
    public void quitButtonPressed() {
        crl.sendQuitRequest();
    }

    @FXML
    public void sendChat() {
        assert !crl.isSpectator();

        String msg = chatField.getText();
        chatField.clear();
        if(msg != null && msg.length()!=0)
            crl.sendChat(msg);
    }

    public void returnToLobby() {
        SceneManager.loadLobbyScreen();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        TableColumn<RoomEvent, String> nameColumn = new TableColumn<>("Messages");
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameColumn.setSortable(false);
        nameColumn.setEditable(false);
        nameColumn.setResizable(false);

        TableColumn<RoomEvent, String> timeColumn = new TableColumn<>("Time");
        timeColumn.setCellValueFactory(new PropertyValueFactory<>("time"));
        timeColumn.setMinWidth(50);
        timeColumn.setPrefWidth(50);
        timeColumn.setSortable(false);
        timeColumn.setEditable(false);
        timeColumn.setResizable(false);

        messages = FXCollections.observableArrayList();
        messageTable.setItems(messages);
        messageTable.getColumns().addAll(timeColumn, nameColumn);
        messageTable.setPlaceholder(new Label("No messages"));
        messageTable.setSkin(new TableViewSkin<RoomEvent>(messageTable));


        nameColumn.prefWidthProperty().bind(messageTable.widthProperty().subtract(54));
    }
}
