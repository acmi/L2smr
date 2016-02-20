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
package acmi.l2.clientmod.l2smr.dialogs;

import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

public class StaticMeshActorDialog extends AbstractDialog {
    private TextField actorClass = new TextField("Engine.StaticMeshActor");
    private CheckBox rotation = new CheckBox("Rotation");
    private CheckBox scale = new CheckBox("Scale");
    private CheckBox scale3d = new CheckBox("Scale3D");
    private CheckBox rotating = new CheckBox("Rotating");
    private CheckBox zoneRenderState = new CheckBox("ZoneRenderState");

    public StaticMeshActorDialog() {
        rotation.setSelected(true);

        setTitle("New StaticMeshActor properties");
        getDialogPane().setContent(new VBox(2, actorClass, rotation, scale, scale3d, rotating, zoneRenderState));
        getDialogPane().getButtonTypes().addAll(ButtonType.OK);
    }

    public String getActorClass() {
        return actorClass.getText();
    }

    public boolean isRotation() {
        return rotation.isSelected();
    }

    public boolean isScale() {
        return scale.isSelected();
    }

    public boolean isScale3d() {
        return scale3d.isSelected();
    }

    public boolean isRotating() {
        return rotating.isSelected();
    }

    public boolean isZoneRenderState() {
        return zoneRenderState.isSelected();
    }
}
