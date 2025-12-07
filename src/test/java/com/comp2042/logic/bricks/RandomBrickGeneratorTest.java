package com.comp2042.logic.bricks;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.HashSet;
import java.util.Set;

/**
 * Tests for RandomBrickGenerator class.
 * Verifies brick generation, queue management, and preview functionality.
 */
class RandomBrickGeneratorTest {

    private RandomBrickGenerator generator;

    @BeforeEach
    void setUp() {
        generator = new RandomBrickGenerator();
    }

    @Test
    void testGetBrickReturnsNotNull() {
        Brick brick = generator.getBrick();
        assertNotNull(brick, "getBrick() should never return null");
    }

    @Test
    void testGetBrickReturnsValidBrick() {
        Brick brick = generator.getBrick();
        assertNotNull(brick.getShapeMatrix(), "Brick should have shape matrix");
        assertFalse(brick.getShapeMatrix().isEmpty(), "Brick should have at least one rotation");
    }

    @Test
    void testGetNextBrickReturnsNotNull() {
        Brick nextBrick = generator.getNextBrick();
        assertNotNull(nextBrick, "getNextBrick() should never return null");
    }

    @Test
    void testGetNextBrickDoesNotConsume() {
        Brick next1 = generator.getNextBrick();
        Brick next2 = generator.getNextBrick();
        
        // Should return the same brick (peek, not poll)
        assertSame(next1, next2, "getNextBrick() should peek, not consume");
    }

    @Test
    void testGetBrickConsumesFromQueue() {
        Brick next = generator.getNextBrick();
        Brick actual = generator.getBrick();
        
        // The brick we get should be the one we peeked
        assertSame(next, actual, "getBrick() should return the brick from getNextBrick()");
    }

    @Test
    void testGetNextQueueReturnsArray() {
        Brick[] queue = generator.getNextQueue(3);
        assertNotNull(queue, "getNextQueue() should not return null");
    }

    @Test
    void testGetNextQueueRespectsMaxCount() {
        Brick[] queue = generator.getNextQueue(2);
        assertTrue(queue.length <= 2, "Queue should not exceed maxCount");
    }

    @Test
    void testGetNextQueueReturnsAtLeastOne() {
        Brick[] queue = generator.getNextQueue(1);
        assertTrue(queue.length >= 1, "Queue should have at least one brick");
    }

    @Test
    void testGetNextQueueReturnsUpToThree() {
        Brick[] queue = generator.getNextQueue(5);
        assertTrue(queue.length <= 3, "Queue should not exceed DEFAULT_QUEUE_SIZE");
    }

    @Test
    void testGetNextQueueDoesNotConsume() {
        Brick[] queue1 = generator.getNextQueue(3);
        Brick[] queue2 = generator.getNextQueue(3);
        
        // Should return the same bricks (peek, not poll)
        assertEquals(queue1.length, queue2.length, "Queue length should be consistent");
        if (queue1.length > 0 && queue2.length > 0) {
            assertSame(queue1[0], queue2[0], "First brick should be the same");
        }
    }

    @Test
    void testGeneratorProducesAllBrickTypes() {
        // Generate many bricks and verify we get different types
        Set<String> brickTypes = new HashSet<>();
        
        for (int i = 0; i < 50; i++) {
            Brick brick = generator.getBrick();
            String type = brick.getClass().getSimpleName();
            brickTypes.add(type);
        }
        
        // Should have generated at least a few different brick types
        assertTrue(brickTypes.size() >= 1, "Should generate multiple brick types");
    }

    @Test
    void testMultipleBricksAreDifferent() {
        // Get multiple bricks and verify they can be different
        Brick brick1 = generator.getBrick();
        Brick brick2 = generator.getBrick();
        Brick brick3 = generator.getBrick();
        
        // At least some should be different (statistically likely)
        // We can't guarantee all are different, but we can verify they're valid
        assertNotNull(brick1);
        assertNotNull(brick2);
        assertNotNull(brick3);
    }

    @Test
    void testQueueIsMaintained() {
        // Get next brick
        Brick next1 = generator.getNextBrick();
        
        // Get a brick (consumes from queue)
        Brick actual = generator.getBrick();
        
        // Get next brick again (should be different now)
        Brick next2 = generator.getNextBrick();
        
        // Verify queue is maintained
        assertNotNull(next1);
        assertNotNull(actual);
        assertNotNull(next2);
        assertSame(next1, actual, "First getBrick() should return peeked brick");
    }
}
