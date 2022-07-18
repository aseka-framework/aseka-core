package dev.shendel.aseka.core.cucumber.type;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor(staticName = "of")
public class Pair {

    private final String first;
    private final String second;

}
