package com.example.users.integration.service;

import com.example.users.configs.RabbitMQConfig;
import com.example.users.domain.model.User;
import com.example.users.domain.repository.UserRepository;
import com.example.users.dto.UserDTO;
import com.example.users.dto.UserEventDTO;
import com.example.users.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
public class UserServiceIntegrationTests {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.account.queue}")
    private String accountQueue;

    @Value("${rabbitmq.email.queue}")
    private String emailQueue;

    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("testdb")
            .withUsername("testuser")
            .withPassword("testpass");

    @Container
    static RabbitMQContainer rabbitmq = new RabbitMQContainer("rabbitmq:3.12-management");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        // MySQL
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);
        registry.add("spring.datasource.driver-class-name", mysql::getDriverClassName);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "update");

        // RabbitMQ
        registry.add("spring.rabbitmq.host", rabbitmq::getHost);
        registry.add("spring.rabbitmq.port", rabbitmq::getAmqpPort);
    }

    @BeforeEach
    void setup() {
        userRepository.deleteAll();
    }

    @Test
    void shouldPersistUserAndSendToQueues() {
        // Arrange
        UserDTO userDTO = new UserDTO();
        userDTO.setName("edu");
        userDTO.setEmail("edu@teste.com");

        // Act
        userService.createUser(userDTO);

        // Assert: DB
        var allUsers = userRepository.findAll();
        assertThat(allUsers).hasSize(1);
        User saved = allUsers.get(0);
        assertThat(saved.getName()).isEqualTo("edu");
        assertThat(saved.getEmail()).isEqualTo("edu@teste.com");

        // Assert: RabbitMQ
        rabbitTemplate.setReceiveTimeout(3000);

        // Modificação aqui para usar ParameterizedTypeReference
        UserEventDTO fromAccountQueue = rabbitTemplate.receiveAndConvert(accountQueue, new ParameterizedTypeReference<UserEventDTO>() {});
        UserEventDTO fromEmailQueue = rabbitTemplate.receiveAndConvert(emailQueue, new ParameterizedTypeReference<UserEventDTO>() {});

        assertThat(fromAccountQueue).isNotNull();
        assertThat(fromAccountQueue.getUserDTO().getName()).isEqualTo("edu");

        assertThat(fromEmailQueue).isNotNull();
        assertThat(fromEmailQueue.getUserDTO().getEmail()).isEqualTo("edu@teste.com");
    }
}
