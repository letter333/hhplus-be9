package io.hhplus.tdd.point;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.point.policy.PointPolicy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PointService {
    private final UserPointTable userPointTable;
    private final PointHistoryTable pointHistoryTable;
    private final PointPolicy pointPolicy;

    public UserPoint getPoint(long id) {
        return userPointTable.selectById(id);
    }

    public UserPoint charge(long id, long amount) {
        UserPoint userPoint = userPointTable.selectById(id);
        Point point = Point.toPoint(userPoint, pointPolicy);

        point.charge(amount);

        UserPoint chargedUserPoint = userPointTable.insertOrUpdate(userPoint.id(), point.getPoint());
        pointHistoryTable.insert(chargedUserPoint.id(), amount, TransactionType.CHARGE, System.currentTimeMillis());

        return chargedUserPoint;
    }

    public UserPoint use(long id, long amount) {
        UserPoint userPoint = userPointTable.selectById(id);
        Point point = Point.toPoint(userPoint, pointPolicy);

        point.use(amount);

        UserPoint usedUserPoint = userPointTable.insertOrUpdate(userPoint.id(), point.getPoint());
        pointHistoryTable.insert(usedUserPoint.id(), amount, TransactionType.USE, System.currentTimeMillis());

        return usedUserPoint;
    }

    public List<PointHistory> getPointHistory(long userId) {
        return pointHistoryTable.selectAllByUserId(userId);
    }
}
