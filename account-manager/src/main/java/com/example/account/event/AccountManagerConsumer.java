package com.example.account.event;

import com.example.account.domain.model.Account;
import com.example.account.domain.repository.AccountRepository;
import com.example.account.dto.UserEventDTO;
import com.example.account.utils.AccountNumber;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class AccountManagerConsumer {

    private final AccountRepository accountRepository;
    private final AccountNumber accountNumber;

    public AccountManagerConsumer(AccountRepository accountRepository, AccountNumber accountNumber) {
        this.accountRepository = accountRepository;
        this.accountNumber = accountNumber;
    }

    @RabbitListener(queues = "${rabbitmq.account.queue}")
    public void consumeEvent(UserEventDTO userEventDTO) {
        log.info("User event received in account-manager service {}", userEventDTO);

        Account account = new Account();
        account.setUserReference(userEventDTO.getUserDTO().getIdReference());
        account.setNumberAccount(accountNumber.generate(userEventDTO.getUserDTO().getIdReference()));
        account.setName(userEventDTO.getUserDTO().getName());

        accountRepository.save(account);

        log.info("Account created successfully {}", account);
    }

}
