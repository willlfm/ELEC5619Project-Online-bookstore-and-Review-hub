package ELEC5619_Practical2_Group_5.bookstore.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseCookie;
import org.springframework.mock.web.MockHttpServletRequest;

import java.lang.reflect.Constructor;
import java.time.Instant;
import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilsTest {

    private JwtUtils newUtils() throws Exception {
        for (Constructor<?> c : JwtUtils.class.getDeclaredConstructors()) {
            var pts = c.getParameterTypes();
            if (pts.length == 3 && pts[0] == String.class && pts[1] == String.class
                    && (pts[2] == long.class || pts[2] == Long.class)) {
                c.setAccessible(true);
                return (JwtUtils) c.newInstance("0123456789ABCDEF0123456789ABCDEF", "test-issuer", 60L);
            }
        }
        throw new IllegalStateException("The JwtUtils constructor signature has changed.");
    }

    @Test
    void generateToken_ThenParse_RoundTripsClaims() throws Exception {
        JwtUtils utils = newUtils();
        List<String> roles = List.of("ROLE_USER", "ROLE_ADMIN");

        String token = utils.generateToken("alice", roles);
        Jws<Claims> jws = utils.parseAndValidate(token);
        Claims c = jws.getBody();

        assertEquals("alice", JwtUtils.getUsername(c));
        assertEquals("test-issuer", c.getIssuer());
        assertEquals(roles, JwtUtils.getRoles(c));
        assertTrue(c.getExpiration().toInstant().isAfter(Instant.now()));
    }

    @Test
    void buildAuthCookie_SetsExpectedAttributes() throws Exception {
        JwtUtils utils = newUtils();
        ResponseCookie ck = utils.buildAuthCookie("jwt-123", true);

        assertEquals(JwtUtils.COOKIE_NAME, ck.getName());
        assertEquals("jwt-123", ck.getValue());
        assertTrue(ck.isHttpOnly());
        assertTrue(ck.isSecure());
        assertEquals("/", ck.getPath());
        assertTrue(ck.toString().contains("SameSite=Lax"));
    }

    @Test
    void clearAuthCookie_ClearsValue_AndSetMaxAgeZero() throws Exception {
        JwtUtils utils = newUtils();
        ResponseCookie ck = utils.clearAuthCookie(false);

        assertEquals(JwtUtils.COOKIE_NAME, ck.getName());
        assertEquals("", ck.getValue());
        assertTrue(ck.isHttpOnly());
        assertFalse(ck.isSecure());
        assertEquals("/", ck.getPath());
        assertTrue(ck.toString().contains("SameSite=Lax"));
        assertEquals(Duration.ZERO, ck.getMaxAge());
    }

    @Test
    void extractUsernameFromRequest_PrefersCookie() throws Exception {
        JwtUtils utils = newUtils();
        String cookieJwt = utils.generateToken("alice", List.of("ROLE_USER"));
        String headerJwt = utils.generateToken("bob", List.of());

        MockHttpServletRequest req = new MockHttpServletRequest();
        req.setCookies(new Cookie(JwtUtils.COOKIE_NAME, cookieJwt));
        req.addHeader("Authorization", "Bearer " + headerJwt);

        assertEquals("alice", utils.extractUsernameFromRequest(req));
    }

    @Test
    void extractUsernameFromRequest_FallsBackToAuthorizationHeader() throws Exception {
        JwtUtils utils = newUtils();
        String headerJwt = utils.generateToken("carol", List.of());

        MockHttpServletRequest req = new MockHttpServletRequest();
        req.addHeader("Authorization", "Bearer " + headerJwt);

        assertEquals("carol", utils.extractUsernameFromRequest(req));
    }

    @Test
    void extractUsernameFromRequest_ReturnsNull_WhenInvalidOrMissing() throws Exception {
        JwtUtils utils = newUtils();

        MockHttpServletRequest none = new MockHttpServletRequest();
        assertNull(utils.extractUsernameFromRequest(none));

        MockHttpServletRequest bad = new MockHttpServletRequest();
        bad.addHeader("Authorization", "Bearer not-a-jwt");
        assertNull(utils.extractUsernameFromRequest(bad));
    }

    @Test
    void extractUsernameFromRequest_CookiesPresentButNoAuthCookie_FallsBackToHeader() throws Exception {
        JwtUtils utils = newUtils();

        String headerJwt = utils.generateToken("fallback", java.util.List.of("ROLE_USER"));

        org.springframework.mock.web.MockHttpServletRequest req = new org.springframework.mock.web.MockHttpServletRequest();
        req.setCookies(new jakarta.servlet.http.Cookie("OTHER", "x"));
        req.addHeader("Authorization", "Bearer " + headerJwt);

        org.junit.jupiter.api.Assertions.assertEquals("fallback", utils.extractUsernameFromRequest(req));
    }

    @Test
    void extractUsernameFromRequest_AuthCookieWithEmptyValue_FallsBackToHeader() throws Exception {
        JwtUtils utils = newUtils();

        String headerJwt = utils.generateToken("from-header", java.util.List.of());

        org.springframework.mock.web.MockHttpServletRequest req = new org.springframework.mock.web.MockHttpServletRequest();
        req.setCookies(new jakarta.servlet.http.Cookie(JwtUtils.COOKIE_NAME, ""));
        req.addHeader("Authorization", "Bearer " + headerJwt);

        org.junit.jupiter.api.Assertions.assertEquals("from-header", utils.extractUsernameFromRequest(req));
    }

    @Test
    void extractUsernameFromRequest_HeaderPresentButNotBearer_ReturnsNull() throws Exception {
        JwtUtils utils = newUtils();
        org.springframework.mock.web.MockHttpServletRequest req = new org.springframework.mock.web.MockHttpServletRequest();
        req.addHeader("Authorization", "Basic abcdefg");
        org.junit.jupiter.api.Assertions.assertNull(utils.extractUsernameFromRequest(req));
    }

    @Test
    void extractUsernameFromRequest_InvalidJwtInCookie_ReturnsNull() throws Exception {
        JwtUtils utils = newUtils();

        org.springframework.mock.web.MockHttpServletRequest req = new org.springframework.mock.web.MockHttpServletRequest();
        req.setCookies(new jakarta.servlet.http.Cookie(JwtUtils.COOKIE_NAME, "not-a-jwt"));

        org.junit.jupiter.api.Assertions.assertNull(utils.extractUsernameFromRequest(req));
    }
}
