package com.agendademais.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;

@ExtendWith(OutputCaptureExtension.class)
@SpringBootTest(classes = DevProfileSafetyCheck.class, webEnvironment = SpringBootTest.WebEnvironment.NONE, properties = {
        // trigger dev flags while NOT activating the 'dev' profile
        "app.security.requireAdmin=false",
        "app.reload-data=true",
        "app.dev.failOnMisconfig=false"
})
public class DevProfileSafetyCheckTest {

    @Test
    public void whenProdProfile_thenSafetyWarningsAreLogged(CapturedOutput output) {
        String logs = output.getOut() + output.getErr();
        // The DevProfileSafetyCheck should log warnings about permissive security and reload-data when not running with 'dev'
        org.assertj.core.api.Assertions.assertThat(logs)
                .contains("Security is configured permissively (app.security.requireAdmin=false)")
                .contains("app.reload-data=true is active while not running with the 'dev' profile");
    }
}
