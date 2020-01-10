package com.example.demo

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseBody

@Controller
class LeakController {
    val leakBuffer: MutableList<ByteArray> = mutableListOf()


    @ResponseBody
    @PostMapping("leak")
    fun leak(): LeakResponse {
        val numMbs = 5
        val numKbs = numMbs * 1024
        val numBytes = numKbs * 1024
        leakBuffer.add(ByteArray(numBytes))
        return LeakResponse(leakBuffer.size, numKbs)
    }

}

data class LeakResponse(val numBlocksLeaked: Int, val lastBlockSizeKb: Int)