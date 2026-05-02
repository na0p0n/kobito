package com.wakaba.service

import com.wakaba.mapper.AppConfigMapper
import com.wakaba.mapper.ContributionMapper
import com.wakaba.mapper.GoalMapper
import com.wakaba.mapper.UserMapper
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClient
import java.time.LocalDate
import java.util.UUID

@Service
class DiscordDigestService(
    private val contributionMapper: ContributionMapper,
    private val goalMapper: GoalMapper,
    private val appConfigMapper: AppConfigMapper,
    private val goalService: GoalService,
    private val userMapper: UserMapper,
    private val restClient: RestClient,
) {
    private val log = LoggerFactory.getLogger(DiscordDigestService::class.java)

    @Scheduled(cron = "0 0 9 * * MON", zone = "Asia/Tokyo")
    fun sendWeeklyDigest() {
        val webhookUrl = appConfigMapper.findByKey("discord_webhook_url")?.value
        if (webhookUrl.isNullOrBlank()) {
            log.info("Discord webhook URL not configured; skipping weekly digest")
            return
        }
        sendDigestToAllUsers(webhookUrl)
    }

    fun triggerManual(): Boolean {
        val webhookUrl = appConfigMapper.findByKey("discord_webhook_url")?.value
        if (webhookUrl.isNullOrBlank()) return false
        sendDigestToAllUsers(webhookUrl)
        return true
    }

    fun buildDigestPayload(
        userId: UUID,
        webhookUrl: String,
        weekFrom: LocalDate,
        weekTo: LocalDate,
    ): Map<String, Any> {
        val rows = contributionMapper.findByUserAndDateRange(userId, weekFrom, weekTo)
        val commit = rows.filter { it.contributionType.name == "COMMIT" }.sumOf { it.count }
        val pr = rows.filter { it.contributionType.name == "PR" }.sumOf { it.count }
        val issue = rows.filter { it.contributionType.name == "ISSUE" }.sumOf { it.count }
        val review = rows.filter { it.contributionType.name == "REVIEW" }.sumOf { it.count }

        val goalsWithProgress = goalService.getGoalsWithProgress(userId)
        val achievedCount = goalsWithProgress.count { it.achieved }
        val totalCount = goalsWithProgress.size

        val description = buildString {
            append("📊 **週次サマリー** ($weekFrom ～ $weekTo)\n\n")
            append("| 種別 | 件数 |\n|---|---|\n")
            append("| Commit | $commit |\n")
            append("| PR | $pr |\n")
            append("| Issue | $issue |\n")
            append("| Review | $review |\n\n")
            if (totalCount > 0) {
                append("🎯 **ゴール進捗**: $achievedCount / $totalCount 達成\n")
            }
        }

        return mapOf(
            "embeds" to listOf(
                mapOf(
                    "title" to "Wakaba 週次レポート",
                    "description" to description,
                    "color" to 0x57F287,
                )
            )
        )
    }

    private fun sendDigestToAllUsers(webhookUrl: String) {
        val to = LocalDate.now().minusDays(1)
        val from = to.minusDays(6)
        userMapper.findAllUserIds().forEach { userId ->
            val payload = buildDigestPayload(userId, webhookUrl, from, to)
            postToDiscord(webhookUrl, payload)
        }
    }

    internal fun postToDiscord(webhookUrl: String, payload: Map<String, Any>) {
        try {
            restClient.post()
                .uri(webhookUrl)
                .header("Content-Type", "application/json")
                .body(payload)
                .retrieve()
                .toBodilessEntity()
        } catch (e: Exception) {
            log.error("Failed to post to Discord: ${e.message}")
        }
    }
}
