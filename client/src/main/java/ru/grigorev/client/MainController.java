package ru.grigorev.client;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.MenuItem;
import javafx.scene.control.cell.TextFieldListCell;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import ru.grigorev.common.AuthMessage;
import ru.grigorev.common.Message;
import ru.grigorev.common.MessageType;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.*;
import java.nio.file.attribute.FileTime;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class MainController implements Initializable {
    public ListView<String> serverListView;
    public ListView<String> clientListView;

    private AuthController authController;
    private Stage primaryStage;
    private ObservableList<String> clientList = FXCollections.observableList(new ArrayList<>());
    private ObservableList<String> serverList = FXCollections.observableList(new ArrayList<>());

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initializeDragAndDropClientListView();
        initContextMenu();
        initClientMainLoop();
        authController = new AuthController();
        authController.setPrimaryStage(primaryStage);
        authController.init();
        //openAuth();
        refreshAll();
        doubleClickRename();
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
                for (File file : db.getFiles()) {
                    try {
                        Files.copy(file.toPath(), Paths.get("client/client_storage/" + file.getName()), StandardCopyOption.REPLACE_EXISTING);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                success = true;
                clientListView.setItems(clientList);
                refreshClientsFilesList();
            }
            event.setDropCompleted(success);
            event.consume();
        });
    }

    private void initContextMenu() {
        clientListView.setCellFactory(lv -> {
            ListCell<String> cell = new ListCell<>();
            ContextMenu contextMenu = new ContextMenu();
            MenuItem editItem = new MenuItem();
            editItem.textProperty().bind(Bindings.format("Rename \"%s\"", cell.itemProperty()));
            editItem.setOnAction(event -> {
                //TODO:
                clientListView.setEditable(true);
                /*cell.startEdit(); // doesnt work!
                clientListView.layout();
                clientListView.edit(clientListView.getSelectionModel().getSelectedIndex()); // doesnt work too!
                clientListView.setEditable(false);*/
            });
            MenuItem deleteItem = new MenuItem();
            deleteItem.textProperty().bind(Bindings.format("About", cell.itemProperty()));
            deleteItem.setOnAction(event -> {
                long size = 0;
                FileTime lastModified = null;
                SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
                try {
                    Path gottenFile = (Paths.get("client/client_storage/" + cell.getItem()));
                    size = Files.size(gottenFile);
                    lastModified = Files.getLastModifiedTime(gottenFile);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                String context = String.format("Size: %,d bytes\nLast modified: %s", size, sdf.format(lastModified.toMillis()));
                Alert alert = new Alert(Alert.AlertType.INFORMATION, context, ButtonType.OK);
                alert.setTitle("About");
                alert.setHeaderText(cell.getItem());
                alert.show();
            });
            contextMenu.getItems().addAll(editItem, deleteItem);

            cell.textProperty().bind(cell.itemProperty());
            cell.emptyProperty().addListener((obs, wasEmpty, isNowEmpty) -> {
                if (isNowEmpty) {
                    cell.setContextMenu(null);
                } else {
                    cell.setContextMenu(contextMenu);
                }
            });
            return cell;
        });
    }

    private void doubleClickRename() {
        clientListView.setOnMouseClicked(click -> {
            if (click.getClickCount() == 2) {
                clientListView.setEditable(true);
                clientListView.setCellFactory(TextFieldListCell.forListView());

                clientListView.setOnEditCommit(new EventHandler<ListView.EditEvent<String>>() {
                    @Override
                    public void handle(ListView.EditEvent<String> t) {
                        System.out.println(clientListView.getSelectionModel().getSelectedItem());
                        clientListView.getItems().set(t.getIndex(), t.getNewValue());
                        System.out.println(t.getNewValue());
                    }
                });

                clientListView.setOnEditCancel(new EventHandler<ListView.EditEvent<String>>() {
                    @Override
                    public void handle(ListView.EditEvent<String> t) {
                        System.out.println("setOnEditCancel");
                    }
                });
            }
        });
    }

    private void initClientMainLoop() {
        Connection.init();
        Thread thread = new Thread(() -> {
            try {
                while (true) {
                    Object received = Connection.receiveMessage();
                    if (received instanceof AuthMessage) {
                        openAuth();
                    }
                    if (received instanceof Message) {
                        Message message = (Message) received;
                        if (message.getType().equals(MessageType.FILE)) {
                            checkFileExisting(message);
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
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                Connection.close();
            }
        });
        thread.setDaemon(true);
        thread.start();
        clientListView.setItems(FXCollections.observableArrayList());
        refreshClientsFilesList();
    }

    public void openAuth() {
        authController.getAuthStage().showAndWait();
    }

    public void refreshServerFilesList() {
        Connection.sendMessage(new Message(MessageType.REFRESH_REQUEST));
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

    public void refreshAll() {
        refreshClientsFilesList();
        refreshServerFilesList();
    }

    private void checkFileExisting(Message message) {
        if (Files.exists(Paths.get("client/client_storage/" + message.getFileName()))) {
            if (Platform.isFxApplicationThread()) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION, "File is already exists!", ButtonType.OK);
                alert.show();
            } else {
                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION, "File is already exists!", ButtonType.OK);
                    alert.show();
                });
            }
        }
    }

    public void downloadFile(ActionEvent actionEvent) {
        String selectedItem = getSelectedAndClearSelection(serverListView);
        if (selectedItem == null) return;
        Connection.sendMessage(new Message(MessageType.FILE_REQUEST, selectedItem));
    }

    private String getSelectedAndClearSelection(ListView<String> listView) {
        String selectedItem = listView.getSelectionModel().getSelectedItem();
        listView.getSelectionModel().clearSelection();
        return selectedItem;
    }

    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    public void sendFile(ActionEvent actionEvent) {
        String selectedItem = getSelectedAndClearSelection(clientListView);
        if (selectedItem == null) {
            return;
        }
        if (Files.exists(Paths.get("client/client_storage/" + selectedItem))) {
            try {
                Message fileMessage = new Message(MessageType.FILE, Paths.get("client/client_storage/" + selectedItem));
                Connection.sendMessage(fileMessage);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        refreshServerFilesList();
    }

    public void showHelp(ActionEvent actionEvent) {
        //TODO
    }

    public void openFile(ActionEvent actionEvent) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open File");
        List<File> list = fileChooser.showOpenMultipleDialog(primaryStage);
        if (list != null) {
            for (File file : list) {
                try {
                    Files.copy(file.toPath(), Paths.get("client/client_storage/" + file.getName()), StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            refreshClientsFilesList();
        }
    }

    public void deleteFileClient(ActionEvent actionEvent) {
        String selectedItem = getSelectedAndClearSelection(clientListView);
        if (selectedItem == null) return;
        try {
            Files.delete(Paths.get("client/client_storage/" + selectedItem));
        } catch (IOException e) {
            e.printStackTrace();
        }
        refreshClientsFilesList();
    }

    public void exit(ActionEvent actionEvent) {
        Platform.exit();
    }

    public void openLink(ActionEvent actionEvent) {
        Desktop desktop = Desktop.getDesktop();
        try {
            desktop.browse(new URI("https://github.com/meltiser/GBCloud"));
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
        }
    }

    public void deleteFileServer(ActionEvent actionEvent) {
        String selectedItem = getSelectedAndClearSelection(serverListView);
        if (selectedItem == null) return;
        Connection.sendMessage(new Message(MessageType.DELETE_FILE, selectedItem));
        refreshServerFilesList();
    }
}
