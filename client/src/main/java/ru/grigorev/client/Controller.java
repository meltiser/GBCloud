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
import ru.grigorev.client.logic.Connection;
import ru.grigorev.common.Message;
import ru.grigorev.common.MessageType;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.*;
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
        //clientListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        //serverListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        initializeDragAndDropClientListView();
        //openAuth();
        initClientMainLoop();
        refreshServerFilesList();
    }

    private void initClientMainLoop() {
        Connection.init();
        Thread t = new Thread(() -> {
            try {
                while (true) {
                    Message message = Connection.receiveMessage();
                    if (message.getType().equals(MessageType.FILE)) {
                        Files.write(Paths.get("client/client_storage/" + message.getFileName()), message.getByteArr(), StandardOpenOption.CREATE);
                        refreshClientsFilesList();
                    }
                    if (message.getType().equals(MessageType.REFRESH_RESPONSE)) {
                        if (Platform.isFxApplicationThread()) {
                            serverList.clear();
                            serverList.addAll(message.getListFileNames());
                            serverListView.setItems(serverList);
                        } else {
                            Platform.runLater(() -> {
                                serverList.clear();
                                serverList.addAll(message.getListFileNames());
                                serverListView.setItems(serverList);
                            });
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                Connection.close();
            }
        });
        t.setDaemon(true);
        t.start();
        clientListView.setItems(FXCollections.observableArrayList());
        refreshClientsFilesList();
    }

    public void downloadFile(ActionEvent actionEvent) {
        String selectedItem = serverListView.getSelectionModel().getSelectedItem();
        if (selectedItem == null) {
            return;
        }
        serverListView.getSelectionModel().clearSelection();
        Connection.sendMessage(new Message(MessageType.FILE_REQUEST, selectedItem));

        /*ObservableList<String> selectedItems = serverListView.getSelectionModel().getSelectedItems();
        if (selectedItems == null) {
            return;
        }
        serverListView.getSelectionModel().clearSelection();
        if (selectedItems.isEmpty()) System.out.println("empty!");
        for (String selectedItem : selectedItems) {
            System.out.println(selectedItem); //!!!
            Connection.sendMessage(new Message(MessageType.FILE_REQUEST, selectedItem));
        }*/
    }

    public void refreshClientsFilesList() {
        if (Platform.isFxApplicationThread()) {
            try {
                clientListView.getItems().clear();
                Files.list(Paths.get("client/client_storage"))
                        .map(p -> p.getFileName().toString())
                        .forEach(o -> clientListView.getItems().add(o));
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            Platform.runLater(() -> {
                try {
                    clientListView.getItems().clear();
                    Files.list(Paths.get("client/client_storage"))
                            .map(p -> p.getFileName().toString())
                            .forEach(o -> clientListView.getItems().add(o));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }
    }

    public void refreshServerFilesList() {
        Connection.sendMessage(new Message(MessageType.REFRESH_REQUEST));
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
            refreshClientsFilesList();
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
            try {
                Files.copy(file.toPath(), Paths.get("client/client_storage/" + file.getName()), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                e.printStackTrace();
            }
            refreshClientsFilesList();
        }
    }

    public void deleteFile(ActionEvent actionEvent) {
        Button pressedButton = (Button) actionEvent.getSource();
        if (pressedButton.getId().equals(clientDeleteButton.getId())) {
            String selectedItem = clientListView.getSelectionModel().getSelectedItem();
            if (selectedItem == null) {
                return;
            }
            clientListView.getSelectionModel().clearSelection();
            try {
                Files.delete(Paths.get("client/client_storage/" + selectedItem));
            } catch (IOException e) {
                e.printStackTrace();
            }
            refreshClientsFilesList();
        } else {
            //deleteSelectedInListView(serverListView, serverList);
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
                    try {
                        Files.copy(file.toPath(), Paths.get("client/client_storage/" + file.getName()), StandardCopyOption.REPLACE_EXISTING);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    //clientList.add(file.getName() + " ");
                }
                success = true;
                clientListView.setItems(clientList);
            }
            event.setDropCompleted(success);
            event.consume();
            refreshClientsFilesList();
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
