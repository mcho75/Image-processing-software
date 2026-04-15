package ImageApp.MenuAction.CustomEffects;

import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import org.junit.jupiter.api.Test;

import static java.lang.Math.floor;
import static org.junit.jupiter.api.Assertions.*;

class SeamCarvingTest{

    @Test
    public void transparentTest() {
        WritableImage transparentImage = new WritableImage(10, 10);
        SeamCarving seamCarving = new SeamCarving(transparentImage, true, 2);
        transparentImage = seamCarving.getResult();
        assertEquals(8, transparentImage.getHeight());
        assertEquals(10, transparentImage.getWidth());
        assertEquals(Color.TRANSPARENT, transparentImage.getPixelReader().getColor(0, 0));
    }

    /**
     * checks that the algorithm removes seam away from a line of coloured pixels
     */
    @Test
    public void lineTest() {
        WritableImage lineImage = new WritableImage(7, 7);
        for (int x = 0; x < 7; x++) {
            for (int y = 0; y < 7; y++) {
                // given that the derivatives are calculated using neighbour pixels, 1-pixel wide line wouldn't work
                Color color = (x == 3 | x==4) ? Color.CHARTREUSE : Color.WHITE; // a single line wouldn't work
                lineImage.getPixelWriter().setColor(x, y, color);
            }
        }
        SeamCarving seamCarving = new SeamCarving(lineImage, false, 1);
        lineImage = seamCarving.getResult();
        assertEquals(6, lineImage.getWidth());
        assertEquals(7, lineImage.getHeight());
        assertEquals(Color.WHITE, lineImage.getPixelReader().getColor(0, 0));
        assertEquals(Color.CHARTREUSE, lineImage.getPixelReader().getColor(2, 1));

        lineImage = new WritableImage(7,7);
        for (int x = 0; x < 7; x++) {
            for (int y = 0; y < 7; y++) {
                // given that the derivatives are calculated using neighbour pixels, 1-pixel wide line wouldn't work
                Color color = (y == 3 | y==4) ? Color.RED : Color.BLUE; // a single line wouldn't work
                lineImage.getPixelWriter().setColor(x, y, color);
            }
        }
        seamCarving = new SeamCarving(lineImage, true, 1);
        lineImage = seamCarving.getResult();
        assertEquals(7, lineImage.getWidth());
        assertEquals(6, lineImage.getHeight());
        assertEquals(Color.BLUE, lineImage.getPixelReader().getColor(0, 0));
        assertEquals(Color.RED, lineImage.getPixelReader().getColor(0, 2));
    }

    /**
     * checks algorithm towards the edges
     */
    @Test
    public void edgeGradientTest(){ // increasing gradient test : lowest gradient should be to the left or at the top
        WritableImage gradientImage = new WritableImage(11,7);
        int[] gradientArray = new int[11];
        for (int x = 0; x < 11; x++) {
            for (int y = 0; y < 7; y++) {
                int shadeOfBlue = (int)floor((1/100.0)*x*x*255.0);
                Color color = Color.rgb(0, 0, shadeOfBlue);
                gradientArray[x]= shadeOfBlue;
                gradientImage.getPixelWriter().setColor(x, y, color);
            }
        }
        SeamCarving seamCarving = new SeamCarving(gradientImage, false, 1);
        gradientImage = seamCarving.getResult();

        assertNotEquals(Color.BLACK, gradientImage.getPixelReader().getColor(0, 0));
        for (int x=0; x<10; x++){
            Color color = Color.rgb(0,0,gradientArray[x+1]);
            assertEquals(color, gradientImage.getPixelReader().getColor(x,0));
        }

        gradientImage = new WritableImage(7,11);
        for (int x = 0; x < 7; x++) {
            for (int y = 0; y < 11; y++) {
                int shadeOfBlue = (int)floor((1/100.0)*y*y*255.0);
                Color color = Color.rgb(0, 0, shadeOfBlue);
                gradientArray[y]= shadeOfBlue;
                gradientImage.getPixelWriter().setColor(x, y, color);
            }
        }
        seamCarving = new SeamCarving(gradientImage, true, 2);
        gradientImage = seamCarving.getResult();

        assertNotEquals(Color.BLACK, gradientImage.getPixelReader().getColor(0, 0));
        for (int y=0; y<9; y++){
            Color color = Color.rgb(0,0,gradientArray[y+2]);
            assertEquals(color, gradientImage.getPixelReader().getColor(0,y));
    }
    }

