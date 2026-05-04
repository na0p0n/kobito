package com.wakaba.config

import org.apache.ibatis.type.UUIDTypeHandler
import org.mybatis.spring.boot.autoconfigure.ConfigurationCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestClient
import java.util.UUID

@Configuration
class AppBeans {
    @Bean
    fun restClient(): RestClient = RestClient.create()

    @Bean
    fun mybatisConfigurationCustomizer() = ConfigurationCustomizer { config ->
        config.isMapUnderscoreToCamelCase = true
        config.typeHandlerRegistry.register(UUID::class.java, UUIDTypeHandler::class.java)
    }
}
