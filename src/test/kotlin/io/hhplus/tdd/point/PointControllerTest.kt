package io.hhplus.tdd.point

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class PointControllerTest {
    @Autowired
    lateinit var controller: PointController
    private val log = LoggerFactory.getLogger(this.javaClass)!!

    @Test
    fun contextLoads() {
        log.info("calling get point")
        val point = controller.point(1)
        assertEquals(point, UserPoint(1, 0, point.updateMillis))
        assertEquals(emptyList<PointHistory>(), controller.history(1))
        assertEquals(UserPoint(0, 0, 0), controller.charge(1, 10000))
        assertEquals(UserPoint(0, 0, 0), controller.use(1, 5000))
    }


    @Test
    @DisplayName("Table에 저장되지 않은 유저의 id로 조회할 경우, 해당 유저의 id에 point는 0 이며, updateMillis는 현재 시간인 UserPoint 객체를 반환합니다.")
    fun notFoundUserPoint() {
        val point = controller.point(1)
        assertEquals(point.id, 1)
        assertEquals(point.point, 0)
        assertTrue(point.updateMillis > 0)
    }

    @Test
    @DisplayName("포인트 충전 시, 해당 유저의 id에 point를 충전한 point만큼 더하고, updateMillis를 현재 시간으로 설정한 UserPoint 객체를 반환합니다.")
    fun charge() {
        val point = controller.charge(1, 10000)
        assertEquals(point.id, 1)
        assertEquals(point.point, 10000)
        assertTrue(point.updateMillis > 0)
        println(controller.point(1))
    }

}