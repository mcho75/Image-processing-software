package ImageApp.MenuAction.CustomEffects;

import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

import static java.lang.Math.*;
import static java.lang.Math.min;

public class SeamCarving {

    private WritableImage writableImage;
    private double[][] energies;

    public SeamCarving(WritableImage image, boolean isHorizontal, int nbSeam) {
        writableImage = image;

        if (isHorizontal){
            for (int n = 0; n < nbSeam; n++) {
                energyMatrix();
                int[] seam = findHorizontalSeam(energies);
                removeHorizontalSeam(seam);
            }
        } else {
            energyMatrix();
            for (int n = 0; n < nbSeam; n++) {

                int[] seam = findVerticalSeam(energies);
                removeVerticalSeam(seam);
                energies = updateVerticalEnergies(energies, seam);

            }
        }
    }

    /**
     * @param x,y          coords of pixel to calculate difference for
     * @param isHorizontal true if calculating horizontal difference, else vertical
     * @return the value of the difference squared at pixel x,y
     */
    private double regularDifference(int x, int y, boolean isHorizontal) {
        PixelReader pr = writableImage.getPixelReader();
        Color pixel1, pixel2;
        if (isHorizontal) {
            pixel1 = pr.getColor(x - 1, y);
            pixel2 = pr.getColor(x + 1, y);
        } else {
            pixel1 = pr.getColor(x, y - 1);
            pixel2 = pr.getColor(x, y + 1);
        }
        double dr = pixel1.getRed() - pixel2.getRed();
        double dg = pixel1.getGreen() - pixel2.getGreen();
        double db = pixel1.getBlue() - pixel2.getBlue();
        return pow(dr, 2) + pow(dg, 2) + pow(db, 2);
    }

    /**
     * calculates finite difference functions for the edges
     *
     * @param x,y          coords of pixel to calculate difference for
     * @param isHorizontal true if calculating horizontal difference, else vertical
     * @param isForward    true if calculating forward difference, else backward
     * @return the value of forward/backward difference squared at pixel x,y
     */
    private double forwdBackwdDifference(int x, int y, boolean isHorizontal, boolean isForward) {
        Color pixelx, pixel1, pixel2;
        PixelReader pr = writableImage.getPixelReader();
        pixelx = pr.getColor(x, y);
        int delta = (isForward) ? 1 : -1;
        if (isHorizontal) {
            pixel1 = pr.getColor(x + delta, y);
            pixel2 = pr.getColor(x + 2 * delta, y);
        } else {
            pixel1 = pr.getColor(x, y + delta);
            pixel2 = pr.getColor(x, y + 2 * delta);
        }
        double dr = -3 * pixelx.getRed() + 4 * pixel1.getRed() - pixel2.getRed(); //
        double dg = -3 * pixelx.getGreen() + 4 * pixel1.getGreen() - pixel2.getGreen();
        double db = -3 * pixelx.getBlue() + 4 * pixel1.getBlue() - pixel2.getBlue();
        return pow(dr, 2) + pow(dg, 2) + pow(db, 2);
    }

    /**
     * a 2-dimensional matrix storing the energy value of each pixel. x and y are flipped for simplicity reasons later on.
     */
    private void energyMatrix() {
        int height = (int) writableImage.getHeight();
        int width = (int) writableImage.getWidth();
        double[][] newEnergies = new double[height][width];

        // all 4 edges and corners of the image (x=0; x=width-1; y=0; y=height-1) require their own formulas

        // corners
        boolean[] a = {true, false};
        int edgeX = 0;
        for (boolean isForwardX : a) {
            int edgeY = 0;
            for (boolean isForwardY : a) {
                double dx2 = forwdBackwdDifference(edgeX, edgeY, true, isForwardX);
                double dy2 = forwdBackwdDifference(edgeX, edgeY, false, isForwardY);
                newEnergies[edgeY][edgeX] = 255 * sqrt(dx2 + dy2);
                edgeY = height - 1; // switch to lower edge
            }
            edgeX = width - 1; // switch to right edge
        }

        // vertical edges - dx2 calculated with forward/backward difference function
        int x = 0;
        for (boolean isForward : a) {
            for (int y = 1; y < height - 1; y++) {// delta = 1: edge x=0; delta = -1: edge x=width-1
                double dx2 = forwdBackwdDifference(x, y, true, isForward);
                double dy2 = regularDifference(x, y, false);
                newEnergies[y][x] = 255 * sqrt(dx2 + dy2);
            }
            x = width - 1;
        }
        // horizontal edges
        for (x = 1; x < width - 1; x++) {
            int y = 0;
            for (boolean isForward : a) {
                double dx = regularDifference(x, y, true);
                double dy = forwdBackwdDifference(x, y, false, isForward);

                newEnergies[y][x] = 255 * sqrt(dx + dy);
                y = height - 1; // goes to backward difference function for lower edge
            }
        }

        // rest of the matrix
        for (x = 1; x < width - 1; x++) {
            for (int y = 1; y < height - 1; y++) {
                double dx2 = regularDifference(x, y, true);
                double dy2 = regularDifference(x, y, false);
                newEnergies[y][x] = 255 * sqrt(dx2 + dy2);
            }
        }
        energies = newEnergies;
    }

