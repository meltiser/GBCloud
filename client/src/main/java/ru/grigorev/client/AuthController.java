package ru.grigorev.client;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * @author Dmitriy Grigorev
 */
public class AuthController {
    public TextField login;
    public PasswordField password;
    public VBox authScene;
    private Stage stage;

    public Stage getStage() {
        return stage;
    }

    public void init() {
        try {
            stage = new Stage();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Authorization.fxml"));
            Parent root = loader.load();
            AuthController auth = loader.getController();
            stage.setTitle("Autorization");
            stage.setScene(new Scene(root, 400, 200));
            stage.initModality(Modality.APPLICATION_MODAL);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void auth(ActionEvent actionEvent) {
        System.out.println(String.format("Login: %s, Pass: %s", login.getText(), password.getText()));
        authScene.getScene().getWindow().hide();
    }
}
