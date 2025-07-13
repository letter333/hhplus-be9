package io.hhplus.tdd.point.policy;

public interface PointPolicy {
    void validateCharge(long currentPoint, long chargeAmount);
    void validateUse(long currentPoint, long useAmount);
}
