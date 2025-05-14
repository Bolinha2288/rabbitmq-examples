package com.example.email.integration;

import com.example.email.dto.UserDTO;
import com.example.email.dto.UserEventDTO;
import com.example.email.event.EmailManagerConsumer;
import com.example.email.utils.DispatchMail;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@SpringBootTest
@TestPropertySource(properties = {
        "spring.mail.host=smtp.example.com",
        "spring.mail.port=587",
        "spring.mail.username=seu_email@example.com",
        "spring.mail.password=sua_senha"
})
@ActiveProfiles("test")
@DirtiesContext
public class EmailManagerConsumerIntegrationTests {


    @MockitoBean
    private DispatchMail dispatchMail;

    @Autowired
    private EmailManagerConsumer emailManagerConsumer;

    private UserEventDTO userEventDTO;
    private UserDTO userDTO;

    @BeforeEach
    void setup() throws InterruptedException {
        TimeUnit.SECONDS.sleep(5);

        userDTO = new UserDTO();
        userDTO.setName("Eduardo");
        userDTO.setEmail("test@test.com.br");

        userEventDTO = new UserEventDTO();
        userEventDTO.setEventType("USER_CREATED");
        userEventDTO.setUserDTO(userDTO);
    }

    @Test
    void shouldProcessUserEventAndSendEmail() throws InterruptedException, ExecutionException {

        emailManagerConsumer.consumer(userEventDTO);

        String expectedRecipientEmail = userDTO.getEmail();
        String expectedSubject = "Welcome!";
        String expectedTemplateName = "welcome-user";
        Map<String, Object> expectedModel = Map.of(
                "userName", userDTO.getName(),
                "greeting", "Hellow, how are you?",
                "subject", expectedSubject
        );


        verify(dispatchMail, timeout(10000)).sendHtmlEmail(
                eq(expectedRecipientEmail),
                eq(expectedSubject),
                eq(expectedTemplateName),
                anyMap()
        );

        ArgumentCaptor<String> emailCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> subjectCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> templateCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Map<String, Object>> modelCaptor = ArgumentCaptor.forClass(Map.class);

        verify(dispatchMail, timeout(10000)).sendHtmlEmail(
                emailCaptor.capture(),
                subjectCaptor.capture(),
                templateCaptor.capture(),
                modelCaptor.capture()
        );

        assertEquals(expectedRecipientEmail, emailCaptor.getValue());
        assertEquals(expectedSubject, subjectCaptor.getValue());
        assertEquals(expectedTemplateName, templateCaptor.getValue());

        assertEquals(expectedModel.get("userName"), modelCaptor.getValue().get("userName"));
        assertEquals(expectedModel.get("greeting"), modelCaptor.getValue().get("greeting"));
        assertEquals(expectedModel.get("subject"), modelCaptor.getValue().get("subject"));

    }
}