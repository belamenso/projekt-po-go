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
        window.setMinWidth(640);
        window.setMinHeight(480);

        Client client = new Client();
        Scene scene = new Scene(new Pane(), 640, 480);
        scene.getStylesheets().add(getClass().getResource("/css/style.css").toString());
        scene.setOnKeyPressed(keyEvent -> {
            if(keyEvent.getCode().equals(KeyCode.M)) {
                Settings.assertConfigurationExists();
                Settings.setSoundOn(!Settings.readSettings().soundOn);
                System.out.println("sound on: " + Settings.readSettings().soundOn);
            }
        });

        SceneManager.setup(scene, client);
        SceneManager.loadConnectionScreen();

        //Sounds.loadSounds();

        window.setScene(scene);

        window.setOnCloseRequest(windowEvent -> {
            client.close();
        });

        window.show();
    }
}
