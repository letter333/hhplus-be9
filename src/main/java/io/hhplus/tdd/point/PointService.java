package io.hhplus.tdd.point;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.point.dto.PointAmountRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PointService {
    private final UserPointTable userPointTable;
    private final PointHistoryTable pointHistoryTable;

    public UserPoint getPoint(long id) {
        return userPointTable.selectById(id);
    }

    public UserPoint charge(PointAmountRequestDto dto) {
        if(dto == null) {
            throw new IllegalArgumentException("충전 요청 정보가 필요합니다.");
        }

        UserPoint userPoint = getPoint(dto.id());

        if(userPoint == null) {
            throw new IllegalArgumentException("사용자 정보가 존재하지 않습니다.");
        }

        Point point = Point.toPoint(userPoint);

        point.charge(dto.amount());

        UserPoint chargedUserPoint = userPointTable.insertOrUpdate(userPoint.id(), point.getPoint());
        pointHistoryTable.insert(chargedUserPoint.id(), dto.amount(), TransactionType.CHARGE, System.currentTimeMillis());

        return chargedUserPoint;
    }
}
