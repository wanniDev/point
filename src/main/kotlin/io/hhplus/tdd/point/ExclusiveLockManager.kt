package io.hhplus.tdd.point

import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.ReentrantLock

@Component
class ExclusiveLockManager {
    private val locks = ConcurrentHashMap<Long, ReentrantLock>()

    fun <T> execute(id: Long, action: () -> T): T {
        val lock = locks.computeIfAbsent(id) { ReentrantLock() }
        if (lock.tryLock(5, TimeUnit.SECONDS)) {
            try {
                return action()
            } finally {
                lock.unlock()
            }
        }
        throw RuntimeException("Timeout")
    }
}