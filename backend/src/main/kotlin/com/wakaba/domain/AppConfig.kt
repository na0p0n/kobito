package com.wakaba.domain

import java.time.OffsetDateTime

data class AppConfig(
    val key: String,
    val value: String?,
    val updatedAt: OffsetDateTime,
)

data class ConfigView(
    val discordWebhookUrl: String?,
    val digestSendDay: String,
    val digestSendHour: Int,
)
