package com.wakaba.service

import com.wakaba.domain.Contribution
import com.wakaba.domain.ContributionType
import com.wakaba.mapper.ContributionMapper
import com.wakaba.mapper.UserMapper
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClient
import java.time.LocalDate
import java.time.OffsetDateTime
import java.util.UUID

data class SyncResult(val syncedAt: String, val totalRecords: Int)

@Service
class SyncService(
    private val contributionMapper: ContributionMapper,
    private val userMapper: UserMapper,
    private val restClient: RestClient,
) {
    private val log = LoggerFactory.getLogger(SyncService::class.java)

    @Scheduled(cron = "0 0 * * * *", zone = "Asia/Tokyo")
    fun scheduledSync() {
        log.info("Scheduled sync triggered")
        userMapper.findAllUsersWithAccessToken().forEach { (userId, accessToken) ->
            try {
                syncForUser(userId, accessToken)
            } catch (e: Exception) {
                log.error("Scheduled sync failed for user $userId: ${e.message}")
            }
        }
    }

    fun syncForUser(userId: UUID, accessToken: String): SyncResult {
        val to = LocalDate.now()
        val from = to.minusDays(364)
        val contributions = fetchFromGitHub(userId, accessToken, from, to)
        contributions.forEach { contributionMapper.upsert(it) }
        val syncedAt = OffsetDateTime.now().toString()
        return SyncResult(syncedAt = syncedAt, totalRecords = contributions.size)
    }

    internal fun fetchFromGitHub(
        userId: UUID,
        accessToken: String,
        from: LocalDate,
        to: LocalDate,
    ): List<Contribution> {
        val query = buildGraphQlQuery(from, to)
        val response = callGitHubGraphQL(accessToken, query)
        return parseContributions(userId, response, to)
    }

    private fun buildGraphQlQuery(from: LocalDate, to: LocalDate): String = """
        {
          viewer {
            contributionsCollection(from: "${from}T00:00:00Z", to: "${to}T23:59:59Z") {
              commitContributionsByRepository {
                contributions(first: 100) { nodes { occurredAt } }
              }
              pullRequestContributions(first: 100) { nodes { occurredAt } }
              issueContributions(first: 100) { nodes { occurredAt } }
              pullRequestReviewContributions(first: 100) { nodes { occurredAt } }
            }
          }
        }
    """.trimIndent()

    @Suppress("UNCHECKED_CAST")
    internal fun parseContributions(
        userId: UUID,
        response: Map<String, Any>,
        syncedAtDate: LocalDate,
    ): List<Contribution> {
        val syncedAt = OffsetDateTime.now()
        val result = mutableListOf<Contribution>()

        val viewer = (response["data"] as? Map<*, *>)?.get("viewer") as? Map<*, *> ?: return result
        val collection = viewer["contributionsCollection"] as? Map<*, *> ?: return result

        fun extractDates(nodes: List<*>): List<LocalDate> =
            nodes.filterIsInstance<Map<*, *>>()
                .mapNotNull { (it["occurredAt"] as? String)?.let { s -> LocalDate.parse(s.take(10)) } }

        fun addContributions(dates: List<LocalDate>, type: ContributionType) {
            dates.groupingBy { it }.eachCount().forEach { (date, count) ->
                result += Contribution(
                    userId = userId,
                    contributionDate = date,
                    contributionType = type,
                    count = count,
                    syncedAt = syncedAt,
                )
            }
        }

        val commitsByRepo = collection["commitContributionsByRepository"] as? List<*> ?: emptyList<Any>()
        val allCommitDates = commitsByRepo.filterIsInstance<Map<*, *>>()
            .flatMap { repo ->
                val contributions = repo["contributions"] as? Map<*, *>
                val nodes = contributions?.get("nodes") as? List<*> ?: emptyList<Any>()
                extractDates(nodes)
            }
        addContributions(allCommitDates, ContributionType.COMMIT)

        val prNodes = ((collection["pullRequestContributions"] as? Map<*, *>)?.get("nodes") as? List<*>) ?: emptyList<Any>()
        addContributions(extractDates(prNodes), ContributionType.PR)

        val issueNodes = ((collection["issueContributions"] as? Map<*, *>)?.get("nodes") as? List<*>) ?: emptyList<Any>()
        addContributions(extractDates(issueNodes), ContributionType.ISSUE)

        val reviewNodes = ((collection["pullRequestReviewContributions"] as? Map<*, *>)?.get("nodes") as? List<*>) ?: emptyList<Any>()
        addContributions(extractDates(reviewNodes), ContributionType.REVIEW)

        return result
    }

    private fun callGitHubGraphQL(accessToken: String, query: String): Map<String, Any> {
        @Suppress("UNCHECKED_CAST")
        return restClient.post()
            .uri("https://api.github.com/graphql")
            .header("Authorization", "Bearer $accessToken")
            .header("Content-Type", "application/json")
            .body(mapOf("query" to query))
            .retrieve()
            .body(Map::class.java) as Map<String, Any>? ?: emptyMap()
    }
}
