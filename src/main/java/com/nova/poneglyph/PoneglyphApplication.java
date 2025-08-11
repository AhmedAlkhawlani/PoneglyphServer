package com.nova.poneglyph;

import com.nova.poneglyph.user.enums.Active;
import com.nova.poneglyph.user.enums.Role;
import com.nova.poneglyph.model.User;
import com.nova.poneglyph.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
@EnableJpaAuditing

public class PoneglyphApplication implements CommandLineRunner {
    private final UserRepository userRepository;


    public PoneglyphApplication(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public static void main(String[] args) {
        SpringApplication.run(PoneglyphApplication.class, args);
    }
    @Override
    public void run(String... args) {
        final String pass = "$2a$10$W1ho9ap8zTrV83sbUUSm5uxAFZihQWPHseCf0HY/7HesQosm4EX7a";//987654321
        var admin = User.builder()
                .phoneNumber("775057134")
//                .username("admin")
                .email("admin@gmail.com")
                .password(pass)
                .active(Active.ACTIVE)
                .role(Role.ADMIN).build();
        if (userRepository.findByPhoneNumber("775057134").isEmpty()) userRepository.save(admin);
//        if (userRepository.findByUsername("admin").isEmpty()) userRepository.save(admin);
    }

}
