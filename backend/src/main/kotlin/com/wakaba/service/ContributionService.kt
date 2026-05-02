package com.wakaba.service

import com.wakaba.domain.Contribution
import com.wakaba.domain.ContributionType
import com.wakaba.mapper.ContributionMapper
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.util.UUID

data class DailySummary(
    val date: LocalDate,
    val commitCount: Int,
    val prCount: Int,
    val issueCount: Int,
    val reviewCount: Int,
)

data class WeeklyTotal(
    val commit: Int,
    val pr: Int,
    val issue: Int,
    val review: Int,
)

@Service
class ContributionService(private val contributionMapper: ContributionMapper) {

    fun getSummary(userId: UUID, from: LocalDate, to: LocalDate): List<DailySummary> {
        val rows = contributionMapper.findByUserAndDateRange(userId, from, to)
        val byDate = rows.groupBy { it.contributionDate }
        return generateSequence(from) { it.plusDays(1) }
            .takeWhile { !it.isAfter(to) }
            .map { date ->
                val group = byDate[date] ?: emptyList()
                DailySummary(
                    date = date,
                    commitCount = group.countOf(ContributionType.COMMIT),
                    prCount = group.countOf(ContributionType.PR),
                    issueCount = group.countOf(ContributionType.ISSUE),
                    reviewCount = group.countOf(ContributionType.REVIEW),
                )
            }.toList()
    }

    fun getWeeklyTotal(userId: UUID): WeeklyTotal {
        val to = LocalDate.now()
        val from = to.minusDays(6)
        val rows = contributionMapper.findByUserAndDateRange(userId, from, to)
        return WeeklyTotal(
            commit = rows.countOf(ContributionType.COMMIT),
            pr = rows.countOf(ContributionType.PR),
            issue = rows.countOf(ContributionType.ISSUE),
            review = rows.countOf(ContributionType.REVIEW),
        )
    }

    fun getLastSyncedAt(userId: UUID): String? = contributionMapper.findLastSyncedAt(userId)

    private fun List<Contribution>.countOf(type: ContributionType): Int =
        filter { it.contributionType == type }.sumOf { it.count }
}
