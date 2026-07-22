package com.campus.domain.activity;

public class Activity {
    private final int capacity;
    private int participants;

    public Activity(int capacity, int participants) {
        if (capacity <= 0 || participants < 0 || participants > capacity)
            throw new IllegalArgumentException("活动人数配置非法");
        this.capacity = capacity;
        this.participants = participants;
    }

    public void register() {
        if (participants >= capacity) throw new IllegalStateException("活动名额已满");
        participants++;
    }

    public void cancel() {
        if (participants == 0) throw new IllegalStateException("活动报名人数不能为负数");
        participants--;
    }

    public int participants() { return participants; }
}
