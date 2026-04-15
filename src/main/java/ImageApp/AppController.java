package ImageApp;

import ImageApp.MenuAction.Effects;
import ImageApp.MenuAction.Transformations;
import ImageApp.MenuAction.FileActions;
import ImageApp.data.ImageData;
import ImageApp.data.Layer;
import ImageApp.ui.*;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.ListChangeListener;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

import java.util.Objects;
import java.util.Optional;

public class AppController {

    private final SimpleBooleanProperty darkTheme = new SimpleBooleanProperty(true);
    @FXML
    private ScrollPane viewArea;
    @FXML
    private VBox editingBar;
    @FXML
    private Rectangle toolsPropertiesRectangle;
    @FXML
    private VBox toolsProperties;
    @FXML
    private VBox root;
    @FXML
    private ColorWheelController colorWheelController;
    @FXML
    private LayerSelectionController layerSelectionController;
    @FXML
    private ToolsGrid toolsGrid;
    @FXML
    private Pane drawArea;
    @FXML
    private Pane drawBounds;
    @FXML
    private Rectangle rectangleClip;

    private final ImageData imageData = new ImageData();

    public void initialize() {

        root.getStylesheets().add(Objects.requireNonNull(getClass().getResource("style.css")).toExternalForm());
        root.getStylesheets().add(Objects.requireNonNull(getClass().getResource("theme-dark.css")).toExternalForm());

        // updates the visual aspect when a layer is added or deleted
        imageData.getLayersList().addListener(
                (ListChangeListener<Layer>) change -> drawArea.getChildren().setAll(change.getList())
        );

        // prevent layers from going out of the frame
        drawBounds.prefWidthProperty().bind(imageData.widthProperty());
        drawBounds.prefHeightProperty().bind(imageData.heightProperty());
        rectangleClip.widthProperty().bind(imageData.widthProperty());
        rectangleClip.heightProperty().bind(imageData.heightProperty());

        // center the image inside the ScrollPane
        drawBounds.translateXProperty().bind(viewArea.widthProperty().subtract(drawBounds.widthProperty()).divide(2));
        drawBounds.translateYProperty().bind(viewArea.heightProperty().subtract(drawBounds.heightProperty()).divide(2));

        // we collect all Canvas interaction then we delegate the reaction to the selected tool
        EventHandler<MouseEvent> eventHandler = e -> toolsGrid.getCurrentTool().eventHandler(
                e,
                imageData,
                colorWheelController
        );
        drawArea.addEventHandler(MouseEvent.ANY, eventHandler);

        // when we change tools, we also change the tool parameters window
        toolsGrid.toolNumber.addListener(observable ->
                toolsProperties.getChildren().setAll(toolsGrid.getCurrentTool().getParameterWindow()));

        // we link the imageData to the layerSelection;
        layerSelectionController.setImageData(imageData);

        // changes theme when darkTheme is changed
        darkTheme.addListener(e -> {
            root.getStylesheets().removeIf(s -> s.contains("theme-"));
            toolsGrid.switchIcons(darkTheme.get());
            if (darkTheme.get()) {
                root.getStylesheets().add(Objects.requireNonNull(getClass().getResource("theme-dark.css")).toExternalForm());
            }
            else {
                root.getStylesheets().add(Objects.requireNonNull(getClass().getResource("theme-light.css")).toExternalForm());
            }
        });

        // We wait for the app to be initialized, then we show the welcome dialog
        Platform.runLater(
                () -> {
                    Stage stage = (Stage) root.getScene().getWindow();
                    if (!newProjectAction()) {
                        Platform.exit();
                    }
                    stage.setOnCloseRequest(
                            (event) -> {
                                event.consume();
                                FileActions.close(imageData, stage);
                            }
                    );
                }
        );
    }

    public void closeAction() {
        FileActions.close(imageData, (Stage) root.getScene().getWindow());
    }

    public void saveAction() {
        FileActions.save(imageData, (Stage) root.getScene().getWindow());
    }

    public void saveAsAction() {
        FileActions.saveAs(imageData, (Stage) root.getScene().getWindow());
    }

    public void exportImageAction() {
        FileActions.exportImage(imageData, (Stage) root.getScene().getWindow());
    }

    public void importImageAction() {
        FileActions.importImage(imageData, (Stage) root.getScene().getWindow());
    }