    /**
     * @param i  horizontal index of current pixel
     * @param i1 value to compare
     * @param i2 value to compare
     * @param i3 value to compare
     * @return the index of the previous minimum among the three parent pixels during backtracking
     */
    private int previousMinIndex(int i, double i1, double i2, double i3) {
        if (i1 <= i2 && i1 <= i3) return i - 1;
        else if (i2 < i1 && i2 <= i3) return i;
        else return i + 1;
    }

    /**
     * @param energies energy matrix, height*width
     * @return int Array of size equal to the height of the image, where Arr[y] corresponds to the x-coordinates of the
     * seam at height y
     */
    private int[] findVerticalSeam(double[][] energies) {
        int height = (int) writableImage.getHeight();
        int width = (int) writableImage.getWidth();

        double[][] M = new double[height][width];

        M[0] = energies[0];

        // cumulative energy matrix for dynamic programming
        for (int i = 1; i < height; i++) {
            M[i][0] = energies[i][0] + min(M[i - 1][0], M[i - 1][1]);
            M[i][width - 1] = energies[i][width - 1] + min(M[i - 1][width - 2], M[i - 1][width - 1]);
            for (int j = 1; j < width - 1; j++) {
                M[i][j] = energies[i][j] + min(M[i - 1][j - 1], min(M[i - 1][j], M[i - 1][j + 1]));
            }
        }

        // backtracking to find minimum cumulative energy seam

        int[] seam = new int[height];
        // initialise at first value
        double minValue = M[height - 1][0];

        // Find lower row pixel of lowest cumulative energy.
        // If multiple pixels have the same cumulative energy, the leftmost one is selected.
        int seamEndIndex = 0;
        for (int j = 0; j < width; j++) {
            if (M[height - 1][j] < minValue) {
                minValue = M[height - 1][j];
                seamEndIndex = j;
            }
        }
        seam[height - 1] = seamEndIndex;

        // use of switch cases didn't seem relevant here
        for (int i = height - 2; i >= 0; i--) {
            //edges handled separately
            if (seam[i + 1] == 0) {
                if (M[i][0] <= M[i][1]) {
                    seam[i] = 0;
                } else {
                    seam[i] = 1;
                }
            } else if (seam[i + 1] == width - 1) {
                if (M[i][width - 2] <= M[i][width - 1]) {
                    seam[i] = width - 2;
                } else {
                    seam[i] = width - 1;
                }
            } else {
                // set to pixel of minimal energy among the three pixels above it
                seam[i] = previousMinIndex(seam[i + 1], M[i][seam[i + 1] - 1], M[i][seam[i + 1]], M[i][seam[i + 1] + 1]);
            }
        }
        return seam;
    }

