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

import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextInputDialog;
import javafx.scene.effect.GaussianBlur;
import javafx.stage.Stage;

import java.util.Optional;

public final class AlertUtil {

    private AlertUtil() {
    }

    public static boolean displayExitConfirmationAlert(Stage stage) {
        Parent root = stage.getScene().getRoot();
        root.setEffect(new GaussianBlur(10));
        ButtonType yesButton = new ButtonType("Yes", ButtonBar.ButtonData.YES);
        ButtonType noButton = new ButtonType("No", ButtonBar.ButtonData.NO);
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.getButtonTypes().clear();
        alert.getButtonTypes().addAll(yesButton, noButton);
        alert.setTitle("Exit confirmation");
        alert.setHeaderText("");
        alert.setContentText("Are you sure you wan't to exit?");
        alert.initOwner(stage);
        Optional<ButtonType> result = alert.showAndWait();
        root.setEffect(null);

        return result.isPresent() && result.get().equals(yesButton);
    }

    public static String displaySearchDialog(Stage stage) {
        TextInputDialog searchDialog = new TextInputDialog();
        searchDialog.initOwner(stage);
        searchDialog.setTitle("Search");
        searchDialog.setHeaderText("");
        searchDialog.setContentText("Search for file...");
        ButtonType search = new ButtonType("Search", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancel = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        searchDialog.getDialogPane().getButtonTypes().clear();
        searchDialog.getDialogPane().getButtonTypes().addAll(search, cancel);
        Optional<String> input = searchDialog.showAndWait();

        return input.orElse("");
    }

}
