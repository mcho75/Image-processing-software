package ImageApp.MenuAction.CustomEffects;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Random;

public class MosaicBlur {

    protected final int[][] cloud;
    protected final KdTree tree;

    public MosaicBlur(long seed, int nbPoints, int width, int height) {
        // generation of random points
        cloud = new int[nbPoints][2];
        Random x = new Random(seed);
        Random y = new Random(x.nextInt());
        for (int i = 0; i < nbPoints; i++) {
            cloud[i][0] = x.nextInt(width);
            cloud[i][1] = y.nextInt(height);
        }
        tree = createRec(0, cloud.length, 0);
    }

    /**
     * recursive function to populate the tree
     * @param start beginning of the slice to consider
     * @param end end of the slice to consider
     * @param lvl whether we are considering vertical or horizontal
     * @return the tree representing the slice given in input
     */
    protected KdTree createRec(int start, int end, int lvl) {
        KdTree current = new KdTree();
        Arrays.sort(cloud, start, end, new PointComparator(lvl));
        current.value = cloud[(start + end) / 2];
        current.level = lvl;
        if (end - start == 1) {
            current.left = null;
            current.right = null;
            return current;
        }
        if (end - start == 2) {
            current.left = createRec(start, start + 1, 1 - lvl);
            current.right = null;
            return current;
        }
        current.left = createRec(start, (start + end) / 2, 1 - lvl);
        current.right = createRec(((start + end) / 2) + 1, end, 1 - lvl);
        return current;
    }

    public int[] closest(int[] point) {
        return tree.closest(point);
    }

    protected static class KdTree {

        public KdTree right;
        public KdTree left;
        public int[] value;
        public int level;

        KdTree() {
            right = null;
            left = null;
            value = null;
            level = 0;
        }

        /**
         * @param x a first point
         * @param y a second point
         * @return the squared distance between them
         */
        private int dist(int[] x, int[] y) {
            return ((x[0] - y[0]) * (x[0] - y[0])) + ((x[1] - y[1]) * (x[1] - y[1]));
        }

        /**
         * @param x The object which we want the closest neighbor of
         * @return The closest neighbor of x
         */
        public int[] closest(int[] x) {
            if (left == null && right == null) {
                return value;
            }
            int[] closest = value;

            // we go down
            if (x[level] < value[level] && left != null) {
                closest = left.closest(x);
            } else if (right != null) {
                closest = right.closest(x);
            }

            // we intersect the plane
            int[] other;
            if (dist(x, closest) > (x[level] - value[level]) * (x[level] - value[level])) {
                if (x[level] < value[level] && right != null) {
                    other = right.closest(x);
                    if (dist(x, other) < dist(x, closest)) {
                        closest = other;
                    }
                } else if (left != null) {
                    other = left.closest(x);
                    if (dist(x, other) < dist(x, closest)) {
                        closest = other;
                    }
                }
            }
            if (dist(x, value) < dist(x, closest)) {
                return value;
            }
            return closest;
        }

        @Override
        public String toString() {
            return "(" + value[0] + " " + value[1] + ") {" + left + ", " + right + "}";
        }
    }

    protected record PointComparator(int coord) implements Comparator<int[]> {

        @Override
        public int compare(int[] o1, int[] o2) {
            return (o1[coord] - o2[coord]);
        }
    }
}
