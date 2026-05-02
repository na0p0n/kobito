package com.wakaba.service

import com.wakaba.domain.ContributionType
import com.wakaba.mapper.ContributionMapper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.springframework.web.client.RestClient
import java.time.LocalDate
import java.util.UUID

class SyncServiceTest {

    private val contributionMapper: ContributionMapper = mock()
    private val restClient: RestClient = mock()
    private lateinit var syncService: SyncService

    private val userId = UUID.randomUUID()
    private val today = LocalDate.of(2026, 5, 1)

    @BeforeEach
    fun setUp() {
        syncService = SyncService(contributionMapper, restClient)
    }

    // ---- parseContributions ----

    @Test
    fun `parseContributions - データが空のとき空リストを返す`() {
        val response = mapOf("data" to mapOf("viewer" to mapOf("contributionsCollection" to emptyMap<String, Any>())))

        val result = syncService.parseContributions(userId, response, today)

        assertThat(result).isEmpty()
    }

    @Test
    fun `parseContributions - data キーがないとき空リストを返す`() {
        val result = syncService.parseContributions(userId, emptyMap(), today)
        assertThat(result).isEmpty()
    }

    @Test
    fun `parseContributions - PR の occurredAt が正しく集計される`() {
        val response = buildResponse(
            prs = listOf("2026-04-15T10:00:00Z", "2026-04-15T15:00:00Z", "2026-04-16T10:00:00Z")
        )

        val result = syncService.parseContributions(userId, response, today)
        val prResults = result.filter { it.contributionType == ContributionType.PR }

        assertThat(prResults).hasSize(2)
        val april15 = prResults.first { it.contributionDate == LocalDate.of(2026, 4, 15) }
        assertThat(april15.count).isEqualTo(2)
        val april16 = prResults.first { it.contributionDate == LocalDate.of(2026, 4, 16) }
        assertThat(april16.count).isEqualTo(1)
    }

    @Test
    fun `parseContributions - Issue の occurredAt が正しく集計される`() {
        val response = buildResponse(
            issues = listOf("2026-04-20T09:00:00Z", "2026-04-20T18:00:00Z")
        )

        val result = syncService.parseContributions(userId, response, today)
        val issueResults = result.filter { it.contributionType == ContributionType.ISSUE }

        assertThat(issueResults).hasSize(1)
        assertThat(issueResults[0].count).isEqualTo(2)
        assertThat(issueResults[0].contributionDate).isEqualTo(LocalDate.of(2026, 4, 20))
    }

    @Test
    fun `parseContributions - Review の occurredAt が正しく集計される`() {
        val response = buildResponse(
            reviews = listOf("2026-04-25T12:00:00Z")
        )

        val result = syncService.parseContributions(userId, response, today)
        val reviewResults = result.filter { it.contributionType == ContributionType.REVIEW }

        assertThat(reviewResults).hasSize(1)
        assertThat(reviewResults[0].count).isEqualTo(1)
    }

    @Test
    fun `parseContributions - Commit が複数リポジトリから集計される`() {
        val response = buildResponseWithCommits(
            listOf(
                listOf("2026-04-10T10:00:00Z", "2026-04-10T11:00:00Z"),
                listOf("2026-04-10T12:00:00Z", "2026-04-11T10:00:00Z"),
            )
        )

        val result = syncService.parseContributions(userId, response, today)
        val commitResults = result.filter { it.contributionType == ContributionType.COMMIT }

        val april10 = commitResults.first { it.contributionDate == LocalDate.of(2026, 4, 10) }
        assertThat(april10.count).isEqualTo(3)
        val april11 = commitResults.first { it.contributionDate == LocalDate.of(2026, 4, 11) }
        assertThat(april11.count).isEqualTo(1)
    }

    @Test
    fun `parseContributions - 各 Contribution の userId が正しく設定される`() {
        val response = buildResponse(prs = listOf("2026-04-15T10:00:00Z"))

        val result = syncService.parseContributions(userId, response, today)

        result.forEach { assertThat(it.userId).isEqualTo(userId) }
    }

    // ---- helpers ----

    private fun buildResponse(
        prs: List<String> = emptyList(),
        issues: List<String> = emptyList(),
        reviews: List<String> = emptyList(),
    ): Map<String, Any> = mapOf(
        "data" to mapOf(
            "viewer" to mapOf(
                "contributionsCollection" to mapOf(
                    "commitContributionsByRepository" to emptyList<Any>(),
                    "pullRequestContributions" to mapOf("nodes" to prs.map { mapOf("occurredAt" to it) }),
                    "issueContributions" to mapOf("nodes" to issues.map { mapOf("occurredAt" to it) }),
                    "pullRequestReviewContributions" to mapOf("nodes" to reviews.map { mapOf("occurredAt" to it) }),
                )
            )
        )
    )

    private fun buildResponseWithCommits(commitsByRepo: List<List<String>>): Map<String, Any> = mapOf(
        "data" to mapOf(
            "viewer" to mapOf(
                "contributionsCollection" to mapOf(
                    "commitContributionsByRepository" to commitsByRepo.map { dates ->
                        mapOf(
                            "contributions" to mapOf(
                                "nodes" to dates.map { mapOf("occurredAt" to it) }
                            )
                        )
                    },
                    "pullRequestContributions" to mapOf("nodes" to emptyList<Any>()),
                    "issueContributions" to mapOf("nodes" to emptyList<Any>()),
                    "pullRequestReviewContributions" to mapOf("nodes" to emptyList<Any>()),
                )
            )
        )
    )
}
