package dev.shendel.aseka.core.cucumber.hooks;

import dev.shendel.aseka.core.api.Cleanable;
import dev.shendel.aseka.core.api.Extension;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.OrderComparator;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class SpringListeners {

    private final List<Cleanable> cleanableBins;
    private final List<Extension> extensions;

    @EventListener(classes = {FeatureEndEvent.class})
    public void cleanContextWhenFeatureEnds() {
        log.info("Clearing context on FeatureEndEvent...");
        cleanableBins.forEach(Cleanable::clean);
    }

    @EventListener(classes = {ApplicationStartedEvent.class})
    public void initExtensions() {
        log.info(
                "Found {} extensions: {}",
                extensions.size(),
                extensions.stream()
                        .map(extension -> extension.getClass().getSimpleName())
                        .collect(Collectors.toList())
        );
        log.info("Start initializing extensions...");
        long start = System.currentTimeMillis();
        extensions.forEach(Extension::init);
        long stop = System.currentTimeMillis();
        log.info("Completed initialization extensions in {} ms", stop - start);
    }

    @PreDestroy
    public void destroyExtensions() {
        log.info("Destroying extensions");
        cleanableBins.forEach(Cleanable::clean);
        extensions.stream().sorted(OrderComparator.INSTANCE.reversed()).forEach(Extension::destroy);
        log.info("All extensions destroyed");
    }

}
