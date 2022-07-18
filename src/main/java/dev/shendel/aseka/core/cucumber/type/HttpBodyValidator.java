package dev.shendel.aseka.core.cucumber.type;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.hamcrest.Matcher;

@Getter
@AllArgsConstructor
public class HttpBodyValidator {

    private final String gpath;
    private final Matcher<?> matcher;

}
