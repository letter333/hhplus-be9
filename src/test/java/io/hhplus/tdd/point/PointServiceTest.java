package io.hhplus.tdd.point;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.point.dto.PointAmountRequestDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PointServiceTest {
    @Mock
    private UserPointTable userPointTable;
    @Mock
    private PointHistoryTable pointHistoryTable;
    @InjectMocks
    private PointService pointService;

    @Test
    void 포인트_조회_성공() {
        //given
        long id = 1L;
        UserPoint existingUserPoint = UserPoint.empty(id);
        when(userPointTable.selectById(id)).thenReturn(existingUserPoint);

        //when
        UserPoint userPoint = pointService.getPoint(id);

        //then
        assertNotNull(userPoint);
        assertEquals(existingUserPoint, userPoint);
        verify(userPointTable, times(1)).selectById(id);
    }

    @Test
    void 존재하지_않는_사용자의_포인트_조회() {
        //given
        long id = 999L;
        when(userPointTable.selectById(id)).thenReturn(null);

        //when
        UserPoint userPoint = pointService.getPoint(id);

        //then
        assertNull(userPoint);
        verify(userPointTable, times(1)).selectById(id);
    }

    @Test
    void 포인트_충전_성공() {
        //given
        long id = 1L;
        long amount = 100000L;
        PointAmountRequestDto dto = new PointAmountRequestDto(id, amount);
        UserPoint existingUserPoint = UserPoint.empty(id);
        UserPoint expectedUserPoint = new UserPoint(id, existingUserPoint.point() + amount, System.currentTimeMillis());

        when(userPointTable.selectById(id)).thenReturn(existingUserPoint);
        when(userPointTable.insertOrUpdate(anyLong(), anyLong())).thenReturn(expectedUserPoint);

        //when
        UserPoint chargedUserPoint = pointService.charge(dto);

        //then
        assertNotNull(chargedUserPoint);
        assertEquals(id, chargedUserPoint.id());
        assertEquals(expectedUserPoint.point(), chargedUserPoint.point());
        verify(userPointTable, times(1)).selectById(id);
        verify(userPointTable, times(1)).insertOrUpdate(id, chargedUserPoint.point());
        verify(pointHistoryTable, times(1)).insert(eq(id), eq(amount), eq(TransactionType.CHARGE), anyLong());
    }

    @Test
    void 충전_요청_정보가_null_이면_예외발생() {
        //given
        PointAmountRequestDto dto = null;

        //when & then
        assertThrows(IllegalArgumentException.class, () -> {
            pointService.charge(dto);
        });

        verify(userPointTable, never()).selectById(anyLong());
        verify(userPointTable, never()).insertOrUpdate(anyLong(), anyLong());
        verify(pointHistoryTable, never()).insert(anyLong(), anyLong(), any(TransactionType.class), anyLong());
    }

    @Test
    void 사용자가_존재하지_않으면_예외발생() {
        //given
        long id = 999L;
        PointAmountRequestDto dto = new PointAmountRequestDto(id, 100000L);
        when(userPointTable.selectById(id)).thenReturn(null);

        //when & then
        assertThrows(IllegalArgumentException.class, () -> {
            pointService.charge(dto);
        });

        verify(userPointTable, times(1)).selectById(id);
        verify(userPointTable, never()).insertOrUpdate(anyLong(), anyLong());
        verify(pointHistoryTable, never()).insert(anyLong(), anyLong(), any(TransactionType.class), anyLong());
    }

    @Test
    void 유효하지_않은_포인트_충전_시_예외발생() {
        //given
        long id = 1L;
        long amount = -1000L;
        PointAmountRequestDto dto = new PointAmountRequestDto(id, amount);
        UserPoint existingUserPoint = UserPoint.empty(id);
        when(userPointTable.selectById(id)).thenReturn(existingUserPoint);

        //when & then
        assertThrows(IllegalArgumentException.class, () -> {
            pointService.charge(dto);
        });

        verify(userPointTable, times(1)).selectById(id);
        verify(userPointTable, never()).insertOrUpdate(anyLong(), anyLong());
        verify(pointHistoryTable, never()).insert(anyLong(), anyLong(), any(TransactionType.class), anyLong());
    }

    @Test
    void 최대_충전_포인트를_초과하면_예외발생() {
        //given
        long id = 1L;
        long amount = 100001L;
        PointAmountRequestDto dto = new PointAmountRequestDto(id, amount);
        UserPoint existingUserPoint = UserPoint.empty(id);
        when(userPointTable.selectById(id)).thenReturn(existingUserPoint);

        //when & then
        assertThrows(IllegalArgumentException.class, () -> {
            pointService.charge(dto);
        });

        verify(userPointTable, times(1)).selectById(id);
        verify(userPointTable, never()).insertOrUpdate(anyLong(), anyLong());
        verify(pointHistoryTable, never()).insert(anyLong(), anyLong(), any(TransactionType.class), anyLong());
    }

    @Test
    void 충전_후_포인트가_최대값을_초과하면_예외발생() {
        //given
        long id = 1L;
        long amount = 1000001L;
        PointAmountRequestDto dto = new PointAmountRequestDto(id, amount);
        UserPoint existingUserPoint = UserPoint.empty(id);

        when(userPointTable.selectById(id)).thenReturn(existingUserPoint);

        //when & then
        assertThrows(IllegalArgumentException.class, () -> {
            pointService.charge(dto);
        });

        verify(userPointTable, times(1)).selectById(id);
        verify(userPointTable, never()).insertOrUpdate(anyLong(), anyLong());
        verify(pointHistoryTable, never()).insert(anyLong(), anyLong(), any(TransactionType.class), anyLong());
    }

    @Test
    void 포인트_충전_성공_후_히스토리_저장() {
        //given
        long id = 1L;
        long amount = 50000L;
        PointAmountRequestDto dto = new PointAmountRequestDto(id, amount);
        UserPoint existingUserPoint = UserPoint.empty(id);
        UserPoint expectedUserPoint = new UserPoint(id, existingUserPoint.point() + amount, System.currentTimeMillis());

        long currentTime = System.currentTimeMillis();
        PointHistory expectedHistory = new PointHistory(1L, id, amount, TransactionType.CHARGE, currentTime);

        when(userPointTable.selectById(id)).thenReturn(existingUserPoint);
        when(userPointTable.insertOrUpdate(anyLong(), anyLong())).thenReturn(expectedUserPoint);
        when(pointHistoryTable.insert(eq(id), eq(amount), eq(TransactionType.CHARGE), anyLong()))
                .thenReturn(expectedHistory);

        //when
        UserPoint chargedUserPoint = pointService.charge(dto);

        //then
        assertNotNull(chargedUserPoint);
        assertEquals(id, chargedUserPoint.id());
        assertEquals(expectedUserPoint.point(), chargedUserPoint.point());

        verify(pointHistoryTable, times(1)).insert(
                eq(id),
                eq(amount),
                eq(TransactionType.CHARGE),
                anyLong()
        );
    }

    @Test
    void 포인트_사용_성공() {
        //given
        long id = 1L;
        long amount = 1000L;
        PointAmountRequestDto dto = new PointAmountRequestDto(id, amount);
        UserPoint existingUserPoint = new UserPoint(id, 100000L, System.currentTimeMillis());
        UserPoint expectedUserPoint = new UserPoint(id, existingUserPoint.point() - amount, System.currentTimeMillis());

        when(userPointTable.selectById(id)).thenReturn(existingUserPoint);
        when(userPointTable.insertOrUpdate(anyLong(), anyLong())).thenReturn(expectedUserPoint);

        //when
        UserPoint usedUserPoint = pointService.use(dto);

        //then
        assertNotNull(usedUserPoint);
        assertEquals(id, usedUserPoint.id());
        assertEquals(expectedUserPoint.point(), usedUserPoint.point());
        verify(userPointTable, times(1)).selectById(id);
        verify(userPointTable, times(1)).insertOrUpdate(id, usedUserPoint.point());
        verify(pointHistoryTable, times(1)).insert(eq(id), eq(amount), eq(TransactionType.USE), anyLong());
    }

    @Test
    void 사용_요청_정보가_null_이면_예외발생() {
        //given
        PointAmountRequestDto dto = null;

        //when & then
        assertThrows(IllegalArgumentException.class, () -> {
            pointService.use(dto);
        });

        verify(userPointTable, never()).selectById(anyLong());
        verify(userPointTable, never()).insertOrUpdate(anyLong(), anyLong());
        verify(pointHistoryTable, never()).insert(anyLong(), anyLong(), any(TransactionType.class), anyLong());
    }

    @Test
    void 사용_시_사용자가_존재하지_않으면_예외발생() {
        //given
        long id = 999L;
        PointAmountRequestDto dto = new PointAmountRequestDto(id, 50000L);
        when(userPointTable.selectById(id)).thenReturn(null);

        //when & then
        assertThrows(IllegalArgumentException.class, () -> {
            pointService.use(dto);
        });

        verify(userPointTable, times(1)).selectById(id);
        verify(userPointTable, never()).insertOrUpdate(anyLong(), anyLong());
        verify(pointHistoryTable, never()).insert(anyLong(), anyLong(), any(TransactionType.class), anyLong());
    }

    @Test
    void 사용하는_포인트가_1보다_작으면_예외발생() {
        //given
        long id = 1L;
        long amount = 0L;
        PointAmountRequestDto dto = new PointAmountRequestDto(id, amount);
        UserPoint existingUserPoint = new UserPoint(id, 100000L, System.currentTimeMillis());
        when(userPointTable.selectById(id)).thenReturn(existingUserPoint);

        //when & then
        assertThrows(IllegalArgumentException.class, () -> {
            pointService.use(dto);
        });

        verify(userPointTable, times(1)).selectById(id);
        verify(userPointTable, never()).insertOrUpdate(anyLong(), anyLong());
        verify(pointHistoryTable, never()).insert(anyLong(), anyLong(), any(TransactionType.class), anyLong());
    }

    @Test
    void 사용하는_포인트가_현재_포인트보다_많으면_예외발생() {
        //given
        long id = 1L;
        long amount = 100001L;
        PointAmountRequestDto dto = new PointAmountRequestDto(id, amount);
        UserPoint existingUserPoint = new UserPoint(id, 100000L, System.currentTimeMillis());
        when(userPointTable.selectById(id)).thenReturn(existingUserPoint);

        //when & then
        assertThrows(IllegalArgumentException.class, () -> {
            pointService.use(dto);
        });

        verify(userPointTable, times(1)).selectById(id);
        verify(userPointTable, never()).insertOrUpdate(anyLong(), anyLong());
        verify(pointHistoryTable, never()).insert(anyLong(), anyLong(), any(TransactionType.class), anyLong());
    }

    @Test
    void 포인트_사용_성공_후_히스토리_저장() {
        //given
        long id = 1L;
        long amount = 30000L;
        PointAmountRequestDto dto = new PointAmountRequestDto(id, amount);
        UserPoint existingUserPoint = new UserPoint(id, 100000L, System.currentTimeMillis());
        UserPoint expectedUserPoint = new UserPoint(id, existingUserPoint.point() - amount, System.currentTimeMillis());

        long currentTime = System.currentTimeMillis();
        PointHistory expectedHistory = new PointHistory(1L, id, amount, TransactionType.USE, currentTime);

        when(userPointTable.selectById(id)).thenReturn(existingUserPoint);
        when(userPointTable.insertOrUpdate(anyLong(), anyLong())).thenReturn(expectedUserPoint);
        when(pointHistoryTable.insert(eq(id), eq(amount), eq(TransactionType.USE), anyLong()))
                .thenReturn(expectedHistory);

        //when
        UserPoint usedUserPoint = pointService.use(dto);

        //then
        assertNotNull(usedUserPoint);
        assertEquals(id, usedUserPoint.id());
        assertEquals(expectedUserPoint.point(), usedUserPoint.point());

        verify(pointHistoryTable, times(1)).insert(
                eq(id),
                eq(amount),
                eq(TransactionType.USE),
                anyLong()
        );
    }

    @Test
    void 포인트_히스토리_조회_성공() {
        //given
        long id = 1L;
        List<PointHistory> expectedHistories = List.of(
                new PointHistory(1L, id, 1000L, TransactionType.CHARGE, System.currentTimeMillis()),
                new PointHistory(2L, id, 500L, TransactionType.USE, System.currentTimeMillis())
        );
        when(pointHistoryTable.selectAllByUserId(id)).thenReturn(expectedHistories);

        //when
        List<PointHistory> histories = pointService.getPointHistory(id);

        //then
        assertNotNull(histories);
        assertEquals(2, histories.size());
        assertEquals(expectedHistories, histories);
        verify(pointHistoryTable, times(1)).selectAllByUserId(id);
    }

    @Test
    void 포인트_충전_후_히스토리_조회_확인() {
        //given
        long id = 1L;
        long amount = 30000L;
        PointAmountRequestDto dto = new PointAmountRequestDto(id, amount);
        UserPoint existingUserPoint = UserPoint.empty(id);
        UserPoint chargedUserPoint = new UserPoint(id, amount, System.currentTimeMillis());

        long currentTime = System.currentTimeMillis();
        PointHistory chargeHistory = new PointHistory(1L, id, amount, TransactionType.CHARGE, currentTime);
        List<PointHistory> expectedHistories = List.of(chargeHistory);

        when(userPointTable.selectById(id)).thenReturn(existingUserPoint);
        when(userPointTable.insertOrUpdate(anyLong(), anyLong())).thenReturn(chargedUserPoint);
        when(pointHistoryTable.insert(eq(id), eq(amount), eq(TransactionType.CHARGE), anyLong()))
                .thenReturn(chargeHistory);
        when(pointHistoryTable.selectAllByUserId(id)).thenReturn(expectedHistories);

        //when
        pointService.charge(dto);
        List<PointHistory> histories = pointService.getPointHistory(id);

        //then
        assertNotNull(histories);
        assertEquals(1, histories.size());

        PointHistory savedHistory = histories.get(0);
        assertEquals(id, savedHistory.userId());
        assertEquals(amount, savedHistory.amount());
        assertEquals(TransactionType.CHARGE, savedHistory.type());
        assertTrue(savedHistory.updateMillis() > 0);

        verify(pointHistoryTable, times(1)).insert(eq(id), eq(amount), eq(TransactionType.CHARGE), anyLong());
        verify(pointHistoryTable, times(1)).selectAllByUserId(id);
    }

    @Test
    void 히스토리가_없으면_빈_리스트_반환() {
        //given
        long id = 1L;
        when(pointHistoryTable.selectAllByUserId(id)).thenReturn(List.of());

        //when
        List<PointHistory> histories = pointService.getPointHistory(id);

        //then
        assertNotNull(histories);
        assertTrue(histories.isEmpty());
        verify(pointHistoryTable, times(1)).selectAllByUserId(id);
    }

    @Test
    void 포인트_사용_후_히스토리_조회_확인() {
        //given
        long id = 1L;
        long amount = 25000L;
        PointAmountRequestDto dto = new PointAmountRequestDto(id, amount);
        UserPoint existingUserPoint = new UserPoint(id, 100000L, System.currentTimeMillis());
        UserPoint usedUserPoint = new UserPoint(id, 75000L, System.currentTimeMillis());

        long currentTime = System.currentTimeMillis();
        PointHistory useHistory = new PointHistory(1L, id, amount, TransactionType.USE, currentTime);
        List<PointHistory> expectedHistories = List.of(useHistory);

        when(userPointTable.selectById(id)).thenReturn(existingUserPoint);
        when(userPointTable.insertOrUpdate(anyLong(), anyLong())).thenReturn(usedUserPoint);
        when(pointHistoryTable.insert(eq(id), eq(amount), eq(TransactionType.USE), anyLong()))
                .thenReturn(useHistory);
        when(pointHistoryTable.selectAllByUserId(id)).thenReturn(expectedHistories);

        //when
        pointService.use(dto);
        List<PointHistory> histories = pointService.getPointHistory(id);

        //then
        assertNotNull(histories);
        assertEquals(1, histories.size());

        PointHistory savedHistory = histories.get(0);
        assertEquals(id, savedHistory.userId());
        assertEquals(amount, savedHistory.amount());
        assertEquals(TransactionType.USE, savedHistory.type());
        assertTrue(savedHistory.updateMillis() > 0);

        verify(pointHistoryTable, times(1)).insert(eq(id), eq(amount), eq(TransactionType.USE), anyLong());
        verify(pointHistoryTable, times(1)).selectAllByUserId(id);
    }
}