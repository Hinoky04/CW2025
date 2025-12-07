package com.comp2042.models;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for NextShapeInfo class.
 * Verifies data integrity and defensive copying.
 */
class NextShapeInfoTest {

    @Test
    void testConstructorAndGetters() {
        int[][] shape = new int[][]{{1, 1}, {1, 1}};
        NextShapeInfo info = new NextShapeInfo(shape, 2);
        
        assertEquals(2, info.getPosition());
        assertNotNull(info.getShape());
    }

    @Test
    void testGetShapeReturnsDefensiveCopy() {
        int[][] originalShape = new int[][]{{1, 2}, {3, 4}};
        NextShapeInfo info = new NextShapeInfo(originalShape, 0);
        
        int[][] returnedShape = info.getShape();
        
        // Modify the returned shape
        returnedShape[0][0] = 999;
        
        // Original should not be affected
        assertEquals(1, originalShape[0][0], "Original shape should not be modified");
        assertEquals(999, returnedShape[0][0], "Returned shape should be modifiable");
    }

    @Test
    void testPositionValues() {
        int[][] shape = new int[][]{{1, 1}};
        
        NextShapeInfo info0 = new NextShapeInfo(shape, 0);
        assertEquals(0, info0.getPosition());
        
        NextShapeInfo info1 = new NextShapeInfo(shape, 1);
        assertEquals(1, info1.getPosition());
        
        NextShapeInfo info3 = new NextShapeInfo(shape, 3);
        assertEquals(3, info3.getPosition());
    }

    @Test
    void testShapeDimensionsPreserved() {
        int[][] originalShape = new int[][]{
            {1, 2, 3},
            {4, 5, 6}
        };
        NextShapeInfo info = new NextShapeInfo(originalShape, 0);
        
        int[][] returnedShape = info.getShape();
        
        assertEquals(originalShape.length, returnedShape.length);
        assertEquals(originalShape[0].length, returnedShape[0].length);
    }

    @Test
    void testEmptyShape() {
        int[][] emptyShape = new int[][]{{0, 0}, {0, 0}};
        NextShapeInfo info = new NextShapeInfo(emptyShape, 0);
        
        assertNotNull(info.getShape());
        assertEquals(0, info.getPosition());
    }
}
