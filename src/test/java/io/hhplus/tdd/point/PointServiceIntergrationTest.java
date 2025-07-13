package io.hhplus.tdd.point;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class PointServiceIntergrationTest {
    @Autowired
    private PointService pointService;
    @Autowired
    private PointHistoryTable pointHistoryTable;
    @Autowired
    private UserPointTable userPointTable;

    @Test
    void 포인트_조회() {
        //given
        long id = 1L;

        //when
        UserPoint userPoint = pointService.getPoint(id);

        //then
        assertNotNull(userPoint);
        assertEquals(id, userPoint.id());
        assertEquals(0, userPoint.point());
    }

    @Test
    void 포인트_충전() {
        //given
        long id = 2L;
        long amount = 10000L;

        //when
        UserPoint chargedUserPoint = pointService.charge(id, amount);

        //then
        assertNotNull(chargedUserPoint);
        assertEquals(id, chargedUserPoint.id());
        assertEquals(amount, chargedUserPoint.point());

        List<PointHistory> histories = pointService.getPointHistory(id);
        assertEquals(1, histories.size());
        PointHistory history = histories.get(0);
        assertEquals(id, history.userId());
        assertEquals(amount, history.amount());
        assertEquals(TransactionType.CHARGE, history.type());
    }

    @Test
    void 포인트_사용() {
        //given
        long id = 3L;
        long chargeAmount = 10000L;
        long useAmount = 5000L;

        pointService.charge(id, chargeAmount);

        //when
        UserPoint usedUserPoint = pointService.use(id, useAmount);

        //then
        assertNotNull(usedUserPoint);
        assertEquals(id, usedUserPoint.id());
        assertEquals(chargeAmount - useAmount, usedUserPoint.point());

        List<PointHistory> histories = pointService.getPointHistory(id);
        assertEquals(2, histories.size());
        PointHistory chargeHistory = histories.get(0);
        assertEquals(id, chargeHistory.userId());
        assertEquals(chargeAmount, chargeHistory.amount());
        assertEquals(TransactionType.CHARGE, chargeHistory.type());
        PointHistory useHistory = histories.get(1);
        assertEquals(id, useHistory.userId());
        assertEquals(useAmount, useHistory.amount());
        assertEquals(TransactionType.USE, useHistory.type());
    }

    @Test
    void 포인트_충전_사용_연속_처리() {
        //given
        long id = 4L;
        long chargeAmount1 = 10000L;
        long chargeAmount2 = 20000L;
        long useAmount = 5000L;

        //when
        pointService.charge(id, chargeAmount1);
        pointService.charge(id, chargeAmount2);
        pointService.use(id, useAmount);

        //then
        UserPoint userPoint = pointService.getPoint(id);
        assertEquals(chargeAmount1 + chargeAmount2 - useAmount, userPoint.point());

        assertEquals(25000L, userPoint.point());
    }


    @Test
    void 포인트_내역_조회() {
        //given
        long id = 5L;
        long chargeAmount1 = 10000L;
        long chargeAmount2 = 20000L;
        long useAmount = 5000L;

        pointService.charge(id, chargeAmount1);
        pointService.charge(id, chargeAmount2);
        pointService.use(id, useAmount);

        //when
        List<PointHistory> histories = pointService.getPointHistory(id);

        //then
        assertNotNull(histories);
        assertEquals(3, histories.size());

        PointHistory chargeHistory1 = histories.get(0);
        assertEquals(id, chargeHistory1.userId());
        assertEquals(chargeAmount1, chargeHistory1.amount());
        assertEquals(TransactionType.CHARGE, chargeHistory1.type());

        PointHistory chargeHistory2 = histories.get(1);
        assertEquals(id, chargeHistory2.userId());
        assertEquals(chargeAmount2, chargeHistory2.amount());
        assertEquals(TransactionType.CHARGE, chargeHistory2.type());

        PointHistory useHistory = histories.get(2);
        assertEquals(id, useHistory.userId());
        assertEquals(useAmount, useHistory.amount());
        assertEquals(TransactionType.USE, useHistory.type());
    }

    @Test
    void 잘못된_포인트_충전() {
        //given
        long id = 6L;
        long amount = -1000L;

        //when & then
        assertThrows(IllegalArgumentException.class, () -> {
            pointService.charge(id, amount);
        });

        List<PointHistory> histories = pointService.getPointHistory(id);
        assertEquals(0, histories.size());
    }

    @Test
    void 잘못된_포인트_사용() {
        //given
        long id = 7L;
        long chargeAmount = 10000L;
        long useAmount = 10001L;

        pointService.charge(id, chargeAmount);

        //when & then
        assertThrows(IllegalArgumentException.class, () -> {
            pointService.use(id, useAmount);
        });

        List<PointHistory> histories = pointService.getPointHistory(id);
        assertEquals(1, histories.size());
        PointHistory history = histories.get(0);
        assertEquals(id, history.userId());
        assertEquals(chargeAmount, history.amount());
        assertEquals(TransactionType.CHARGE, history.type());

        UserPoint userPoint = pointService.getPoint(id);
        assertEquals(chargeAmount, userPoint.point());
    }

    @Test
    void 한번에_충전_가능한_최대_포인트_초과() {
        //given
        long id = 8L;
        long amount = 1000001L;

        //when & then
        assertThrows(IllegalArgumentException.class, () -> {
            pointService.charge(id, amount);
        });

        List<PointHistory> histories = pointService.getPointHistory(id);
        assertEquals(0, histories.size());
        UserPoint userPoint = pointService.getPoint(id);
        assertEquals(0, userPoint.point());
    }

    @Test
    void 충전_후_포인트가_최대치를_초과() {
        //given
        long id = 9L;
        long chargeAmount = 1000001L;

        //when & then
        assertThrows(IllegalArgumentException.class, () -> {
            pointService.charge(id, chargeAmount);
        });

        List<PointHistory> histories = pointService.getPointHistory(id);
        assertEquals(0, histories.size());
        UserPoint userPoint = pointService.getPoint(id);
        assertEquals(0, userPoint.point());
    }
}
