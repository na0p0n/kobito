package com.wakaba.controller

import com.wakaba.domain.ConfigView
import com.wakaba.service.ConfigService
import com.wakaba.service.DiscordDigestService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

data class UpdateConfigRequest(val discordWebhookUrl: String? = null)

@RestController
@RequestMapping("/api")
class ConfigController(
    private val configService: ConfigService,
    private val discordDigestService: DiscordDigestService,
) {
    @GetMapping("/config")
    fun getConfig(): ResponseEntity<ConfigView> =
        ResponseEntity.ok(configService.getConfig())

    @PutMapping("/config")
    fun updateConfig(@RequestBody req: UpdateConfigRequest): ResponseEntity<ConfigView> =
        ResponseEntity.ok(configService.updateDiscordWebhookUrl(req.discordWebhookUrl))

    @PostMapping("/digest/trigger")
    fun triggerDigest(): ResponseEntity<Map<String, Boolean>> {
        val sent = discordDigestService.triggerManual()
        return ResponseEntity.ok(mapOf("sent" to sent))
    }
}
