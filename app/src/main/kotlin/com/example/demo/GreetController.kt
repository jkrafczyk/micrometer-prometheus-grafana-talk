package com.example.demo

import io.micrometer.core.instrument.DistributionSummary
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Timer
import io.prometheus.client.Summary
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody

@Controller
class GreetController(meterRegistry: MeterRegistry) {
    val nameMetric = DistributionSummary
            .builder("name_length")
            .publishPercentileHistogram()
            .minimumExpectedValue(1)
            .maximumExpectedValue(20)
            .register(meterRegistry)

    @GetMapping("/greet")
    @ResponseBody
    fun greet(@RequestParam("name", defaultValue = "world") name: String): String {
        nameMetric.record(name.length.toDouble())
        return "Hello, ${name}"
    }
}

