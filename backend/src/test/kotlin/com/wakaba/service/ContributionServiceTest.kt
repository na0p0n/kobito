package com.wakaba.service

import com.wakaba.domain.Contribution
import com.wakaba.domain.ContributionType
import com.wakaba.mapper.ContributionMapper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*
import java.time.LocalDate
import java.time.OffsetDateTime
import java.util.UUID

class ContributionServiceTest {

    private val contributionMapper: ContributionMapper = mock()
    private lateinit var contributionService: ContributionService

    private val userId = UUID.randomUUID()
    private val now = OffsetDateTime.now()

    @BeforeEach
    fun setUp() {
        contributionService = ContributionService(contributionMapper)
    }

    // ---- getSummary ----

    @Test
    fun `getSummary - データなしのとき全日付で0カウントのエントリを返す`() {
        val from = LocalDate.of(2026, 5, 1)
        val to = LocalDate.of(2026, 5, 3)
        whenever(contributionMapper.findByUserAndDateRange(userId, from, to)).thenReturn(emptyList())

        val result = contributionService.getSummary(userId, from, to)

        assertThat(result).hasSize(3)
        result.forEach { summary ->
            assertThat(summary.commitCount).isEqualTo(0)
            assertThat(summary.prCount).isEqualTo(0)
            assertThat(summary.issueCount).isEqualTo(0)
            assertThat(summary.reviewCount).isEqualTo(0)
        }
    }

    @Test
    fun `getSummary - 各タイプの件数が正しく集計される`() {
        val date = LocalDate.of(2026, 5, 1)
        val contributions = listOf(
            contribution(date, ContributionType.COMMIT, 3),
            contribution(date, ContributionType.PR, 1),
            contribution(date, ContributionType.ISSUE, 2),
            contribution(date, ContributionType.REVIEW, 4),
        )
        whenever(contributionMapper.findByUserAndDateRange(userId, date, date)).thenReturn(contributions)

        val result = contributionService.getSummary(userId, date, date)

        assertThat(result).hasSize(1)
        assertThat(result[0].commitCount).isEqualTo(3)
        assertThat(result[0].prCount).isEqualTo(1)
        assertThat(result[0].issueCount).isEqualTo(2)
        assertThat(result[0].reviewCount).isEqualTo(4)
    }

    @Test
    fun `getSummary - from と to が同じ日付のとき1件を返す`() {
        val date = LocalDate.of(2026, 5, 1)
        whenever(contributionMapper.findByUserAndDateRange(userId, date, date)).thenReturn(emptyList())

        val result = contributionService.getSummary(userId, date, date)

        assertThat(result).hasSize(1)
        assertThat(result[0].date).isEqualTo(date)
    }

    @Test
    fun `getSummary - 複数日付で各日のデータが正しく分割される`() {
        val day1 = LocalDate.of(2026, 5, 1)
        val day2 = LocalDate.of(2026, 5, 2)
        val contributions = listOf(
            contribution(day1, ContributionType.COMMIT, 2),
            contribution(day2, ContributionType.PR, 3),
        )
        whenever(contributionMapper.findByUserAndDateRange(userId, day1, day2)).thenReturn(contributions)

        val result = contributionService.getSummary(userId, day1, day2)

        assertThat(result).hasSize(2)
        assertThat(result[0].date).isEqualTo(day1)
        assertThat(result[0].commitCount).isEqualTo(2)
        assertThat(result[0].prCount).isEqualTo(0)
        assertThat(result[1].date).isEqualTo(day2)
        assertThat(result[1].prCount).isEqualTo(3)
    }

    // ---- getWeeklyTotal ----

    @Test
    fun `getWeeklyTotal - データなしのとき全項目0を返す`() {
        whenever(contributionMapper.findByUserAndDateRange(eq(userId), any(), any())).thenReturn(emptyList())

        val result = contributionService.getWeeklyTotal(userId)

        assertThat(result.commit).isEqualTo(0)
        assertThat(result.pr).isEqualTo(0)
        assertThat(result.issue).isEqualTo(0)
        assertThat(result.review).isEqualTo(0)
    }

    @Test
    fun `getWeeklyTotal - 各タイプの合計が正しく集計される`() {
        val today = LocalDate.now()
        val contributions = listOf(
            contribution(today, ContributionType.COMMIT, 5),
            contribution(today.minusDays(1), ContributionType.COMMIT, 3),
            contribution(today, ContributionType.PR, 2),
            contribution(today, ContributionType.REVIEW, 1),
        )
        whenever(contributionMapper.findByUserAndDateRange(eq(userId), any(), any())).thenReturn(contributions)

        val result = contributionService.getWeeklyTotal(userId)

        assertThat(result.commit).isEqualTo(8)
        assertThat(result.pr).isEqualTo(2)
        assertThat(result.issue).isEqualTo(0)
        assertThat(result.review).isEqualTo(1)
    }

    // ---- getLastSyncedAt ----

    @Test
    fun `getLastSyncedAt - 同期履歴がないとき null を返す`() {
        whenever(contributionMapper.findLastSyncedAt(userId)).thenReturn(null)

        val result = contributionService.getLastSyncedAt(userId)

        assertThat(result).isNull()
    }

    @Test
    fun `getLastSyncedAt - 同期履歴があるとき日時文字列を返す`() {
        val syncedAt = "2026-05-01T12:00:00+09:00"
        whenever(contributionMapper.findLastSyncedAt(userId)).thenReturn(syncedAt)

        val result = contributionService.getLastSyncedAt(userId)

        assertThat(result).isEqualTo(syncedAt)
    }

    // ---- helpers ----

    private fun contribution(date: LocalDate, type: ContributionType, count: Int) = Contribution(
        userId = userId,
        contributionDate = date,
        contributionType = type,
        count = count,
        syncedAt = now,
    )
}
