package ImageApp.ui;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;

/**
 * Color wheel
 */
public class ColorWheelController {

    private final SimpleDoubleProperty hue = new SimpleDoubleProperty(0.0);
    private final SimpleDoubleProperty sat = new SimpleDoubleProperty(0.0);
    private final SimpleDoubleProperty bright = new SimpleDoubleProperty(0.0);
    @FXML
    private StackPane colorPane;
    @FXML
    private Rectangle colorRect;
    @FXML
    public Rectangle colorRect1;
    @FXML
    public Rectangle colorRect2;
    @FXML
    private Circle colorCursor;
    @FXML
    private StackPane huePane;
    @FXML
    private Rectangle hueRect;
    @FXML
    private Line hueCursor;

    @FXML
    public void initialize() {

        colorRect.fillProperty().bind(new ObjectBinding<>() {
            {
                bind(hue);
            }

            @Override
            protected Color computeValue() {
                return Color.hsb(hue.get(), 1.0, 1.0);
            }
        });

        // dynamic resizing of the colored rectangles
        colorRect.widthProperty().bind(colorPane.widthProperty());
        colorRect1.widthProperty().bind(colorPane.widthProperty());
        colorRect2.widthProperty().bind(colorPane.widthProperty());
        hueRect.widthProperty().bind(huePane.widthProperty());

        // events linked to the movements of the mouse
        EventHandler<MouseEvent> colorPaneMouse = e -> {
            sat.set(clamp(e.getX() / colorPane.getWidth()));
            bright.set(1 - clamp(e.getY() / colorPane.getHeight()));
        };
        EventHandler<MouseEvent> colorBarMouse = e -> hue.set(clamp(e.getX() / huePane.getWidth()) * 360);
        colorPane.addEventHandler(MouseEvent.MOUSE_DRAGGED, colorPaneMouse);
        colorPane.addEventHandler(MouseEvent.MOUSE_CLICKED, colorPaneMouse);
        huePane.addEventHandler(MouseEvent.MOUSE_DRAGGED, colorBarMouse);
        huePane.addEventHandler(MouseEvent.MOUSE_CLICKED, colorBarMouse);

        // binding of those events to the mouse dragging
        colorCursor.translateXProperty().bind(sat.subtract(0.5).multiply(colorPane.widthProperty()));
        colorCursor.translateYProperty().bind(Bindings.subtract(0.5, bright).multiply(colorPane.heightProperty()));
        hueCursor.translateXProperty().bind(hue.divide(360).subtract(0.5).multiply(huePane.widthProperty()));

    }

    /**
     * Let the other components access the current color of the wheel
     * @return Current color of the widget
     */
    public Color getColor() {
        return Color.hsb(hue.get(), sat.get(), bright.get());
    }

    /**
     * Change the color displayed by the wheel
     * @param color The new color
     */
    public void setColor(Color color) {
        hue.set(color.getHue());
        sat.set(color.getSaturation());
        bright.set(color.getBrightness());
    }

    /**
     * Returns the value of x clamped between 0 and 1
     * @param x The value to clamp
     * @return if x is already in [0., 1.], then x; otherwise, the closest value of [0., 1.] to x
     */
    private double clamp(double x) {
        return Math.min(Math.max(x, 0.0), 1.0);
    }
}