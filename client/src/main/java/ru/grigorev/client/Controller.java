package ru.grigorev.client;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.MenuItem;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class Controller implements Initializable {
    public Button clientDeleteButton;
    public Button clientSendButton;
    public Button clientRefreshButton;
    public Button serverRefreshButton;
    public Button serverDeleteButton;
    public ListView<String> serverListView;
    public ListView<String> clientListView;
    public MenuItem menuOpen;

    private Stage primaryStage;
    private ObservableList<String> clientList = FXCollections.observableList(new ArrayList<>());
    private ObservableList<String> serverList = FXCollections.observableList(new ArrayList<>());

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        clientListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        serverListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        initializeDragAndDropClientListView();
        openAuth();
    }

    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    public void sendFile(ActionEvent actionEvent) {
        ObservableList<String> selectedItems = clientListView.getSelectionModel().getSelectedItems();
        if (selectedItems == null) return;
        serverList.addAll(selectedItems);
        clientList.removeAll(selectedItems);
        clientListView.setItems(clientList);
        serverListView.setItems(serverList);
        clientListView.getSelectionModel().clearSelection();
    }

    public void showHelp(ActionEvent actionEvent) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, "cool huh?", ButtonType.NO);
        alert.show();
    }

    public void refreshList(ActionEvent actionEvent) {
        //TODO:
        Button pressedButton = (Button) actionEvent.getSource();
        if (pressedButton.getId().equals(clientRefreshButton.getId())) {
            //clientListView.refresh();
            clientList.clear();
        } else {
            //serverListView.refresh();
            serverList.clear();
        }
    }

    public void openFile(ActionEvent actionEvent) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open File");
        File file = fileChooser.showOpenDialog(primaryStage);
        if (file != null) {
            clientList.add(file.getName());
            clientListView.setItems(clientList);
        }
    }

    public void addSmth(ActionEvent actionEvent) {
        double random = Math.random();
        clientList.add(String.valueOf(random));
        clientListView.setItems(clientList);
    }

    public void deleteFile(ActionEvent actionEvent) {
        Button pressedButton = (Button) actionEvent.getSource();
        if (pressedButton.getId().equals(clientDeleteButton.getId())) {
            deleteSelectedInListView(clientListView, clientList);
        } else {
            deleteSelectedInListView(serverListView, serverList);
        }
    }

    private void deleteSelectedInListView(ListView<String> listView, ObservableList<String> list) {
        ObservableList<String> selectedItems = listView.getSelectionModel().getSelectedItems();
        if (selectedItems == null) return;
        list.removeAll(selectedItems);
        listView.setItems(list);
        listView.getSelectionModel().clearSelection();
    }

    public void clearSelection() { //??????????????
        /*if (clientListView.getSelectionModel().getSelectedItem() != null)
            clientListView.getSelectionModel().clearSelection();*/
    }


    public void exit(ActionEvent actionEvent) {
        Platform.exit();
    }

    public void openAuth() {
        try {
            Stage stage = new Stage();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Authorization.fxml"));
            Parent root = loader.load();
            Authorization auth = loader.getController();
            //openAuth.id = 100;
            stage.setTitle("Autorization");
            stage.setScene(new Scene(root, 400, 200));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void initializeDragAndDropClientListView() {
        clientListView.setOnDragOver(event -> {
            if (event.getGestureSource() != clientListView && event.getDragboard().hasFiles()) {
                event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
            }
            event.consume();
        });

        clientListView.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            boolean success = false;
            if (db.hasFiles()) {
                //clientListView.setText("");
                for (File file : db.getFiles()) {
                    clientList.add(file.getName() + " ");
                }
                success = true;
                clientListView.setItems(clientList);
            }
            event.setDropCompleted(success);
            event.consume();
        });
    }

    public void openLink(ActionEvent actionEvent) {
        Desktop desktop = Desktop.getDesktop();
        try {
            desktop.browse(new URI("https://github.com/meltiser/GBCloud"));
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
        }
    }
}
