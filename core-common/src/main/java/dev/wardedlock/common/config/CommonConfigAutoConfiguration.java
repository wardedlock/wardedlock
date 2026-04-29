package dev.wardedlock.common.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@AutoConfiguration
@EnableConfigurationProperties({
        CacheProperties.class,
        SecurityProperties.class,
        RateLimitProperties.class
})
public class CommonConfigAutoConfiguration {
}
