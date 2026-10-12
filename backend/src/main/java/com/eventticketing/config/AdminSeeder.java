package com.eventticketing.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.eventticketing.entity.User;
import com.eventticketing.repository.UserRepository;

/**
 * Creates a demo admin and a demo user on first start so you can log in right away.
 * Public registration always creates USER accounts, so without this there would be no ADMIN.
 */
@Component
@org.springframework.core.annotation.Order(1)
public class AdminSeeder implements CommandLineRunner {

    private final UserRepository users;
    private final PasswordEncoder encoder;

    public AdminSeeder(UserRepository users, PasswordEncoder encoder) {
        this.users = users;
        this.encoder = encoder;
    }

    @Override
    public void run(String... args) {
        seed("admin@demo.com", "Admin", "Admin@123", "ADMIN");
        seed("gate@demo.com", "Gary Gate", "Gate@123", "GATE_STAFF");
        seed("customer@demo.com", "Cathy Customer", "Customer@123", "USER");
    }

    private void seed(String email, String name, String password, String role) {
        if (users.findByEmail(email).isPresent()) {
            return;
        }
        User u = new User();
        u.setEmail(email);
        u.setName(name);
        u.setPassword(encoder.encode(password));
        u.setRole(role);
        users.save(u);
    }
}
