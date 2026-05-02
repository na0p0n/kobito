package com.wakaba.config

import org.mybatis.spring.boot.autoconfigure.ConfigurationCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestClient

@Configuration
class AppBeans {
    @Bean
    fun restClient(): RestClient = RestClient.create()

    @Bean
    fun mybatisConfigurationCustomizer() = ConfigurationCustomizer { config ->
        config.isMapUnderscoreToCamelCase = true
    }
}
