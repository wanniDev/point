package io.hhplus.tdd.point

import io.hhplus.tdd.database.PointHistoryTable
import io.hhplus.tdd.database.UserPointTable
import io.hhplus.tdd.point.domain.UserPoint
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.ReentrantLock

@RestController
@RequestMapping("/point")
class PointController(
    private val userPointTable: UserPointTable,
    private val pointHistoryTable: PointHistoryTable,
) {
    private val logger: Logger = LoggerFactory.getLogger(javaClass)
    private val locks = ConcurrentHashMap<Long, ReentrantLock>()
    
    /**
     * TODO - 특정 유저의 포인트를 조회하는 기능을 작성해주세요.
     */
    @GetMapping("{id}")
    fun point(
        @PathVariable id: Long,
    ): UserPoint {
        return userPointTable.selectById(id)
    }

    /**
     * TODO - 특정 유저의 포인트 충전/이용 내역을 조회하는 기능을 작성해주세요.
     */
    @GetMapping("{id}/histories")
    fun history(
        @PathVariable id: Long,
    ): List<PointHistory> {
        return pointHistoryTable.selectAllByUserId(id);
    }

    /**
     * TODO - 특정 유저의 포인트를 충전하는 기능을 작성해주세요.
     */
    @PatchMapping("{id}/charge")
    fun charge(
        @PathVariable id: Long,
        @RequestBody amount: Long,
    ): UserPoint {
        val lock = locks.computeIfAbsent(id) { ReentrantLock() }
        if (lock.tryLock(5, TimeUnit.SECONDS)) {
            try {
                return chargePoint(id, amount)
            } finally {
                lock.unlock()
            }
        }
        throw RuntimeException("Timeout")
    }

    private fun chargePoint(id: Long, amount: Long): UserPoint {
        pointHistoryTable.insert(id, amount, TransactionType.CHARGE, System.currentTimeMillis())
        return userPointTable.insertOrUpdate(id, amount)
    }

    /**
     * TODO - 특정 유저의 포인트를 사용하는 기능을 작성해주세요.
     */
    @PatchMapping("{id}/use")
    fun use(
        @PathVariable id: Long,
        @RequestBody amount: Long,
    ): UserPoint {
        val lock = locks.computeIfAbsent(id) { ReentrantLock() }
        if (lock.tryLock(5, TimeUnit.SECONDS)) {
            try {
                return usePoint(id, amount)
            } finally {
                lock.unlock()
            }
        }
        throw RuntimeException("Timeout")
    }

    private fun usePoint(id: Long, amount: Long): UserPoint {
        val userPoint = userPointTable.selectById(id)
        if (userPoint.point < amount) {
            throw IllegalArgumentException("포인트가 부족합니다.")
        }
        pointHistoryTable.insert(id, amount, TransactionType.USE, System.currentTimeMillis())
        return userPointTable.insertOrUpdate(id, userPoint.point - amount)
    }
}