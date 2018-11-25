package ru.grigorev.client;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldListCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.stage.Stage;
import ru.grigorev.common.ConnectionSingleton;
import ru.grigorev.common.Info;
import ru.grigorev.common.message.Message;
import ru.grigorev.common.message.MessageType;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.FileTime;
import java.text.DecimalFormat;
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

    public static void initClientContextMenu(ListView<String> clientListView) {
        System.out.println("Initing client context menu");
        clientListView.setCellFactory(TextFieldListCell.forListView());
        ContextMenu contextMenu = new ContextMenu();
        MenuItem aboutItem = new MenuItem("About");
        aboutItem.setOnAction(event -> {
            String selected = clientListView.getSelectionModel().getSelectedItem();
            if (selected == null) {
                //TODO
                System.out.println("returning");
                return;
            }
            long size = 0;
            FileTime lastModified = null;
            try {
                Path gottenFile = Paths.get(Info.CLIENT_FOLDER_NAME + selected);
                size = Files.size(gottenFile);
                lastModified = Files.getLastModifiedTime(gottenFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
            String context = String.format("Size: %s\nLast modified: %s",
                    GUIhelper.getFormattedSize(size),
                    GUIhelper.getFormattedLastModified(lastModified));
            GUIhelper.showAlert(context, selected, "About");
        });
        contextMenu.getItems().addAll(aboutItem);
        clientListView.setContextMenu(contextMenu);
    }

    public static void initServerContextMenu(ListView<String> serverListView) {
        System.out.println("Initing server context menu");
        serverListView.setCellFactory(TextFieldListCell.forListView());
        ContextMenu contextMenu = new ContextMenu();
        MenuItem aboutItem = new MenuItem("About");
        aboutItem.setOnAction(event -> {
            String selected = serverListView.getSelectionModel().getSelectedItem();
            ConnectionSingleton.getInstance().sendMessage(new Message(MessageType.ABOUT_FILE, selected)); // тут отправка
        });
        contextMenu.getItems().addAll(aboutItem);
        serverListView.setContextMenu(contextMenu);
    }

    /**
     * Honestly, this solution copypasted from one of students... But it's really brilliant!
     */
    public static String getFormattedSize(long size) {
        if (size <= 0) return "0 B";
        String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return new DecimalFormat("#,##0.#").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
        /*return String.format("%,d", size);*/
    }

    public static String getFormattedLastModified(FileTime lastModified) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
        return sdf.format(lastModified.toMillis());
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

    public static void initListViewIcons(ListView<String> listView) {
        final Image FILE = new Image("/file.png");
        final Image FOLDER = new Image("/folder.png");

        listView.setCellFactory(param -> new ListCell<>() {
            ImageView imageView = new ImageView();

            @Override
            public void updateItem(String name, boolean empty) {
                super.updateItem(name, empty);
                if (empty) {
                    setText(null);
                    setGraphic(null);
                } else {
                    if (Files.isDirectory(Paths.get(Info.CLIENT_FOLDER_NAME + name))) {
                        imageView.setImage(FOLDER);
                    } else
                        imageView.setImage(FILE);
                    imageView.fitHeightProperty().set(17.0);
                    imageView.fitWidthProperty().set(14.0);
                    setText(name);
                    setGraphic(imageView);
                }
            }
        });
    }


    public static void setAuthController(AuthController authController) {
        GUIhelper.authController = authController;
    }

    public static void setMainController(MainController mainController) {
        GUIhelper.mainController = mainController;
    }
}
