package ImageApp.tools;

import ImageApp.data.ImageData;
import ImageApp.ui.ColorWheelController;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;

public class Empty implements Tool {

    /**
     * @return an empty pane
     */
    @Override
    public Pane getParameterWindow() {
        return new Pane();
    }

    /**
     * @param darkTheme whether we want the dark or light version of the tool
     * @return the path of the disabled icon
     */
    @Override
    public String getIconPath(boolean darkTheme) {
        if (darkTheme) {
            return "src/main/resources/ImageApp/icons/toolDisabled-dark.png";
        }
        return "src/main/resources/ImageApp/icons/toolDisabled-light.png";
    }

    /**
     * @return the human-readable name of this tool
     */
    @Override
    public String getHumanName() {
        return "Aucun";
    }

    /**
     * @param e          an event
     * @param imageData  the canva graphics
     * @param colorWheel the color wheel
     */
    @Override
    public void eventHandler(MouseEvent e, ImageData imageData, ColorWheelController colorWheel) {
        // empty tool does not react to anything
    }
}
