package com.wakaba

import org.mybatis.spring.annotation.MapperScan
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
@MapperScan("com.wakaba.mapper")
class WakabaApplication

fun main(args: Array<String>) {
    runApplication<WakabaApplication>(*args)
}
