package com.comp2042.logic.bricks;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

/**
 * Tests for Brick implementations (IBrick, JBrick, LBrick, OBrick, SBrick, TBrick, ZBrick).
 * Verifies that all brick types have valid shape matrices and rotations.
 */
class BrickTest {

    @Test
    void testIBrick() {
        IBrick brick = new IBrick();
        List<int[][]> shapes = brick.getShapeMatrix();
        
        assertNotNull(shapes);
        assertFalse(shapes.isEmpty(), "IBrick should have at least one rotation");
        assertTrue(shapes.size() >= 1, "IBrick should have rotations");
        
        // Verify each shape is valid
        for (int[][] shape : shapes) {
            assertNotNull(shape);
            assertTrue(shape.length > 0);
            assertTrue(shape[0].length > 0);
        }
    }

    @Test
    void testJBrick() {
        JBrick brick = new JBrick();
        List<int[][]> shapes = brick.getShapeMatrix();
        
        assertNotNull(shapes);
        assertFalse(shapes.isEmpty());
        
        for (int[][] shape : shapes) {
            assertNotNull(shape);
            assertTrue(shape.length > 0);
        }
    }

    @Test
    void testLBrick() {
        LBrick brick = new LBrick();
        List<int[][]> shapes = brick.getShapeMatrix();
        
        assertNotNull(shapes);
        assertFalse(shapes.isEmpty());
        
        for (int[][] shape : shapes) {
            assertNotNull(shape);
            assertTrue(shape.length > 0);
        }
    }

    @Test
    void testOBrick() {
        OBrick brick = new OBrick();
        List<int[][]> shapes = brick.getShapeMatrix();
        
        assertNotNull(shapes);
        assertFalse(shapes.isEmpty());
        
        for (int[][] shape : shapes) {
            assertNotNull(shape);
            assertTrue(shape.length > 0);
        }
    }

    @Test
    void testSBrick() {
        SBrick brick = new SBrick();
        List<int[][]> shapes = brick.getShapeMatrix();
        
        assertNotNull(shapes);
        assertFalse(shapes.isEmpty());
        
        for (int[][] shape : shapes) {
            assertNotNull(shape);
            assertTrue(shape.length > 0);
        }
    }

    @Test
    void testTBrick() {
        TBrick brick = new TBrick();
        List<int[][]> shapes = brick.getShapeMatrix();
        
        assertNotNull(shapes);
        assertFalse(shapes.isEmpty());
        
        for (int[][] shape : shapes) {
            assertNotNull(shape);
            assertTrue(shape.length > 0);
        }
    }

    @Test
    void testZBrick() {
        ZBrick brick = new ZBrick();
        List<int[][]> shapes = brick.getShapeMatrix();
        
        assertNotNull(shapes);
        assertFalse(shapes.isEmpty());
        
        for (int[][] shape : shapes) {
            assertNotNull(shape);
            assertTrue(shape.length > 0);
        }
    }

    @Test
    void testBrickShapesAreDefensiveCopies() {
        IBrick brick = new IBrick();
        List<int[][]> shapes1 = brick.getShapeMatrix();
        List<int[][]> shapes2 = brick.getShapeMatrix();
        
        // Should return different lists (defensive copy)
        assertNotSame(shapes1, shapes2, "getShapeMatrix() should return defensive copies");
        
        if (!shapes1.isEmpty() && !shapes2.isEmpty()) {
            // The shape arrays themselves should also be copies
            int[][] shape1 = shapes1.get(0);
            int[][] shape2 = shapes2.get(0);
            assertNotSame(shape1, shape2, "Shape arrays should be defensive copies");
        }
    }

    @Test
    void testAllBricksHaveNonEmptyShapes() {
        Brick[] bricks = {
            new IBrick(),
            new JBrick(),
            new LBrick(),
            new OBrick(),
            new SBrick(),
            new TBrick(),
            new ZBrick()
        };
        
        for (Brick brick : bricks) {
            List<int[][]> shapes = brick.getShapeMatrix();
            assertNotNull(shapes, "Brick should have shape matrix");
            assertFalse(shapes.isEmpty(), "Brick should have at least one rotation");
            
            for (int[][] shape : shapes) {
                assertNotNull(shape, "Shape should not be null");
                assertTrue(shape.length > 0, "Shape should have rows");
                if (shape.length > 0) {
                    assertTrue(shape[0].length > 0, "Shape should have columns");
                }
            }
        }
    }
}
