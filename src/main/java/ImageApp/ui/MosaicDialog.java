package ImageApp.ui;

import ImageApp.data.ImageData;
import ImageApp.data.Layer;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.util.converter.IntegerStringConverter;

import java.io.IOException;

public class MosaicDialog extends Dialog<ButtonType> {

    @FXML
    private TextField fieldSeed;
    @FXML
    private TextField fieldPoints;
    @FXML
    private ChoiceBox<String> choiceBox;
    @FXML
    private ChoiceBox<Layer> choiceLayer;
    @FXML
    private Label labelSeed;
    @FXML
    private Label labelPoints;

    private boolean isFixedNumber;

    public MosaicDialog(ImageData imageData) {

        super();

        DialogPane mosaicPane;
        try {

            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("MosaicDialog.fxml"));
            fxmlLoader.setController(this);
            mosaicPane = fxmlLoader.load();

            fieldSeed.setTextFormatter(new TextFormatter<>(
                    new IntegerStringConverter(),
                    0,
                    e -> e.getControlNewText().matches("\\d*") ? e : null
            ));
            fieldPoints.setTextFormatter(new TextFormatter<>(
                    new IntegerStringConverter(),
                    0,
                    e -> e.getControlNewText().matches("\\d*") ? e : null
            ));

            choiceBox.getItems().addAll("Fixed number of points", "Density mask");
            choiceLayer.getItems().addAll(imageData.getLayersList());

            choiceBox.valueProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue.equals("Fixed number of points")) {
                    fieldPoints.setVisible(true);
                    labelPoints.setVisible(true);
                    choiceLayer.setVisible(false);
                    isFixedNumber = true;
                }
                else if (newValue.equals("Density mask")) {
                    fieldPoints.setVisible(false);
                    labelPoints.setVisible(false);
                    choiceLayer.setVisible(true);
                    isFixedNumber = false;
                }
            });

            fieldSeed.setText(String.valueOf(0));
            fieldPoints.setText(String.valueOf(100));
            choiceBox.setValue("Fixed number of points");
            choiceLayer.setValue(imageData.getCurrentLayer());

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        setDialogPane(mosaicPane);
    }

    public long getSeed() {
        return Long.parseLong(fieldSeed.getText());
    }

    public int getPoints() {
        return Integer.parseInt(fieldPoints.getText());
    }

    public boolean getFixedNumber() {
        return isFixedNumber;
    }

    public Layer getLayer() {
        return choiceLayer.getValue();
    }
}