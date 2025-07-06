package io.hhplus.tdd.point.policy;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DefaultPointPolicyTest {
    private final PointPolicy pointPolicy = new DefaultPointPolicy();

    @Test
    void 유효한_충전_포인트면_성공() {
        //when & then
        assertDoesNotThrow(() -> pointPolicy.validateCharge(0, 100000L));
    }

    @Test
    void 충전_포인트가_0보다_작거나_같으면_예외발생() {
        //when & then
        assertThrows(IllegalArgumentException.class, () -> pointPolicy.validateCharge(100000L, 0L));
        assertThrows(IllegalArgumentException.class, () -> pointPolicy.validateCharge(100000L, -1L));
    }

    @Test
    void 충전_포인트가_100000을_초과하면_예외발생() {
        //when & then
        assertThrows(IllegalArgumentException.class, () -> pointPolicy.validateCharge(0, 100001L));
    }

    @Test
    void 충전_후_포인트가_1000000을_초과하면_예외발생() {
        //when & then
        assertThrows(IllegalArgumentException.class, () -> pointPolicy.validateCharge(0, 1000001L));
    }

    @Test
    void 유효한_사용_포인트면_성공() {
        //when & then
        assertDoesNotThrow(() -> pointPolicy.validateUse(100000L, 1000L));
    }

    @Test
    void 사용_포인트가_1보다_작으면_예외발생() {
        //when & then
        assertThrows(IllegalArgumentException.class, () -> pointPolicy.validateUse(0, 0L));
    }

    @Test
    void 사용_포인트가_보유_포인트보다_많으면_예외발생() {
        //when & then
        assertThrows(IllegalArgumentException.class, () -> pointPolicy.validateUse(100000L, 100001L));
    }
}