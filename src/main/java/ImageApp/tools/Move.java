package ImageApp.tools;

import ImageApp.data.ImageData;
import ImageApp.ui.ColorWheelController;
import javafx.geometry.Point2D;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;

public class Move implements Tool {

    private Point2D origin = new Point2D(0, 0);

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
            return "src/main/resources/ImageApp/icons/toolMove-dark.png";
        }
        return "src/main/resources/ImageApp/icons/toolMove-light.png";
    }


    /**
     * @return the human-readable name of the tool
     */
    @Override
    public String getHumanName() {
        return "move";
    }

    /**
     * @param e          the event to react to
     * @param imageData  the canva context to modify
     * @param colorWheel the color wheel to update
     */
    @Override
    public void eventHandler(MouseEvent e, ImageData imageData, ColorWheelController colorWheel) {
        if (e.getEventType().equals(MouseEvent.MOUSE_PRESSED)) {
            origin = imageData.getCurrentLayer().parentToLocal(e.getX(), e.getY());
        }
        else if (e.getEventType().equals(MouseEvent.MOUSE_DRAGGED)) {
            imageData.getCurrentLayer().moveToPosition(e.getX() - origin.getX(), e.getY() - origin.getY());
        }
    }
}
