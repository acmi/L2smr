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
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

public class ImportExportDialog extends AbstractDialog {
    private TextField x = new TextField();
    private TextField y = new TextField();
    private TextField z = new TextField();
    private TextField angle = new TextField();

    public ImportExportDialog(int mx, int my) {
        x.setText(String.valueOf((mx - 20) * 0x8000 + 0x4000));
        y.setText(String.valueOf((my - 18) * 0x8000 + 0x4000));
        z.setText(String.valueOf(0));
        angle.setText(String.valueOf(0));

        GridPane pane = new GridPane();
        pane.setHgap(2);
        pane.setVgap(2);
        pane.add(new Label("X:"), 0, 0);
        pane.add(x, 1, 0);
        pane.add(new Label("Y:"), 0, 1);
        pane.add(y, 1, 1);
        pane.add(new Label("Z:"), 0, 2);
        pane.add(z, 1, 2);
        pane.add(new Label("Angle:"), 0, 3);
        pane.add(angle, 1, 3);
        pane.add(new Label(" in degrees"), 2, 3);

        setTitle("Group properties");
        getDialogPane().setContent(pane);
        getDialogPane().getButtonTypes().addAll(ButtonType.OK);
    }

    public float getX() {
        try {
            return Float.parseFloat(x.getText());
        } catch (Exception e) {
            return 0;
        }
    }

    public float getY() {
        try {
            return Float.parseFloat(y.getText());
        } catch (Exception e) {
            return 0;
        }
    }

    public float getZ() {
        try {
            return Float.parseFloat(z.getText());
        } catch (Exception e) {
            return 0;
        }
    }

    public double getAngle() {
        try {
            return Double.parseDouble(angle.getText());
        } catch (Exception e) {
            return 0;
        }
    }
}
