package dev.shendel.aseka.core;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.Ignore;
import org.junit.runner.RunWith;

@Ignore
@CucumberOptions(
        features = Constants.DEFAULT_FEATURES,
        tags = "@ReadyForTest",
//        tags = DEFAULT_TAGS,
        glue = Constants.DEFAULT_GLUE,
        plugin = {"pretty", Constants.ALLURE_PLUGIN}
)
@RunWith(Cucumber.class)
public class AsekaTests {
}
