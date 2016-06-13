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

import acmi.l2.clientmod.unreal.Environment;
import acmi.l2.clientmod.unreal.UnrealSerializerFactory;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

import java.io.File;

import static acmi.l2.clientmod.util.Util.find;
import static acmi.l2.clientmod.util.Util.nameFilter;

public class ControllerBase {
    private final ObjectProperty<File> l2Dir = new SimpleObjectProperty<>();
    private final ObjectProperty<File> mapsDir = new SimpleObjectProperty<>();
    private final ObjectProperty<File> staticMeshDir = new SimpleObjectProperty<>();
    private final ObjectProperty<File> systemDir = new SimpleObjectProperty<>();
    private final ObjectProperty<UnrealSerializerFactory> classLoader = new SimpleObjectProperty<>();

    public File getL2Dir() {
        return l2Dir.get();
    }

    public ObjectProperty<File> l2DirProperty() {
        return l2Dir;
    }

    public void setL2Dir(File l2Dir) {
        this.l2Dir.set(l2Dir);
    }

    public File getMapsDir() {
        return mapsDir.get();
    }

    public ReadOnlyObjectProperty<File> mapsDirProperty() {
        return mapsDir;
    }

    public File getStaticMeshDir() {
        return staticMeshDir.get();
    }

    public ReadOnlyObjectProperty<File> staticMeshDirProperty() {
        return staticMeshDir;
    }

    public File getSystemDir() {
        return systemDir.get();
    }

    public ReadOnlyObjectProperty<File> systemDirProperty() {
        return systemDir;
    }

    public UnrealSerializerFactory getClassLoader() {
        return classLoader.get();
    }

    public ReadOnlyObjectProperty<UnrealSerializerFactory> classLoaderProperty() {
        return classLoader;
    }

    public ControllerBase() {
        staticMeshDir.bind(Bindings.createObjectBinding(() -> find(getL2Dir(), File::isDirectory, nameFilter("staticmeshes")), l2DirProperty()));
        mapsDir.bind(Bindings.createObjectBinding(() -> find(getL2Dir(), File::isDirectory, nameFilter("maps")), l2DirProperty()));
        systemDir.bind(Bindings.createObjectBinding(() -> find(getL2Dir(), File::isDirectory, nameFilter("system")), l2DirProperty()));
        classLoader.bind(Bindings.createObjectBinding(() -> {
            File l2ini = find(getSystemDir(), nameFilter("l2.ini"), File::isFile);
            if (l2ini == null)
                return null;
            return new UnrealSerializerFactory(Environment.fromIni(l2ini));
        }, systemDirProperty()));
        classLoaderProperty().addListener((observable, oldValue, newValue) -> {
        });
    }
}