    /**
     * checks detection of straight seam
     */
    @Test
    public void centeredGradientTest(){
        WritableImage gradientImage = new WritableImage(11,7);
        int[] gradientArray = new int[11];
        for (int x = 0; x < 11; x++) {
            for (int y = 0; y < 7; y++) {
                // quadratic function centered around x=5; lowest gradient (0) at x=5
                int shadeOfBlue = (int)floor((1/25.0)*(x-5)*(x-5)*255.0);
                Color color = Color.rgb(0, 0, shadeOfBlue);
                gradientArray[x]= shadeOfBlue;
                gradientImage.getPixelWriter().setColor(x, y, color);
            }
        }
        SeamCarving seamCarving = new SeamCarving(gradientImage, false, 1);
        gradientImage = seamCarving.getResult();

        assertNotEquals(Color.BLACK, gradientImage.getPixelReader().getColor(5, 0));
        for (int x=0; x<5; x++){
            // check if left side is identical
            Color color = Color.rgb(0,0,gradientArray[x]);
            assertEquals(color, gradientImage.getPixelReader().getColor(x,0));
            // check if right side is moved to the left by 2 pixels
            color = Color.rgb(0,0,gradientArray[x+6]);
            assertEquals(color, gradientImage.getPixelReader().getColor(x+5,0));
        }

        gradientImage = new WritableImage(7,11);
        for (int x = 0; x < 7; x++) {
            for (int y = 0; y < 11; y++) {
                int shadeOfBlue = (int)floor((1/25.0)*(y-5)*(y-5)*255.0);
                Color color = Color.rgb(0, 0, shadeOfBlue);
                gradientArray[y]= shadeOfBlue;
                gradientImage.getPixelWriter().setColor(x, y, color);
            }
        }
        seamCarving = new SeamCarving(gradientImage, true, 2);
        gradientImage = seamCarving.getResult();

        assertNotEquals(Color.BLACK, gradientImage.getPixelReader().getColor(0, 0));
        for (int y=0; y<4; y++){
            // top identical
            Color color = Color.rgb(0,0,gradientArray[y]);
            assertEquals(color, gradientImage.getPixelReader().getColor(4,y));
            // bottom moved 2 pixels upwards
            color = Color.rgb(0,0, gradientArray[y+6]);
            assertEquals(color, gradientImage.getPixelReader().getColor(4,y+4));
        }
        assertEquals(Color.BLUE, gradientImage.getPixelReader().getColor(0,8));
    }

    /**
     * checks if the seam can follow the diagonal
     */
    @Test
    public void blackDiagonalTest(){
        WritableImage diagImage = new WritableImage(11,11);
        for (int x = 0; x < 11; x++) {
            for (int y = 0; y < 11; y++) {
                // black for x=y, lowest gradient
                int shadeOfBlue = (int)floor((1/100.0)*(x-y)*(x-y)*255.0);
                Color color = Color.rgb(0, 0, shadeOfBlue);
                diagImage.getPixelWriter().setColor(x, y, color);
            }
        }
        SeamCarving seamCarving = new SeamCarving(diagImage, false, 1);
        diagImage = seamCarving.getResult();
        for (int x=0; x<10; x++){
            for (int y=0; y<10; y++) {
                // black pixels should be removed
                assertNotEquals(Color.BLACK, diagImage.getPixelReader().getColor(x, y));
            }
        }
    }

    /**
     * in case it only worked for black diagonals
     */
    @Test
    public void colourDiagonalTest(){
        WritableImage diagImage = new WritableImage(11,11);
        for (int x = 0; x < 11; x++) {
            for (int y = 0; y < 11; y++) {
                // black for x=y
                int shadeOfBlue = (int)floor((1/100.0)*(x-y)*(x-y)*255.0);
                Color color = Color.rgb(125, 123, 255-shadeOfBlue);
                diagImage.getPixelWriter().setColor(x, y, color);
            }
        }
        SeamCarving seamCarving = new SeamCarving(diagImage, false, 1);
        diagImage = seamCarving.getResult();
        for (int x=0; x<10; x++){
            for (int y=0; y<10; y++) {
                // Middle should be removed
                assertNotEquals(Color.rgb(125,123,255), diagImage.getPixelReader().getColor(x, y));
            }
        }
    }

    /**
     * checks whether the algorithm can identify the lowest energy seam when guided:
     * a complex path is given where the gradients will be lowest
     */
    @Test
    public void customPathTest(){
        int[] seam = {3,4,5,4,5,6,7,8,7}; // arbitrary path to follow; lowest gradient will be at x=seam[y]
        WritableImage pathImage = new WritableImage(9,9);
        for (int x = 0; x < 9; x++) {
            for (int y = 0; y < 9; y++) {
                // black pixel and null horizontal gradient for x=seam[y] (nonzero vertical gradient, low nonetheless)
                // path includes an edge pixel (x=8, y=7)
                int shadeOfBlue = (int)floor((1/100.0)*(x-seam[y])*(x-seam[y])*255.0);
                Color color = Color.rgb(0,0, shadeOfBlue);
                pathImage.getPixelWriter().setColor(x, y, color);
            }
        }
        for (int y=0; y<9; y++){
            assertEquals(Color.BLACK, pathImage.getPixelReader().getColor(seam[y], y));
        }
        SeamCarving seamCarving = new SeamCarving(pathImage, false, 1);
        pathImage = seamCarving.getResult();
        for (int x=0; x<8; x++){
            for (int y=0; y<9; y++) {
                // there should be no black pixels
                assertNotEquals(Color.rgb(0,0,50), pathImage.getPixelReader().getColor(x, y));
            }
        }
    }
}

