package com.wakaba.mapper

import com.wakaba.domain.AppConfig
import org.apache.ibatis.annotations.*

@Mapper
interface AppConfigMapper {

    @Select("SELECT key, value, updated_at FROM app_config WHERE key = #{key}")
    fun findByKey(@Param("key") key: String): AppConfig?

    @Select("SELECT key, value, updated_at FROM app_config")
    fun findAll(): List<AppConfig>

    @Update("UPDATE app_config SET value = #{value}, updated_at = NOW() WHERE key = #{key}")
    fun update(@Param("key") key: String, @Param("value") value: String?): Int
}
