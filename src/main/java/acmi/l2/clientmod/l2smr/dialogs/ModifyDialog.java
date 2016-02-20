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

import acmi.l2.clientmod.l2smr.model.Actor;
import javafx.collections.FXCollections;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;

import java.util.function.Consumer;
import java.util.function.Function;

public class ModifyDialog extends AbstractDialog {
    private ComboBox<Transform> type = new ComboBox<>(FXCollections.observableArrayList(
            new Transform("Set X", s -> actor -> actor.setX(Float.parseFloat(s))),
            new Transform("Set Y", s -> actor -> actor.setY(Float.parseFloat(s))),
            new Transform("Set Z", s -> actor -> actor.setZ(Float.parseFloat(s))),
            new Transform("Translate X", s -> actor -> actor.setX(actor.getX() + Float.parseFloat(s))),
            new Transform("Translate Y", s -> actor -> actor.setY(actor.getY() + Float.parseFloat(s))),
            new Transform("Translate Z", s -> actor -> actor.setZ(actor.getZ() + Float.parseFloat(s)))
    ));
    private TextField value = new TextField("0");

    public ModifyDialog() {
        type.getSelectionModel().selectFirst();

        HBox pane = new HBox(type, value);
        pane.setSpacing(2);

        setTitle("Modify actors");
        getDialogPane().setContent(pane);
        getDialogPane().getButtonTypes().addAll(ButtonType.OK);
    }

    public Consumer<Actor> getTransform() {
        return type.getSelectionModel().getSelectedItem().transformFactory.apply(value.getText());
    }

    private static class Transform {
        String name;
        Function<String, Consumer<Actor>> transformFactory;

        public Transform(String name, Function<String, Consumer<Actor>> transformFactory) {
            this.name = name;
            this.transformFactory = transformFactory;
        }

        @Override
        public String toString() {
            return name;
        }
    }
}
