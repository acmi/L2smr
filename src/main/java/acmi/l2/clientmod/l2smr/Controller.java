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

import acmi.l2.clientmod.io.UnrealPackage;
import acmi.l2.clientmod.l2smr.dialogs.ImportExportDialog;
import acmi.l2.clientmod.l2smr.dialogs.ModifyDialog;
import acmi.l2.clientmod.l2smr.dialogs.StaticMeshActorDialog;
import acmi.l2.clientmod.l2smr.model.Actor;
import acmi.l2.clientmod.l2smr.model.L2Map;
import acmi.l2.clientmod.l2smr.model.Offsets;
import acmi.l2.clientmod.unreal.Environment;
import acmi.l2.clientmod.unreal.UnrealSerializerFactory;
import acmi.l2.clientmod.util.Util;
import acmi.util.AutoCompleteComboBox;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.beans.value.ObservableBooleanValue;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.io.*;
import java.net.URL;
import java.util.*;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static acmi.l2.clientmod.util.CollectionsMethods.indexIf;
import static acmi.l2.clientmod.util.Util.*;
import static javafx.scene.input.KeyCombination.keyCombination;

@SuppressWarnings({"ConstantConditions", "unused"})
public class Controller implements Initializable {
    @FXML
    private TextField l2Path;
    @FXML
    private ComboBox<String> unrChooser;
    @FXML
    private TableView<Actor> table;
    @FXML
    private TableColumn<Actor, String> actorColumn;
    @FXML
    private TableColumn<Actor, String> staticMeshColumn;

    @FXML
    private TextField filterStaticMesh;
    @FXML
    private TitledPane filterPane;
    @FXML
    private TextField filterX;
    @FXML
    private TextField filterY;
    @FXML
    private TextField filterZ;
    @FXML
    private TextField filterRange;
    @FXML
    private CheckBox rotatable;
    @FXML
    private CheckBox scalable;
    @FXML
    private CheckBox rotating;

    @FXML
    private TitledPane smPane;
    @FXML
    private ComboBox<String> usxChooser;
    @FXML
    private ComboBox<String> smChooser;
    @FXML
    private Button addToUnr;
    @FXML
    private Button createNew;
    @FXML
    private Button smView;

    @FXML
    private TitledPane smaPane;
    @FXML
    private ComboBox<UnrealPackage.ImportEntry> actorStaticMeshChooser;
    @FXML
    private Button actorStaticMeshView;
    @FXML
    private TextField locationX;
    @FXML
    private TextField locationY;
    @FXML
    private TextField locationZ;
    @FXML
    private TextField drawScale3DX;
    @FXML
    private TextField drawScale3DY;
    @FXML
    private TextField drawScale3DZ;
    @FXML
    private TextField drawScale;
    @FXML
    private TextField rotationPitch;
    @FXML
    private TextField rotationYaw;
    @FXML
    private TextField rotationRoll;
    @FXML
    private TextField rotationPitchRate;
    @FXML
    private TextField rotationYawRate;
    @FXML
    private TextField rotationRollRate;
    @FXML
    private TextField zoneState;
    @FXML
    private Button set;
    @FXML
    private Button copy;

    @FXML
    private ProgressIndicator progress;

    private final ObjectProperty<Stage> stage = new SimpleObjectProperty<>(this, "");
    private final ObjectProperty<File> l2Dir = new SimpleObjectProperty<>();
    private final ObjectProperty<File> mapsDir = new SimpleObjectProperty<>();
    private final ObjectProperty<File> staticMeshDir = new SimpleObjectProperty<>();
    private final ObjectProperty<File> systemDir = new SimpleObjectProperty<>();
    private final ListProperty<Actor> actors = new SimpleListProperty<>();
    private final ObjectProperty<File> usx = new SimpleObjectProperty<>();
    private final StringProperty umodelPath = new SimpleStringProperty();
    private final ObjectProperty<UnrealSerializerFactory> classLoader = new SimpleObjectProperty<>();

    public Stage getStage() {
        return stage.get();
    }

    public ObjectProperty<Stage> stageProperty() {
        return stage;
    }

    public void setStage(Stage stage) {
        this.stage.set(stage);
    }

    public File getL2Dir() {
        return l2Dir.get();
    }

    public ObjectProperty<File> l2DirProperty() {
        return l2Dir;
    }

    public void setL2Dir(File l2Dir) {
        this.l2Dir.set(l2Dir);
    }

    public String getUmodelPath() {
        return umodelPath.get();
    }

    public StringProperty umodelPathProperty() {
        return umodelPath;
    }

