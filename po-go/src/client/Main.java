package client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public class Main extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage window) {
        window.setTitle("Pogo");
        window.setMinWidth(600);
        window.setMinHeight(400);

        Client client = new Client();
        Scene scene = new Scene(new Pane(), 800, 600);

        SceneManager.setup(scene, client);
        SceneManager.loadConnectionScreen();



        scene.getStylesheets().add(getClass().getResource("style.css").toString());

        window.setScene(scene);
        window.show();

        window.setOnCloseRequest(windowEvent -> {
            client.close();
        });
    }
}
