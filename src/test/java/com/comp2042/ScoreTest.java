package com.comp2042;

import com.comp2042.models.Score;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ScoreTest {

    @Test
    void reset_resetsScoreLevelLinesAndCombo() {
        Score score = new Score();
        score.add(100);
        score.registerLinesCleared(4, 400);
        score.registerLandingWithoutClear();

        score.reset();

        assertEquals(0, score.scoreProperty().get(), "Score should reset to 0");
        assertEquals(1, score.getLevel(), "Level should reset to 1");
        assertEquals(0, score.getTotalLines(), "Total lines should reset to 0");
        assertEquals(0, score.getCombo(), "Combo should reset to 0");
    }

    @Test
    void levelIncreasesEveryTenLines() {
        Score score = new Score();

        // 10 single-line clears → level 2
        for (int i = 0; i < 10; i++) {
            score.registerLinesCleared(1, 100);
        }

        assertEquals(2, score.getLevel(), "Level should be 2 after 10 lines");
    }

    @Test
    void levelDoesNotExceedMaxLevel() {
        Score score = new Score();

        // clear enough lines to overshoot max level
        for (int i = 0; i < 200; i++) {
            score.registerLinesCleared(1, 100);
        }

        assertEquals(10, score.getLevel(), "Level should not go above max level");
    }

    @Test
    void comboIncreasesOnConsecutiveClears() {
        Score score = new Score();

        score.registerLinesCleared(1, 100); // combo 1
        score.registerLinesCleared(2, 200); // combo 2
        score.registerLinesCleared(3, 300); // combo 3

        assertEquals(3, score.getCombo(), "Combo should reach 3 after three clears in a row");
    }

    @Test
    void comboIsCappedAtMaxCombo() {
        Score score = new Score();

        // more consecutive clears than MAX_COMBO
        for (int i = 0; i < 10; i++) {
            score.registerLinesCleared(1, 100);
        }

        assertEquals(4, score.getCombo(), "Combo should be capped at 4");
    }

    @Test
    void comboResetsWhenLandingWithoutClear() {
        Score score = new Score();

        score.registerLinesCleared(1, 100); // combo 1
        score.registerLinesCleared(1, 100); // combo 2
        score.registerLandingWithoutClear(); // break chain

        assertEquals(0, score.getCombo(), "Combo should reset to 0 after landing without clear");
    }

    @Test
    void comboMultiplierAffectsScore() {
        Score score = new Score();

        // First clear: combo 1 => 1 * 100
        score.registerLinesCleared(1, 100);
        // Second clear: combo 2 => 2 * 100
        score.registerLinesCleared(1, 100);

        // Total score: 100 + 200 = 300
        assertEquals(300, score.scoreProperty().get(), "Score should reflect combo multiplier");
    }
}
