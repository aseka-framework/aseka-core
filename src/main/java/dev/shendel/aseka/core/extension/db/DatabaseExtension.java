package dev.shendel.aseka.core.extension.db;

import dev.shendel.aseka.core.api.Extension;

import java.util.List;
import java.util.Map;

public interface DatabaseExtension extends Extension {

    void init();

    void destroy();

    void clean();

    void setScenarioDataSource(String dataSourceName);

    List<Map<String, Object>> executeSelect(String sqlScript);

   void execute(String sqlScript);

}
