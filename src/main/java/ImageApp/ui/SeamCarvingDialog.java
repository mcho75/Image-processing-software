package ImageApp.ui;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.util.converter.IntegerStringConverter;

import java.io.IOException;

public class SeamCarvingDialog extends Dialog<ButtonType> {

    @FXML
    private Slider slider;
    @FXML
    private TextField field;
    @FXML
    private ChoiceBox<String> choiceBox;

    public SeamCarvingDialog(int width, int height) {

        super();

        DialogPane seamCarvingPane;
        try {

            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("SeamCarvingDialog.fxml"));
            fxmlLoader.setController(this);
            seamCarvingPane = fxmlLoader.load();

            choiceBox.getItems().addAll("Horizontally", "Vertically");
            choiceBox.setValue("Horizontally");

            // double listener for field and slider
            slider.valueProperty().addListener(e ->
                    field.setText(String.valueOf((int) slider.getValue())));
            field.textProperty().addListener(e ->
                    slider.setValue(Double.parseDouble(field.getText())));

            field.setTextFormatter(new TextFormatter<>(
                    new IntegerStringConverter(),
                    0,
                    e -> e.getControlNewText().matches("\\d*") ? e : null
            ));

            // cap the slider with the dimensions
            choiceBox.valueProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue.equals("Vertically")) {
                    slider.setMax(width - 2);
                }
                else if (newValue.equals("Horizontally")) {
                    slider.setMax(height - 2);
                }
            });

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        setDialogPane(seamCarvingPane);
    }

    public int getSlider() {
        return (int) slider.getValue();
    }

    public boolean getHorizontal() {
        return choiceBox.getValue().equals("Horizontally");
    }
}