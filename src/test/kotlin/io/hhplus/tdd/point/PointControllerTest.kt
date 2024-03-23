package io.hhplus.tdd.point

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors

@SpringBootTest
class PointControllerTest {
    @Autowired
    lateinit var controller: PointController
    private val log = LoggerFactory.getLogger(this.javaClass)!!

    @Test
    @DisplayName("Table에 저장되지 않은 유저의 id로 조회할 경우, 해당 유저의 id에 point는 0 이며, updateMillis는 현재 시간인 UserPoint 객체를 반환합니다.")
    fun notFoundUserPoint() {
        val point = controller.point(2)
        log.info("notFoundUserPoint : $point")
        assertEquals(point.id, 2)
        assertEquals(point.point, 0)
        assertTrue(point.updateMillis > 0)
    }

    @Test
    @DisplayName("포인트 충전 시, 해당 유저의 id에 point를 충전한 point만큼 더하고, updateMillis를 현재 시간으로 설정한 UserPoint 객체를 반환합니다.")
    fun charge() {
        val point = controller.charge(1, 10000)
        log.info("charge : $point")
        assertEquals(point.id, 1)
        assertEquals(point.point, 10000)
        assertTrue(point.updateMillis > 0)
    }

    @Test
    @DisplayName("포인트 사용 시, 해당 유저의 id에 point를 사용한 point만큼 빼고, updateMillis를 현재 시간으로 설정한 UserPoint 객체를 반환합니다.")
    fun use() {
        controller.charge(99, 10000)
        val point = controller.use(99, 5000)
        log.info("use : $point")
        assertEquals(point.id, 99)
        assertEquals(point.point, 5000)
        assertTrue(point.updateMillis > 0)
    }

    @Test
    @DisplayName("포인트 사용 시, 해당 유저의 id에 point가 부족할 경우, IllegalArgumentException을 반환합니다.")
    fun useNotEnoughPoint() {
        controller.charge(100, 10000)
        val exception = assertThrows<IllegalArgumentException> {
            controller.use(100, 20000)
        }
        log.info("useNotEnoughPoint : $exception")
        assertEquals(exception.message, "포인트가 부족합니다.")
    }

    @Test
    @DisplayName("특정 사용자의 포인트 충전/이용 내역이 없을 경우, 비어있는 리스트를 반환합니다.")
    fun emptyHistory() {
        val histories = controller.history(888)
        log.info("emptyHistory : $histories")
        assertTrue(histories.isEmpty())
    }

    @Test
    @DisplayName("특정 사용자의 포인트 충전/이용 내역이 있을 경우, 해당 내역을 반환합니다.")
    fun notEmptyHistory() {
        controller.charge(900, 10000)
        val histories = controller.history(900)
        log.info("notEmptyHistory : $histories")
        assertEquals(histories.size, 1)
        assertEquals(histories[0].amount, 10000)
    }

    @Test
    @DisplayName("충전, 사용 요청이 동시에 발생하면 순차적으로 처리해야 한다.")
    fun chargeAndUseConcurrent() {
        val balance = controller.point(4).point
        assertEquals(0, balance)

        CompletableFuture.allOf(
            CompletableFuture.runAsync { controller.charge(4, 10000) },
            CompletableFuture.runAsync { controller.use(4, 5000) }
        ).join()

        val point = controller.point(4)
        log.info("chargeAndUseConcurrent : $point")
        assertEquals(point.point, 5000)
    }
}