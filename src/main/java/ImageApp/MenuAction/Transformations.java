package ImageApp.MenuAction;

import ImageApp.data.ImageData;
import ImageApp.data.Layer;
import ImageApp.ui.ResizeDialog;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextInputDialog;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.transform.Affine;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Translate;

import java.util.Optional;

public class Transformations {

    /**
     * changes the size and position of a layer
     * @param imageData the data to manipulate
     * @param newWidth the new width to set
     * @param newHeight the new height to set
     */
    public static void resize(ImageData imageData, double newWidth, double newHeight) {
        Layer layer = imageData.getCurrentLayer();
        SnapshotParameters params = new SnapshotParameters();
        params.setFill(Color.TRANSPARENT);
        Image base = layer.snapshot(params, null);
        Affine affine = new Affine();
        affine.append(new Scale(newWidth / layer.getWidth(), newHeight / layer.getHeight()));
        layer.setWidth(newWidth);
        layer.setHeight(newHeight);
        layer.getGraphicsContext2D().clearRect(0, 0, newWidth, newHeight);
        layer.getGraphicsContext2D().save();
        layer.getGraphicsContext2D().setTransform(affine);
        layer.getGraphicsContext2D().drawImage(base, 0, 0);
        layer.getGraphicsContext2D().restore();
    }

    /**
     * applies a horizontal flip
     * @param imageData the data to manipulate
     */
    public static void hflipTrans(ImageData imageData) {
        Affine affine = new Affine();
        affine.append(new Translate(imageData.getCurrentLayer().getWidth(), 0));
        affine.append(new Scale(-1, 1));
        applyAffine(imageData, affine);
    }

    /** applies a vertical flip
     * @param imageData the canvas to be modified
     */
    public static void vflipTrans(ImageData imageData) {
        Affine affine = new Affine();
        affine.append(new Translate(0, imageData.getCurrentLayer().getHeight()));
        affine.append(new Scale(1, -1));
        applyAffine(imageData, affine);
    }

    /**
     * prompts the user for the rotation angle desired and applies it
     * @param imageData the data to manipulate
     */
    public static void rotate(ImageData imageData) {
        // asking the user for the angle
        TextInputDialog textInputDialog = new TextInputDialog("0");
        textInputDialog.setHeaderText("Rotation angle:");
        textInputDialog.showAndWait();

        // parsing the user's answer
        try {
            Double angle = Double.valueOf(textInputDialog.getResult());
            rotate(imageData, angle);
        } catch (NumberFormatException e) {
            // in case the user's answer cannot be interpreted as a number
            Alert alert = new Alert(Alert.AlertType.ERROR, "Invalid number");
            alert.show();
        }
    }

    /** applies the given rotation angle
     * @param imageData the target to be modifed
     * @param angle the angle by which to rotate the canva. can be negative
     */
    public static void rotate(ImageData imageData, Double angle) {
        Layer layer = imageData.getCurrentLayer();

        Affine affine = new Affine();
        affine.append(new Translate(layer.getWidth() / 2, layer.getHeight() / 2));
        affine.append(new Rotate(angle));
        affine.append(new Translate(- layer.getWidth() / 2, - layer.getHeight() / 2));

        applyAffine(imageData, affine);
    }

    /** we apply any affine transformation to the whole image
     * @param imageData the data to be modified
     * @param affine the affine transform to be applied
     */
    private static void applyAffine(ImageData imageData, Affine affine) {
        // saving image pixels
        SnapshotParameters params = new SnapshotParameters();
        params.setFill(Color.TRANSPARENT);
        Image base = imageData.getCurrentLayer().snapshot(params, null);

        // saving canvas' config
        GraphicsContext gc = imageData.getCurrentLayer().getGraphicsContext2D();

        imageData.beginTransaction();
        gc.save();

        // resetting the canvas
        gc.clearRect(0, 0, imageData.getCurrentLayer().getWidth(), imageData.getCurrentLayer().getHeight());

        // applying the transform
        gc.setTransform(affine);
        gc.drawImage(base, 0, 0);

        // restoring the canvas' config
        gc.restore();
        imageData.endTransaction();
    }

}
