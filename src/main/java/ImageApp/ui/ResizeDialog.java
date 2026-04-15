package ImageApp.ui;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.util.converter.IntegerStringConverter;
import java.io.IOException;

public class ResizeDialog extends Dialog<ButtonType> {

    @FXML
    private TextField fieldSizeX;
    @FXML
    private TextField fieldSizeY;
    @FXML
    private CheckBox checkBox;
    private boolean setByCode = false;

    public ResizeDialog(double width, double height) {
        super();

        DialogPane resizePane;
        try {

            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("ResizeDialog.fxml"));
            fxmlLoader.setController(this);
            resizePane = fxmlLoader.load();
            fieldSizeX.textProperty().addListener(e -> changeValues(true, width, height));
            fieldSizeY.textProperty().addListener(e -> changeValues(false, width, height));
            checkBox.selectedProperty().addListener(e -> changeValues(true, width, height));

            fieldSizeX.setTextFormatter(new TextFormatter<>(
                    new IntegerStringConverter(),
                    0,
                    e -> e.getControlNewText().matches("\\d*") ? e : null
            ));
            fieldSizeY.setTextFormatter(new TextFormatter<>(
                    new IntegerStringConverter(),
                    0,
                    e -> e.getControlNewText().matches("\\d*") ? e : null
            ));

            fieldSizeX.setText(String.valueOf((int) width));
            fieldSizeY.setText(String.valueOf((int) height));

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        setDialogPane(resizePane);
    }

    private void changeValues(boolean valueX, double width, double height) {
        if (checkBox.isSelected() && !setByCode) {
            setByCode = true;
            if (valueX) {
                fieldSizeY.setText(String.valueOf((int) (height * Double.parseDouble(fieldSizeX.getText()) / width)));
            }
            else {
                fieldSizeX.setText(String.valueOf((int) (width * Double.parseDouble(fieldSizeY.getText()) / height)));
            }
            setByCode = false;
        }
    }

    public double getSizeX() throws NumberFormatException {
        return Double.parseDouble(fieldSizeX.getText());
    }

    public double getSizeY() throws NumberFormatException {
        return Double.parseDouble(fieldSizeY.getText());
    }
}
