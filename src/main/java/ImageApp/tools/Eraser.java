package ImageApp.tools;

import ImageApp.data.ImageData;
import ImageApp.ui.ColorWheelController;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Point2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.image.PixelWriter;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.util.converter.IntegerStringConverter;
import java.io.IOException;

public class Eraser implements Tool{

    @FXML
    private Slider sizeSlider;
    @FXML
    private Pane parameterWindow;
    @FXML
    private TextField sizeField;

    private double brushSize = 20;
    private double lastX = 0;
    private double lastY = 0;

    public Eraser() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("Eraser.fxml"));
            fxmlLoader.setController(this);
            parameterWindow = fxmlLoader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // instanciation of the different widgets
        sizeSlider.setValue(brushSize);
        sizeSlider.valueProperty().addListener(
                (observableValue, oldValue, newValue) -> brushSize = newValue.doubleValue()
        );

        // double listener for fields and sliders
        sizeSlider.valueProperty().addListener(e ->
                sizeField.setText(String.valueOf((int) sizeSlider.getValue())));
        sizeField.textProperty().addListener(e ->
                sizeSlider.setValue(Double.parseDouble(sizeField.getText())));

        // only allows numerical values
        sizeField.setTextFormatter(new TextFormatter<>(
                new IntegerStringConverter(),
                0,
                e -> e.getControlNewText().matches("\\d*") ? e : null
        ));

        // default values
        sizeSlider.setValue(20);
    }

    /**
     * @return a Pane representing the parameters subwindow.
     * The pane should include tickboxes, sliders, etc
     */
    @Override
    public Pane getParameterWindow() {
        return parameterWindow;
    }

    /**
     * @param darkTheme whether we want the dark or light version of the tool
     * @return the path of the icon representing the tool in the tool box
     */
    @Override
    public String getIconPath(boolean darkTheme) {
        if (darkTheme) {
            return "src/main/resources/ImageApp/icons/toolEraser-dark.png";
        }
        return "src/main/resources/ImageApp/icons/toolEraser-light.png";
    }

    /**
     * @return the human-readable name of the tool
     */
    @Override
    public String getHumanName() {
        return "Eraser";
    }

    /**
     * @param e          the event to react to
     * @param imageData  the GraphicsContext to be modified
     * @param colorWheel the state of the colorwheel
     */
    @Override
    public void eventHandler(MouseEvent e, ImageData imageData, ColorWheelController colorWheel) {
        Point2D point2D = imageData.getCurrentLayer().parentToLocal(e.getX(), e.getY());
        if (e.getEventType().equals(MouseEvent.MOUSE_PRESSED)) {
            imageData.beginTransaction();
            lastX = point2D.getX();
            lastY = point2D.getY();
        }
        if (e.getEventType().equals(MouseEvent.MOUSE_DRAGGED) | e.getEventType().equals(MouseEvent.MOUSE_PRESSED)) {
            GraphicsContext gc = imageData.getCurrentLayer().getGraphicsContext2D();
            double coordX = point2D.getX();
            double coordY = point2D.getY();

            // will write pixel by pixel, since clearOval does not exist
            PixelWriter pixelWriter = gc.getPixelWriter();

            // clearing a circle
            for (int x = (int) (-brushSize / 2 + 1 + coordX); x < brushSize / 2 + coordX; x++) {
                for (int y = (int) (-brushSize / 2 + 1 + coordY); y < brushSize / 2 + coordY; y++) {
                    if ((x - coordX) * (x - coordX) + (y - coordY) * (y - coordY) < brushSize * brushSize / 4) {
                        pixelWriter.setColor(x, y, Color.TRANSPARENT);
                    }
                }
            }

            // clearing the path between the current and the last circle
            double dist = Math.sqrt((coordX - lastX) * (coordX - lastX) + (coordY - lastY) * (coordY - lastY));
            for (int k = 0; k < dist; k++) {
                double currentX = lastX + k * (coordX - lastX) / dist;
                double currentY = lastY + k * (coordY - lastY) / dist;
                for (int i = (int) - brushSize / 2; i < brushSize / 2; i++) {
                    pixelWriter.setColor((int) (currentX + (coordY - lastY) * i / dist),
                                         (int) (currentY + (lastX - coordX) * i / dist),
                                         Color.TRANSPARENT);
                }
            }
            lastX = coordX;
            lastY = coordY;
        }

        if (e.getEventType().equals(MouseEvent.MOUSE_RELEASED)) {
            imageData.endTransaction();
        }
    }
}
