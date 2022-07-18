package dev.shendel.aseka.core.configuration;

import com.google.common.collect.Lists;
import dev.shendel.aseka.core.util.Validator;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.cert.X509Certificate;
import java.util.List;

import static dev.shendel.aseka.core.configuration.AsekaProperties.Proxy.HTTPS_PROXY_HOST;
import static dev.shendel.aseka.core.configuration.AsekaProperties.Proxy.HTTPS_PROXY_PASSWORD;
import static dev.shendel.aseka.core.configuration.AsekaProperties.Proxy.HTTPS_PROXY_PORT;
import static dev.shendel.aseka.core.configuration.AsekaProperties.Proxy.HTTPS_PROXY_USER;
import static dev.shendel.aseka.core.configuration.AsekaProperties.Proxy.HTTP_NON_PROXY_HOSTS;
import static dev.shendel.aseka.core.configuration.AsekaProperties.Proxy.HTTP_PROXY_HOST;
import static dev.shendel.aseka.core.configuration.AsekaProperties.Proxy.HTTP_PROXY_PASSWORD;
import static dev.shendel.aseka.core.configuration.AsekaProperties.Proxy.HTTP_PROXY_PORT;
import static dev.shendel.aseka.core.configuration.AsekaProperties.Proxy.HTTP_PROXY_USER;

@Slf4j
@Configuration
@RequiredArgsConstructor
@ComponentScan(basePackages = {"dev.shendel.aseka.*"})
@EnableConfigurationProperties
@EnableAspectJAutoProxy
public class AsekaConfig {

    private final AsekaProperties properties;
    private final Environment environment;

    @EventListener(classes = {ContextRefreshedEvent.class})
    public void configureOnStartUp() {
        logCurrentEnvironment();
        setUpProxy();
        configureSslValidation();
        configureTrustStore();
    }

    private void logCurrentEnvironment() {
        List<String> activeProfiles = Lists.newArrayList(environment.getActiveProfiles());
        List<String> defaultProfiles = Lists.newArrayList(environment.getDefaultProfiles());

        if (activeProfiles.isEmpty()) {
            log.info("Tests will run in default '{}' environment", defaultProfiles);
        } else {
            log.info("Tests will run in '{}' environment", activeProfiles);
        }
    }

    private void setUpProxy() {
        if (!properties.getProxy().isEnabled()) {
            return;
        }

        String host = properties.getProxy().getHost();
        String port = properties.getProxy().getPort();
        String user = properties.getProxy().getUser();
        String password = properties.getProxy().getPassword();
        String nonProxyHosts = properties.getProxy().getNonProxyHosts();

        Validator.checkThatAllNotBlank("Wrong proxy settings", host, port, user, password, nonProxyHosts);

        System.setProperty(HTTP_PROXY_HOST, host);
        System.setProperty(HTTPS_PROXY_HOST, host);
        System.setProperty(HTTP_PROXY_PORT, port);
        System.setProperty(HTTPS_PROXY_PORT, port);
        System.setProperty(HTTP_PROXY_USER, user);
        System.setProperty(HTTPS_PROXY_USER, user);
        System.setProperty(HTTP_PROXY_PASSWORD, password);
        System.setProperty(HTTPS_PROXY_PASSWORD, password);
        System.setProperty(HTTP_NON_PROXY_HOSTS, nonProxyHosts);

        log.info("Found proxy settings: {}:{}", host, port);
    }

    @SneakyThrows
    private void configureSslValidation() {
        if (properties.isSslEnabled()) {
            return;
        }

        log.info("SSL validation disabled");
        TrustManager[] trustManagers = new TrustManager[]{
                new X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers() {
                        return new X509Certificate[0];
                    }

                    public void checkClientTrusted(X509Certificate[] certs, String authType) {
                        //ignore
                    }

                    public void checkServerTrusted(X509Certificate[] certs, String authType) {
                        //ignore
                    }
                }
        };

        SSLContext sslContext = SSLContext.getInstance("SSL");
        sslContext.init(null, trustManagers, new java.security.SecureRandom());
        HostnameVerifier verifier = (hostname, session) -> true;

        HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());
        HttpsURLConnection.setDefaultHostnameVerifier(verifier);
    }

    private void configureTrustStore() {
        String trustStore = properties.getTrustStore();
        if (StringUtils.isNotBlank(trustStore)) {
            log.info("Found trust store config:{}", trustStore);
            System.setProperty("javax.net.ssl.trustStore", trustStore);
        }
    }

}
