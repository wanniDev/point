package io.hhplus.tdd.point

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.util.concurrent.CompletableFuture

@SpringBootTest
class PointControllerIntegrationTest @Autowired constructor(
    private val controller: PointController
){
    private val log = LoggerFactory.getLogger(this.javaClass)!!

    @Test
    @DisplayName("충전, 사용 요청이 동시에 발생하면 순차적으로 처리해야 한다.")
    fun chargeAndUseConcurrent() {
        val balance = controller.point(4).point
        Assertions.assertEquals(0, balance)

        CompletableFuture.allOf(
            CompletableFuture.runAsync { controller.charge(4, 10000) },
            CompletableFuture.runAsync { controller.use(4, 5000) }
        ).join()

        val point = controller.point(4)
        log.info("chargeAndUseConcurrent : $point")
        Assertions.assertEquals(point.point, 5000)
    }
}