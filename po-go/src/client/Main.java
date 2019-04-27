package client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class Main extends Application {

    private Stage window;
    private Client client;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        window = primaryStage;
        window.setTitle("Po-Go");
        window.setResizable(false);

        client = new Client();

        FXMLLoader loader = new FXMLLoader(getClass().getResource("ConnectionPrompt.fxml"));
        Parent root = loader.load();
        Scene scene = new Scene(root, 800, 600);
        ConnectionPrompt controller = loader.getController();
        controller.setScene(scene);
        controller.setClient(client);

        window.setScene(scene);
        window.show();
    }


}
