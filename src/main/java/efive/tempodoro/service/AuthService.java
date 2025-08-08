package efive.tempodoro.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import efive.tempodoro.entity.User;
import efive.tempodoro.repository.UserRepository;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    public Optional<String> login(String username, String password) {
        return userRepository.findByUsername(username)
                .filter(user -> passwordEncoder.matches(password, user.getPassword()))
                .map(user -> jwtService.generateToken(username));
    }

    public Optional<User> register(String username, String password) {
        return userRepository.findByUsername(username)
                .isEmpty() ? Optional.of(createAndSaveUser(username, password)) : Optional.empty();
    }

    private User createAndSaveUser(String username, String password) {
        String encodedPassword = passwordEncoder.encode(password);

        User user = User.builder()
                .username(username)
                .password(encodedPassword)
                .build();

        return userRepository.save(user);
    }
}
