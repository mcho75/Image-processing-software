package ImageApp.MenuAction;

import ImageApp.data.ImageData;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.image.Image;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javax.imageio.ImageIO;
import java.io.*;
import java.util.*;

public class FileActions {

    private record NamedImage(String name, Image image){}

    public static void newProject(ImageData imageData, int sizeX, int sizeY) {
        imageData.initialize(sizeX, sizeY);
    }

    /** creates a new project with just an image
     * @param imageData the data we are manipulating
     * @param stage the app instance
     */
    public static boolean newImage(ImageData imageData, Stage stage) {
        NamedImage namedImage = loadImage(stage);
        if (namedImage == null) {
            return false;
        }
        imageData.initialize(namedImage.image, namedImage.name);
        return true;
    }

    /** set imageData to a previously saved state
     * @param imageData the data we are manipulating
     * @param stage the app instance
     */
    public static boolean openProject(ImageData imageData, Stage stage) {
        if (imageData.isSaved() || savePopUp(imageData, stage)) {

            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Open Project File");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Project Data (.idfx)", "*.idfx"));
            fileChooser.setInitialDirectory(new File(System.getProperty("user.home", null)));
            File selectedFile = fileChooser.showOpenDialog(stage);
            if (selectedFile != null) {
                return imageData.openFromDisk(selectedFile);
            }
        }
        return false;
    }

    /** save the drawing area to a File
     * @param imageData the data we are manipulating
     * @param stage the app instance
     * @return whether saving was successful
     */
    public static boolean save(ImageData imageData, Stage stage) {
        if (imageData.getFile() != null) {
            return imageData.saveToDisk();
        } else {
            return saveAs(imageData, stage);
        }
    }

    /** ask the user for place to save the work to
     * @param imageData the data we are manipulating
     * @param stage the app instance
     * @return whether saving was successful
     */
    public static boolean saveAs(ImageData imageData, Stage stage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Project File");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Project Data (.idfx)", "*.idfx"));
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home", null)));
        fileChooser.setInitialFileName("Untitled.idfx");
        File selectedFile = fileChooser.showSaveDialog(stage);
        if (selectedFile == null) {
            return false;
        } else {
            imageData.setFile(selectedFile);
            return imageData.saveToDisk();
        }
    }

    /** closes the main window
     * @param imageData the data we are manipulating
     * @param stage the app instance
     */
    public static void close(ImageData imageData, Stage stage) {
        // we use || to enable short-circuit evaluation :
        // no need for the popup if the work is already saved
        if (imageData.isSaved() || savePopUp(imageData, stage)) {
            stage.close();
        }
    }

    /** exports the project to an image
     * @param imageData the data we are manipulating
     * @param stage the app instance
     */
    public static void exportImage(ImageData imageData, Stage stage) {

        // we retrieve the list of all supported formats and remove the uppercase variants
        ArrayList<String> format = new ArrayList<>(List.of(ImageIO.getWriterFormatNames()));
        format.removeIf(
                (extension) -> (extension.equals(extension.toUpperCase()))
        );

        // we ask the user for the location of the resulting file
        FileChooser fileChooser = new FileChooser();
        for (String f : format) {
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(f, "*." + f));
        }
        fileChooser.setTitle("Save Project File");
        fileChooser.setInitialFileName("Untitled");
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home", null)));

        File selectedFile = fileChooser.showSaveDialog(stage);
        if (selectedFile != null) {
            String extension = fileChooser.getSelectedExtensionFilter().getExtensions().getFirst().substring(2);
            if (!(selectedFile.getName().endsWith("." + extension))) {
                selectedFile = new File(selectedFile.getAbsolutePath() + "." + extension);
            }

            imageData.export(selectedFile, extension);
        }
    }

    /**
     * import an image as a new layer
     *
     * @param imageData the data we are manipulating
     * @param stage     the app instance
     */
    public static void importImage(ImageData imageData, Stage stage) {
        NamedImage namedImage = loadImage(stage);
        if (namedImage == null) {
            return;
        }
        imageData.importImage(namedImage.image, namedImage.name);
    }

    private static NamedImage loadImage(Stage stage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Import an image");
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home", null)));
        fileChooser.setSelectedExtensionFilter(
                new FileChooser.ExtensionFilter("Supported Image Format", ImageIO.getWriterFormatNames())
        );

        File selectedFile = fileChooser.showOpenDialog(stage);
        if (selectedFile == null) {
            return null;
        }

        try (FileInputStream fileInputStream = new FileInputStream(selectedFile)) {
            return new NamedImage(selectedFile.getName(), new Image(fileInputStream));
        } catch (IOException e) {
            Alert alert = new Alert(AlertType.ERROR, "Error while reading file : %s".formatted(e.getLocalizedMessage()));
            alert.show();
            return null;
        }
    }

    /** in case there is unsaved work, ask the user whether to save it or cancel
     * @param imageData the data we are manipulating
     * @param stage the app instance
     * @return whether the action should proceed
     */
    private static boolean savePopUp(ImageData imageData, Stage stage){
        // creation of the alert window
        Alert saveAlert = new Alert(AlertType.WARNING);
        saveAlert.setTitle("Save");
        saveAlert.setHeaderText("Do you want to save the current project before closing it?");

        // creation of the alert content
        ButtonType yesButton = new ButtonType("Yes", ButtonData.YES);
        ButtonType noButton = new ButtonType("No",ButtonData.NO);
        ButtonType cancelButton = new ButtonType("Cancel", ButtonData.CANCEL_CLOSE);
        saveAlert.getButtonTypes().setAll(yesButton, noButton, cancelButton);

        // parse the answer of the user
        Optional<ButtonType> result = saveAlert.showAndWait();
        ButtonData popupResult = result.orElse(ButtonType.CLOSE).getButtonData();

        // react accordingly to user decision
        return switch (popupResult) {
            case ButtonData.YES -> save(imageData, stage);
            case ButtonData.NO -> true;
            case ButtonData.CANCEL_CLOSE -> false;
            case null -> throw new NullPointerException("popup result is null");
            default ->
                    throw new RuntimeException("Popup result invalid : %s".formatted(popupResult.toString()));
        };
    }
}


