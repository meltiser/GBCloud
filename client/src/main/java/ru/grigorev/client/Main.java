package ru.grigorev.client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import ru.grigorev.common.Message;
import ru.grigorev.common.MessageType;

public class Main extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/GUI.fxml"));
        Parent root = loader.load();
        MainController controller = loader.getController();
        controller.setPrimaryStage(primaryStage);
        primaryStage.setTitle("GBCloud");
        primaryStage.setScene(new Scene(root, 700, 400));
        //primaryStage.setOnCloseRequest((c) -> Connection.sendMessage(new Message(MessageType.GOODBYE)));
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
