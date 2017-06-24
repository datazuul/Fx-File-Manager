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
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.Tooltip;
import main.nschultz.filemanager.model.FileModel;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;

public class PopulateService extends Service<ObservableList<FileModel>> {

    private TableView<FileModel> tableView;
    private Label pathField;
    private ObservableList<FileModel> resultList;

    public PopulateService(TableView<FileModel> tableView, Label pathField) {
        this.tableView = tableView;
        this.pathField = pathField;
    }

    public final void setTableView(TableView<FileModel> tableView) {
        this.tableView = tableView;
    }

    public final void setPathField(Label pathField) {
        this.pathField = pathField;
    }

    @Override
    protected Task<ObservableList<FileModel>> createTask() {
        return new Task<ObservableList<FileModel>>() {
            @Override
            protected synchronized ObservableList<FileModel> call() throws Exception {
                Path dirPath = Paths.get(pathField.getText());

                Platform.runLater(() -> {
                    tableView.setTooltip(null);
                    tableView.setCursor(Cursor.WAIT);
                    tableView.getItems().clear();
                    tableView.getItems().add(0, new FileModel("..", "<DIR>", "", "",
                            getPreviousDirFromDir(dirPath).toString()));
                });

                final long DIR_SIZE = Files.list(dirPath).count();
                resultList = FXCollections.observableArrayList();

                try (DirectoryStream<Path> dirStream = Files.newDirectoryStream(dirPath)) {
                    for (Path entry : dirStream) {
                        if (isCancelled()) {
                            resultList.clear();
                            return resultList;
                        }
                        if (Files.isReadable(entry)) {
                            resultList.add(new FileModel(
                                    getFileName(entry),
                                    getFileType(entry),
                                    getFileSizeInBytes(entry),
                                    getFormattedTimeStamp(entry),
                                    getAbsolutePath(entry))
                            );

                            // we subtract one because it is not added to the tableview yet
                            updateProgress(resultList.size() == 0 ? 1 : resultList.size() - 1, DIR_SIZE);
                        }
                    }
                    Platform.runLater(() -> tableView.getItems().addAll(resultList));
                    // now everything is done
                    updateProgress(resultList.size(), DIR_SIZE);
                }
                return resultList;
            }
        };
    }

    @Override
    protected void succeeded() {
        Platform.runLater(() -> {
            tableView.setCursor(Cursor.DEFAULT);
            tableView.setTooltip(new Tooltip(resultList.size() + " accessible file(s)"));
        });
        reset();
    }

    @Override
    protected void cancelled() {
        Platform.runLater(() -> {
            tableView.setCursor(Cursor.DEFAULT);
            tableView.setTooltip(new Tooltip(null));
        });
        reset();
    }

    @Override
    protected void failed() {
        getException().printStackTrace();
        Platform.runLater(() -> {
            tableView.setCursor(Cursor.DEFAULT);
            tableView.setTooltip(new Tooltip(null));
        });
        reset();
    }

    private String getFileName(Path path) {
        return path.getFileName().toString();
    }

    private String getFileType(Path path) {
        return Files.isRegularFile(path) ? "<FILE>" : "<DIR>";
    }

    private String getFileSizeInBytes(Path file) throws IOException {
        return Files.isRegularFile(file) ? Files.size(file) + " Bytes" : "";
    }

    private String getAbsolutePath(Path path) {
        return path.toAbsolutePath().toString();
    }

    private String getFormattedTimeStamp(Path path) {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date(new File(
                path.toAbsolutePath().toString()).lastModified()));
    }

    private Path getPreviousDirFromDir(Path dir) {
        return dir.getParent() == null ? dir : dir.getParent();
    }
}
