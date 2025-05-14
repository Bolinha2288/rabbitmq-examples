package com.example.email.utils;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.nio.charset.StandardCharsets;
import java.util.Map;


@Component
@Slf4j
public class DispatchMail {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    public DispatchMail(JavaMailSender mailSender, TemplateEngine templateEngine) {
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
    }

    public void sendHtmlEmail(String to, String subject, String templateName, Map<String, Object> model) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED, StandardCharsets.UTF_8.name());

            Context thymeleafContext = new Context();
            thymeleafContext.setVariables(model);

            String htmlBody = templateEngine.process(templateName, thymeleafContext);

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);

            mailSender.send(message);
            log.info("Send e-mail successfully to {}", to);

        } catch (MessagingException e) {
            log.error("Fail send e-mail to: {} - {}", to, e.getMessage());
        }
    }
}
