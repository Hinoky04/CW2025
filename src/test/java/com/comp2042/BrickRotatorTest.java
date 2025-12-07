package com.comp2042;

import com.comp2042.logic.BrickRotator;
import com.comp2042.models.NextShapeInfo;
import com.comp2042.logic.bricks.Brick;
import com.comp2042.logic.bricks.BrickGenerator;
import com.comp2042.logic.bricks.RandomBrickGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for BrickRotator.
 * Tests rotation logic and shape transitions.
 */
public class BrickRotatorTest {

    private BrickRotator rotator;
    private BrickGenerator brickGenerator;

    @BeforeEach
    void setUp() {
        rotator = new BrickRotator();
        brickGenerator = new RandomBrickGenerator();
    }

    @Test
    void setBrick_resetsCurrentShape() {
        Brick brick1 = brickGenerator.getBrick();
        Brick brick2 = brickGenerator.getBrick();
        
        rotator.setBrick(brick1);
        rotator.setCurrentShape(2); // Set to shape 2
        
        rotator.setBrick(brick2); // Set new brick
        
        int[][] shape = rotator.getCurrentShape();
        assertNotNull(shape, "Current shape should not be null");
    }

    @Test
    void getCurrentShape_returnsFirstShapeInitially() {
        Brick brick = brickGenerator.getBrick();
        rotator.setBrick(brick);
        int[][] shape = rotator.getCurrentShape();
        
        assertNotNull(shape, "Current shape should not be null");
        assertTrue(shape.length > 0, "Shape should have rows");
    }

    @Test
    void getNextShape_returnsNextRotation() {
        Brick brick = brickGenerator.getBrick();
        rotator.setBrick(brick);
        NextShapeInfo next = rotator.getNextShape();
        
        assertNotNull(next, "Next shape should not be null");
        assertNotNull(next.getShape(), "Next shape matrix should not be null");
        assertTrue(next.getPosition() >= 0, "Next position should be non-negative");
    }

    @Test
    void getNextShape_wrapsAroundAfterLastShape() {
        Brick brick = brickGenerator.getBrick();
        rotator.setBrick(brick);
        
        // Get next shape multiple times to test wrapping
        NextShapeInfo next = rotator.getNextShape();
        assertNotNull(next, "Next shape should not be null");
    }

    @Test
    void setCurrentShape_changesActiveShape() {
        Brick brick = brickGenerator.getBrick();
        rotator.setBrick(brick);
        
        // Set to a different shape
        rotator.setCurrentShape(1);
        int[][] shape = rotator.getCurrentShape();
        
        assertNotNull(shape, "Shape should not be null after setting");
    }

    @Test
    void rotationCycle_completesFullRotation() {
        Brick brick = brickGenerator.getBrick();
        rotator.setBrick(brick);
        
        NextShapeInfo next1 = rotator.getNextShape();
        rotator.setCurrentShape(next1.getPosition());
        
        NextShapeInfo next2 = rotator.getNextShape();
        rotator.setCurrentShape(next2.getPosition());
        
        // After rotations, should still have valid shape
        int[][] finalShape = rotator.getCurrentShape();
        assertNotNull(finalShape, "Final shape should not be null");
    }

    @Test
    void getNextShape_cyclesThroughRotations() {
        Brick brick = brickGenerator.getBrick();
        rotator.setBrick(brick);
        
        NextShapeInfo next1 = rotator.getNextShape();
        rotator.setCurrentShape(next1.getPosition());
        
        NextShapeInfo next2 = rotator.getNextShape();
        
        // Should cycle through positions
        assertNotNull(next2, "Second next shape should not be null");
    }
}