    /**
     * @param energies energy matrix, height*width
     * @return int Array of size equal to the width of the image, where Arr[x] corresponds to the y-coordinates of the
     * seam at width x
     */
    private int[] findHorizontalSeam(double[][] energies){
        int width = (int) writableImage.getWidth();
        int height = (int) writableImage.getHeight();

        double[][] M = new double[width][height];

        for (int y=0; y<height; y++){
            M[0][y]=energies[y][0];
        }

        // cumulative energy matrix for dynamic programming
        for (int x = 1; x < width; x++) {
            M[x][0] = energies[0][x] + min(M[x - 1][0], M[x - 1][1]);
            M[x][height - 1] = energies[height - 1][x] + min(M[x - 1][height - 2], M[x - 1][height - 1]);
            for (int y = 1; y < height - 1; y++) {
                M[x][y] = energies[y][x] + min(M[x - 1][y-1], min(M[x-1][y], M[x - 1][y + 1]));
            }
        }
        // backtracking to find minimum cumulative energy seam

        int[] seam = new int[width];
        // initialise at first value
        double minValue = M[width - 1][0];
        // Find lower row pixel of lowest cumulative energy.
        // If multiple pixels have the same cumulative energy, the leftmost one is selected.
        int seamEndIndex = 0;
        for (int y = 0; y < height; y++) {
            if (M[width - 1][y] < minValue) {
                minValue = M[width - 1][y];
                seamEndIndex = y;
            }
        }
        seam[width-1]=seamEndIndex;
        // use of switch cases didn't seem relevant here
        for (int x = width - 2; x >= 0; x--) {
            //edges handled separately
            if (seam[x + 1] == 0) {
                if (M[x][0] <= M[x][1]) {
                    seam[x] = 0;
                } else {
                    seam[x] = 1;
                }
            } else if (seam[x + 1] == height - 1) {
                if (M[x][height - 2] <= M[x][height - 1]) {
                    seam[x] = height - 2;
                } else {
                    seam[x] = height - 1;
                }
            } else {
                // set to pixel of minimal energy among the three pixels above it
                seam[x] = previousMinIndex(seam[x + 1], M[x][seam[x + 1] - 1], M[x][seam[x + 1]], M[x][seam[x + 1] + 1]);
            }
        }
        return seam;
    }

