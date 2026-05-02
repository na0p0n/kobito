package com.wakaba.domain

import java.time.LocalDate
import java.time.OffsetDateTime
import java.util.UUID

data class Goal(
    val id: Long = 0,
    val userId: UUID,
    val title: String,
    val contributionType: ContributionType,
    val targetCount: Int,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val createdAt: OffsetDateTime = OffsetDateTime.now(),
)

data class GoalWithProgress(
    val id: Long,
    val userId: UUID,
    val title: String,
    val contributionType: ContributionType,
    val targetCount: Int,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val currentCount: Int,
    val achieved: Boolean,
)
