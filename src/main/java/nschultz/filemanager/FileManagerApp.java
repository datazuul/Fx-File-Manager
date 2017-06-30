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
package nschultz.filemanager;

import java.io.IOException;
import javafx.animation.FadeTransition;
import javafx.application.Application;
import javafx.application.HostServices;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.util.Duration;
import nschultz.filemanager.util.AlertUtil;
import nschultz.filemanager.util.Version;

public class FileManagerApp extends Application {

  public static final String TITLE = "Fx File Manager " + Version.getVersion();

  private static HostServices hostServices;

  @Override
  public void start(Stage primaryStage) throws Exception {
    hostServices = getHostServices();
    setupStage(primaryStage).show();
  }

  private Stage setupStage(Stage primaryStage) throws IOException {
    primaryStage.setTitle(TITLE);

    Scene scene = new Scene(FXMLLoader.load(getClass().getResource("fxml/MainView.fxml")));
    setUserAgentStylesheet(STYLESHEET_CASPIAN);
    scene.getStylesheets().add(getClass().getResource("css/default.css").toExternalForm());
    primaryStage.setScene(scene);

    addCloseListener(primaryStage);
    fadeIn(primaryStage);

    return primaryStage;
  }

  private void addCloseListener(Stage primaryStage) {
    primaryStage.setOnCloseRequest(event -> {
      if (AlertUtil.displayExitConfirmationAlert(primaryStage)) {
        shutdown();
      } else {
        event.consume();
      }
    });
  }

  private void fadeIn(Stage primaryStage) {
    FadeTransition fadeTransition = new FadeTransition(Duration.seconds(2), primaryStage.getScene().getRoot());
    fadeTransition.setFromValue(0);
    fadeTransition.setToValue(1);
    fadeTransition.play();
  }

  public static HostServices fetchHostServices() {
    return hostServices;
  }

  public static void shutdown() {
    System.gc();
    Platform.exit();
  }

  public static void main(String[] args) {
    launch(args);
  }
}
