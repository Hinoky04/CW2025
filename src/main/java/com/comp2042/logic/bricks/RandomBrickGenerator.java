package com.comp2042.logic.bricks;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class RandomBrickGenerator implements BrickGenerator {

    /**
     * Minimum size of the upcoming queue we maintain.
     * We want to support a preview of up to 3 next bricks.
     */
    private static final int DEFAULT_QUEUE_SIZE = 3;

    private final List<Brick> brickList;
    private final Deque<Brick> nextBricks = new ArrayDeque<>();

    public RandomBrickGenerator() {
        brickList = new ArrayList<>();
        brickList.add(new IBrick());
        brickList.add(new JBrick());
        brickList.add(new LBrick());
        brickList.add(new OBrick());
        brickList.add(new SBrick());
        brickList.add(new TBrick());
        brickList.add(new ZBrick());

        // Seed the queue so the game always has upcoming bricks ready.
        fillQueue(DEFAULT_QUEUE_SIZE);
    }

    /**
     * Ensures the queue has at least {@code minSize} bricks by appending
     * new random bricks as needed.
     */
    private void fillQueue(int minSize) {
        while (nextBricks.size() < minSize) {
            Brick randomBrick = brickList.get(ThreadLocalRandom.current().nextInt(brickList.size()));
            nextBricks.add(randomBrick);
        }
    }

    @Override
    public Brick getBrick() {
        // Ensure the queue is populated, then consume the head.
        fillQueue(DEFAULT_QUEUE_SIZE);
        return nextBricks.poll();
    }

    @Override
    public Brick getNextBrick() {
        // Ensure we have something to peek at.
        fillQueue(DEFAULT_QUEUE_SIZE);
        return nextBricks.peek();
    }

    @Override
    public Brick[] getNextQueue(int maxCount) {
        // Ensure the queue is populated before exposing it.
        fillQueue(DEFAULT_QUEUE_SIZE);

        int size = Math.min(maxCount, nextBricks.size());
        Brick[] result = new Brick[size];

        int index = 0;
        for (Brick brick : nextBricks) {
            if (index >= size) {
                break;
            }
            result[index++] = brick;
        }

        return result;
    }
}
