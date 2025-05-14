package com.example.users.event;

import com.example.users.configs.RabbitMQConfig;
import com.example.users.dto.UserDTO;
import com.example.users.dto.UserEventDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class UserManagerProducer {

    private final RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.account.queue}")
    private String accountQueue;

    @Value("${rabbitmq.email.queue}")
    private String emailQueue;

    public UserManagerProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void sendMessage(UserDTO userDTO){

        log.info("Receive message create user: {}", userDTO);
        UserEventDTO userEventDTO = createUserEventDTO(userDTO);
        rabbitTemplate.convertAndSend(accountQueue, userEventDTO);
        rabbitTemplate.convertAndSend(emailQueue, userEventDTO);
        log.info("Successfully producing message: {}", userEventDTO);

    }

    private UserEventDTO createUserEventDTO(UserDTO userDTO) {
        UserEventDTO userEventDTO = new UserEventDTO();
        userEventDTO.setUserDTO(userDTO);
        return userEventDTO;
    }

}
