package com.wakaba.service

import com.wakaba.domain.ConfigView
import com.wakaba.mapper.AppConfigMapper
import org.springframework.stereotype.Service

@Service
class ConfigService(private val appConfigMapper: AppConfigMapper) {

    fun getConfig(): ConfigView {
        val all = appConfigMapper.findAll().associateBy { it.key }
        return ConfigView(
            discordWebhookUrl = all["discord_webhook_url"]?.value,
            digestSendDay = all["digest_send_day"]?.value ?: "MONDAY",
            digestSendHour = all["digest_send_hour"]?.value?.toIntOrNull() ?: 9,
        )
    }

    fun updateDiscordWebhookUrl(url: String?): ConfigView {
        appConfigMapper.update("discord_webhook_url", url)
        return getConfig()
    }
}
