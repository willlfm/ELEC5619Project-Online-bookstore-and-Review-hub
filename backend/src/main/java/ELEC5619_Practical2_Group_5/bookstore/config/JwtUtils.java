package ELEC5619_Practical2_Group_5.bookstore.config;


import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Component
public class JwtUtils {

    public static final String COOKIE_NAME = "BOOKSTORE_AUTH";
    private static final String CLAIM_ROLES = "roles";

    private final SecretKey key;
    private final String issuer;
    private final long expMinutes;

    public JwtUtils(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.issuer:bookstore}") String issuer,
            @Value("${jwt.exp-minutes:120}") long expMinutes
    ) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.issuer = issuer;
        this.expMinutes = expMinutes;
    }

    public String generateToken(String username, List<String> roles) {
        Instant now = Instant.now();
        Instant exp = now.plusSeconds(expMinutes * 60);
        return Jwts.builder()
                .setSubject(username)
                .setIssuer(issuer)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(exp))
                .addClaims(Map.of(CLAIM_ROLES, roles))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public Jws<Claims> parseAndValidate(String jwt) throws JwtException {
        return Jwts.parserBuilder()
                .requireIssuer(issuer)
                .setSigningKey(key)
                .build()
                .parseClaimsJws(jwt);
    }

    public static String getUsername(Claims claims) {
        return claims.getSubject();
    }

    @SuppressWarnings("unchecked")
    public static List<String> getRoles(Claims claims) {
        Object v = claims.get(CLAIM_ROLES);
        return v instanceof List<?> list ? (List<String>) list : List.of();
    }

    public ResponseCookie buildAuthCookie(String token, boolean secure) {
        return ResponseCookie.from(COOKIE_NAME, token)
                .httpOnly(true)
                .secure(secure)
                .path("/")
                .sameSite("Lax")
                .build();
    }

    public ResponseCookie clearAuthCookie(boolean secure) {
        return ResponseCookie.from(COOKIE_NAME, "")
                .httpOnly(true)
                .secure(secure)
                .path("/")
                .sameSite("Lax")
                .maxAge(0)
                .build();
    }


    public String extractUsernameFromRequest(jakarta.servlet.http.HttpServletRequest request) {
        try {
            jakarta.servlet.http.Cookie[] cookies = request.getCookies();
            if (cookies != null) {
                for (jakarta.servlet.http.Cookie cookie : cookies) {
                    if (COOKIE_NAME.equals(cookie.getName())) {
                        String token = cookie.getValue();
                        if (token != null && !token.isEmpty()) {
                            Jws<Claims> claims = parseAndValidate(token);
                            return getUsername(claims.getBody());
                        }
                    }
                }
            }

            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                Jws<Claims> claims = parseAndValidate(token);
                return getUsername(claims.getBody());
            }

            return null;
        } catch (JwtException e) {
            return null;
        }
    }


}
