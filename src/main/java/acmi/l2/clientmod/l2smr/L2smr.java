/*
 * Copyright (c) 2016 acmi
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package acmi.l2.clientmod.l2smr;

import javafx.application.Application;
import javafx.beans.InvalidationListener;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.prefs.Preferences;

public class L2smr extends Application {
    private final ObjectProperty<File> l2Dir = new SimpleObjectProperty<>();

    @Override
    public void start(Stage stage) throws Exception {
        loadConfig();

        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(L2smr.class.getResource("l2smr.fxml"));

        String version = readAppVersion();

        Parent root = loader.load();

        stage.setScene(new Scene(root));
        stage.titleProperty().bind(Bindings.createStringBinding(() ->
                (l2Dir.get() != null ? l2Dir.get().toString() + " - " : "") + "L2smr " + version, l2Dir));

        Controller controller = loader.getController();
        controller.l2DirProperty().bindBidirectional(l2Dir);
        controller.setStage(stage);

        stage.show();

        stage.setX(Double.parseDouble(L2smr.getPrefs().get("l2smr.x", String.valueOf(stage.getX()))));
        stage.setY(Double.parseDouble(L2smr.getPrefs().get("l2smr.y", String.valueOf(stage.getY()))));
        stage.setWidth(Double.parseDouble(L2smr.getPrefs().get("l2smr.width", String.valueOf(stage.getWidth()))));
        stage.setHeight(Double.parseDouble(L2smr.getPrefs().get("l2smr.height", String.valueOf(stage.getHeight()))));

        InvalidationListener listener = observable -> {
            L2smr.getPrefs().put("l2smr.x", String.valueOf(Math.round(stage.getX())));
            L2smr.getPrefs().put("l2smr.y", String.valueOf(Math.round(stage.getY())));
            L2smr.getPrefs().put("l2smr.width", String.valueOf(Math.round(stage.getWidth())));
            L2smr.getPrefs().put("l2smr.height", String.valueOf(Math.round(stage.getHeight())));
        };
        stage.xProperty().addListener(listener);
        stage.yProperty().addListener(listener);
        stage.widthProperty().addListener(listener);
        stage.heightProperty().addListener(listener);
    }

    private String readAppVersion() throws IOException, URISyntaxException {
        try (JarFile jarFile = new JarFile(Paths.get(getClass().getProtectionDomain().getCodeSource().getLocation().toURI()).toFile())) {
            Manifest manifest = jarFile.getManifest();
            return manifest.getMainAttributes().getValue("Version");
        } catch (FileNotFoundException ignore) {
        } catch (IOException | URISyntaxException e) {
            System.err.println("version info load error");
            e.printStackTrace(System.err);
        }
        return "";
    }

    private void loadConfig() {
        try {
            l2Dir.set(new File(getPrefs().get("path.l2", null)));
        } catch (Exception ignore) {
        }

        l2Dir.addListener(observable -> {
            try {
                getPrefs().put("path.l2", l2Dir.get().getPath());
            } catch (Exception ignore) {
            }
        });
    }

    static Preferences getPrefs() {
        return Preferences.userRoot().node("l2smr");
    }

    public static void main(String[] args) {
        launch(args);
    }
}
