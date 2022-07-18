package dev.shendel.aseka.core.configuration;

import dev.shendel.aseka.core.extension.db.DatabaseType;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Data
@Configuration
@ConfigurationProperties(prefix = "aseka.database")
public class DatabaseProperties {

    private boolean enabled = false;
    private List<DataSource> dataSources = new ArrayList<>();

    @Data
    public static class DataSource {
        private String name;
        private boolean isDefault = false;
        private DatabaseType type;
        private String url;
        private String user;
        private String password;
    }

}
