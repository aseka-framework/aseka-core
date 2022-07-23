package dev.shendel.aseka.core.configuration;

import dev.shendel.aseka.core.api.InterpolationHelper;
import dev.shendel.aseka.core.api.InterpolatorHelpersSupplier;
import dev.shendel.aseka.core.matcher.object.ObjectMatcherHelper;
import dev.shendel.aseka.core.util.InterpolatorFunctions;
import dev.shendel.aseka.core.service.FileManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class InterpolationConfig {
    //TODO add registrator with method

    @Bean
    public InterpolatorHelpersSupplier defaultInterpolatorHelpers(@Lazy FileManager fileManager) {
        return () -> {
            List<InterpolationHelper> helpers = new ArrayList<>();
            helpers.add(InterpolationHelper.of("regex", InterpolatorFunctions.REGEX_GENERATOR));
            helpers.add(InterpolationHelper.of("randomLong", InterpolatorFunctions.RANDOM_LONG_GENERATOR));
            helpers.add(InterpolationHelper.of("date", InterpolatorFunctions.DATE_GENERATOR));
            helpers.add(InterpolationHelper.of("script", InterpolatorFunctions.SCRIPT_EXECUTOR));
            helpers.add(InterpolationHelper.of("env", InterpolatorFunctions.ENV_GETTER));
            helpers.add(InterpolationHelper.of("file", fileManager::readFileAsString));
            helpers.add(InterpolationHelper.of("base64Encoder", InterpolatorFunctions.BASE_64_ENCODER));
            helpers.add(InterpolationHelper.of("base64Decoder", InterpolatorFunctions.BASE_64_DECODER));
            return helpers;
        };
    }

    @Bean
    public InterpolatorHelpersSupplier matcherHelper() {
        return () -> {
            List<InterpolationHelper> helpers = new ArrayList<>();
            helpers.add(InterpolationHelper.of("matcher", ObjectMatcherHelper.getFunction()));
            return helpers;
        };
    }

}
