package ImageApp.tools;

import ImageApp.data.ImageData;
import ImageApp.ui.ColorWheelController;
import javafx.geometry.Point2D;
import javafx.scene.image.PixelReader;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;

public class ColorPicker implements Tool {

    /**
     * @return a pane with a single size slider
     */
    @Override
    public Pane getParameterWindow() {
        return new Pane();
    }

    /**
     * @param darkTheme whether we want the dark or light version of the tool
     * @return the path of the color picker icon
     */
    @Override
    public String getIconPath(boolean darkTheme) {
        if (darkTheme) {
            return "src/main/resources/ImageApp/icons/toolPicker-dark.png";
        }
        return "src/main/resources/ImageApp/icons/toolPicker-light.png";
    }


    /**
     * @return the human-readable name of the tool
     */
    @Override
    public String getHumanName() {
        return "color picker";
    }

    /**
     * @param e          the event to react to
     * @param imageData  the canva context to modify
     * @param colorWheel the color wheel to update
     */
    @Override
    public void eventHandler(MouseEvent e, ImageData imageData, ColorWheelController colorWheel) {
        Point2D point2D = imageData.getCurrentLayer().parentToLocal(e.getX(), e.getY());
        PixelReader pr = imageData.getCurrentLayer().snapshot(null, null).getPixelReader();
        if (e.getEventType().equals(MouseEvent.MOUSE_DRAGGED) | e.getEventType().equals(MouseEvent.MOUSE_PRESSED)) {
            try {
                colorWheel.setColor(pr.getColor((int) point2D.getX(), (int) point2D.getY()));
            } catch (IndexOutOfBoundsException ignored) {}
        }
    }
}
