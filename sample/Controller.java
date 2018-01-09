package sample;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Semaphore;

public class Controller {

    private Stage mainStage;
    private AnchorPane root = new AnchorPane();
    private TextField volumeNameLabel = new TextField("Volume name");
    private Button addFileButton = new Button("Add file");
    private Button writeDiskButton = new Button("Write");
    private Button deleteButton = new Button("Delete");
    ProgressIndicator progressIndicator = new ProgressIndicator(0.0);

    private TableView<FileInfo> table = new TableView<FileInfo>();
    private final ObservableList<FileInfo> files =
            FXCollections.observableArrayList(

            );

    Controller(Stage mainStage) {
        this.mainStage = mainStage;
    }

    public Parent getRoot() {
        AnchorPane.setLeftAnchor(volumeNameLabel, 0.0);
        AnchorPane.setRightAnchor(volumeNameLabel, 0.0);
        AnchorPane.setTopAnchor(volumeNameLabel, 0.0);

        AnchorPane.setTopAnchor(table, 30.0);
        AnchorPane.setLeftAnchor(table, 0.0);
        AnchorPane.setRightAnchor(table, 0.0);
        AnchorPane.setBottomAnchor(table, 50.0);
        prepareTable();
        table.setItems(files);
        HBox bottomPanel = new HBox();
        bottomPanel.setSpacing(10.0);
        bottomPanel.setAlignment(Pos.CENTER);
        AnchorPane.setLeftAnchor(bottomPanel, 20.0);
        AnchorPane.setBottomAnchor(bottomPanel, 0.0);
        addFileButton.setOnAction(e -> addFileButtonAction());
        writeDiskButton.setOnAction(e -> writeDiskButtonAction());
        deleteButton.setOnAction(e -> deleteButtonAction());
        bottomPanel.getChildren().addAll(addFileButton, writeDiskButton, progressIndicator, deleteButton);
        root.getChildren().add(volumeNameLabel);
        root.getChildren().add(table);
        root.getChildren().add(bottomPanel);
        return root;
    }

    private void prepareTable() {
        TableColumn nameCol = new TableColumn("Name");
        nameCol.prefWidthProperty().bind(table.widthProperty().divide(4));
        nameCol.setCellValueFactory(
                new PropertyValueFactory<FileInfo, String>("name"));

        TableColumn sizeCol = new TableColumn("Size");
        sizeCol.prefWidthProperty().bind(table.widthProperty().divide(4));
        sizeCol.setCellValueFactory(
                new PropertyValueFactory<FileInfo, String>("sizeString"));

        TableColumn pathCol = new TableColumn("Path");
        pathCol.prefWidthProperty().bind(table.widthProperty().divide(2));
        pathCol.setCellValueFactory(
                new PropertyValueFactory<FileInfo, String>("path"));

        table.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        table.setItems(files);
        table.getColumns().addAll(nameCol, sizeCol, pathCol);
    }

    private void addFileButtonAction() {
        final FileChooser fileChooser = new FileChooser();
        List<File> list =
                fileChooser.showOpenMultipleDialog(mainStage);
        if (list != null) {
            for (File file : list) {
                files.add(new FileInfo(file.getName(), file.length(), file.getAbsolutePath()));
            }
        }
    }

    private long getImageSize() {
        long total = 0;
        for (FileInfo file :
                files) {
            total += file.getSize();
        }
        return total;
    }

    private void writeDiskButtonAction() {
        String line = "0";
        try {
            line = SudoExecutor.exec("getDiskSize", "cdrwtool -i -d /dev/sr1 | awk '$1 == \"track_size\" {print $3 * 2048}'");
            long imageSize = getImageSize();
            System.out.println("Disk free space " + SizeToString.convert(new Long(line)));
            System.out.println("Image size " + SizeToString.convert(imageSize));
            if (imageSize > new Long(line)) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText(null);
                alert.setContentText("Free disk space is not enough!");
                alert.showAndWait();
            } else {
                System.out.println(imageSize / (2770 * 1024));
                Thread myThready = new Thread(new Runnable() {
                    public void run() {
                        try {
                            DiskWriter.makeISO(volumeNameLabel.getText(), files);
                            SudoExecutor.exec("umount", "umount /dev/sr1");
                            DiskWriteControl dwc = new DiskWriteControl((imageSize / (2770 * 1024))+8);
                            progressIndicator.progressProperty().bind(dwc.getProgress());
                            dwc.exec("write", "wodim -eject -tao  speed=2 dev=/dev/sr1 -v -data diskImage.iso");

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
                myThready.start();
            }
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText("Disk not found");
            alert.showAndWait();
            e.printStackTrace();
        }
    }

    private void deleteButtonAction(){
        table.getItems().removeAll(table.getSelectionModel().getSelectedItems());
    }
}
