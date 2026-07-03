package ru.dude.cass.check.tracker

import com.fasterxml.jackson.annotation.JsonProperty

internal data class TrackerResponse(
    val status: String,
    @JsonProperty("cpu_average_total") val cpuAverageTotal: Double?,
    @JsonProperty("ram_average_mb") val ramAverageMb: Double?,
    @JsonProperty("ram_max_mb") val ramMaxMb: Int?,
    val message: String? = null
)
