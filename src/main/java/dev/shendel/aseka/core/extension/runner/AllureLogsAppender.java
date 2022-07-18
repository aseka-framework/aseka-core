package dev.shendel.aseka.core.extension.runner;

import io.qameta.allure.Allure;
import io.qameta.allure.listener.TestLifecycleListener;
import io.qameta.allure.model.Attachment;
import io.qameta.allure.model.Link;
import io.qameta.allure.model.TestResult;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.UUID;

import static io.qameta.allure.AllureConstants.ATTACHMENT_FILE_SUFFIX;
import static io.qameta.allure.util.ResultsUtils.CUSTOM_LINK_TYPE;
import static org.apache.commons.lang3.StringUtils.isEmpty;

@Slf4j
@Service
@RequiredArgsConstructor
public class AllureLogsAppender implements TestLifecycleListener {

    private static final String TXT_EXTENSION = ".txt";
    private static final String TEXT_PLAIN = "text/plain";

    private final JavaAppRunnerExtension runner;

    @SneakyThrows
    public void afterTestStop(TestResult result) {
        addLinkToKibanaLogs(result);
        addAppLogs(result);
    }

    private void addLinkToKibanaLogs(TestResult result) {
        Link linkToLogs = new Link();
        linkToLogs.setName("Execution logs");
        linkToLogs.setType(CUSTOM_LINK_TYPE);
        linkToLogs.setUrl("https://logz.io/blog/logstash-tutorial");
        result.getLinks().add(linkToLogs);
    }

    private void addAppLogs(TestResult result) {
        InputStream logsIn = new ByteArrayInputStream(collectLogs().getBytes(StandardCharsets.UTF_8));
        Attachment attachment = createAttachment("APP LOGS", TEXT_PLAIN, TXT_EXTENSION);
        result.getAttachments().add(attachment);
        Allure.getLifecycle().writeAttachment(attachment.getSource(), logsIn);
    }

    @NotNull
    private String collectLogs() {
        final int size = runner.getAppLogsQueue().size();
        StringJoiner joiner = new StringJoiner("\n");
        for (int i = 0; i < size; i++) {
            joiner.add(runner.getAppLogsQueue().poll());
        }
        return joiner.toString();
    }

    public Attachment createAttachment(final String name, final String type, final String fileExtension) {
        final String extension = Optional.ofNullable(fileExtension)
                .filter(ext -> !ext.isEmpty())
                .map(ext -> ext.charAt(0) == '.' ? ext : "." + ext)
                .orElse("");
        final String source = UUID.randomUUID() + ATTACHMENT_FILE_SUFFIX + extension;

        return new Attachment()
                .setName(isEmpty(name) ? null : name)
                .setType(isEmpty(type) ? null : type)
                .setSource(source);
    }

}