    public boolean newProjectAction() {
        NewProjectDialog newProjectDialog = new NewProjectDialog();
        newProjectDialog.getDialogPane().getStylesheets().addAll(root.getStylesheets());

        boolean b = true;
        Optional<Integer> result = newProjectDialog.showAndWait();
        int popupResult = result.orElse(0);

        switch (popupResult) {
            case 0 -> FileActions.close(imageData, (Stage) root.getScene().getWindow());
            case 1 -> FileActions.newProject(imageData, (int) newProjectDialog.getSizeX(), (int) newProjectDialog.getSizeY());
            case 2 -> b = FileActions.openProject(imageData, (Stage) root.getScene().getWindow());
            case 3 -> b = FileActions.newImage(imageData, (Stage) root.getScene().getWindow());
        }

        return b;
    }

    public void openProjectAction() {
        FileActions.openProject(imageData, (Stage) root.getScene().getWindow());
    }

    public void resize() {
        ResizeDialog resizeDialog = new ResizeDialog(imageData.getCurrentLayer().getWidth(), imageData.getCurrentLayer().getHeight());
        resizeDialog.getDialogPane().getStylesheets().addAll(root.getStylesheets());
        Optional<ButtonType> result = resizeDialog.showAndWait();
        ButtonBar.ButtonData popupResult = result.orElse(ButtonType.CLOSE).getButtonData();

        switch (popupResult) {
            case ButtonBar.ButtonData.APPLY -> Transformations.resize(imageData, resizeDialog.getSizeX(), resizeDialog.getSizeY());
            case ButtonBar.ButtonData.CANCEL_CLOSE -> {}
            default -> throw new RuntimeException("Popup result invalid : %s".formatted(popupResult.toString()));
        }
    }

    public void htransFlip() {
        Transformations.hflipTrans(imageData);
    }

    public void vtransFlip() {
        Transformations.vflipTrans(imageData);
    }

    public void rotateClockwise() {
        Transformations.rotate(imageData, 90.0);
    }

    public void rotateAnti() {
        Transformations.rotate(imageData, -90.0);
    }

    public void rotate() {
        Transformations.rotate(imageData);
    }

    public void mosaicBlur() {
        MosaicDialog mosaicDialog = new MosaicDialog(imageData);
        mosaicDialog.getDialogPane().getStylesheets().addAll(root.getStylesheets());
        Optional<ButtonType> result = mosaicDialog.showAndWait();
        ButtonBar.ButtonData popupResult = result.orElse(ButtonType.CANCEL).getButtonData();

        switch (popupResult) {
            case ButtonBar.ButtonData.APPLY -> Effects.mosaic(imageData, mosaicDialog.getSeed(), mosaicDialog.getPoints(), mosaicDialog.getLayer(), mosaicDialog.getFixedNumber());
            case ButtonBar.ButtonData.CANCEL_CLOSE -> {}
            default -> throw new RuntimeException("Popup result invalid : %s".formatted(popupResult.toString()));
        }
    }

    public void gaussianBlur() {
        Effects.gaussianBlur(imageData);
    }

    public void seamCarving() {
        SeamCarvingDialog seamCarvingDialog = new SeamCarvingDialog((int) imageData.getCurrentLayer().getWidth(), (int) imageData.getCurrentLayer().getHeight());
        seamCarvingDialog.getDialogPane().getStylesheets().addAll(root.getStylesheets());
        Optional<ButtonType> result = seamCarvingDialog.showAndWait();
        ButtonBar.ButtonData popupResult = result.orElse(ButtonType.CANCEL).getButtonData();

        switch (popupResult) {
            case ButtonBar.ButtonData.APPLY -> Effects.seamCarving(imageData, seamCarvingDialog.getSlider(), seamCarvingDialog.getHorizontal());
            case ButtonBar.ButtonData.CANCEL_CLOSE -> {}
            default -> throw new RuntimeException("Popup result invalid : %s".formatted(popupResult.toString()));
        }
    }

    public void greyScale() {
        Effects.greyScale(imageData);
    }

    public void openAbout() {
        AboutDialog aboutDialog = new AboutDialog();
        aboutDialog.getDialogPane().getStylesheets().addAll(root.getStylesheets());
        aboutDialog.showAndWait();
    }

    public void switchTheme() {
        darkTheme.set(!darkTheme.get());
    }

    public void undo() {
        imageData.undo();
    }

    public void redo() {
        imageData.redo();
    }
}

