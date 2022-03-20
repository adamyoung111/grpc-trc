package com.bizzan.bc.trc.config;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ActivitiConfig implements ApplicationContextAware {
    static ApplicationContext applicationContext;
    public static <T> T getBean(Class<T> clazz) {
        return applicationContext.getBean(clazz);
    }
    @Override
    public void setApplicationContext(ApplicationContext arg0) throws BeansException {
        if (applicationContext == null) {
            applicationContext = arg0;
        }
    }

}

