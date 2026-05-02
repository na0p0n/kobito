package com.wakaba.controller

import com.wakaba.service.ContributionService
import com.wakaba.service.DailySummary
import com.wakaba.service.WeeklyTotal
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.LocalDate
import java.util.UUID

@RestController
@RequestMapping("/api/contributions")
class ContributionController(private val contributionService: ContributionService) {

    @GetMapping("/summary")
    fun summary(
        @RequestHeader("X-User-Id") userId: UUID,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) from: LocalDate,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) to: LocalDate,
    ): ResponseEntity<Map<String, List<DailySummary>>> {
        val items = contributionService.getSummary(userId, from, to)
        return ResponseEntity.ok(mapOf("items" to items))
    }

    @GetMapping("/weekly")
    fun weekly(@RequestHeader("X-User-Id") userId: UUID): ResponseEntity<Map<String, WeeklyTotal>> {
        val week = contributionService.getWeeklyTotal(userId)
        return ResponseEntity.ok(mapOf("week" to week))
    }
}
