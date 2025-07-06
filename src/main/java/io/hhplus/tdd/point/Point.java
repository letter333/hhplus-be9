package io.hhplus.tdd.point;

import io.hhplus.tdd.point.policy.DefaultPointPolicy;
import io.hhplus.tdd.point.policy.PointPolicy;
import lombok.Getter;

@Getter
public class Point {
    private long id;
    private long point;
    private long updateMillis;
    private final PointPolicy pointPolicy;

    public Point(long id, long point, long updateMillis) {
        this.id = id;
        this.point = point;
        this.updateMillis = updateMillis;
        this.pointPolicy = new DefaultPointPolicy();
    }

    public void charge(long amount) {
        pointPolicy.validateCharge(point, amount);

        this.point += amount;
        this.updateMillis = System.currentTimeMillis();
    }

    public void use(long amount) {
        pointPolicy.validateUse(point, amount);

        this.point -= amount;
        this.updateMillis = System.currentTimeMillis();
    }
}
