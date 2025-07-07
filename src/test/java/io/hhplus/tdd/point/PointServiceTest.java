package io.hhplus.tdd.point;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
}