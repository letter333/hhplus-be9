package io.hhplus.tdd.point;

import lombok.Getter;

@Getter
public class Point {
    private long id;
    private long point;
    private long updateMillis;

    public Point(long id, long point, long updateMillis) {
        this.id = id;
        this.point = point;
        this.updateMillis = updateMillis;
    }

    public void charge(long amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("충전하는 포인트는 0보다 커야됩니다.");
        }
        
        if (amount > 100000) {
            throw new IllegalArgumentException("충전하는 포인트는 100000을 초과할 수 없습니다.");
        }

        if(point + amount > 1000000) {
            throw new IllegalArgumentException("충전 후 포인트는 1000000을 초과할 수 없습니다..");
        }

        this.point += amount;
        this.updateMillis = System.currentTimeMillis();
    }
}
