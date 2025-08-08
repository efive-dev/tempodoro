package efive.tempodoro.service;

import java.util.Date;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;

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

    public Optional<String> validateTokenAndGetUsername(String token) {
        try {
            DecodedJWT decodedJWT = verifier.verify(token);
            return Optional.ofNullable(decodedJWT.getSubject());
        } catch (JWTVerificationException e) {
            return Optional.empty();
        }
    }

    public boolean isTokenValid(String token) {
        return validateTokenAndGetUsername(token).isPresent();
    }

}
