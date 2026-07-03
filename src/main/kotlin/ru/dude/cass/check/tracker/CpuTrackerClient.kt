package ru.dude.cass.check.tracker

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import java.time.Duration

@Service
internal class CpuTrackerClient(
    @Value("\${casstool.tracker.url:}") private val trackerUrl: String
) {
    // Создаем WebClient с базовым URL трекера
    private val webClient = WebClient.builder()
        .baseUrl(trackerUrl)
        .build()

    /**
     * Отправляет GET /start для запуска мониторинга
     */
    fun startMeasurement(): TrackerResponse? {
        return webClient.get()
            .uri("/start")
            .retrieve()
            .bodyToMono(TrackerResponse::class.java)
            // Задаем таймаут на случай, если Linux-сервер недоступен
            .block(Duration.ofSeconds(5))
    }

    /**
     * Отправляет GET /stop, завершает мониторинг и возвращает метрики
     */
    fun stopMeasurement(): TrackerResponse? {
        return webClient.get()
            .uri("/stop")
            .retrieve()
            .bodyToMono(TrackerResponse::class.java)
            .block(Duration.ofSeconds(5))
    }
}
