package com.agendademais.config;

import java.util.Arrays;

import jakarta.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * Startup-time safety checks to warn when development-only flags are enabled
 * while the application is not running with the {@code dev} profile.
 */
@Component
public class DevProfileSafetyCheck {

    private static final Logger log = LoggerFactory.getLogger(DevProfileSafetyCheck.class);

    private final Environment env;

    @Value("${app.security.requireAdmin:true}")
    private boolean requireAdmin;

    @Value("${app.reload-data:false}")
    private boolean reloadData;

    @Value("${app.dev.failOnMisconfig:false}")
    private boolean failOnMisconfig;

    public DevProfileSafetyCheck(Environment env) {
        this.env = env;
    }

    @PostConstruct
    public void checkDevFlags() {
        String[] active = env.getActiveProfiles();
        boolean isDev = Arrays.asList(active).contains("dev");

        if (isDev) {
            log.debug("Running with 'dev' profile active; dev-only features are allowed.");
            return;
        }

        // Warn if permissive/dev flags are enabled while NOT in 'dev' profile
        boolean misconfigured = false;
        if (!requireAdmin) {
            misconfigured = true;
            log.warn("Security is configured permissively (app.security.requireAdmin=false) "
                    + "but application is not running with the 'dev' profile. This exposes admin endpoints without authentication.");
        }

        if (reloadData) {
            misconfigured = true;
            log.warn("app.reload-data=true is active while not running with the 'dev' profile. "
                    + "This may perform destructive data reloads on application startup.");
        }

        if (!misconfigured) {
            log.info(
                    "No dev flags detected, but application is running without the 'dev' profile (active profiles: {}).",
                    (Object) active);
        }

        if (misconfigured && failOnMisconfig) {
            throw new IllegalStateException(
                    "Dev-only configuration flags are active while not running with the 'dev' profile. Aborting startup because app.dev.failOnMisconfig=true");
        }
    }
}
