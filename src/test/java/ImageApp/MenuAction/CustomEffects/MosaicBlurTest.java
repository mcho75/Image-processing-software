package ImageApp.MenuAction.CustomEffects;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MosaicBlurTest extends MosaicBlur{

    public MosaicBlurTest() {
        super(0, 4, 4, 4);
    }

    @Test
    public void testing() {
        int[][] expectedCloud = new int[][]{
                new int[]{2, 0},
                new int[]{0, 1},
                new int[]{2, 1},
                new int[]{3, 2},
        };
        assertEquals(4, cloud.length);
        assertArrayEquals(expectedCloud, cloud);

        assertNotNull(tree);
        assertArrayEquals(expectedCloud[0], closest(expectedCloud[0]));
        assertArrayEquals(expectedCloud[1], closest(new int[2])); // 0,0
    }

    @Test
    public void comparatorTest() {
        PointComparator xComparator = new PointComparator(0);
        PointComparator yComparator = new PointComparator(1);
        assertEquals(0, xComparator.coord());
        assertEquals(1, yComparator.coord());
        int[] point55 = new int[]{5, 5};
        int[] point45 = new int[]{4, 5};
        int[] point65 = new int[]{6, 5};
        int[] point44 = new int[]{4, 4};
        int[] point66 = new int[]{6, 6};
        assertEquals(0, xComparator.compare(point55, point55));
        assertEquals(0, yComparator.compare(point55, point55));
        assertTrue(xComparator.compare(point45, point65) < 0);
        assertTrue(yComparator.compare(point66, point44) > 0);
    }

}