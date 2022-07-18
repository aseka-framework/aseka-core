package dev.shendel.aseka.core.configuration;

import dev.shendel.aseka.core.service.StringInterpolatorImpl;
import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(
        classes = AsekaConfig.class,
        properties = {
                "spring.main.banner-mode=off",
                "spring.profiles.default=default",
                StringInterpolatorImpl.EMPTY_STRING_PLACEHOLDER_PROPERTY
        }
)
@CucumberContextConfiguration
public class CucumberSpringConfig {
}