    public void setUmodelPath(String umodelPath) {
        this.umodelPath.set(umodelPath);
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        stageProperty().addListener(observable -> initializeKeyCombinations());

        this.l2Path.textProperty().bind(Bindings.createStringBinding(() -> l2Dir.getValue() != null ?
                l2Dir.getValue().getAbsolutePath() : "", l2Dir));

        initializeUnr();
        initializeUsx();
        initializeT3d();

        this.filterPane.setExpanded(false);
        this.filterPane.setDisable(true);

        this.smaPane.setDisable(true);

        table.itemsProperty().bind(Bindings.createObjectBinding(() -> {
                    if (actors.get() == null)
                        return FXCollections.emptyObservableList();

                    return FXCollections.observableArrayList(actors.get().stream()
                            .filter(actor -> !rotatable.isSelected() || actor.getRotation() != null)
                            .filter(actor -> !scalable.isSelected() || actor.getScale() != null || actor.getScale3D() != null)
                            .filter(actor -> !rotating.isSelected() || actor.getRotationRate() != null)
                            .filter(actor -> filterStaticMesh.getText() == null ||
                                    filterStaticMesh.getText().isEmpty() ||
                                    actor.getStaticMesh().toLowerCase().contains(filterStaticMesh.getText().toLowerCase()))
                            .filter(actor -> {
                                Double x = getDoubleOrClearTextField(filterX);
                                Double y = getDoubleOrClearTextField(filterY);
                                Double z = getDoubleOrClearTextField(filterZ);
                                Double range = getDoubleOrClearTextField(filterRange);

                                return range == null || range(actor.getLocation(), x, y, z) < range;
                            })
                            .collect(Collectors.toList()));
                },
                actors, filterStaticMesh.textProperty(),
                filterX.textProperty(), filterY.textProperty(), filterZ.textProperty(), filterRange.textProperty(),
                rotatable.selectedProperty(), scalable.selectedProperty(), rotating.selectedProperty()));
    }

    private void initializeKeyCombinations() {
        Map<KeyCombination, Runnable> keyCombinations = new HashMap<>();
        keyCombinations.put(keyCombination("F1"), this::showHelp);
        keyCombinations.put(keyCombination("CTRL+O"), this::chooseL2Folder);
        keyCombinations.put(keyCombination("CTRL+U"), this::selectUmodel);
        keyCombinations.put(keyCombination("CTRL+M"), this::modify);
        keyCombinations.put(keyCombination("CTRL+E"), this::exportSM);
        keyCombinations.put(keyCombination("CTRL+I"), this::importSM);
        keyCombinations.put(keyCombination("CTRL+T"), this::exportSMT3d);

        getStage().getScene().addEventFilter(KeyEvent.KEY_PRESSED, event -> keyCombinations.entrySet()
                .stream()
                .filter(e -> e.getKey().match(event))
                .findAny()
                .ifPresent(e -> {
                    e.getValue().run();
                    event.consume();
                }));
    }

