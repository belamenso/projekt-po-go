package client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage window) throws Exception {
        window.setTitle("Pogo");

        Client client = new Client();

        FXMLLoader loader = new FXMLLoader(getClass().getResource("ConnectionPrompt.fxml"));
        Parent root = loader.load();
        Scene scene = new Scene(root, 800, 600);
        ConnectionPrompt controller = loader.getController();
        controller.setScene(scene);
        controller.setClient(client);

        window.setScene(scene);
        window.show();

        window.setOnCloseRequest(windowEvent -> {
            client.close();
        });
    }
}
