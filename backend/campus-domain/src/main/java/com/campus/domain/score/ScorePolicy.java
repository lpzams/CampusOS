package com.campus.domain.score;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public final class ScorePolicy {
    private ScorePolicy() {}

    public record CourseScore(BigDecimal credit, BigDecimal score) {}
    public record Statistics(BigDecimal gpa, BigDecimal totalCredits, BigDecimal average, int count) {}

    public static Statistics calculate(List<CourseScore> scores) {
        BigDecimal credits = scores.stream().map(CourseScore::credit).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal weightedPoints = scores.stream()
                .map(s -> gradePoint(s.score()).multiply(s.credit()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalScore = scores.stream().map(CourseScore::score).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal gpa = credits.signum() == 0 ? BigDecimal.ZERO
                : weightedPoints.divide(credits, 2, RoundingMode.HALF_UP);
        BigDecimal average = scores.isEmpty() ? BigDecimal.ZERO
                : totalScore.divide(BigDecimal.valueOf(scores.size()), 2, RoundingMode.HALF_UP);
        return new Statistics(gpa, credits, average, scores.size());
    }

    private static BigDecimal gradePoint(BigDecimal score) {
        if (score.compareTo(BigDecimal.valueOf(60)) < 0) return BigDecimal.ZERO;
        return score.subtract(BigDecimal.valueOf(50)).divide(BigDecimal.TEN, 2, RoundingMode.HALF_UP)
                .min(BigDecimal.valueOf(5));
    }
}