    /**
     *
     * @param previousEnergies energy matrix of size height * (width+1) to update
     * @param seam             int array giving x-coord of seam at index y
     * @return a new energy matrix
     */
    private double[][] updateVerticalEnergies(double[][] previousEnergies, int[] seam) {
        // only updates energies just before the seam and on the pixel replacing it
        // uses native copy (arraycopy), faster than for loops for manual copying
        final int width = (int) writableImage.getWidth();
        int height = (int) writableImage.getHeight();
        double[][] energies = new double[height][width];

        // top and bottom rows
        boolean isForwardY = true;
        for (int y : new int[]{0, height - 1}) {
            System.arraycopy(previousEnergies[y], 0, energies[y], 0, seam[y]);
            double dx;
            double dy;
            int x0 = seam[y];

            // trick to make switch cases usable, can't use case width: (...) since width isn't constant
            if (seam[y] >= width - 1) {
                x0 -= width + 1; // = -1 if width, =-2 if width-1
            }
            int length = width - seam[y] - 1; // length to copy

            // switch cases to avoid redundancy for horizontal differences
            switch (x0) {
                case 1:
                    dx = regularDifference(1, y, true);
                    dy = forwdBackwdDifference(1, y, false, isForwardY);
                    energies[y][1] = 255 * sqrt(dx + dy);
                case 0:
                    dx = forwdBackwdDifference(0, y, true, true);
                    dy = forwdBackwdDifference(0, y, false, isForwardY);
                    energies[y][0] = 255 * sqrt(dx + dy);
                    System.arraycopy(previousEnergies[y], seam[y] + 2, energies[y], seam[y] + 1, length);
                    break;
                case -2:
                    dx = regularDifference(width - 2, y, true);
                    dy = forwdBackwdDifference(width - 2, y, false, isForwardY);
                    energies[y][width - 2] = 255 * sqrt(dx + dy);
                case -1:
                    dx = forwdBackwdDifference(width - 1, y, true, false);
                    dy = forwdBackwdDifference(width - 1, y, false, isForwardY);
                    energies[y][width - 1] = 255 * sqrt(dx + dy);
                    break;
                default:
                    double dx1 = regularDifference(x0 - 1, y, true);
                    double dy1 = forwdBackwdDifference(x0 - 1, y, false, isForwardY);
                    double dx2 = regularDifference(x0, y, true);
                    double dy2 = forwdBackwdDifference( x0, y, false, isForwardY);
                    energies[y][x0 - 1] = 255 * sqrt(dx1 + dy1);
                    energies[y][x0] = 255 * sqrt(dx2 + dy2);
                    System.arraycopy(previousEnergies[y], seam[y] + 1, energies[y], seam[y], length);
            }
            isForwardY = false;
        }

        // rest of the matrix
        for (int y = 1; y < height - 1; y++) {
            System.arraycopy(previousEnergies[y], 0, energies[y], 0, seam[y]);
            double dx;
            double dy;
            int x0 = seam[y];

            // trick to make switch cases usable, can't use case width: (...) since width isn't constant
            if (seam[y] >= width - 1) {
                x0 -= width + 1; // x0= -1 if seam[y]==width, x0=-2 if pS[y]== width-1
            }

            int length = width - seam[y] - 1; // length to copy

            // switch cases to avoid redundancy
            switch (x0) {
                case 1:
                    dx = regularDifference(1, y, true);
                    dy = regularDifference(1, y, false);
                    energies[y][1] = 255 * sqrt(dx + dy);
                case 0:
                    dx = forwdBackwdDifference(0, y, true, true);
                    dy = regularDifference(0, y, false);
                    energies[y][0] = 255 * sqrt(dx + dy);
                    System.arraycopy(previousEnergies[y], seam[y] + 1, energies[y], seam[y], length);
                    break;
                case -2:
                    dx = regularDifference(width - 2, y, true);
                    dy = regularDifference(width - 2, y, false);
                    energies[y][width - 2] = 255 * sqrt(dx + dy);
                case -1:
                    dx = forwdBackwdDifference(width - 1, y, true, false);
                    dy = regularDifference(width - 1, y, false);
                    energies[y][width - 1] = 255 * sqrt(dx + dy);
                    break;
                default:
                    double dx1 = regularDifference(x0 - 1, y, true);
                    double dy1 = regularDifference(x0 - 1, y, false);
                    double dx2 = regularDifference(x0, y, true);
                    double dy2 = regularDifference(x0, y, false);
                    energies[y][x0 - 1] = 255 * sqrt(dx1 + dy1);
                    energies[y][x0] = 255 * sqrt(dx2 + dy2);
                    System.arraycopy(previousEnergies[y], seam[y] + 2, energies[y], seam[y] + 1, length);
            }
        }
        return energies;
    }

    /**
     * in-place removal of the seam
     * @param seam   int Array of size equal to the height of the image, where Arr[y] corresponds to the x-coordinates of
     *               the seam at height y
     */
    private void removeVerticalSeam( int[] seam) {
        PixelReader pr = writableImage.getPixelReader();
        int width = (int) writableImage.getWidth();
        int height = (int) writableImage.getHeight();

        WritableImage newImage = new WritableImage(width - 1, height);
        PixelWriter pw = newImage.getPixelWriter();

        // translate all pixels after the seam to the right
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < seam[y]; x++) {
                pw.setColor(x, y, pr.getColor(x, y));
            }
            for (int x = seam[y]; x < width - 1; x++) {
                pw.setColor(x, y, pr.getColor(x + 1, y));
            }
        }
        writableImage = newImage;
    }

    /**
     * in-place removal of the seam
     *
     * @param seam   int Array of size equal to the width of the image, where Arr[x] corresponds to the y-coordinates of
     *               the seam at width x
     */
    private void removeHorizontalSeam(int[] seam) {
        PixelReader pr = writableImage.getPixelReader();
        int width = (int) writableImage.getWidth();
        int height = (int) writableImage.getHeight();

        WritableImage newImage = new WritableImage(width, height - 1);
        PixelWriter pw = newImage.getPixelWriter();

        // translate all pixels after the seam upwards
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < seam[x]; y++) {
                pw.setColor(x, y, pr.getColor(x, y));
            }
            for (int y = seam[x]; y < height - 1; y++) {
                pw.setColor(x, y, pr.getColor(x, y + 1));
            }
        }
        writableImage = newImage;
    }

    public WritableImage getResult() {
        return writableImage;
    }
}
