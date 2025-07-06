package io.hhplus.tdd.point.policy;

public class DefaultPointPolicy implements PointPolicy{

    @Override
    public void validateCharge(long currentPoint, long chargeAmount) {
        if(chargeAmount <= 0) {
            throw new IllegalArgumentException("충전하는 포인트는 0보다 커야됩니다.");
        }

        if(chargeAmount > PointConstants.MAX_CHARGE_AMOUNT) {
            throw new IllegalArgumentException("충전하는 포인트는 100000을 초과할 수 없습니다.");
        }

        if(currentPoint + chargeAmount > PointConstants.MAX_TOTAL_POINT) {
            throw new IllegalArgumentException("충전 후 포인트는 1000000을 초과할 수 없습니다.");
        }
    }

    @Override
    public void validateUse(long currentPoint, long useAmount) {
        if(useAmount < PointConstants.MIN_AMOUNT) {
            throw new IllegalArgumentException("사용하는 포인트는 1보다 작을 수 없습니다.");
        }

        if(useAmount > currentPoint) {
            throw new IllegalArgumentException("사용 포인트가 현재 보유중인 포인트보다 많습니다.");
        }
    }
}
