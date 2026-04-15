package ImageApp.ui;

import ImageApp.tools.*;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.Node;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

/**
 * Group of buttons, each associated with a different tool
 */
public class ToolsGrid extends HBox {

    final public SimpleIntegerProperty toolNumber = new SimpleIntegerProperty(0);
    private ToggleGroup group;

    // This array lists all Tool objects possible. To be modified every time
    // a new tool is added
    final private Tool[] listOfTools = {
            new Empty(),
            new Paint(),
            new ColorPicker(),
            new Eraser(),
            new Move(),
            new BucketFill(),
    };

    public ToolsGrid() {

        group = new ToggleGroup();

        // populating the box with buttons
        for (int i = 0; i < listOfTools.length; i++) {
            FileInputStream inputStream;
            try {
                inputStream = new FileInputStream(listOfTools[i].getIconPath(true));

            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
            ToggleButton toggleButton = getToggleButton(i, inputStream, group);

            getChildren().add(toggleButton);
        }
        group.selectToggle(group.getToggles().getFirst());
    }

    public void switchIcons(boolean darkTheme) {
        for (int i = 0; i < listOfTools.length; i++) {
            ToggleButton toggleButton = (ToggleButton) getChildren().get(i);
            FileInputStream inputStream;
            try {
                inputStream = new FileInputStream(listOfTools[i].getIconPath(darkTheme));

            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
            ImageView imgView = new ImageView(new Image(inputStream));
            toggleButton.setGraphic(imgView);
            imgView.setFitHeight(20);
            imgView.setPreserveRatio(true);
        }
    }

    private ToggleButton getToggleButton(int i, FileInputStream inputStream, ToggleGroup group) {
        ImageView imgView = new ImageView(new Image(inputStream));
        imgView.setFitHeight(20);
        imgView.setPreserveRatio(true);
        ToggleButton toggleButton = new ToggleButton(null, imgView);
        toggleButton.setToggleGroup(group);
        toggleButton.setOnAction(
                (actionEvent) -> {
                    Toggle toggle = (Toggle) actionEvent.getSource();
                    if (!toggle.isSelected()) {
                        toggle.setSelected(true);
                    } else {
                        toolNumber.set(i);
                    }
                }
        );
        return toggleButton;
    }

    /**
     * Lets the other components access the id of the current selected tool
     *
     * @return The id of the current tool
     */
    public Tool getCurrentTool() {
        return listOfTools[toolNumber.get()];
    }
}
