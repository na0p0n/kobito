package com.wakaba.mapper

import com.wakaba.domain.Contribution
import com.wakaba.domain.ContributionType
import org.apache.ibatis.annotations.*
import java.time.LocalDate
import java.util.UUID

@Mapper
interface ContributionMapper {

    @Insert("""
        INSERT INTO contributions (user_id, contribution_date, contribution_type, count, synced_at)
        VALUES (#{userId}, #{contributionDate}, #{contributionType}, #{count}, #{syncedAt})
        ON CONFLICT (user_id, contribution_date, contribution_type)
        DO UPDATE SET count = EXCLUDED.count, synced_at = EXCLUDED.synced_at
    """)
    fun upsert(contribution: Contribution)

    @Select("""
        SELECT id, user_id, contribution_date, contribution_type, count, synced_at
        FROM contributions
        WHERE user_id = #{userId}
          AND contribution_date BETWEEN #{from} AND #{to}
        ORDER BY contribution_date
    """)
    fun findByUserAndDateRange(
        @Param("userId") userId: UUID,
        @Param("from") from: LocalDate,
        @Param("to") to: LocalDate,
    ): List<Contribution>

    @Select("""
        SELECT COALESCE(SUM(count), 0)
        FROM contributions
        WHERE user_id = #{userId}
          AND contribution_type = #{type}
          AND contribution_date BETWEEN #{from} AND #{to}
    """)
    fun sumByUserAndTypeAndDateRange(
        @Param("userId") userId: UUID,
        @Param("type") type: ContributionType,
        @Param("from") from: LocalDate,
        @Param("to") to: LocalDate,
    ): Int

    @Select("""
        SELECT MAX(synced_at) FROM contributions WHERE user_id = #{userId}
    """)
    fun findLastSyncedAt(@Param("userId") userId: UUID): String?
}
