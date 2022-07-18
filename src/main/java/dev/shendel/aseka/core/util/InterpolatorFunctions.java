package dev.shendel.aseka.core.util;

import com.github.javafaker.Faker;
import com.github.javafaker.service.FakeValuesService;
import com.github.javafaker.service.RandomService;
import dev.shendel.aseka.core.exception.AsekaException;
import lombok.experimental.UtilityClass;
import org.apache.commons.text.lookup.StringLookupFactory;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.function.Function;

import static java.util.Locale.ENGLISH;

@UtilityClass
public final class InterpolatorFunctions {

    public static final Function<String, String> REGEX_GENERATOR = new RegexGenerator();
    public static final Function<String, String> RANDOM_LONG_GENERATOR = new RandomLongGenerator();
    public static final Function<String, String> DATE_GENERATOR = new DateGenerator();
    public static final Function<String, String> SCRIPT_EXECUTOR = new ScriptExecutor();
    public static final Function<String, String> ENV_GETTER = new EnvironmentGetter();
    public static final Function<String, String> BASE_64_ENCODER = new Base64Encoder();
    public static final Function<String, String> BASE_64_DECODER = new Base64Decoder();

    private static final RandomService RANDOM_SERVICE = new RandomService();
    private static final FakeValuesService FAKE_VALUES_SERVICE = new FakeValuesService(ENGLISH, new RandomService());
    private static final Faker FAKER = new Faker();


    public static class RegexGenerator implements Function<String, String> {
        @Override
        public String apply(String regex) {
            return FAKE_VALUES_SERVICE.regexify(regex);
        }
    }

    public static class RandomLongGenerator implements Function<String, String> {
        @Override
        public String apply(String range) {
            try {
            //TODO add work with custom range
            //ThreadLocalRandom.current().nextLong(m, n)
                return String.valueOf(RANDOM_SERVICE.nextLong(Long.parseLong(range)));
            } catch (Exception exception) {
                throw new AsekaException("Error during generating long from range 0-{}", range);
            }
        }
    }

    public static class DateGenerator implements Function<String, String> {
        @Override
        public String apply(String datePattern) {
            return StringLookupFactory.INSTANCE.dateStringLookup().lookup(datePattern);
        }
    }

    public static class ScriptExecutor implements Function<String, String> {
        @Override
        public String apply(String script) {
            return StringLookupFactory.INSTANCE.scriptStringLookup().lookup(script);
        }
    }

    public static class EnvironmentGetter implements Function<String, String> {
        @Override
        public String apply(String key) {
            return StringLookupFactory.INSTANCE.environmentVariableStringLookup().lookup(key);
        }
    }

    public static class Base64Encoder implements Function<String, String> {
        @Override
        public String apply(String content) {
            return Base64.getEncoder().encodeToString(content.getBytes(StandardCharsets.UTF_8));
        }
    }

    public static class Base64Decoder implements Function<String, String> {
        @Override
        public String apply(String content) {
            return new String(Base64.getDecoder().decode(content), StandardCharsets.UTF_8);
        }
    }

}
