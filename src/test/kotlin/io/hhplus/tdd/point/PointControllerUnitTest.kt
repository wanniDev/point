package io.hhplus.tdd.point

import io.hhplus.tdd.point.domain.UserPoint
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.mock
import org.slf4j.LoggerFactory

@ExtendWith(MockitoExtension::class)
class PointControllerUnitTest {
    private val log = LoggerFactory.getLogger(this.javaClass)!!

    @Test
    @DisplayName("Table에 저장되지 않은 유저의 id로 조회할 경우, 해당 유저의 id에 point는 0 이며, updateMillis는 현재 시간인 UserPoint 객체를 반환합니다.")
    fun notFoundUserPoint() {
        // given
        val mockPointController = mock<PointController> {
            on { point(2) } doReturn UserPoint(2, 0, System.nanoTime())
        }

        // when
        val point = mockPointController.point(2)

        // then
        log.info("notFoundUserPoint : $point")
        assertEquals(point.id, 2)
        assertEquals(point.point, 0)
        assertTrue(point.updateMillis > 0)
    }

    @Test
    @DisplayName("포인트 충전 시, 해당 유저의 id에 point를 충전한 point만큼 더하고, updateMillis를 현재 시간으로 설정한 UserPoint 객체를 반환합니다.")
    fun charge() {
        // given
        val mockPointController = mock<PointController> {
            on { charge(1, 10000) } doReturn UserPoint(1, 10000, System.nanoTime())
        }

        // when
        val point = mockPointController.charge(1, 10000)

        // then
        log.info("charge : $point")
        assertEquals(point.id, 1)
        assertEquals(point.point, 10000)
        assertTrue(point.updateMillis > 0)
    }

    @Test
    @DisplayName("포인트 사용 시, 해당 유저의 id에 point를 사용한 point만큼 빼고, updateMillis를 현재 시간으로 설정한 UserPoint 객체를 반환합니다.")
    fun use() {
        // given
        val mockPointController = mock<PointController> {
            on { use(1, 5000) } doReturn UserPoint(1, 5000, System.nanoTime())
        }

        // when
        val point = mockPointController.use(1, 5000)

        // then
        log.info("use : $point")
        assertEquals(point.id, 1)
        assertEquals(point.point, 5000)
        assertTrue(point.updateMillis > 0)
    }

    @Test
    @DisplayName("포인트 사용 시, 해당 유저의 id에 point가 부족할 경우, IllegalArgumentException을 반환합니다.")
    fun useNotEnoughPoint() {
        // given
        val mockPointController = mock<PointController> {
            on { use(any(), any()) } doThrow IllegalArgumentException("포인트가 부족합니다.")
        }

        // when
        val exception = assertThrows<IllegalArgumentException> {
            mockPointController.use(100, 20000)
        }

        // then
        log.info("useNotEnoughPoint : $exception")
        assertEquals(exception.message, "포인트가 부족합니다.")
    }

    @Test
    @DisplayName("특정 사용자의 포인트 충전/이용 내역이 없을 경우, 비어있는 리스트를 반환합니다.")
    fun emptyHistory() {
        // given
        val mockPointController = mock<PointController> {
            on { history(888) } doReturn emptyList()
        }

        // when
        val histories = mockPointController.history(888)

        // then
        log.info("emptyHistory : $histories")
        assertTrue(histories.isEmpty())
    }

    @Test
    @DisplayName("특정 사용자의 포인트 충전/이용 내역이 있을 경우, 해당 내역을 반환합니다.")
    fun notEmptyHistory() {
        // given
        val mockPointController = mock<PointController> {
            on { history(900) } doReturn listOf(
                PointHistory(1, 900, TransactionType.CHARGE, 10000, System.nanoTime())
            )
        }

        // when
        val histories = mockPointController.history(900)

        // then
        log.info("notEmptyHistory : $histories")
        assertEquals(histories.size, 1)
        assertEquals(histories[0].amount, 10000)
    }
}