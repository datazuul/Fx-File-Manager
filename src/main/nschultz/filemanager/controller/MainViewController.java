/*
Copyright (c) 2017 Niklas Schultz
Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit
persons to whom the Software is furnished to do so, subject to the following conditions:
The above copyright notice and this permission notice shall be included in all copies or substantial portions of the
Software. THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED
TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN
ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
DEALINGS IN THE SOFTWARE.
 */
package main.nschultz.filemanager.controller;

import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.stage.Modality;
import javafx.stage.Stage;
import main.nschultz.filemanager.FileManagerApp;
import main.nschultz.filemanager.model.FileModel;
import main.nschultz.filemanager.model.MainViewModel;
import main.nschultz.filemanager.util.AlertUtil;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MainViewController implements Initializable {

    @FXML
    private MenuBar menuBar;

    @FXML
    private ComboBox<String> driveComboBoxLeft;

    @FXML
    private ComboBox<String> driveComboBoxRight;

    @FXML
    private Label pathFieldLeft;

    @FXML
    private Label pathFieldRight;

    @FXML
    private TableView<FileModel> tableViewLeft;

    @FXML
    private TableView<FileModel> tableViewRight;

    private MainViewModel model;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        model = new MainViewModel();

        populateComboBox(driveComboBoxLeft);
        populateComboBox(driveComboBoxRight);

        Path startingPath = Paths.get(driveComboBoxLeft.getSelectionModel().getSelectedItem());
        pathFieldLeft.setText(startingPath.toAbsolutePath().toString());
        pathFieldRight.setText(startingPath.toAbsolutePath().toString());

        ensureColumnsFillTableViewWidth(tableViewLeft);
        ensureColumnsFillTableViewWidth(tableViewRight);
        enableMultipleSelectionInTableViews();

        try {
            ObservableList<FileModel> list = model.createObservableListForTableViews(model.getFileList(startingPath));
            populateTableView(tableViewLeft, list);
            populateTableView(tableViewRight, list);
        } catch (IOException ex) {
            Logger.getLogger("population").log(Level.SEVERE, ex.toString());
        }

        addInputListenersToTableView();
    }

    private void populateComboBox(ComboBox<String> comboBox) {
        comboBox.getItems().addAll(model.getAvailableDrives());
        comboBox.getSelectionModel().selectFirst();
    }

    private void enableMultipleSelectionInTableViews() {
        tableViewLeft.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        tableViewRight.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    }

    private void addInputListenersToTableView() {
        addMouseListenerToTableView(tableViewLeft, pathFieldLeft);
        addMouseListenerToTableView(tableViewRight, pathFieldRight);
        addKeyListenerToTableView(tableViewLeft, pathFieldLeft);
        addKeyListenerToTableView(tableViewRight, pathFieldRight);
    }

    private void ensureColumnsFillTableViewWidth(TableView<FileModel> tableView) {
        final int COLUMN_NUMBER = tableView.getColumns().size(); // divisor

        ObservableList<TableColumn<FileModel, ?>> tableViewColumns = tableView.getColumns();
        for (TableColumn<FileModel, ?> tableViewColumn : tableViewColumns) {
            tableViewColumn.prefWidthProperty().bind(tableView.widthProperty().divide(COLUMN_NUMBER));
        }
    }

    private void populateTableView(TableView<FileModel> tableView, ObservableList<FileModel> list) {
        final int NAME_COLUMN = 0;
        final int TYPE_COLUMN = 1;
        final int SIZE_COLUMN = 2;
        final int DATE_COLUMN = 3;

        tableView.getColumns().get(NAME_COLUMN).setCellValueFactory(new PropertyValueFactory<>("Name"));
        tableView.getColumns().get(TYPE_COLUMN).setCellValueFactory(new PropertyValueFactory<>("Type"));
        tableView.getColumns().get(SIZE_COLUMN).setCellValueFactory(new PropertyValueFactory<>("Size"));
        tableView.getColumns().get(DATE_COLUMN).setCellValueFactory(new PropertyValueFactory<>("Date"));

        tableView.getItems().clear();
        for (FileModel fileModel : list) {
            if (Files.isReadable(Paths.get(fileModel.getAbsolutePath()))) { // @TODO make this configurable
                tableView.getItems().add(fileModel);
            }
        }
    }

    private void addMouseListenerToTableView(TableView<FileModel> tableView, Label pathField) {
        tableView.setOnMousePressed(event -> {
            final int DOUBLE_CLICK = 2;
            if (event.getButton().equals(MouseButton.PRIMARY) && event.getClickCount() == DOUBLE_CLICK) {
                navigateForward(tableView, pathField);
            } else if (event.getButton().equals(MouseButton.SECONDARY)) {
                navigateBackwards(tableView, pathField);
            }
        });
    }

    private void addKeyListenerToTableView(TableView<FileModel> tableView, Label pathField) {
        tableView.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                navigateForward(tableView, pathField);
            }
            if (event.getCode() == KeyCode.BACK_SPACE) {
                navigateBackwards(tableView, pathField);
            }
        });
    }

    private FileModel getSelectedFileModel(TableView<FileModel> tableView) {
        return tableView.getSelectionModel().getSelectedItem();
    }

    private void navigateForward(TableView<FileModel> tableView, Label pathField) {
        FileModel fileModel = getSelectedFileModel(tableView);
        if (fileModel == null) {
            return;
        }

        Path filePath = Paths.get(fileModel.getAbsolutePath());

        if (model.isFile(filePath)) {
            model.openFileWithAssociatedProgram(filePath);
        } else {
            updateGuiAccordingToDirectoryChange(tableView, pathField, filePath);
        }
    }

    private void navigateBackwards(TableView<FileModel> tableView, Label pathField) {
        FileModel fileModel = getSelectedFileModel(tableView);
        if (fileModel == null) {
            return;
        }

        updateGuiAccordingToDirectoryChange(tableView, pathField,
                model.getPreviousFile(Paths.get(fileModel.getAbsolutePath())));
    }

    private void updateGuiAccordingToDirectoryChange(TableView<FileModel> tableView, Label pathField, Path file) {
        try {
            populateTableView(tableView, model.createObservableListForTableViews(model.getFileList(file)));
            pathField.setText(file.toString());
            tableView.getSelectionModel().select(0);
        } catch (IOException ex) {
            Logger.getLogger("navigation").log(Level.SEVERE, ex.toString());
            ex.printStackTrace(System.err);
        }
    }


    @FXML
    private void driveComboBoxLeft_onAction(ActionEvent event) {
        Path drive = Paths.get(driveComboBoxLeft.getSelectionModel().getSelectedItem());
        pathFieldLeft.setText(drive.toAbsolutePath().toString());
        updateGuiAccordingToDirectoryChange(tableViewLeft, pathFieldLeft, drive);
    }


    @FXML
    private void driveComboBoxRight_onAction(ActionEvent event) {
        Path drive = Paths.get(driveComboBoxRight.getSelectionModel().getSelectedItem());
        pathFieldRight.setText(drive.toAbsolutePath().toString());
        updateGuiAccordingToDirectoryChange(tableViewRight, pathFieldRight, drive);
    }

    @FXML
    private void gotoStartingDirItem_onAction(ActionEvent event) {
        if (tableViewLeft.focusedProperty().get()) {
            updateGuiAccordingToDirectoryChange(tableViewLeft, pathFieldLeft, Paths.get("").toAbsolutePath());
        } else {
            updateGuiAccordingToDirectoryChange(tableViewRight, pathFieldRight, Paths.get("").toAbsolutePath());
        }
    }

    @FXML
    private void exitItem_onAction(ActionEvent event) {
        if (AlertUtil.displayExitConfirmationAlert((Stage) menuBar.getScene().getWindow())) {
            FileManagerApp.shutdown();
        }
    }

    @FXML
    private void aboutItem_onAction(ActionEvent event) {
        URL url = getClass().getClassLoader().getResource("main/nschultz/filemanager/fxml/AboutDialog.fxml");
        if (url == null) {
            Logger.getLogger("AboutDialog").log(Level.SEVERE, "AboutDialog.fxml file could not be found.");
            return;
        }
        try {
            Stage mainView = (Stage) menuBar.getScene().getWindow();
            mainView.getScene().getRoot().setEffect(new GaussianBlur(10));

            Stage aboutDialog = new Stage();
            aboutDialog.setTitle("About " + FileManagerApp.TITLE);
            aboutDialog.setScene(new Scene(FXMLLoader.load(url)));
            aboutDialog.initOwner(mainView);
            aboutDialog.initModality(Modality.APPLICATION_MODAL);
            aboutDialog.showAndWait();

            mainView.getScene().getRoot().setEffect(null);
        } catch (IOException ex) {
            Logger.getLogger("AboutDialog").log(Level.SEVERE, ex.toString());
        }
    }

    @FXML
    private void contextCopyItemLeft_onAction(ActionEvent event) {
        model.copyStringToClipBoard(pathFieldLeft.getText());
    }

    @FXML
    private void contextCopyItemRight_onAction(ActionEvent event) {
        model.copyStringToClipBoard(pathFieldRight.getText());
    }
}
