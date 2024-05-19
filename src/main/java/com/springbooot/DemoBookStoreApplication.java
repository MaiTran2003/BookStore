package com.springbooot;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.springbooot.entities.Role;
import com.springbooot.entities.User;
import com.springbooot.repository.UserRepository;

@SpringBootApplication
public class DemoBookStoreApplication implements CommandLineRunner {

    private final UserRepository userRepository;
    
    public DemoBookStoreApplication(UserRepository userRepository) {
        super();
        this.userRepository = userRepository;
    }

    public static void main(String[] args) {
        SpringApplication.run(DemoBookStoreApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        User adminAccount = userRepository.findByRole(Role.ADMIN);
        if (adminAccount == null) {
            User user = new User();

            user.setEmail("tranthengocmaia14@gmail.com");
            user.setFirstname("admin");
            user.setLastname("admin");
            user.setRole(Role.ADMIN);
            user.setPassword(new BCryptPasswordEncoder().encode("admin"));
            user.setVerified(true);
            userRepository.save(user);
        }

    }

}
