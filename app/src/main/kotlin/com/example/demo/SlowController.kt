package com.example.demo

import io.micrometer.core.instrument.MeterRegistry
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ResponseBody
import java.util.function.Supplier
import kotlin.random.Random

@Controller
class SlowController(meterRegistry: MeterRegistry) {
    val waitTimes = meterRegistry.timer("wait_times")

    @ResponseBody
    @GetMapping("/slow")
    fun slow(): SlowResponse {
        return waitTimes.record(Supplier<SlowResponse>{
            val delay = Random.nextLong(500, 3000)
            Thread.sleep(delay)
            SlowResponse(delay)
        })
    }
}

data class SlowResponse(val delay: Long)