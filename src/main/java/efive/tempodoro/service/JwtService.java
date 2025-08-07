package efive.tempodoro.service;

import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;

import jakarta.annotation.PostConstruct;
import lombok.AccessLevel;
import lombok.Setter;

@Service
public class JwtService {

    @Setter(AccessLevel.PACKAGE)
    @Value("${jwt.secret}")
    String secret;

    @Setter(AccessLevel.PACKAGE)
    @Value("${jwt.expiration}")
    Long expiration;

    private JWTVerifier verifier;
    private Algorithm algorithm;

    @PostConstruct
    public void init() {
        if (secret == null || secret.isEmpty()) {
            throw new IllegalStateException("JWT secret is not configured.");
        }
        algorithm = Algorithm.HMAC256(secret);
        verifier = JWT.require(algorithm).build();
    }

    protected Date now() {
        return new Date();
    }

    public String generateToken(String username) {
        Date now = now();
        Date expiresAt = new Date(now.getTime() + expiration);

        return JWT.create()
                .withSubject(username)
                .withIssuedAt(now)
                .withExpiresAt(expiresAt)
                .sign(algorithm);
    }
}
