package edu.ggc.lutz.dtmf.gui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class GUI extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("dtmf.fxml"));
        primaryStage.setTitle("DTMF DETECTOR GOERTZEL ALGORITHM");
        primaryStage.setScene(new Scene(root, 400, 302));
        root.requestFocus();
        primaryStage.show();



    }
}
