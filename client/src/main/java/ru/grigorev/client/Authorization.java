package ru.grigorev.client;

import javafx.event.ActionEvent;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

/**
 * @author Dmitriy Grigorev
 */
public class Authorization {
    public TextField login;
    public PasswordField password;
    public VBox authScene;

    public void auth(ActionEvent actionEvent) {
        System.out.println(String.format("Login: %s, Pass: %s", login.getText(), password.getText()));
        authScene.getScene().getWindow().hide();
    }
}
