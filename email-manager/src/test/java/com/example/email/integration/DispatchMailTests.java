package com.example.email.integration;

import com.example.email.utils.DispatchMail;
import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.ServerSetupTest;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.thymeleaf.TemplateEngine;

import jakarta.mail.internet.MimeMessage;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@DirtiesContext
public class DispatchMailTests {

    static GreenMail greenMail;

    @Autowired
    private DispatchMail dispatchMail;

    @Autowired
    private TemplateEngine templateEngine; // Para verificar o processamento do template (opcional, mas útil)

    @BeforeAll
    static void startGreenMail() {
        greenMail = new GreenMail(ServerSetupTest.SMTP);
        greenMail.start();
    }

    @AfterAll
    static void stopGreenMail() {
        greenMail.stop();
    }

    @DynamicPropertySource
    static void configureMail(DynamicPropertyRegistry registry) {
        registry.add("spring.mail.host", () -> greenMail.getSmtp().getServerSetup().getBindAddress());
        registry.add("spring.mail.port", () -> String.valueOf(greenMail.getSmtp().getPort()));
        registry.add("spring.mail.username", () -> "");
        registry.add("spring.mail.password", () -> "");
        registry.add("spring.mail.properties.mail.smtp.auth", () -> "false");
        registry.add("spring.mail.properties.mail.smtp.starttls.enable", () -> "false");
    }

    @Test
    void shouldSendHtmlEmailSuccessfully() throws Exception {
        // Arrange
        String to = "eduardo-borsato@hotmail.com";
        String subject = "Test Email";
        String templateName = "test-template";
        Map<String, Object> model = Map.of("name", "Test User");

        // Simula o template processado (opcional, para verificar o TemplateEngine)
        String expectedHtmlBody = templateEngine.process(templateName, new org.thymeleaf.context.Context(null, model));

        // Act
        dispatchMail.sendHtmlEmail(to, subject, templateName, model);

        // Assert
        greenMail.waitForIncomingEmail(1);
        MimeMessage[] receivedMessages = greenMail.getReceivedMessages();
        assertEquals(1, receivedMessages.length);

        MimeMessage receivedMessage = receivedMessages[0];
        assertEquals(subject, receivedMessage.getSubject());
        assertEquals(to, receivedMessage.getAllRecipients()[0].toString());
        assertTrue(receivedMessage.getContentType().contains("multipart/mixed")); // Ou multipart/related dependendo do conteúdo
        assertEquals(1, receivedMessages.length, "Um e-mail deveria ter sido enviado");
    }

}
