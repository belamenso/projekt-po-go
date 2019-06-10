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
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import shared.RoomEvent;
import util.Pair;
import java.net.URL;
import java.util.*;

import static go.GameLogic.gameLogic;

public class RoomScene implements Initializable {
    private ClientRoomListener crl;

    @FXML private Pane boardPane;
    private GoBoard board;

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

    @FXML private Button yesButton;
    @FXML private Button noButton;
    @FXML private Button doneButton;

    @FXML private TextField forkNameField;
    @FXML private Button joinWhiteButton;
    @FXML private Button joinBlackButton;
    @FXML private Button confirmButton;
    @FXML private Button createForkButton;

    private Optional<GameplayManager.Result> cachedGameResult = Optional.empty();

    void setup(ClientRoomListener clientRoomListener) {
        this.crl = clientRoomListener;

        SimpleBooleanProperty isPlayer = new SimpleBooleanProperty(!crl.isSpectator());

        if(crl.isSpectator()) {
            infoLabel.setText("You are spectating");
        }

        board = new GoBoard(boardPane, crl.getBoard());
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

        passButton.visibleProperty().bind(isPlayer);
        passButton.managedProperty().bind(passButton.visibleProperty());

        chatField.disableProperty().bind(isPlayer.not());
        sendButton.disableProperty().bind(isPlayer.not());

        createForkButton.visibleProperty().bind(isPlayer.not());
        createForkButton.managedProperty().bind(isPlayer.not());
        forkNameField.visibleProperty().bind(isPlayer.not());
        forkNameField.managedProperty().bind(isPlayer.not());

        chatField.visibleProperty().bind(isPlayer);
        chatField.managedProperty().bind(isPlayer);
        sendButton.visibleProperty().bind(isPlayer);
        sendButton.managedProperty().bind(isPlayer);

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
                    if(crl.isRemovalPhaseOn()) return;

                    if (crl.myTurn()) {
                        Optional<ReasonMoveImpossible> reason = gameLogic.movePossible(crl.getBoard(), x, y, crl.getColor());
                        if (reason.isEmpty()) {
                            Board boardCopy = crl.getBoard().cloneBoard();
                            boardCopy.getBoard()[x][y] = Optional.of(crl.getColor());
                            ArrayList<Pair<Integer, Integer>> potentiallyCaptured = gameLogic.captured(boardCopy, crl.getColor());
                            Platform.runLater(() -> {
                                potentiallyCaptured.forEach(pos -> {
                                        board.colorStone(pos.x, pos.y, board.colorToCapturedClass(crl.getColor().opposite));
                                });
                            });
                        }
                    }
                });

                rect.setOnMouseExited(e -> renderBoard());
            }
        }

        renderBoard();
    }

    /**
     * Renderuje planszę
     */
    void renderBoard() {

        if(historyCount.getValue() != crl.getTurnCount()) {
            boolean changeToNew = historySlider.getValue() == historySlider.getMax();
            historyCount.set(crl.getTurnCount());
            if (changeToNew) historySlider.setValue(crl.getTurnCount());
        }

        renderInfoLabel();

        int boardNum = (int) Math.round(historySlider.getValue());

        double val = historySlider.getValue();
        int idx = getLastMessageInTurn(boardNum);
        messageTable.getSelectionModel().select(idx);
        messageTable.scrollTo(idx);
        historySlider.setValue(val);

        if(crl.isSpectator()) {
            capturedLabel.setText(Integer.toString(crl.manager.getCapturedBy(Stone.Black, boardNum)));
            lostLabel    .setText(Integer.toString(crl.manager.getCapturedBy(Stone.White, boardNum)));
        } else {
            capturedLabel.setText(Integer.toString(crl.manager.getCapturedBy(crl.getColor(), boardNum)));
            lostLabel    .setText(Integer.toString(crl.manager.getCapturedBy(crl.getColor().opposite, boardNum)));
        }

        Board toRender = crl.manager.getBoardByNumber(boardNum);

        board.render(toRender);

        if(crl.manager.finished() && boardNum == historyCount.getValue()) markTerritories();

        if(boardNum != 1) {
            GameplayManager.Move m = crl.manager.getMoveHistory().get(boardNum - 2);
            if(m instanceof GameplayManager.StonePlacement) {
                GameplayManager.StonePlacement sp = (GameplayManager.StonePlacement) m;
                board.colorStone(sp.position.x, sp.position.y,
                                board.colorToLastMoveClass(toRender.get(sp.position.x, sp.position.y).get()));
            }
        }

        if(crl.isRemovalPhaseOn() && boardNum == historyCount.getValue()) {
            crl.getRemovalStones().forEach(p -> board.colorStone(p.x, p.y,
                        board.colorToCapturedClass(toRender.get(p.x, p.y).get())));
        }
    }

    private int getLastMessageInTurn(int turn) {
        int idx = messages.size() - 1;
        while(idx > 0 && messages.get(idx).turnNumber > turn) -- idx;
        return idx;
    }

    /**
     * Koloruje teretoria po zakonczeniu gry
     */
    private void markTerritories() {
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
     * Ustawia infoLabel w zależności od stanu pokoju
     */
    private void renderInfoLabel() {
        if(crl.isSpectator()) return;

        infoLabel.setTextFill(Color.BLACK);

        if(!crl.isGameStarted()) {
            infoLabel.setText("Waiting for the game to start");
        } else if (crl.myTurn()) {
            infoLabel.setText("Your move");
        } else if (crl.wasInterruped()) {
            infoLabel.setText("Opponent has left the game");
        } else if (crl.manager.finished()) {
            if (cachedGameResult.isEmpty())
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

        if(crl.isRemovalPhaseOn()) {
            infoLabel.setText("Stone removal phase");

            if (crl.nominating()) {
                infoLabel.setText("Choose groups to remove");
            } else if (crl.accepting()) {
                infoLabel.setText("Do you agree to removal of these groups?");
            }
        }
    }

    void addMessage(RoomEvent msg) {
        messages.add(msg);
        messageTable.scrollTo(messageTable.getItems().size() - 1);
    }

    private void hideButton(Button button) { button.setManaged(false); button.setVisible(false); }
    private void showButton(Button button) { button.setVisible(true); button.setManaged(true); }

    void showAcceptanceButtons() { showButton(yesButton); showButton(noButton); }
    void showNominationButton() { showButton(doneButton); }

    @FXML
    void accept() {
        crl.acceptRemoval();
        hideButton(yesButton);
        hideButton(noButton);
    }

    @FXML
    void decline() {
        crl.declineRemoval();
        hideButton(yesButton);
        hideButton(noButton);
    }

    @FXML
    void doneNominating() {
        crl.finishNominating();
        hideButton(doneButton);
    }

    @FXML
    void incSlider() {
        if (historySlider.getValue() + 1 <= historyCount.get())
            historySlider.setValue(historySlider.getValue() + 1);
    }

    @FXML
    void decSlider() {
        if (historySlider.getValue() - 1 >= 0)
            historySlider.setValue(historySlider.getValue() - 1);
    }

    @FXML
    public void passButtonPressed() {
        assert !crl.isSpectator();

        crl.attemptedToPass();
    }

    @FXML
    public void quitButtonPressed() { crl.sendQuitRequest(); }

    @FXML
    public void sendChat() {
        assert !crl.isSpectator();

        String msg = chatField.getText();
        chatField.clear();
        if(msg != null && msg.length()!=0)
            crl.sendChat(msg);
    }

    @FXML
    public void joinWhite() {
        crl.joinFork(Stone.White);
        hideButton(joinWhiteButton);
        hideButton(joinBlackButton);
        infoLabel.setText("You are spectating");
    }

    @FXML
    public void joinBlack() {
        crl.joinFork(Stone.Black);
        hideButton(joinWhiteButton);
        hideButton(joinBlackButton);
        infoLabel.setText("You are spectating");
    }

    @FXML
    public void confirm() {
        crl.confirm();
        hideButton(confirmButton);
        infoLabel.setText("You are spectating");
    }

    @FXML
    public void createFork() {
        String name = forkNameField.getText();
        crl.createFork(name, (int) Math.round(historySlider.getValue()));
    }

    void displayForkError() {
        infoLabel.setText("Fork couldn't be created");
        showButton(confirmButton);
    }

    void displayForkCreated() {
        infoLabel.setText("Fork created");
        showButton(joinWhiteButton);
        showButton(joinBlackButton);
    }

    void returnToLobby() {
        SceneManager.loadLobbyScreen();
    }

    void moveToAnotherRoom(Stone color, Board.BoardSize size, List<GameplayManager.Move> moves, List<RoomEvent> events) {
        SceneManager.loadRoomScreen(color, size, moves, events);
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        hideButton(yesButton);
        hideButton(noButton);
        hideButton(doneButton);
        hideButton(joinBlackButton);
        hideButton(joinWhiteButton);
        hideButton(confirmButton);

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

        nameColumn.setCellFactory(tc -> {
            TableCell<RoomEvent, String> cell = new TableCell<>();
            Text text = new Text();
            cell.setGraphic(text);
            cell.setPrefHeight(Control.USE_COMPUTED_SIZE);
            text.wrappingWidthProperty().bind(nameColumn.widthProperty());
            text.textProperty().bind(cell.itemProperty());
            return cell;
        });

        messageTable.widthProperty().addListener((value) -> {
            ScrollBar scrollBar = (ScrollBar) messageTable.lookup(".scroll-bar:vertical");
            if(scrollBar == null) return;
            //System.out.println("asdf");
            nameColumn.prefWidthProperty().bind(Bindings.when(scrollBar.visibleProperty())
                        .then(messageTable.widthProperty().subtract(54).subtract(scrollBar.widthProperty()))
                        .otherwise(messageTable.widthProperty().subtract(54)));
        });

        nameColumn.prefWidthProperty().bind(messageTable.widthProperty().subtract(80));
    }
}
