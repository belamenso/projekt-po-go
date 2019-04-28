package client;

import go.Board;
import go.GameplayManager;
import go.ReasonMoveImpossible;
import go.Stone;
import javafx.application.Platform;
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
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import util.Pair;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Optional;
import java.util.ResourceBundle;

import static go.GameLogic.gameLogic;

public class RoomGUI implements Initializable {
    private Client client;
    private Scene scene;
    private ClientRoomListener crl;

    @FXML private TextField input;
    @FXML private Label messageLabel;
    @FXML private Group boardGroup;
    @FXML private Circle[][] stones;
    @FXML private TableView<Message> messageTable;
    @FXML private Label infoLabel;
    private ObservableList<Message> messages;

    private Optional<GameplayManager.Result> cachedGameResult = Optional.empty();

    private String colorToCapturedClass(Stone color) {
        return color == Stone.White ? "stone-potentially-captured-white" : "stone-potentially-captured-black";
    }

    public void setup(Scene scene, Client client, Stone color) {
        this.scene = scene;
        this.client = client;
        crl = new ClientRoomListener(client, color, this);
        client.setListener(crl);

        int size = crl.getSize();
        double r = 250.0 / size;

        stones = new Circle[size][size];

        for(int i = 0; i < size; ++ i) {
            boardGroup.getChildren().add(new Line(r, (2 * i + 1) * r, (2 * size - 1) * r, (2 * i + 1) * r));
            boardGroup.getChildren().add(new Line((2 * i + 1) * r, r, (2 * i + 1) * r, (2 * size - 1) * r));

            Text text = new Text(crl.getBoard().columnNumeral(i));
            text.setX((2 * i + 1) * r-3);
            text.setY(-2);
            boardGroup.getChildren().add(text);
            Text text2 = new Text(Integer.toString(i+1));
            text2.setY((2 * (size-i-1) + 1) * r+5);
            text2.setX(-10);
            boardGroup.getChildren().add(text2);
        }

        for(int i = 0; i < size; ++ i) {
            for(int j = 0; j < size; ++ j) {

                final int x = j;
                final int y = i;
                Rectangle rect = new Rectangle(2*x*r, 2*y*r, 2*r, 2*r);
                rect.setStyle("-fx-fill: transparent");
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
                                    stones[pos.x][pos.y].getStyleClass().clear();
                                    stones[pos.x][pos.y].getStyleClass().add(colorToCapturedClass(crl.getColor().opposite));
                                });
                            }
                        }
                    }
                });

                rect.setOnMouseExited(e -> renderBoard());

                Circle stone = new Circle((2*x+1)*r, (2*y+1)*r, r*0.8);
                stones[x][y] = stone;
                stone.setMouseTransparent(true);
                stone.getStyleClass().clear();
                stone.getStyleClass().add("stone-invisible");
                boardGroup.getChildren().addAll(rect, stone);
            }
        }
    }


    // Renderuje planszę
    void renderBoard() {

        infoLabel.setTextFill(Color.BLACK);
        if (crl.myTurn()) {
            infoLabel.setText("Your move");
        } else if (crl.manager.interrupted()) {
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
            for(int i = 0; i < toRender.getSize(); ++ i) {
                for(int j = 0; j < toRender.getSize(); ++ j) {
                    Optional<Stone> stone = toRender.get(i, j);
                    if(stone.isEmpty()) {
                        stones[i][j].getStyleClass().clear();
                        stones[i][j].getStyleClass().add("stone-invisible");
                    } else if(stone.get().equals(Stone.White)){
                        stones[i][j].getStyleClass().clear();
                        stones[i][j].getStyleClass().add("stone-white");
                    } else {
                        stones[i][j].getStyleClass().clear();
                        stones[i][j].getStyleClass().add("stone-black");
                    }
                }
            }
        });
    }

    public void setInfo(String info) {
        infoLabel.setText(info);
    }

    public void addMessage(String msg) {
        System.out.println("addMessage: " + msg);
        messages.add(new Message(msg));
        messageTable.scrollTo(messageTable.getItems().size() - 1);
    }

    @FXML
    public void passButtonPressed() { crl.attemptedToPass(); }

    @FXML
    public void quitButtonPressed() { // TODO
        System.out.println("quit");
        client.sendMessage("quit");
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
        public Message(){ this.name = ""; }
        public Message(String name){ this.name = name; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
    }


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        TableColumn<Message, String> nameColumn = new TableColumn<>("Messages");
        nameColumn.setMinWidth(198);
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameColumn.setSortable(false);
        nameColumn.setEditable(false);

        messages = FXCollections.observableArrayList();
        messageTable.setItems(messages);
        messageTable.getColumns().add(nameColumn);
        nameColumn.setPrefWidth(messageTable.getPrefWidth() - 2);
        nameColumn.setResizable(false);
    }

}
