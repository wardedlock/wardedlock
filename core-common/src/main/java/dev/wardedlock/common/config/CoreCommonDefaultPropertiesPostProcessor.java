package dev.wardedlock.common.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.util.List;

/**
 * EnvironmentPostProcessor that automatically loads 'core-common-default.yml' 
 * and appends it to the end of the environment property sources.
 * This provides default configuration values for all microservices in the Monorepo, 
 * while allowing individual microservice 'application.yml' or 'application-test.yml' 
 * files to cleanly override these defaults.
 */
public class CoreCommonDefaultPropertiesPostProcessor implements EnvironmentPostProcessor {

    private final YamlPropertySourceLoader loader = new YamlPropertySourceLoader();

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        try {
            // 1. If 'test' profile is active, load test defaults first (giving them higher priority than main defaults)
            if (environment.acceptsProfiles(org.springframework.core.env.Profiles.of("test"))) {
                Resource testResource = new ClassPathResource("core-common-default-test.yml");
                if (testResource.exists()) {
                    List<PropertySource<?>> testSources = loader.load("core-common-default-test", testResource);
                    for (PropertySource<?> testSource : testSources) {
                        environment.getPropertySources().addLast(testSource);
                    }
                }
            }

            // 2. Load main defaults last (giving them the absolute lowest priority)
            Resource defaultResource = new ClassPathResource("core-common-default.yml");
            if (defaultResource.exists()) {
                List<PropertySource<?>> mainSources = loader.load("core-common-default", defaultResource);
                for (PropertySource<?> source : mainSources) {
                    environment.getPropertySources().addLast(source);
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load default configurations", e);
        }
    }
}
