package com.example.users.integration.event;

import com.example.users.controller.UserController;
import com.example.users.domain.repository.UserRepository;
import com.example.users.dto.UserDTO;
import com.example.users.dto.UserEventDTO;
import com.example.users.event.UserManagerProducer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.test.RabbitListenerTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ImportAutoConfiguration(
        exclude = {
                DataSourceAutoConfiguration.class,
                HibernateJpaAutoConfiguration.class
        }
)
@Testcontainers
@ActiveProfiles("test")
@RabbitListenerTest
public class UserManagerProducerTests {

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private UserController userController;

    @Value("${rabbitmq.account.queue}")
    private String accountQueue;

    @Value("${rabbitmq.email.queue}")
    private String emailQueue;


    @Container
    static final RabbitMQContainer rabbitMQContainer = new RabbitMQContainer("rabbitmq:3.12-management");

    @DynamicPropertySource
    static void configureRabbitProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.rabbitmq.host", rabbitMQContainer::getHost);
        registry.add("spring.rabbitmq.port", rabbitMQContainer::getAmqpPort);
    }

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private UserManagerProducer userManagerProducer;

    @BeforeEach
    void setup() {
        rabbitTemplate.setReceiveTimeout(2000);
    }

    @Test
    void shouldSendUserEventDTOToQueues() {
        UserDTO user = new UserDTO();
        user.setName("Edu");
        user.setEmail("edu@teste.com");

        userManagerProducer.sendMessage(user);

        UserEventDTO fromAccountQueue = rabbitTemplate.receiveAndConvert(accountQueue, new ParameterizedTypeReference<UserEventDTO>() {});
        UserEventDTO fromEmailQueue = rabbitTemplate.receiveAndConvert(emailQueue, new ParameterizedTypeReference<UserEventDTO>() {});

        assertThat(fromAccountQueue).isNotNull();
        assertThat(fromAccountQueue.getUserDTO().getName()).isEqualTo("Edu");

        assertThat(fromEmailQueue).isNotNull();
        assertThat(fromEmailQueue.getUserDTO().getEmail()).isEqualTo("edu@teste.com");
    }
}
