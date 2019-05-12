package client;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
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
        scene.getStylesheets().add(getClass().getResource("style.css").toString());
        scene.setOnKeyPressed(keyEvent -> {
            System.out.println("keyPressed " + keyEvent.getCode().getChar());
            if(keyEvent.getCode().equals(KeyCode.M)) {
                Settings.assertConfigurationExists();
                System.out.println(Settings.readSettings().soundOn);
                Settings.setSoundOn(!Settings.readSettings().soundOn);
            }
        });

        SceneManager.setup(scene, client);
        SceneManager.loadConnectionScreen();

        Sounds.loadSounds();

        window.setScene(scene);

        window.setOnCloseRequest(windowEvent -> {
            client.close();
        });

        window.show();
    }
}
