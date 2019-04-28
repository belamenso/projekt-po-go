package client;

import go.Board;
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
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

public class RoomGUI implements Initializable {
    private Client client;
    private Scene scene;
    private ClientRoomListener crl;

    @FXML private TextField input;
    @FXML private Label messageLabel;
    @FXML private Group boardGroup;
    @FXML private Circle[][] stones;

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
        }

        for(int i = 0; i < size; ++ i) {
            for(int j = 0; j < size; ++ j) {

                final int x = j;
                final int y = i;
                Rectangle rect = new Rectangle(2*x*r, 2*y*r, 2*r, 2*r);
                rect.setStyle("-fx-fill: transparent");
                rect.setOnMouseClicked(e -> {
                    System.out.println("click " + y + " " + x);
                    // Z tego co się orientuje tu ma być jeszcze wywoływanie ruchu na lokalnym managerze
                    // Pewnie jakaś funkcja w ClientRoomListenerze która przeprowadza ruch
                    //client.sendMessage("MOVE " + x + " " + y);
                });

                Circle stone = new Circle((2*x+1)*r, (2*y+1)*r, r*0.8);
                stones[x][y] = stone;
                stone.setMouseTransparent(true);
                stone.setStyle("-fx-fill: #666666"); // ma byc transparent - teraz jest zeby wida było gdzie są
                boardGroup.getChildren().addAll(rect, stone);
            }
        }
    }


    // Zmienia kolor kamieni, wywoływana gedzie po każdej zmianie
    void renderBoard() {
        // Powinnow renderować planszę
        final Board toRender = crl.getBoard();
        Platform.runLater(() -> {
            System.out.println("Rendering board");
            for(int i = 0; i < toRender.getSize(); ++ i) {
                for(int j = 0; j < toRender.getSize(); ++ j) {
                    Optional<Stone> stone = toRender.get(i, j);
                    // TODO zmienic to na klasy w stylesheecie
                    System.out.print(stone.isEmpty() ? " " : (stone.get().equals(Stone.White)?"W":"B"));
                    if(stone.isEmpty()) {
                        stones[i][j].setStyle("-fx-fill: transparent");
                    } else if(stone.get().equals(Stone.White)){
                        stones[i][j].setStyle("-fx-fill: #ffffff");
                    } else {
                        stones[i][j].setStyle("-fx-fill: #000000");
                    }
                }
                System.out.print("\n");
            }
        });
    }

    public void setMessage(String msg) {
        messageLabel.setText(msg);
    }

    @FXML
    public void passButtonPressed() {
        System.out.println("pass");
        // Wysyła ruch do listenera
    }

    @FXML
    public void quitButtonPressed() {
        System.out.println("quit");
        // TODO
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
