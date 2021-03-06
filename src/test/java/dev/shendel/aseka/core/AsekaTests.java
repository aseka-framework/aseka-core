package dev.shendel.aseka.core;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.runner.RunWith;

@CucumberOptions(
        features = Constants.DEFAULT_FEATURES,
        tags = "@AlphaRelease",
//        tags = DEFAULT_TAGS,
        glue = Constants.DEFAULT_GLUE,
        plugin = {"pretty", Constants.ALLURE_PLUGIN}
)
@RunWith(Cucumber.class)
public class AsekaTests {
}
