package ImageApp.tools;

import ImageApp.data.ImageData;
import ImageApp.ui.ColorWheelController;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Point2D;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.util.converter.IntegerStringConverter;

import java.io.IOException;
import java.util.LinkedList;

public class BucketFill implements Tool{

    @FXML
    private Slider toleranceSlider;
    @FXML
    private Slider alphaSlider;
    @FXML
    private TextField toleranceField;
    @FXML
    private TextField alphaField;
    @FXML
    private Pane parameterWindow;

    private final SimpleDoubleProperty tolerance = new SimpleDoubleProperty();
    private final SimpleDoubleProperty alpha = new SimpleDoubleProperty();

    public BucketFill() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("BucketFill.fxml"));
            fxmlLoader.setController(this);
            parameterWindow = fxmlLoader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // instanciation of the different widgets
        tolerance.bind(toleranceSlider.valueProperty().divide(100));
        alpha.bind(alphaSlider.valueProperty().divide(100));

        // double listener for fields and sliders
        toleranceSlider.valueProperty().addListener(e ->
                toleranceField.setText(String.valueOf((int) toleranceSlider.getValue())));
        alphaSlider.valueProperty().addListener(e ->
                alphaField.setText(String.valueOf((int) alphaSlider.getValue())));
        toleranceField.textProperty().addListener(e ->
                toleranceSlider.setValue(Double.parseDouble(toleranceField.getText())));
        alphaField.textProperty().addListener(e ->
                alphaSlider.setValue(Double.parseDouble(alphaField.getText())));

        // only allows numerical values

        toleranceField.setTextFormatter(new TextFormatter<>(
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
        toleranceSlider.setValue(0);
        alphaSlider.setValue(100);
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
            return "src/main/resources/ImageApp/icons/toolFill-dark.png";
        }
        return "src/main/resources/ImageApp/icons/toolFill-light.png";
    }

    /**
     * @return the human-readable name of the tool
     */
    @Override
    public String getHumanName() {
        return "Bucket Fill";
    }

    /**
     * @param pr the pixelReader to read from
     * @param oldColor the color to be replaced
     * @param x the x coordinate considered
     * @param y the y coordinate considered
     * @param width the width of the image
     * @param height the height of the image
     * @return whether the pixel can be considered inside the region to fill
     */
    private boolean isInside(PixelReader pr, Color oldColor, int x, int y, int width, int height) {
        if (x < 0 || y < 0 || x >= width || y >= height) {
            return false;
        }
        return isClose(pr.getColor(x, y), oldColor);
    }

    /**
     * @param c1 a first color
     * @param c2 a second color
     * @return whether the two color can be considered equal, with respect to the tolerance
     */
    private boolean isClose(Color c1, Color c2) {
        if (c1.equals(c2)) {
            return true;
        }

        double brightness = c1.getBrightness() - c2.getBrightness();
        double saturation = c1.getSaturation() - c2.getSaturation();
        double alpha = c1.getOpacity() - c2.getOpacity();
        double hue = (c1.getHue() - c2.getHue()) / 360;
        hue = (hue < 0) ? -hue : hue;

        return Math.sqrt((brightness * brightness) + (saturation * saturation) + (hue * hue) + (alpha * alpha))
                < 2 * tolerance.get() / 100;
    }

    /**
     * @param e          the event to react to
     * @param imageData  the GraphicsContext to be modified
     * @param colorWheel the state of the colorwheel
     */
    @Override
    public void eventHandler(MouseEvent e, ImageData imageData, ColorWheelController colorWheel) {
        if (e.getEventType().equals(MouseEvent.MOUSE_CLICKED)) {
            // we extract coordinates for convenience
            Point2D point2D = imageData.getCurrentLayer().parentToLocal(e.getX(), e.getY());
            int x = (int) point2D.getX();
            int y = (int) point2D.getY();

            // we copy the canvas into a buffer, for performance reasons
            SnapshotParameters parameters = new SnapshotParameters();
            parameters.setFill(Color.TRANSPARENT);
            WritableImage buffer = imageData.getCurrentLayer().snapshot(parameters, null);

            // we retrieve the reader and the writer of the buffer
            PixelReader pixelReader = buffer.getPixelReader();
            PixelWriter pixelWriter = buffer.getPixelWriter();
            // for further performance enhancements,
            // we could operate on a byte array obtained via pixelReader.getPixels()

            // we obtain the color to be replaced and the color to fill with
            Color oldColor = pixelReader.getColor(x, y);
            Color newColor = colorWheel.getColor().deriveColor(0, 1, 1, alpha.get());

            // if the source pixel is already of the same color, we stop here
            // failure of this if statement can cause an infinite loop, beware
            if (oldColor.equals(newColor)) {return;}

            imageData.beginTransaction();

            // we don't want to go outside the buffer
            int width = (int) imageData.getCurrentLayer().getWidth();
            int height = (int) imageData.getCurrentLayer().getHeight();

            // we use this as a stack, could be a queue as well
            LinkedList<int[]> to_check = new LinkedList<>();

            // following 40 lines are a transcription of a flood-fill algorithm found on the wikipedia page
            to_check.add(new int[]{x, x, y, 1});
            to_check.add(new int[]{x, x, y - 1, -1});
            while (!to_check.isEmpty()) {
                int[] next = to_check.removeLast(); // we then unpack next
                int x1 = next[0];
                int x2 = next[1];
                y = next[2];
                int dy = next[3];
                x = x1;
                if (isInside(pixelReader, oldColor, x, y, width, height)) {
                    // filling the line to the left
                    while (isInside(pixelReader, oldColor, x - 1, y, width, height)) {
                        pixelWriter.setColor(x - 1, y, newColor);
                        x--;
                    }
                    if (x < x1) {
                        to_check.add(new int[]{x, x1 - 1, y - dy, -dy});
                    }
                }
                while (x1 <= x2) {
                    // filling the line to the right
                    while (isInside(pixelReader, oldColor, x1, y, width, height)) {
                        pixelWriter.setColor(x1, y, newColor);
                        x1++;
                    }
                    // looking for the line below
                    if (x1 > x) {
                        to_check.add(new int[]{x, x1 - 1, y + dy, dy});
                    }
                    // looking for the line above
                    if (x1 - 1 > x2) {
                        to_check.add(new int[]{x2 + 1, x1 - 1, y - dy, -dy});
                    }
                    do {
                        x1++;
                    } while (x1 < x2 && !isInside(pixelReader, oldColor, x1, y, width, height));
                    x = x1;
                }
            }

            imageData.getCurrentLayer().getGraphicsContext2D().drawImage(buffer, 0, 0);
            imageData.endTransaction();
        }
    }
}
