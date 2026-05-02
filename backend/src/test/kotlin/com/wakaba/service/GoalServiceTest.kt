package com.wakaba.service

import com.wakaba.domain.ContributionType
import com.wakaba.domain.Goal
import com.wakaba.mapper.ContributionMapper
import com.wakaba.mapper.GoalMapper
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*
import java.time.LocalDate
import java.time.OffsetDateTime
import java.util.UUID

class GoalServiceTest {

    private val goalMapper: GoalMapper = mock()
    private val contributionMapper: ContributionMapper = mock()
    private lateinit var goalService: GoalService

    private val userId = UUID.randomUUID()
    private val today = LocalDate.of(2026, 5, 1)

    @BeforeEach
    fun setUp() {
        goalService = GoalService(goalMapper, contributionMapper)
    }

    // ---- getGoalsWithProgress ----

    @Test
    fun `getGoalsWithProgress - ゴールが0件のときは空リストを返す`() {
        whenever(goalMapper.findByUserId(userId)).thenReturn(emptyList())

        val result = goalService.getGoalsWithProgress(userId)

        assertThat(result).isEmpty()
    }

    @Test
    fun `getGoalsWithProgress - currentCount が targetCount 未満のとき achieved は false`() {
        val goal = goal(targetCount = 5, type = ContributionType.PR)
        whenever(goalMapper.findByUserId(userId)).thenReturn(listOf(goal))
        whenever(
            contributionMapper.sumByUserAndTypeAndDateRange(
                userId, ContributionType.PR, goal.startDate, goal.endDate
            )
        ).thenReturn(3)

        val result = goalService.getGoalsWithProgress(userId)

        assertThat(result).hasSize(1)
        assertThat(result[0].currentCount).isEqualTo(3)
        assertThat(result[0].achieved).isFalse()
    }

    @Test
    fun `getGoalsWithProgress - currentCount が targetCount に等しいとき achieved は true`() {
        val goal = goal(targetCount = 10, type = ContributionType.COMMIT)
        whenever(goalMapper.findByUserId(userId)).thenReturn(listOf(goal))
        whenever(
            contributionMapper.sumByUserAndTypeAndDateRange(
                userId, ContributionType.COMMIT, goal.startDate, goal.endDate
            )
        ).thenReturn(10)

        val result = goalService.getGoalsWithProgress(userId)

        assertThat(result[0].achieved).isTrue()
    }

    @Test
    fun `getGoalsWithProgress - currentCount が targetCount を超えても achieved は true`() {
        val goal = goal(targetCount = 5, type = ContributionType.ISSUE)
        whenever(goalMapper.findByUserId(userId)).thenReturn(listOf(goal))
        whenever(
            contributionMapper.sumByUserAndTypeAndDateRange(any(), any(), any(), any())
        ).thenReturn(8)

        val result = goalService.getGoalsWithProgress(userId)

        assertThat(result[0].achieved).isTrue()
    }

    // ---- createGoal ----

    @Test
    fun `createGoal - 正常に作成されると GoalWithProgress を返す`() {
        val goal = goal(targetCount = 3)
        whenever(goalMapper.insert(any())).thenReturn(1)
        whenever(
            contributionMapper.sumByUserAndTypeAndDateRange(any(), any(), any(), any())
        ).thenReturn(0)

        val result = goalService.createGoal(userId, goal)

        assertThat(result.title).isEqualTo(goal.title)
        assertThat(result.targetCount).isEqualTo(3)
        verify(goalMapper).insert(any())
    }

    @Test
    fun `createGoal - targetCount が 0 のとき IllegalArgumentException をスローする`() {
        val goal = goal(targetCount = 0)

        assertThatThrownBy { goalService.createGoal(userId, goal) }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("targetCount")
    }

    @Test
    fun `createGoal - targetCount が負のとき IllegalArgumentException をスローする`() {
        val goal = goal(targetCount = -1)

        assertThatThrownBy { goalService.createGoal(userId, goal) }
            .isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun `createGoal - endDate が startDate より前のとき IllegalArgumentException をスローする`() {
        val goal = goal().copy(startDate = today, endDate = today.minusDays(1))

        assertThatThrownBy { goalService.createGoal(userId, goal) }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("endDate")
    }

    @Test
    fun `createGoal - startDate と endDate が同日でも有効`() {
        val goal = goal().copy(startDate = today, endDate = today, targetCount = 1)
        whenever(goalMapper.insert(any())).thenReturn(1)
        whenever(contributionMapper.sumByUserAndTypeAndDateRange(any(), any(), any(), any())).thenReturn(0)

        val result = goalService.createGoal(userId, goal)

        assertThat(result.startDate).isEqualTo(result.endDate)
    }

    // ---- deleteGoal ----

    @Test
    fun `deleteGoal - 削除対象が存在するとき true を返す`() {
        whenever(goalMapper.deleteByIdAndUserId(1L, userId)).thenReturn(1)

        val result = goalService.deleteGoal(userId, 1L)

        assertThat(result).isTrue()
    }

    @Test
    fun `deleteGoal - 削除対象が存在しないとき false を返す`() {
        whenever(goalMapper.deleteByIdAndUserId(999L, userId)).thenReturn(0)

        val result = goalService.deleteGoal(userId, 999L)

        assertThat(result).isFalse()
    }

    @Test
    fun `deleteGoal - 他ユーザーのゴールは削除できない`() {
        val otherUserId = UUID.randomUUID()
        whenever(goalMapper.deleteByIdAndUserId(1L, otherUserId)).thenReturn(0)

        val result = goalService.deleteGoal(otherUserId, 1L)

        assertThat(result).isFalse()
    }

    // ---- helpers ----

    private fun goal(
        targetCount: Int = 5,
        type: ContributionType = ContributionType.PR,
    ) = Goal(
        id = 1L,
        userId = userId,
        title = "テストゴール",
        contributionType = type,
        targetCount = targetCount,
        startDate = today,
        endDate = today.plusDays(30),
        createdAt = OffsetDateTime.now(),
    )
}
