package ImageApp.tools;

import ImageApp.data.ImageData;
import ImageApp.ui.ColorWheelController;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;

// every tool in the toolbox must respect this interface
public interface Tool {

    /**
     * @return a Pane representing the parameters subwindow.
     * The pane should include tickboxes, sliders, etc
     */
    Pane getParameterWindow();

    /**
     * @param darkTheme whether we want the dark or light version of the tool
     * @return the path of the icon representing the tool in the tool box
     */
    String getIconPath(boolean darkTheme);

    /**
     * @return the human-readable name of the tool
     */
    String getHumanName();

    /**
     * @param e          the event to react to
     * @param imageData  the GraphicsContext to be modified
     * @param colorWheel the state of the colorwheel
     */
    void eventHandler(MouseEvent e, ImageData imageData, ColorWheelController colorWheel);

}
