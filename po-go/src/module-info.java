module po.go {
    requires javafx.fxml;
    requires javafx.controls;
    requires javafx.media;
    requires json.simple;

    opens client;
    opens shared;
}