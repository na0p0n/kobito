package com.wakaba.controller

import com.wakaba.domain.ContributionType
import com.wakaba.domain.Goal
import com.wakaba.domain.GoalWithProgress
import com.wakaba.service.GoalService
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Positive
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.LocalDate
import java.util.UUID

data class CreateGoalRequest(
    @field:NotBlank val title: String = "",
    val contributionType: ContributionType = ContributionType.PR,
    @field:Positive val targetCount: Int = 1,
    val startDate: LocalDate = LocalDate.now(),
    val endDate: LocalDate = LocalDate.now(),
)

@RestController
@RequestMapping("/api/goals")
class GoalController(private val goalService: GoalService) {

    @GetMapping
    fun list(@RequestHeader("X-User-Id") userId: UUID): ResponseEntity<Map<String, List<GoalWithProgress>>> {
        val goals = goalService.getGoalsWithProgress(userId)
        return ResponseEntity.ok(mapOf("goals" to goals))
    }

    @PostMapping
    fun create(
        @RequestHeader("X-User-Id") userId: UUID,
        @Valid @RequestBody req: CreateGoalRequest,
    ): ResponseEntity<GoalWithProgress> {
        val goal = Goal(
            userId = userId,
            title = req.title,
            contributionType = req.contributionType,
            targetCount = req.targetCount,
            startDate = req.startDate,
            endDate = req.endDate,
        )
        return ResponseEntity.ok(goalService.createGoal(userId, goal))
    }

    @DeleteMapping("/{id}")
    fun delete(
        @RequestHeader("X-User-Id") userId: UUID,
        @PathVariable id: Long,
    ): ResponseEntity<Void> {
        val deleted = goalService.deleteGoal(userId, id)
        return if (deleted) ResponseEntity.noContent().build() else ResponseEntity.notFound().build()
    }
}
