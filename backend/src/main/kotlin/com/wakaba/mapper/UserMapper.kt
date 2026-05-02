package com.wakaba.mapper

import org.apache.ibatis.annotations.Mapper
import org.apache.ibatis.annotations.Select
import java.util.UUID

data class UserAccount(val userId: UUID, val accessToken: String)

@Mapper
interface UserMapper {

    @Select("SELECT id FROM users")
    fun findAllUserIds(): List<UUID>

    @Select("""
        SELECT u.id AS user_id, a.access_token
        FROM users u
        INNER JOIN accounts a ON a.user_id = u.id
        WHERE a.provider = 'github'
          AND a.access_token IS NOT NULL
    """)
    fun findAllUsersWithAccessToken(): List<UserAccount>
}
