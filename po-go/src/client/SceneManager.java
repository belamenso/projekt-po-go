package client;

import go.Stone;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;

import java.io.IOException;

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
            FXMLLoader loader = loadFXML("ConnectionPrompt.fxml");
            Parent root = loader.load();
            ConnectionPrompt controller = loader.getController();

            controller.setClient(client);

            Platform.runLater(() -> scene.setRoot(root));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static void loadLobbyScreen() {
        try {
            FXMLLoader loader = loadFXML("LobbyScreen.fxml");
            Parent root = loader.load();
            LobbyScreen controller = loader.getController();

            controller.setClient(client);

            Platform.runLater(() -> scene.setRoot(root));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static void loadRoomScreen(Stone color) {
        try {
            FXMLLoader loader = loadFXML("RoomGUI.fxml");
            Parent root = loader.load();
            RoomGUI controller = loader.getController();

            controller.setup(client, color);
            Platform.runLater(() -> scene.setRoot(root));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static FXMLLoader loadFXML(String name) {
        return new FXMLLoader(SceneManager.class.getResource(name));
    }
}
