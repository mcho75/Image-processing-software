package ImageApp.MenuAction;

import ImageApp.MenuAction.CustomEffects.MosaicBlur;
import ImageApp.MenuAction.CustomEffects.SeamCarving;
import ImageApp.data.ImageData;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.*;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import java.util.Optional;

public class Effects {

    /**
     * main function for seam carving
     *
     * @param imageData the data to manipulate
     * @param nbSeams the number of seams
     * @param isHorizontal whether the seam carving is done horizontally or not
     */
    public static void seamCarving(ImageData imageData, int nbSeams, boolean isHorizontal) {
        imageData.beginTransaction();

        WritableImage writableImage = imageData.getCurrentLayer().snapshot(null, null);
        SeamCarving seamCarving = new SeamCarving(writableImage, isHorizontal, nbSeams);
        writableImage = seamCarving.getResult();

        imageData.getCurrentLayer().setHeight(writableImage.getHeight());
        imageData.getCurrentLayer().setWidth(writableImage.getWidth());
        imageData.getCurrentLayer().getGraphicsContext2D().drawImage(writableImage, 0, 0);

        imageData.endTransaction();
    }

    public static void mosaic(ImageData imageData, long seed, int nbPoints) {
        int width = (int) imageData.getCurrentLayer().getWidth();
        int height = (int) imageData.getCurrentLayer().getHeight();

        MosaicBlur mosaicBlur = new MosaicBlur(seed, nbPoints, width, height);

        // color each pixel based on its closest neighbor
        imageData.beginTransaction();

        SnapshotParameters parameters = new SnapshotParameters();
        parameters.setFill(Color.TRANSPARENT);
        PixelReader pr = imageData.getCurrentLayer().snapshot(parameters, null).getPixelReader();
        PixelWriter pw = imageData.getCurrentLayer().getGraphicsContext2D().getPixelWriter();
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                int[] closest = mosaicBlur.closest(new int[]{i, j});
                pw.setColor(i, j, pr.getColor(closest[0], closest[1]));
            }
        }
        imageData.endTransaction();
    }

    /**
     * applies a standard gaussian blur with a default radius of 63
     *
     * @param imageData the data to be modified
     */
    public static void gaussianBlur(ImageData imageData) {

        TextInputDialog textInputDialog = new TextInputDialog("10");
        textInputDialog.setHeaderText("Radius (between 0 and 63) :");
        textInputDialog.showAndWait();

        try {
            double radius = Double.parseDouble(textInputDialog.getResult());
            if (radius < 0 || radius > 63) {
                throw new NumberFormatException("Number must be between 0 and 63");
            }
            imageData.beginTransaction();
            imageData.getCurrentLayer().getGraphicsContext2D().applyEffect(new GaussianBlur(radius));
            imageData.endTransaction();
        } catch (NumberFormatException e) {
            // in case the user's answer cannot be interpreted as a number
            Alert alert = new Alert(Alert.AlertType.ERROR, "Invalid number : %s".formatted(e.getLocalizedMessage()));
            alert.show();
        }
    }

    /**
     * turn the layer to black and white (levels of grey)
     *
     * @param imageData the data to be modified
     */
    public static void greyScale(ImageData imageData) {
        imageData.beginTransaction();
        imageData.getCurrentLayer().getGraphicsContext2D().applyEffect(new ColorAdjust(0, -1, 0, 0));
        imageData.endTransaction();
    }
}
