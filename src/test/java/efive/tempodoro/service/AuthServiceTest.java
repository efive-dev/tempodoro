package efive.tempodoro.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import efive.tempodoro.entity.User;
import efive.tempodoro.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    private String username = "testUser";
    private String rawPassword = "rawPassword";
    private String encodedPassword = "encodedPassword";
    private String mockedToken = "mockedToken";

    private User user = User.builder()
            .username(username)
            .password(encodedPassword)
            .build();

    @Test
    void login_shouldReturnTokenWhenCredentialsAreValid() {
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(rawPassword, encodedPassword)).thenReturn(true);
        when(jwtService.generateToken(username)).thenReturn(mockedToken);

        Optional<String> token = authService.login(username, rawPassword);

        assertThat(token).contains("mockedToken");
        verify(jwtService).generateToken("testUser");
    }

    @Test
    void login_shouldReturnEmpytWhenCredentialAreInvalid() {
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongPassword", encodedPassword)).thenReturn(false);

        Optional<String> token = authService.login(username, "wrongPassword");
        assertThat(token).isEmpty();
        verify(jwtService, never()).generateToken(anyString());
    }

    @Test
    void register_shouldSaveAndReturnUserWhenUserDoesNotExist() {
        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());
        when(passwordEncoder.encode(rawPassword)).thenReturn(encodedPassword);
        when(userRepository.save(any(User.class))).thenReturn(user);

        Optional<User> result = authService.register(username, rawPassword);
        assertThat(result).isPresent();
        assertThat(result.get().getUsername()).isEqualTo(username);
        assertThat(result.get().getPassword()).isEqualTo(encodedPassword);

        verify(userRepository).findByUsername(username);
        verify(passwordEncoder).encode(rawPassword);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_shouldReturnEmpytWhenUsernameAlreadyExists() {
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));

        Optional<User> result = authService.register(username, "anyPassword");
        assertThat(result).isEmpty();

        verify(userRepository).findByUsername(username);
        verify(passwordEncoder, never()).encode(rawPassword);
        verify(userRepository, never()).save(any(User.class));
    }
}
