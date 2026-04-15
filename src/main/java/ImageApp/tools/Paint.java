package ImageApp.tools;

import ImageApp.data.ImageData;
import ImageApp.ui.ColorWheelController;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Point2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;
import javafx.util.converter.IntegerStringConverter;

import java.io.IOException;

public class Paint implements Tool {
    @FXML
    private Slider sizeSlider;
    @FXML
    private Slider alphaSlider;
    @FXML
    private Pane parameterWindow;
    @FXML
    private TextField sizeField;
    @FXML
    private TextField alphaField;

    private final SimpleDoubleProperty brushSize = new SimpleDoubleProperty();
    private final SimpleDoubleProperty alpha = new SimpleDoubleProperty();

    public Paint() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("Paint.fxml"));
            fxmlLoader.setController(this);
            parameterWindow = fxmlLoader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // instanciation of the different widgets
        brushSize.bind(sizeSlider.valueProperty());
        alpha.bind(alphaSlider.valueProperty().divide(100));

        // double listener for fields and sliders
        sizeSlider.valueProperty().addListener(e ->
                sizeField.setText(String.valueOf((int) sizeSlider.getValue())));
        alphaSlider.valueProperty().addListener(e ->
                alphaField.setText(String.valueOf((int) alphaSlider.getValue())));
        sizeField.textProperty().addListener(e ->
                sizeSlider.setValue(Double.parseDouble(sizeField.getText())));
        alphaField.textProperty().addListener(e ->
                alphaSlider.setValue(Double.parseDouble(alphaField.getText())));

        // only allows numerical values
        sizeField.setTextFormatter(new TextFormatter<>(
                new IntegerStringConverter(),
                0,
                e -> e.getControlNewText().matches("\\d*") ? e : null
        ));
        alphaField.setTextFormatter(new TextFormatter<>(
                new IntegerStringConverter(),
                0,
                e -> e.getControlNewText().matches("\\d*") ? e : null
        ));

        // default values
        sizeSlider.setValue(20);
        alphaSlider.setValue(100);
    }

    /**
     * @return a pane with a single size slider
     */
    @Override
    public Pane getParameterWindow() {
        return parameterWindow;
    }

    /**
     * @param darkTheme whether we want the dark or light version of the tool
     * @return the painting icon path
     */
    @Override
    public String getIconPath(boolean darkTheme) {
        if (darkTheme) {
            return "src/main/resources/ImageApp/icons/toolBrush-dark.png";
        }
        return "src/main/resources/ImageApp/icons/toolBrush-light.png";
    }

    /**
     * @return the human-readable name of the tool
     */
    @Override
    public String getHumanName() {
        return "pinceau";
    }

    /**
     * @param e          the event to react to
     * @param imageData  the canva context to modify
     * @param colorWheel the color wheel
     */
    @Override
    public void eventHandler(MouseEvent e, ImageData imageData, ColorWheelController colorWheel) {
        // we translate view coordinates into canvas coordinates
        Point2D point2D = imageData.getCurrentLayer().parentToLocal(e.getX(), e.getY());

        if (e.getEventType().equals(MouseEvent.MOUSE_PRESSED)) {
            imageData.beginTransaction();
            GraphicsContext gc = imageData.getCurrentLayer().getGraphicsContext2D();
            gc.setLineJoin(StrokeLineJoin.ROUND);
            gc.setLineCap(StrokeLineCap.ROUND);
            gc.setLineWidth(brushSize.get());
            gc.setStroke(colorWheel.getColor());
            gc.setGlobalAlpha(alpha.get());

            gc.moveTo(point2D.getX(), point2D.getY());
            gc.beginPath();
        }
        if (e.getEventType().equals(MouseEvent.MOUSE_RELEASED)) {
            GraphicsContext gc = imageData.getCurrentLayer().getGraphicsContext2D();
            gc.lineTo(point2D.getX(), point2D.getY());
            gc.stroke();
            gc.closePath();
            imageData.endTransaction();
        }
        if (e.getEventType().equals(MouseEvent.MOUSE_DRAGGED)) {
            GraphicsContext gc = imageData.getCurrentLayer().getGraphicsContext2D();
            gc.lineTo(point2D.getX(), point2D.getY());
            gc.stroke();
            gc.closePath();
            gc.beginPath();
            gc.moveTo(point2D.getX(), point2D.getY());
        }
    }
}
