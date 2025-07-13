package io.hhplus.tdd.point;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(PointController.class)
class PointControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PointService pointService;

    @Test
    void 포인트_조회_성공() throws Exception {
        // given
        long userId = 1L;
        UserPoint userPoint = new UserPoint(userId, 50000L, System.currentTimeMillis());
        given(pointService.getPoint(userId)).willReturn(userPoint);

        // when & then
        mockMvc.perform(get("/point/{id}", userId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId))
                .andExpect(jsonPath("$.point").value(50000L))
                .andExpect(jsonPath("$.updateMillis").exists());

        verify(pointService).getPoint(userId);
    }

    @Test
    void 포인트_조회_시_서비스_예외_발생() throws Exception {
        // given
        long userId = 999L;
        given(pointService.getPoint(userId)).willThrow(new IllegalArgumentException("사용자 정보가 존재하지 않습니다."));

        // when & then
        mockMvc.perform(get("/point/{id}", userId))
                .andDo(print())
                .andExpect(status().is5xxServerError());

        verify(pointService).getPoint(userId);
    }

    @Test
    void 포인트_히스토리_조회_성공() throws Exception {
        // given
        long userId = 1L;
        List<PointHistory> histories = List.of(
                new PointHistory(1L, userId, 10000L, TransactionType.CHARGE, System.currentTimeMillis()),
                new PointHistory(2L, userId, 5000L, TransactionType.USE, System.currentTimeMillis())
        );
        given(pointService.getPointHistory(userId)).willReturn(histories);

        // when & then
        mockMvc.perform(get("/point/{id}/histories", userId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].userId").value(userId))
                .andExpect(jsonPath("$[0].amount").value(10000L))
                .andExpect(jsonPath("$[0].type").value("CHARGE"))
                .andExpect(jsonPath("$[1].id").value(2L))
                .andExpect(jsonPath("$[1].userId").value(userId))
                .andExpect(jsonPath("$[1].amount").value(5000L))
                .andExpect(jsonPath("$[1].type").value("USE"));

        verify(pointService).getPointHistory(userId);
    }

    @Test
    void 포인트_히스토리_조회_빈_리스트_반환() throws Exception {
        // given
        long userId = 1L;
        given(pointService.getPointHistory(userId)).willReturn(List.of());

        // when & then
        mockMvc.perform(get("/point/{id}/histories", userId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        verify(pointService).getPointHistory(userId);
    }

    @Test
    void 포인트_충전_성공() throws Exception {
        // given
        long userId = 1L;
        long amount = 10000L;
        UserPoint chargedUserPoint = new UserPoint(userId, 60000L, System.currentTimeMillis());
        given(pointService.charge(userId, amount)).willReturn(chargedUserPoint);

        // when & then
        mockMvc.perform(patch("/point/{id}/charge", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.valueOf(amount)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId))
                .andExpect(jsonPath("$.point").value(60000L))
                .andExpect(jsonPath("$.updateMillis").exists());

        verify(pointService).charge(userId, amount);
    }

    @Test
    void 포인트_충전_시_서비스_예외_발생() throws Exception {
        // given
        long userId = 1L;
        long amount = -1000L;
        given(pointService.charge(userId, amount)).willThrow(new IllegalArgumentException("충전하는 포인트는 0보다 커야됩니다."));

        // when & then
        mockMvc.perform(patch("/point/{id}/charge", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.valueOf(amount)))
                .andDo(print())
                .andExpect(status().is5xxServerError());

        verify(pointService).charge(userId, amount);
    }

    @Test
    void 포인트_충전_시_최대_충전_금액_초과() throws Exception {
        // given
        long userId = 1L;
        long amount = 100001L;
        given(pointService.charge(userId, amount)).willThrow(new IllegalArgumentException("충전하는 포인트는 100000을 초과할 수 없습니다."));

        // when & then
        mockMvc.perform(patch("/point/{id}/charge", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.valueOf(amount)))
                .andDo(print())
                .andExpect(status().is5xxServerError());

        verify(pointService).charge(userId, amount);
    }

    @Test
    void 포인트_사용_성공() throws Exception {
        // given
        long userId = 1L;
        long amount = 5000L;
        UserPoint usedUserPoint = new UserPoint(userId, 45000L, System.currentTimeMillis());
        given(pointService.use(userId, amount)).willReturn(usedUserPoint);

        // when & then
        mockMvc.perform(patch("/point/{id}/use", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.valueOf(amount)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId))
                .andExpect(jsonPath("$.point").value(45000L))
                .andExpect(jsonPath("$.updateMillis").exists());

        verify(pointService).use(userId, amount);
    }

    @Test
    void 포인트_사용_시_서비스_예외_발생() throws Exception {
        // given
        long userId = 1L;
        long amount = 0L;
        given(pointService.use(userId, amount)).willThrow(new IllegalArgumentException("사용하는 포인트는 1보다 작을 수 없습니다."));

        // when & then
        mockMvc.perform(patch("/point/{id}/use", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.valueOf(amount)))
                .andDo(print())
                .andExpect(status().is5xxServerError());

        verify(pointService).use(userId, amount);
    }

    @Test
    void 포인트_사용_시_잔액_부족() throws Exception {
        // given
        long userId = 1L;
        long amount = 100000L;
        given(pointService.use(userId, amount)).willThrow(new IllegalArgumentException("사용 포인트가 현재 보유중인 포인트보다 많습니다."));

        // when & then
        mockMvc.perform(patch("/point/{id}/use", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.valueOf(amount)))
                .andDo(print())
                .andExpect(status().is5xxServerError());

        verify(pointService).use(userId, amount);
    }
}