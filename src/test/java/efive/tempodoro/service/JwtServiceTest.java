package efive.tempodoro.service;

import static org.assertj.core.api.Assertions.*;

import java.util.Date;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;

class JwtServiceTest {

    private static final Date FIXED_DATE = new Date(1723032000000L);
    private final String secret = "test-secret-key";
    private final long expiration = 7200000L; // 2 hour

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService() {
            @Override
            protected Date now() {
                return FIXED_DATE;
            }
        };
        jwtService.setSecret(secret);
        jwtService.setExpiration(expiration);
        jwtService.init();
    }

    @Test
    void init_ShouldThrowExceptionSecretNotValid() {
        jwtService.setSecret(null);
        assertThatThrownBy(() -> jwtService.init())
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("JWT secret is not configured.");

        jwtService.setSecret("");
        assertThatThrownBy(() -> jwtService.init())
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("JWT secret is not configured.");

    }

    @Test
    void generateToken_shouldProduceCorrectToken() {
        String username = "testUser";

        String token = jwtService.generateToken(username);
        DecodedJWT decodedJWT = JWT.decode(token);

        assertThat(decodedJWT.getSubject()).isEqualTo(username);
        assertThat(decodedJWT.getIssuedAt()).isEqualTo(FIXED_DATE);
        assertThat(decodedJWT.getExpiresAt()).isEqualTo(new Date(FIXED_DATE.getTime() + expiration));
    }
}
