package ru.grigorev.client;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.scene.control.Alert;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.cell.TextFieldListCell;
import javafx.scene.image.Image;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.stage.Stage;
import ru.grigorev.common.Info;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.FileTime;
import java.text.SimpleDateFormat;

/**
 * @author Dmitriy Grigorev
 */
public class GUIhelper {
    private static AuthController authController;
    private static MainController mainController;

    public static void showFXThreadSafeAlert(String info, String header, String title) {
        if (Platform.isFxApplicationThread()) {
            showAlert(info, header, title);
        }
        Platform.runLater(() -> showAlert(info, header, title));
    }

    public static void showAlert(String info, String header, String title) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, info);
        alert.setHeaderText(header);
        alert.setTitle(title);

        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.getIcons().add(new Image("/icon.png"));

        alert.showAndWait();
    }

    public static void initContextMenu(ListView<String> clientListView) {
        clientListView.setCellFactory(TextFieldListCell.forListView());
        ContextMenu contextMenu = new ContextMenu();
        MenuItem aboutItem = new MenuItem("About");
        aboutItem.setOnAction(event -> {
            String selected = clientListView.getSelectionModel().getSelectedItem();
            long size = 0;
            FileTime lastModified = null;
            SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
            try {
                Path gottenFile = Paths.get(Info.CLIENT_FOLDER_NAME + selected);
                size = Files.size(gottenFile);
                lastModified = Files.getLastModifiedTime(gottenFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
            String context = String.format("Size: %,d bytes\nLast modified: %s", size,
                    sdf.format(lastModified.toMillis()));
            GUIhelper.showAlert(context, selected, "About");
        });
        contextMenu.getItems().addAll(aboutItem);
        clientListView.setContextMenu(contextMenu);
    }

    public static void initDragAndDropClientListView(ListView<String> listView, ObservableList<String> list) {
        listView.setOnDragOver(event -> {
            if (event.getGestureSource() != listView && event.getDragboard().hasFiles()) {
                event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
            }
            event.consume();
        });

        listView.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            boolean success = false;
            if (db.hasFiles()) {
                for (File file : db.getFiles()) {
                    try {
                        Files.copy(file.toPath(), Paths.get(Info.CLIENT_FOLDER_NAME + file.getName()),
                                StandardCopyOption.REPLACE_EXISTING);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                success = true;
                listView.setItems(list);
                mainController.refreshClientsFilesList();
            }
            event.setDropCompleted(success);
            event.consume();
        });
    }

    public static void runWatchServiceThread() {
        Task task = new Task() {
            @Override
            protected Object call() throws Exception {
                System.out.println("Initing WatchServiceThread");
                Path pathToLocalStorage = Paths.get(Info.CLIENT_FOLDER_NAME);
                WatchService watchService = null;
                try {
                    watchService = pathToLocalStorage.getFileSystem().newWatchService();
                    pathToLocalStorage.register(watchService, StandardWatchEventKinds.ENTRY_CREATE,
                            StandardWatchEventKinds.ENTRY_DELETE, StandardWatchEventKinds.ENTRY_MODIFY);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                while (true) {
                    WatchKey key = null;
                    try {
                        key = watchService.take();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    for (WatchEvent event : key.pollEvents()) {
                        try {
                            mainController.refreshClientsFilesList();
                        } catch (IllegalStateException e) {
                            e.printStackTrace();
                        }
                    }
                    key.reset();
                }
            }
        };
        Thread wsThread = new Thread(task);
        wsThread.setDaemon(true);
        wsThread.start();
    }

    public static void setAuthController(AuthController authController) {
        GUIhelper.authController = authController;
    }

    public static void setMainController(MainController mainController) {
        GUIhelper.mainController = mainController;
    }
}