    private void initializeUnr() {
        this.mapsDir.bind(Bindings.createObjectBinding(() -> {
            if (l2Dir.getValue() == null)
                return null;

            return Arrays.stream(l2Dir.getValue().listFiles())
                    .filter(path -> path.isDirectory() && path.getName().equalsIgnoreCase("maps"))
                    .findAny()
                    .orElse(null);
        }, l2Dir));
        this.mapsDir.addListener((observable, oldValue, newValue) -> {
            unrChooser.getSelectionModel().clearSelection();
            unrChooser.getItems().clear();
            unrChooser.setDisable(true);

            if (newValue == null)
                return;

            unrChooser.getItems().addAll(Arrays
                    .stream(newValue.listFiles(MAP_FILE_FILTER))
                    .map(File::getName)
                    .collect(Collectors.toList()));

            unrChooser.setDisable(false);

            AutoCompleteComboBox.autoCompleteComboBox(unrChooser, AutoCompleteComboBox.AutoCompleteMode.CONTAINING);
        });
        this.unrChooser.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            table.getSelectionModel().clearSelection();
            filterPane.setDisable(true);
            actors.set(null);
            actorStaticMeshChooser.getItems().clear();

            System.gc();

            if (newValue == null)
                return;

            try (UnrealPackage up = new UnrealPackage(new File(mapsDir.get(), newValue), true)) {
                longTask(new Task<Void>() {
                    @Override
                    protected Void call() throws Exception {
                        List<UnrealPackage.ImportEntry> staticMeshes = up.getImportTable()
                                .parallelStream()
                                .filter(ie -> ie.getFullClassName().equalsIgnoreCase("Engine.StaticMesh"))
                                .sorted((ie1, ie2) -> String.CASE_INSENSITIVE_ORDER.compare(ie1.getObjectInnerFullName(), ie2.getObjectInnerFullName()))
                                .collect(Collectors.toList());
                        Platform.runLater(() -> {
                            actorStaticMeshChooser.getItems().setAll(staticMeshes);
                            AutoCompleteComboBox.autoCompleteComboBox(actorStaticMeshChooser, AutoCompleteComboBox.AutoCompleteMode.CONTAINING);
                        });

                        List<Actor> actors = up.getExportTable().parallelStream()
                                .filter(e -> UnrealPackage.ObjectFlag.getFlags(e.getObjectFlags()).contains(UnrealPackage.ObjectFlag.HasStack))
                                .map(entry -> {
                                    try {
                                        return new Actor(entry.getIndex(), entry.getObjectInnerFullName(), entry.getObjectRawDataExternally(), up);
                                    } catch (Throwable e) {
                                        return null;
                                    }
                                })
                                .filter(Objects::nonNull)
                                .filter(actor -> actor.getStaticMeshRef() != 0 && actor.getOffsets().location != 0)
                                .collect(Collectors.toList());
                        Platform.runLater(() -> Controller.this.actors.set(FXCollections.observableArrayList(actors)));
                        return null;
                    }
                }, e -> showAlert(Alert.AlertType.ERROR, "Import failed", e.getClass().getSimpleName(), e.getMessage()));
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Read failed", e.getClass().getSimpleName(), e.getMessage());
            }

            resetFilter();
            filterPane.setDisable(false);
        });
        this.actorColumn.setCellValueFactory(actorStringCellDataFeatures -> new SimpleStringProperty(actorStringCellDataFeatures.getValue().getActorName()));
        this.staticMeshColumn.setCellValueFactory(actorStringCellDataFeatures -> new SimpleStringProperty(actorStringCellDataFeatures.getValue().getStaticMesh()));
        this.table.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        this.table.getSelectionModel().selectedItemProperty().addListener((observable) -> updateSMAPane());
    }

    private void updateSMAPane() {
        smaPane.setDisable(true);
        Arrays.stream(new TextField[]{
                locationX, locationY, locationZ,
                rotationPitch, rotationYaw, rotationRoll,
                drawScale3DX, drawScale3DY, drawScale3DZ,
                drawScale,
                rotationPitchRate, rotationYawRate, rotationRollRate,
                zoneState
        }).forEach(this::clearTextAndDisable);
        actorStaticMeshChooser.getSelectionModel().clearSelection();

        Actor actor2 = table.getSelectionModel().getSelectedItem();
        if (actor2 == null)
            return;

        smaPane.setDisable(false);
        float[] location = actor2.getLocation();
        if (location != null) {
            setTextAndEnable(locationX, String.valueOf(location[0]));
            setTextAndEnable(locationY, String.valueOf(location[1]));
            setTextAndEnable(locationZ, String.valueOf(location[2]));
        }
        int[] rotator = actor2.getRotation();
        if (rotator != null) {
            setTextAndEnable(rotationPitch, String.valueOf(rotator[0]));
            setTextAndEnable(rotationYaw, String.valueOf(rotator[1]));
            setTextAndEnable(rotationRoll, String.valueOf(rotator[2]));
        }
        Float ds = actor2.getScale();
        if (ds != null) {
            setTextAndEnable(drawScale, ds.toString());
        }
        float[] drawScale3D = actor2.getScale3D();
        if (drawScale3D != null) {
            setTextAndEnable(drawScale3DX, String.valueOf(drawScale3D[0]));
            setTextAndEnable(drawScale3DY, String.valueOf(drawScale3D[1]));
            setTextAndEnable(drawScale3DZ, String.valueOf(drawScale3D[2]));
        }
        int[] rotationRate = actor2.getRotationRate();
        if (rotationRate != null) {
            setTextAndEnable(rotationPitchRate, String.valueOf(rotationRate[0]));
            setTextAndEnable(rotationYawRate, String.valueOf(rotationRate[1]));
            setTextAndEnable(rotationRollRate, String.valueOf(rotationRate[2]));
        }
        int[] zoneRenderState = actor2.getZoneRenderState();
        if (zoneRenderState != null) {
            String s = Arrays.toString(zoneRenderState);
            setTextAndEnable(zoneState, s.substring(1, s.length() - 1));
        }
        actorStaticMeshChooser.getSelectionModel()
                .select(indexIf(actorStaticMeshChooser.getItems(), ie -> ie.getObjectReference() == actor2.getStaticMeshRef()));
    }

    private void clearTextAndDisable(TextField tf) {
        tf.setText("");
        tf.setDisable(true);
    }

    private void setTextAndEnable(TextField tf, String text) {
        tf.setText(text);
        tf.setDisable(false);
    }

    private void resetFilter() {
        filterStaticMesh.setText("");
        filterX.setText("");
        filterY.setText("");
        filterZ.setText("");
        filterRange.setText("");
        rotatable.setSelected(false);
        scalable.setSelected(false);
        rotating.setSelected(false);
        filterPane.setExpanded(false);
    }

    private void initializeUsx() {
        this.staticMeshDir.bind(Bindings.createObjectBinding(() -> {
            if (l2Dir.getValue() == null)
                return null;
            return Arrays.stream(l2Dir.getValue().listFiles())
                    .filter(path -> path.isDirectory() && path.getName().equalsIgnoreCase("staticmeshes"))
                    .findAny()
                    .orElse(null);
        }, l2Dir));
        this.staticMeshDir.addListener((observable, oldValue, newValue) -> {
            smPane.setDisable(true);

            usxChooser.getSelectionModel().clearSelection();
            usxChooser.getItems().clear();

            if (newValue == null) {
                return;
            }

            smPane.setDisable(false);
            usxChooser.getItems().addAll(Arrays
                    .stream(newValue.listFiles(STATICMESH_FILE_FILTER))
                    .map(File::getName)
                    .collect(Collectors.toList()));
            AutoCompleteComboBox.autoCompleteComboBox(usxChooser, AutoCompleteComboBox.AutoCompleteMode.CONTAINING);
        });
        this.usx.bind(Bindings.createObjectBinding(() -> {
            String selected = usxChooser.getSelectionModel().getSelectedItem();
            if (selected == null)
                return null;
            return new File(staticMeshDir.getValue(), selected);
        }, staticMeshDir, usxChooser.getSelectionModel().selectedItemProperty()));
        this.usx.addListener((observable, oldValue, newValue) -> {
            smChooser.getSelectionModel().clearSelection();
            smChooser.getItems().clear();
            smChooser.setDisable(true);
            if (newValue == null) {
                return;
            }

            try (UnrealPackage up = new UnrealPackage(newValue, true)) {
                smChooser.getItems().setAll(up.getExportTable().stream()
                        .filter(entry -> entry.getObjectClass().getObjectFullName().equals("Engine.StaticMesh"))
                        .map(UnrealPackage.ExportEntry::getObjectInnerFullName)
                        .sorted(String.CASE_INSENSITIVE_ORDER)
                        .collect(Collectors.toList()));
                smChooser.setDisable(false);
                AutoCompleteComboBox.autoCompleteComboBox(smChooser, AutoCompleteComboBox.AutoCompleteMode.CONTAINING);
            } catch (Exception e) {
                showAlert(Alert.AlertType.WARNING, "Read failed", e.getClass().getSimpleName(), e.getMessage());
            }
        });
        this.smView.disableProperty().bind(Bindings.isNull(smChooser.getSelectionModel().selectedItemProperty()));
        ObservableBooleanValue b = Bindings.or(
                Bindings.isNull(unrChooser.getSelectionModel().selectedItemProperty()),
                Bindings.isNull(smChooser.getSelectionModel().selectedItemProperty()));
        this.addToUnr.disableProperty().bind(b);
        this.createNew.disableProperty().bind(b);
    }

    private void initializeT3d() {
        this.systemDir.bind(Bindings.createObjectBinding(() -> {
            if (l2Dir.getValue() == null)
                return null;
            return Arrays.stream(l2Dir.getValue().listFiles())
                    .filter(path -> path.isDirectory() && path.getName().equalsIgnoreCase("system"))
                    .findAny()
                    .orElse(null);
        }, l2Dir));
        this.classLoader.bind(Bindings.createObjectBinding(() -> {
            if (systemDir.get() == null)
                return null;
            return new UnrealSerializerFactory(Environment.fromIni(new File(systemDir.get(), "L2.ini")));
        }, systemDir));
    }

    @FXML
    private void chooseL2Folder() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select L2 folder");
        File dir = directoryChooser.showDialog(getStage());
        if (dir == null)
            return;

        this.l2Dir.setValue(dir);
    }

    @FXML
    private void setLocationRotationStaticMesh() {
        Actor selected = this.table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }
        try (UnrealPackage up = new UnrealPackage(new File(this.mapsDir.get(), this.unrChooser.getSelectionModel().getSelectedItem()), false)) {
            int staticMesh = this.actorStaticMeshChooser.getSelectionModel().getSelectedItem().getObjectReference();
            float[] location = selected.getLocation();
            if (location != null) {
                location[0] = getFloat(this.locationX, location[0]);
                location[1] = getFloat(this.locationY, location[1]);
                location[2] = getFloat(this.locationZ, location[2]);
            }

            int[] rotation = selected.getRotation();
            if (rotation != null) {
                rotation[0] = getInt(this.rotationPitch, rotation[0]);
                rotation[1] = getInt(this.rotationYaw, rotation[1]);
                rotation[2] = getInt(this.rotationRoll, rotation[2]);
            }

            Float ds = getFloat(this.drawScale, selected.getScale());

            float[] drawScale3D = selected.getScale3D();
            if (drawScale3D != null) {
                drawScale3D[0] = getFloat(this.drawScale3DX, drawScale3D[0]);
                drawScale3D[1] = getFloat(this.drawScale3DY, drawScale3D[1]);
                drawScale3D[2] = getFloat(this.drawScale3DZ, drawScale3D[2]);
            }

            int[] rotationRate = selected.getRotationRate();
            if (rotationRate != null) {
                rotationRate[0] = getInt(this.rotationPitchRate, rotationRate[0]);
                rotationRate[1] = getInt(this.rotationYawRate, rotationRate[1]);
                rotationRate[2] = getInt(this.rotationRollRate, rotationRate[2]);
            }

            int[] zoneRenderState = selected.getZoneRenderState();
            if (zoneRenderState != null) {
                try {
                    String[] ss = this.zoneState.getText().split("\\s*,\\s*");
                    int[] zs = new int[ss.length];
                    for (int i = 0; i < ss.length; i++)
                        zs[i] = Integer.parseInt(ss[i]);
                    zoneRenderState = zs;
                } catch (Exception ignore) {
                }
            }

            UnrealPackage.ExportEntry entry = up.getExportTable().get(selected.getInd());
            byte[] raw = entry.getObjectRawData();
            Offsets offsets = selected.getOffsets();
            raw = StaticMeshActorUtil.setStaticMesh(raw, offsets, staticMesh);
            if (location != null) {
                StaticMeshActorUtil.setLocation(raw, offsets, location[0], location[1], location[2]);
            }
            if (rotation != null) {
                StaticMeshActorUtil.setRotation(raw, offsets, rotation[0], rotation[1], rotation[2]);
            }
            if (rotationRate != null) {
                StaticMeshActorUtil.setRotationRate(raw, offsets, rotationRate[0], rotationRate[1], rotationRate[2]);
            }
            if (ds != null) {
                StaticMeshActorUtil.setDrawScale(raw, offsets, ds);
                selected.setScale(ds);
            }
            if (drawScale3D != null) {
                StaticMeshActorUtil.setDrawScale3D(raw, offsets, drawScale3D[0], drawScale3D[1], drawScale3D[2]);
            }
            if (zoneRenderState != null) {
                raw = StaticMeshActorUtil.setZoneRenderState(raw, offsets, zoneRenderState);
                selected.setZoneRenderState(zoneRenderState);
            }
            entry.setObjectRawData(raw);

            this.table.getSelectionModel().getSelectedItem().setStaticMeshRef(staticMesh);
            this.table.getSelectionModel().getSelectedItem().setStaticMesh(this.actorStaticMeshChooser.getSelectionModel().getSelectedItem().toString());
            this.staticMeshColumn.setVisible(false);
            this.staticMeshColumn.setVisible(true);
        } catch (UncheckedIOException e) {
            showAlert(Alert.AlertType.ERROR, "Staticmesh set failed", e.getClass().getSimpleName(), e.getMessage());
        }
    }

    @FXML
    private void addStaticMeshToUnr() {
        String usx = this.usxChooser.getSelectionModel().getSelectedItem();
        usx = usx.substring(0, usx.indexOf('.'));
        try (UnrealPackage up = new UnrealPackage(new File(this.mapsDir.get(), this.unrChooser.getSelectionModel().getSelectedItem()), false)) {
            up.addImportEntries(Collections.singletonMap(usx + "." + this.smChooser.getSelectionModel().getSelectedItem(), "Engine.StaticMesh"));

            updateSMAPane();
        } catch (UncheckedIOException e) {
            showAlert(Alert.AlertType.ERROR, "Staticmesh import failed", e.getClass().getSimpleName(), e.getMessage());
        }
    }

    @FXML
    private void createStaticMeshToUnr() {
        addStaticMeshToUnr();

        String usx = this.usxChooser.getSelectionModel().getSelectedItem();
        usx = usx.substring(0, usx.indexOf('.'));
        try (UnrealPackage up = new UnrealPackage(new File(this.mapsDir.get(), this.unrChooser.getSelectionModel().getSelectedItem()), false)) {
            StaticMeshActorDialog dlg = new StaticMeshActorDialog();
            ButtonType response = dlg.showAndWait().orElse(null);
            if (response != ButtonType.OK)
                return;

            boolean rot = dlg.isRotating();
            boolean zrs = dlg.isZoneRenderState();

            int actorInd = StaticMeshActorUtil.addStaticMeshActor(up, up.objectReferenceByName(usx + "." + this.smChooser.getSelectionModel().getSelectedItem(), c -> true), dlg.getActorClass(), rot, zrs) - 1;

            int ind = this.unrChooser.getSelectionModel().getSelectedIndex();
            this.unrChooser.getSelectionModel().clearSelection();
            this.unrChooser.getSelectionModel().select(ind);

            ind = 0;
            for (int i = 0; i < actors.size(); i++)
                if (actors.get(i).getInd() == actorInd)
                    ind = i;

            table.getSelectionModel().select(ind);
            table.scrollTo(ind);
        } catch (UncheckedIOException e) {
            showAlert(Alert.AlertType.ERROR, "Creation failed", e.getClass().getSimpleName(), e.getMessage());
        }
    }

    @FXML
    private void viewForeignStaticMesh() {
        String obj = this.smChooser.getSelectionModel().getSelectedItem();
        String file = this.usxChooser.getSelectionModel().getSelectedItem();
        showUmodel(obj, file);
    }

    @FXML
    private void viewActorStaticMesh() {
        String obj = this.actorStaticMeshChooser.getSelectionModel().getSelectedItem().toString();
        String file = obj.substring(0, obj.indexOf('.')) + ".usx";
        showUmodel(obj, file);
    }

    private void showUmodel(final String obj, final String file) {
        if (this.umodelPath.get() == null && !selectUmodel())
            return;

        Thread umodel = new Thread(() -> {
            try {
                File f = Arrays.stream(staticMeshDir.get().listFiles())
                        .filter(tmp -> tmp.getName().equalsIgnoreCase(file))
                        .findAny()
                        .orElse(null);

                ProcessBuilder pb = new ProcessBuilder(
                        umodelPath.get(),
                        "-view", "-game=l2",
                        "-obj=" + obj.substring(obj.lastIndexOf('.') + 1),
                        f.getAbsolutePath());
                pb.redirectOutput(ProcessBuilder.Redirect.INHERIT);

                Process process = pb.start();

                StringBuilder sb = new StringBuilder();
                BufferedReader br = new BufferedReader(new InputStreamReader(process.getErrorStream()));

                process.waitFor();

                br.lines().forEach(line -> sb.append(line).append("\r\n"));

                if (sb.length() > 0) {
                    String errText = sb.toString();
                    Platform.runLater(() -> showAlert(Alert.AlertType.WARNING, "View error", "Umodel output", errText.replaceAll("\\<-", "\r\n\\<-")));
                }
            } catch (Exception e) {
                Platform.runLater(() -> showAlert(Alert.AlertType.ERROR, "View error", e.getClass().getSimpleName(), e.getMessage()));
            }
        });
        umodel.setPriority(Thread.MIN_PRIORITY);
        umodel.setDaemon(true);
        umodel.start();
    }

    private boolean selectUmodel() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select umodel");
        File file = fileChooser.showOpenDialog(getStage());
        if (file == null) {
            return false;
        }
        this.umodelPath.setValue(file.getAbsolutePath());
        return true;
    }

    @FXML
    private void copyStaticMesh() {
        Actor selected = this.table.getSelectionModel().getSelectedItem();
        if (selected == null)
            return;

        try (UnrealPackage up = new UnrealPackage(new File(this.mapsDir.get(), this.unrChooser.getSelectionModel().getSelectedItem()), false)) {
            int actorInd = StaticMeshActorUtil.copyStaticMeshActor(up, selected.getInd()) - 1;

            int ind = this.unrChooser.getSelectionModel().getSelectedIndex();
            this.unrChooser.getSelectionModel().clearSelection();
            this.unrChooser.getSelectionModel().select(ind);

            ind = 0;
            for (int i = 0; i < actors.size(); i++)
                if (actors.get(i).getInd() == actorInd)
                    ind = i;

            table.getSelectionModel().select(ind);
            table.scrollTo(ind);
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Copy failed", e.getClass().getSimpleName(), e.getMessage());
        }
    }

    @FXML
    private void modify() {
        ModifyDialog modifyDialog = new ModifyDialog();
        if (ButtonType.OK == modifyDialog.showAndWait().orElse(ButtonType.CANCEL)) {
            Collection<Actor> selected = this.table.getSelectionModel().getSelectedItems();

            selected.forEach(modifyDialog.getTransform());

            updateSMAPane();

            try (UnrealPackage up = new UnrealPackage(new File(this.mapsDir.get(), this.unrChooser.getSelectionModel().getSelectedItem()), false)) {
                for (Actor actor : selected) {
                    UnrealPackage.ExportEntry entry = up.getExportTable().get(actor.getInd());
                    byte[] raw = entry.getObjectRawData();
                    Offsets offsets = actor.getOffsets();
                    StaticMeshActorUtil.setLocation(raw, offsets, actor.getX(), actor.getY(), actor.getZ());
                    entry.setObjectRawData(raw);
                }
            } catch (UncheckedIOException e) {
                showAlert(Alert.AlertType.ERROR, "Staticmesh modify failed", e.getClass().getSimpleName(), e.getMessage());
            }
        }
    }

    @FXML
    private void copyNameToClipboard() {
        Actor selected = this.table.getSelectionModel().getSelectedItem();
        if (selected == null)
            return;

        Toolkit.getDefaultToolkit()
                .getSystemClipboard()
                .setContents(new StringSelection(selected.getStaticMesh()), null);
    }

    @FXML
    private void exportSM() {
        List<Actor> actors = this.table.getSelectionModel().getSelectedItems()
                .stream()
                .map(Actor::clone)
                .collect(Collectors.toList());

        if (actors.isEmpty())
            return;

        int xy = 18 | (20 << 8);
        try {
            xy = getXY(mapsDir.get(), this.unrChooser.getSelectionModel().getSelectedItem());
        } catch (IOException e) {
            showAlert(Alert.AlertType.WARNING, "Export", null, "Couldn't read map coords, using default 18_20");
        }
        ImportExportDialog dlg = new ImportExportDialog(xy & 0xff, (xy >> 8) & 0xff);
        ButtonType response = dlg.showAndWait().orElse(null);
        if (response != ButtonType.OK)
            return;

        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON", "*.json"));
        fileChooser.setTitle("Save");
        File file = fileChooser.showSaveDialog(getStage());
        if (file == null)
            return;

        longTask(new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                float x = dlg.getX(), y = dlg.getY(), z = dlg.getZ();
                double angle = dlg.getAngle();
                AffineTransform rotate = AffineTransform.getRotateInstance(Math.PI * angle / 180, x, y);
                AffineTransform translate = AffineTransform.getTranslateInstance(-x, -y);

                actors.stream().forEach(o -> {
                    Point2D.Float point = new Point2D.Float(o.getX(), o.getY());
                    rotate.transform(point, point);
                    translate.transform(point, point);

                    o.setX(point.x);
                    o.setY(point.y);
                    o.setZ(o.getZ() - z);
                    if (o.getYaw() == null) o.setYaw(0);
                    o.setYaw(((int) (o.getYaw() + angle * 0xFFFF / 360)) & 0xFFFF);
                });

                L2Map map = new L2Map(x, y, z, actors);
                ObjectMapper objectMapper = new ObjectMapper();

                try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                    objectMapper.writeValue(baos, map);

                    try (OutputStream fos = new FileOutputStream(file)) {
                        baos.writeTo(fos);
                    }
                }
                return null;
            }
        }, e -> showAlert(Alert.AlertType.ERROR, "Export failed", e.getClass().getSimpleName(), e.getMessage()));
    }

    @FXML
    private void importSM() {
        if (this.unrChooser.getSelectionModel().getSelectedItem() == null)
            return;

        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON", "*.json"));
        fileChooser.setTitle("Open");
        File file = fileChooser.showOpenDialog(getStage());
        if (file == null)
            return;

        int xy = 18 | (20 << 8);
        try {
            xy = getXY(mapsDir.get(), this.unrChooser.getSelectionModel().getSelectedItem());
        } catch (IOException e) {
            showAlert(Alert.AlertType.WARNING, "Import", null, "Couldn't read map coords, using default 18_20");
        }

        ImportExportDialog dlg = new ImportExportDialog(xy & 0xff, (xy >> 8) & 0xff);
        ButtonType response = dlg.showAndWait().orElse(null);
        if (response != ButtonType.OK)
            return;

        AffineTransform transform = AffineTransform.getRotateInstance(Math.PI * dlg.getAngle() / 180);

        ObjectMapper objectMapper = new ObjectMapper();
        try {
            L2Map map = objectMapper.readValue(file, L2Map.class);

            if (map.getStaticMeshes().isEmpty())
                return;

            longTask(new Task<Void>() {
                @Override
                protected Void call() throws Exception {
                    try (UnrealPackage up = new UnrealPackage(new File(mapsDir.get(), unrChooser.getSelectionModel().getSelectedItem()), false)) {
                        up.addImportEntries(map.getStaticMeshes().stream()
                                .collect(Collectors.toMap(Actor::getStaticMesh, a -> "Engine.StaticMesh", (o1, o2) -> o1)));

                        for (int i = 0; i < map.getStaticMeshes().size(); i++) {
                            Actor actor = map.getStaticMeshes().get(i);

                            int newActorInd = StaticMeshActorUtil.addStaticMeshActor(up, up.objectReferenceByName(actor.getStaticMesh(), c -> true), actor.getActorClass(), true, true);
                            UnrealPackage.ExportEntry newActor = (UnrealPackage.ExportEntry) up.objectReference(newActorInd);

                            actor.setActorName(newActor.getObjectInnerFullName());
                            actor.setStaticMeshRef(up.objectReferenceByName(actor.getStaticMesh(), c -> true));

                            byte[] bytes = newActor.getObjectRawData();
                            Offsets offsets = StaticMeshActorUtil.getOffsets(bytes, up);

                            Point2D.Float point = new Point2D.Float(actor.getX(), actor.getY());
                            transform.transform(point, point);

                            actor.setX(dlg.getX() + point.x);
                            actor.setY(dlg.getY() + point.y);
                            actor.setZ(dlg.getZ() + actor.getZ());

                            actor.setYaw((actor.getYaw() + (int) (0xFFFF * dlg.getAngle() / 360)) & 0xFFFF);

                            StaticMeshActorUtil.setLocation(bytes, offsets, actor.getX(), actor.getY(), actor.getZ());
                            StaticMeshActorUtil.setRotation(bytes, offsets, actor.getPitch(), actor.getYaw(), actor.getRoll());
                            if (actor.getScale3D() != null)
                                StaticMeshActorUtil.setDrawScale3D(bytes, offsets, actor.getScaleX(), actor.getScaleY(), actor.getScaleZ());
                            if (actor.getScale() != null)
                                StaticMeshActorUtil.setDrawScale(bytes, offsets, actor.getScale());
                            if (actor.getRotationRate() != null)
                                StaticMeshActorUtil.setRotationRate(bytes, offsets, actor.getPitchRate(), actor.getYawRate(), actor.getRollRate());
                            if (actor.getZoneRenderState() != null)
                                bytes = StaticMeshActorUtil.setZoneRenderState(bytes, offsets, actor.getZoneRenderState());

                            newActor.setObjectRawData(bytes);

                            updateProgress(i, map.getStaticMeshes().size());
                        }
                    }

                    Platform.runLater(() -> {
                        String unr = unrChooser.getSelectionModel().getSelectedItem();
                        Actor act = map.getStaticMeshes().get(map.getStaticMeshes().size() - 1);

                        unrChooser.getSelectionModel().clearSelection();
                        unrChooser.getSelectionModel().select(unr);

                        table.getSelectionModel().select(act);
                        table.scrollTo(act);
                    });
                    return null;
                }
            }, e -> showAlert(Alert.AlertType.ERROR, "Import failed", e.getClass().getSimpleName(), e.getMessage()));
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Import failed", e.getClass().getSimpleName(), e.getMessage());
        }
    }

    @FXML
    private void exportSMT3d() {
        List<Actor> actors = this.table.getSelectionModel().getSelectedItems()
                .stream()
                .map(Actor::clone)
                .collect(Collectors.toList());

        if (actors.isEmpty())
            return;

        if (showConfirm(Alert.AlertType.CONFIRMATION, "Export", null, "Separate? (new file per Actor)")) {
            DirectoryChooser directoryChooser = new DirectoryChooser();
            directoryChooser.setTitle("Select save folder");
            File dir = directoryChooser.showDialog(getStage());
            if (dir == null)
                return;

            longTask(new Task<Void>() {
                @Override
                protected Void call() throws Exception {
                    T3d t3d = new T3d(classLoader.get());
                    try (UnrealPackage up = new UnrealPackage(new File(mapsDir.get(), unrChooser.getSelectionModel().getSelectedItem()), false)) {
                        for (Actor actor : actors) {
                            UnrealPackage.ExportEntry entry = up.getExportTable().get(actor.getInd());
                            try (Writer fos = new FileWriter(new File(dir, entry.getObjectName().getName() + ".t3d"))) {
                                fos.write(t3d.toT3d(entry, 0).toString());
                            }
                        }
                    }
                    return null;
                }
            }, e -> showAlert(Alert.AlertType.ERROR, "Export failed", e.getClass().getSimpleName(), e.getMessage()));
        } else {
            FileChooser fileChooser = new FileChooser();
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Unreal text", "*.t3d"));
            fileChooser.setTitle("Save");
            File file = fileChooser.showSaveDialog(getStage());
            if (file == null)
                return;

            longTask(new Task<Void>() {
                @Override
                protected Void call() throws Exception {
                    StringBuilder sb = new StringBuilder("Begin Map");
                    T3d t3d = new T3d(classLoader.get());
                    try (UnrealPackage up = new UnrealPackage(new File(mapsDir.get(), unrChooser.getSelectionModel().getSelectedItem()), false)) {
                        actors.stream()
                                .map(actor -> up.getExportTable().get(actor.getInd()))
                                .forEach(entry -> sb.append(Util.newLine()).append(t3d.toT3d(entry, 0)));
                    }
                    sb.append(newLine()).append("End Map");

                    try (Writer fos = new FileWriter(file)) {
                        fos.write(sb.toString());
                    }
                    return null;
                }
            }, e -> showAlert(Alert.AlertType.ERROR, "Export failed", e.getClass().getSimpleName(), e.getMessage()));
        }
    }

    private void showHelp() {
        showAlert(Alert.AlertType.INFORMATION, "Help", null,
                "Hotkeys:\n" +
                        "F1 - help\n" +
                        "CTRL+O - select L2 folder\n" +
                        "CTRL+U - select UE viewer\n" +
                        "CTRL+M - modify selected actors\n" +
                        "CTRL+E - export selected staticmeshes to json file\n" +
                        "CTRL+I - import staticmeshes from json file\n" +
                        "CTRL+T - export selected staticmeshes to t3d file");
    }

    private void longTask(Task<Void> task, Consumer<Throwable> exceptionHandler) {
        progress.progressProperty().bind(task.progressProperty());
        task.setOnScheduled(event -> Platform.runLater(() -> progress.setVisible(true)));
        task.setOnFailed(event -> Platform.runLater(() -> progress.setVisible(false)));
        task.setOnSucceeded(event -> Platform.runLater(() -> progress.setVisible(false)));

        ForkJoinPool.commonPool().submit(task);
    }
}