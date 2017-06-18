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
package main.nschultz.filemanager.util;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.Tooltip;
import main.nschultz.filemanager.model.FileModel;
import main.nschultz.filemanager.model.MainViewModel;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

public class PopulateService extends Service {

    private TableView<FileModel> tableView;
    private ObservableList<FileModel> list;
    private Label pathField;
    private MainViewModel model;

    private volatile int addedFileCount = 0;

    public PopulateService(TableView<FileModel> tableView, ObservableList<FileModel> list, Label pathField) {
        this.tableView = tableView;
        this.list = list;
        this.pathField = pathField;

        model = new MainViewModel();
    }

    @Override
    protected Task createTask() {
        return new Task<Void>() {
            @Override
            protected synchronized Void call() throws Exception {
                addedFileCount = 0;
                tableView.setTooltip(null);

                Scene scene = tableView.getScene();
                // sometimes the scene is NULL
                // seems to only happen at the start of the application therefore it is likely that
                // the scene is not fully initialized at this point
                // Anyway for now just prevent NPE which would cause the service to crash
                if (scene != null) {
                    scene.setCursor(Cursor.WAIT);
                }

                tableView.getItems().add(0, new FileModel("...", "<DIR>", "", "",
                        model.getPreviousDirFromDir(Paths.get(pathField.getText())).toString()));

                for (FileModel fileModel : list) {
                    if (Files.isReadable(Paths.get(fileModel.getAbsolutePath()))) { // @TODO make this configurable
                        Platform.runLater(() -> tableView.getItems().add(fileModel));
                        addedFileCount++;
                    }
                    TimeUnit.MILLISECONDS.sleep(1);
                }
                return null;
            }
        };
    }

    @Override
    protected void succeeded() {
        tableView.getScene().setCursor(Cursor.DEFAULT);
        tableView.setTooltip(new Tooltip(addedFileCount + " accessible file(s)"));
    }
}
