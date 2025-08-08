package efive.tempodoro.service;

import static org.assertj.core.api.Assertions.*;

import java.util.Date;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;

class JwtServiceTest {

    private static final Date FIXED_DATE = new Date(1723032000000L);
    private final String secret = "test-secret-key";
    private final long expiration = 7200000L; // 2 hour
    private final String username = "testUser";

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
        String token = jwtService.generateToken(username);
        DecodedJWT decodedJWT = JWT.decode(token);

        assertThat(decodedJWT.getSubject()).isEqualTo(username);
        assertThat(decodedJWT.getIssuedAt()).isEqualTo(FIXED_DATE);
        assertThat(decodedJWT.getExpiresAt()).isEqualTo(new Date(FIXED_DATE.getTime() + expiration));
    }

    @Test
    void validateTokenAndGetUsername_shouldGetUsernameAndValidToken() {
        String token = JWT.create()
                .withSubject(username)
                .withIssuedAt(FIXED_DATE)
                .withExpiresAt(new Date(System.currentTimeMillis() + 3600000L)) // 1 hour from now
                .sign(Algorithm.HMAC256(secret));

        Optional<String> result = jwtService.validateTokenAndGetUsername(token);
        assertThat(result).contains(username);
    }

    @Test
    void validateTokenAndGetUsername_shouldReturnEmpyforInvalidToken() {
        String token = JWT.create()
                .withSubject(username)
                .withIssuedAt(FIXED_DATE)
                .withExpiresAt(new Date(System.currentTimeMillis() + 3600000L)) // 1 hour from now
                .sign(Algorithm.HMAC256(secret));
        String invalidToken = token + "string to make the token invalid";

        Optional<String> result = jwtService.validateTokenAndGetUsername(invalidToken);
        assertThat(result).isEmpty();
    }

    @Test
    void isTokenValid_shouldReturnTrue() {
        String token = JWT.create()
                .withSubject(username)
                .withIssuedAt(FIXED_DATE)
                .withExpiresAt(new Date(System.currentTimeMillis() + 3600000L)) // 1 hour from now
                .sign(Algorithm.HMAC256(secret));

        boolean result = jwtService.isTokenValid(token);
        assertThat(result).isTrue();
    }

    @Test
    void isTokenValid_shouldReturnFalse() {
        String token = JWT.create()
                .withSubject(username)
                .withIssuedAt(FIXED_DATE)
                .withExpiresAt(new Date(System.currentTimeMillis() + 3600000L)) // 1 hour from now
                .sign(Algorithm.HMAC256(secret));
        String invalidToken = token + "string to make the token invalid";

        boolean result = jwtService.isTokenValid(invalidToken);
        assertThat(result).isFalse();
    }
}
