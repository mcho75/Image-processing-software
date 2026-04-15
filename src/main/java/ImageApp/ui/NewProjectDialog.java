package ImageApp.ui;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.util.converter.IntegerStringConverter;
import java.io.IOException;

public class NewProjectDialog extends Dialog<Integer> {

    @FXML
    private TextField fieldWidth;
    @FXML
    private TextField fieldHeight;
    @FXML
    private Button newProjectButton;
    @FXML
    private Button openButton;
    @FXML
    private Button importButton;
    @FXML
    private Button cancelButton;

    public NewProjectDialog() {
        super();

        DialogPane newProjectPane;
        try {

            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("NewProjectDialog.fxml"));
            fxmlLoader.setController(this);
            newProjectPane = fxmlLoader.load();

            cancelButton.setOnAction(e -> setResult(0));
            newProjectButton.setOnAction(e -> setResult(1));
            openButton.setOnAction(e -> setResult(2));
            importButton.setOnAction(e -> setResult(3));

            fieldWidth.setTextFormatter(new TextFormatter<>(
                    new IntegerStringConverter(),
                    0,
                    e -> e.getControlNewText().matches("\\d*") ? e : null
            ));
            fieldHeight.setTextFormatter(new TextFormatter<>(
                    new IntegerStringConverter(),
                    0,
                    e -> e.getControlNewText().matches("\\d*") ? e : null
            ));

            fieldWidth.setText(String.valueOf(500));
            fieldHeight.setText(String.valueOf(500));

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        setDialogPane(newProjectPane);
    }

    public double getSizeX() throws NumberFormatException {
        return Double.parseDouble(fieldWidth.getText());
    }

    public double getSizeY() throws NumberFormatException {
        return Double.parseDouble(fieldHeight.getText());
    }
}
