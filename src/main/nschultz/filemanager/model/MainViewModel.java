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
package main.nschultz.filemanager.model;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import main.nschultz.filemanager.FileManagerApp;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class MainViewModel {

    public MainViewModel() {
    }

    public List<Path> getFileList(Path path) throws IOException {
        return Files.list(path).collect(Collectors.toList());
    }

    public Path getPreviousFile(Path dir) {
        return dir.getParent().getParent() == null ? dir.getParent() : dir.getParent().getParent();
    }

    public ObservableList<FileModel> createObservableListForTableViews(List<Path> files) throws IOException {
        ObservableList<FileModel> list = FXCollections.observableArrayList();
        for (Path path : files) {
            list.add(new FileModel(
                    getFileName(path),
                    getFileType(path),
                    getFileSizeInBytes(path),
                    getFormattedTimeStamp(path),
                    getAbsolutePath(path)));
        }
        return list;
    }

    public void openFileWithAssociatedProgram(Path filePath) {
        FileManagerApp.fetchHostServices().showDocument(filePath.toString());
    }

    public boolean isFile(Path path) {
        return Files.isRegularFile(path);
    }

    public List<String> getAvailableDrives() {
        List<String> drives = new ArrayList<>();
        for (File drive : File.listRoots()) {
            drives.add(drive.getAbsolutePath());
        }
        return drives;
    }

    public void copyStringToClipBoard(String stringToCopy) {
        Clipboard clipboard = Clipboard.getSystemClipboard();
        ClipboardContent clipboardContent = new ClipboardContent();
        clipboardContent.putString(stringToCopy);
        clipboard.setContent(clipboardContent);
    }

    private String getFileName(Path path) {
        return path.getFileName().toString();
    }

    private String getFileType(Path path) {
        return isFile(path) ? "<FILE>" : "<DIR>";
    }

    private String getFileSizeInBytes(Path file) throws IOException {
        return isFile(file) ? Files.size(file) + " Bytes" : "";
    }

    private String getAbsolutePath(Path path) {
        return path.toAbsolutePath().toString();
    }

    private String getFormattedTimeStamp(Path path) {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date(new File(
                path.toAbsolutePath().toString()).lastModified()));
    }
}
