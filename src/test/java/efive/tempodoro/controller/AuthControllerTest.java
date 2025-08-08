package efive.tempodoro.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import efive.tempodoro.dto.LoginRequest;
import efive.tempodoro.dto.RegisterRequest;
import efive.tempodoro.entity.User;
import efive.tempodoro.service.AuthService;

@ExtendWith(MockitoExtension.class)
public class AuthControllerTest {

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper = new ObjectMapper();
    private String username = "testUsername";
    private String password = "testPassword";
    private String mockToken = "mockedToken";

    private User user = User.builder()
            .username(username)
            .password(password)
            .build();

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(authController).build();
    }

    @Test
    void login_shouldReturnTokenSuccess() throws JsonProcessingException, Exception {
        when(authService.login(username, password)).thenReturn(Optional.of(mockToken));

        LoginRequest request = new LoginRequest(username, password);

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value(mockToken));
    }

    @Test
    void login_shouldErrorInvalidCredentials() throws JsonProcessingException, Exception {
        when(authService.login(username, password)).thenReturn(Optional.empty());

        LoginRequest request = new LoginRequest(username, password);

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Invalid credentials"));
    }

    @Test
    void register_shouldWorkOkSuccess() throws JsonProcessingException, Exception {
        when(authService.register(username, password)).thenReturn(Optional.of(user));

        RegisterRequest request = new RegisterRequest(username, password);

        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("User registered successfully"));
    }

    @Test
    void register_shouldFailWhenUsernameExists() throws JsonProcessingException, Exception {
        when(authService.register(username, password)).thenReturn(Optional.empty());

        RegisterRequest request = new RegisterRequest(username, password);

        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Username already exists"));
    }

}