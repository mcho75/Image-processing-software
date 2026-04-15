package ImageApp.ui;

import ImageApp.data.ImageData;
import ImageApp.data.Layer;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxListCell;
import javafx.scene.effect.BlendMode;
import javafx.util.StringConverter;
import java.util.Objects;

public class LayerSelectionController {

    @FXML
    public ChoiceBox<BlendMode> blendmodeChoiceBox;
    @FXML
    public Slider opacitySlider;
    @FXML
    private ListView<Layer> listView;
    private ImageData imageData;

    @FXML
    public void initialize() {
        // we establish how the list view will be populated
        listView.setCellFactory(
                CheckBoxListCell.forListView(
                        Node::visibleProperty,
                        new StringConverter<>() {
                            @Override
                            public String toString(Layer layer) {
                                return layer.getName();
                            }

                            @Override
                            public Layer fromString(String s) {
                                return null;
                            }
                        }
                )
        );

        // add every blendmode to the choice box
        blendmodeChoiceBox.getItems().add(null);
        blendmodeChoiceBox.getItems().addAll(BlendMode.values());

        // add a converter
        blendmodeChoiceBox.setConverter(new StringConverter<BlendMode>() {
            @Override
            public String toString(BlendMode blendMode) {
                if (blendMode == null) {
                    return "Normal";
                }
                String name = blendMode.name().toLowerCase().replace("_", " ");
                return Character.toUpperCase(name.charAt(0)) + name.substring(1);
            }

            @Override
            public BlendMode fromString(String s) {
                if (Objects.equals(s, "Normal")) {
                    return null;
                }
                return BlendMode.valueOf(s.toUpperCase().replace(" ", "_"));
            }
        });

        // we make sure the selected row is always visible
        listView.getSelectionModel().selectedIndexProperty().addListener(
                (observable, oldIndex, newIndex) -> listView.scrollTo(newIndex.intValue())
        );
    }

    /**
     * @param img the imageData to associate with this controller
     */
    public void setImageData(ImageData img) {

        // we associate the model to this controller
        imageData = img;

        // we create a bidirectional bind (kinda)
        imageData.currentLayer.addListener(
                (observableValue, oldValue, newValue) ->
                        listView.getSelectionModel().select(newValue.intValue())
        );
        listView.getSelectionModel().selectedIndexProperty().addListener(
                (observableValue, oldValue, newValue) -> {
                    if (newValue.intValue() >= 0) imageData.currentLayer.set(newValue.intValue());
                }
        );

        // we set the content
        listView.setItems(imageData.getLayersList());

        // link the blend mode choice box to the current layer blend mode
        imageData.currentLayer.addListener(
                e -> {
                    if (imageData.currentLayer.get() >= 0) blendmodeChoiceBox.setValue(imageData.getCurrentLayer().getBlendMode());
                }
        );
        blendmodeChoiceBox.setOnAction(
                e -> imageData.getCurrentLayer().setBlendMode(blendmodeChoiceBox.getValue())
        );

        // link the opacity slider to the current layer opacity
        imageData.currentLayer.addListener(
                e -> {
                    if (imageData.currentLayer.get() >= 0) opacitySlider.setValue(imageData.getCurrentLayer().getOpacity() * 100);
                }
        );
        opacitySlider.valueProperty().addListener(
                (e -> imageData.getCurrentLayer().setOpacity(opacitySlider.getValue() / 100))
        );
    }

    @FXML
    public void addLayer() {
        imageData.createNewLayer(true);
    }

    @FXML
    public void deleteLayer() {
        imageData.deleteLayer();
    }

    @FXML
    public void moveUp() {
        imageData.moveLayer(true);
    }

    @FXML
    public void moveDown() {
        imageData.moveLayer(false);
    }
}
