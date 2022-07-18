package dev.shendel.aseka.core.allure;

import org.jetbrains.annotations.NotNull;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class SpringContext implements ApplicationContextAware {

    private static ConfigurableApplicationContext context;

    static <T> Map<String, T> getBeans(Class<T> beanClass) {
        if (context == null || !context.isActive()) {
            return new HashMap<>();
        } else {
            return context.getBeansOfType(beanClass);
        }
    }

    @Override
    public void setApplicationContext(@NotNull ApplicationContext applicationContext) throws BeansException {
        SpringContext.context = (ConfigurableApplicationContext) applicationContext;
    }

}
