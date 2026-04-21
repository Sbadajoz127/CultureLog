package com.cultureSL.CultureLog.config;

import com.cultureSL.CultureLog.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        int updated = userRepository.setDefaultCreatedAtWhereNull();
        if (updated > 0) {
            log.info("Asignada fecha de registro (NOW) a {} usuarios existentes sin created_at", updated);
        }
    }
}
