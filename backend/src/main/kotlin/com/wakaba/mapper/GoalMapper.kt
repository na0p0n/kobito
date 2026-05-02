package com.wakaba.mapper

import com.wakaba.domain.Goal
import org.apache.ibatis.annotations.*
import java.util.UUID

@Mapper
interface GoalMapper {

    @Select("""
        SELECT id, user_id, title, contribution_type, target_count, start_date, end_date, created_at
        FROM goals
        WHERE user_id = #{userId}
        ORDER BY created_at DESC
    """)
    fun findByUserId(@Param("userId") userId: UUID): List<Goal>

    @Select("""
        SELECT id, user_id, title, contribution_type, target_count, start_date, end_date, created_at
        FROM goals
        WHERE id = #{id} AND user_id = #{userId}
    """)
    fun findByIdAndUserId(@Param("id") id: Long, @Param("userId") userId: UUID): Goal?

    @Insert("""
        INSERT INTO goals (user_id, title, contribution_type, target_count, start_date, end_date, created_at)
        VALUES (#{userId}, #{title}, #{contributionType}, #{targetCount}, #{startDate}, #{endDate}, #{createdAt})
    """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    fun insert(goal: Goal): Int

    @Delete("DELETE FROM goals WHERE id = #{id} AND user_id = #{userId}")
    fun deleteByIdAndUserId(@Param("id") id: Long, @Param("userId") userId: UUID): Int
}
