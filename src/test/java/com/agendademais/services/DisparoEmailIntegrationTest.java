package com.agendademais.services;

import com.icegreen.greenmail.junit5.GreenMailExtension;
import com.agendademais.testutil.GreenMailTestUtil;
import com.agendademais.entities.Instituicao;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import static org.junit.jupiter.api.Assertions.*;

public class DisparoEmailIntegrationTest {

    @RegisterExtension
    static GreenMailExtension greenMail = GreenMailTestUtil.createGreenMail();

    @Test
    public void institutionSenderSendsEmail() throws Exception {
        // Arrange: create institution with GreenMail SMTP settings
        Instituicao inst = new Instituicao();
        inst.setSmtpHost(GreenMailTestUtil.getHost());
        inst.setSmtpPort(GreenMailTestUtil.getPort());
        inst.setSmtpUsername("user@local");
        inst.setSmtpPassword("pass");
        inst.setSmtpSsl(false);

        // Ensure GreenMail knows the user so authentication succeeds
        greenMail.getUserManager().createUser("user@local", "user@local", "pass");

        // Build sender using the same helper logic as DisparoEmailService
        DisparoEmailService svc = new DisparoEmailService();
        try {
            java.lang.reflect.Field cryptoField = DisparoEmailService.class.getDeclaredField("cryptoService");
            cryptoField.setAccessible(true);
            cryptoField.set(svc, new CryptoService());
        } catch (Exception e) {
            fail(e);
        }

        java.lang.reflect.Method m = svc.getClass().getDeclaredMethod("buildSenderForInstitution",
                com.agendademais.entities.Instituicao.class);
        m.setAccessible(true);
        JavaMailSenderImpl sender = (JavaMailSenderImpl) m.invoke(svc, inst);

        // send a simple message
        jakarta.mail.internet.MimeMessage msg = sender.createMimeMessage();
        jakarta.mail.internet.InternetAddress[] to = { new jakarta.mail.internet.InternetAddress("to@local") };
        msg.setRecipients(jakarta.mail.Message.RecipientType.TO, to);
        msg.setSubject("Test Integration");
        msg.setText("Hello");

        sender.send(msg);

        // Assert GreenMail received
        MimeMessage[] received = greenMail.getReceivedMessages();
        assertEquals(1, received.length);
        assertEquals("Test Integration", received[0].getSubject());
    }
}
