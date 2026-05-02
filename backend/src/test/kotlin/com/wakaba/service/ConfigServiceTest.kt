package com.wakaba.service

import com.wakaba.domain.AppConfig
import com.wakaba.mapper.AppConfigMapper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*
import java.time.OffsetDateTime

class ConfigServiceTest {

    private val appConfigMapper: AppConfigMapper = mock()
    private lateinit var configService: ConfigService

    private val now = OffsetDateTime.now()

    @BeforeEach
    fun setUp() {
        configService = ConfigService(appConfigMapper)
    }

    private fun allConfigs(webhookUrl: String?) = listOf(
        AppConfig("discord_webhook_url", webhookUrl, now),
        AppConfig("digest_send_day", "MONDAY", now),
        AppConfig("digest_send_hour", "9", now),
    )

    // ---- getConfig ----

    @Test
    fun `getConfig - discord_webhook_url が設定されている場合に返す`() {
        whenever(appConfigMapper.findAll()).thenReturn(allConfigs("https://discord.com/api/webhooks/xxx"))

        val result = configService.getConfig()

        assertThat(result.discordWebhookUrl).isEqualTo("https://discord.com/api/webhooks/xxx")
        assertThat(result.digestSendDay).isEqualTo("MONDAY")
        assertThat(result.digestSendHour).isEqualTo(9)
    }

    @Test
    fun `getConfig - discord_webhook_url が null のとき null を返す`() {
        whenever(appConfigMapper.findAll()).thenReturn(allConfigs(null))

        val result = configService.getConfig()

        assertThat(result.discordWebhookUrl).isNull()
    }

    @Test
    fun `getConfig - digest_send_hour が数値でないとき 9 をデフォルトで返す`() {
        val configs = listOf(
            AppConfig("discord_webhook_url", null, now),
            AppConfig("digest_send_day", "MONDAY", now),
            AppConfig("digest_send_hour", "invalid", now),
        )
        whenever(appConfigMapper.findAll()).thenReturn(configs)

        val result = configService.getConfig()

        assertThat(result.digestSendHour).isEqualTo(9)
    }

    @Test
    fun `getConfig - レコードが存在しないとき デフォルト値を返す`() {
        whenever(appConfigMapper.findAll()).thenReturn(emptyList())

        val result = configService.getConfig()

        assertThat(result.discordWebhookUrl).isNull()
        assertThat(result.digestSendDay).isEqualTo("MONDAY")
        assertThat(result.digestSendHour).isEqualTo(9)
    }

    // ---- updateDiscordWebhookUrl ----

    @Test
    fun `updateDiscordWebhookUrl - 正しく update を呼び出し更新後の config を返す`() {
        val newUrl = "https://discord.com/api/webhooks/new"
        whenever(appConfigMapper.update("discord_webhook_url", newUrl)).thenReturn(1)
        whenever(appConfigMapper.findAll()).thenReturn(allConfigs(newUrl))

        val result = configService.updateDiscordWebhookUrl(newUrl)

        verify(appConfigMapper).update("discord_webhook_url", newUrl)
        assertThat(result.discordWebhookUrl).isEqualTo(newUrl)
    }

    @Test
    fun `updateDiscordWebhookUrl - null を渡すと webhook URL が null になる`() {
        whenever(appConfigMapper.update("discord_webhook_url", null)).thenReturn(1)
        whenever(appConfigMapper.findAll()).thenReturn(allConfigs(null))

        val result = configService.updateDiscordWebhookUrl(null)

        verify(appConfigMapper).update("discord_webhook_url", null)
        assertThat(result.discordWebhookUrl).isNull()
    }
}
