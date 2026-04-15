package ImageApp.ui;

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

    public MosaicDialog() {

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

            fieldSeed.setText(String.valueOf(0));
            fieldPoints.setText(String.valueOf(100));

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
}