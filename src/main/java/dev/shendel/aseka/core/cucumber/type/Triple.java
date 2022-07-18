package dev.shendel.aseka.core.cucumber.type;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor(staticName = "of")
public class Triple {

    private final String first;
    private final String second;
    private final String third;

}
