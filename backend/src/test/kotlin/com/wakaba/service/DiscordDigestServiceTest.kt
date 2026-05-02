package com.wakaba.service

import com.wakaba.domain.AppConfig
import com.wakaba.domain.ContributionType
import com.wakaba.domain.GoalWithProgress
import com.wakaba.mapper.AppConfigMapper
import com.wakaba.mapper.ContributionMapper
import com.wakaba.mapper.GoalMapper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*
import org.springframework.web.client.RestClient
import java.time.LocalDate
import java.time.OffsetDateTime
import java.util.UUID
import com.wakaba.domain.Contribution

class DiscordDigestServiceTest {

    private val contributionMapper: ContributionMapper = mock()
    private val goalMapper: GoalMapper = mock()
    private val appConfigMapper: AppConfigMapper = mock()
    private val goalService: GoalService = mock()
    private val restClient: RestClient = mock()
    private lateinit var discordDigestService: DiscordDigestService

    private val userId = UUID.randomUUID()
    private val weekFrom = LocalDate.of(2026, 4, 27)
    private val weekTo = LocalDate.of(2026, 5, 3)
    private val now = OffsetDateTime.now()

    @BeforeEach
    fun setUp() {
        discordDigestService = DiscordDigestService(
            contributionMapper, goalMapper, appConfigMapper, goalService, restClient
        )
    }

    // ---- buildDigestPayload ----

    @Test
    fun `buildDigestPayload - contribution がないとき0カウントのペイロードを返す`() {
        whenever(contributionMapper.findByUserAndDateRange(userId, weekFrom, weekTo)).thenReturn(emptyList())
        whenever(goalService.getGoalsWithProgress(userId)).thenReturn(emptyList())

        val payload = discordDigestService.buildDigestPayload(userId, "https://example.com", weekFrom, weekTo)

        assertThat(payload).containsKey("embeds")
        val embeds = payload["embeds"] as List<*>
        assertThat(embeds).hasSize(1)
        val embed = embeds[0] as Map<*, *>
        val description = embed["description"] as String
        assertThat(description).contains("Commit | 0")
        assertThat(description).contains("PR | 0")
    }

    @Test
    fun `buildDigestPayload - contribution があるとき正しいカウントを含む`() {
        val contributions = listOf(
            contribution(weekFrom, ContributionType.COMMIT, 5),
            contribution(weekFrom, ContributionType.PR, 2),
            contribution(weekFrom.plusDays(1), ContributionType.ISSUE, 3),
            contribution(weekFrom.plusDays(2), ContributionType.REVIEW, 1),
        )
        whenever(contributionMapper.findByUserAndDateRange(userId, weekFrom, weekTo)).thenReturn(contributions)
        whenever(goalService.getGoalsWithProgress(userId)).thenReturn(emptyList())

        val payload = discordDigestService.buildDigestPayload(userId, "https://example.com", weekFrom, weekTo)

        val embeds = payload["embeds"] as List<*>
        val embed = embeds[0] as Map<*, *>
        val description = embed["description"] as String
        assertThat(description).contains("Commit | 5")
        assertThat(description).contains("PR | 2")
        assertThat(description).contains("Issue | 3")
        assertThat(description).contains("Review | 1")
    }

    @Test
    fun `buildDigestPayload - ゴールがある場合に進捗情報を含む`() {
        whenever(contributionMapper.findByUserAndDateRange(userId, weekFrom, weekTo)).thenReturn(emptyList())
        val goals = listOf(
            goalWithProgress(achieved = true),
            goalWithProgress(achieved = false),
            goalWithProgress(achieved = true),
        )
        whenever(goalService.getGoalsWithProgress(userId)).thenReturn(goals)

        val payload = discordDigestService.buildDigestPayload(userId, "https://example.com", weekFrom, weekTo)

        val embeds = payload["embeds"] as List<*>
        val embed = embeds[0] as Map<*, *>
        val description = embed["description"] as String
        assertThat(description).contains("2 / 3")
    }

    @Test
    fun `buildDigestPayload - ゴールがない場合にゴール進捗情報を含まない`() {
        whenever(contributionMapper.findByUserAndDateRange(userId, weekFrom, weekTo)).thenReturn(emptyList())
        whenever(goalService.getGoalsWithProgress(userId)).thenReturn(emptyList())

        val payload = discordDigestService.buildDigestPayload(userId, "https://example.com", weekFrom, weekTo)

        val embeds = payload["embeds"] as List<*>
        val embed = embeds[0] as Map<*, *>
        val description = embed["description"] as String
        assertThat(description).doesNotContain("ゴール進捗")
    }

    // ---- triggerManual ----

    @Test
    fun `triggerManual - webhook URL が設定されていないとき false を返す`() {
        whenever(appConfigMapper.findByKey("discord_webhook_url")).thenReturn(
            AppConfig("discord_webhook_url", null, now)
        )

        val result = discordDigestService.triggerManual()

        assertThat(result).isFalse()
    }

    @Test
    fun `triggerManual - webhook URL が空文字のとき false を返す`() {
        whenever(appConfigMapper.findByKey("discord_webhook_url")).thenReturn(
            AppConfig("discord_webhook_url", "", now)
        )

        val result = discordDigestService.triggerManual()

        assertThat(result).isFalse()
    }

    // ---- helpers ----

    private fun contribution(date: LocalDate, type: ContributionType, count: Int) = Contribution(
        userId = userId,
        contributionDate = date,
        contributionType = type,
        count = count,
        syncedAt = now,
    )

    private fun goalWithProgress(achieved: Boolean) = GoalWithProgress(
        id = 1L,
        userId = userId,
        title = "Test Goal",
        contributionType = ContributionType.PR,
        targetCount = 5,
        startDate = weekFrom,
        endDate = weekTo,
        currentCount = if (achieved) 5 else 2,
        achieved = achieved,
    )
}
