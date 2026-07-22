package com.campus.domain.repair;

public class RepairOrder {
    public enum Status { SUBMITTED, PROCESSING, COMPLETED, EVALUATED }

    private Status status;
    private Integer score;
    private String evaluation;

    public RepairOrder(Status status) { this.status = status == null ? Status.SUBMITTED : status; }

    public void evaluate(int score, String content) {
        if (status != Status.COMPLETED) throw new IllegalStateException("只有已完成的报修可以评价");
        if (score < 1 || score > 5) throw new IllegalArgumentException("评分必须为 1-5");
        if (content == null || content.isBlank()) throw new IllegalArgumentException("评价内容不能为空");
        this.score = score;
        this.evaluation = content.trim();
        this.status = Status.EVALUATED;
    }

    public Status status() { return status; }
    public Integer score() { return score; }
    public String evaluation() { return evaluation; }
}
