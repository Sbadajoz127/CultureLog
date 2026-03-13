package com.cultureSL.CultureLog.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * Servicio de gestión de tokens JWT (JSON Web Tokens).
 * <p>
 * Proporciona métodos para generar, validar y extraer información de tokens JWT
 * utilizados en la autenticación stateless de la API. El token contiene el ID
 * del usuario como subject y el username como claim adicional.
 * </p>
 */
@Service
public class JwtService {

    /** Clave secreta para la firma HMAC-SHA del token. */
    @Value("${jwt.secret}")
    private String secret;

    /** Tiempo de expiración del token en milisegundos. */
    @Value("${jwt.expiration}")
    private long expirationMs;

    @PostConstruct
    private void validateSecret() {
        if (secret == null || secret.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalStateException(
                    "JWT_SECRET debe tener al menos 32 caracteres (256 bits) para HMAC-SHA256");
        }
    }

    /**
     * Genera un nuevo token JWT para un usuario autenticado.
     *
     * @param userId   ID del usuario (se almacena como subject)
     * @param username nombre de usuario (se almacena como claim)
     * @return token JWT firmado como cadena compacta
     */
    public String generateToken(Long userId, String username) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
                .subject(userId.toString())
                .claim("username", username)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * Extrae el ID del usuario del token JWT.
     *
     * @param token token JWT
     * @return ID del usuario
     */
    public Long extractUserId(String token) {
        return Long.parseLong(extractClaims(token).getSubject());
    }

    /**
     * Extrae el nombre de usuario del token JWT.
     *
     * @param token token JWT
     * @return nombre de usuario
     */
    public String extractUsername(String token) {
        return extractClaims(token).get("username", String.class);
    }

    /**
     * Verifica si un token JWT es válido (firma correcta y no expirado).
     *
     * @param token token JWT a validar
     * @return {@code true} si el token es válido y no ha expirado
     */
    public boolean isTokenValid(String token) {
        try {
            Claims claims = extractClaims(token);
            return !claims.getExpiration().before(new Date());
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Extrae todos los claims del token JWT tras verificar la firma.
     *
     * @param token token JWT
     * @return claims contenidos en el token
     */
    private Claims extractClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Genera la clave de firma HMAC-SHA a partir del secreto configurado.
     *
     * @return clave secreta para firma y verificación de tokens
     */
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }
}
