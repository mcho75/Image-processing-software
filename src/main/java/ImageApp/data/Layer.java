package ImageApp.data;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.effect.BlendMode;
import javafx.scene.image.Image;
import javafx.scene.image.PixelFormat;
import javafx.scene.paint.Color;
import java.io.*;

public class Layer extends Canvas implements Serializable {

    private transient StringProperty name = new SimpleStringProperty("Layer");

    public Layer(int width, int height, String nameLayer, boolean transparent) {
        super(width, height);
        name.set(nameLayer);
        if (! transparent) {
            getGraphicsContext2D().setFill(Color.WHITE);
            getGraphicsContext2D().fillRect(0, 0, width, height);
        }
        getGraphicsContext2D().setImageSmoothing(false);
    }

    public Layer(Image image, String imageName) {
        super(image.getWidth(), image.getHeight());
        name.set(imageName);
        getGraphicsContext2D().drawImage(image, 0, 0);
        getGraphicsContext2D().setImageSmoothing(false);
    }

    public void moveToPosition(double x, double y) {
        setTranslateX(x);
        setTranslateY(y);
    }

    public StringProperty nameProperty() {
        return name;
    }

    public void setName(String newName) {
        name.set(newName);
    }

    public String getName() {
        return name.get();
    }

    public byte[] toByteArray() {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try {
            ObjectOutputStream outputStream = new ObjectOutputStream(byteArrayOutputStream);
            outputStream.writeObject(this);
            outputStream.close();
            byteArrayOutputStream.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return byteArrayOutputStream.toByteArray();
    }

    public static Layer fromByteArray(byte[] bytes) {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
        try {
            ObjectInputStream inputStream = new ObjectInputStream(byteArrayInputStream);
            return (Layer) inputStream.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String toString() {
        return name.get();
    }

    @Serial
    private void readObject(ObjectInputStream in) throws ClassNotFoundException, IOException {
        in.defaultReadObject();

        int width = in.readInt();
        int height = in.readInt();
        int translateX = in.readInt();
        int translateY = in.readInt();
        double opacity = in.readDouble();
        Object blendMode = in.readObject();
        String newName = in.readUTF();

        setWidth(width);
        setHeight(height);
        setOpacity(opacity);
        setBlendMode((BlendMode) blendMode);
        name = new SimpleStringProperty(newName);

        getGraphicsContext2D()
                .getPixelWriter()
                .setPixels(
                        0,
                        0,
                        width,
                        height,
                        PixelFormat.getByteBgraInstance(), in.readNBytes(width * height * 4), 0, width * 4);
        moveToPosition(translateX, translateY);

    }

    @Serial
    private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();

        // we acquire necessary properties of the Layer
        int width = (int) getWidth();
        int height = (int) getHeight();
        int translateX = (int) getTranslateX();
        int translateY = (int) getTranslateY();
        double opacity = getOpacity();
        Object blendMode = getBlendMode();
        String newName = name.get();

        // we use a buffer for better performance
        byte[] buffer = new byte[width * height * 4];

        // we want only the pixels, with no backgrounds
        SnapshotParameters params = new SnapshotParameters();
        params.setFill(Color.TRANSPARENT);

        // a Canvas must be visible to be snapshot
        boolean visibleState = isVisible();
        setVisible(true);
        snapshot(params, null)
                .getPixelReader()
                .getPixels(0, 0, width, height, PixelFormat.getByteBgraInstance(), buffer, 0, width * 4);
        setVisible(visibleState);

        // we write all the data (order matters)
        out.writeInt(width);
        out.writeInt(height);
        out.writeInt(translateX);
        out.writeInt(translateY);
        out.writeDouble(opacity);
        out.writeObject(blendMode);
        out.writeUTF(newName);
        out.write(buffer);

    }
}
