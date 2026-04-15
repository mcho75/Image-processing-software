package ImageApp.data;

import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

class ImageDataTest {

    @Test
    void setDimensions() {
        ImageData imageData = new ImageData();
        imageData.setDimensions(100, 10);
        assertEquals(100, imageData.getWidth());
        assertEquals(10, imageData.getHeight());
    }


    @Test
    void setFile() {
        ImageData imageData = new ImageData();
        imageData.setFile(new File("bonsoir.txt"));
        assertEquals("bonsoir.txt", imageData.getFile().getPath());
    }

    @Test
    void isSaved() {
        ImageData imageData = new ImageData();
        assertTrue(imageData.isSaved());
    }

    @Test
    void beginTransaction() {
        ImageData imageData = new ImageData();
        imageData.initialize(10, 10);
        assertDoesNotThrow(
                () -> {
                    imageData.beginTransaction();
                    imageData.beginTransaction();
                }
        );
    }

    @Test
    void endTransaction() {
        ImageData imageData = new ImageData();
        assertDoesNotThrow(
                () -> {
                    imageData.endTransaction();
                    imageData.endTransaction();
                }
        );
    }

    @Test
    void getCurrentLayer() {
        ImageData imageData = new ImageData();
        assertNull(imageData.getCurrentLayer());
        imageData.initialize(10, 100);
        assertNotNull(imageData.getCurrentLayer());
        assertEquals(0, imageData.currentLayer.get());
    }

    @Test
    void getLayersList() {
        ImageData imageData = new ImageData();
        assertEquals(0, imageData.getLayersList().size());
        imageData.initialize(10, 10);
        assertEquals(1, imageData.getLayersList().size());
    }
}