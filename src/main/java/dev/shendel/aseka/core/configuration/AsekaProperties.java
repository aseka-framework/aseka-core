package dev.shendel.aseka.core.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@Configuration
@ConfigurationProperties(prefix = "aseka")
public class AsekaProperties {

    private final Map<String, String> variables = new HashMap<>();

    private boolean sslEnabled = false;
    private String trustStore = "";
    private Proxy proxy = new Proxy();

    private boolean deprecatedFeaturesEnabled = false;

    //TODO add extension order comparator
    private List<String> extensionsOrder = new ArrayList<>();

    public Map<String, String> getDefaultVariables() {
        return variables;
    }

    @Data
    public static class Proxy {
        public static final String HTTP_PROXY_HOST = "http.proxyHost";
        public static final String HTTPS_PROXY_HOST = "https.proxyHost";
        public static final String HTTP_PROXY_PORT = "http.proxyPort";
        public static final String HTTPS_PROXY_PORT = "https.proxyPort";
        public static final String HTTP_PROXY_USER = "http.proxyUser";
        public static final String HTTPS_PROXY_USER = "https.proxyUser";
        public static final String HTTP_PROXY_PASSWORD = "http.proxyPassword";
        public static final String HTTPS_PROXY_PASSWORD = "https.proxyPassword";
        public static final String HTTP_NON_PROXY_HOSTS = "http.nonProxyHosts";

        private boolean enabled = false;
        private String host;
        private String port;
        private String user;
        private String password;
        private String nonProxyHosts;
    }

}