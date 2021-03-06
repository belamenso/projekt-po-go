package client;

import go.Board;
import go.GameplayManager;
import go.Stone;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import shared.RoomEvent;

import java.io.IOException;
import java.util.List;

/**
 * Zarządza ładowaniem scen i zmienianiem listenerów klienta
 */

class SceneManager {
    private static Scene scene;
    private static Client client;

    static ReadOnlyDoubleProperty  getWidthProperty() { return scene.widthProperty(); }
    static ReadOnlyDoubleProperty getHeightProperty() { return scene.heightProperty(); }

    static void setup(Scene scene, Client client) {
        SceneManager.scene = scene;
        SceneManager.client = client;
    }

    static void loadConnectionScreen() {
        try {
            FXMLLoader loader = loadFXML("ConnectionScene");
            Parent root = loader.load();
            ConnectionScene controller = loader.getController();

            ConnectListener cl = new ConnectListener(controller, client);
            controller.setup(cl);

            Platform.runLater(() -> scene.setRoot(root));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static void loadLobbyScreen() {
        try {
            FXMLLoader loader = loadFXML("LobbyScene");
            Parent root = loader.load();
            LobbyScene controller = loader.getController();

            ClientLobbyListener cl = new ClientLobbyListener(controller, client);
            controller.setup(cl);

            Platform.runLater(() -> scene.setRoot(root));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static void loadRoomScreen(Stone color, Board.BoardSize size, List<GameplayManager.Move> moves, List<RoomEvent> events) {
        try {
            FXMLLoader loader = loadFXML("RoomScene");
            Parent root = loader.load();
            RoomScene controller = loader.getController();

            ClientRoomListener crl = new ClientRoomListener(controller, client, color, size, moves, events);
            controller.setup(crl);

            Platform.runLater(() -> scene.setRoot(root));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private static FXMLLoader loadFXML(String name) {
        return new FXMLLoader(SceneManager.class.getResource("/fxml/"+name+".fxml"));
    }
}
