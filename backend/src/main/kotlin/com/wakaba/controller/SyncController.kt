package com.wakaba.controller

import com.wakaba.service.SyncResult
import com.wakaba.service.SyncService
import com.wakaba.service.ContributionService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/api/sync")
class SyncController(
    private val syncService: SyncService,
    private val contributionService: ContributionService,
) {
    @PostMapping
    fun sync(
        @RequestHeader("X-User-Id") userId: UUID,
        @RequestHeader("X-Access-Token") accessToken: String,
    ): ResponseEntity<SyncResult> {
        val result = syncService.syncForUser(userId, accessToken)
        return ResponseEntity.ok(result)
    }

    @GetMapping("/status")
    fun status(@RequestHeader("X-User-Id") userId: UUID): ResponseEntity<Map<String, String?>> {
        val lastSyncedAt = contributionService.getLastSyncedAt(userId)
        return ResponseEntity.ok(mapOf("lastSyncedAt" to lastSyncedAt))
    }
}
