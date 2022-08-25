package dev.crashteam.bigboss.config

import dev.crashteam.bigboss.converter.DataConverter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.context.support.ConversionServiceFactoryBean

@Configuration
@ComponentScan(
    basePackages = [
        "dev.crashteam.bigboss.converter",
    ]
)
class ConverterConfig {

    @Bean
    @Primary
    fun conversionServiceFactoryBean(
        dataConverters: Set<DataConverter<*, *>>,
    ): ConversionServiceFactoryBean {
        val conversionServiceFactoryBean = ConversionServiceFactoryBean()
        conversionServiceFactoryBean.setConverters(dataConverters)

        return conversionServiceFactoryBean
    }
}
