package com.infrastructure.http.listener;

import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.PropertySource;

import java.util.Properties;

public class FeignConfigListener implements ApplicationListener<ApplicationEnvironmentPreparedEvent> {

    @Override
    public void onApplicationEvent(ApplicationEnvironmentPreparedEvent event) {
        PropertySource propertySource = null;
        if ((propertySource = event.getEnvironment().getPropertySources().get("infraEnvironment")) == null){
            return;
        }

        Properties properties = (Properties)(propertySource.getSource());
        properties.put("feign.okhttp.enabled", event.getEnvironment().containsProperty("feign.okhttp.enabled") ?
                event.getEnvironment().getProperty("feign.okhttp.enabled") : true);
        if (event.getEnvironment().containsProperty("infra.http.connect-timeout")){
            properties.put("feign.client.config.default.connect-timeout",
                    event.getEnvironment().getProperty("infra.http.connect-timeout"));
            properties.put("ribbon.ConnectTimeout",
                    event.getEnvironment().getProperty("infra.http.connect-timeout"));

        }
        if (event.getEnvironment().containsProperty("infra.http.read-timeout")){
            properties.put("feign.client.config.default.read-timeout",
                    event.getEnvironment().getProperty("infra.http.read-timeout"));
            properties.put("ribbon.ReadTimeout",
                    event.getEnvironment().getProperty("infra.http.read-timeout"));
        }
    }
}
