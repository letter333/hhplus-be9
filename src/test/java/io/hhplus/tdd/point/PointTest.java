package io.hhplus.tdd.point;

import io.hhplus.tdd.point.policy.DefaultPointPolicy;
import io.hhplus.tdd.point.policy.PointPolicy;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class PointTest {
    private final PointPolicy pointPolicy = new DefaultPointPolicy();

    @Test
    void 포인트를_충전하면_포인트가_증가해야_한다() {
        //given
        Point point = new Point(1, 0, System.currentTimeMillis(), pointPolicy);
        long chargeAmount = 1000L;
        long expectedPoint = point.getPoint() + chargeAmount;

        //when
        point.charge(chargeAmount);

        //then
        assertEquals(expectedPoint, point.getPoint());
    }

    @ParameterizedTest
    @ValueSource(longs = {-1, 0})
    void 충전하는_포인트가_0보다_작거나_같으면_예외발생(long chargeAmount) {
        //given
        Point point = new Point(1, 0, System.currentTimeMillis(), pointPolicy);

        //when & then
        assertThrows(IllegalArgumentException.class, () -> point.charge(chargeAmount));
    }

    @Test
    void 충전하는_포인트가_100000을_초과하면_예외발생() {
        //given
        Point point = new Point(1, 0, System.currentTimeMillis(), pointPolicy);
        long chargeAmount = 100001L;

        //when & then
        assertThrows(IllegalArgumentException.class, () -> point.charge(chargeAmount));
    }

    @Test
    void 충전하는_포인트가_정확히_100000이면_성공() {
        //given
        Point point = new Point(1, 0, System.currentTimeMillis(), pointPolicy);
        long chargeAmount = 100000L;

        //when
        point.charge(chargeAmount);

        //then
        assertEquals(100000L, point.getPoint());
    }

    @Test
    void 충전_후_포인트가_1000000을_초과하면_예외발생() {
        //given
        Point point = new Point(1, 900001, System.currentTimeMillis(), pointPolicy);
        long chargeAmount = 100000L;

        //when & then
        assertThrows(IllegalArgumentException.class, () -> point.charge(chargeAmount));
    }

    @Test
    void 충전_후_포인트가_정확히_1000000이면_성공() {
        //given
        Point point = new Point(1, 900000, System.currentTimeMillis(), pointPolicy);
        long chargeAmount = 100000L;

        //when
        point.charge(chargeAmount);

        //then
        assertEquals(1000000L, point.getPoint());
    }

    @Test
    void 충전_후_업데이트_시간이_갱신되어야_한다() throws InterruptedException {
        //given
        long initialTime = System.currentTimeMillis();
        Point point = new Point(1, 0, initialTime, pointPolicy);
        long chargeAmount = 1000L;

        //when
        Thread.sleep(100); //시간 업데이트를 위해 100만큼 슬립
        point.charge(chargeAmount);

        //then
        assertTrue(point.getUpdateMillis() > initialTime);
    }

    @Test
    void 포인트를_사용하면_포인트가_감소해야_한다() {
        //given
        Point point = new Point(1, 100000, System.currentTimeMillis(), pointPolicy);
        long useAmount = 1000L;
        long expectedPoint = point.getPoint() - useAmount;

        //when
        point.use(useAmount);

        //then
        assertEquals(expectedPoint, point.getPoint());
    }

    @Test
    void 사용하는_포인트가_1보다_작으면_예외발생() {
        //given
        Point point = new Point(1, 100000, System.currentTimeMillis(), pointPolicy);
        long useAmount = 0L;

        //when & then
        assertThrows(IllegalArgumentException.class, () -> point.use(useAmount));
    }

    @Test
    void 사용하는_포인트가_가진_포인트보다_많으면_예외발생() {
        //given
        Point point = new Point(1, 100000, System.currentTimeMillis(), pointPolicy);
        long useAmount = 100001L;

        //when & then
        assertThrows(IllegalArgumentException.class, () -> point.use(useAmount));
    }

    @Test
    void 연속으로_포인트를_사용하면_계속_감소해야_한다() {
        //given
        Point point = new Point(1, 100000, System.currentTimeMillis(), pointPolicy);
        long useAmount1 = 1000L;
        long useAmount2 = 1000L;
        long useAmount3 = 1000L;
        long expectedPoint = point.getPoint() - useAmount1 - useAmount2 - useAmount3;

        //when
        point.use(useAmount1);
        point.use(useAmount2);
        point.use(useAmount3);

        //then
        assertEquals(expectedPoint, point.getPoint());
    }

    @Test
    void 연속_사용_후_포인트가_부족하면_예외발생() {
        //given
        Point point = new Point(1, 100000, System.currentTimeMillis(), pointPolicy);

        //when
        point.use(50000L);
        point.use(40000L);

        //then
        assertThrows(IllegalArgumentException.class, () -> point.use(20000L));
    }


    @Test
    void 사용_후_포인트가_정확히_0이면_성공() {
        //given
        Point point = new Point(1, 100000, System.currentTimeMillis(), pointPolicy);
        long useAmount = 100000L;

        //when
        point.use(useAmount);

        //then
        assertEquals(0L, point.getPoint());
    }

    @Test
    void 사용_후_업데이트_시간이_갱신되어야_한다() throws InterruptedException {
        //given
        long initialTime = System.currentTimeMillis();
        Point point = new Point(1, 10000, initialTime, pointPolicy);
        long useAmount = 1000L;

        //when
        Thread.sleep(100); //시간 업데이트를 위해 100만큼 슬립
        point.use(useAmount);

        //then
        assertTrue(point.getUpdateMillis() > initialTime);
    }
}