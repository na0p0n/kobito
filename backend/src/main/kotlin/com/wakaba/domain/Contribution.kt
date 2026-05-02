package com.wakaba.domain

import java.time.LocalDate
import java.time.OffsetDateTime
import java.util.UUID

enum class ContributionType { COMMIT, PR, ISSUE, REVIEW }

data class Contribution(
    val id: Long = 0,
    val userId: UUID,
    val contributionDate: LocalDate,
    val contributionType: ContributionType,
    val count: Int,
    val syncedAt: OffsetDateTime,
)
