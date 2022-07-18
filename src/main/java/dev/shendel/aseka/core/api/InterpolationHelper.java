package dev.shendel.aseka.core.api;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.function.Function;

@Data
@AllArgsConstructor(staticName = "of")
public class InterpolationHelper {

    private String name;
    private Function<String, String> function;

}
