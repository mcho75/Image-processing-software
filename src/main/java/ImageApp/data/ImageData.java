package ImageApp.data;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.SnapshotParameters;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.*;
import java.util.*;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class ImageData {

    // layer properties
    public final SimpleIntegerProperty currentLayer = new SimpleIntegerProperty(-1);
    private final ObservableList<Layer> layersList = FXCollections.observableArrayList();

    // project attributes
    private final SimpleIntegerProperty width = new SimpleIntegerProperty();
    private final SimpleIntegerProperty height = new SimpleIntegerProperty();
    private File file = null;
    private boolean saved = true;

    // History
    private final Deque<Transaction> undoHistory = new ArrayDeque<>();
    private final Deque<Transaction> redoHistory = new ArrayDeque<>();
    private byte[] savedState;

    /**
     * @param w the new width of the project
     * @param h the new height of the project
     */
    public void setDimensions(int w, int h) {
        width.set(w);
        height.set(h);
    }

    public int getWidth() {
        return width.get();
    }

    public int getHeight() {
        return height.get();
    }

    public SimpleIntegerProperty widthProperty() {
        return width;
    }

    public SimpleIntegerProperty heightProperty() {
        return height;
    }

    /** sets the file associated with the current work
     * @param f a representation of a file on disk
     */
    public void setFile(File f) {
        file = f;
    }

    /**
     * @return the file associated with the data or null if not given
     */
    public File getFile() {
        return file;
    }

    /**
     * @return whether the work is already saved
     */
    public boolean isSaved() {
        return saved;
    }

    /** we use this function to reset the redo history
     * @param transaction the action to add to the history
     */
    private void addToHistory(Transaction transaction) {
        redoHistory.clear();
        saved = false;
        undoHistory.push(transaction);
    }

    /**
     * prepares for a new transaction to come
     */
    public void beginTransaction() {
        savedState = getCurrentLayer().toByteArray();
    }

    /**
     * build the latest transaction and send it to be registered
     */
    public void endTransaction() {
        if (savedState == null) {
            return;
        }
        Transaction transaction = new Transaction.Modified(currentLayer.get(), savedState, getCurrentLayer().toByteArray());
        addToHistory(transaction);
        savedState = null;
    }

    /** Let the other components access the current layer
     * @return The canvas corresponding to the current layer
     */
    public Layer getCurrentLayer() {
        if (layersList.isEmpty()) {
            return null;
        }
        saved = false;
        return layersList.get(currentLayer.get());
    }

    /** exposes the inner list to allow visual updates when a layer is added or deleted
     * @return the list of Canvas
     */
    public ObservableList<Layer> getLayersList() {
        return layersList;
    }


    /** Creates a new empty layer
     * @param transparent whether this canvas can be seen through
     */
    public void createNewLayer(boolean transparent) {
        Layer layer = new Layer(getWidth(), getHeight(), "Layer " + layersList.size(), transparent);
        layersList.add(currentLayer.get() + 1, layer);
        currentLayer.set(currentLayer.get() + 1);
        addToHistory(new Transaction.Added(currentLayer.get(), layer.toByteArray()));
    }

    /** Deletes the current layer
     */
    public void deleteLayer() {
        saved = false;
        if (layersList.size() > 1) {
            addToHistory(new Transaction.Deleted(currentLayer.get(), getCurrentLayer().toByteArray()));
            layersList.remove(currentLayer.get());
        }
    }

    /**
     * Switches the current layer with its neighbor (+1 or -1)
     * @param moveUp Whether the current must be moved up or down
     */
    public void moveLayer(boolean moveUp) {
        // we calculate the index to exchange with
        int destination = moveUp ? currentLayer.get() - 1 : currentLayer.get() + 1;

        // we return if the destination is not valid
        if (destination < 0 || destination >= layersList.size()) {return;}

        saved = false;

        // the index of the layer to be moved up
        int lowest = Math.min(destination, currentLayer.get());
        addToHistory(new Transaction.MovedUp(lowest));

        // we proceed to the swap
        Layer temp = layersList.get(lowest);
        layersList.remove(lowest);
        layersList.add(lowest + 1, temp);

        currentLayer.set(destination);
    }

    /**
     * set the project to a single image
     */
    public void initialize(Image image, String imageName) {
        setDimensions((int) image.getWidth(), (int) image.getHeight());
        currentLayer.set(-1);
        layersList.setAll(new Layer(image, imageName));
        currentLayer.set(0);
        file = null;
        undoHistory.clear();
        redoHistory.clear();
    }

    /**
     * returns the data to a blank state
     */
    public void initialize(int width, int height) {
        setDimensions(width, height);
        currentLayer.set(-1);
        layersList.setAll(new Layer(width, height, "Layer 0", false));
        currentLayer.set(0);
        file = null;
        undoHistory.clear();
        redoHistory.clear();
    }

    /**
     * sets the imageData back to the last state registered on the stack
     */
    public void undo() {
        if (undoHistory.isEmpty()) {
            return;
        }
        saved = false;
        Transaction transaction = undoHistory.pop();
        redoHistory.push(transaction);
        switch (transaction) {
            case Transaction.Added(int index, byte[] ignored) -> {
                currentLayer.set(index - 1);
                layersList.remove(index);
            }
            case Transaction.Deleted(int index, byte[] layer) -> {
                layersList.add(index, Layer.fromByteArray(layer));
                currentLayer.set(index);

            }
            case Transaction.Modified(int index, byte[] oldLayer, byte[] ignored) -> {
                layersList.set(index, Layer.fromByteArray(oldLayer));
                currentLayer.set(index);
            }
            case Transaction.MovedUp(int index) -> {
                Layer temp = layersList.get(index);
                layersList.remove(index);
                layersList.add(index + 1, temp);
                currentLayer.set(index + 1);
            }
           case null, default -> throw new IllegalStateException("Unexpected value: " + transaction);
        }
    }

    /**
     * reverses the undo actions
     */
    public void redo() {
        if (redoHistory.isEmpty()) {
            return;
        }
        Transaction transaction = redoHistory.pop();
        undoHistory.push(transaction);
        switch (transaction) {
            case Transaction.Deleted(int index, byte[] ignored) -> {
                currentLayer.set(index - 1);
                layersList.remove(index);
            }
            case Transaction.Added(int index, byte[] newLayer) -> {
                layersList.add(index, Layer.fromByteArray(newLayer));
                currentLayer.set(index);
            }
            case Transaction.Modified(int index, byte[] ignored, byte[] newLayer) -> {
                layersList.set(index, Layer.fromByteArray(newLayer));
                currentLayer.set(index);
            }
            case Transaction.MovedUp(int index) -> {
                Layer temp = layersList.get(index);
                layersList.remove(index);
                layersList.add(index + 1, temp);
                currentLayer.set(index + 1);
            }
            case null, default -> throw new IllegalStateException("Unexpected value: " + transaction);
        }
    }

    /**
     * @param file the file to export the image to
     * @param ext the format of the image
     */
    public void export(File file, String ext) {

        final HashSet<String> noAlphaExt = new HashSet<>(
                List.of(new String[]{"jpg", "jpeg", "bmp"})
        );

        Parent parent = layersList.getFirst().getParent();
        SnapshotParameters snapshotParameters = new SnapshotParameters();
        snapshotParameters.setViewport(new Rectangle2D(0, 0, getWidth(), getHeight()));

        BufferedImage bufferedImage = null;
        if (noAlphaExt.contains(ext)) {
            snapshotParameters.setFill(Color.WHITE);
            bufferedImage = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_RGB);
        } else {
            snapshotParameters.setFill(Color.TRANSPARENT);
        }
        WritableImage writableImage1 = parent.snapshot(snapshotParameters, null);
        RenderedImage renderedImage = SwingFXUtils.fromFXImage(writableImage1, bufferedImage);
        try {
            if (!ImageIO.write(renderedImage, ext, file)) {
                throw new IOException("writing returned false");
            }
        } catch (IOException e) {
            System.out.printf("Error : %s %n", e);
        }
    }

    /**
     * @param image the image to be added to the current work as a new layer
     */
    public void importImage(Image image, String imageName) {
        Layer layer = new Layer(image, imageName);
        layersList.add(currentLayer.get() + 1, layer);
        currentLayer.set(currentLayer.get() + 1);
        addToHistory(new Transaction.Added(currentLayer.get(), layer.toByteArray()));
    }

    /** write imageData to a project file
     * @return whether saving was successful
     */
    public boolean saveToDisk() {

        try (FileOutputStream fileOutputStream = new FileOutputStream(file)) {
            GZIPOutputStream gzipOutputStream = new GZIPOutputStream(fileOutputStream);
            ObjectOutputStream out = new ObjectOutputStream(gzipOutputStream);
            out.writeObject(new ProjectArchive(this));
            // it's important to close the streams to avoid archive corruption
            out.close();
            gzipOutputStream.close();
            fileOutputStream.close();
            saved = true;
        } catch (IOException e) {
            System.out.printf("Error : %s %n", e);
        }
        return saved;
    }

    /** reconstructs imageData from a project file
     * @param f the project file
     */
    public boolean openFromDisk(File f) {

        try (FileInputStream fileInputStream = new FileInputStream(f)) {
            GZIPInputStream gzipInputStream = new GZIPInputStream(fileInputStream);
            ObjectInputStream in = new ObjectInputStream(gzipInputStream);

            // recovering the projectArchive
            ProjectArchive projectArchive = (ProjectArchive) in.readObject();

            // closing the streams
            in.close();
            gzipInputStream.close();
            fileInputStream.close();

            // restoring the state of ImageData
            width.set(projectArchive.width);
            height.set(projectArchive.height);
            layersList.setAll(projectArchive.arrayList);
            file = f;
            saved = true;
            currentLayer.set(0);

            return true;

        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

    }

    /**
     * This class is a serializable representation of imageData
     * You can also see it as a proxy class for ImageData
     */
    static class ProjectArchive implements Serializable {
        @Serial
        private static final long serialVersionUID = 20251026L;
        private int width;
        private int height;
        private ArrayList<Layer> arrayList;

        ProjectArchive(ImageData imageData) {
            width = imageData.getWidth();
            height = imageData.getHeight();
            arrayList = new ArrayList<>(imageData.layersList);
        }

        @Serial
        private void readObject(ObjectInputStream in) throws ClassNotFoundException, IOException {
            width = in.readInt();
            height = in.readInt();
            arrayList = new ArrayList<>();
            int nbLayers = in.readInt();
            for (int i = 0; i < nbLayers; i++) {
                arrayList.add((Layer)(in.readObject()));
            }
        }

        @Serial
        private void writeObject(ObjectOutputStream out) throws IOException {
            out.writeInt(height);
            out.writeInt(width);
            out.writeInt(arrayList.size());
            for (Layer layer : arrayList) {
                out.writeObject(layer);
            }
        }

    }
}
