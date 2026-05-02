package com.wakaba.service

import com.wakaba.domain.Goal
import com.wakaba.domain.GoalWithProgress
import com.wakaba.mapper.ContributionMapper
import com.wakaba.mapper.GoalMapper
import org.springframework.stereotype.Service
import java.time.OffsetDateTime
import java.util.UUID

@Service
class GoalService(
    private val goalMapper: GoalMapper,
    private val contributionMapper: ContributionMapper,
) {
    fun getGoalsWithProgress(userId: UUID): List<GoalWithProgress> =
        goalMapper.findByUserId(userId).map { goal -> toGoalWithProgress(goal, userId) }

    fun createGoal(userId: UUID, goal: Goal): GoalWithProgress {
        require(goal.targetCount > 0) { "targetCount must be positive" }
        require(!goal.endDate.isBefore(goal.startDate)) { "endDate must not be before startDate" }
        val toInsert = goal.copy(userId = userId, createdAt = OffsetDateTime.now())
        goalMapper.insert(toInsert)
        return toGoalWithProgress(toInsert, userId)
    }

    fun deleteGoal(userId: UUID, goalId: Long): Boolean =
        goalMapper.deleteByIdAndUserId(goalId, userId) > 0

    private fun toGoalWithProgress(goal: Goal, userId: UUID): GoalWithProgress {
        val currentCount = contributionMapper.sumByUserAndTypeAndDateRange(
            userId, goal.contributionType, goal.startDate, goal.endDate
        )
        return GoalWithProgress(
            id = goal.id,
            userId = goal.userId,
            title = goal.title,
            contributionType = goal.contributionType,
            targetCount = goal.targetCount,
            startDate = goal.startDate,
            endDate = goal.endDate,
            currentCount = currentCount,
            achieved = currentCount >= goal.targetCount,
        )
    }
}
