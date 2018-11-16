package ru.grigorev.client;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import ru.grigorev.common.AuthMessage;
import ru.grigorev.common.Message;
import ru.grigorev.common.MessageType;

import java.io.IOException;

/**
 * @author Dmitriy Grigorev
 */
public class AuthController {
    public TextField login;
    public PasswordField password;
    public VBox authScene;
    private Stage authStage;
    private Stage primaryStage;

    public boolean isAuthorized() {
        return isAuthorized;
    }

    private boolean isAuthorized = false;

    public Stage getAuthStage() {
        return authStage;
    }

    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    public void init() {
        try {
            authStage = new Stage();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Authorization.fxml"));
            Parent root = loader.load();
            AuthController auth = loader.getController();
            authStage.setTitle("Autorization");
            authStage.setScene(new Scene(root, 400, 200));
            authStage.initModality(Modality.APPLICATION_MODAL);
        } catch (IOException e) {
            e.printStackTrace();
        }
        //initAuthLoop();
    }

    private void initAuthLoop() {
        Thread thread = new Thread(() -> {
            while (true) {
                Object received = Connection.receiveMessage();
                if (received instanceof AuthMessage) {
                    AuthMessage authMessage = (AuthMessage) received;
                    if (authMessage.getType().equals(MessageType.AUTH_FAIL)) {
                        new Alert(Alert.AlertType.INFORMATION, authMessage.getMessage()).show();
                    }
                    if (authMessage.getType().equals(MessageType.AUTH_OK)) {
                        new Alert(Alert.AlertType.INFORMATION, authMessage.getMessage()).show();
                        authScene.getScene().getWindow().hide();
                        isAuthorized = true;
                        break;
                    }
                }
                if (received instanceof Message) {

                }
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    public void signIn(ActionEvent actionEvent) {
        //TODO
        System.out.println(String.format("Login: %s, Pass: %s", login.getText(), password.getText()));
        Connection.sendAuthMessage(new AuthMessage(MessageType.SIGN_IN_REQUEST, login.getText(), password.getText()));
    }

    public void signUp(ActionEvent actionEvent) {
        //TODO
    }
}
