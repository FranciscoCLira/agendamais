package com.agendademais.config;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@Component
public class DotenvLoader implements ApplicationListener<ApplicationEnvironmentPreparedEvent> {
    @Override
    public void onApplicationEvent(@NonNull ApplicationEnvironmentPreparedEvent event) {
        Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
        ConfigurableEnvironment env = event.getEnvironment();
        dotenv.entries().forEach(entry -> env.getSystemProperties().putIfAbsent(entry.getKey(), entry.getValue()));
    }
}
