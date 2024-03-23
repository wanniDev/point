package io.hhplus.tdd.point

import com.fasterxml.jackson.databind.ObjectMapper
import io.hhplus.tdd.database.PointHistoryTable
import io.hhplus.tdd.database.UserPointTable
import io.hhplus.tdd.point.domain.UserPoint
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.*
import java.util.concurrent.Executors

@RestController
@RequestMapping("/point")
class PointController(
    private val userPointTable: UserPointTable,
    private val pointHistoryTable: PointHistoryTable,
    private val exclusiveLockManager: ExclusiveLockManager,
) {
    private val logger: Logger = LoggerFactory.getLogger(javaClass)
    private val executor = Executors.newFixedThreadPool(10)

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
        return exclusiveLockManager.execute(id) {
            chargePoint(id, amount)
        }
    }

    private fun chargePoint(id: Long, amount: Long): UserPoint {
        executor.submit {
            val currentNanoMillis = System.nanoTime()
            logger.info("$id, $amount, ${TransactionType.CHARGE}, $currentNanoMillis")
            pointHistoryTable.insert(id, amount, TransactionType.CHARGE, currentNanoMillis)
        }
        return exclusiveLockManager.execute(id) {
            val userPoint = userPointTable.selectById(id)
            userPointTable.insertOrUpdate(id, userPoint.point + amount)
        }
    }

    /**
     * TODO - 특정 유저의 포인트를 사용하는 기능을 작성해주세요.
     */
    @PatchMapping("{id}/use")
    fun use(
        @PathVariable id: Long,
        @RequestBody amount: Long,
    ): UserPoint {
        return exclusiveLockManager.execute(id) {
            usePoint(id, amount)
        }
    }

    private fun usePoint(id: Long, amount: Long): UserPoint {
        executor.submit {
            val currentNanoMillis = System.nanoTime()
            logger.info("$id, $amount, ${TransactionType.USE}, $currentNanoMillis")
            pointHistoryTable.insert(id, amount, TransactionType.USE, currentNanoMillis)
        }
        return exclusiveLockManager.execute(id) {
            val userPoint = userPointTable.selectById(id)
            if (userPoint.point < amount) {
                throw IllegalArgumentException("포인트가 부족합니다.")
            }
            userPointTable.insertOrUpdate(id, userPoint.point - amount)
        }
    }
}