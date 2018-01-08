package sample;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Controller {

    private Stage mainStage;
    private AnchorPane root = new AnchorPane();
    private Button addFileButton = new Button("Add file");
    private Button writeDiskButton = new Button("Write");

    private TableView<FileInfo> table = new TableView<FileInfo>();
    private final ObservableList<FileInfo> files =
            FXCollections.observableArrayList(

            );

    Controller(Stage mainStage){
        this.mainStage = mainStage;
    }

    public Parent getRoot(){
        AnchorPane.setLeftAnchor(table, 0.0);
        AnchorPane.setRightAnchor(table, 0.0);
        prepareTable();
        table.setItems(files);
        HBox bottomPanel = new HBox();
        AnchorPane.setBottomAnchor(bottomPanel, 0.0);
        addFileButton.setOnAction(e -> addFileButtonAction());
        writeDiskButton.setOnAction(e -> writeDiskButtonAction());
        bottomPanel.getChildren().addAll(addFileButton, writeDiskButton);
        root.getChildren().add(table);
        root.getChildren().add(bottomPanel);
        return root;
    }

    private void prepareTable(){
        TableColumn nameCol = new TableColumn("Name");
        nameCol.prefWidthProperty().bind(table.widthProperty().divide(4));
        nameCol.setCellValueFactory(
                new PropertyValueFactory<FileInfo, String>("name"));

        TableColumn sizeCol = new TableColumn("Size");
        sizeCol.prefWidthProperty().bind(table.widthProperty().divide(4));
        sizeCol.setCellValueFactory(
                new PropertyValueFactory<FileInfo, String>("size"));

        TableColumn pathCol = new TableColumn("Path");
        pathCol.prefWidthProperty().bind(table.widthProperty().divide(2));
        pathCol.setCellValueFactory(
                new PropertyValueFactory<FileInfo, String>("path"));

        table.setItems(files);
        table.getColumns().addAll(nameCol, sizeCol, pathCol);
    }

    private void addFileButtonAction(){
        final FileChooser fileChooser = new FileChooser();
        List<File> list =
                fileChooser.showOpenMultipleDialog(mainStage);
        if (list != null) {
            for (File file : list) {
                files.add(new FileInfo(file.getName(), SizeToString.convert(file.length()), file.getAbsolutePath()));
            }
        }
    }

    private void writeDiskButtonAction(){
        try {
            DiskWriter.makeISO(files);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
